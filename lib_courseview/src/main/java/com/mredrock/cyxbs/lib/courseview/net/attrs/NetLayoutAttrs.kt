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
 * @date 2022/1/17
 */
open class NetLayoutAttrs(
    rowCount: Int,
    columnCount: Int
) : BaseViewAttrs {

    var rowCount = rowCount
        internal set
    var columnCount = columnCount
        internal set

    companion object {
        fun newInstance(
            context: Context,
            attrs: AttributeSet
        ): NetLayoutAttrs {
            return newAttrs(context, attrs, R.styleable.NetLayout) {
                NetLayoutAttrs(
                    R.styleable.NetLayout_net_rowCount.int(ROW_COUNT),
                    R.styleable.NetLayout_net_columnCount.int(COLUMN_COUNT)
                )
            }
        }

        const val ROW_COUNT = 4
        const val COLUMN_COUNT = 4
    }
}