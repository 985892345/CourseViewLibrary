package com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll

import android.util.Log
import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView

/**
 * ## 用于手动滚动 [CourseScrollView] 的多指事件处理者
 *
 * ### 该类作用：
 * - 在事件被多指拦截后，需要该处理者来实现手动滚动
 *
 * ### 注意事项：
 * - 除了可以 Down 事件外，UP 或者 CANCEL 事件必须传递进来才能保证被下次使用。意思是只会有一个手指才能实现滑动
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 23:12
 */
internal class ScrollTouchHandler private constructor() : IPointerTouchHandler {

    private var mScroll: ICourseScrollView? = null
    private var mHandlingPointerId = -1

    override fun onPointerTouchEvent(event: IPointerEvent, view: View) {
        if (mHandlingPointerId != event.pointerId) {
            if (mHandlingPointerId == -1) {
                mHandlingPointerId = event.pointerId
            } else {
                // 已经被一个手指拦截，所以不处理事件
                return
            }
        }
        mScroll?.let {
            when (event.action) {
                IPointerEvent.Action.MOVE -> {
                    val pointer = it.getPointer(event.pointerId)
                    it.scrollBy(-pointer.diffMoveY)
                }
                IPointerEvent.Action.UP,
                IPointerEvent.Action.CANCEL -> {
                    /*
                    * 这里记得要把 UP 和 CANCEL 事件传给他用于还原
                    * */
                    mScroll = null
                    mHandlingPointerId = -1
                }
                else -> {}
            }
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
         * 会在已经被其他手指获取后返回 null，不然会出现多个手指对应同一个 ScrollTouchHandler，造成重复滚动
         */
        fun get(scroll: ICourseScrollView): ScrollTouchHandler {
            INSTANCE.mScroll = scroll
            return INSTANCE
        }
    }
}