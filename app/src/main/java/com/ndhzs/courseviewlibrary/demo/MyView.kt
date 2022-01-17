package com.ndhzs.courseviewlibrary.demo

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
class MyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY, MeasureSpec.UNSPECIFIED -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
            MeasureSpec.AT_MOST -> {
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(275, MeasureSpec.EXACTLY), heightMeasureSpec)
            }
        }
    }
}