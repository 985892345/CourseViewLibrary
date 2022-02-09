package com.mredrock.cyxbs.lib.courseview.course

import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * View 内部使用的课表数据
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/22
 */
interface CourseBeanInternal {
    /**
     * 星期数，星期一为 1
     */
    val day: Int

    /**
     * 开始位置，从 0 开始
     */
    val startPos: Int

    /**
     * 长度
     */
    val length: Int

    /**
     * 类型
     */
    val type: CourseType
}