package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseBeanInternal
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.helper.*
import com.mredrock.cyxbs.lib.courseview.lesson.LessonView
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseLayoutContainer
import com.mredrock.cyxbs.lib.courseview.utils.LessonHelper
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

    fun addMyCourse(
        day: Int,
        beginLesson: Int,
        period: Int,
        title: String,
        content: String,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
    ) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            title, content,
            LessonHelper.LessonType.MY,
            onClick
        )
    }

    fun addMyAffair(
        day: Int,
        beginLesson: Int,
        period: Int,
        title: String,
        content: String,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
    ) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            title, content,
            LessonHelper.LessonType.AFFAIR,
            onClick
        )
    }

    fun addLinkCourse(
        day: Int,
        beginLesson: Int,
        period: Int,
        title: String,
        content: String,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
    ) {
        LessonHelper.addLessonItem(
            day,
            beginLesson,
            period,
            mCourse.layout,
            title, content,
            LessonHelper.LessonType.LINK,
            onClick
        )
    }

    fun addCourse(
        bean: CourseBeanInternal,
        title: String,
        content: String,
        type: LessonHelper.LessonType,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
    ) {
        LessonHelper.addLessonItem(
            bean,
            mCourse.layout,
            title,
            content,
            type,
            onClick
        )
    }

    fun clear() {
        LessonHelper.clearLessonAndAffair(mCourse.layout)
    }

    fun setOnClickTouchAffairListener(onClick: (view: View, lp: CourseLayoutParams) -> Unit) {
        mOnClickTouchAffairListener = onClick
    }

    private val mWeek = WeekLayoutContainer(this)
    private val mCourse = CourseLayoutContainer(this)
    private var mOnClickTouchAffairListener: ((View, CourseLayoutParams) -> Unit)? = null

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
                mOnClickTouchAffairListener?.invoke(it, it.layoutParams as CourseLayoutParams)
                removeTouchAffairView()
                addCourse(
                    it.layoutParams as CourseLayoutParams,
                    "自习", "114514",
                    LessonHelper.LessonType.AFFAIR
                )
            }
        }
        CourseLongPressAffairHelper.attach(mCourse.layout)
        CourseTimelineHelper.attach(mCourse.layout)

        addMyCourse(0, 3, 2, "高等数学", "114514")
        addMyCourse(3, 5, 2, "大学物理", "114514")
        addMyCourse(5, 9, 4, "数据结构", "114514")
        addMyCourse(1, 11, 2, "离散数学", "114514")
        addMyAffair(2, -1, 1, "自习", "114514")
    }
}