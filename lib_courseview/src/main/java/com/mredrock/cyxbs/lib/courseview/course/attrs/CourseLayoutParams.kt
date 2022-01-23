package com.mredrock.cyxbs.lib.courseview.course.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.CourseBean
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.int
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayoutParams : NetLayoutParams, CourseBean {

    override var day: Int
    override var startPos: Int
    override var length: Int

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.CourseLayout_Layout)
        day = R.styleable.CourseLayout_Layout_course_layout_day.int(ty, UNSET)
        startPos = R.styleable.CourseLayout_Layout_course_layout_startPos.int(ty, UNSET)
        length = R.styleable.CourseLayout_Layout_course_layout_length.int(ty, UNSET)
        ty.recycle()
        startRow = startPos - 1
        endRow = startRow + length - 1
        startColumn = day
        endColumn = day
    }

    constructor(
        day: Int,
        startPos : Int,
        length: Int
    ) : super(
        MATCH_PARENT,
        MATCH_PARENT,
        startPos,
        startPos + length - 1,
        day,
        day
    ) {
        this.day = day
        this.startPos = startPos
        this.length = length
    }

    constructor(lp: CourseLayoutParams) : super(lp) {
        day = lp.day
        startPos = lp.startPos
        length = lp.length
    }

    constructor(bean: CourseBean) : this(bean.day, bean.startPos, bean.length)
}