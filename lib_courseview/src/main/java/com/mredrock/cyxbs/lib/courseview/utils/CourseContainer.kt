package com.mredrock.cyxbs.lib.courseview.utils

import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView

/**
 * ## Course 的 Layout 容器
 *
 * ### 该类作用：
 * - 用于快速得到 View 控件，方便使用
 * - 将一些耦合的操作单独提出来，方便以后修改
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseContainer(val viewGroup: ViewGroup) : BaseViewAttrs {

    // 使用 xml 便于以后修改，且增强可读性
    val scroll =
        LayoutInflater.from(viewGroup.context).inflate(
            R.layout.lib_course_layout_course, viewGroup, false) as CourseScrollView

    val course = scroll.getChildAt(0) as AbstractCourseLayout

    internal fun initAttrs(attrs: AttributeSet) {
        newAttrs(viewGroup.context, attrs, R.styleable.CourseView) {
            val timeColor = R.styleable.CourseView_course_time_color.color(Color.BLACK)
            val timeSize = R.styleable.CourseView_course_time_size.dimens(11.dp2pxF())
            val noonColor = R.styleable.CourseView_course_noon_color.color(Color.BLACK)
            val noonSize = R.styleable.CourseView_course_noon_size.dimens(11.dp2pxF())
            val duskColor = R.styleable.CourseView_course_dusk_color.color(Color.BLACK)
            val duskSize = R.styleable.CourseView_course_dusk_size.dimens(11.dp2pxF())
            course.forEach {
                val lp = it.layoutParams
                if (lp is CourseLayoutParams) {
                    when (lp.type) {
                        CourseType.TIME -> {
                            val tv = it as TextView
                            tv.setTextColor(timeColor)
                            tv.textSize = timeSize
                        }
                        CourseType.NOON -> {
                            val tv = it as TextView
                            tv.setTextColor(noonColor)
                            tv.textSize = noonSize
                        }
                        CourseType.DUSK -> {
                            val tv = it as TextView
                            tv.setTextColor(duskColor)
                            tv.textSize = duskSize
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}