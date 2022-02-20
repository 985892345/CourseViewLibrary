package com.mredrock.cyxbs.lib.courseview.utils

import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.mredrock.cyxbs.lib.courseview.course.CourseBeanInternal
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.lesson.LessonView
import com.mredrock.cyxbs.lib.courseview.lesson.affair.AffairView
import java.lang.RuntimeException

/**
 * 操作 [CourseLayout] 的帮助类
 *
 * **NOTE:** 该类是 object 类型，不要保存具有生命周期的对象，防止内存泄漏
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 20:01
 */
object LessonHelper {

    /**
     * 清楚全部课程和事务
     */
    fun clearLessonAndAffair(course: CourseLayout) {
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

    /**
     * 自动添加课程或者事务
     * @param day 星期数，注意：星期一为 0，所以后面会进行一次装换 [getCorrectDay]
     * @param beginLesson 课程或职务开始的节数，第一节开始为 1，依次类推，中午和傍晚分别为 -1 和 -2 [getStartPos]
     * @param period 课程或者事务或者事务的长度
     * @param course [CourseLayout] 实例，会自动帮你把课程或事务添加进去
     * @param title 标题
     * @param content 内容
     * @param type 用于区分课程和事务的类型
     */
    fun addLessonItem(
        day: Int,
        beginLesson: Int,
        period: Int,
        course: CourseLayout,
        title: String,
        content: String,
        type: LessonType,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
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
                getCorrectDay(day), getStartPos(beginLesson),
                period, CourseType.UNKNOWN
            )
        }
        lessonView.setOnClickListener {
            // 这里不建议直接传入 lp
            onClick?.invoke(lessonView, it.layoutParams as CourseLayoutParams)
        }
        addLessonItemInternal(lessonView, lp, course, title, content, type)
    }

    /**
     * 自动添加课程或者事务
     * @param bean 内部的课程数据，注意：是内部的数据，并不是后端返回的数据
     * @param course [CourseLayout] 实例，会自动帮你把课程或事务添加进去
     * @param title 标题
     * @param content 内容
     * @param type 用于区分课程和事务的类型
     */
    fun addLessonItem(
        bean: CourseBeanInternal,
        course: CourseLayout,
        title: String,
        content: String,
        type: LessonType,
        onClick: ((view: LessonView, lp: CourseLayoutParams) -> Unit)? = null
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
            CourseLayoutParams(bean)
        }
        lessonView.setOnClickListener {
            // 这里不建议直接传入 lp
            onClick?.invoke(lessonView, it.layoutParams as CourseLayoutParams)
        }
        addLessonItemInternal(lessonView, lp, course, title, content, type)
    }

    private fun addLessonItemInternal(
        lessonView: LessonView,
        lp: CourseLayoutParams,
        course: CourseLayout,
        title: String,
        content: String,
        type: LessonType
    ) {
        when (type) {
            LessonType.MY -> setMyLesson(lessonView, lp, title, content, lp.startPos)
            LessonType.LINK -> setLinkLesson(lessonView, lp, title, content, lp.startPos)
            LessonType.AFFAIR -> setAffair(lessonView, lp, title, content, lp.startPos)
        }
        course.addCourse(lessonView, lp)
        judgeIsUnfoldNoonOrDusk(course, lp)
    }

    /**
     * 设置自己的课
     */
    private fun setMyLesson(
        lessonView: LessonView,
        lp: CourseLayoutParams,
        title: String,
        content: String,
        startPos: Int
    ) {
        val context = lessonView.context
        val tvTop: TextView = lessonView.findViewById(R.id.tv_top)
        val tvBottom: TextView = lessonView.findViewById(R.id.tv_bottom)
        lp.type = CourseType.MY
        when (startPos) {
            in 0..3 -> {
                // 上午时间段
                lessonView.setBackgroundColor(R.color.common_morning_course_color.color(context))
                val color = R.color.common_morning_course_text_color.color(context)
                tvTop.text = title
                tvTop.setTextColor(color)
                tvBottom.text = content
                tvBottom.setTextColor(color)
            }
            in 5..8 -> {
                // 下午时间段
                lessonView.setBackgroundColor(R.color.common_afternoon_course_color.color(context))
                val color = R.color.common_afternoon_course_text_color.color(context)
                tvTop.text = title
                tvTop.setTextColor(color)
                tvBottom.text = content
                tvBottom.setTextColor(color)
            }
            in 10..13 -> {
                // 晚上时间段
                lessonView.setBackgroundColor(R.color.common_evening_course_color.color(context))
                val color = R.color.common_evening_course_text_color.color(context)
                tvTop.text = title
                tvTop.setTextColor(color)
                tvBottom.text = content
                tvBottom.setTextColor(color)
            }
        }
    }

    /**
     * 设置关联人的课
     */
    private fun setLinkLesson(
        lessonView: LessonView,
        lp: CourseLayoutParams,
        title: String,
        content: String,
        beginLesson: Int
    ) {
        val context = lessonView.context
        val tvTop: TextView = lessonView.findViewById(R.id.tv_top)
        val tvBottom: TextView = lessonView.findViewById(R.id.tv_bottom)
        lp.type = CourseType.LINK
        tvTop.text = title
        tvTop.setTextColor(Color.BLUE)
        tvBottom.text = content
        tvBottom.setTextColor(Color.BLUE)
    }

    /**
     * 设置事务
     */
    private fun setAffair(
        lessonView: LessonView,
        lp: CourseLayoutParams,
        title: String,
        content: String,
        beginLesson: Int
    ) {
        val context = lessonView.context
        lp.type = CourseType.AFFAIR
        lessonView.addView(AffairView(context), 0)
        val tvTop: TextView = lessonView.findViewById(R.id.tv_top)
        val tvBottom: TextView = lessonView.findViewById(R.id.tv_bottom)
        val color = R.color.course_affair_tv_color.color(context)
        tvTop.text = title
        tvTop.setTextColor(color)
        tvBottom.text = content
        tvBottom.setTextColor(color)
    }

    /**
     * 判断是否展开中午或者傍晚时间段
     */
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

    /**
     * 转换 [day] 参数
     *
     * 由于后端返回的星期数是：星期一为 0，星期二为 1，所以需要进行一次装换
     * @param day 后端返回的星期数
     */
    private fun getCorrectDay(day: Int): Int {
        return day + 1
    }

    /**
     * 装换 [beginLesson]
     *
     * [beginLesson]: 第一节开始为 1，第二节为 2，依次类推，中午和傍晚比较特殊，分别为 -1 和 -2
     *
     * StartPos: 按照摆放的顺序进行排序
     * @param beginLesson 后端返回的课程或职务开始的节数
     */
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
        /**
         * 自己的课
         */
        MY,

        /**
         * 关联人的课
         */
        LINK,

        /**
         * 事务
         */
        AFFAIR
    }
}