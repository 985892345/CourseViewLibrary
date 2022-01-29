package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.helper.CourseCreateAffairHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseFoldHelper
import com.mredrock.cyxbs.lib.courseview.utils.CourseLayoutContainer
import com.mredrock.cyxbs.lib.courseview.utils.WeekLayoutContainer

/**
 * ```
 * 该 View 作用：
 * 1、添加子 View
 * 2、与课表的操控行为强耦合
 * ```
 * [CourseView] ↘
 * -----------------------------------------------------------------------
 * |  [mWeek] ↘                                                          |
 * |  -----------------------------------------------------------------  |
 * |  |       |       |       |       |       |       |       |       |  |
 * |  | month |  mon  |  tue  |  wed  |  thu  |  fri  |  sat  |  sun  |  |
 * |  |       |       |       |       |       |       |       |       |  |
 * |  -----------------------------------------------------------------  |
 * |  [mCourse] ↘                                                        |
 * |  -----------------------------------------------------------------  |
 * |  | [CourseScrollView] ↗                                          |  |
 * |  |                                                               |  |
 * |  |  -----------------------------------------------------------  |  |
 * |  |  | [CourseLayout] ↗                                        |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  -----------------------------------------------------------  |  |
 * |  -----------------------------------------------------------------  |
 * -----------------------------------------------------------------------
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
*/
class CourseView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {

    private val mWeek = WeekLayoutContainer(this)
    private val mCourse = CourseLayoutContainer(this)

    init {
        orientation = VERTICAL
        initWeek()
        initCourse()
    }

    private fun initWeek() {
        addView(mWeek.layout)
    }

    private fun initCourse() {
        addView(mCourse.scrollView)
        CourseFoldHelper.attach(mCourse.layout)
        CourseCreateAffairHelper.attach(mCourse.layout)
    }
}