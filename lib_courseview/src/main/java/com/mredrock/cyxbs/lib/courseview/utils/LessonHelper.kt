package com.mredrock.cyxbs.lib.courseview.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mredrock.cyxbs.lib.courseview.CourseBean
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.lesson.LessonView
import com.mredrock.cyxbs.lib.courseview.lesson.affair.AffairDrawable
import java.lang.RuntimeException
import kotlin.math.max
import kotlin.math.sqrt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 20:01
 */
object LessonHelper {

    fun clear(course: CourseLayout) {
        var index = 0
        while (index < course.childCount) {
            val child = course.getChildAt(index)
            val lp = child.layoutParams as CourseLayoutParams
            when (lp.type) {
                CourseType.MY,
                CourseType.LINK,
                CourseType.AFFAIR -> {
                    course.removeViewAt(index)
                }
                else -> {
                    index++
                }
            }
        }
        course.foldNoonWithoutAnim()
        course.foldDuskWithoutAnim()
    }

    fun addLessonItem(
        day: Int,
        beginLesson: Int,
        period: Int,
        course: CourseLayout,
        type: LessonType
    ) {
        val context = course.context
        val lessonView = LayoutInflater
            .from(context)
            .inflate(R.layout.item_lesson, course, false) as LessonView
        val layoutParams = lessonView.layoutParams
        val lp = if (layoutParams is CourseLayoutParams) {
            layoutParams.day = getCorrectDay(day)
            layoutParams.startPos = getStartPos(beginLesson)
            layoutParams.length = period
            layoutParams
        } else {
            CourseLayoutParams(
                day, getStartPos(beginLesson),
                period, CourseType.UNKNOWN
            ).apply { lessonView.layoutParams = this }
        }
        addLessonItemInterval(lessonView, lp, course, type)
    }

    fun addLessonItem(
        bean: CourseBean,
        course: CourseLayout,
        type: LessonType
    ) {
        val context = course.context
        val lessonView = LayoutInflater
            .from(context)
            .inflate(R.layout.item_lesson, course, false) as LessonView
        val layoutParams = lessonView.layoutParams
        val lp = if (layoutParams is CourseLayoutParams) {
            layoutParams.day = bean.day
            layoutParams.startPos = bean.startPos
            layoutParams.length = bean.length
            layoutParams
        } else {
            CourseLayoutParams(bean).apply { lessonView.layoutParams = this }
        }
        addLessonItemInterval(lessonView, lp, course, type)
    }

    private fun addLessonItemInterval(
        lessonView: LessonView,
        bean: CourseBean,
        course: CourseLayout,
        type: LessonType
    ) {
        val context = lessonView.context
        val tvTop: TextView = lessonView.findViewById(R.id.tv_top)
        val tvBottom: TextView = lessonView.findViewById(R.id.tv_bottom)
        val lp = if (bean is CourseLayoutParams) bean else CourseLayoutParams(bean)
        when (type) {
            LessonType.MY -> setMyLesson(context, lessonView, tvTop, tvBottom, lp, bean.startPos)
            LessonType.LINK -> setLinkLesson(
                context,
                lessonView,
                tvTop,
                tvBottom,
                lp,
                bean.startPos
            )
            LessonType.AFFAIR -> setAffair(context, lessonView, tvTop, tvBottom, lp, bean.startPos)
        }
        course.addCourse(lessonView, lp)
        judgeIsUnfoldNoonOrDusk(course, lp)
    }

    private fun setMyLesson(
        context: Context,
        lessonView: LessonView,
        tvTop: TextView,
        tvBottom: TextView,
        lp: CourseLayoutParams,
        startPos: Int
    ) {
        lp.type = CourseType.MY
        when (startPos) {
            in 0..3 -> {
                lessonView.setBackgroundColor(R.color.common_morning_course_color.color(context))
                val color = R.color.common_morning_course_text_color.color(context)
                tvTop.setTextColor(color)
                tvBottom.setTextColor(color)
            }
            in 5..8 -> {
                lessonView.setBackgroundColor(R.color.common_afternoon_course_color.color(context))
                val color = R.color.common_afternoon_course_text_color.color(context)
                tvTop.setTextColor(color)
                tvBottom.setTextColor(color)
            }
            in 10..13 -> {
                lessonView.setBackgroundColor(R.color.common_evening_course_color.color(context))
                val color = R.color.common_evening_course_text_color.color(context)
                tvTop.setTextColor(color)
                tvBottom.setTextColor(color)
            }
        }
    }

    private fun setLinkLesson(
        context: Context,
        lessonView: LessonView,
        tvTop: TextView,
        tvBottom: TextView,
        lp: CourseLayoutParams,
        beginLesson: Int
    ) {
        lp.type = CourseType.LINK
    }

    private fun setAffair(
        context: Context,
        lessonView: LessonView,
        tvTop: TextView,
        tvBottom: TextView,
        lp: CourseLayoutParams,
        beginLesson: Int
    ) {
        lp.type = CourseType.AFFAIR
        lessonView.background = AffairDrawable(context)
    }

    private fun judgeIsUnfoldNoonOrDusk(course: CourseLayout, lp: CourseLayoutParams) {
        if (lp.startRow <= CourseLayout.NOON_TOP && lp.endRow >= CourseLayout.NOON_BOTTOM) {
            if (course.getNoonRowState() == RowState.FOLD) {
                course.unfoldNoonWithoutAnim()
            }
        }
        if (lp.startRow <= CourseLayout.DUSK_TOP && lp.endRow >= CourseLayout.DUSK_BOTTOM) {
            if (course.getDuskRowState() == RowState.FOLD) {
                course.unfoldDuskWithoutAnim()
            }
        }
    }

    private fun getCorrectDay(day: Int): Int {
        return day + 1
    }

    private fun getStartPos(beginLesson: Int): Int {
        return when (beginLesson) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 3
            -1 -> 4
            5 -> 5
            6 -> 6
            7 -> 7
            8 -> 8
            -2 -> 9
            9 -> 10
            10 -> 11
            11 -> 12
            12 -> 13
            else -> throw RuntimeException("如果出现这个错误说明后端的 begin_lesson 返回参数有问题！")
        }
    }

    enum class LessonType {
        MY,
        LINK,
        AFFAIR
    }
}