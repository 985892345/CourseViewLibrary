package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 20:44
 */
class MultiTouchDispatcherHelper<V: ViewGroup> : AbstractMultiTouchDispatcher<V>() {

    fun addPointerDispatcher(dispatcher: IPointerDispatcher<V>) {
        mDispatchers.add(dispatcher)
    }

    private val mDispatchers = ArrayList<IPointerDispatcher<V>>(5)
    private val mPrepareDispatchers = SparseArray<IPointerDispatcher<V>>(5)

    override fun getInterceptHandler(
        event: IPointerEvent,
        view: V
    ): IPointerTouchHandler<V>? {
        if (event.event.action == MotionEvent.ACTION_DOWN) {
            mPrepareDispatchers.clear()
        }
        when (event.action) {
            DOWN, MOVE -> return findPointerTouchHandler(event, view)
            else -> { /*剩下 UP 和 CANCEL 是不会被回调的*/ }
        }
        return null
    }

    private fun findPointerTouchHandler(event: IPointerEvent, view: V): IPointerTouchHandler<V>? {
        val dispatcher = mPrepareDispatchers.get(event.pointerId, null)
        if (dispatcher != null) {
            val handler = dispatcher.getInterceptHandler(event, view)
            if (handler != null) {
                mPrepareDispatchers.remove(event.pointerId)
                return handler
            }
        } else {
            mDispatchers.forEach {
                if (it.isPrepareToIntercept(event, view)) {
                    val handler = it.getInterceptHandler(event, view)
                    return if (handler != null) {
                        handler
                    } else {
                        mPrepareDispatchers.put(event.pointerId, it)
                        null
                    }
                }
            }

        }
        return null
    }

    override fun onRobEvent(
        event: IPointerEvent,
        handler: IPointerTouchHandler<V>?,
        view: V
    ) {
        if (event.action == CANCEL) {
            val dispatcher = mPrepareDispatchers.get(event.pointerId, null)
            if (dispatcher != null) {
                dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
                mPrepareDispatchers.remove(event.pointerId)
            }
        }
    }

    override fun onNoListenerUpEvent(
        event: IPointerEvent,
        view: V
    ) {
        val dispatcher = mPrepareDispatchers.get(event.pointerId, null)
        if (dispatcher != null) {
            dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
            mPrepareDispatchers.remove(event.pointerId)
        }
    }

    override fun onCancelDownEvent(event: MotionEvent, view: V) {
        mDispatchers.forEach {
            it.onCancelDownEvent(event, view)
        }
    }

    override fun onDispatchTouchEvent(event: MotionEvent, view: V) {
        mDispatchers.forEach {
            it.onDispatchTouchEvent(event, view)
        }
    }
}