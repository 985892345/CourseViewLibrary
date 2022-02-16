package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.ViewGroup

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:13
 */
interface OnPointerTouchListener<T: ViewGroup> {
    fun onPointerTouchEvent(event: IPointerEvent, view: T)
}