package com.mredrock.cyxbs.lib.courseview.vertical

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.net.NetLayout
import com.mredrock.cyxbs.lib.courseview.vertical.attrs.VercitalLayoutParams

/**
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class VerticalLayout(
    context: Context,
    attrs: AttributeSet
) : NetLayout(context, attrs) {

    private val mWeightChildren = ArrayList<View>(5)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return VercitalLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is VercitalLayoutParams -> VercitalLayoutParams(lp)
            is MarginLayoutParams -> VercitalLayoutParams(lp)
            else -> VercitalLayoutParams(lp)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return VercitalLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is VercitalLayoutParams
    }
}

