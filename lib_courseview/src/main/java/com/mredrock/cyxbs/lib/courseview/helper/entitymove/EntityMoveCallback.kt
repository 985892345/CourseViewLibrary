package com.mredrock.cyxbs.lib.courseview.helper.entitymove

import android.view.View

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 14:22
 */
interface EntityMoveCallback {
    fun isPrepareToEntityMove(child: View, pointerIndex: Int, pointerId: Int): Boolean
    fun onPlanCancelled(child: View, pointerIndex: Int, pointerId: Int)
}