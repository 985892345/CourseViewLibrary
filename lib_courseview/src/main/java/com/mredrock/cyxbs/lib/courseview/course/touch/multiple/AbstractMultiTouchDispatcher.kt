package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.graphics.Canvas
import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:09
 */
abstract class AbstractMultiTouchDispatcher<T : ViewGroup> :
    OnItemTouchListener<T> {

    private val mListenerById = SparseArray<OnPointerTouchListener<T>>(3)

    final override fun isAdvanceIntercept(event: MotionEvent, view: T): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mListenerById.clear() // 防止出现意外
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val listener = getInterceptPointer(pointerEvent, view)
                if (listener != null) {
                    onRobEvent(pointerEvent, listener, view)
                    mListenerById.put(id, listener)
                    return true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val listener = getInterceptPointer(pointerEvent, view)
                if (listener != null) {
                    onRobEvent(pointerEvent, listener, view)
                    mListenerById.put(id, listener)
                    // 因为这里 return true 之后不会在 onTouchEvent() 中回调 POINTER_DOWN，所以需要自己手动调用
                    listener.onPointerTouchEvent(pointerEvent, view)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                var isIntercept = false
                // 这里需要遍历完所有的手指，询问是否有要拦截的 listener，可能要拦截的不止一个
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    val listener = getInterceptPointer(pointerEvent, view)
                    if (listener != null) {
                        onRobEvent(pointerEvent, listener, view)
                        mListenerById.put(id, listener)
                        isIntercept = true
                    }
                }
                return isIntercept
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                onNoListenerUpEvent(pointerEvent, view)
            }
            MotionEvent.ACTION_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                onNoListenerUpEvent(pointerEvent, view)
            }
            MotionEvent.ACTION_CANCEL -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    // 在 isAdvanceIntercept() 中的 CANCEL 说明一直没有 listener 拦截事件
                    onRobEvent(pointerEvent, null, view)
                }
            }
        }
        return false
    }

    final override fun onTouchEvent(event: MotionEvent, view: T) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 走到这里说明之前在 isAdvanceIntercept() 的 DOWN 事件 return true 了
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                mListenerById[id].onPointerTouchEvent(pointerEvent, view)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // 走到这一步说明之前在 isAdvanceIntercept() 的 DOWN 事件 return true 了
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val listener = getInterceptPointer(pointerEvent, view)
                if (listener != null) {
                    onRobEvent(pointerEvent, listener, view)
                    mListenerById.put(id, listener)
                    listener.onPointerTouchEvent(pointerEvent, view)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    val interceptingListener = mListenerById.get(id, null)
                    if (interceptingListener == null) {
                        val listener = getInterceptPointer(pointerEvent, view)
                        if (listener != null) {
                            onRobEvent(pointerEvent, listener, view)
                            mListenerById.put(id, listener)
                            listener.onPointerTouchEvent(pointerEvent, view)
                        }
                    } else {
                        interceptingListener.onPointerTouchEvent(pointerEvent, view)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val interceptingListener = mListenerById[id]
                if (interceptingListener != null) {
                    interceptingListener.onPointerTouchEvent(pointerEvent, view)
                } else {
                    onNoListenerUpEvent(pointerEvent, view)
                }
                mListenerById.remove(id)
            }
            MotionEvent.ACTION_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val interceptingListener = mListenerById[id]
                if (interceptingListener != null) {
                    interceptingListener.onPointerTouchEvent(pointerEvent, view)
                } else {
                    onNoListenerUpEvent(pointerEvent, view)
                }
                mListenerById.remove(id)
            }
            MotionEvent.ACTION_CANCEL -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    onRobEvent(pointerEvent, mListenerById.get(id, null), view)
                }
                mListenerById.clear()
            }
        }
    }

    final override fun isIntercept(event: MotionEvent, view: T): Boolean = false

    abstract fun getInterceptPointer(event: IPointerEvent, view: T): OnPointerTouchListener<T>?

    /**
     * 当前 [event] 对应的事件被其他 [OnPointerTouchListener] 或者是被前面的 [OnItemTouchListener] 抢夺
     */
    abstract fun onRobEvent(
        event: IPointerEvent,
        interceptingListener: OnPointerTouchListener<T>?,
        view: T
    )

    abstract fun onNoListenerUpEvent(
        event: IPointerEvent,
        view: T
    )
}