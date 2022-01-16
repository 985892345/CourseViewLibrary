package com.mredrock.cyxbs.lib.courseview.net.layout.column

import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.layout.AbstractNetLayoutManger

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/16
 */
class ColumnLayoutManger(
    attrs: NetLayoutAttrs
) : AbstractNetLayoutManger(attrs) {

    private val lines = ArrayList<ColumnLineLayout>(attrs.minColumn).apply {
        repeat(size) { add(ColumnLineLayout()) }
    }

    override fun addView(child: View, childAttrs: INetChildAttrs) {
        if (childAttrs.position > 0) {
            lines[childAttrs.position].addView(child, childAttrs)
        }
    }

    override fun measureWidth(wSpec: Int, hSpec: Int): Int {
        TODO("Not yet implemented")
    }

    override fun measureHeight(wSpec: Int, hSpec: Int): Int {
        TODO("Not yet implemented")
    }

    override fun layout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }
}