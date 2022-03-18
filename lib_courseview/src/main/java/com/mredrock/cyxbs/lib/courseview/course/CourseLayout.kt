package com.mredrock.cyxbs.lib.courseview.course

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.fold.OnFoldListener
import com.mredrock.cyxbs.lib.courseview.course.fold.RowState
import com.mredrock.cyxbs.lib.courseview.course.utils.*
import com.mredrock.cyxbs.lib.courseview.net.save.OnSaveStateListener

/**
 * 该 View 作用：
 * - 继承于 NetLayout，自定义对课程的布局
 * - 与课表需求进行高度耦合
 *
 * 注意事项：
 * - 注意该类实现的所有接口，尽量用接口来解耦合
 *
 * ```
 * ● ======== app:net_columnCount = 8 ======== ●
 * ↓                                           ↓
 * --------------------------------------------- ←- ●
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |   L   |                  S                |    ║
 * |   E   |                  H                |    ║
 * |   F   |                  O                |    ║ ←-←-←-←-←- Noon 时间段，占 1 x 7
 * |   T   |                  W                |    ║
 * |       |                                   | app:net_rowCount = 14
 * |   T   |                  L                |    ║
 * |   I   |                  E                |    ║
 * |   M   |                  S                |    ║ ←-←-←-←-←- Dusk 时间段，占 1 x 7
 * |   E   |                  S                |    ║
 * |       |                  O                |    ║
 * |       |                  N                |    ║
 * |       |                  S                |    ║
 * |       |                                   |    ║
 * --------------------------------------------- ←- ●
 * ↑       ↑                                   ↑
 * ↑       ↑                                   ↑
 * ↑       ● =========== Column = 7 ========== ●
 * ↑       ↑                  ↑
 * ● ===== ●              这里显示课程
 *     ↑
 *  这一列显示左侧的时间
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
internal class CourseLayout : AbstractCourseLayout, OnSaveStateListener {

    override fun getNoonRowState(): RowState {
        if (mNoonAnimation is FoldAnimation) return RowState.FOLD_ANIM
        if (mNoonAnimation is UnfoldAnimation) return RowState.UNFOLD_ANIM
        return mNoonRowState
    }

    override fun getDuskRowState(): RowState {
        if (mDuskAnimation is FoldAnimation) return RowState.FOLD_ANIM
        if (mDuskAnimation is UnfoldAnimation) return RowState.UNFOLD_ANIM
        return mDuskRowState
    }

    override fun changeNoonWeight(weight: Float) {
        for (row in NOON_TOP..NOON_BOTTOM) {
            setRowWeight(row, weight)
        }
    }

    override fun changeDuskWeight(weight: Float) {
        for (row in DUSK_TOP..DUSK_BOTTOM) {
            setRowWeight(row, weight)
        }
    }

    override fun foldNoonForce(onChanged: ((weight: Float, fraction: Float) -> Unit)?) {
        when (getNoonRowState()) {
            RowState.FOLD, RowState.FOLD_ANIM -> {
                mNoonAnimation?.doOnChange(onChanged)
                return
            }
            else -> mNoonAnimation?.cancel()
        }
        mNoonRowState = RowState.FOLD_ANIM
//        mNoonImageView.animation?.cancel()
//        mNoonImageView.startAnimation(mImgShowAnimation)
        mOnFoldNoonListener.forEach { it.onFoldStart(this) }
        val nowWeight = mNoonAnimation?.nowWeight ?: 0.99999F
        mNoonAnimation = FoldAnimation(nowWeight) { weight, fraction ->
            changeNoonWeight(weight)
            onChanged?.invoke(weight, fraction)
            mOnFoldNoonListener.forEach { it.onFolding(this, fraction) }
        }.doOnEnd {
            mNoonAnimation = null
            mNoonRowState = RowState.FOLD
//            mNoonImageView.visibility = VISIBLE
            mOnFoldNoonListener.forEach { it.onFoldEnd(this) }
        }.doOnCancel {
            mOnFoldNoonListener.forEach { it.onFoldCancel(this) }
        }.start()
    }

    override fun foldNoonWithoutAnim() {
        if (mNoonAnimation is FoldAnimation) return
        mNoonAnimation?.cancel()
        mNoonAnimation = null
        changeNoonWeight(0F)
        mNoonRowState = RowState.FOLD
//        mNoonImageView.animation?.cancel()
//        mNoonImageView.visibility = VISIBLE
        mOnFoldNoonListener.forEach { it.onFoldWithoutAnim(this) }
    }

    override fun unfoldNoonForce(onChanged: ((weight: Float, fraction: Float) -> Unit)?) {
        when (getNoonRowState()) {
            RowState.UNFOLD, RowState.UNFOLD_ANIM -> {
                mNoonAnimation?.doOnChange(onChanged)
                return
            }
            else -> mNoonAnimation?.cancel()
        }
        mNoonRowState = RowState.UNFOLD_ANIM
        val nowWeight = mNoonAnimation?.nowWeight ?: 0.00001F
//        mNoonImageView.animation?.cancel()
//        mNoonImageView.startAnimation(mImgHideAnimation)
        mOnFoldNoonListener.forEach { it.onUnfoldStart(this) }
        mNoonAnimation = UnfoldAnimation(nowWeight) { weight, fraction ->
            changeNoonWeight(weight)
            onChanged?.invoke(weight, fraction)
            mOnFoldNoonListener.forEach { it.onUnfolding(this, fraction) }
        }.doOnEnd {
            mNoonAnimation = null
            mNoonRowState = RowState.UNFOLD
//            mNoonImageView.visibility = INVISIBLE
            mOnFoldNoonListener.forEach { it.onUnfoldEnd(this) }
        }.doOnCancel {
            mOnFoldNoonListener.forEach { it.onUnfoldCancel(this) }
        }.start()
    }

    override fun unfoldNoonWithoutAnim() {
        if (mNoonAnimation is UnfoldAnimation) return
        mNoonAnimation?.cancel()
        mNoonAnimation = null
        changeNoonWeight(1F)
        mNoonRowState = RowState.UNFOLD
//        mNoonImageView.animation?.cancel()
//        mNoonImageView.visibility = INVISIBLE
        mOnFoldNoonListener.forEach { it.onUnfoldWithoutAnim(this) }
    }

    override fun foldDuskForce(onChanged: ((weight: Float, fraction: Float) -> Unit)?) {
        when (getDuskRowState()) {
            RowState.FOLD, RowState.FOLD_ANIM -> {
                mDuskAnimation?.doOnChange(onChanged)
                return
            }
            else -> mDuskAnimation?.cancel()
        }
        mDuskRowState = RowState.FOLD_ANIM
//        mDuskImageView.animation?.cancel()
//        mDuskImageView.startAnimation(mImgShowAnimation)
        mOnFoldDuskListener.forEach { it.onFoldStart(this) }
        val nowWeight = mDuskAnimation?.nowWeight ?: 0.99999F
        mDuskAnimation = FoldAnimation(nowWeight) { weight, fraction ->
            changeDuskWeight(weight)
            onChanged?.invoke(weight, fraction)
            mOnFoldDuskListener.forEach { it.onFolding(this, fraction) }
        }.doOnEnd {
            mDuskAnimation = null
            mDuskRowState = RowState.FOLD
//            mDuskImageView.visibility = VISIBLE
            mOnFoldDuskListener.forEach { it.onFoldEnd(this) }
        }.doOnCancel {
            mOnFoldDuskListener.forEach { it.onFoldCancel(this) }
        }.start()
    }

    override fun foldDuskWithoutAnim() {
        if (mDuskAnimation is FoldAnimation) return
        mDuskAnimation?.cancel()
        mDuskAnimation = null
        changeDuskWeight(0F)
        mDuskRowState = RowState.FOLD
//        mDuskImageView.animation?.cancel()
//        mDuskImageView.visibility = VISIBLE
        mOnFoldDuskListener.forEach { it.onFoldWithoutAnim(this) }
    }

    override fun unfoldDuskForce(onChanged: ((weight: Float, fraction: Float) -> Unit)?) {
        when (getDuskRowState()) {
            RowState.UNFOLD, RowState.UNFOLD_ANIM -> {
                mDuskAnimation?.doOnChange(onChanged)
                return
            }
            else -> mDuskAnimation?.cancel()
        }
        mDuskRowState = RowState.UNFOLD_ANIM
//        mDuskImageView.animation?.cancel()
//        mDuskImageView.startAnimation(mImgHideAnimation)
        mOnFoldDuskListener.forEach { it.onUnfoldStart(this) }
        val nowWeight = mDuskAnimation?.nowWeight ?: 0.00001F
        mDuskAnimation = UnfoldAnimation(nowWeight) { weight, fraction ->
            changeDuskWeight(weight)
            onChanged?.invoke(weight, fraction)
            mOnFoldDuskListener.forEach { it.onUnfolding(this, fraction) }
        }.doOnEnd {
            mDuskAnimation = null
            mDuskRowState = RowState.UNFOLD
//            mDuskImageView.visibility = INVISIBLE
            mOnFoldDuskListener.forEach { it.onUnfoldEnd(this) }
        }.doOnCancel {
            mOnFoldDuskListener.forEach { it.onUnfoldCancel(this) }
        }.start()
    }

    override fun unfoldDuskWithoutAnim() {
        if (mDuskAnimation is UnfoldAnimation) return
        mDuskAnimation?.cancel()
        mDuskAnimation = null
        changeDuskWeight(1F)
        mDuskRowState = RowState.UNFOLD
//        mDuskImageView.animation?.cancel()
//        mDuskImageView.visibility = INVISIBLE
        mOnFoldDuskListener.forEach { it.onUnfoldWithoutAnim(this) }
    }

    override fun addFoldNoonListener(l: OnFoldListener) {
        mOnFoldNoonListener.add(l)
    }

    override fun addFoldDuskListener(l: OnFoldListener) {
        mOnFoldDuskListener.add(l)
    }

//    private var mImgShowAnimation = AlphaAnimation(0F, 1F).apply { duration = 360 }
//    private var mImgHideAnimation = AlphaAnimation(1F, 0F).apply { duration = 300 }
    private var mNoonRowState = RowState.FOLD // 当前中午时间段的状态，主要用于上一层保险，不能光靠他来判断
    private var mDuskRowState = RowState.FOLD // 当前傍晚时间段的状态，主要用于上一层保险，不能光靠他来判断
    private var mOnFoldNoonListener = ArrayList<OnFoldListener>(3)
    private var mOnFoldDuskListener = ArrayList<OnFoldListener>(3)

    constructor(context: Context, attrs: CourseLayoutAttrs) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        /*
        * 以下两个 for 循环有如下作用：
        * 1、设置初始时中午和傍晚时间段的比重为 0，为了让板块初始时刚好撑满整个能够显示的高度，
        *    使中午和傍晚在折叠的状态下，外面的 ScrollView 不用滚动就刚好能显示其余板块
        * */
        for (row in NOON_TOP..NOON_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
        for (row in DUSK_TOP..DUSK_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
        // 下面这个 for 用于设置时间轴的初始化宽度
        for (column in TIME_LINE_LEFT..TIME_LINE_RIGHT) {
            setColumnInitialWeight(column, 0.8F)
        }
        addSaveStateListener(478741894, this)
    }

    private var mNoonAnimation: ChangeWeightAnimation? = null // 中午折叠或者展开的动画
    private var mDuskAnimation: ChangeWeightAnimation? = null // 傍晚折叠或者展开的动画

//    // 显示展开中午时间段的箭头，一个 ImageView
//    private val mNoonImageView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
//        findViewById(R.id.img_noon_arrow)
//    }
//
//    // 显示展开傍晚时间段的箭头，一个 ImageView
//    private val mDuskImageView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
//        findViewById(R.id.img_dusk_arrow)
//    }

    override fun isContainNoon(lp: CourseLayoutParams): Boolean {
        return lp.startRow <= NOON_TOP && lp.endRow >= NOON_BOTTOM
    }

    override fun isContainDusk(lp: CourseLayoutParams): Boolean {
        return lp.startRow <= DUSK_TOP && lp.endRow >= DUSK_BOTTOM
    }

    override fun measureChildWithRatio(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        childWidthRatio: Float,
        childHeightRatio: Float
    ) {
        val lp = child.layoutParams.net()
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val childWidth = (childWidthRatio * parentWidth).toInt()
        val childWidthMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childWidth,
                /*
                * 这里为什么直接给 EXACTLY ?
                * 1、课表初始状态需要填满整个父布局
                * 2、如果给 AT_MOST，则会使子 View 变成自己想要的大小,
                * 3、如果给 UNSPECIFIED 值，则会使没被重写的子 View 的宽直接变成 0，直接为 0 了就没了意义。
                *    原因可看：View#onMeasure() -> View#getDefaultSize()
                * */
                MeasureSpec.EXACTLY
            ),
            lp.leftMargin + lp.rightMargin, lp.width
        )

        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val childHeight = (childHeightRatio * parentHeight).toInt()
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childHeight,
                /*
                * 这里原因与上面相同，但还需要添加一些
                * 4、目前需求（22年）课表在开始时不显示中午和傍晚时间段，我设计的 NetLayout 可以把高度设置成
                *    wrap_content，再调用 setRowInitialWeight() 来隐藏中午和傍晚时间段
                * 5、课表如果要显示中午和傍晚时间段，则外布局需要包裹一个 NestedScrollView，这时，父布局得到的
                *    测量模式为 UNSPECIFIED，该模式会使课表初始状态不再填充父布局，所以需要改为 EXACTLY 模式
                * */
                MeasureSpec.EXACTLY
            ),
            lp.topMargin + lp.bottomMargin, lp.height
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return CourseLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is CourseLayoutParams -> CourseLayoutParams(lp)
            else -> throw CourseLayoutAttrsException("LayoutParams 必须是 CourseLayoutParams！")
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        throw CourseLayoutAttrsException("强制要求 LayoutParams 必须是 CourseLayoutParams！")
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is CourseLayoutParams
    }

    // 折叠动画
    private class FoldAnimation(
        nowWeight: Float = 0.99999F,
        onChanged: (weight: Float, fraction: Float) -> Unit
    ) : ChangeWeightAnimation(nowWeight, 0F, (nowWeight * 200).toLong(), onChanged)

    // 展开动画
    private class UnfoldAnimation(
        nowWeight: Float = 0.00001F,
        onChanged: (weight: Float, fraction: Float) -> Unit
    ) : ChangeWeightAnimation(nowWeight, 1F, ((1 - nowWeight) * 200).toLong(), onChanged)

    // 比重改变的动画封装类
    private abstract class ChangeWeightAnimation(
        startWeight: Float,
        endWeight: Float,
        val time: Long,
        private val onChanged: (weight: Float, fraction: Float) -> Unit
    ) {
        val nowWeight: Float
            get() = animator.animatedValue as Float

        private var animator: ValueAnimator = ValueAnimator.ofFloat(startWeight, endWeight)
        fun start(): ChangeWeightAnimation {
            animator.run {
                addUpdateListener { onChanged.invoke(nowWeight, animatedFraction) }
                duration = time
                this.start()
            }
            return this
        }
        fun doOnEnd(onEnd: () -> Unit): ChangeWeightAnimation {
            animator.doOnEnd { onEnd.invoke() }
            return this
        }
        fun doOnCancel(onCancel: () -> Unit): ChangeWeightAnimation {
            animator.doOnCancel { onCancel.invoke() }
            return this
        }
        fun doOnChange(onChanged: ((weight: Float, fraction: Float) -> Unit)?): ChangeWeightAnimation {
            if (onChanged == null) return this
            animator.addUpdateListener { onChanged.invoke(nowWeight, animator.animatedFraction) }
            return this
        }

        fun cancel() {
            animator.cancel()
        }
    }

    @CallSuper
    override fun onSaveState(): Parcelable {
        // 即将被摧毁，保存折叠状态
        val isFoldNoon = when (getNoonRowState()) {
            RowState.FOLD, RowState.FOLD_ANIM -> true
            RowState.UNFOLD, RowState.UNFOLD_ANIM -> false
        }
        val isFoldDusk = when (getDuskRowState()) {
            RowState.FOLD, RowState.FOLD_ANIM -> true
            RowState.UNFOLD, RowState.UNFOLD_ANIM -> false
        }
        return Bundle().apply {
            putBoolean("IS_FOLD_NOON", isFoldNoon)
            putBoolean("IS_FOLD_DUSK", isFoldDusk)
        }
    }

    @CallSuper
    override fun onRestoreState(savedState: Parcelable?) {
        if (savedState == null) return
        val bundle = savedState as Bundle
        val isFoldNoon = bundle.getBoolean("IS_FOLD_NOON")
        val isFoldDusk = bundle.getBoolean("IS_FOLD_DUSK")
        if (isFoldNoon) foldNoonWithoutAnim() else unfoldNoonWithoutAnim()
        if (isFoldDusk) foldDuskWithoutAnim() else unfoldDuskWithoutAnim()
    }
}