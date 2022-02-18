package com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll

import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractTouchHandler
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerState

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 23:12
 */
class ScrollTouchHandler private constructor() : AbstractTouchHandler<CourseLayout>() {

    override var state: PointerState = PointerState.OVER

    override fun onPointerTouchEvent(event: IPointerEvent, view: CourseLayout) {
        when (event.action) {
            IPointerEvent.Action.MOVE -> {
                val pointer = view.getAbsolutePointer(event.pointerId)
                view.scrollView.scrollBy(-pointer.diffMoveY)
            }
            IPointerEvent.Action.UP,
            IPointerEvent.Action.CANCEL -> {
                state = PointerState.OVER
            }
            else -> {}
        }
    }

    companion object {
        /*
        * 只保留一个单例，这里不存在多线程竞争的问题，所以不用加锁
        * */
        private val INSTANCE: ScrollTouchHandler = ScrollTouchHandler()

        /**
         * 获取 ScrollTouchHandler
         *
         * 会在已经被其他手指获取后返回 null，不然会出现多个手指对应同一个 ScrollTouchHandler 的情况
         */
        fun get(): ScrollTouchHandler? {
            return if (INSTANCE.state == PointerState.OVER) {
                INSTANCE.state = PointerState.START
                INSTANCE
            } else {
                null
            }
        }
    }
}