package com.mredrock.cyxbs.lib.courseview.course.touch.multiple

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent

/**
 * 当前手指对应事件的处理者
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/17 11:03
 */
interface IPointerTouchHandler<V: ViewGroup> {
    fun onPointerTouchEvent(event: IPointerEvent, view: V)
}