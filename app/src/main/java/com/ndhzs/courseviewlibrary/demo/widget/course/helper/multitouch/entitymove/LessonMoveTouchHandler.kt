package com.ndhzs.courseviewlibrary.demo.widget.course.helper.multitouch.entitymove

import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView

/**
 * 长按课程整体移动的事件处理者
 *
 * 该类作用：
 * 1、绑定一根手指的事件；
 * 2、封装长按移动课程的功能
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/20 16:15
 */
internal class LessonMoveTouchHandler(
    scroll: ICourseScrollView,
    course: AbstractCourseLayout,
    dispatcher: EntityMovePointerDispatcher
) : AffairMoveTouchHandler(scroll, course, dispatcher) {

    /**
     * 课程的移动只有展示下面隐藏课程的作用，所以松手就直接回到原位置
     */
    override fun changeLocationIfNecessary(isFinalUpEvent: Boolean) {
        restoreAffairViewToOldLocation(isFinalUpEvent)
    }
}