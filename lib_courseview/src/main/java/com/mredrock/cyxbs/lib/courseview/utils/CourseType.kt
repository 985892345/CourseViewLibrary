package com.mredrock.cyxbs.lib.courseview.utils

import android.content.res.TypedArray
import com.mredrock.cyxbs.lib.courseview.R

/**
 * 这个枚举类记录 CourseLayout
 *
 * 这里面的数据与 [R.styleable.CourseLayout_Layout_course_layout_type] 对应
 *
 * **NOTE:** 每个类型的位置决定了它的大小顺序，越在上面的，在课表的显示也会在上面，这是 kt 给枚举类的语法题，
 * 自然顺序是它们在枚举类中定义的顺序，意思是 MY < AFFAIR
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
enum class CourseType {
    MY, // 我的课程
    AFFAIR, // 我的事务
    LINK, // 关联人的课程
    TIME, // 时间轴上的数字
    NOON, // 中午时间段
    DUSK, // 傍晚时间段
    AFFAIR_TOUCH, // 带有加号的那个 View
    SUBSTITUTE, // 长按事务后生成的替身
    UNKNOWN; // 未知
    // 按顺序书写

    companion object {
        fun default() = UNKNOWN

        fun get(i: Int): CourseType {
            return values().getOrNull(i) ?: default()
        }
    }
}

internal fun Int.courseType(ty: TypedArray): CourseType {
    return CourseType.get(ty.getInt(this, CourseType.default().ordinal))
}