package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.helper.CourseCreateAffairHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseFoldHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseLongPressAffairHelper
import com.mredrock.cyxbs.lib.courseview.helper.CourseTimelineHelper
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseLayoutContainer
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.LessonHelper
import com.mredrock.cyxbs.lib.courseview.utils.WeekLayoutContainer
import kotlin.random.Random

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

    fun addMyCourse(day: Int, beginLesson: Int, period: Int) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            LessonHelper.LessonType.MY
        )
    }

    fun addMyAffair(day: Int, beginLesson: Int, period: Int) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            LessonHelper.LessonType.AFFAIR
        )
    }

    fun addLinkCourse(day: Int, beginLesson: Int, period: Int) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            LessonHelper.LessonType.LINK
        )
    }

    fun addCourse(bean: CourseBean, type: LessonHelper.LessonType) {
        LessonHelper.addLessonItem(bean, mCourse.layout, type)
    }

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
        CourseCreateAffairHelper.attach(mCourse.layout).apply {
            setTouchAffairViewClickListener {
                removeTouchAffairView()
                addCourse(getCourseBean(), LessonHelper.LessonType.AFFAIR)
            }
        }
        CourseLongPressAffairHelper.attach(mCourse.layout)
        CourseTimelineHelper.attach(mCourse.layout)

        addMyCourse(0, 3, 2)
        addMyCourse(3, 5, 2)
        addMyAffair(2, -1, 1)
    }
}