package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler

/**
 * 配合 [AbstractPointerDispatcher] 一起使用的 [IPointerTouchHandler]
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 17:00
 */
abstract class AbstractTouchHandler<V: ViewGroup> : IPointerTouchHandler<V> {

    /**
     * 用于配合 [AbstractPointerDispatcher] 是否能复用的标记
     *
     * 如果为 [PointerFlag.OVER]，则会自动复用
     *
     * **NOTE:** 请正确使用，防止生成重复对象
     */
    abstract var flag: PointerFlag
}