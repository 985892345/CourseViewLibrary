package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.toPointerEvent

/**
 * 用于分离多指事件的抽离层
 *
 * 该类作用：
 * 1、将多个手指的事件单独分离出来，实现像一个手指一样的操作
 *
 * 该操作分为：DOWN、MOVE、UP、CANCEL 四个事件，就像你直接使用 event.action 来处理事件的逻辑一样，
 * 其中我把事件分发进行了一层封装，把每个手指的事件交给 [IPointerTouchHandler] 来处理
 *
 * 该类的分发逻辑虽然不肯绝对保证，但 99% 是完善的，如果你对一般的事件处理都不熟悉，建议你先熟悉了再来看
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:09
 */
abstract class AbstractMultiTouchDispatcher<V : ViewGroup> : OnItemTouchListener<V> {

    private val mHandlerById = SparseArray<IPointerTouchHandler<V>>(3)

    final override fun isAdvanceIntercept(event: MotionEvent, view: V): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mHandlerById.clear() // 防止出现意外
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val handler = getInterceptHandler(pointerEvent, view)
                if (handler != null) {
                    onRobEvent(pointerEvent, handler, view)
                    mHandlerById.put(id, handler)
                    // 这里 return true 后以后的事件就直接回调 onTouchEvent()
                    return true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val handler = getInterceptHandler(pointerEvent, view)
                if (handler != null) {
                    onRobEvent(pointerEvent, handler, view)
                    mHandlerById.put(id, handler)
                    // 因为这里 return true 之后不会在 onTouchEvent() 中回调 POINTER_DOWN，所以需要自己手动调用
                    handler.onPointerTouchEvent(pointerEvent, view)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                var isIntercept = false
                // 这里需要遍历完所有的手指，询问是否有要拦截的 handler，可能要拦截的不止一个
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    val handler = getInterceptHandler(pointerEvent, view)
                    if (handler != null) {
                        onRobEvent(pointerEvent, handler, view)
                        mHandlerById.put(id, handler)
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
                    // 在 isAdvanceIntercept() 中的 CANCEL 说明一直没有 handler 拦截事件
                    onRobEvent(pointerEvent, null, view)
                }
            }
        }
        return false
    }

    final override fun onTouchEvent(event: MotionEvent, view: V) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 走到这里说明之前在 isAdvanceIntercept() 的 DOWN 事件 return true 了
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                mHandlerById[id].onPointerTouchEvent(pointerEvent, view)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // 走到这一步说明之前在 isAdvanceIntercept() 的 DOWN 事件 return true 了
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val handler = getInterceptHandler(pointerEvent, view)
                if (handler != null) {
                    onRobEvent(pointerEvent, handler, view)
                    mHandlerById.put(id, handler)
                    handler.onPointerTouchEvent(pointerEvent, view)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    var handler = mHandlerById.get(id, null)
                    if (handler == null) {
                        handler = getInterceptHandler(pointerEvent, view)
                        if (handler != null) {
                            onRobEvent(pointerEvent, handler, view)
                            mHandlerById.put(id, handler)
                            handler.onPointerTouchEvent(pointerEvent, view)
                        }
                    } else {
                        handler.onPointerTouchEvent(pointerEvent, view)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val handler = mHandlerById[id]
                if (handler != null) {
                    handler.onPointerTouchEvent(pointerEvent, view)
                } else {
                    onNoListenerUpEvent(pointerEvent, view)
                }
                mHandlerById.remove(id)
            }
            MotionEvent.ACTION_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val pointerEvent = event.toPointerEvent(index, id)
                val handler = mHandlerById[id]
                if (handler != null) {
                    handler.onPointerTouchEvent(pointerEvent, view)
                } else {
                    onNoListenerUpEvent(pointerEvent, view)
                }
                mHandlerById.remove(id)
            }
            MotionEvent.ACTION_CANCEL -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    val pointerEvent = event.toPointerEvent(index, id)
                    val handler = mHandlerById[id]
                    // 有 handler 就直接分发给 handler，没有就回调 onRobEvent() 方法
                    if (handler != null) {
                        handler.onPointerTouchEvent(pointerEvent, view)
                    } else {
                        onRobEvent(pointerEvent, null, view)
                    }
                }
                mHandlerById.clear()
            }
        }
    }

    final override fun isIntercept(event: MotionEvent, view: V): Boolean = false

    /**
     * 询问当前手指谁来拦截，要拦截就交出处理者
     */
    protected abstract fun getInterceptHandler(
        event: IPointerEvent,
        view: V
    ): IPointerTouchHandler<V>?

    /**
     * 1、当前手指被拦截时，回调；
     *
     * 2、被前面的 [OnItemTouchListener] 拦截时，事件类型为 CANCEL；
     *
     * 3、被外布局拦截时，回调，事件类型为 CANCEL；
     */
    protected abstract fun onRobEvent(
        event: IPointerEvent,
        handler: IPointerTouchHandler<V>?,
        view: V
    )

    /**
     * 当前手指还没有被拦截就抬起的回调
     */
    protected abstract fun onNoListenerUpEvent(
        event: IPointerEvent,
        view: V
    )
}