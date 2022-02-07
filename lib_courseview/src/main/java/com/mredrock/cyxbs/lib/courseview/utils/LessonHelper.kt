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
        lp.type = CourseType.OTHER
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

    private fun Int.color(context: Context): Int {
        return ContextCompat.getColor(context, this)
    }

    /**
     * 描述:课表中事务的背景View，
     * 别问为什么不用图片，问就是图片太麻烦，而且效果还不好
     *
     * @author Jovines
     * @create 2020-01-26 2:36 PM
     *
     * @author 985892345
     * @data 2022/2/7 16:40
     * @describe 因需求变更，我开始重构课表，将学长之前写的该自定义 View 改为了一张 Drawable
     */
    private class AffairDrawable(context: Context) : Drawable() {

        private val mBgColor = R.color.white.color(context)
        private val mPaint = Paint().apply {
            color = R.color.common_transaction_background_stripe_color.color(context)
        }
        private val mRectF = RectF()
        private val mClipBounds = Rect()

        override fun draw(canvas: Canvas) {
            canvas.drawColor(mBgColor)
            canvas.getClipBounds(mClipBounds)
            val width = mClipBounds.width()
            val height = mClipBounds.height()
            val drawEdge = max(width, height) * sqrt(2F)
            val space = 8.dp2px()
            val num = (drawEdge / (space * 2)).toInt()
            canvas.save()
            canvas.translate(width / 2F, height / 2F)
            canvas.rotate(45F)
            mRectF.set(
                -drawEdge / 2,
                drawEdge / 2,
                -drawEdge / 2 + space,
                -drawEdge / 2
            )
            for (i in 0 until num) {
                canvas.drawRect(mRectF, mPaint)
                mRectF.set(
                    mRectF.left + (space * 2),
                    mRectF.top,
                    mRectF.right + (space * 2),
                    mRectF.bottom
                )
            }
            canvas.restore()
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }
}