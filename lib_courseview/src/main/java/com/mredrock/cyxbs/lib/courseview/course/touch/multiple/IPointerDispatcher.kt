package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent

/**
 * 每个手指的触摸事件分发类
 *
 * 该类作用：
 * 1、判断是否需要拦截当前手指的事件
 * 2、用于集结同一类型事件的分发，比如：长按移动和长按生成事务，这属于不同类型的事件，需要写两个 [IPointerDispatcher]
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/17 10:57
 */
interface IPointerDispatcher<V: ViewGroup> {
    /**
     * 是否准备拦截，如果返回 true 的话，则将会把当前手指对应的事件以后都直接分发给自己
     */
    fun isPrepareToIntercept(event: IPointerEvent, view: V): Boolean

    /**
     * 得到该哪个 [IPointerTouchHandler] 处理事件
     *
     * 1、如果 [isPrepareToIntercept] 返回 true 后，则立马会调用该函数
     * 2、如果需要延后才能处理事件，则可以返回 null
     */
    fun getInterceptHandler(event: IPointerEvent, view: V): IPointerTouchHandler<V>?

    /**
     * Down 事件中，被顺序在前面的 OnItemTouchListener 拦截时回调
     * ```
     * 注：只能接收到 Down 事件
     * ```
     */
    fun onCancelDownEvent(event: MotionEvent, view: V) { }

    /**
     * 在 CourseLayout 的 dispatchTouchEvent() 中调用，即事件分发下来时就回调，
     * 每一个 OnItemTouchListener 都可以收到
     * ```
     * 注：可以接收到 Down、Move、Cancel 等全部事件
     * ```
     */
    fun onDispatchTouchEvent(event: MotionEvent, view: V) { }
}