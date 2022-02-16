package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.MotionEvent

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:30
 */
interface IPointerEvent {
    val event: MotionEvent
    val pointerIndex: Int
    val pointerId: Int
    val x: Float
    val y: Float
    val action: Action

    enum class Action {
        DOWN,
        MOVE,
        UP,
        CANCEL
    }
}