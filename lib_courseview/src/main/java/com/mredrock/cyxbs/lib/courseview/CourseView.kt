package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.helper.CourseCreateAffairHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseFoldHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseLongPressAffairHelper
import com.mredrock.cyxbs.lib.courseview.utils.CourseLayoutContainer
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
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
        CourseLongPressAffairHelper.attach(mCourse.layout)

        mCourse.layout.DEBUG = true

        val v1 = View(context).apply {
            setBackgroundColor(Color.BLUE)
        }
        val v2 = View(context).apply {
            setBackgroundColor(Color.GREEN)
        }
        val v3 = View(context).apply {
            setBackgroundColor(Color.BLACK)
        }
        val v4 = View(context).apply {
            setBackgroundColor(Color.LTGRAY)
        }
        val v5 = View(context).apply {
            setBackgroundColor(Color.RED)
        }
        mCourse.layout.addCourse(v1, CourseLayoutParams(2, 1, 4, CourseType.AFFAIR))
        mCourse.layout.addCourse(v2, CourseLayoutParams(3, 1, 1, CourseType.AFFAIR))
        mCourse.layout.addCourse(v3, CourseLayoutParams(4, 1, 4, CourseType.AFFAIR))
        mCourse.layout.addCourse(v4, CourseLayoutParams(3, 4, 1, CourseType.AFFAIR))
        mCourse.layout.addCourse(v5, CourseLayoutParams(1, 2, 2, CourseType.AFFAIR))
    }
}