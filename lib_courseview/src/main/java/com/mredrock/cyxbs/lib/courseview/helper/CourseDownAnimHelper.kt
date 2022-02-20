package com.mredrock.cyxbs.lib.courseview.helper

import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.CourseMultiTouchHelper
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import kotlin.math.abs
import kotlin.math.pow

/**
 * 点击 View 实现 Q 弹动画的事件帮助类
 *
 * 为什么写在这里?
 * 原因：
 * 多指触摸的那个分发帮助类会拦截子 View 的事件，
 * 所以点击实现的 Q 弹动画就得写在这里，而且使用 onDispatchTouchEvent() 来实现
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/19 14:44
 */
internal class CourseDownAnimHelper private constructor(): OnItemTouchListener<CourseLayout> {

    private var mTouchSlop = 0
    private val mViewById = SparseArray<View>(3)

    override fun onDispatchTouchEvent(event: MotionEvent, view: CourseLayout) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (mTouchSlop == 0) {
                    mTouchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
                }
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = event.getX(index).toInt()
                val y = event.getY(index).toInt()
                val child = view.findItemUnderByXY(x, y) ?: return
                val lp = child.layoutParams as CourseLayoutParams
                when (lp.type) {
                    CourseType.TIME,
                    CourseType.ARROW_NOON,
                    CourseType.ARROW_DUSK,
                    CourseType.SUBSTITUTE,
                    CourseType.UNKNOWN-> {}
                    else -> {
                        mViewById.put(id, child)
                        startAnim(child)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    val id = event.getPointerId(index)
                    mViewById[id]?.apply {
                        val pointer = view.getAbsolutePointer(id)
                        if (abs(pointer.lastMoveX - pointer.initialX) > mTouchSlop
                            || abs(pointer.lastMoveY - pointer.initialY) > mTouchSlop
                        ) {
                            recoverAnim(this)
                            mViewById.remove(id)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                mViewById[id]?.apply {
                    recoverAnim(this)
                    mViewById.remove(id)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mViewById.forEach { _, value ->
                    recoverAnim(value)
                }
                mViewById.clear()
            }
        }
    }

    /**
     * 实现按下后的 Q 弹动画
     */
    private fun startAnim(view: View) {
        view.animate()
            .scaleX(0.85F)
            .scaleY(0.85F)
            .setInterpolator { 1 - 1F / (1F + it).pow(6) }
            .start()
    }

    private fun recoverAnim(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(1F)
            .scaleY(1F)
            .setInterpolator { 1 - 1F / (1F + it).pow(6) }
            .start()
    }

    override fun onTouchEvent(event: MotionEvent, view: CourseLayout) {}

    companion object {
        /**
         * 采用这种方式更能明白该类的作用
         */
        fun attach(course: CourseLayout): CourseDownAnimHelper {
            return CourseDownAnimHelper().apply {
                course.addCourseTouchListener(this) // 监听 course 的事件分发
            }
        }
    }
}