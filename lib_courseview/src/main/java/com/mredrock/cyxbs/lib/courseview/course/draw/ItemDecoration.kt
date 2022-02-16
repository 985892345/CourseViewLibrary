package com.mredrock.cyxbs.lib.courseview.course.draw

import android.graphics.Canvas
import android.view.ViewGroup

/**
 * 该类主要用于实现一些简单的绘图处理
 *
 * 设计参考了 RV 的 ItemDecoration
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
interface ItemDecoration<T: ViewGroup> {
    /**
     * 在所有子 View 的 onDraw() 前的回调，在这里面绘图可以绘制在子 View 下方
     */
    fun onDrawBelow(canvas: Canvas, view: T) { }

    /**
     * 在所有子 View 的 onDraw() 后的回调，在这里面绘图可以绘制在子 View 上方
     */
    fun onDrawAbove(canvas: Canvas, view: T) { }
}