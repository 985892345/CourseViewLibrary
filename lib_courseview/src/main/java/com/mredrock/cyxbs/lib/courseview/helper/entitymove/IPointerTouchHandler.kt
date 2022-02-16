package com.mredrock.cyxbs.lib.courseview.helper.entitymove

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 13:38
 */
interface IPointerTouchHandler {
    val pointerId: Int
    fun isIntercept(event: MotionEvent): Boolean
    fun onTouchEvent(event: MotionEvent)
    /**
     * 在所有子 View 的 onDraw() 前的回调，在这里面绘图可以绘制在子 View 下方
     */
    fun onDrawBelow(canvas: Canvas) { }

    /**
     * 在所有子 View 的 onDraw() 后的回调，在这里面绘图可以绘制在子 View 上方
     */
    fun onDrawAbove(canvas: Canvas) { }

    interface IManger : Iterable<IPointerTouchHandler> {
        fun createHandlerIfCan(
            view: View,
            y: Int,
            pointerId: Int,
            absolutePointer: IAbsoluteCoordinates.IAbsolutePointer,
            scrollView: ICourseScrollView
        ): IPointerTouchHandler?

        fun getHandler(pointerId: Int): IPointerTouchHandler

        fun containHandler(pointerId: Int): Boolean
    }
}