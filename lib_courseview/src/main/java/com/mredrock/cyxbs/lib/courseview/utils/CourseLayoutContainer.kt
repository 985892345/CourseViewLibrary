package com.mredrock.cyxbs.lib.courseview.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView

/**
 * ```
 * 该类作用：
 * 1、用于快速得到 View 控件，方便使用
 * 2、将一些耦合的操作单独提出来，方便以后修改
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseLayoutContainer(viewGroup: ViewGroup) {

    // 使用 xml 便于以后修改，且增强可读性
    val scrollView =
        LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_course, viewGroup, false) as CourseScrollView

    val layout = scrollView.getChildAt(0) as CourseLayout
}