package com.mredrock.cyxbs.lib.courseview.lesson.affair

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.utils.color
import com.mredrock.cyxbs.lib.courseview.utils.dp2px
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 描述:课表中事务的背景View，
 * 别问为什么不用图片，问就是图片太麻烦，而且效果还不好
 *
 * @author Jovines
 * @create 2020-01-26 2:36 PM
 *
 * @author 985892345
 * @data 2022/2/7 16:40
 * @describe 因需求变更，我开始重构课表，将学长之前写的该自定义 View 改为了一张 Drawable
 */
class AffairDrawable(context: Context) : Drawable() {

    private val mBgColor = R.color.white.color(context)
    private val mPaint = Paint().apply {
        color = R.color.common_transaction_background_stripe_color.color(context)
    }
    private val mRectF = RectF()
    private val mClipBounds = Rect()

    override fun draw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        canvas.getClipBounds(mClipBounds)
        val width = mClipBounds.width()
        val height = mClipBounds.height()
        val drawEdge = max(width, height) * sqrt(2F)
        val space = 8.dp2px()
        val num = (drawEdge / (space * 2)).toInt()
        canvas.save()
        canvas.translate(width / 2F, height / 2F)
        canvas.rotate(45F)
        mRectF.set(
            -drawEdge / 2,
            drawEdge / 2,
            -drawEdge / 2 + space,
            -drawEdge / 2
        )
        for (i in 0 until num) {
            canvas.drawRect(mRectF, mPaint)
            mRectF.set(
                mRectF.left + (space * 2),
                mRectF.top,
                mRectF.right + (space * 2),
                mRectF.bottom
            )
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}