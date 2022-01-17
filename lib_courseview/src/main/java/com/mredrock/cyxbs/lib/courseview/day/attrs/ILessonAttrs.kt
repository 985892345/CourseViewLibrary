package com.mredrock.cyxbs.lib.courseview.day.attrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
interface ILessonAttrs : Comparable<ILessonAttrs> {
    var startIndex: Int
    var length: Int

    override fun compareTo(other: ILessonAttrs): Int {
        val dIndex = startIndex - other.startIndex
        if (dIndex == 0) {
            return length - other.length
        }
        return dIndex
    }
}