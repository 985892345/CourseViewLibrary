package com.ndhzs.courseviewlibrary.demo.widget.course

import com.mredrock.cyxbs.lib.courseview.CourseView
import com.ndhzs.courseviewlibrary.demo.bean.StuWeekBean

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/13 15:58
 */
class CourseHelper(val courseView: CourseView) {

    fun refresh(bean: StuWeekBean) {

    }

    init {
        initWeek()
        initCourse()
    }

    private val mWeek = courseView.getWeekContainer()
    private val mCourse = courseView.getCourseContainer()

    private fun initWeek() {
    }

    private fun initCourse() {

    }
}