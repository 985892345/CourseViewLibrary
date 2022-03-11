package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/20 20:44
 */
interface ITouchView {
    /**
     * 克隆一个 [CourseLayoutParams]
     */
    fun cloneLp(): CourseLayoutParams

    /**
     * 移除显示
     */
    fun remove()
}