package com.mredrock.cyxbs.lib.courseview

import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/22
 */
interface CourseBean {
    val day: Int
    val startPos: Int
    val length: Int
    val type: CourseType
}