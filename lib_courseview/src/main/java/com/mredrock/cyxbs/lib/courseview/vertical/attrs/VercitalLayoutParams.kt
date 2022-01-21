package com.mredrock.cyxbs.lib.courseview.vertical.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.boolean
import com.mredrock.cyxbs.lib.courseview.base.int

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class VercitalLayoutParams : ViewGroup.MarginLayoutParams {

    var isShow: Boolean
    var weight: Int
    var gravity: Int

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        val ty = context.obtainStyledAttributes(R.styleable.VerticalLayout_Layout)
        isShow = R.styleable.VerticalLayout_Layout_vertical_layout_isShow.boolean(ty, true)
        weight = R.styleable.VerticalLayout_Layout_vertical_layout_weight.int(ty, 0)
        gravity = R.styleable.NetLayout_Layout_net_layout_gravity.int(ty, Gravity.CENTER)
        ty.recycle()
    }

    constructor(
        width: Int,
        height: Int,
        isShow: Boolean = true,
        weight: Int = 0,
        gravity: Int = Gravity.CENTER
    ) : super(width, height) {
        this.isShow = isShow
        this.weight = weight
        this.gravity = gravity
    }

    constructor(
        lp: ViewGroup.LayoutParams
    ) : super(lp) {
        isShow = true
        weight = 0
        gravity = Gravity.CENTER
    }

    constructor(
        lp: VercitalLayoutParams
    ) : super(lp) {
        isShow = lp.isShow
        weight = lp.weight
        gravity = lp.gravity
    }
}