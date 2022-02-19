package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerFlag
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll.ScrollTouchHandler
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.VibratorUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 长按生成事务的事件处理类
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 19:15
 */
internal class CreateAffairTouchHandler(
    val course: CourseLayout,
    dispatcher: CreateAffairPointerDispatcher
) : CreateAffairPointerDispatcher.AbstractCreateAffairTouchHandler(dispatcher),
    ItemDecoration<CourseLayout> {

    /**
     * 设置触摸空白区域生成的用于添加事务的 View 的事件监听
     */
    fun setTouchAffairListener(l: OnTouchAffairListener) {
        mOnTouchAffairListener = l
    }

    fun start(event: IPointerEvent, view: TouchAffairView) {
        flag = PointerFlag.START
        mPointerId = event.pointerId

        mTouchAffairView = view
        view.isUsed = true

        mInitialRow = course.getRow(event.y.toInt())
        mInitialColumn = course.getColumn(event.x.toInt())
        mTopRow = mInitialRow // 重置
        mBottomRow = mInitialRow // 重置

        mIsInLongPress = false // 重置
        mLongPressRunnable.start()

        // 计算上下限，并且会再次判断是否能生成 mTouchAffair，所以只能放在最后
        calculateUpperLowerRow()
    }

    fun cancel() {
        if (mIsInLongPress) {
            mIsInLongPress = false // 重置
            mScrollRunnable.cancel()
        } else {
            mLongPressRunnable.cancel()
        }
        flag = PointerFlag.OVER
    }

    override var flag: PointerFlag = PointerFlag.OVER

    private lateinit var mTouchAffairView: TouchAffairView

    private var mIsNoonFoldedWhenLongPress = true // 长按开始时中午时间段是否处于折叠状态
    private var mIsDuskFoldedWhenLongPress = true // 长按开始时傍晚时间段是否处于折叠状态

    private var mTopRow = 0 // Move 事件中选择区域的开始行数
    private var mBottomRow = 0 // Move 事件中选择区域的结束行数
    private var mInitialRow = 0 // Down 事件中触摸的初始行数
    private var mInitialColumn = 0 // Down 事件中触摸的初始列数

    private var mTouchRow = 0 // 当前触摸的行数
    private var mUpperRow = 0 // 选择区域的上限
    private var mLowerRow = course.getRowCount() - 1 // 选择区域的下限

    private var mOnTouchAffairListener: OnTouchAffairListener? = null

    private var mPointerId = 0
    private var mIsInLongPress = false

    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    private val mLongPressRunnable = object : Runnable {
        private val mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
        override fun run() {
            mIsInLongPress = true
            longPressStart()
        }
        fun start() = course.postDelayed(this, mLongPressTimeout)
        fun cancel() = course.removeCallbacks(this)
    }

    override fun onPointerTouchEvent(event: IPointerEvent, view: CourseLayout) {
        if (flag == PointerFlag.OVER) {
            // 把事件转移给 ScrollTouchHandler 处理
            ScrollTouchHandler.get(event.pointerId)?.onPointerTouchEvent(event, view)
            return
        }
        when (event.action) {
            DOWN -> {
                // 这个会触发 DOWN 事件，但为了方便，我就不写在这里了，写在了 start() 中
            }
            MOVE -> {
                if (mIsInLongPress) {
                    scrollIsNecessary()
                    course.invalidate()
                } else {
                    val pointer = course.getAbsolutePointer(mPointerId)
                    if (abs(pointer.diffMoveX) > mTouchSlop
                        || abs(pointer.diffMoveY) > mTouchSlop
                    ) {
                        cancel() // 把事件转移给 ScrollTouchHandler 处理
                    }
                }
            }
            UP -> {
                if (mIsInLongPress) {
                    mIsInLongPress = false // 重置
                    mScrollRunnable.cancel()
                } else {
                    mLongPressRunnable.cancel()
                    val pointer = course.getAbsolutePointer(mPointerId)
                    if (abs(pointer.diffMoveX) <= mTouchSlop
                        && abs(pointer.diffMoveY) <= mTouchSlop
                    ) {
                        // 这里说明移动的距离小于 mTouchSlop，但还是得把点击的事务给绘制上，但是只有一格
                        if (!isAlreadyShow()) { // 如果屏幕上已经有了，则下一次的点击是删除操作
                            mTouchAffairView.show(mTopRow, mBottomRow, mInitialColumn)
                        }
                    }
                }
                // 在最后一个手指抬起时才恢复状态
                if (event.event.action == MotionEvent.ACTION_UP) {
                    recoverFoldState()
                }
                flag = PointerFlag.OVER
            }
            CANCEL -> {
                cancel()
            }
        }
    }

    override fun onDrawBelow(canvas: Canvas, view: CourseLayout) {
        /*
        * 在每次刷新时修改 mTouchAffairView 的位置
        * */
        if (mTouchAffairView.isCanRefresh() && mIsInLongPress) {
            refreshTouchAffairView()
        }
    }

    private fun longPressStart() {
        course.parent.requestDisallowInterceptTouchEvent(true)
        VibratorUtil.start(course.context, 36) // 长按被触发来个震动提醒
        // 记录长按开始时的中午状态
        mIsNoonFoldedWhenLongPress = when (course.getNoonRowState()) {
            RowState.FOLD, RowState.UNFOLD_ANIM -> true
            RowState.UNFOLD, RowState.FOLD_ANIM -> false
        }
        // 记录长按开始时的傍晚状态
        mIsDuskFoldedWhenLongPress = when (course.getDuskRowState()) {
            RowState.FOLD, RowState.UNFOLD_ANIM -> true
            RowState.UNFOLD, RowState.FOLD_ANIM -> false
        }
        mTouchAffairView.show(mTopRow, mBottomRow, mInitialColumn)
        // 回调监听
        mOnTouchAffairListener?.onCreateTouchAffair(
            mTouchAffairView,
            mTouchAffairView.layoutParams as CourseLayoutParams
        )
    }

    /**
     * 计算 [mUpperRow] 和 [mLowerRow]
     */
    private fun calculateUpperLowerRow() {
        mUpperRow = 0 // 重置
        mLowerRow = course.getRowCount() - 1 // 重置
        for (i in 0 until course.childCount) {
            val child = course.getChildAt(i)
            val lp = child.layoutParams as CourseLayoutParams
            if (mInitialColumn in lp.startColumn..lp.endColumn) {
                when {
                    mInitialRow > lp.endRow -> mUpperRow = max(mUpperRow, lp.endRow + 1)
                    mInitialRow < lp.startRow -> mLowerRow = min(mLowerRow, lp.startRow - 1)
                    else -> {
                        /*
                        * 这一步按理说是永远不会出现的，但在测试中一位学弟还真的触发了，怀疑是 getRow() 中精度的问题
                        * 虽然概率很低很低，但以防万一，直接在这里结束 mLongPressRunnable，允许父布局拦截事件即可解决
                        * */
                        mLongPressRunnable.cancel()
                    }
                }
            }
        }
    }

    /**
     * 判断当前滑动中是否需要自动展开中午或者傍晚时间段
     * @param touchRow 当前触摸的行数
     */
    private fun unfoldNoonOrDuskIfNecessary(touchRow: Int) {
        if (mInitialRow < NOON_TOP && mLowerRow >= NOON_TOP - 1 && touchRow >= NOON_TOP
            || mInitialRow > NOON_BOTTOM && mUpperRow <= NOON_BOTTOM + 1 && touchRow <= NOON_BOTTOM
            || touchRow in NOON_TOP..NOON_BOTTOM
        ) {
            when (course.getNoonRowState()) {
                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldNoonForce()
                else -> {}
            }
        }
        if (mInitialRow < DUSK_TOP && mLowerRow >= DUSK_TOP - 1 && touchRow >= DUSK_TOP
            || mInitialRow > DUSK_BOTTOM && mUpperRow <= DUSK_BOTTOM + 1 && touchRow <= NOON_BOTTOM
            || touchRow in DUSK_TOP..DUSK_BOTTOM
        ) {
            when (course.getDuskRowState()) {
                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldDuskForce()
                else -> {}
            }
        }
    }

    /**
     * 如果你手指滑到屏幕显示边缘区域时，则可能需要调用 [CourseScrollView] 滚动
     * ```
     * 该方法作用：
     * 1、计算当前触摸位置与 ScrollView 的距离来判断是否需要让 ScrollView 滚动
     * 2、计算让 ScrollView 滚动的速度
     * ```
     */
    private fun scrollIsNecessary() {
        mScrollRunnable.startIfCan()
    }

    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
    private val mScrollRunnable = object : Runnable {

        var isInScrolling = false // 是否处于滚动状态
            private set

        private var velocity = 0 // 滚动的速度

        override fun run() {
            if (isAllowScrollAndCalculateVelocity()) {
                course.scrollView.scrollBy(velocity)
                course.invalidate()
                ViewCompat.postOnAnimation(course, this)
            } else {
                isInScrolling = false
            }
        }

        /**
         * 如果能开启滚动的话就开启滚动，注意：滚动是会自己取消的
         */
        fun startIfCan() {
            if (!isInScrolling) { // 防止重复允许 Runnable
                isInScrolling = true
                run()
            }
        }

        /**
         * 取消滚动
         */
        fun cancel() {
            if (isInScrolling) {
                isInScrolling = false
                course.removeCallbacks(this)
            }
        }

        /**
         * 是否允许滚动，如果允许，则计算滚动速度给 [velocity] 变量
         */
        private fun isAllowScrollAndCalculateVelocity(): Boolean {
            val scroll = course.scrollView
            val nowHeight = course.getAbsolutePointer(mPointerId).lastMoveY
            val moveBoundary = 100 // 移动的边界值
            // 向上滚动，即手指移到底部，需要显示下面的内容
            val isNeedScrollUp =
                nowHeight > scroll.getHeight() - moveBoundary
                        && mTouchRow <= mLowerRow // 当前触摸的行数在下限以上
                        && scroll.getHeight() + scroll.getScrollY() != scroll.innerHeight // 是否滑到底

            // 向下滚动，即手指移到顶部，需要显示上面的内容
            val isNeedScrollDown =
                nowHeight < moveBoundary
                        && mTouchRow >= mUpperRow // 当前触摸的行数在上限以下
                        && scroll.getScrollY() != 0 // 是否滑到顶
            val isAllowScroll = isNeedScrollUp || isNeedScrollDown
            if (isAllowScroll) {
                velocity = if (isNeedScrollUp) {
                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
                    min((nowHeight - (scroll.getHeight() - moveBoundary)) / 10 + 6, 20)
                } else {
                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
                    -min(((moveBoundary - nowHeight) / 10 + 6), 20)
                }
            }
            return isAllowScroll
        }
    }

    private fun refreshTouchAffairView() {
        val y = course.let {
            it.getAbsolutePointer(mPointerId).lastMoveY - it.getDistanceToScrollView()
        }
        mTouchRow = course.getRow(y) // 当前触摸的行数
        var topRow: Int
        var bottomRow: Int
        // 根据当前触摸的行数与初始行数比较，得到 topRow、bottomRow
        if (mTouchRow > mInitialRow) {
            topRow = mInitialRow
            bottomRow = mTouchRow
        } else {
            topRow = mTouchRow
            bottomRow = mInitialRow
        }
        // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
        unfoldNoonOrDuskIfNecessary(mTouchRow)
        if (topRow < mUpperRow) topRow = mUpperRow // 根据上限再次修正 topRow
        if (bottomRow > mLowerRow) bottomRow = mLowerRow // 根据下限再次修正 bottomRow
        if (topRow != mTopRow || bottomRow != mBottomRow) { // 避免不必要的刷新
            mTouchAffairView.refresh(mTopRow, mBottomRow, topRow, bottomRow)
            mTopRow = topRow
            mBottomRow = bottomRow
        }
    }

    /**
     * 用于恢复折叠状态
     */
    private fun recoverFoldState() {
        var hasViewInNoon = false
        var hasViewInDusk = false
        for (i in 0 until course.childCount) {
            // 判断中午和傍晚时间段是否存在 View
            val child = course.getChildAt(i)
            if (child === mTouchAffairView) continue
            val lp = child.layoutParams as CourseLayoutParams
            when (lp.type) {
                // 目前只知道时间轴上的那几个类型不算
                CourseType.TIME, CourseType.ARROW_NOON, CourseType.ARROW_DUSK,
                CourseType.UNKNOWN -> {}
                else -> {
                    if (!hasViewInNoon && CourseLayout.isContainNoon(lp)) {
                        hasViewInNoon = true
                    }
                    if (!hasViewInDusk && CourseLayout.isContainDusk(lp)) {
                        hasViewInDusk = true
                    }
                }
            }
            if (hasViewInNoon && hasViewInDusk) break
        }
        val lp = mTouchAffairView.layoutParams as CourseLayoutParams
        if (mIsNoonFoldedWhenLongPress && !CourseLayout.isContainNoon(lp) && !hasViewInNoon) {
            when (course.getNoonRowState()) {
                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldNoonForce()
                else -> {}
            }
        }
        if (mIsDuskFoldedWhenLongPress && !CourseLayout.isContainDusk(lp) && !hasViewInDusk) {
            when (course.getDuskRowState()) {
                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldDuskForce()
                else -> {}
            }
        }
    }

//    override fun onSaveInstanceState(): Bundle? {
//        // mTouchAffairView 没有被添加时不用保存信息
//        if (mTouchAffairView.isAdded()) return null
//        // mTouchAffairView 被添加时保存 layoutParams
//        return Bundle().also {
//            it.putSerializable(
//                this::mTouchAffairView.name,
//                mTouchAffairView.layoutParams as CourseLayoutParams
//            )
//        }
//    }
//
//    override fun onRestoreInstanceState(bundle: Bundle?) {
//        if (bundle == null) return
//        // 恢复之前保存的 layoutParams
//        val lp = bundle.getSerializable(this::mTouchAffairView.name) as CourseLayoutParams
//        course.addCourse(mTouchAffairView, lp)
//        // 从竖屏到横屏时，显示的 mTouchAffairView 可能会在屏幕显示区域外，所以需要调整 ScrollView 的 scrollY，
//        // 让 mTouchAffairView 刚好显示在屏幕内
//        // 但回调 onRestoreInstanceState() 时还没有开始布局，得不到 top 值，所以需要用 post 在布局后设置
//        course.post {
//            course.scrollView.setScrollY(mTouchAffairView.top + course.getDistanceToScrollView() - 60)
//        }
//    }

    init {
        course.addCourseDecoration(this)
//        course.addSaveBundleListener(this)
    }

    interface OnTouchAffairListener {
        /**
         * 创建 TouchAffairView 的监听
         *
         * 在 addView 之前回调，可以直接设置一些属性
         */
        fun onCreateTouchAffair(view: View, lp: CourseLayoutParams)
    }
}