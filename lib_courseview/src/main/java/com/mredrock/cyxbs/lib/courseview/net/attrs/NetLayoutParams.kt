package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs.Companion.newAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayoutParams : ViewGroup.MarginLayoutParams {

    open fun isComplete(): Boolean {
        return netAttrs.startRow in 0..netAttrs.endRow
                && netAttrs.startColumn in 0..netAttrs.endColumn
    }

    val netAttrs: INetAttrs
    val gravity: Int

    constructor(
        gravity: Int = Gravity.CENTER,
        lessonAttrs: NetAttrsImpl = NetAttrsImpl.default()
    ) : super(MATCH_PARENT, MATCH_PARENT) {
        this.netAttrs = lessonAttrs
        this.gravity = gravity
    }
    constructor(
        c: Context,
        attrs: AttributeSet
    ) : super(c, attrs) {
        this.netAttrs = NetAttrsImpl.newInstance(c, attrs)
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
        gravity: Int = Gravity.CENTER,
        netAttrs: INetAttrs
    ) : super(width, height) {
        this.netAttrs = netAttrs
        this.gravity = gravity
    }
    constructor(
        source: NetLayoutParams,
    ) : super(source) {
        this.netAttrs = NetAttrsImpl.clone(source.netAttrs)
        this.gravity = source.gravity
    }
    constructor(
        source: ViewGroup.LayoutParams,
        gravity: Int = Gravity.CENTER,
        netAttrs: INetAttrs = NetAttrsImpl.default()
    ) : super(source) {
        this.netAttrs = netAttrs
        this.gravity = gravity
    }
    constructor(
        source: ViewGroup.MarginLayoutParams,
        gravity: Int = Gravity.CENTER,
        netAttrs: INetAttrs = NetAttrsImpl.default()
    ) : super(source) {
        this.netAttrs = netAttrs
        this.gravity = gravity
    }

    data class NetAttrsImpl(
        override var startRow: Int,
        override var endRow: Int,
        override var startColumn: Int,
        override var endColumn: Int
    ) : BaseViewAttrs, INetAttrs {

        companion object {
            fun newInstance(
                context: Context,
                attrs: AttributeSet
            ): NetAttrsImpl {
                return newAttrs(context, attrs, R.styleable.NetLayout_Layout) {
                    NetAttrsImpl(
                        R.styleable.NetLayout_Layout_net_startRow.int(UNSET),
                        R.styleable.NetLayout_Layout_net_endRow.int(UNSET),
                        R.styleable.NetLayout_Layout_net_startColumn.int(UNSET),
                        R.styleable.NetLayout_Layout_net_endColumn.int(UNSET)
                    )
                }
            }

            fun clone(netAttrs: INetAttrs): NetAttrsImpl {
                return NetAttrsImpl(
                    startRow = netAttrs.startRow,
                    endRow = netAttrs.endRow,
                    startColumn = netAttrs.startColumn,
                    endColumn = netAttrs.endColumn
                )
            }

            fun default() = NetAttrsImpl(UNSET, UNSET, UNSET, UNSET)

            const val UNSET = -1
        }
    }
}