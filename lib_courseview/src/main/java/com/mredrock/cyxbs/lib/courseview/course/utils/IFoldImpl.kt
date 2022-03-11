package com.mredrock.cyxbs.lib.courseview.course.utils

import com.mredrock.cyxbs.lib.courseview.course.CourseLayout

/**
 * 折叠接口类
 *
 * 主要作用：降低 [CourseLayout] 与每个事件处理的耦合程度
*
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/7 16:02
 */
interface IFoldImpl {
    /**
     * 得到当前中午那一行的状态
     */
    fun getNoonRowState(): RowState

    /**
     * 得到当前中午那一行的状态
     */
    fun getDuskRowState(): RowState

    /**
     * 改变中午时间段所在行数的比重
     */
    fun changeNoonWeight(weight: Float)

    /**
     * 改变傍晚时间段所在行数的比重
     */
    fun changeDuskWeight(weight: Float)

    /**
     * 带有动画的强制折叠中午时间段。会 cancel 掉之前的动画
     */
    fun foldNoonForce(onChanged: ((Float) -> Unit)? = null)

    /**
     * 不带动画的立即折叠中午时间段。如果此时正处于展开动画，则立马取消；如果正处于折叠动画，则不做取消操作
     */
    fun foldNoonWithoutAnim()

    /**
     * 带有动画的强制展开中午时间段。会 cancel 掉之前的动画
     */
    fun unfoldNoonForce(onChanged: ((Float) -> Unit)? = null)

    /**
     * 不带动画的立即展开中午时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldNoonWithoutAnim()

    /**
     * 带有动画的强制折叠傍晚时间段。会 cancel 掉之前的动画
     */
    fun foldDuskForce(onChanged: ((Float) -> Unit)? = null)

    /**
     * 不带动画的立即折叠傍晚时间段。如果此时正处于展开动画，则立马取消；如果正处于折叠动画，则不做取消操作
     */
    fun foldDuskWithoutAnim()

    /**
     * 带有动画的强制展开中午时间段。会 cancel 掉之前的动画
     */
    fun unfoldDuskForce(onChanged: ((Float) -> Unit)? = null)

    /**
     * 不带动画的立即展开傍晚时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldDuskWithoutAnim()
}