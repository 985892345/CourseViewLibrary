package com.mredrock.cyxbs.lib.courseview.helper

import android.animation.ValueAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.Vibrator
import kotlin.math.abs
import kotlin.math.min

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/2 10:07
 */
class CourseLongPressAffairHelper private constructor(
    private val course: CourseLayout
) : OnCourseTouchListener {

    private var mInitialX = 0
    private var mInitialY = 0
    private var mLastMoveX = 0
    private var mLastMoveY = 0

    private var mAffairView: View? = null

    // 认定是滚动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    // 识别为长按所需的时间
    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    // 是否处于长按状态
    private var mIsInLongPress = false

    // 长按时执行的 Runnable
    private val mLongPressRunnable = Runnable {
        mIsInLongPress = true
        Vibrator.start(course.context, 36) // 长按被触发来个震动提醒
    }

    init {
        // 给 CourseLayout 设置触摸监听
        course.addCourseTouchListener(this)
    }

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (event.action == MotionEvent.ACTION_DOWN) {
            val child = course.findItemUnderByXY(x, y)
            val lp = child?.layoutParams as CourseLayoutParams?
            if (lp?.type == CourseType.AFFAIR) {
                if (child !== mAffairView) {
                    child?.translationZ = 20F // 让 AffairView 显示在所有 View 之上
                    mAffairView = child
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y

                mIsInLongPress = false // 重置
                course.postDelayed(mLongPressRunnable, mLongPressTimeout)
                // 禁止外面的 ScrollView 拦截事件
                course.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsInLongPress) { // 处于长按状态
                    mLastMoveX = x
                    mLastMoveY = y

                    scrollIsNecessary(y)
                    if (!mScrollRunnable.isInScrolling) {
                        /*
                        * 如果 isInScrolling = true，则 translateAffairView()
                        * 该方法应交由 mScrollRunnable 调用，而不是让 onTouchEvent() 调用
                        * 原因如下：
                        * 1、避免在同一帧时与 mScrollRunnable 重复调用
                        * 2、存在手指正触摸让 ScrollView 的滚动区，但却因为手指没有移动而不回调 onTouchEvent()
                        *    这种情况下就得让 mScrollRunnable 给 mLastMoveY 加上 ScrollView 将偏移的值来
                        *    调用 translateAffairView()，不然就会出现滚轴滚动，但 mAffairView 却没动（横屏时尤其明显）
                        * */
                        translateAffairView(y) // 平移显示事务的 View
                    }
                } else {
                    if (abs(x - mLastMoveX) <= mTouchSlop
                        && abs(y - mLastMoveY) <= mTouchSlop
                    ) {
                        /*
                        * 走到该分支说明：
                        * 1、不处于长按状态，即 mIsInLongPress = false
                        * 2、移动距离小于认定是滑动的 mTouchSlop
                        *
                        * 下一次移动有下面三条路可以走
                        * 1、可能仍然回到这里
                        * 2、如果移动距离大于 mTouchSlop，则会走下面那个 else 分支
                        * 3、如果 mIsInLongPress 被赋值为 true（说明达到长按时间）
                        * */
                    } else {
                        // 走到该分支说明在判定为长按的时间内，移动的距离大于了 mTouchSlop
                        // 接下来的一系列事件就不该自身处理，应该被 CourseScrollView 当成滚动而拦截
                        course.parent.requestDisallowInterceptTouchEvent(false)
                        course.removeCallbacks(mLongPressRunnable)
                        mAffairView = null // 重置
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                changePositionIfNecessary()
                mScrollRunnable.cancel()
                mAffairView = null // 重置
            }
            MotionEvent.ACTION_CANCEL -> {
                restoreAffairViewToOldPosition()
                mScrollRunnable.cancel()
                course.removeCallbacks(mLongPressRunnable)
                mAffairView = null // 重置
            }
        }
    }

    /**
     * 平移 AffairView
     */
    private fun translateAffairView(y: Int) {
        mAffairView?.translationX = (mLastMoveX - mInitialX).toFloat()
        mAffairView?.translationY = (y - mInitialY).toFloat()
        // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
        unfoldNoonOrDuskIfNecessary()
    }

    /**
     * 判断当前平移 AffairView 中是否需要自动展开中午或者傍晚时间段
     */
    private fun unfoldNoonOrDuskIfNecessary() {
        mAffairView?.let {
            val top = it.y.toInt()
            val bottom = top + it.height
            val topRow = course.getRow(top)
            val bottomRow = course.getRow(bottom)
            if (topRow <= CourseLayout.NOON_TOP && bottomRow >= CourseLayout.NOON_BOTTOM) {
                if (course.getNoonRowState() == RowState.FOLD) {
                    course.unfoldNoon()
                }
            }
            if (topRow <= CourseLayout.DUSK_TOP && bottomRow >= CourseLayout.DUSK_BOTTOM) {
                if (course.getDuskRowState() == RowState.FOLD) {
                    course.unfoldDusk()
                }
            }
        }
    }

    /**
     * 如果可以改变位置，则带有动画的移动到新位置
     */
    private fun changePositionIfNecessary() {
        val view = mAffairView
        if (view != null) {
            val left = view.x.toInt()
            val right = left + view.width
            val top = view.y.toInt()
            val bottom = top + view.height

            var topRow = course.getRow(top)
            var bottomRow = course.getRow(bottom)
            var leftColumn = course.getColumn(left)
            var rightColumn = course.getColumn(right)

            val layoutParams = view.layoutParams as CourseLayoutParams
            val rowCount = layoutParams.rowCount
            val columnCount = layoutParams.columnCount

            val topDistance = course.getRowsHeight(0, topRow) - top
            val bottomDistance = bottom - course.getRowsHeight(0, bottomRow - 1)
            val leftDistance = course.getColumnsWidth(0, leftColumn) - left
            val rightDistance = right - course.getColumnsWidth(0, rightColumn - 1)

            if (topDistance > bottomDistance) {
                bottomRow = topRow + rowCount - 1
            } else {
                topRow = bottomRow - rowCount + 1
            }
            if (leftDistance > rightDistance) {
                rightColumn = leftColumn + columnCount - 1
            } else {
                leftColumn = rightColumn - columnCount + 1
            }

            fun isIntersect(aMin: Int, aMax: Int, bMin: Int, bMax: Int) = !(aMax < bMin || aMin > bMax)

            for (i in 0 until course.childCount - 1) {
                val child = course.getChildAt(i)
                val lp = child.layoutParams as CourseLayoutParams

                if (isIntersect(lp.startRow, lp.endRow, topRow, bottomRow)
                    && isIntersect(lp.startColumn, lp.endColumn, leftColumn, rightColumn)
                    && child !== view
                ) {
                    restoreAffairViewToOldPosition()
                    return
                }
            }

            val finalX = course.getColumnsWidth(0, leftColumn - 1).toFloat()
            val finalY = course.getRowsHeight(0, topRow - 1).toFloat()

            MoveAnimation(view.x, view.y, finalX, finalY, 200) { x, y ->
                view.x = x
                view.y = y
            }.addEndListener {
                view.translationX = 0F
                view.translationY = 0F
                view.translationZ = 0F // 重置
                // 防止动画未结束而又开启了下一次移动导致 mAffairView 改变
                if (view === mAffairView) mAffairView = null
                if (view.parent is CourseLayout) { // 防止动画结束时 View 已经被删除了
                    val lp = view.layoutParams as CourseLayoutParams
                    lp.startRow = topRow
                    lp.endRow = bottomRow
                    lp.startColumn = leftColumn
                    lp.endColumn = rightColumn
                    view.layoutParams = lp // 刷新布局
                }
            }.start()
        }
    }

    /**
     * 带有动画的恢复 AffairView 到原位置
     */
    private fun restoreAffairViewToOldPosition() {
        val view = mAffairView
        if (view != null) {
            val translationX = view.translationX
            val translationY = view.translationY
            // 没有平移量时直接结束
            if (translationX == 0F && translationY == 0F) return
            MoveAnimation(translationX, translationY, 0F, 0F, 200) { x, y ->
                view.translationX = x
                view.translationY = y
                view.translationZ = 0F // 重置
            }.addEndListener {
                // 防止动画未结束而又开启了下一次移动导致 mAffairView 改变
                if (view === mAffairView) mAffairView = null
            }.start()
        }
    }

    /**
     * 如果你把 AffairView 滑到屏幕显示边缘区域时，则可能需要调用 [CourseScrollView] 滚动
     * ```
     * 该方法作用：
     * 1、计算当前 AffairView 的 top 和 bottom 值与 ScrollView 的距离来判断是否需要让 ScrollView 滚动
     * 2、计算让 ScrollView 滚动的速度
     * ```
     */
    private fun scrollIsNecessary(y: Int) {
        mAffairView?.let { mScrollRunnable.startIfCan(y, it) }
    }

    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
    private val mScrollRunnable = object : Runnable {

        var isInScrolling = false // 是否处于滚动状态
            private set

        private var view: View? = null

        private var isScrolled = false // 是否已经滚动过

        private var velocity = 0 // 滚动的速度

        private var touchY = 0 // 当前手指触摸的 Y 值

        override fun run() {
            view?.let {
                if (isAllowScrollAndCalculateVelocity(it)) {
                    val startScrollY = course.mCourseScrollView.scrollY
                    course.mCourseScrollView.scrollBy(0, velocity)
                    val endScrollY = course.mCourseScrollView.scrollY
                    // 调用 scrollBy 后不一定会滑动你设置的值
                    // 所有需要得到滚动前和滚动后的 scrollY 来算偏移量
                    touchY += endScrollY - startScrollY
                    translateAffairView(touchY)
                    ViewCompat.postOnAnimation(course, this)
                } else {
                    isInScrolling = false
                }
            }
        }

        /**
         * 如果能开启滚动的话就开启滚动，注意：滚动是会自己取消的
         */
        fun startIfCan(y: Int, view: View) {
            touchY = y
            this.view = view
            if (!isInScrolling) { // 防止重复添加 Runnable
                isInScrolling = true
                run()
                isScrolled = true // 必须放在 run() 后，该变量表示在一次滑动分发中是否之前调用过 run()
            }
        }

        /**
         * 取消滚动
         */
        fun cancel() {
            isScrolled = false
            if (isInScrolling) {
                isInScrolling = false
                course.removeCallbacks(this)
                view = null
            }
        }

        /**
         * 是否允许滚动，如果允许，则计算滚动速度给 [velocity] 变量
         */
        private fun isAllowScrollAndCalculateVelocity(view: View): Boolean {
            val scroll = course.mCourseScrollView
            val diffHeight = course.getDiffHeightWithScrollView()
            val topHeight = (view.y + diffHeight).toInt()
            val bottomHeight = topHeight + view.height
            val moveBoundary = 100 // 移动的边界值
            // 向上滚动，即手指移到底部，需要显示下面的内容
            val isNeedScrollUp =
                bottomHeight > scroll.height - moveBoundary
                        && if (isScrolled) true else touchY > mInitialY + mTouchSlop // 只限制刚触摸时
                        && scroll.height + scroll.scrollY != scroll.getChildAt(0).height // 是否滑到底

            // 向下滚动，即手指移到顶部，需要显示上面的内容
            val isNeedScrollDown =
                topHeight < moveBoundary
                        && if (isScrolled) true else touchY < mInitialX - mTouchSlop // 只限制刚触摸时
                        && scroll.scrollY != 0 // 是否滑到顶
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

    private class MoveAnimation(
        private val startX: Float,
        private val startY: Float,
        private val endX: Float,
        private val endY: Float,
        private val time: Long,
        private val onChange: (x: Float, y: Float) -> Unit
    ) {
        private val animator = ValueAnimator.ofFloat(0F, 1F)
        fun start(): MoveAnimation {
            animator.run {
                addUpdateListener {
                    val now = animatedValue as Float
                    val x = startX - (startX - endX) * now
                    val y = startY - (startY - endY) * now
                    onChange.invoke(x, y)
                }
                duration = time
                interpolator = OvershootInterpolator(1F)
                start()
            }
            return this
        }

        fun addEndListener(onEnd: () -> Unit): MoveAnimation {
            animator.addListener(onEnd = { onEnd.invoke() })
            return this
        }
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的。
         *
         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
         */
        fun attach(course: CourseLayout): CourseLongPressAffairHelper {
            return CourseLongPressAffairHelper(course)
        }
    }
}