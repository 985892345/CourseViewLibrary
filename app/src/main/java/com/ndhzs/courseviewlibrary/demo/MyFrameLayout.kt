package com.ndhzs.courseviewlibrary.demo

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
class MyFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val a = MeasureSpec.getMode(widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}