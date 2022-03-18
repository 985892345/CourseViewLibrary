package com.mredrock.cyxbs.lib.courseview.net.callback

import com.mredrock.cyxbs.lib.courseview.net.utils.SideType

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/25
 */
fun interface OnWeightChangeListener {
    /**
     * @param oldWeight 之前的比重
     * @param newWeight 现在的比重
     * @param which 第几条边
     * @param sideType 边的类型
     */
    fun onChange(oldWeight: Float, newWeight: Float, which: Int, sideType: SideType)
}