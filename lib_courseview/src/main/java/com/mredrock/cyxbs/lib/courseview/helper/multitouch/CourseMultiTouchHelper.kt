package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.ICourseLayout
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll.ScrollTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.MultiTouchDispatcherHelper
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView

/**
 * ## 使用 [attach] 来绑定 [CourseLayout]，更形象
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/20 20:58
 */
class CourseMultiTouchHelper(
    private val course: ICourseLayout,
    private val scroll: ICourseScrollView
): MultiTouchDispatcherHelper() {

    fun add(dispatcher: CoursePointerDispatcher) {
        dispatcher.course = course
        dispatcher.scroll = scroll
        dispatcher.initCourse(course)
        dispatcher.initScroll(scroll)
        super.addPointerDispatcher(dispatcher)
    }

    fun add(vararg dispatchers: CoursePointerDispatcher) {
        dispatchers.forEach { add(it) }
    }

    @Deprecated(
        "建议使用内聚程度更高的 CoursePointerDispatcher",
        ReplaceWith("add(dispatcher)"),
        DeprecationLevel.HIDDEN
    )
    override fun addPointerDispatcher(dispatcher: IPointerDispatcher) {
        if (dispatcher is CoursePointerDispatcher) {
            add(dispatcher)
        } else {
            super.addPointerDispatcher(dispatcher)
        }
    }

    override fun askOtherWantToHandle(event: IPointerEvent, view: View): IPointerTouchHandler? {
        if (event.pointerId != 1) {
            /*
            * 解释：如果不是第一个手指且没有任何 dispatcher 拦截，就直接交给 ScrollTouchHandler 处理
            * 为什么要去掉第一个手指？
            * 原因：
            *  1、为了让父布局直接拦截，而不是自身(View)拦截了事件
            * */
            return ScrollTouchHandler.get(scroll)
        }
        return null
    }

    /**
     * 专门用于 Course 的 PointerDispatcher
     */
    abstract class CoursePointerDispatcher : IPointerDispatcher {
        lateinit var scroll: ICourseScrollView
            internal set
        lateinit var course: ICourseLayout
            internal set

        internal open fun initScroll(scroll: ICourseScrollView) { }
        internal open fun initCourse(course: ICourseLayout) { }
    }
}