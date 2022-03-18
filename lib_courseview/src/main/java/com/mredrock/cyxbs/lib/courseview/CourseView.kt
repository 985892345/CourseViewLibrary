package com.mredrock.cyxbs.lib.courseview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseContainer
import com.mredrock.cyxbs.lib.courseview.utils.WeekContainer

/**
 * 该 View 作用：
 * - 添加子 View
 * - 与课表的操控行为强耦合
 *
 * ```
 * [CourseView] ↘
 * -----------------------------------------------------------------------
 * |  [mWeek] ↘                                                          |
 * |  -----------------------------------------------------------------  |
 * |  |       |       |       |       |       |       |       |       |  |
 * |  | month |  mon  |  tue  |  wed  |  thu  |  fri  |  sat  |  sun  |  |
 * |  |       |       |       |       |       |       |       |       |  |
 * |  -----------------------------------------------------------------  |
 * |  [mCourse] ↘                                                        |
 * |  -----------------------------------------------------------------  |
 * |  | [CourseScrollView] ↗                                          |  |
 * |  |                                                               |  |
 * |  |  -----------------------------------------------------------  |  |
 * |  |  | [CourseLayout] ↗                                        |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  |                                                         |  |  |
 * |  |  -----------------------------------------------------------  |  |
 * |  -----------------------------------------------------------------  |
 * -----------------------------------------------------------------------
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        mWeek.initAttrs(attrs)
        mCourse.initAttrs(attrs)
    }

    fun getWeekContainer() = mWeek

    fun getCourseContainer() = mCourse

    fun setOnClickTouchAffairListener(onClick: (view: View, lp: CourseLayoutParams) -> Unit) {
        mOnClickTouchAffairListener = onClick
    }

    private val mWeek = WeekContainer(this)
    private val mCourse = CourseContainer(this)
    private var mOnClickTouchAffairListener: ((View, CourseLayoutParams) -> Unit)? = null

    init {
        orientation = VERTICAL
        initWeek()
        initCourse()
    }

    private fun initWeek() {
        addView(mWeek.layout)
    }

    private fun initCourse() {
        addView(mCourse.scroll)
//        mCourse.course.addItemDecoration(
//            CourseTimelineHelper(mCourse.course),
//        )
//        mCourse.course.addItemTouchListener(
//            CourseDownAnimHelper(mCourse.scroll, mCourse.course),
//            CourseMultiTouchHelper(mCourse.course, mCourse.scroll).apply {
//                add(
//                    FoldPointerDispatcher(),
//                    EntityMovePointerDispatcher(),
//                    CreateAffairPointerDispatcher().apply {
//                        setOnClickListener {
//                            it.remove()
//                            addAnyLesson(it.cloneLp(), "自习", "233", LessonHelper.LessonType.AFFAIR)
//                        }
//                    },
//                )
//            }
//        )
//
//        addMyLesson(0, 3, 2, "高等数学", "233")
//        addMyLesson(3, 5, 2, "大学物理", "233")
//        addMyLesson(5, 9, 4, "数据结构", "233")
//        addMyLesson(1, 11, 2, "离散数学", "233")
//        addMyAffair(2, -1, 1, "自习", "233")
//
//        addMyLesson(0, 5, 4, "顶部的课", "233")
//        addMyLesson(0, 5, 3, "中间的课", "233")
//        addMyLesson(0, 5, 2, "底部的课", "233")
    }
}