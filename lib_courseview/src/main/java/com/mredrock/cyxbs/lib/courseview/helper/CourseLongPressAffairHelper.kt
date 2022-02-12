package com.mredrock.cyxbs.lib.courseview.helper

import android.view.MotionEvent
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * ```
 * 该类作用：
 * 1、实现长按事务的整体移动功能
 *
 * 注意事项：
 * 1、该类的实现基本依靠 CourseLongPressMoveHelper
 * 2、那个构造器的第二个参数不要自己传入，主要是为了使用接口代理的 by 关键字
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/10 13:09
 */
class CourseLongPressAffairHelper(
    course: CourseLayout,
    private val entityMoveHelper: ILongPressEntityMove = CourseLongPressEntityMoveHelper(course) {
        val lp = it.layoutParams as CourseLayoutParams
        lp.type == CourseType.AFFAIR
    }
) : OnCourseTouchListener, ILongPressEntityMove by entityMoveHelper {

    override fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        return entityMoveHelper.isAdvanceIntercept(event, course)
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        entityMoveHelper.onTouchEvent(event, course)
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 [CourseLayout]，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的。
         *
         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
         */
        fun attach(course: CourseLayout): CourseLongPressAffairHelper {
            return CourseLongPressAffairHelper(course).apply {
                course.addCourseTouchListener(this) // 给 CourseLayout 设置触摸监听
            }
        }
    }
}