package com.mredrock.cyxbs.lib.courseview.net.touch

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/18
 */
abstract class MoveItemController {

    abstract fun isIntercept(view: View, netAttrs: NetAttrs): Boolean

    protected lateinit var mDragView: View
    protected lateinit var mNetAttrs: NetAttrs

    protected var mInitialX = 0F
    protected var mInitialY = 0F

    protected var mX = 0F
    protected var mY = 0F

    open fun onInterceptDownEvent(event: MotionEvent, view: View, netAttrs: NetAttrs): Boolean {
        mDragView = view
        mNetAttrs = netAttrs
        return isIntercept(view, netAttrs)
    }

    open fun onTouchEvent(event: MotionEvent) {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {

            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
    }

    open fun drawItem(view: View, dx: Float, dy: Float) {
        view.translationX = dx
        view.translationY = dy
    }

    class RecoverAnimation(
        view: View,
        startDx: Float,
        startDy: Float,
        targetX: Float,
        targetY: Float
    ) : Animator.AnimatorListener {

        var mX = startDx
        var mY = startDy
        private var mFraction = 0F

        private val mValueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { mFraction = it.animatedFraction }
            addListener(this@RecoverAnimation)
        }

        fun start() {
            mValueAnimator.start()
        }

        fun update() {
        }

        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }
}