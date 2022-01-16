package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs.Companion.newAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/16
 */
data class NetLayoutAttrs(
    internal val minRow: Int,
    internal val minColumn: Int,
    internal val orientation: Orientation
) : BaseViewAttrs {
    companion object {
        fun newInstance(
            context: Context,
            attrs: AttributeSet
        ) : NetLayoutAttrs {
            return newAttrs(context, attrs, R.styleable.NetLayout) {
                NetLayoutAttrs(
                    R.styleable.NetLayout_net_minRow.int(MIN_ROW),
                    R.styleable.NetLayout_net_minColumn.int(MIN_COLUMN),
                    R.styleable.NetLayout_net_orientation.orientation(this)
                )
            }
        }

        const val MIN_ROW = 3
        const val MIN_COLUMN = 3
    }
}