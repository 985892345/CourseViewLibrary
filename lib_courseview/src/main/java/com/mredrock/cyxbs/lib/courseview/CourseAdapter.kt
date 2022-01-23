package com.mredrock.cyxbs.lib.courseview

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/22
 */
abstract class CourseAdapter {
    abstract fun getAmData(): List<CourseBean>
    abstract fun getNoonData(): List<CourseBean>
    abstract fun getPmData(): List<CourseBean>
    abstract fun getDuskData(): List<CourseBean>
    abstract fun getNightData(): List<CourseBean>
}