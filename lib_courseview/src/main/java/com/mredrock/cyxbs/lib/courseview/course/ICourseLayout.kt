package com.mredrock.cyxbs.lib.courseview.course

import android.content.Context
import android.view.View
import android.view.ViewGroupOverlay
import android.view.ViewParent
import com.mredrock.cyxbs.lib.courseview.course.utils.IFoldImpl
import com.mredrock.cyxbs.lib.courseview.net.INetLayout
import com.mredrock.cyxbs.lib.courseview.net.draw.ItemDecorationProvider
import com.mredrock.cyxbs.lib.courseview.net.save.SaveStateProvider
import com.mredrock.cyxbs.lib.courseview.net.touch.ItemTouchProvider

/**
 * 为了规范，不允许你直接传 CourseLayout，而是使用抽象
 *
 * 如果你需要使用 View 的一个方法的话，请使用下面用法：
 * - 在这个接口里面写上方法
 * -
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:26
 */
interface ICourseLayout :
    INetLayout, IFoldImpl,
    ItemDecorationProvider, ItemTouchProvider, SaveStateProvider {

    fun getContext(): Context
    fun post(action: Runnable): Boolean
    fun postOnAnimation(action: Runnable)
    fun postDelayed(action: Runnable, delayMillis: Long): Boolean
    fun postDelayed(delayMillis: Long, action: Runnable): Boolean = postDelayed(action, delayMillis)
    fun removeCallbacks(action: Runnable): Boolean
    fun invalidate()
    fun requestLayout()
    fun getParent(): ViewParent
    fun getChildCount(): Int
    fun getChildAt(index: Int): View
    fun setClipChildren(boolean: Boolean)
    fun getOverlay(): ViewGroupOverlay
    fun removeView(view: View)
    fun isAttachedToWindow(): Boolean
    fun addOnAttachStateChangeListener(l: View.OnAttachStateChangeListener)
    fun getTop(): Int
}