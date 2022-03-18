package com.mredrock.cyxbs.lib.courseview.scroll

import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout

/**
 * [CourseLayout] 外层包裹的 ScrollView
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 14:34
 */
interface ICourseScrollView : IAbsoluteCoordinates {
    fun scrollBy(dy: Int)
    fun getScrollY(): Int
    fun setScrollY(y: Int)
    fun getHeight(): Int

    /**
     * ScrollView 的内部高度
     */
    val innerHeight: Int
    fun smoothScrollTo(x: Int, y: Int, scrollDurationMs: Int = 250)
    fun smoothScrollBy(dx: Int, dy: Int, scrollDurationMs: Int = 250)

    /**
     * 得到与内部 CourseLayout 之间相差的高度，是眼睛能看见的高度差，用于坐标系装换
     * ```
     * 如：
     *                 |---------- CourseScrollView ----------|
     *                               |------------- CourseLayout -------------|
     *                 |-- 得到的值 --| (值为正)
     * or
     *                 |---------- CourseScrollView ----------|
     *   |------------- CourseLayout -------------|
     *   |-- 得到的值 --| (注意：此时值为负)
     * ```
     */
    fun getDistance(course: AbstractCourseLayout): Int
}