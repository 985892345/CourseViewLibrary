package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mredrock.cyxbs.lib.courseview.net.NetLayout

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {

    private val mChild = View.inflate(context, R.layout.course_combine, this) as NetLayout

    init {
    }

}