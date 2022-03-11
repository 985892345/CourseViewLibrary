package com.mredrock.cyxbs.lib.courseview.net

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.net.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.net.touch.OnItemTouchListener
import com.mredrock.cyxbs.lib.courseview.net.touch.TouchDispatcher
import com.mredrock.cyxbs.lib.courseview.net.save.OnSaveStateListener
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.draw.ItemDecorationProvider
import com.mredrock.cyxbs.lib.courseview.net.save.SaveStateProvider
import com.mredrock.cyxbs.lib.courseview.net.touch.ItemTouchProvider

/**
 * 专门用于提供一些分发扩展的 ViewGroup
 *
 * - 可扩展事件分发
 * - 提供绘图的分发
 * - 提供在试图被摧毁时保存数据的接口
 *
 * 因为提供了扩展，所以部分方法不允许重写
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/7 16:16
 */
open class NetLayout2 : NetLayout,
    ItemDecorationProvider, ItemTouchProvider, SaveStateProvider {

    override fun addItemDecoration(decor: ItemDecoration) {
        mItemDecoration.add(mItemDecoration.size, decor)
    }

    override fun addItemDecoration(decor: ItemDecoration, index: Int) {
        mItemDecoration.add(index, decor)
    }

    override fun addItemTouchListener(l: OnItemTouchListener) {
        mTouchDispatchHelper.addItemTouchListener(l, mTouchDispatchHelper.size)
    }

    override fun addItemTouchListener(l: OnItemTouchListener, index: Int) {
        mTouchDispatchHelper.addItemTouchListener(l, index)
    }

    override fun addSaveStateListener(tag: Int, l: OnSaveStateListener) {
        val bundle = mSaveBundleListenerCache[tag]
        if (bundle != null) {
            // 如果有之前保留的数据，意思是设置监听前就得到了保留的数据
            l.onRestoreState(bundle)
            mSaveBundleListenerCache.remove(tag)
        }
        mSaveBundleListeners.put(tag, l)
    }

    constructor(context: Context, attrs: NetLayoutAttrs) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    // 自定义绘图的监听
    private val mItemDecoration = ArrayList<ItemDecoration>(5)
    // 自定义事件分发帮助类
    private val mTouchDispatchHelper = TouchDispatcher()
    // 在 View 被摧毁时需要保存必要信息的监听
    private val mSaveBundleListeners = SparseArray<OnSaveStateListener>(3)

    final override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mTouchDispatchHelper.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }

    final override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mTouchDispatchHelper.onInterceptTouchEvent(ev, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    final override fun onTouchEvent(event: MotionEvent): Boolean {
        return mTouchDispatchHelper.onTouchEvent(event, this)
    }

    final override fun dispatchDraw(canvas: Canvas) {
        mItemDecoration.forEach {
            it.onDrawBelow(canvas, this)
        }
        super.dispatchDraw(canvas)
        mItemDecoration.forEach {
            it.onDrawAbove(canvas, this)
        }
    }

    // 如果没有设置监听，就暂时保存
    private val mSaveBundleListenerCache = SparseArray<Parcelable?>(3)

    final override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is NetSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        mSaveBundleListenerCache.clear()
        // 再恢复 mSaveBundleListeners 的状态
        state.saveBundleListeners.forEach { key, value ->
            val listener = mSaveBundleListeners[key]
            if (listener != null) {
                listener.onRestoreState(value)
            } else {
                mSaveBundleListenerCache.put(key, value)
            }
        }

    }

    final override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = NetSavedState(superState)
        // 保存 mSaveBundleListeners 的状态
        ss.saveBundleListeners = SparseArray(mSaveBundleListeners.size())
        mSaveBundleListeners.forEach { key, value ->
            ss.saveBundleListeners.put(key, value.onSaveState())
        }
        return ss
    }

    /**
     * 用于在布局被摧毁时保存必要的信息
     */
    private class NetSavedState : BaseSavedState {
        lateinit var saveBundleListeners: SparseArray<Parcelable?> // 保存的 mSaveBundleListeners 的信息

        constructor(superState: Parcelable?) : super(superState)

        @SuppressLint("ParcelClassLoader")
        constructor(source: Parcel) : super(source) {
            saveBundleListeners = source.readSparseArray<Parcelable?>(null)!!
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSparseArray(saveBundleListeners)
        }

        companion object CREATOR : Parcelable.Creator<NetSavedState> {
            override fun createFromParcel(source: Parcel): NetSavedState {
                return NetSavedState(source)
            }

            override fun newArray(size: Int): Array<NetSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}