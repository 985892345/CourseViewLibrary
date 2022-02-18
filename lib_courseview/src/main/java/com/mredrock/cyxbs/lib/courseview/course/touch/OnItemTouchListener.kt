package com.mredrock.cyxbs.lib.courseview.course.touch

import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout

/**
 * 该类主要用于实现一些简单的触摸事件处理，将一个 View 的复杂的触摸功能分为几个 listener 的来实现，
 * 增强代码可读性和维护性
 *
 * 设计参考了 RV 的 ItemTouchListener，但与它又有些不同。RV 的 ItemTouchListener 是想实现像 View 一般的事件
 * 监听，而针对于课表的需求，我更改为了一种更好的思路来分配事件
 * ```
 *
 * 例如：共设置了 3 个 listener，分别为 l1、l2、l3，且按顺序添加
 *
 * 一、View.dispatchTouchEvent: 事件总分发的地方
 *   DOWN、MOVE
 *       ↓
 *       ↓
 *       ↓
 *   l1.onDispatchTouchEvent
 *   l2.onDispatchTouchEvent
 *   l3.onDispatchTouchEvent
 *
 *
 * 二、View.onInterceptTouchEvent: 可以提前拦截子 View 事件的地方
 * 1、DOWN: 在某一个 listener 的 isAdvanceIntercept() 返回 true 后，
 *       ↓     将把该 listener 赋值给 mAdvanceInterceptingOnTouchListener
 *       ↓
 *       ↓
 *       ↓
 *   l1.isAdvanceIntercept() → → → → → → l2.isAdvanceIntercept() → → → → → → l3.isAdvanceIntercept()
 *       ↓                     false         ↓                     false         ↓
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *   l2.onCancelDownEvent                l3.onCancelDownEvent                    ↓
 *   l3.onCancelDownEvent                    ↓                                   ↓
 *       ↓                                   ↓                            mAdvanceInterceptingOnTouchListener = l3
 *       ↓                                   ↓               通知前面分发了 Down 事件的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓                                   ↓
 *       ↓                             mAdvanceInterceptingOnTouchListener = l2
 *       ↓                通知前面分发了 Down 事件的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓
 *   mAdvanceInterceptingOnTouchListener = l1
 *   通知前面分发了 Down 事件的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *
 * 2、MOVE: 在某一个 listener 的 isAdvanceIntercept() 返回 true 后，
 *       ↓      将把该 listener 赋值给 mAdvanceInterceptingOnTouchListener
 *       ↓           这里可以拦截已经被子 View 拦截的事件
 *       ↓
 *   l1.isAdvanceIntercept() → → → → → → l2.isAdvanceIntercept() → → → → → → l3.isAdvanceIntercept()
 *       ↓                     false         ↓                     false         ↓
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *       ↓                                   ↓                            mAdvanceInterceptingOnTouchListener = l3
 *       ↓                                   ↓            通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓                                   ↓
 *       ↓                             mAdvanceInterceptingOnTouchListener = l2
 *       ↓             通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓
 *   mAdvanceInterceptingOnTouchListener = l1
 *   通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *
 *
 *
 * 三、View.onTouchEvent:
 * 1、DOWN: 在 mAdvanceInterceptingOnTouchListener = null 时，
 *       ↓       则某一个 listener 的 isIntercept() 返回 true 后，将把该 listener 赋值给 mInterceptingOnTouchListener
 *       ↓  在 != null 时，
 *       ↓       直接分配事件给 mAdvanceInterceptingOnTouchListener
 *       ↓
 *       ↓                                              false
 *   if (mAdvanceInterceptingOnTouchListener == null) --------> mAdvanceInterceptingOnTouchListener.onTouchEvent()
 *       ↓
 *       ↓ true
 *       ↓
 *   l1.isIntercept() → → → → → → l2.isIntercept() → → → → → → l3.isIntercept() → → → → → → return true
 *       ↓               false         ↓              false         ↓              false
 *       ↓ true                        ↓ true                       ↓ true
 *       ↓                             ↓                            ↓
 *       ↓                             ↓                            ↓
 *       ↓                             ↓                            ↓
 *   l1.onTouchEvent              l2.onTouchEvent              l3.onTouchEvent
 *   l2.onCancelDownEvent         l3.onCancelDownEvent              ↓
 *   l3.onCancelDownEvent              ↓                            ↓
 *       ↓                             ↓              mInterceptingOnTouchListener = l3
 *       ↓                             ↓
 *       ↓               mInterceptingOnTouchListener = l2
 *       ↓
 *   mInterceptingOnTouchListener = l1
 *
 * 2、MOVE: 如果有提前拦截的 mAdvanceInterceptingOnTouchListener，就直接交给它处理
 *       ↓      没有就会询问一遍是否有 listener 要提前拦截，有的话就赋值给 mAdvanceInterceptingOnTouchListener
 *       ↓           如果询问完后都没有，这时才会把事件交给 mInterceptingOnTouchListener 处理
 *       ↓
 *       ↓                                              false
 *   if (mAdvanceInterceptingOnTouchListener == null) --------> mAdvanceInterceptingOnTouchListener.onTouchEvent()
 *       ↓
 *       ↓ true  接下来会重新走一遍在 View.onInterceptTouchEvent 中 Move 的逻辑
 *       ↓
 *   l1.isAdvanceIntercept() → → → → → → l2.isAdvanceIntercept() → → → → → → l3.isAdvanceIntercept() → → → → → → mInterceptingOnTouchListener?.onTouchEvent()
 *       ↓                     false         ↓                     false         ↓                     false
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *       ↓                                   ↓                     mAdvanceInterceptingOnTouchListener = l3
 *       ↓                                   ↓                     mInterceptingOnTouchListener?.onTouchEvent(CANCEL)
 *       ↓                                   ↓          通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓                                   ↓
 *       ↓                     mAdvanceInterceptingOnTouchListener = l2
 *       ↓                     mInterceptingOnTouchListener?.onTouchEvent(CANCEL)
 *       ↓          通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *       ↓
 *   mAdvanceInterceptingOnTouchListener = l1
 *   mInterceptingOnTouchListener?.onTouchEvent(CANCEL)
 *   通知所有除自己外的 listener 取消事件: listener.isAdvanceIntercept(CANCEL)
 *
 *   整体与 View 的事件分发有些类似，在 Down 事件中找到拦截的 View，在 Move 事件中直接把事件传递给它
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
interface OnItemTouchListener<V: ViewGroup> {

    /**
     * 是否拦截事件。
     *
     * 只会在子 View 不拦截时的 Down 事件中调用，即如果在 Down 时不拦截，则之后就不会再有拦截的机会
     * (除非使用 [isAdvanceIntercept] 来拦截)
     *
     * **NOTE：** 与 onInterceptTouchEvent 有部分相同之处，但回调是在 View 的 onTouchEvent 里面调用的，
     * 所以是不能提前拦截子 View 的事件的，只会在子 View 不拦截时才会轮到回调这个方法
     * ```
     * 注：只能接收到子 View 不拦截时的 Down 事件
     * ```
     */
    fun isIntercept(event: MotionEvent, view: V): Boolean = false

    /**
     * 是否提前拦截事件。
     *
     * 可以在 Down 和 Move 中都拦截事件，且拦截后会拦截子 View 的事件，
     * 并且也会拦截使用 [isIntercept] 来拦截的 listener
     *
     * **NOTE：** 与 onInterceptTouchEvent 完全一样，**只有一次返回 true 的机会**，返回后就不会再次调用
     * ```
     * 注：可以接收到子 View 拦截前的 Down、Move、Cancel 等全部事件
     * ```
     */
    fun isAdvanceIntercept(event: MotionEvent, view: V): Boolean = false

    /**
     * 处理事件
     * ```
     * 注：可以接收到 Down、Move、Cancel 等全部事件
     * ```
     */
    fun onTouchEvent(event: MotionEvent, view: V)

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