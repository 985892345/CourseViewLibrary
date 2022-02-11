package com.mredrock.cyxbs.lib.courseview.helper

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_10_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_10_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_11_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_11_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_12_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_12_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_1_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_1_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_2_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_2_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_3_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_3_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_4_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_4_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_5_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_5_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_6_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_6_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_7_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_7_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_8_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_8_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_9_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.LESSON_9_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseDecoration
import com.mredrock.cyxbs.lib.courseview.utils.dp2pxF
import java.util.*

/**
 * ```
 * 该类作用：
 * 1、封装左侧时间栏中当前时间线的绘制
 *
 * 该类设计：
 * 1、对 CourseLayout 增加自定义绘图的监听来实现
 * 2、绘图监听参考了 RV 的 ItemDecoration 的设计
 *
 * 注意事项：
 * 1、该类只管理左侧时间栏中当前时间线的绘制，请不要添加一些不属于该类的功能，想添加功能应该再写一个 CourseDecoration
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 15:21
 */
class CourseTimelineHelper private constructor(
    private val course: CourseLayout
) : CourseDecoration {

    fun setVisible(boolean: Boolean) {
        mVisible = boolean
        course.invalidate()
        mRefreshRunnable.cancel()
        if (boolean) {
            mRefreshRunnable.start()
        }
    }

    private val mCalendar = Calendar.getInstance() // 用于装换时间
    private val mCircleRadius = 3.dp2pxF // 小圆半径
    private var mVisible = true // 是否显示

    // 画笔
    private val mPaint = Paint().apply {
        color = 0xFF2A4E84.toInt()
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = 1.dp2pxF
    }

    // 用于每隔一段时间就刷新的 Runnable
    private val mRefreshRunnable = object : Runnable {
        override fun run() {
            course.invalidate()
            course.postDelayed(this, 1000 * 60) // 一分钟刷新一次，但记得要取消，防止内存泄漏
        }

        fun start() {
            run()
        }

        fun cancel() {
            course.removeCallbacks(this)
        }
    }

    init {
        // 如果类初始化时 View 已经被添加到屏幕上，则直接 start()
        if (course.isAttachedToWindow) {
            mRefreshRunnable.start()
        }
        // 添加 View 状态的监听，在脱离视图时取消 mRefreshRunnable，防止内存泄漏
        course.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                mRefreshRunnable.start()
            }

            override fun onViewDetachedFromWindow(v: View) {
                mRefreshRunnable.cancel()
            }
        })
    }

    override fun onDrawOver(canvas: Canvas, course: CourseLayout) {
        if (mVisible) {
            val left = course.getColumnsWidth(0, CourseLayout.TIME_LINE_LEFT - 1)
            val width =
                course.getColumnsWidth(CourseLayout.TIME_LINE_LEFT, CourseLayout.TIME_LINE_RIGHT)
            val right = left + width
            val lineHeight = getLineHeight()
            val cx = left + 28F
            canvas.drawCircle(cx, lineHeight, mCircleRadius, mPaint)
            canvas.drawLine(cx, lineHeight, right - 20F, lineHeight, mPaint)
        }
    }

    /**
     * 得到当前时间该显示的高度
     *
     * 因为包含下课时间段，还有中午时间段和傍晚时间段，尤其是他们又要展开，
     * 所以只能用穷举法列出了所有的时间段来计算对应的高度
     */
    private fun getLineHeight(): Float {
        mCalendar.timeInMillis = System.currentTimeMillis()
        val hour = mCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = mCalendar.get(Calendar.MINUTE)
        val now = hour * 60 + minute

        return when {
            now <= 8 * 60 -> { //  8:00 前
                mCircleRadius + course.getRowsHeight(
                    0, LESSON_1_TOP - 1
                )
            }
            now <= 8 * 60 + 45 -> { // 8:00 - 8:45 第一节课
                val start = 8 * 60
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_1_TOP,
                    LESSON_1_BOTTOM
                ) - mCircleRadius
                multiple * lessonH + mCircleRadius + course.getRowsHeight(
                    0, LESSON_1_TOP - 1
                )
            }
            now <= 8 * 60 + 55 -> { // 8:45 - 8:55 第一节课课间
                val start = 8 * 60 + 45
                val end = 8 * 60 + 55
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_1_BOTTOM + 1,
                    LESSON_2_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_1_BOTTOM
                )
            }
            now <= 9 * 60 + 40 -> { // 8:55 - 9:40 第二节课
                val start = 8 * 60 + 55
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_2_TOP,
                    LESSON_2_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_2_TOP - 1
                )
            }
            now <= 10 * 60 + 15 -> { // 9:40 - 10:15 第二节课课间
                val start = 9 * 60 + 40
                val end = 10 * 60 + 15
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_2_BOTTOM + 1,
                    LESSON_3_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_2_BOTTOM
                )
            }
            now <= 11 * 60 -> { // 10:15 - 11:00 第三节课
                val start = 10 * 60 + 15
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_3_TOP,
                    LESSON_3_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_3_TOP - 1
                )
            }
            now <= 11 * 60 + 10 -> { // 11:00 - 11:10 第三节课课间
                val start = 11 * 60
                val end = 11 * 60 + 10
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_3_BOTTOM + 1,
                    LESSON_4_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_3_BOTTOM
                )
            }
            now <= 11 * 60 + 55 -> { // 11:10 - 11:55 第四节课
                val start = 11 * 60 + 10
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_4_TOP,
                    LESSON_4_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_4_TOP - 1
                )
            }
            now <= 14 * 60 -> { // 11:55 - 14:00 中午时间段
                val start = 11 * 60 + 55
                val end = 14 * 60
                val multiple = (now - start) / (end - start).toFloat()
                val noonH = course.getRowsHeight(
                    NOON_TOP,
                    NOON_BOTTOM
                )
                multiple * noonH + course.getRowsHeight(
                    0, NOON_TOP - 1
                )
            }
            now <= 14 * 60 + 45 -> { // 14:00 - 14:45 第五节课
                val start = 14 * 60
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_5_TOP,
                    LESSON_5_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_5_TOP - 1
                )
            }
            now <= 14 * 60 + 55 -> { // 14:45 - 14:55 第五节课课间
                val start = 14 * 60 + 45
                val end = 14 * 60 + 55
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_5_BOTTOM + 1,
                    LESSON_6_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_5_BOTTOM
                )
            }
            now <= 15 * 60 + 40 -> { // 14:55 - 15:40 第六节课
                val start = 14 * 60 + 55
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_6_TOP,
                    LESSON_6_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_6_TOP - 1
                )
            }
            now <= 16 * 60 + 15 -> { // 15:40 - 16:15 第六节课课间
                val start = 15 * 60 + 40
                val end = 16 * 60 + 15
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_6_BOTTOM + 1,
                    LESSON_7_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_6_BOTTOM
                )
            }
            now <= 17 * 60 -> { // 16:15 - 17:00 第七节课
                val start = 16 * 60 + 15
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_7_TOP,
                    LESSON_7_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_7_TOP - 1
                )
            }
            now <= 17 * 60 + 10 -> { // 17:00 - 17:10 第七节课课间
                val start = 17 * 60
                val end = 17 * 60 + 10
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_7_BOTTOM + 1,
                    LESSON_8_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_7_BOTTOM
                )
            }
            now <= 17 * 60 + 55 -> { // 17:10 - 17:55 第八节课
                val start = 17 * 60 + 10
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_8_TOP,
                    LESSON_8_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_8_TOP - 1
                )
            }
            now <= 19 * 60 -> { // 17:55 - 19:00 傍晚时间段
                val start = 17 * 60 + 55
                val end = 19 * 60
                val multiple = (now - start) / (end - start).toFloat()
                val duskH = course.getRowsHeight(
                    DUSK_TOP,
                    DUSK_BOTTOM
                )
                multiple * duskH + course.getRowsHeight(
                    0, DUSK_TOP - 1
                )
            }
            now <= 19 * 60 + 45 -> { // 19:00 - 19:45 第九节课
                val start = 19 * 60
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_9_TOP,
                    LESSON_9_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_9_TOP - 1
                )
            }
            now <= 19 * 60 + 55 -> { // 19:45 - 19:55 第九节课课间
                val start = 19 * 60 + 45
                val end = 19 * 60 + 55
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_9_BOTTOM + 1,
                    LESSON_10_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_9_BOTTOM
                )
            }
            now <= 20 * 60 + 40 -> { // 19:55 - 20:40 第十节课
                val start = 19 * 60 + 55
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_10_TOP,
                    LESSON_10_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_10_TOP - 1
                )
            }
            now <= 20 * 60 + 50 -> { // 20:40 - 20:50 第十节课课间
                val start = 20 * 60 + 40
                val end = 20 * 60 + 50
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_10_BOTTOM + 1,
                    LESSON_11_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_10_BOTTOM
                )
            }
            now <= 21 * 60 + 35 -> { // 20:50 - 21:35 第十一节课
                val start = 20 * 60 + 50
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_11_TOP,
                    LESSON_11_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_11_TOP - 1
                )
            }
            now <= 21 * 60 + 45 -> { // 21:35 - 21:45 第十一节课课间
                val start = 21 * 60 + 35
                val end = 21 * 60 + 45
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_11_BOTTOM + 1,
                    LESSON_12_TOP - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_11_BOTTOM
                )
            }
            now <= 22 * 60 + 30 -> { // 21:45 - 22:30 第十二节课
                val start = 21 * 60 + 45
                val multiple = (now - start) / 45F
                val lessonH = course.getRowsHeight(
                    LESSON_12_TOP,
                    LESSON_12_BOTTOM
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_12_TOP - 1
                )
            }
            now <= 24 * 60 -> { // 22:30 - 24:00 晚上最后一节课下课到凌晨
                val start = 22 * 60 + 30
                val end = 24 * 60
                val multiple = (now - start) / (end - start).toFloat()
                val lessonH = course.getRowsHeight(
                    LESSON_12_BOTTOM + 1,
                    course.getRowCount() - 1
                )
                multiple * lessonH + course.getRowsHeight(
                    0, LESSON_12_BOTTOM
                )
            }
            else -> {
                -999F // 跑到屏幕外不显示，但应该不会到这一分支
            }
        }
    }

    companion object {
        fun attach(course: CourseLayout): CourseTimelineHelper {
            return CourseTimelineHelper(course).apply {
                course.addCourseDecoration(this)
            }
        }
    }
}