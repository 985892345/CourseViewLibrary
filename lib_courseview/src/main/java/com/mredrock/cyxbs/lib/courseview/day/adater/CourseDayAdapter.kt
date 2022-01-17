package com.mredrock.cyxbs.lib.courseview.day.adater

import com.mredrock.cyxbs.lib.courseview.day.attrs.ILessonAttrs
import com.mredrock.cyxbs.lib.courseview.day.CourseDayLayout

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
abstract class CourseDayAdapter {
    /**
     * 设置课程属性
     *
     * 在你添加 View 到 [CourseDayLayout] 后，如果这个 View 缺少课程属性，就会回调这个方法
     *
     * **NOTE：** 如果该方法没有设置好属性，则 View 会**直接添加失败**
     */
    abstract fun setLessonAttrs(lessonAttrs: ILessonAttrs)
}