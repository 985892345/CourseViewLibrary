package com.mredrock.cyxbs.lib.courseview.net.layout.column

import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.layout.AbstractNetLayoutManger

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/16
 */
class ColumnLineLayout {

    val children = mutableListOf<Child>()

    fun addView(child: View, childAttrs: AbstractNetLayoutManger.INetChildAttrs) {
        children.add(Child(child, childAttrs))
    }

    fun measureWidth(wSpec: Int, hSpec: Int): Int {

    }

    fun measureHeight(wSpec: Int, hSpec: Int): Int {

    }

    class Child(
        val view: View,
        val attrs: AbstractNetLayoutManger.INetChildAttrs
    )
}