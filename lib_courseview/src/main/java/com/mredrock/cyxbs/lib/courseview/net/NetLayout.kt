package com.mredrock.cyxbs.lib.courseview.net

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import com.mredrock.cyxbs.lib.courseview.net.attrs.Orientation
import com.mredrock.cyxbs.lib.courseview.net.layout.AbstractNetLayoutManger
import com.mredrock.cyxbs.lib.courseview.net.layout.column.ColumnLayoutManger
import com.mredrock.cyxbs.lib.courseview.net.layout.row.RowLayoutManger

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/16
 */
class NetLayout : ViewGroup {

    private val mAttrs: NetLayoutAttrs
    private val mLayout: AbstractNetLayoutManger

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAttrs = NetLayoutAttrs.newInstance(context, attrs)
        mLayout = when (mAttrs.orientation) {
            Orientation.LEFT_RIGHT, Orientation.RIGHT_LEFT -> ColumnLayoutManger(mAttrs)
            Orientation.TOP_BOTTOM, Orientation.BOTTOM_TOP -> RowLayoutManger(mAttrs)
        }
    }

    constructor(context: Context, attrs: NetLayoutAttrs) : super(context) {
        mAttrs = attrs.copy()
        mLayout = when (mAttrs.orientation) {
            Orientation.LEFT_RIGHT, Orientation.RIGHT_LEFT -> ColumnLayoutManger(mAttrs)
            Orientation.TOP_BOTTOM, Orientation.BOTTOM_TOP -> RowLayoutManger(mAttrs)
        }
    }

    override fun onViewAdded(child: View) {
        mLayout.addView(child,)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = mLayout.measureWidth(widthMeasureSpec, heightMeasureSpec)
        val height = mLayout.measureHeight(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mLayout.layout(changed, l, t, r, b)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return NetLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return if (p is MarginLayoutParams) {
            NetLayoutParams(p) // 这个 p 会隐式装换成 MarginLayoutParams
        } else {
            NetLayoutParams(p)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return NetLayoutParams()
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is NetLayoutParams
    }
}