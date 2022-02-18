package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/17 10:57
 */
interface IPointerDispatcher<V: ViewGroup> {
    fun isPrepareToIntercept(event: IPointerEvent, view: V): Boolean
    fun getInterceptHandler(event: IPointerEvent, view: V): IPointerTouchHandler<V>?

    /**
     * Down 事件中，被顺序在前面的 OnCourseTouchListener 拦截时回调
     * ```
     * 注：只能接收到 Down 事件
     * ```
     */
    fun onCancelDownEvent(event: MotionEvent, view: V) { }

    /**
     * 在 CourseLayout 的 dispatchTouchEvent() 中调用，即事件分发下来时就回调，
     * 每一个 OnCourseTouchListener 都可以收到
     * ```
     * 注：可以接收到 Down、Move、Cancel 等全部事件
     * ```
     */
    fun onDispatchTouchEvent(event: MotionEvent, view: V) { }
}