package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayoutParams : ViewGroup.MarginLayoutParams, Comparable<NetLayoutParams> {

    open fun isComplete(): Boolean {
        return netAttrs.startRow in 0..netAttrs.endRow
                && netAttrs.startColumn in 0..netAttrs.endColumn
    }

    var netAttrs: NetAttrs
        internal set
    val gravity: Int

    constructor(
        c: Context,
        attrs: AttributeSet
    ) : super(c, attrs) {
        this.netAttrs = NetAttrs.newInstance(c, attrs)
        val a = c.obtainStyledAttributes(attrs, R.styleable.NetLayout_Layout)
        gravity = a.getInt(
            R.styleable.NetLayout_Layout_net_layoutGravity,
            Gravity.CENTER
        )
        a.recycle()
    }
    constructor(
        width: Int,
        height: Int,
        netAttrs: NetAttrs,
        gravity: Int = Gravity.CENTER
    ) : super(width, height) {
        this.netAttrs = netAttrs
        this.gravity = gravity
    }
    constructor(
        source: NetLayoutParams,
    ) : super(source) {
        this.netAttrs = source.netAttrs.copy()
        this.gravity = source.gravity
    }

    override fun compareTo(other: NetLayoutParams): Int {
        val dNetAttrs = netAttrs.compareTo(other.netAttrs)
        if (dNetAttrs == 0) {
            return leftMargin + rightMargin + topMargin + bottomMargin -
                    (other.leftMargin + other.rightMargin + other.topMargin + other.bottomMargin)
        }
        return dNetAttrs
    }
}