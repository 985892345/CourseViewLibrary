package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.MotionEvent

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:29
 */
object PointerEventImpl : IPointerEvent {
    override lateinit var event: MotionEvent
    override var pointerIndex: Int = -1
    override var pointerId: Int = -1
    override val x: Float
        get() = event.getX(pointerIndex)
    override val y: Float
        get() = event.getY(pointerIndex)
    override val action: IPointerEvent.Action
        get() = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> IPointerEvent.Action.DOWN
            MotionEvent.ACTION_POINTER_DOWN -> IPointerEvent.Action.DOWN
            MotionEvent.ACTION_MOVE -> IPointerEvent.Action.MOVE
            MotionEvent.ACTION_POINTER_UP -> IPointerEvent.Action.UP
            MotionEvent.ACTION_UP -> IPointerEvent.Action.UP
            MotionEvent.ACTION_CANCEL -> IPointerEvent.Action.CANCEL
            else -> IPointerEvent.Action.CANCEL
        }
}

fun MotionEvent.toPointerEvent(
    pointerIndex: Int,
    pointerId: Int
) : IPointerEvent = PointerEventImpl.also {
    it.event = this
    it.pointerIndex = pointerIndex
    it.pointerId = pointerId
}