package com.mredrock.cyxbs.lib.courseview.net.adater

import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.attrs.INetAttrs
import com.mredrock.cyxbs.lib.courseview.net.NetLayout

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
abstract class NetAdapter {
    /**
     * 设置课程属性
     *
     * 在你添加 View 到 [NetLayout] 后，如果这个 View 缺少课程属性，就会回调这个方法
     *
     * **NOTE：** 如果该方法没有设置好属性，则 View 会**直接添加失败**
     */
    abstract fun setNetAttrs(view: View, netAttrs: INetAttrs)
}