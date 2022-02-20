package com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll

import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent

/**
 * 用于手动滚动 CourseScrollView 的多指事件处理者
 *
 * 在事件被拦截后，需要该处理者来实现手动滚动
 *
 * 注意事项：
 * 1、除了可以不在 Down 事件外，UP 或者 CANCEL 事件必须传递进来才能保证被下次使用
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 23:12
 */
internal class ScrollTouchHandler private constructor() : IPointerTouchHandler<CourseLayout> {

    override fun onPointerTouchEvent(event: IPointerEvent, view: CourseLayout) {
        when (event.action) {
            IPointerEvent.Action.MOVE -> {
                val pointer = view.getAbsolutePointer(event.pointerId)
                view.scrollView.scrollBy(-pointer.diffMoveY)
            }
            IPointerEvent.Action.UP,
            IPointerEvent.Action.CANCEL -> {
                /*
                * 这里记得要把 UP 和 CANCEL 事件传给他
                * */
                sPointerId = -1 // 还原
            }
            else -> {}
        }
    }

    companion object {
        /*
        * 只保留一个单例，这里不存在多线程竞争的问题，所以不用加锁
        * */
        private val INSTANCE: ScrollTouchHandler = ScrollTouchHandler()

        private var sPointerId: Int = -1

        /**
         * 获取 ScrollTouchHandler
         *
         * 会在已经被其他手指获取后返回 null，不然会出现多个手指对应同一个 ScrollTouchHandler，造成重复滚动
         */
        fun get(pointerId: Int): ScrollTouchHandler? {
            return if (pointerId != sPointerId) {
                INSTANCE
            } else {
                null
            }
        }
    }
}