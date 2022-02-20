package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerDispatcher
import kotlin.collections.ArrayList

/**
 * 配合 [RecyclerTouchHandler] 一起使用的手指事件分发者
 *
 * 该类作用：
 * 1、加入回收机制，减少类重复生成
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 13:40
 */
abstract class RecyclerPointerDispatcher<V: ViewGroup> : IPointerDispatcher<V> {

    /**
     * [RecyclerTouchHandler] 池，用于复用，本身实现了 [Iterable]，支持遍历
     *
     * **NOTE:** 能否复用取决于 [RecyclerTouchHandler.flag] 标记，一定要在合适时间结束它
     */
    protected inner class HandlerPool<H: RecyclerTouchHandler<V>>(
        val create: () -> H
    ) : Iterable<H> {
        private val mHandlers = ArrayList<H>(3)

        fun getHandler(): H {
            mHandlers.forEach {
                if (it.flag == PointerFlag.OVER) {
                    return it
                }
            }
            return create().also {
                mHandlers.add(it)
            }
        }

        override fun iterator(): Iterator<H> {
            return mHandlers.iterator()
        }
    }
}