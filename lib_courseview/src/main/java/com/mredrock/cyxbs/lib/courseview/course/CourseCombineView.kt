package com.mredrock.cyxbs.lib.courseview.course

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/21
 */
@SuppressLint("ViewConstructor")
abstract class CourseCombineView(
    context: Context,
    val rowCount: Int,
    val isShow: Boolean
) : CourseLayout(
    context,
    CourseLayoutAttrs(rowCount, 8)
) {

    private val mLeftText = ArrayList<TextView>(rowCount)
    init {
        for (i in 0 until mNetAttrs.rowCount) {
            val textView = TextView(context)
            mLeftText.add(textView)
            addCourse(textView, CourseLayoutParams(0, i, 1))
        }
    }

    fun setLeftText(leftText: List<String>) {
        for (i in 0 until mNetAttrs.rowCount) {
            val textView = TextView(context)
            textView.text = leftText[i]
            addCourse(textView, CourseLayoutParams(0, i, 1))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        if (isShow) {
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(
                    hSize / TOTAL_ROW_COUNT * rowCount,
                    MeasureSpec.EXACTLY
                )
            )
        } else {
            setMeasuredDimension(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isShow) {
            super.onLayout(changed, l, t, r, b)
        }
    }

    companion object {
        /**
         * 总的行数，包含 上午四节 + 中午一节 + 下午四节 + 傍晚一节 + 晚上四节
         */
        const val TOTAL_ROW_COUNT = 14
    }
}