//package com.mredrock.cyxbs.lib.courseview.helper
//
//import android.view.MotionEvent
//import android.view.View
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
//import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
//import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
//import com.mredrock.cyxbs.lib.courseview.utils.CourseType
//
///**
// * ```
// * 该类作用：
// * 1、实现长按事务的整体移动功能
// * 2、
// *
// * 注意事项：
// * 1、该类的长按整体移动的实现基本依靠 CourseLongPressEntityMoveHelper，
// *    用帮助类的形式引入长按移动功能，主要是为了以后需求的扩展
// * ```
// * @author 985892345 (Guo Xiangrui)
// * @email 2767465918@qq.com
// * @date 2022/2/10 13:09
// */
//class CourseLongPressAffairHelper(
//    course: CourseLayout,
//) : OnItemTouchListener, ILongPressEntityMove {
//
//    override fun setEntityMoveListener(l: ILongPressEntityMove.OnEntityMoveListener) {
//        mEntityMoveHelper.setEntityMoveListener(l)
//    }
//
//    override fun forceEnd() {
//        mEntityMoveHelper.forceEnd()
//    }
//
//    override fun isSubstituteView(child: View?): Boolean {
//        return mEntityMoveHelper.isSubstituteView(child)
//    }
//
//    override fun getLongPressState(): ILongPressEntityMove.LongPressState {
//        return mEntityMoveHelper.getLongPressState()
//    }
//
//    private val mEntityMoveHelper: ILongPressEntityMove = CourseLongPressEntityMoveHelper(course) {
//        val lp = it.layoutParams as CourseLayoutParams
//        lp.type == CourseType.AFFAIR
//    }
//
//    override fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean {
//        return mEntityMoveHelper.isAdvanceIntercept(event, course)
//    }
//
//    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
//        mEntityMoveHelper.onTouchEvent(event, course)
//    }
//
//    companion object {
//        /**
//         * 换成一个静态方法来 attach 到 [CourseLayout]，
//         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的。
//         *
//         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
//         */
//        fun attach(course: CourseLayout): CourseLongPressAffairHelper {
//            return CourseLongPressAffairHelper(course).apply {
//                course.addCourseTouchListener(this) // 给 CourseLayout 设置触摸监听
//            }
//        }
//    }
//}