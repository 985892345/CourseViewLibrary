package com.mredrock.cyxbs.lib.courseview.course

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.fold.IFoldImpl
import com.mredrock.cyxbs.lib.courseview.course.fold.OnFoldListener
import com.mredrock.cyxbs.lib.courseview.net.NetLayout2
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/13 14:11
 */
abstract class AbstractCourseLayout(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : NetLayout2(context, attrs, defStyleAttr, defStyleRes), IFoldImpl {

    /**
     * 添加课程
     */
    fun addCourse(view: View, lp: CourseLayoutParams) {
        super.addItem(view, lp)
    }

    /**
     * 增加中午时间段的折叠或展开的监听
     */
    abstract fun addFoldNoonListener(l: OnFoldListener)

    /**
     * 增加傍晚时间段的折叠或展开的监听
     */
    abstract fun addFoldDuskListener(l: OnFoldListener)

    /**
     * 静止状态下是否包含中午时间段
     */
    abstract fun isContainNoon(lp: CourseLayoutParams): Boolean

    /**
     * 静止状态下是否包含傍晚时间段
     */
    abstract fun isContainDusk(lp: CourseLayoutParams): Boolean

    /**
     * 属性文件
     */
    protected val mCourseAttrs: CourseLayoutAttrs = CourseLayoutAttrs(mNetAttrs)

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun addItem(item: View, lp: NetLayoutParams) {
        super.addItem(item, lp)
    }

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun addView(child: View) {
        super.addView(child)
    }

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun addView(child: View, index: Int) {
        super.addView(child, index)
    }

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun addView(child: View, width: Int, height: Int) {
        super.addView(child, width, height)
    }

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun addView(child: View, params: LayoutParams?) {
        super.addView(child, params)
    }

    @Deprecated(
        "不建议使用该方法",
        ReplaceWith("addCourse(view, lp)"),
        DeprecationLevel.HIDDEN
    )
    override fun attachViewToParent(child: View, index: Int, params: LayoutParams?) {
        super.attachViewToParent(child, index, params)
    }

    companion object {
        /*
        * 为什么要这样区分开始行和结束行？
        * 原因如下：
        * 1、方便以后好维护
        * 2、虽然目前中午和傍晚只有一行，但也不能保证以后不改为两行，所以中午和傍晚也得分为开始行和结束行
        * 3、课的话也是如此，但因为有了中午和傍晚，所以课与行数不对应
        * 尽量不要自己写数字来固定
        * */
        const val AM_TOP = 0 // 上午板块开始行
        const val AM_BOTTOM = 3 // 上午板块结束行
        const val NOON_TOP = 4 // 中午板块开始行
        const val NOON_BOTTOM = 4 // 中午板块结束行
        const val PM_TOP = 5 // 下午板块开始行
        const val PM_BOTTOM = 8 // 下午板块结束行
        const val DUSK_TOP = 9 // 傍晚板块开始行
        const val DUSK_BOTTOM = 9 // 傍晚板块结束行
        const val NIGHT_TOP = 10 // 晚上板块开始行
        const val NIGHT_BOTTOM = 13 // 晚上板块结束行

        const val TIME_LINE_LEFT = 0 // 时间轴开始列
        const val TIME_LINE_RIGHT = 0 // 时间轴结束列

        // 第一节课
        const val LESSON_1_TOP = 0
        const val LESSON_1_BOTTOM = 0
        // 第二节课
        const val LESSON_2_TOP = 1
        const val LESSON_2_BOTTOM = 1
        // 第三节课
        const val LESSON_3_TOP = 2
        const val LESSON_3_BOTTOM = 2
        // 第四节课
        const val LESSON_4_TOP = 3
        const val LESSON_4_BOTTOM = 3
        // 第五节课
        const val LESSON_5_TOP = 5
        const val LESSON_5_BOTTOM = 5
        // 第六节课
        const val LESSON_6_TOP = 6
        const val LESSON_6_BOTTOM = 6
        // 第七节课
        const val LESSON_7_TOP = 7
        const val LESSON_7_BOTTOM = 7
        // 第八节课
        const val LESSON_8_TOP = 8
        const val LESSON_8_BOTTOM = 8
        // 第九节课
        const val LESSON_9_TOP = 10
        const val LESSON_9_BOTTOM = 10
        // 第十节课
        const val LESSON_10_TOP = 11
        const val LESSON_10_BOTTOM = 11
        // 第十一节课
        const val LESSON_11_TOP = 12
        const val LESSON_11_BOTTOM = 12
        // 第十二节课
        const val LESSON_12_TOP = 13
        const val LESSON_12_BOTTOM = 13
    }
}