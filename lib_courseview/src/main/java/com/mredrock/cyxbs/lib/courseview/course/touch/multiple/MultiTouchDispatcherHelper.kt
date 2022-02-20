package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*

/**
 * 处理多指触摸的帮助类
 *
 * 该类作用：
 * 1、将事件分发给需要拦截的 [IPointerDispatcher]，让他们来决定了同一种类型的事件谁来处理
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 20:44
 */
open class MultiTouchDispatcherHelper<V: ViewGroup> : AbstractMultiTouchDispatcher<V>() {

    fun addPointerDispatcher(dispatcher: IPointerDispatcher<V>) {
        mDispatchers.add(dispatcher)
    }

    // 全部分发者
    private val mDispatchers = ArrayList<IPointerDispatcher<V>>(5)
    // 延迟拦截当前手指事件的分发者
    private val mDelayDispatchers = SparseArray<IPointerDispatcher<V>>(5)

    override fun getInterceptHandler(
        event: IPointerEvent,
        view: V
    ): IPointerTouchHandler<V>? {
        if (event.event.actionMasked == MotionEvent.ACTION_DOWN) {
            mDelayDispatchers.clear()
        }
        when (event.action) {
            DOWN, MOVE -> return findPointerTouchHandler(event, view)
            else -> { /*剩下 UP 和 CANCEL 是不会被回调的*/ }
        }
        return null
    }

    /**
     * 寻找当前手指所触摸的状态对应的 [IPointerDispatcher]
     */
    private fun findPointerTouchHandler(event: IPointerEvent, view: V): IPointerTouchHandler<V>? {
        val dispatcher = mDelayDispatchers.get(event.pointerId, null)
        if (dispatcher != null) {
            val handler = dispatcher.getInterceptHandler(event, view)
            if (handler != null) {
                mDelayDispatchers.remove(event.pointerId)
                return handler
            }
        } else {
            mDispatchers.forEach {
                if (it.isPrepareToIntercept(event, view)) {
                    mDispatchers.forEach { dispatcher ->
                        if (dispatcher !== it) {
                            // 通知其他分发者，我抢夺了这个事件
                            dispatcher.onOtherDispatcherRobEvent(event, it)
                        }
                    }
                    val handler = it.getInterceptHandler(event, view)
                    return if (handler != null) {
                        handler
                    } else {
                        mDelayDispatchers.put(event.pointerId, it)
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
            // 为 CANCEL 的时候，说明被前一个 OnItemTouchListener 拦截或者被外布局拦截
            val dispatcher = mDelayDispatchers.get(event.pointerId, null)
            if (dispatcher != null) {
                dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
                mDelayDispatchers.remove(event.pointerId)
            }
        }
    }

    override fun onNoListenerUpEvent(
        event: IPointerEvent,
        view: V
    ) {
        val dispatcher = mDelayDispatchers.get(event.pointerId, null)
        if (dispatcher != null) {
            dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
            mDelayDispatchers.remove(event.pointerId)
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