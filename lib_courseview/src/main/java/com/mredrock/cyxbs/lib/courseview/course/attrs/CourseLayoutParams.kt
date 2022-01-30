package com.mredrock.cyxbs.lib.courseview.course.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.CourseBean
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.int
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.courseType
import java.io.Serializable

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayoutParams : NetLayoutParams, CourseBean, Serializable {

    override val day: Int
        get() = startColumn
    override val startPos: Int
        get() = startRow + 1
    override val length: Int
        get() = endRow - startRow + 1
    override var type: CourseType

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.CourseLayout_Layout)
        val day = R.styleable.CourseLayout_Layout_course_layout_day.int(ty, UNSET)
        val startPos = R.styleable.CourseLayout_Layout_course_layout_startPos.int(ty, UNSET)
        val length = R.styleable.CourseLayout_Layout_course_layout_length.int(ty, UNSET)
        type = R.styleable.CourseLayout_Layout_course_layout_type.courseType(ty)
        ty.recycle()
        if (startPos != UNSET) {
            startRow = startPos - 1
        }
        if (length != UNSET) {
            endRow = startRow + length - 1
        }
        if (day != UNSET) {
            startColumn = day
            endColumn = day
        }
    }

    constructor(
        day: Int,
        startPos : Int,
        length: Int,
        type: CourseType
    ) : this(
        startPos,
        startPos + length - 1,
        day,
        day,
        type
    )

    constructor(
        startRow: Int,
        endRow: Int,
        startColumn: Int,
        endColumn: Int,
        type: CourseType
    ) : super(startRow, endRow, startColumn, endColumn) {
        this.type = type
    }

    constructor(lp: CourseLayoutParams) : super(lp) {
        type = lp.type
    }

    constructor(bean: CourseBean) : this(bean.day, bean.startPos, bean.length, bean.type)
}