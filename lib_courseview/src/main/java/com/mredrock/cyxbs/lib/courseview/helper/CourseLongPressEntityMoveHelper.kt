package com.mredrock.cyxbs.lib.courseview.helper

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.ILongPressEntityMove.LongPressState.*
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.VibratorUtil
import com.mredrock.cyxbs.lib.courseview.utils.lazyUnlock
import kotlin.math.*

/**
 * ```
 * 该类作用：
 * 1、封装长按整体移动的功能
 * 2、以帮助类的形式给 CourseLayout 的某个 item 添加整体移动功能
 *
 * 使用方式：
 * 1、在 isAdvanceIntercept() 中调用 mHelper#isAdvanceIntercept()，
 *    如果返回 true，则 isAdvanceIntercept() 也要返回 true
 *    注：如果不返回 true，则会影响后面事件的接收，想强行停止可以调用 forceCancel()
 * 2、在 onTouchEvent() 中调用 mHelper#onTouchEvent()
 *
 * 该类设计：
 * 1、对 CourseLayout 增加事件监听来实现
 * 2、事件监听参考了 RV 的 ItemTouchHelper 的设计
 * 3、支持子 View 设置 onClickListener，与它互相兼容
 *
 * 注意事项：
 * 1、对于长按的那个 View 在激活长按时是会被移除父布局的，在移动动画结束后会重新添加（如果不移除父布局就会造成很多因重新布局大小改变的问题）
 * 2、该类只管理长按整体移动，请不要添加一些不属于该类的功能，想添加功能应该再写一个 OnCourseTouchListener
 * 3、该类侵入性很低，可以随便给 CourseLayout 下某个 View 添加长按移动功能，但与当前只有 Noon 和 Dusk 时间段展开和折叠耦合
 * 4、尤其注意 isAdvanceIntercept() 和 onTouchEvent() 方法的处理，不然不能实现长按整体移动
 *
 * 这个类的代码量有点小多，主要是长按松手时的逻辑判断和一些细节处理，虽然我不敢保证，应该是没什么很影响用户体验的 bug，
 * 整体设计也是考虑了很多，只要以后需求不涉及到长按整体移动，基本上是不用修改这个类的
 * ```
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/10 15:31
 * @param openLongPress 返回 true 则为这个 View 开启长按整体移动功能
 */
class CourseLongPressEntityMoveHelper(
    private val course: CourseLayout,
    private val openLongPress: (child: View) -> Boolean
) : OnItemTouchListener, ItemDecoration, ILongPressEntityMove {

    /**
     * 设置长按移动监听
     */
    override fun setEntityMoveListener(l: ILongPressEntityMove.OnEntityMoveListener) {
        mOnEntityMoveListener = l
    }

    /**
     * 用于移动到半途中想强行停止移动
     *
     * 该方法会直接结束动画
     */
    override fun forceEnd() {
        if (mLongPressState >= OVER) return
        val animation = mMoveAnimation
        if (animation == null) {
            // 这里说明还没有抬手
            val view = mLongPressView
            if (view == null) {
                // 这里说明长按没有被激活
                course.removeCallbacks(mLongPressRunnable)
            } else {
                // 这里说明长按被激活了但还没抬手
                mScrollRunnable.cancel()
                course.removeView(mSubstituteView)
                if (mIsInOverlay) {
                    course.overlay.remove(view)
                    course.addView(view)
                }
                mLongPressView = null
            }
        } else {
            // 这里说明已经抬手在进行最后的动画
            animation.end()
        }
    }

    /**
     * 得到是否是替身 View
     *
     * 在长按激活后，按中的 View 会被移除父布局，然后被替身 View 代替位置
     */
    override fun isSubstituteView(child: View?): Boolean {
        return child === mSubstituteView
    }

    /**
     * 得到当前长按整体移动的状态
     */
    override fun getLongPressState(): ILongPressEntityMove.LongPressState {
        return mLongPressState
    }

    init {
        // 监听 course 的绘图
        course.addCourseDecoration(this)
    }

    private var mOnEntityMoveListener: ILongPressEntityMove.OnEntityMoveListener? = null

    private var mIsNoonFoldedLongPressStart = true // 长按开始时中午时间段是否处于折叠状态
    private var mIsDuskFoldedLongPressStart = true // 长按开始时傍晚时间段是否处于折叠状态
    private var mIsNeedFoldNoon = false // 是否需要折叠中午时间段
    private var mIsNeedFoldDusk = false // 是否需要折叠傍晚时间段
    private var mIsContainNoonLongPressStart = false // 长按开始时自身是否包含中午时间段
    private var mIsContainDuskLongPressStart = false // 长按开始时自身是否包含傍晚时间段

    private var mLongPressView: View? = null // 长按的那个 View 的引用
    private var mIsInOverlay = false // mLongPressView 是否处于 overlay 中

    private var mLongPressState = OVER // 长按状态

    private var mMoveAnimation: MoveAnimation? = null // 抬手后回到原位置或者移动到新位置的动画

    // Down 时触摸点到 LongPressView 顶部的距离
    private var mDistanceDownToViewTop = 0

    // LongPressView 的替身 View，用于提前占位，防止点击穿透。在长按激活时就会被添加到 course 中
    private val mSubstituteView by lazyUnlock {
        object : View(course.context) {

            private var mOnNextLayoutCallback: ((View) -> Unit)? = null

            /**
             * 设置紧接着下一次布局后的回调
             *
             * 因为动画的回调提前于布局的回调，所以在有些情况下需要得到重新布局后的高度值才能开启动画
             */
            fun setOnNextLayoutCallback(call: (View) -> Unit) {
                mOnNextLayoutCallback = call
            }

            override fun layout(l: Int, t: Int, r: Int, b: Int) {
                super.layout(l, t, r, b)
                mOnNextLayoutCallback?.invoke(this)
                mOnNextLayoutCallback = null
            }
        }
    }

    // 替身 View 的 lp
    private val mSubstituteLp = CourseLayoutParams(0, 0, 0, CourseType.UNKNOWN)

    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    // 识别为长按所需的时间
    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    // 是否处于长按状态
    private var mIsInLongPress = false

    // 长按时执行的 Runnable
    private val mLongPressRunnable = Runnable {
        mLongPressState = START
        mIsInLongPress = true
        mLongPressView!!.translationZ = 6F // 给 LongPressView 加些阴影
        VibratorUtil.start(course.context, 36) // 长按被触发来个震动提醒
        val lp = mLongPressView!!.layoutParams as CourseLayoutParams
        // 记录长按开始时的中午状态
        mIsNoonFoldedLongPressStart = when (course.getNoonRowState()) {
            RowState.FOLD, RowState.UNFOLD_ANIM -> true
            RowState.UNFOLD, RowState.FOLD_ANIM -> false
        }
        // 记录长按开始时的傍晚状态
        mIsDuskFoldedLongPressStart = when (course.getDuskRowState()) {
            RowState.FOLD, RowState.UNFOLD_ANIM -> true
            RowState.UNFOLD, RowState.FOLD_ANIM -> false
        }
        mIsContainNoonLongPressStart = CourseLayout.isContainNoon(lp)
        mIsContainDuskLongPressStart = CourseLayout.isContainDusk(lp)
        // 如果需要就自动展开中午和傍晚时间段
        unfoldNoonOrDuskIfNecessary(mLongPressView!!)
        putViewIntoOverlayIfCan() // 只能放在展开动画后
        // 回调监听
        mOnEntityMoveListener?.onLongPressStart(mLongPressView!!, lp)

        // 用一个透明的 View 去代替 LongPressView 的位置，因为使用 overlay 会使 View 被移除
        // 这里还有一个原因，防止在回到正确位置的动画中点击导致穿透
        course.addCourse(mSubstituteView, mSubstituteLp.copy(lp))
    }

    override fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val child = course.findItemUnderByXY(x, y)
                if (child == mSubstituteView || child == null) return false
                if (openLongPress.invoke(child)) { // 询问哪种 View 可以移动
                    mLongPressState = DOWN
                    mLongPressView = child
                    mIsInLongPress = false // 重置
                    mIsNeedFoldNoon = false // 重置
                    mIsNeedFoldDusk = false // 重置
                    mIsInOverlay = false // 重置
                    mDistanceDownToViewTop = y - child.top

                    course.postDelayed(mLongPressRunnable, mLongPressTimeout)
                    // 禁止外面的 ScrollView 拦截事件
                    course.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mLongPressView != null) {
                    if (mIsInLongPress) {
                        return true
                    } else {
                        /*
                        * LongPressView 的点击事件是在 UP 中触发，
                        * 1、如果超过 mTouchSlop，则直接取消拦截（后面会被 CourseScrollView 拦截）
                        * 2、如果在 mTouchSlop 范围内，且也处于长按状态，则直接拦截
                        *
                        * 这里使用 mCourseScrollView 的原因：
                        * 1、存在因为时间段扩展而使偏移量也改变，所以只能使用绝对坐标系来计算真正的手指偏移量
                        * */
                        if (abs(course.mCourseScrollView.mDiffMoveX) > mTouchSlop
                            || abs(course.mCourseScrollView.mDiffMoveY) > mTouchSlop
                        ) {
                            course.removeCallbacks(mLongPressRunnable)
                            mLongPressView = null // 重置
                            // 允许外面的 ScrollView 拦截事件
                            course.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mLongPressView == null) return false
                mLongPressState = UP
                // 走到这里说明没有达到长按的要求：没有移动或者没到长按时间就松手
                if (mIsInLongPress) {
                    restoreAffairViewToOldLocation()
                } else {
                    course.removeCallbacks(mLongPressRunnable)
                }
                mLongPressView = null // 重置
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mLongPressView == null) return false
                mLongPressState = CANCEL
                // 走到这里说明：父布局或者前面的 onCourseTouchListener 拦截了事件
                if (mIsInLongPress) {
                    restoreAffairViewToOldLocation()
                } else {
                    course.removeCallbacks(mLongPressRunnable)
                }
                mLongPressView = null // 重置
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 注意：这里 Down 事件是不会被调用的
                // 原因：因为是在 Move 中才拦截的事件，不是在 Down 时拦截的
            }
            MotionEvent.ACTION_MOVE -> {
                putViewIntoOverlayIfCan()
                scrollIsNecessary()
                course.invalidate() // 直接重绘，交给 onDrawAbove() 处理移动
            }
            MotionEvent.ACTION_UP -> {
                mLongPressState = UP
                changeLocationIfNecessary()
                mScrollRunnable.cancel()
                mLongPressView = null // 重置
            }
            MotionEvent.ACTION_CANCEL -> {
                mLongPressState = CANCEL
                restoreAffairViewToOldLocation()
                mScrollRunnable.cancel()
                mLongPressView = null // 重置
            }
        }
    }

    /**
     * 作用：在没有进行动画的时候放进 overlay
     *
     * 原因：主要因为存在这种情况，当该 View 的中间部分包含了正处于展开或折叠时的中午(傍晚)时间段，
     * 这个时候添加进 overlay 将会缺少中午(傍晚)时间段的高度
     */
    private fun putViewIntoOverlayIfCan(
        noonState: RowState = course.getNoonRowState(),
        duskState: RowState = course.getDuskRowState()
    ) {
        if (!mIsInOverlay) { // 用于判断只调用一次
            if (mIsContainNoonLongPressStart
                && (noonState == RowState.FOLD_ANIM || noonState == RowState.UNFOLD_ANIM)
            ) {
                return // 开始包含中午时间段，且此时中午时间段又处于动画中
            }
            if (mIsContainDuskLongPressStart
                && (duskState == RowState.FOLD_ANIM || duskState == RowState.UNFOLD_ANIM)
            ) {
                return // 开始包含傍晚时间段，且此时傍晚时间段又处于动画中
            }
            /*
            * overlay 是一个很神奇的东西，有了这个东西就可以防止布局对 View 的影响，
            * 而且仍可以在父布局中显示
            * 这个相当于是在父布局顶层专门绘制，View 的位置不会受到
            * 父布局重新布局的影响
            * */
            mLongPressView?.let {
                mIsInOverlay = true
                course.overlay.add(it)
            }
        }
    }

    /**
     * 平移 LongPressView
     */
    private fun translateView(view: View) {
        mLongPressState = MOVE
        // 使用 CourseScrollView 来计算绝对坐标系下的偏移量，而不是使用 course 自身的坐标系
        val dx = course.mCourseScrollView.let { it.mLastMoveX - it.mInitialX }
        view.translationX = dx.toFloat()
        view.y = course.let {
            it.mCourseScrollView.mLastMoveY -
                    it.getDistanceToScrollView() - mDistanceDownToViewTop
        }.toFloat()
        // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
        unfoldNoonOrDuskIfNecessary(view)
    }

    /**
     * 判断当前平移 LongPressView 中是否需要自动展开中午或者傍晚时间段
     */
    private fun unfoldNoonOrDuskIfNecessary(view: View) {
        // 如果长按开始时或者当前包含了中午时间段
        if (mIsContainNoonLongPressStart || CourseLayout.isContainNoonNow(view, course)) {
            when (course.getNoonRowState()) {
                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldNoonForce()
                else -> {}
            }
        }
        // 如果长按开始时或者当前包含了傍晚时间段
        if (mIsContainDuskLongPressStart || CourseLayout.isContainDuskNow(view, course)) {
            when (course.getDuskRowState()) {
                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldDuskForce()
                else -> {}
            }
        }
    }

    /**
     * 作用：如果可以改变位置，则带有动画的移动到新位置
     *
     * 判断有点多，但不算很复杂，为了以后更好的兼容，我写成了任意行宽和列宽的判断，
     * 意思就是：目前课表虽然列宽都是为 1 的 View，但我并没有使用这特殊条件，而是写出了任意列宽的比较
     *
     * 如果你要测试的话，建议把 [CourseLayout.DEBUG] 属性给打开
     */
    private fun changeLocationIfNecessary() {
        val view = mLongPressView
        if (view != null) {
            val layoutParams = view.layoutParams as CourseLayoutParams
            /*
            * 可能你会问为什么不直接用 View.left + View.translationX 或者直接 View.x
            * 原因如下：
            * 虽然目前课表的每个 item 宽高都是 match，
            * 但 item 的移动都是限制在方格内的，不应该用 View 的位置来判断
            * */
            val l = layoutParams.constraintLeft + view.translationX.toInt()
            val r = layoutParams.constraintRight + view.translationX.toInt()
            val t = layoutParams.constraintTop + view.translationY.toInt()
            val b = layoutParams.constraintBottom + view.translationY.toInt()

            val rowCount = layoutParams.rowCount
            val columnCount = layoutParams.columnCount

            var topRow = course.getRow(t)
            var bottomRow = course.getRow(b)
            var leftColumn = course.getColumn(l)
            var rightColumn = course.getColumn(r)

            val topDistance = course.getRowsHeight(0, topRow) - t
            val bottomDistance = b - course.getRowsHeight(0, bottomRow - 1)
            val leftDistance = course.getColumnsWidth(0, leftColumn) - l
            val rightDistance = r - course.getColumnsWidth(0, rightColumn - 1)

            /*
            * 第一次修正：
            * 根据与 View 内部最相邻的格子约束线的距离，修正此时的位置（格子约束线：因为是网状布局嘛，所以会有一个一个的格子）
            * 如果不修正，那由边界高度值取得的行数和列数将比总行数和总列数大上一行和一列
            * */
            if (leftDistance > rightDistance) {
                rightColumn = leftColumn + columnCount - 1
            } else {
                leftColumn = rightColumn - columnCount + 1
            }
            if (topDistance > bottomDistance) {
                bottomRow = topRow + rowCount - 1
            } else {
                topRow = bottomRow - rowCount + 1
            }

            // 记录与其他子 View 部分相交时该取得的最大最小值
            var maxTopRow = 0
            var minBottomRow = course.getRowCount() - 1
            var maxLeftColumn = 0
            var minRightColumn = course.getColumnCount() - 1

            // 判断行和列是否都完全包含或被包含，不包含取等时，取等要单独判断
            fun judgeIsContain(
                l1: Int, r1: Int, t1: Int, b1: Int,
                l2: Int, r2: Int, t2: Int, b2: Int
            ): Boolean {
                val c1 = l1 < l2 && r1 > r2
                val c2 = l1 > l2 && r1 < r2
                val d1 = t1 < t2 && b1 > b2
                val d2 = t1 > t2 && b1 < b2
                return (c1 || c2) && (d1 || d2)
            }

            /*
            * 第一次遍历：
            * 1、判断行或列是否完全包含或被包含，如果完全包含或被包含则回到原位置
            * 2、计算与其他 View 相交时该取得的边界最大最小值
            * */
            for (i in 0 until course.childCount) {
                val child = course.getChildAt(i)
                if (isSkipForeachJudge(child)) continue
                val lp = child.layoutParams as CourseLayoutParams
                val l1 = lp.constraintLeft
                val r1 = lp.constraintRight
                val t1 = lp.constraintTop
                val b1 = lp.constraintBottom

                /*
                * 由于没有判断是否是 mLongPressView，只判断了 mSubstituteView，
                * 所以当在 mLongPressView 没有添加进 overlay 时，是会直接回到原位置的，
                * 没有被添加进 overlay 说明要么处在动画中松手，要么没有长按就
                * */

                // 如果完全包含或被包含则回到原位置
                if (judgeIsContain(l, r, t, b, l1, r1, t1, b1)) {
                    restoreAffairViewToOldLocation()
                    return
                }
                val centerX = (l + r) / 2
                val centerY = (t + b) / 2
                // 以下是只有列完全包含或被包含时的特殊情况
                if (l <= l1 && r >= r1 || l >= l1 && r <= r1) {
                    when {
                        centerY < t1 -> minBottomRow = min(minBottomRow, lp.startRow - 1)
                        centerY > b1 -> maxTopRow = max(maxTopRow, lp.endRow + 1)
                        else -> {
                            restoreAffairViewToOldLocation()
                            return
                        }
                    }
                }
                // 以下是只有行完全包含或被包含的特殊情况
                if (t <= t1 && b >= b1 || t >= t1 && b <= b1) {
                    when {
                        centerX < l1 -> minRightColumn = min(minRightColumn, lp.startColumn - 1)
                        centerX > r1 -> maxLeftColumn = max(maxLeftColumn, lp.endColumn + 1)
                        else -> {
                            restoreAffairViewToOldLocation()
                            return
                        }
                    }
                }
                /*
                * 以下是只相交一个角时，此时主要是计算边界最大最小值
                * 情况如下：
                * 一、水平重叠的距离超过自身一半，且垂直重叠的距离也超过一半，不允许放置，回到原位置
                * 二、水平重叠的距离少于自身一半，且垂直重叠的距离也少于一半，根据重叠间距来计算对应的最大最小值
                * 三、水平重叠的距离超过一半，垂直重叠的距离少于一半，计算对应的最大最小值
                * 四、垂直重叠的距离超过一半，水平重叠的距离少于一半，计算对应的最大最小值
                * */
                val e1 = centerX in l1..r1
                val e2 = centerY in t1..b1
                if (e1 && e2) { // 情况一
                    restoreAffairViewToOldLocation()
                    return
                } else if (!e1 && !e2) { // 比较复杂的情况二
                    if (centerX < l1 && centerY < t1) { // 在一个子 View 的左上角
                        val dl = r - l1 // 水平重叠间距
                        val dt = b - t1 // 垂直重叠间距
                        if (dl > dt) {
                            minBottomRow = min(minBottomRow, lp.startRow - 1)
                        } else {
                            minRightColumn = min(minRightColumn, lp.startColumn - 1)
                        }
                    } else if (centerX > r1 && centerY < t1) { // 在一个子 View 的右上角
                        val dr = r1 - l // 水平重叠间距
                        val dt = b - t1 // 垂直重叠间距
                        if (dr > dt) {
                            minBottomRow = min(minBottomRow, lp.startRow - 1)
                        } else {
                            maxLeftColumn = max(maxLeftColumn, lp.endColumn + 1)
                        }
                    } else if (centerX > r1 && centerY > b1) { // 在一个子 View 的右下角
                        val dr = r1 - l // 水平重叠间距
                        val db = b1 - t // 垂直重叠间距
                        if (dr > db) {
                            maxTopRow = max(maxTopRow, lp.endRow + 1)
                        } else {
                            maxLeftColumn = max(maxLeftColumn, lp.endColumn + 1)
                        }
                    } else { // 在一个子 View 的左下角
                        val dl = r - l1 // 水平重叠间距
                        val db = b1 - t // 垂直重叠间距
                        if (dl > db) {
                            maxTopRow = max(maxTopRow, lp.endRow + 1)
                        } else {
                            minRightColumn = min(minRightColumn, lp.startColumn - 1)
                        }
                    }
                } else if (e1) { // 情况三
                    if (centerY < t1) {
                        minBottomRow = min(minBottomRow, lp.startRow - 1)
                    } else if (centerY > b1) {
                        maxTopRow = max(maxTopRow, lp.endRow + 1)
                    }
                } else { // 情况四
                    if (centerX < l1) {
                        minRightColumn = min(minRightColumn, lp.startColumn - 1)
                    } else if (centerX > r1) {
                        maxLeftColumn = max(maxLeftColumn, lp.endColumn + 1)
                    }
                }
            }

            // 判断最大最小值是否能装下自己，如果不能，则回到原位置
            if (minRightColumn - maxLeftColumn + 1 < columnCount
                || minBottomRow - maxTopRow + 1 < rowCount
            ) {
                restoreAffairViewToOldLocation()
                return
            }

            /*
            * 第二次修正：
            * 根据最大最小值修正最终的位置
            * */
            if (maxLeftColumn > leftColumn) {
                leftColumn = maxLeftColumn
                rightColumn = maxLeftColumn + columnCount - 1
            } else if (minRightColumn < rightColumn) {
                leftColumn = minRightColumn - columnCount + 1
                rightColumn = minRightColumn
            }
            if (maxTopRow > topRow) {
                topRow = maxTopRow
                bottomRow = maxTopRow + rowCount - 1
            } else if (minBottomRow < bottomRow) {
                topRow = minBottomRow - rowCount + 1
                bottomRow = minBottomRow
            }

            // 如果最终的位置没有发生改变则直接回到原位置
            if (topRow == mSubstituteLp.startRow
                && bottomRow == mSubstituteLp.endRow
                && leftColumn == mSubstituteLp.startColumn
                && rightColumn == mSubstituteLp.endColumn
            ) {
                restoreAffairViewToOldLocation()
                return
            }

            var hasViewInNoon = false // 是否有 View 处在中午时间段
            var hasViewInDusk = false // 是否有 View 处在傍晚时间段
            /*
            * 第二次遍历：
            * 1、对于修正后最终位置再次遍历子 View，寻找是否与其他子 View 有交集，若有，则回到原位置
            * */
            for (i in 0 until course.childCount) {
                val child = course.getChildAt(i)
                if (isSkipForeachJudge(child)) continue
                val lp = child.layoutParams as CourseLayoutParams
                val a1 = lp.startRow in topRow..bottomRow
                val a2 = lp.endRow in topRow..bottomRow
                val b1 = lp.startColumn in leftColumn..rightColumn
                val b2 = lp.endColumn in leftColumn..rightColumn
                if ((a1 || a2) && (b1 || b2)) {
                    restoreAffairViewToOldLocation()
                    return
                }
                // 判断中午和傍晚时间段是否存在 View
                when (lp.type) {
                    // 目前只知道时间轴上的那几个类型不算
                    CourseType.TIME, CourseType.ARROW_NOON, CourseType.ARROW_DUSK -> {}
                    else -> {
                        if (!hasViewInNoon && CourseLayout.isContainNoon(lp)) {
                            hasViewInNoon = true
                        }
                        if (!hasViewInDusk && CourseLayout.isContainDusk(lp)) {
                            hasViewInDusk = true
                        }
                    }
                }
            }
            mSubstituteLp.startRow = topRow
            mSubstituteLp.endRow = bottomRow
            mSubstituteLp.startColumn = leftColumn
            mSubstituteLp.endColumn = rightColumn
            // 让替身提前去占位，防止点击穿透，
            // 但请注意：这个占位有些延迟，动画的回调会先于重新布局执行
            mSubstituteView.layoutParams = mSubstituteLp

            /*
            * 判断是否需要折叠或展开时间段
            * 有以下几种情况需要折叠：
            * 1、刚开始中午(傍晚)时间段处于折叠，且开始时自身不包含中午(傍晚)时间段，
            *    长按移动后打开了中午(傍晚)时间段，最后松手时也不包含中午(傍晚)时间段
            * 2、刚开始自身包含中午(傍晚)时间段，长按松手后不包含中午(傍晚)时间段，且没有其他 View 在中午(傍晚)时间段
            *
            * 有个共同部分：最后松手时都不包含中午(傍晚)时间段
            * */
            if (!CourseLayout.isContainNoon(mSubstituteLp)) {
                mIsNeedFoldNoon =
                    mIsNoonFoldedLongPressStart && !mIsContainNoonLongPressStart
                            || mIsContainNoonLongPressStart && !hasViewInNoon
            } else {
                // 要移去的位置包含中午时间段，就自动展开
                when (course.getNoonRowState()) {
                    RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldNoonForce()
                    else -> {}
                }
            }

            if (!CourseLayout.isContainDusk(mSubstituteLp)) {
                mIsNeedFoldDusk =
                    mIsDuskFoldedLongPressStart && !mIsContainDuskLongPressStart
                            || mIsContainDuskLongPressStart && !hasViewInDusk
            } else {
                // 要移去的位置包含傍晚时间段，就自动展开
                when (course.getDuskRowState()) {
                    RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldDuskForce()
                    else -> {}
                }
            }

            if (mIsInOverlay) {
                /*
                * 进入了 overlay，此时说明 view.height 的高度是不变且准确的
                * 如果没有进入 overlay，说明是包含中午(傍晚)时间段且中午(傍晚)时间段又处于动画中
                * 此时移动到新位置时因为 view.height 得到的高度不准确，所以不能直接恢复折叠状态，
                * 需要在移动动画结束后恢复
                * */
                recoverFoldState()
            }

            mLongPressState = ANIM_TRAVEL

            /*
            * 这里为什么要设置回调才开启动画?
            * 原因如下：
            * 1、前面让 mSubstituteView 提前去占位，但下一次布局回调是在下面这个移动动画回调之后完成的，
            *    所以动画快于 onLayout() 方法执行，就会导致在前两帧得不到正确的 left、top 值
            * */
            mSubstituteView.setOnNextLayoutCallback {
                val dx = view.x  - it.left
                val dy = view.y - it.top

                // 记录动画开始前的 translationZ
                val translationZ = view.translationZ

                /*
                * 开启动画移动到最终位置
                * 为什么还要改变 view.left 和 view.top ?
                * 原因如下：
                * 1、存在此时正处于展开或折叠动画中，最终的位置是无法计算的，但可以使用 view.translationY + view.top
                *    来计算展开动画每一帧此时对应的位置
                *
                * 为什么不直接用 mSubstituteView 的 right、bottom，而只用 left、top ?
                * 原因如下：
                * 1、mSubstituteView 即使已经占好了位，也会出现 mSubstituteView 刚好处于正在展开的中午(傍晚)时间段上，
                *    导致得到的值 right、bottom 小于正确的值
                * */
                mMoveAnimation = MoveAnimation(
                    dx, dy, 0F, 0F,
                    getMoveAnimatorDuration(dx, dy)
                ) { x, y, fraction ->
                    view.translationZ = translationZ * (1 - fraction)
                    if (mIsInOverlay) {
                        view.translationX = x
                        view.translationY = y
                        val ll = mSubstituteView.left
                        val rr = ll + view.width
                        val tt = mSubstituteView.top
                        val bb = tt + view.height
                        view.left = ll
                        view.right = rr
                        view.top = tt
                        view.bottom = bb
                    } else {
                        // 没有添加进 overlay，说明 view 还在 course 中，而且是原来的位置
                        view.translationX = -(view.left - it.left - x)
                        view.translationY = -(view.top - it.top - y)
                    }
                }.addEndListener {
                    view.translationX = 0F // 还原
                    view.translationY = 0F // 还原
                    view.translationZ = 0F // 重置
                    val lp = view.layoutParams as CourseLayoutParams
                    lp.startRow = topRow
                    lp.endRow = bottomRow
                    lp.startColumn = leftColumn
                    lp.endColumn = rightColumn
                    course.removeView(mSubstituteView)
                    if (mIsInOverlay) {
                        course.overlay.remove(view)
                        course.addView(view, lp)
                    } else {
                        view.layoutParams = lp
                        // 与前面互相对应，如果 view 没有添加进 overlay，则在移动动画结束后恢复
                        recoverFoldState()
                    }
                    mOnEntityMoveListener?.onMoveToNewLocation(view, lp)
                    mLongPressState = OVER
                }.start()
            }
        }
    }

    /**
     * 带有动画的恢复 LongPressView 到原位置
     */
    private fun restoreAffairViewToOldLocation() {
        val view = mLongPressView
        if (view != null) {
            // 长按激活时是折叠的，且长按激活时也不包含中午(傍晚)时间段，就允许折叠
            mIsNeedFoldNoon = mIsNoonFoldedLongPressStart && !mIsContainNoonLongPressStart
            mIsNeedFoldDusk = mIsDuskFoldedLongPressStart && !mIsContainDuskLongPressStart
            recoverFoldState()

            val dx = view.x - mSubstituteView.left
            val dy = view.y - mSubstituteView.top

            val translationZ = view.translationZ

            mLongPressState = ANIM_RESTORE

            /*
            * 这里就可以直接使用 mSubstituteView 的 right、bottom，
            * 因为是回到原位置，mSubstituteView.height 即使要变化，这个变化也不会造成视觉上的影响
            * */
            mMoveAnimation = MoveAnimation(
                dx, dy, 0F, 0F,
                getMoveAnimatorDuration(dx, dy)
            ) { x, y, fraction ->
                view.translationX = x
                view.translationY = y
                view.translationZ = translationZ * (1 - fraction)
                if (mIsInOverlay) {
                    /*
                    * 1、如果没有进入 overlay，那么 view 就没有被添加进 course，设置它的属性是失效的，
                    *    并且也不需要设置，因为 view 与 mSubstituteView 处于相同的位置
                    * 2、如果进入了 overlay，view 与 mSubstituteView 就不处于相同的位置了，
                    *    并且 view 也脱离了 course，此时就得在移动的动画中就得实时获取
                    *    mSubstituteView 的 left、top、right、bottom，防止 mSubstituteView 正处于宽高改变的动画中
                    * */
                    view.left = mSubstituteView.left
                    view.top = mSubstituteView.top
                    view.right = mSubstituteView.right
                    view.bottom = mSubstituteView.bottom
                }
            }.addEndListener {
                view.translationX = 0F // 还原
                view.translationY = 0F // 还原
                view.translationZ = 0F // 重置
                course.removeView(mSubstituteView)
                if (mIsInOverlay) {
                    course.overlay.remove(view)
                    course.addView(view)
                }
                mLongPressState = OVER
            }.start()
        }
    }

    /**
     * 用于恢复折叠状态
     */
    private fun recoverFoldState() {
        if (mIsNeedFoldNoon) {
            when (course.getNoonRowState()) {
                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldNoonForce()
                else -> {}
            }
        }
        if (mIsNeedFoldDusk) {
            when (course.getDuskRowState()) {
                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldDuskForce()
                else -> {}
            }
        }
    }

    /**
     * 如果你把 [mLongPressView] 滑到屏幕显示边缘区域时，则可能需要调用 [CourseScrollView] 滚动
     * ```
     * 该方法作用：
     * 1、计算当前 LongPressView 的 top 和 bottom 值与 ScrollView 的距离来判断是否需要让 ScrollView 滚动
     * 2、计算让 ScrollView 滚动的速度
     * ```
     */
    private fun scrollIsNecessary() {
        mLongPressView?.let { mScrollRunnable.startIfCan(it) }
    }

    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
    private val mScrollRunnable = object : Runnable {

        var isInScrolling = false // 是否处于滚动状态
            private set

        private var view: View? = null

        private var velocity = 0 // 滚动的速度

        override fun run() {
            view?.let {
                if (isAllowScrollAndCalculateVelocity(it)) {
                    course.mCourseScrollView.scrollBy(0, velocity)
                    course.invalidate() // 直接重绘，交给 onDrawAbove() 处理移动
                    ViewCompat.postOnAnimation(course, this)
                } else {
                    isInScrolling = false
                }
            }
        }

        /**
         * 如果能开启滚动的话就开启滚动，注意：滚动是会自己取消的
         */
        fun startIfCan(view: View) {
            this.view = view
            if (!isInScrolling) { // 防止重复添加 Runnable
                isInScrolling = true
                run()
            }
        }

        /**
         * 取消滚动
         */
        fun cancel() {
            if (isInScrolling) {
                isInScrolling = false // 重置
                course.removeCallbacks(this)
                view = null
            }
        }

        /**
         * 是否允许滚动，如果允许，则计算滚动速度给 [velocity] 变量
         */
        private fun isAllowScrollAndCalculateVelocity(view: View): Boolean {
            val scroll = course.mCourseScrollView
            val diffHeight = course.getDistanceToScrollView()
            val topHeight = (view.y + diffHeight).toInt()
            val bottomHeight = topHeight + view.height
            val moveBoundary = 50 // 移动的边界值
            // 向上滚动，即手指移到底部，需要显示下面的内容
            val isNeedScrollUp =
                bottomHeight > scroll.height - moveBoundary
                        && course.mCourseScrollView.let { it.mLastMoveY - it.mInitialY } > 0
                        && scroll.height + scroll.scrollY < scroll.getChildAt(0).height // 是否滑到底

            // 向下滚动，即手指移到顶部，需要显示上面的内容
            val isNeedScrollDown =
                topHeight < moveBoundary
                        && course.mCourseScrollView.let { it.mLastMoveY - it.mInitialY } < 0
                        && scroll.scrollY > 0 // 是否滑到顶
            val isAllowScroll = isNeedScrollUp || isNeedScrollDown
            if (isAllowScroll) {
                velocity = if (isNeedScrollUp) {
                    // 速度最小为 6，最大为 12，与边界的差值成线性关系
                    min((bottomHeight - (scroll.height - moveBoundary)) / 10 + 6, 12)
                } else {
                    // 速度最小为 6，最大为 12，与边界的差值成线性关系
                    -min(((moveBoundary - topHeight) / 10 + 6), 12)
                }
            }
            return isAllowScroll
        }
    }

    override fun onDrawAbove(canvas: Canvas, course: CourseLayout) {
        /*
        * 为什么在这里平移呢？
        * 1、一般情况下是直接在 onTouchEvent() 的 Move 中平移位置
        * 2、但存在 onTouchEvent() 不回调的情况，而位置又因为 CourseScrollView 的滚动而改变
        * 3、所以为了减少不必要的判断，直接放在绘图的回调里是最方便的
        * */
        mLongPressView?.let {
            translateView(it)
        }
    }

    /**
     * 是否跳过遍历判断
     *
     * 我把抬手时对 LongPressView 与其他 View 能够相交的判断移动了出来，如果以后需要添加的可以直接写在这里，
     * 比如以后添加一个只有装饰作用的 View 在所有 View 的底部，就得写在这个地方
     */
    private fun isSkipForeachJudge(child: View): Boolean {
        return isSubstituteView(child) || child === mLongPressView
    }

    private fun getMoveAnimatorDuration(dx: Float, dy: Float): Long {
        // 自己拟合的一条由距离求出时间的函数，感觉比较适合动画效果 :)
        // y = 50 * x^0.25 + 90
        return (hypot(dx, dy).pow(0.25F) * 50 + 90).toLong()
    }

    // 移动动画的封装
    private class MoveAnimation(
        private val startX: Float,
        private val startY: Float,
        private val endX: Float,
        private val endY: Float,
        private val time: Long,
        private val onChange: (x: Float, y: Float, fraction: Float) -> Unit
    ) {
        private val animator = ValueAnimator.ofFloat(0F, 1F)
        fun start(): MoveAnimation {
            animator.run {
                addUpdateListener {
                    val now = animatedValue as Float
                    val x = startX - (startX - endX) * now
                    val y = startY - (startY - endY) * now
                    onChange.invoke(x, y, it.animatedFraction)
                }
                duration = time
                interpolator = OvershootInterpolator(0.6F) // 个人认为 0.6F 的回弹比较合适
                start()
            }
            return this
        }

        fun addEndListener(onEnd: () -> Unit): MoveAnimation {
            animator.addListener(onEnd = { onEnd.invoke() })
            return this
        }

        fun end() {
            animator.end()
        }
    }
}