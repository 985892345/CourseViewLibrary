package com.mredrock.cyxbs.lib.courseview.helper.multitouch

import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import kotlin.collections.ArrayList

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 13:40
 */
abstract class AbstractPointerDispatcher
<V: ViewGroup, H: AbstractTouchHandler<V>> : IPointerDispatcher<V> {

    protected val mManger = Manger()

    abstract fun createNewHandler(): H

    abstract override fun getInterceptHandler(event: IPointerEvent, view: V): IPointerTouchHandler<V>?

    protected inner class Manger : Iterable<H> {
        private val mHandlers = ArrayList<H>(3)

        fun getHandler(): H {
            for (i in mHandlers.indices) {
                val handler = mHandlers[i]
                if (handler.state == PointerState.OVER) {
                    return handler
                }
            }
            return createNewHandler().also {
                mHandlers.add(it)
            }
        }

        override fun iterator(): Iterator<H> {
            return mHandlers.iterator()
        }
    }
}