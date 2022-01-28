package com.mredrock.cyxbs.lib.courseview.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseDecoration
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import kotlin.RuntimeException
import kotlin.math.abs

/**
 * 参考 RV 的 ItemTouchHelper 的设计，将长按生成自定义行程的功能与 [CourseLayout] 进行分离，
 * 增强代码可读性和维护性
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

    /**
     * 取消触摸空白区域时生成的灰色的 View
     *
     * **NOTE：** 内部并没有调用去掉这个 View，因为 OnCourseTouchListener 的设计
     * 造成会被之前的 listener 提前拦截，所以需要在外面去判断什么时候取消显示这个 View
     */
    fun removeTouchAffairView() {
        course.removeView(mTouchAffairView)
    }

    private var mInitialX = 0
    private var mInitialY = 0
    private var mLastMoveX = 0
    private var mLastMoveY = 0

    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop
    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private var mIsInLongPress = false
    private val mLongPressRunnable = Runnable {
        mIsInLongPress = true
        // 添加 mTouchAffairView
        course.addCourse(
            mTouchAffairView,
            CourseLayoutParams(mDay, mTopRow, mBottomRow, CourseType.AFFAIR_TOUCH)
        )
        vibrator(course.context) // 长按被触发来个震动
    }

    private var mDay = 0
    private var mTopRow = 0
    private var mBottomRow = 0
    private var mInitialRow = 0

    // 触摸时显示的事务 View
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

    init {
        course.addCourseTouchListener(this)
    }

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchView = course.findItemUnder(x, y)
            if (touchView == null) {
                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y
                mDay = course.getColumn(x)
                mInitialRow = course.getRow(y)
                mTopRow = mInitialRow
                mBottomRow = mInitialRow
                course.postDelayed(mLongPressRunnable, mLongPressTimeout)
                // 禁止 CourseScrollView 拦截事件
                course.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }
        return false
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
                        course.requestDisallowInterceptTouchEvent(false)
                        course.removeCallbacks(mLongPressRunnable)
                        mIsInLongPress = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                course.removeCallbacks(mLongPressRunnable)
                mIsInLongPress = false
                if (abs(x - mLastMoveX) <= mTouchSlop
                    && abs(y - mLastMoveY) <= mTouchSlop
                ) {
                    // 这里说明移动的距离小于 mTouchSlop，但还是得把点击的事务给绘制上
                    course.addCourse(
                        mTouchAffairView,
                        CourseLayoutParams(mDay, mTopRow, mBottomRow, CourseType.AFFAIR_TOUCH)
                    )
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                course.removeCallbacks(mLongPressRunnable)
                mIsInLongPress = false
            }
        }
    }

    /**
     * 根据 [y] 值来计算当前 mTouchAffairView 的位置并刷新布局
     */
    private fun changeTouchAffairView(y: Int) {
        val touchView = course.findItemUnder(mInitialX, y)
        if (touchView == null || touchView === mTouchAffairView) {
            val row = course.getRow(y)
            val topRow: Int
            val bottomRow: Int
            if (row > mInitialRow) {
                topRow = mInitialRow
                bottomRow = row
            } else {
                topRow = row
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
        }
    }

    /**
     * 得到 CourseLayout 外层包裹的 CourseScrollView
     *
     * 因为在长按选择事务时，滑到屏幕显示边缘区域时需要调用 CourseScrollView 滚动，
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
            "CourseLayout 必须拥有 CourseScrollView 父布局，因为在一些情况下要调用它滑动"
        )
        scrollView!!
    }

    /**
     * 如果你手指滑到屏幕显示边缘区域时，则可能需要调用 mCourseScrollView 滚动
     */
    private fun scrollIsNecessary(y: Int) {
        var dHeight = course.top // 与 mCourseScrollView 去掉 scrollY 后的高度差，即屏幕上显示的高度差
        var parent = course.parent
        while (parent is ViewGroup) { // 这个循环用于计算 dHeight
            dHeight -= parent.scrollY
            if (parent === mCourseScrollView) {
                break
            }
            dHeight += parent.top
            parent = parent.parent
        }
        val nowHeight = dHeight + y
        val moveBoundary = 40 // 移动的边界值
        val velocity = 10 // 移动的速度，不能过于太大
        if (nowHeight < moveBoundary) {
            mCourseScrollView.scrollBy(0, -velocity)
        } else if (nowHeight > mCourseScrollView.height - moveBoundary) {
            mCourseScrollView.scrollBy(0, velocity)
        }
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

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的
         */
        fun attach(course: CourseLayout): CourseCreateAffairHelper {
            return CourseCreateAffairHelper(course)
        }
    }
}