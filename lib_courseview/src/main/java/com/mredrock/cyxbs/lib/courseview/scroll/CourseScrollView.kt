package com.mredrock.cyxbs.lib.courseview.scroll

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView
import kotlin.math.max

/**
 * 包裹整个课表的一个 NestedScrollView
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
     * 3、题外话：在 NestedScrollView 中嵌套 RecyclerView 会使 RecyclerView 的复用失效，直接原因就与
     *    这个方法有关，而使用 ScrollView 就不会造成复用失效的情况
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
}