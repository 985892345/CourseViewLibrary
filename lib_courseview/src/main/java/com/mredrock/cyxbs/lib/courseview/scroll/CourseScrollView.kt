package com.mredrock.cyxbs.lib.courseview.scroll

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import kotlin.math.max

/**
 * 包裹整个课表的一个 NestedScrollView
 * -----------------------------------------------
 * |
 * |
 * |
 * |
 * |
 * |
 * |          控件布局可以看 [R.layout.layout_course]（使用 xml 来布局，是为了方便以后修改）
 * |          里面包裹了一个 [CourseLayout]
 * |
 * |
 * |
 * |
 * |
 * |
 * ----------------------------------------------
 * ```
 * 该类有以下作用：
 * 1、NestedScrollView 自带的作用：解决嵌套滑动
 * 2、在触摸中如果 child 高度发生变化，自动修正 scrollY，使触摸点不发生偏移
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2021/9/20
 * @time 19:40
 */
class CourseScrollView(
    context: Context,
    attrs: AttributeSet
) : NestedScrollView(context, attrs) {

    /**
     * 重写该方法的几个原因：
     * 1、为了在 UNSPECIFIED 模式下，课表也能得到 NestedScrollView 的高度
     * 2、NestedScrollView 与 ScrollView 在对于子 View 高度处理时在下面这个方法不一样, 导致
     *    NestedScrollView 中子 View 必须使用具体的高度, 设置成 wrap_content 或 match_parent
     *    都将无效，具体的可以去看 ScrollView 和 NestedScrollView 中对于这同一方法的源码
     * 3、题外话：在 NestedScrollView 中嵌套 RecyclerView 会使 RecyclerView 的懒加载失效，直接原因就与
     *    这个方法有关，而使用 ScrollView 就不会造成懒加载失效的情况
     * 4、至于为什么 NestedScrollView 与 ScrollView 在该方法不同，我猜测原因是为了兼容以前的 Android 版本，
     *    在 ViewGroup#getChildMeasureSpec() 方法中可以发现使用了一个静态变量 sUseZeroUnspecifiedMeasureSpec
     *    来判断 UNSPECIFIED 模式下子 View 该得到的大小，但可能设计 NestedScrollView “偷懒”了，没有加这个东西
     */
    override fun measureChildWithMargins(
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams

        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width
        )
        val usedTotal = paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed
        val childHeightMeasureSpec: Int = MeasureSpec.makeMeasureSpec(
            max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotal),
            MeasureSpec.UNSPECIFIED
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    private var mInitialY = 0 // Down 时的初始 Y 值
    private var mLastMoveY = 0 // Move 时的移动 Y 值
    private var mOldChildHeight = 0 // 之前 child 的高度

    private var mIsInTouch = false // 是否处于触摸状态

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialY = y
                mLastMoveY = y
                mIsInTouch = true
            }
            MotionEvent.ACTION_MOVE -> {
                mLastMoveY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mIsInTouch = false
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        amendScrollY() // 在 child 被测量后再修正 scrollY
    }

    /**
     * ```
     * 该方法作用：
     * 1、在触摸时修正当前的 scrollY
     * 2、使当前触摸的点不会因控件高度变化而产生偏移
     *
     * 原因：
     * 1、在手指滑过中午和傍晚时间段时需要自动展开，此时控件高度会变化
     * 2、在展开时，ScrollView 是向下展开的，触摸点就会产生偏移
     * ```
     */
    private fun amendScrollY() {
        if (mOldChildHeight == 0) mOldChildHeight = getChildAt(0).height
        // 不处于触摸状态时不进行修正（所以在你提前抬手时，他会突然向下扩展）
        if (!mIsInTouch) return
        val nowChildHeight = getChildAt(0).height
        if (nowChildHeight != mOldChildHeight) {
            val add = nowChildHeight - mOldChildHeight // 用两次 child 的高度差来修正
            // 如果手指向下滑，就得让 ScrollView 向上扩展，所以 scrollY 得增加 child 的高度差
            scrollBy(0, if (mLastMoveY > mInitialY) add else 0)
            mOldChildHeight = nowChildHeight
        }
    }
}