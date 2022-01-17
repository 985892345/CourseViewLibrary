package com.mredrock.cyxbs.lib.courseview.day.attrs

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.StyleableRes
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs.Companion.newAttrs
import com.mredrock.cyxbs.lib.courseview.day.utils.CourseDayAttrsException

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class CourseDayLayoutParams : ViewGroup.MarginLayoutParams {

    open fun isComplete(): Boolean {
        return lessonAttrs.startIndex >= 0 && lessonAttrs.length >= 1
    }

    val lessonAttrs: ILessonAttrs
    val gravity: Int

    constructor(
        gravity: Int = Gravity.CENTER,
        lessonAttrs: LessonAttrsImpl = LessonAttrsImpl.default()
    ) : super(MATCH_PARENT, MATCH_PARENT) {
        this.lessonAttrs = lessonAttrs
        this.gravity = gravity
    }
    constructor(
        c: Context,
        attrs: AttributeSet
    ) : super(c, attrs) {
        this.lessonAttrs = LessonAttrsImpl.newInstance(c, attrs)
        val a = c.obtainStyledAttributes(attrs, R.styleable.CourseDayLayout_Layout)
        gravity = a.getInt(
            R.styleable.CourseDayLayout_Layout_day_layoutGravity,
            Gravity.CENTER
        )
        a.recycle()
    }
    constructor(
        width: Int,
        height: Int,
        gravity: Int = Gravity.CENTER,
        lessonAttrs: ILessonAttrs
    ) : super(width, height) {
        this.lessonAttrs = lessonAttrs
        this.gravity = gravity
    }
    constructor(
        source: CourseDayLayoutParams,
    ) : super(source) {
        this.lessonAttrs = LessonAttrsImpl.clone(source.lessonAttrs)
        this.gravity = source.gravity
    }
    constructor(
        source: ViewGroup.LayoutParams,
        gravity: Int = Gravity.CENTER,
        lessonAttrs: ILessonAttrs = LessonAttrsImpl.default()
    ) : super(source) {
        this.lessonAttrs = lessonAttrs
        this.gravity = gravity
    }
    constructor(
        source: ViewGroup.MarginLayoutParams,
        gravity: Int = Gravity.CENTER,
        lessonAttrs: ILessonAttrs = LessonAttrsImpl.default()
    ) : super(source) {
        this.lessonAttrs = lessonAttrs
        this.gravity = gravity
    }

    data class LessonAttrsImpl(
        override var startIndex: Int,
        override var length: Int
    ) : BaseViewAttrs, ILessonAttrs {

        companion object {
            fun newInstance(
                context: Context,
                attrs: AttributeSet
            ): LessonAttrsImpl {
                return newAttrs(context, attrs, R.styleable.CourseDayLayout_Layout) {
                    LessonAttrsImpl(
                        R.styleable.CourseDayLayout_Layout_day_lessonStartIndex
                            .intOrThrow<CourseDayAttrsException>("day_lessonStartIndex"),
                        R.styleable.CourseDayLayout_Layout_day_lessonLength
                            .intOrThrow<CourseDayAttrsException>("day_lessonLength")
                    )
                }
            }

            fun clone(lessonAttrs: ILessonAttrs): LessonAttrsImpl {
                return LessonAttrsImpl(
                    startIndex = lessonAttrs.startIndex,
                    length = lessonAttrs.length
                )
            }

            fun default() = LessonAttrsImpl(UNSET, UNSET)

            const val UNSET = -1
        }
    }
}