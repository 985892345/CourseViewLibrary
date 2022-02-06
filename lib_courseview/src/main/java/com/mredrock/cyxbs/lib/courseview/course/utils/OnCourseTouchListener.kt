package com.mredrock.cyxbs.lib.courseview.course.utils

import android.view.MotionEvent
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
 * 1、DOWN: 在某一个 listener 的 isAdvanceIntercept() 返回 true 后，将把该 listener 赋值给 mInterceptingOnTouchListener
 *       ↓
 *       ↓
 *       ↓
 *   l1.isAdvanceIntercept() → → → → → → l2.isAdvanceIntercept() → → → → → → l3.isAdvanceIntercept()
 *       ↓                     false         ↓                     false         ↓
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *   l2.onCancelDownEvent                l3.onCancelDownEvent                    ↓
 *   l3.onCancelDownEvent                    ↓                                   ↓
 *       ↓                                   ↓                            mInterceptingOnTouchListener = l3
 *       ↓                                   ↓
 *       ↓                             mInterceptingOnTouchListener = l2
 *       ↓
 *   mInterceptingOnTouchListener = l1
 *
 * 2、MOVE: 在某一个 listener 的 isAdvanceIntercept() 返回 true 后，将把该 listener 赋值给 mInterceptingOnTouchListener
 *       ↓   注意：这里拦截是会拦截使用 isIntercept() 来拦截的 listener
 *       ↓
 *       ↓
 *   l1.isAdvanceIntercept() → → → → → → l2.isAdvanceIntercept() → → → → → → l3.isAdvanceIntercept()
 *       ↓                     false         ↓                     false         ↓
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *   l2.onTouchEvent(CANCEL)             l3.onTouchEvent(CANCEL)                 ↓
 *   l3.onTouchEvent(CANCEL)                 ↓                                   ↓
 *       ↓                                   ↓                            mInterceptingOnTouchListener = l3
 *       ↓                                   ↓
 *       ↓                             mInterceptingOnTouchListener = l2
 *       ↓
 *   mInterceptingOnTouchListener = l1
 *
 *
 *
 * 三、View.onTouchEvent:
 * 1、DOWN: 在 mInterceptingOnTouchListener = null 时，
 *       ↓       在某一个 listener 的 isIntercept() 返回 true 后，将把该 listener 赋值给 mInterceptingOnTouchListener
 *       ↓  在 != null 时，
 *       ↓       直接分配事件给 mInterceptingOnTouchListener
 *       ↓
 *       ↓                                       false
 *   if (mInterceptingOnTouchListener == null) --------> mInterceptingOnTouchListener.onTouchEvent()
 *       ↓
 *       ↓ true
 *       ↓
 *   l1.isIntercept() → → → → → → l2.isIntercept() → → → → → → l3.isIntercept()
 *       ↓               false         ↓              false         ↓
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
 * 2、MOVE: 这里直接把事件分配给已经拦截的 listener 处理，即 mInterceptingOnTouchListener
 *       ↓
 *       ↓
 *   mInterceptingOnTouchListener.onTouchEvent()
 *
 *   整体与 View 的事件分发有些类似，在 Down 事件中找到拦截的 View，在 Move 事件中直接把事件传递给它
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
interface OnCourseTouchListener {

    /**
     * 是否拦截事件。只会在 Down 事件中调用，即如果在 Down 时不拦截，则之后就不会再有拦截的机会 (除非使用 [isAdvanceIntercept] 来拦截)
     *
     * **NOTE：** 与 onInterceptTouchEvent 有部分相同之处，只是回调是在 View 的 onTouchEvent 里面调用的，
     * 所以是不能拦截子 View 的事件的
     */
    fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean = false

    /**
     * 是否提前拦截事件。可以在 Down 和 Move 中都拦截事件，且拦截后会拦截子 View 的事件，
     * 并且也会拦截使用 [isIntercept] 来拦截的 listener
     *
     * **NOTE：** 与 onInterceptTouchEvent 完全一样，只有一次返回 true 的机会，返回后就不会再次调用
     */
    fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean = false

    /**
     * 处理事件
     */
    fun onTouchEvent(event: MotionEvent, course: CourseLayout)

    /**
     * Down 事件中，被顺序在前面的 OnCourseTouchListener 拦截时回调
     */
    fun onCancelDownEvent(event: MotionEvent, course: CourseLayout) { }

    /**
     * 在 CourseLayout 的 dispatchTouchEvent() 中调用，即事件分发下来时就回调，
     * 每一个 OnCourseTouchListener 都可以收到
     */
    fun onDispatchTouchEvent(event: MotionEvent, course: CourseLayout) { }
}