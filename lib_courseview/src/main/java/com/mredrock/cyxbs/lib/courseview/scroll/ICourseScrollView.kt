package com.mredrock.cyxbs.lib.courseview.scroll

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 14:34
 */
interface ICourseScrollView {
    fun scrollBy(dy: Int)
    fun getScrollY(): Int
    fun setScrollY(y: Int)
    fun getHeight(): Int
    val innerHeight: Int
    fun smoothScrollTo(x: Int, y: Int, scrollDurationMs: Int = 250)
    fun smoothScrollBy(dx: Int, dy: Int, scrollDurationMs: Int = 250)
}