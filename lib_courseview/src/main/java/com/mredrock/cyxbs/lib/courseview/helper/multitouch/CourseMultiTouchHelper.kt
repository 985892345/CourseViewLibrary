package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.MultiTouchDispatcherHelper

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/20 20:58
 */
class CourseMultiTouchHelper private constructor(): MultiTouchDispatcherHelper<CourseLayout>() {
    companion object {
        /**
         * 采用这种方式更能明白该类的作用
         */
        fun attach(course: CourseLayout): CourseMultiTouchHelper {
            return CourseMultiTouchHelper().apply {
                course.addCourseTouchListener(this) // 监听 course 的事件分发
            }
        }
    }
}