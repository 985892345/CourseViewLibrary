package com.mredrock.cyxbs.lib.courseview.utils

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.animation.addListener
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_LEFT
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_RIGHT
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import kotlin.math.abs

/**
 * 参考 RV 的 ItemTouchHelper 的设计，将点击中午和傍晚时间段折叠的功能与 [CourseLayout] 进行分离，
 * 增强代码可读性和维护性
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseFoldHelper private constructor(
    private val course: CourseLayout
) : OnCourseTouchListener {

    private var mNoonAnimation: ChangeWeightAnimation? = null
    private var mDuskAnimation: ChangeWeightAnimation? = null

    private var mInitialX = 0
    private var mInitialY = 0
    private var mLastMoveX = 0
    private var mLastMoveY = 0

    private var mClickWhich = DownWhich.OTHER

    private val mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    init {
        course.addCourseTouchListener(this)
    }

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (event.action == MotionEvent.ACTION_DOWN) {
            val noonState = getNoonRowState()
            val duskState = getDuskRowState()
            // 如果处于动画中
            if (noonState == RowState.ANIMATION || duskState == RowState.ANIMATION) {
                mClickWhich = DownWhich.OTHER
                return true // 防止下一个 CourseTouchListener 处理正在动画时的触摸事件
            }

            val timeLineLeft = course.getColumnsWidth(0, TIME_LINE_LEFT - 1)
            val timeLineRight = timeLineLeft + course.getColumnsWidth(
                TIME_LINE_LEFT,
                TIME_LINE_RIGHT
            )
            if (x in timeLineLeft..timeLineRight) { // 如果 x 落在左侧时间轴上
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
            return // 配合 isIntercept()，目前正处于动画中，不做任何处理
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

    private fun clickNoon() {
        when (getNoonRowState()) {
            RowState.FOLD -> unfoldNoon()
            RowState.UNFOLD -> foldNoon()
            RowState.ANIMATION -> return
        }
    }

    private fun clickDusk() {
        when (getDuskRowState()) {
            RowState.FOLD -> unfoldDusk()
            RowState.UNFOLD -> foldDusk()
            RowState.ANIMATION -> return
        }
    }

    private fun getNoonRowState(): RowState {
        return when (course.getRowsWeight(NOON_TOP, NOON_BOTTOM) / (NOON_BOTTOM - NOON_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> RowState.ANIMATION
        }
    }

    private fun getDuskRowState(): RowState {
        return when (course.getRowsWeight(DUSK_TOP, DUSK_BOTTOM) / (DUSK_BOTTOM - DUSK_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> RowState.ANIMATION
        }
    }

    private fun foldNoon() {
        if (mNoonAnimation == null) {
            mNoonAnimation = FoldAnimation(
                onChanged = {
                    for (row in NOON_TOP..NOON_BOTTOM) {
                        course.setRowWeight(row, it)
                    }
                },
                onEnd = { mNoonAnimation = null }
            ).apply { start() }
            course.requestLayout()
            course.invalidate()
        }
    }

    private fun unfoldNoon() {
        if (mNoonAnimation == null) {
            mNoonAnimation = UnfoldAnimation(
                onChanged =  {
                    for (row in NOON_TOP..NOON_BOTTOM) {
                        course.setRowWeight(row, it)
                    }
                },
                onEnd = { mNoonAnimation = null }
            ).apply { start() }
            course.requestLayout()
        }
    }

    private fun foldDusk() {
        if (mDuskAnimation == null) {
            mDuskAnimation = FoldAnimation(
                onChanged = {
                    for (row in DUSK_TOP..DUSK_BOTTOM) {
                        course.setRowWeight(row, it)
                    }
                },
                onEnd = { mDuskAnimation = null }
            ).apply { start() }
            course.requestLayout()
        }
    }

    private fun unfoldDusk() {
        if (mDuskAnimation == null) {
            mDuskAnimation = UnfoldAnimation(
                onChanged = {
                    for (row in DUSK_TOP..DUSK_BOTTOM) {
                        course.setRowWeight(row, it)
                    }
                },
                onEnd = { mDuskAnimation = null }
            ).apply { start() }
            course.requestLayout()
        }
    }

    private class FoldAnimation(
        onChanged: ((now: Float) -> Unit)? = null,
        onEnd: (() -> Unit)? = null
    ) : ChangeWeightAnimation(1F, 0F, 200, onChanged, onEnd)

    private class UnfoldAnimation(
        onChanged: ((now: Float) -> Unit)? = null,
        onEnd: (() -> Unit)? = null
    ) : ChangeWeightAnimation(0F, 1F, 200, onChanged, onEnd)

    // 比重改变的动画封装类
    private abstract class ChangeWeightAnimation(
        val startWeight: Float,
        val endWeight: Float,
        val time: Long,
        private val onChanged: ((now: Float) -> Unit)? = null,
        private val onEnd: (() -> Unit)? = null,
    ) {
        val isRunning: Boolean
            get() = animator.isRunning

        private var animator: ValueAnimator = ValueAnimator.ofFloat(startWeight, endWeight)

        fun start() {
            animator.run {
                addUpdateListener{
                    onChanged?.invoke(animator.animatedValue as Float)
                }
                addListener(
                    onEnd = { onEnd?.invoke() },
                )
                duration = time
                this.start()
            }
        }
    }

    enum class RowState {
        FOLD, // 完全展开
        UNFOLD, // 完全折叠
        ANIMATION // 处于动画中
    }

    enum class DownWhich {
        NOON, // Down Noon 区域
        DUSK, // Down Dusk 区域
        OTHER // Down 其他区域
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的
         */
        fun attach(course: CourseLayout): CourseFoldHelper {
            return CourseFoldHelper(course)
        }
    }
}