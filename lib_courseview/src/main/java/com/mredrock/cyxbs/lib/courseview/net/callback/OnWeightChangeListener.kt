package com.mredrock.cyxbs.lib.courseview.net.callback

import com.mredrock.cyxbs.lib.courseview.net.utils.SideType

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/25
 */
fun interface OnWeightChangeListener {
    fun onChange(oldWeight: Float, newWeight: Float, which: Int, sideType: SideType)
}