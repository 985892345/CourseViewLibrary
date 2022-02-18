package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 17:00
 */
abstract class AbstractTouchHandler<V: ViewGroup> : IPointerTouchHandler<V> {
    abstract var state: PointerState
}