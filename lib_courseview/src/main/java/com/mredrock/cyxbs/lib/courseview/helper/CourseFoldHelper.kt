package com.mredrock.cyxbs.lib.courseview.helper

import android.view.MotionEvent
import android.view.ViewConfiguration
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_LEFT
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_RIGHT
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import kotlin.math.abs

/**
 * ```
 * 该类作用：
 * 1、封装点击中午和傍晚时间段功能
 *
 * 该类设计：
 * 1、对 CourseLayout 增加事件监听来实现
 * 2、事件监听参考了 RV 的 ItemTouchHelper 的设计
 *
 * 注意事项：
 * 1、该类只管理点击中午和傍晚的事件，请不要添加一些不属于该类的功能，想添加功能应该再写一个 OnCourseTouchListener
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseFoldHelper private constructor(
    private val course: CourseLayout
) : OnCourseTouchListener {

    private var mInitialX = 0 // Down 时的初始 X 值
    private var mInitialY = 0 // Down 时的初始 Y 值
    private var mLastMoveX = 0 // Move 时的移动 X 值
    private var mLastMoveY = 0 // Move 时的移动 Y 值

    private var mClickWhich = DownWhich.OTHER // Down 时记录当前点击的位置

    // 认定是滚动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private val mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    init {
        // 给 CourseLayout 设置触摸监听
        course.addCourseTouchListener(this)
    }

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (event.action == MotionEvent.ACTION_DOWN) {
            val noonState = course.getNoonRowState()
            val duskState = course.getDuskRowState()
            // 如果处于动画中
            if (noonState == RowState.ANIMATION || duskState == RowState.ANIMATION) {
                mClickWhich = DownWhich.OTHER
                return true // 防止下一个 CourseTouchListener 处理正在动画时的触摸事件
            }
            // 左侧时间轴的显示范围
            val timeLineLeft = course.getColumnsWidth(0, TIME_LINE_LEFT - 1)
            val timeLineRight = timeLineLeft + course.getColumnsWidth(
                TIME_LINE_LEFT,
                TIME_LINE_RIGHT
            )
            if (x in timeLineLeft..timeLineRight) { // 如果 x 落在左侧时间轴上
                // 中午那一行的显示范围
                val noonTopHeight = course.getRowsHeight(0, NOON_TOP - 1)
                val noonBottomHeight = noonTopHeight + course.getRowsHeight(
                    NOON_TOP,
                    NOON_BOTTOM
                )
                // 如果 y 落在 Noon 的行数
                if (y in (noonTopHeight - 10)..(noonBottomHeight + 10)) {
                    mClickWhich = DownWhich.NOON
                    return true
                } else {
                    // 傍晚那一行的显示范围
                    val duskTopHeight = course.getRowsHeight(0, DUSK_TOP - 1)
                    val duskBottomHeight = duskTopHeight + course.getRowsHeight(
                        DUSK_TOP,
                        DUSK_BOTTOM
                    )
                    // 如果 y 落在 Dusk 的行数
                    if (y in (duskTopHeight - 10)..(duskBottomHeight + 10)) {
                        mClickWhich = DownWhich.DUSK
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        if (mClickWhich == DownWhich.OTHER) {
            return // 配合 isIntercept() 中的处理，目前正处于动画中，不做任何处理
        }
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y
            }
            MotionEvent.ACTION_MOVE -> {
                mLastMoveX = x
                mLastMoveY = y
            }
            MotionEvent.ACTION_UP -> {
                // 如果抬手时移动的距离小于 mTouchSlop
                if (abs(x - mInitialX) <= mTouchSlop
                    && abs(y - mInitialY) <= mTouchSlop
                ) {
                    when (mClickWhich) {
                        DownWhich.NOON -> {
                            clickNoon()
                        }
                        DownWhich.DUSK -> {
                            clickDusk()
                        }
                        DownWhich.OTHER -> {} // 不做任何处理
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                // 这里说明已经超过了 mTouchSlop，被外面的 NestedScrollViw 拦截，
                // 因为我没有调用 requestDisallowInterceptTouchEvent 去阻止它拦截
            }
        }
    }

    /**
     * 点击中午
     */
    private fun clickNoon() {
        when (course.getNoonRowState()) {
            RowState.FOLD -> course.unfoldNoon()
            RowState.UNFOLD -> course.foldNoon()
            RowState.ANIMATION -> return
        }
    }

    /**
     * 点击傍晚
     */
    private fun clickDusk() {
        when (course.getDuskRowState()) {
            RowState.FOLD -> course.unfoldDusk()
            RowState.UNFOLD -> course.foldDusk()
            RowState.ANIMATION -> return
        }
    }

    enum class DownWhich {
        NOON, // Down Noon 区域
        DUSK, // Down Dusk 区域
        OTHER // Down 其他区域
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的。
         *
         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
         */
        fun attach(course: CourseLayout): CourseFoldHelper {
            return CourseFoldHelper(course)
        }
    }
}