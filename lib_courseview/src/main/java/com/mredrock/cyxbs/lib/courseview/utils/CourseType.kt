package com.mredrock.cyxbs.lib.courseview.utils

import android.content.res.TypedArray

/**
 * 这个枚举类记录 CourseLayout
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
enum class CourseType(val i: Int) {
    MY(0), // 我的课程
    LINK(1), // 关联人的课程
    AFFAIR(2), // 我的事务
    TIME(3), // 时间轴上的数字和中午、傍晚
    AFFAIR_TOUCH(4), // 带有加号的那个 View
    ARROW_NOON(5), // 中午时间段的那个箭头
    ARROW_DUSK(6), // 傍晚时间段的那个箭头
    SUBSTITUTE(7), // 长按事务后生成的替身
    UNKNOWN(114514); // 未知

    companion object {
        fun default() = TIME

        fun get(i: Int): CourseType {
            return when (i) {
                MY.i -> MY
                LINK.i -> LINK
                AFFAIR.i -> AFFAIR
                TIME.i -> TIME
                AFFAIR_TOUCH.i -> AFFAIR_TOUCH
                ARROW_NOON.i -> ARROW_NOON
                ARROW_DUSK.i -> ARROW_DUSK
                else -> UNKNOWN
            }
        }
    }
}

internal fun Int.courseType(ty: TypedArray): CourseType {
    return CourseType.get(ty.getInt(this, CourseType.default().i))
}