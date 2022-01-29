package com.mredrock.cyxbs.lib.courseview.helper

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import kotlin.RuntimeException
import kotlin.math.abs

/**
 * ```
 * 该类作用：
 * 1、封装长按生成自定义行程的功能
 *
 * 该类设计：
 * 1、对 CourseLayout 增加事件监听来实现
 * 2、事件监听参考了 RV 的 ItemTouchHelper 的设计
 *
 * 注意事项：
 * 1、该类只管理创建事务，请不要添加一些不属于该类的功能，想添加功能应该再写一个 OnCourseTouchListener
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseCreateAffairHelper private constructor(
    private val course: CourseLayout
) : OnCourseTouchListener {

    /**
     * 设置触摸空白区域生成的灰色的 View 的点击监听
     */
    fun setTouchAffairViewClickListener(l: View.OnClickListener) {
        mTouchAffairView.setOnClickListener(l)
    }

    private var mInitialX = 0 // Down 时的初始 X 值
    private var mInitialY = 0 // Down 时的初始 Y 值
    private var mLastMoveX = 0 // Move 时的移动 X 值
    private var mLastMoveY = 0 // Move 时的移动 Y 值

    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop
    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private var mIsInLongPress = false
    private val mLongPressRunnable = Runnable {
        mIsInLongPress = true
        // 添加 mTouchAffairView
        course.addCourse(
            mTouchAffairView,
            CourseLayoutParams(
                mInitialColumn,
                mTopRow,
                mBottomRow - mTopRow + 1,
                CourseType.AFFAIR_TOUCH
            )
        )
        vibrator(course.context) // 长按被触发来个震动
    }

    private var mTopRow = 0 // Move 事件中选择区域的开始行数
    private var mBottomRow = 0 // Move 事件中选择区域的结束行数
    private var mInitialRow = 0 // Down 事件中触摸的初始行数
    private var mInitialColumn = 0 // Down 事件中触摸的初始列数

    private var mTouchRow = 0 // 当前触摸的行数

    init {
        // 给 CourseLayout 设置触摸监听
        course.addCourseTouchListener(this)
    }

    /**
     * 触摸时显示的事务 View
     * ```
     * 这里直接使用 ImageView 的原因：
     * 1、显示更方便，主要设置图片、背景这些
     * 2、计算更方便，直接修改该 View 的 layoutParams 即可
     * 3、可以更好的设置点击监听
     * 缺点：
     * 1、稍稍耗性能，每次修改 layoutParams 都会重新布局，比不上在 onDraw() 里面绘图（但绘图需要计算，比较麻烦）
     * ```
     */
    private val mTouchAffairView by lazy(LazyThreadSafetyMode.NONE) {
        val radius = course.context.resources.getDimension(R.dimen.course_course_item_radius)
        ImageView(course.context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = GradientDrawable().apply {
                // 设置圆角
                cornerRadii =
                    floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
                // 背景颜色
                setColor(ContextCompat.getColor(course.context, R.color.course_add_affair_color))
            }
            // 设置 ImageView 的前景图片
            setImageResource(R.drawable.course_ic_add_circle_white)
        }
    }

    private var mIsInIntercepting = false // 是否处于拦截中

    /**
     * 你可能会比较疑惑为什么在事件分发中对事件拦截进行提前判断? 或者为什么我要重写这个方法?
     * ```
     * 原因如下：
     * 1、onDispatchTouchEvent() 早于 isIntercept() 调用，所以提前判断是否拦截不会出现问题
     * 2、主要原因在于何时取消 mTouchAffairView 的显示，需要知道当前点击的是否是这个 mTouchAffairView，
     *       如果不是就 remove 掉
     *    而却会出现事件被 CourseLayout 的子 View 拦截的情况，出现这种情况时，isIntercept() 是不会被调用的，
     *    所以就没有办法去判断何时 remove 掉 mTouchAffairView
     * 3、接 2，为了将 mTouchAffairView 的 remove 封装起来，就只能将拦截提前判断
     * 缺点：
     * 1、在一些不必要的情况下（比如子 View 会拦截时）也进行了判断的处理，但为了更好的封装性，这点消耗还是能接受的
     * ```
     */
    override fun onDispatchTouchEvent(event: MotionEvent, course: CourseLayout) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mIsInIntercepting = false
            val x = event.x.toInt()
            val y = event.y.toInt()
            val touchView = course.findItemUnder(x, y)
            /*
            * 这里 touchView = null 时一定会将事件给 CourseLayout#onTouchEvent() 处理，
            * 之后事件就会分发到每个 OnCourseTouchListener 中，
            * 如果事件能够传递到该 listener，则会直接交给自身的 onTouchEvent() 处理
            *
            * 但存在顺序在前面的 OnCourseTouchListener 提前拦截事件，
            * 所以需要重写 onCancelDownEvent() 方法取消 mLongPressRunnable
            * */
            if (touchView == null) {
                mIsInIntercepting = true

                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y
                mInitialColumn = course.getColumn(x)
                mInitialRow = course.getRow(y)
                mTopRow = mInitialRow
                mBottomRow = mInitialRow

                mIsInScrolling = false // 重置

                course.postDelayed(mLongPressRunnable, mLongPressTimeout)
                // 禁止 CourseScrollView 拦截事件
                course.parent.requestDisallowInterceptTouchEvent(true)
            }
            if (touchView !== mTouchAffairView) {
                // 此时说明点击的是其他地方，不是 mTouchAffairView，则 remove 掉 mTouchAffairView
                if (mTouchAffairView.parent != null) {
                    course.removeView(mTouchAffairView)
                }
            }
        }
    }

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        return mIsInIntercepting
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (mIsInLongPress) { // 处于长按状态
                    mLastMoveX = x
                    mLastMoveY = y
                    changeTouchAffairView(y)
                    scrollIsNecessary(y)
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
                        mIsInLongPress = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsInLongPress = false
                course.removeCallbacks(mLongPressRunnable)
                course.removeCallbacks(mScrollRunnable)
                if (abs(x - mLastMoveX) <= mTouchSlop
                    && abs(y - mLastMoveY) <= mTouchSlop
                ) {
                    // 这里说明移动的距离小于 mTouchSlop，但还是得把点击的事务给绘制上，但是只有一格
                    if (mTouchAffairView.parent == null) { // 防止之前已经被添加
                        course.addCourse(
                            mTouchAffairView,
                            CourseLayoutParams(
                                mInitialColumn,
                                mTopRow,
                                mBottomRow - mTopRow + 1,
                                CourseType.AFFAIR_TOUCH
                            )
                        )
                    }
                }
                // 大于 mTouchSlop 时会被 ScrollView 拦截
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsInLongPress = false
                course.removeCallbacks(mLongPressRunnable)
                course.removeCallbacks(mScrollRunnable)
            }
        }
    }

    override fun onCancelDownEvent(course: CourseLayout) {
        mIsInLongPress = false
        course.removeCallbacks(mLongPressRunnable)
    }

    /**
     * 该方法作用：
     *
     * 1、根据 [y] 值来计算当前 mTouchAffairView 的位置并刷新布局
     */
    private fun changeTouchAffairView(y: Int) {
        val touchView = course.findItemUnder(mInitialX, y)
        if (touchView == null || touchView === mTouchAffairView) {
            mTouchRow = course.getRow(y) // 当前触摸的行数
            val topRow: Int
            val bottomRow: Int
            // 根据当前触摸的行数与初始行数比较，得到 topRow、bottomRow
            if (mTouchRow > mInitialRow) {
                topRow = mInitialRow
                bottomRow = mTouchRow
            } else {
                topRow = mTouchRow
                bottomRow = mInitialRow
            }
            if (topRow != mTopRow || bottomRow != mBottomRow) { // 避免不必要的刷新
                mTopRow = topRow
                mBottomRow = bottomRow
                val lp = mTouchAffairView.layoutParams as CourseLayoutParams
                lp.startRow = topRow
                lp.endRow = bottomRow
                mTouchAffairView.layoutParams = lp // 设置属性，刷新布局
            }
            // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
            unfoldNoonOrDuskIfNecessary()
        }
    }

    /**
     * 得到 [CourseLayout] 外层包裹的 [CourseScrollView]
     *
     * 因为在长按选择事务时，滑到屏幕显示边缘区域时需要调用 [CourseScrollView] 进行滚动，
     * 所以只能采用这种强耦合的方式
     */
    private val mCourseScrollView by lazy(LazyThreadSafetyMode.NONE) {
        var scrollView: CourseScrollView? = null
        var parent = course.parent
        while (parent is ViewGroup) {
            if (parent is CourseScrollView) {
                scrollView = parent as CourseScrollView
                break
            }
            parent = parent.parent
        }
        if (scrollView == null) throw RuntimeException(
            "CourseLayout 必须拥有 CourseScrollView 父布局，因为在一些情况下要调用它滚动"
        )
        scrollView!!
    }

    private var mIsInScrolling = false // 是否处于滚动状态

    private var mScrollVelocity = 0 // 滚动的速度
    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
    private val mScrollRunnable = object : Runnable {
        override fun run() {
            mCourseScrollView.scrollBy(0, mScrollVelocity)
            ViewCompat.postOnAnimation(course, this)
        }
    }

    /**
     * 如果你手指滑到屏幕显示边缘区域时，则可能需要调用 [mCourseScrollView] 滚动
     */
    private fun scrollIsNecessary(y: Int) {
        val nowHeight = y + getDiffHeightWithScrollView()
        val moveBoundary = 100 // 移动的边界值
        val velocity = 7 // 移动的速度，不能过于太大，目前由于滚轴不是很长，所以速度固定
        val isNeedScrollUp = nowHeight > mCourseScrollView.height - moveBoundary
        val isNeedScrollDown = nowHeight < moveBoundary
        if (isNeedScrollUp || isNeedScrollDown) {
            mScrollVelocity = if (isNeedScrollUp) velocity else -velocity
            if (!mIsInScrolling) { // 防止重复添加 Runnable
                mIsInScrolling = true
                mScrollRunnable.run()
            }
        } else {
            if (mIsInScrolling) {
                mIsInScrolling = false
                course.removeCallbacks(mScrollRunnable)
            }
        }
    }

    /**
     * 得到 [course] 与 [mCourseScrollView] 之间相差的高度，是眼睛能看见的高度差
     */
    private fun getDiffHeightWithScrollView(): Int {
        var dHeight = course.top // 与 mCourseScrollView 去掉 scrollY 后的高度差，即屏幕上显示的高度差
        var parent = course.parent
        while (parent is ViewGroup) { // 这个循环用于计算 dHeight
            dHeight -= parent.scrollY
            if (parent === mCourseScrollView) { // 找到 mCourseScrollView 就结束
                break
            }
            dHeight += parent.top
            parent = parent.parent
        }
        return dHeight
    }

    /**
     * 根据不同的 Android 版本调用不同的震动方法
     */
    private fun vibrator(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = context.getSystemService(VibratorManager::class.java)
            vibrator.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createOneShot(
                        30,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            )
        } else {
            // 做了版本判断，但它却没有做判断，仍认为该方法被 Deprecated。。。
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        30,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(30)
            }
        }
    }

    private class UnfoldAnimation(
        private val onEnd: (() -> Unit)? = null,
        private val onChanged: (nowWeight: Float) -> Unit
    ) {
        private val animator = ValueAnimator.ofFloat(0F, 1F)
        fun start() {
            animator.run {
                addUpdateListener { onChanged.invoke(animatedValue as Float) }
                addListener(onEnd = { onEnd?.invoke() })
                duration = 150 // 时间不能过于太长，防止用户突然反向滑动，出现奇怪的操作
                start()
            }
        }
    }

    /**
     * 判断当前滑动中是否需要自动展开中午或者傍晚时间段
     */
    private fun unfoldNoonOrDuskIfNecessary() {
        if (mTopRow <= NOON_TOP && mBottomRow >= NOON_BOTTOM ) {
            if (course.getNoonRowState() == RowState.FOLD) {
                course.foldDusk {
                    amendScrollViewScrollYIfNecessary(0, mTouchRow > NOON_BOTTOM)
                }
            }
        } else if (mTopRow <= DUSK_TOP && mBottomRow >= DUSK_BOTTOM) {
            if (course.getDuskRowState() == RowState.FOLD) {
                course.foldDusk {
                    amendScrollViewScrollYIfNecessary(0, mTouchRow > DUSK_BOTTOM)
                }
            }
        }
    }

    /**
     * 用于修正 [mCourseScrollView] 当前的 scrollY。因为在展开时，ScrollView 是向下展开的，
     * 所以需要控制它向上移动，使当前触摸的点不会产生偏移
     * @param add 重新布局后会增加的高度
     * @param isAbove 如果为 true，则是当前触摸点上方的布局增高
     */
    private fun amendScrollViewScrollYIfNecessary(add: Int, isAbove: Boolean) {
        val oldScrollY = mCourseScrollView.scrollY
        val nowScrollY = if (isAbove) oldScrollY + add else oldScrollY
        mCourseScrollView.scrollY = nowScrollY
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的
         *
         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
         */
        fun attach(course: CourseLayout): CourseCreateAffairHelper {
            return CourseCreateAffairHelper(course)
        }
    }
}