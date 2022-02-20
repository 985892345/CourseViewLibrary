package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseBeanInternal
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.helper.*
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.CourseMultiTouchHelper
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair.CreateAffairPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.entitymove.EntityMovePointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.fold.FoldPointerDispatcher
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

    fun addMyLesson(
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

    fun addLinkLesson(
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

    fun addAnyLesson(
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
        CourseTimelineHelper.attach(mCourse.layout)
        CourseDownAnimHelper.attach(mCourse.layout)
        CourseMultiTouchHelper.attach(mCourse.layout).apply {
            addPointerDispatcher(FoldPointerDispatcher(mCourse.layout))
            addPointerDispatcher(EntityMovePointerDispatcher(mCourse.layout))
            addPointerDispatcher(
                CreateAffairPointerDispatcher(mCourse.layout).apply {
                    setOnClickListener {
                        it.remove()
                        addAnyLesson(it.cloneLp(), "自习", "233", LessonHelper.LessonType.AFFAIR)
                    }
                }
            )
        }

        addMyLesson(0, 3, 2, "高等数学", "233")
        addMyLesson(3, 5, 2, "大学物理", "233")
        addMyLesson(5, 9, 4, "数据结构", "233")
        addMyLesson(1, 11, 2, "离散数学", "233")
        addMyAffair(2, -1, 1, "自习", "233")

        addMyLesson(0, 5, 4, "顶部的课", "233")
        addMyLesson(0, 5, 3, "中间的课", "233")
        addMyLesson(0, 5, 2, "底部的课", "233")
    }
}