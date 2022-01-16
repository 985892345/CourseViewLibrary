package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs.Companion.newAttrs
import com.mredrock.cyxbs.lib.courseview.net.layout.AbstractNetLayoutManger

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/14
 */
class NetLayoutParams : ViewGroup.MarginLayoutParams {

    val childAttrs: NetChildAttrsImpl

    constructor(
        childAttrs: NetChildAttrsImpl = NetChildAttrsImpl.default()
    ) : super(MATCH_PARENT, MATCH_PARENT) {
        this.childAttrs = childAttrs
    }
    constructor(
        c: Context,
        attrs: AttributeSet
    ) : super(c, attrs) {
        this.childAttrs = NetChildAttrsImpl.newInstance(c, attrs)
    }
    constructor(
        width: Int,
        height: Int,
        childAttrs: NetChildAttrsImpl = NetChildAttrsImpl.default()
    ) : super(width, height) {
        this.childAttrs = childAttrs
    }
    constructor(
        source: NetLayoutParams,
    ) : super(source) {
        this.childAttrs = source.childAttrs.copy()
    }
    constructor(
        source: ViewGroup.LayoutParams,
        childAttrs: NetChildAttrsImpl = NetChildAttrsImpl.default()
    ) : super(source) {
        this.childAttrs = childAttrs
    }
    constructor(
        source: ViewGroup.MarginLayoutParams,
        childAttrs: NetChildAttrsImpl = NetChildAttrsImpl.default()
    ) : super(source) {
        this.childAttrs = childAttrs
    }

    data class NetChildAttrsImpl(
        override val position: Int = UNSET,
        override val start: Int = UNSET,
        override val end: Int = UNSET
    ) : BaseViewAttrs, AbstractNetLayoutManger.INetChildAttrs {

        companion object {
            fun newInstance(
                context: Context,
                attrs: AttributeSet
            ): NetChildAttrsImpl {
                return newAttrs(context, attrs, R.styleable.NetLayout_Layout) {
                    NetChildAttrsImpl(
                        R.styleable.NetLayout_Layout_net_position.int(UNSET),
                        R.styleable.NetLayout_Layout_net_start.int(UNSET),
                        R.styleable.NetLayout_Layout_net_end.int(UNSET)
                    )
                }
            }

            fun default(): NetChildAttrsImpl = NetChildAttrsImpl()

            const val UNSET = -1
        }
    }
}