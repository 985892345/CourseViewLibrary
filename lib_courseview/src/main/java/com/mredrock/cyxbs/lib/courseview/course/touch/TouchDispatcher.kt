package com.mredrock.cyxbs.lib.courseview.course.touch

import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 14:55
 */
class TouchDispatcher<T: ViewGroup> {
    // 自定义事件处理的监听
    private val mItemTouchListener = ArrayList<OnItemTouchListener<T>>(5)
    // 自定义事件处理中拦截的监听者
    private var mInterceptingOnTouchListener: OnItemTouchListener<T>? = null
    // 自定义事件处理中提前拦截的监听者
    private var mAdvanceInterceptingOnTouchListener: OnItemTouchListener<T>? = null

    val size: Int
        get() = mItemTouchListener.size

    fun addCourseTouchListener(l: OnItemTouchListener<T>, index: Int = size) {
        mItemTouchListener.add(index, l)
    }

    fun dispatchTouchEvent(event: MotionEvent, view: T) {
        mItemTouchListener.forEach {
            it.onDispatchTouchEvent(event, view)
        }
    }

    fun onInterceptTouchEvent(event: MotionEvent, view: T): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            mAdvanceInterceptingOnTouchListener = null // 重置
            var isIntercept = false
            mItemTouchListener.forEach { listener ->
                if (!isIntercept) {
                    if (listener.isAdvanceIntercept(event, view)) {
                        mAdvanceInterceptingOnTouchListener = listener
                        isIntercept = true
                        val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
                        // 通知前面已经分发 Down 事件了的 listener 取消事件
                        for (i in mItemTouchListener.indices) {
                            val l = mItemTouchListener[i]
                            if (l !== listener) {
                                l.isAdvanceIntercept(cancelEvent, view)
                            } else {
                                break
                            }
                        }
                        event.action = action // 还原
                    }
                } else {
                    // 通知后面没有收到 Down 事件的 listener，Down 事件被前面的 listener 拦截
                    listener.onCancelDownEvent(event, view)
                }
            }
            return isIntercept
        } else {
            /*
            * 走到这一步说明：
            * 1、mInterceptingOnTouchListener 一定为 null
            *   （如果 mInterceptingOnTouchListener 不为 null，则说明：
            *      1、没有子 View 拦截事件；
            *      2、CourseLayout 自身拦截了事件
            *      ==> onInterceptTouchEvent() 不会再被调用，也就不会再走到这一步）
            * 2、事件一定被子 View 拦截
            * 3、mAdvanceInterceptingOnTouchListener 也一定为 null
            * */
            mItemTouchListener.forEach { listener ->
                if (listener.isAdvanceIntercept(event, view)) {
                    mAdvanceInterceptingOnTouchListener = listener
                    val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
                    // 因为之前所有 listener 都通知了 Down 事件，所以需要全部都通知取消事件
                    mItemTouchListener.forEach {
                        if (it !== listener) {
                            it.isAdvanceIntercept(cancelEvent, view)
                        }
                    }
                    event.action = action // 恢复
                    return true
                }
            }
        }
        return false
    }

    fun onTouchEvent(event: MotionEvent, view: T): Boolean {
        if (mAdvanceInterceptingOnTouchListener != null) {
            mAdvanceInterceptingOnTouchListener!!.onTouchEvent(event, view)
            return true
        }
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            mInterceptingOnTouchListener = null // 重置
            // 分配自定义事件处理的监听
            mItemTouchListener.forEach {
                if (mInterceptingOnTouchListener == null) {
                    if (it.isIntercept(event, view)) {
                        mInterceptingOnTouchListener = it
                    }
                } else {
                    it.onCancelDownEvent(event, view)
                }
            }
        } else {
            /*
            * 走到这里说明：
            * 1、Down 事件中没有提前拦截的 listener，即 mAdvanceInterceptingOnTouchListener 为 null
            * 2、Down 事件中没有任何子 View 拦截
            * 3、CourseLayout 自身拦截事件
            * 4、因为自身拦截事件，onInterceptTouchEvent() 不会再被调用
            * */
            mItemTouchListener.forEach { listener ->
                if (listener.isAdvanceIntercept(event, view)) {
                    mAdvanceInterceptingOnTouchListener = listener
                    listener.onTouchEvent(event, view)
                    val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
                    if (mInterceptingOnTouchListener !== listener) {
                        // 如果不是同一个就通知 mInterceptingOnTouchListener CANCEL 事件
                        mInterceptingOnTouchListener?.onTouchEvent(cancelEvent, view)
                    }
                    // 因为之前所有 listener 都通知了 Down 事件，所以需要全部都通知取消事件
                    mItemTouchListener.forEach {
                        if (it !== listener) {
                            it.isAdvanceIntercept(cancelEvent, view)
                        }
                    }
                    event.action = action // 恢复
                    return true
                }
            }
        }
        mInterceptingOnTouchListener?.onTouchEvent(event, view)
        return true
    }
}