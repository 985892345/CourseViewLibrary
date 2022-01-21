package com.mredrock.cyxbs.lib.courseview.course.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.int
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayoutParams : NetLayoutParams {

    var day: Int
    var lessonStartPos: Int
    var lessonLength: Int

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.CourseLayout_Layout)
        day = R.styleable.CourseLayout_Layout_course_layout_day.int(ty, UNSET)
        lessonStartPos = R.styleable.CourseLayout_Layout_course_layout_startPos.int(ty, UNSET)
        lessonLength = R.styleable.CourseLayout_Layout_course_layout_length.int(ty, UNSET)
        ty.recycle()
        startRow = lessonStartPos - 1
        endRow = startRow + lessonLength - 1
        startColumn = day
        endColumn = day
    }

    constructor(
        day: Int,
        lessonStartPos : Int,
        lessonLength: Int
    ) : super(
        MATCH_PARENT,
        MATCH_PARENT,
        lessonStartPos,
        lessonStartPos + lessonLength - 1,
        day,
        day
    ) {
        this.day = day
        this.lessonStartPos = lessonStartPos
        this.lessonLength = lessonLength
    }

    constructor(lp: CourseLayoutParams) : super(lp) {
        day = lp.day
        lessonStartPos = lp.lessonStartPos
        lessonLength = lp.lessonLength
    }
}