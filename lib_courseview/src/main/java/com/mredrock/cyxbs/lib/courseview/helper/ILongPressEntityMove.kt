package com.mredrock.cyxbs.lib.courseview.helper

import android.view.MotionEvent
import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams

/**
 * 对应方法的注释可以看 [CourseLongPressEntityMoveHelper]
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/12 15:34
 */
interface ILongPressEntityMove {
    fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean
    fun onTouchEvent(event: MotionEvent, course: CourseLayout)
    fun setEntityMoveListener(l: OnEntityMoveListener)
    fun forceEnd()
    fun isSubstituteView(child: View?): Boolean
    fun getLongPressState(): LongPressState

    enum class LongPressState(private val i: Int) : Comparator<LongPressState> {
        DOWN(0), // 成功长按到设置的 View 时
        START(1), // 长按开始时
        MOVE(2), // View 在移动中
        UP(3), // 手指抬手时
        CANCEL(3), // 事件被父布局或者前面的 onCourseTouchListener 拦截时
        ANIM_RESTORE(4), // 正在执行回到原位置的动画中
        ANIM_TRAVEL(4), // 正在执行移动到新位置的动画中
        OVER(5); // 动画结束

        override fun compare(o1: LongPressState, o2: LongPressState): Int {
            return o1.i - o2.i
        }
    }

    interface OnEntityMoveListener {
        /**
         * 长按开始时回调
         */
        fun onLongPressStart(view: View, lp: CourseLayoutParams)

        /**
         * 移动到新位置时回调
         */
        fun onMoveToNewLocation(view: View, lp: CourseLayoutParams)
    }
}