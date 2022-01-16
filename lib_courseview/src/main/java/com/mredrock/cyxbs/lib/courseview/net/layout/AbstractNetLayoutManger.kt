package com.mredrock.cyxbs.lib.courseview.net.layout

import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/16
 */
abstract class AbstractNetLayoutManger(
    attrs: NetLayoutAttrs
) {
    abstract fun addView(child: View, childAttrs: INetChildAttrs)
    abstract fun measureWidth(wSpec: Int, hSpec: Int): Int
    abstract fun measureHeight(wSpec: Int, hSpec: Int): Int
    abstract fun layout(changed: Boolean, l: Int, t: Int, r: Int, b: Int)

    interface INetChildAttrs {
        val position: Int
        val start: Int
        val end: Int
    }
}