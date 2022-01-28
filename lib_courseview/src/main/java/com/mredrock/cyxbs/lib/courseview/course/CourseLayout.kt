package com.mredrock.cyxbs.lib.courseview.course

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.NestedScrollingChildHelper
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseDecoration
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseLayoutAttrsException
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.net.NetLayout

/**
 * ```
 * 该 View 作用：
 * 1、继承于 NetLayout，自定义对课程的布局
 *
 * ● ======== app:net_columnCount = 8 ======== ●
 * ↓                                           ↓
 * --------------------------------------------- ←- ●
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |   L   |                  S                |    ║
 * |   E   |                  H                |    ║
 * |   F   |                  O                |    ║ ←-←-←-←-←- Noon 时间段，占 1 x 7
 * |   T   |                  W                |    ║
 * |       |                                   | app:net_rowCount = 14
 * |   T   |                  L                |    ║
 * |   I   |                  E                |    ║
 * |   M   |                  S                |    ║ ←-←-←-←-←- Dusk 时间段，占 1 x 7
 * |   E   |                  S                |    ║
 * |       |                  O                |    ║
 * |       |                  N                |    ║
 * |       |                  S                |    ║
 * |       |                                   |    ║
 * --------------------------------------------- ←- ●
 * ↑       ↑                                   ↑
 * ↑       ↑                                   ↑
 * ↑       ● =========== Column = 7 ========== ●
 * ↑       ↑                  ↑
 * ● ===== ●              这里显示课程
 *     ↑
 *  这一列显示左侧的时间
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayout : NetLayout {

    fun addCourse(view: View, lp: CourseLayoutParams) {
        addItem(view, lp)
    }

    fun addCourseDecoration(decor: CourseDecoration, index: Int = mCourseDecoration.size) {
        mCourseDecoration.add(index, decor)
    }

    fun addCourseTouchListener(l: OnCourseTouchListener, index: Int = mCourseTouchListener.size) {
        mCourseTouchListener.add(index, l)
    }

    private val mCourseAttrs: CourseLayoutAttrs

    private val mCourseDecoration = ArrayList<CourseDecoration>(1)
    private val mCourseTouchListener = ArrayList<OnCourseTouchListener>(1)

    private var mInterceptingOnTouchListener: OnCourseTouchListener? = null

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        mCourseAttrs = CourseLayoutAttrs(mNetAttrs)
    }

    constructor(
        context: Context,
        attrs: CourseLayoutAttrs
    ) : super(context, attrs) {
        mCourseAttrs = attrs
    }

    override fun measureChildWithRatio(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        childWidthRatio: Float,
        childHeightRatio: Float
    ) {
        val lp = child.layoutParams.net()
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val childWidth = (childWidthRatio * parentWidth).toInt()
        val childWidthMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childWidth,
                /*
                * 这里为什么直接给 EXACTLY ?
                * 1、课表初始状态需要填满整个父布局
                * 2、如果给 AT_MOST，则会使子 View 变成自己想要的大小,
                * 3、如果给 UNSPECIFIED 值，则会使没被重写的子 View 的宽直接变成 0，直接为 0 了就没了意义。
                *    原因可看：View#onMeasure() -> View#getDefaultSize()
                * */
                MeasureSpec.EXACTLY
            ),
            lp.leftMargin + lp.rightMargin, lp.width
        )

        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val childHeight = (childHeightRatio * parentHeight).toInt()
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childHeight,
                /*
                * 这里原因与上面相同，但还需要添加一些
                * 4、目前需求（22年）课表在开始时不显示中午和傍晚时间段，我设计的 NetLayout 可以把高度设置成
                *    wrap_content，再调用 setRowInitialWeight（若不存在，请查看 git）来隐藏中午和傍晚时间段
                * 5、课表如果要显示中午和傍晚时间段，则外布局需要包裹一个 NestedScrollView，这时，父布局得到的
                *    测量模式为 UNSPECIFIED，该模式会使课表初始状态不再填充父布局
                * */
                MeasureSpec.EXACTLY
            ),
            lp.topMargin + lp.bottomMargin, lp.height
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mInterceptingOnTouchListener = null
            mCourseTouchListener.forEach {
                if (it.isIntercept(event, this)) {
                    mInterceptingOnTouchListener = it
                }
            }
        }
        mInterceptingOnTouchListener?.onTouchEvent(event, this)
        return mInterceptingOnTouchListener != null
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mCourseDecoration.forEach {
            it.onDrawOver(canvas, this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCourseDecoration.forEach {
            it.onDraw(canvas, this)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return CourseLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is CourseLayoutParams -> CourseLayoutParams(lp)
            else -> throw CourseLayoutAttrsException("LayoutParams 必须是 CourseLayoutParams！")
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        throw CourseLayoutAttrsException("强制要求 LayoutParams 必须是 CourseLayoutParams！")
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is CourseLayoutParams
    }

    companion object {
        const val AM_TOP = 0
        const val AM_BOTTOM = 3
        const val NOON_TOP = 4
        const val NOON_BOTTOM = 4
        const val PM_TOP = 5
        const val PM_BOTTOM = 8
        const val DUSK_TOP = 9
        const val DUSK_BOTTOM = 9
        const val NIGHT_TOP = 10
        const val NIGHT_BOTTOM = 13

        const val TIME_LINE_LEFT = 0
        const val TIME_LINE_RIGHT = 0
    }
}