package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import kotlin.collections.ArrayList

/**
 * ## 配合 [RecyclerTouchHandler] 一起使用的手指事件分发者
 *
 * ### 该类作用：
 * - 加入回收机制，减少类重复生成
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 13:40
 */
abstract class CourseRecyclerPointerDispatcher : CourseMultiTouchHelper.CoursePointerDispatcher() {

    /**
     * [RecyclerTouchHandler] 池，用于复用，本身实现了 [Iterable]，支持遍历
     *
     * **NOTE:** 能否复用取决于 [RecyclerTouchHandler.flag] 标记，一定要在合适时间结束它
     */
    protected inner class HandlerPool<H: RecyclerTouchHandler>(
        val create: () -> H
    ) : Iterable<H> {
        private val mHandlers = ArrayList<H>(3)

        fun getHandler(): H {
            mHandlers.forEach {
                // 检测标记，只有是 OVER 才会回收复用
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

    abstract class RecyclerTouchHandler : IPointerTouchHandler {

        /**
         * 用于配合 [CourseRecyclerPointerDispatcher] 是否能复用的标记
         *
         * 如果为 [PointerFlag.OVER]，则会自动复用
         *
         * **NOTE:** 请正确使用，防止生成重复对象
         */
        abstract var flag: PointerFlag

        /**
         * 从分发者中移除，应该在 UP 和 CANCEL 时调用
         *
         * **NOTE:** 移除后可能仍有动画的进行
         */
        protected abstract fun removeFromDispatcher(pointerId: Int)
    }
}