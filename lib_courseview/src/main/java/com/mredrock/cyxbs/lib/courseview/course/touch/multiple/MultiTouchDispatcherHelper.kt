package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerEvent.Action.DOWN

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 20:44
 */
class MultiTouchDispatcherHelper<T: ViewGroup> : AbstractMultiTouchDispatcher<T>() {

    override fun getInterceptPointer(
        event: IPointerEvent,
        view: T
    ): OnPointerTouchListener<T>? {
        when (event.action) {
            DOWN -> {

            }
        }
    }

    override fun onRobEvent(
        event: IPointerEvent,
        interceptingListener: OnPointerTouchListener<T>?,
        view: T
    ) {
        TODO("Not yet implemented")
    }

    override fun onNoListenerUpEvent(
        event: IPointerEvent,
        view: T
    ) {
        TODO("Not yet implemented")
    }
}