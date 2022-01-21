package com.mredrock.cyxbs.lib.courseview.course.attrs

import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayoutAttrs(
    rowCount: Int,
    columnCount: Int
) : NetLayoutAttrs(rowCount, columnCount) {
    constructor(
        attrs: NetLayoutAttrs
    ) : this(attrs.rowCount, attrs.columnCount)
}