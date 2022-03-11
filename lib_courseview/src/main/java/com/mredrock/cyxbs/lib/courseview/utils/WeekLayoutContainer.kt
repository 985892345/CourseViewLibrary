package com.mredrock.cyxbs.lib.courseview.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.net.NetLayout

/**
 * ## Week 的 Layout 容器
 *
 * ### 该类作用：
 * - 用于快速得到 View 控件，方便使用
 * - 将一些耦合的操作单独提出来，方便以后修改
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class WeekLayoutContainer(viewGroup: ViewGroup) {

    // 使用 xml 便于以后修改，且增强可读性
    val layout =
        LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_week, viewGroup, false) as NetLayout

    val month = layout.getChildAt(0)

    val mon = WeekContainer(layout.getChildAt(1) as ViewGroup).apply { week.text = "周一" }
    val tue = WeekContainer(layout.getChildAt(2) as ViewGroup).apply { week.text = "周二" }
    val wed = WeekContainer(layout.getChildAt(3) as ViewGroup).apply { week.text = "周三" }
    val thu = WeekContainer(layout.getChildAt(4) as ViewGroup).apply { week.text = "周四" }
    val fri = WeekContainer(layout.getChildAt(5) as ViewGroup).apply { week.text = "周五" }
    val sat = WeekContainer(layout.getChildAt(6) as ViewGroup).apply { week.text = "周六" }
    val sun = WeekContainer(layout.getChildAt(7) as ViewGroup).apply { week.text = "周日" }

    class WeekContainer(viewGroup: ViewGroup) {
        val week = viewGroup.getChildAt(0) as TextView
        @SuppressLint("SetTextI18n")
        val date = (viewGroup.getChildAt(1) as TextView).apply {
            text = "30日"
        }
    }
}