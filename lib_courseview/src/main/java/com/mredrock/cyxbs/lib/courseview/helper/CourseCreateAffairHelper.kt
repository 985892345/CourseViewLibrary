//package com.mredrock.cyxbs.lib.courseview.helper
//
//import android.animation.ValueAnimator
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.drawable.GradientDrawable
//import android.os.*
//import android.view.*
//import android.view.animation.AlphaAnimation
//import android.view.animation.DecelerateInterpolator
//import android.widget.ImageView
//import androidx.core.animation.doOnCancel
//import androidx.core.animation.doOnEnd
//import androidx.core.view.ViewCompat
//import com.mredrock.cyxbs.lib.courseview.R
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
//import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
//import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
//import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
//import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
//import com.mredrock.cyxbs.lib.courseview.course.utils.OnSaveBundleListener
//import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
//import com.mredrock.cyxbs.lib.courseview.helper.ILongPressEntityMove.LongPressState.ANIM_RESTORE
//import com.mredrock.cyxbs.lib.courseview.helper.ILongPressEntityMove.LongPressState.ANIM_TRAVEL
//import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
//import com.mredrock.cyxbs.lib.courseview.utils.CourseType
//import com.mredrock.cyxbs.lib.courseview.utils.VibratorUtil
//import com.mredrock.cyxbs.lib.courseview.utils.ViewExtend
//import com.mredrock.cyxbs.lib.courseview.utils.lazyUnlock
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
///**
// * ```
// * 该类作用：
// * 1、封装长按生成自定义行程的功能
// * 2、支持添加事务的灰色 View 的长按整体移动
// *
// * 该类设计：
// * 1、对 CourseLayout 增加事件监听来实现
// * 2、事件监听参考了 RV 的 ItemTouchHelper 的设计
// * 3、手机转屏后仍能恢复之前长按显示的事务（虽然目前掌邮不会用到这个功能）
// *
// * 注意事项：
// * 1、该类只管理创建事务，请不要添加一些不属于该类的功能，想添加功能应该再写一个 OnCourseTouchListener
// * 2、长按整体移动的实现基本依靠 CourseLongPressEntityMoveHelper
// * ```
// * @author 985892345 (Guo Xiangrui)
// * @email 2767465918@qq.com
// * @date 2022/1/27
// */
//class CourseCreateAffairHelper private constructor(
//    private val course: CourseLayout
//) : OnItemTouchListener<CourseLayout>, ItemDecoration<CourseLayout>, OnSaveBundleListener, ViewExtend {
//
//    /**
//     * 设置触摸空白区域生成的用于添加事务的 View 的点击监听
//     */
//    fun setTouchAffairViewClickListener(l: View.OnClickListener) {
//        mTouchAffairView.setOnClickListener(l)
//    }
//
//    fun removeTouchAffairView() {
//        if (mTouchAffairView.parent != null) {
//            course.removeView(mTouchAffairView)
//        }
//    }
//
//    /**
//     * 设置长按整体移动监听，不是长按生成事务的监听
//     */
//    fun setEntiretyMoveListener(l: ILongPressEntityMove.OnEntityMoveListener) {
//        mEntityMoveHelper.setEntityMoveListener(l)
//    }
//
//    /**
//     * 设置触摸空白区域生成的用于添加事务的 View 的事件监听
//     */
//    fun setTouchAffairListener(l: OnTouchAffairListener) {
//        mOnTouchAffairListener = l
//    }
//
//    override val context: Context = course.context
//    private var mInitialX = 0 // Down 时的初始 X 值
//    private var mInitialY = 0 // Down 时的初始 Y 值
//    private var mLastMoveX = 0 // Move 时的移动 X 值
//    private var mLastMoveY = 0 // Move 时的移动 Y 值
//
//    private var mIsNoonFoldedWhenLongPress = true // 长按开始时中午时间段是否处于折叠状态
//    private var mIsDuskFoldedWhenLongPress = true // 长按开始时傍晚时间段是否处于折叠状态
//
//    private var mTopRow = 0 // Move 事件中选择区域的开始行数
//    private var mBottomRow = 0 // Move 事件中选择区域的结束行数
//    private var mInitialRow = 0 // Down 事件中触摸的初始行数
//    private var mInitialColumn = 0 // Down 事件中触摸的初始列数
//
//    private var mTouchRow = 0 // 当前触摸的行数
//    private var mUpperRow = 0 // 选择区域的上限
//    private var mLowerRow = course.getRowCount() - 1 // 选择区域的下限
//
//    private var mOnTouchAffairListener: OnTouchAffairListener?= null
//
//    // 点击了其他地方让 mTouchAffairView 消失的动画
//    private var mAlphaValueAnimator: ValueAnimator? = null
//
//    private var mIsInEntityMove = false // 是否该长按整体移动拦截事件
//    // 长按整体移动的帮助类
//    private var mEntityMoveHelper = CourseLongPressEntityMoveHelper(course) {
//        it === mTouchAffairView // 只有是 mTouchAffairView 才能开启长按整体移动
//    }
//
//    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
//    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop
//
//    // 识别为长按所需的时间
//    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
//
//    // 是否处于长按状态
//    private var mIsInLongPress = false
//
//    // 长按时执行的 Runnable
//    private val mLongPressRunnable = Runnable {
//        mIsInLongPress = true
//        mAlphaValueAnimator?.cancel() // 取消之前 mTouchAffairView 可能存在的动画
//        mTouchAffairView.show()
//        VibratorUtil.start(course.context, 36) // 长按被触发来个震动提醒
//        // 记录长按开始时的中午状态
//        mIsNoonFoldedWhenLongPress = when (course.getNoonRowState()) {
//            RowState.FOLD, RowState.UNFOLD_ANIM -> true
//            RowState.UNFOLD, RowState.FOLD_ANIM -> false
//        }
//        // 记录长按开始时的傍晚状态
//        mIsDuskFoldedWhenLongPress = when (course.getDuskRowState()) {
//            RowState.FOLD, RowState.UNFOLD_ANIM -> true
//            RowState.UNFOLD, RowState.FOLD_ANIM -> false
//        }
//    }
//
//    override fun isAdvanceIntercept(event: MotionEvent, course: CourseLayout): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            mIsInEntityMove = false // 重置
//            val x = event.x.toInt()
//            val y = event.y.toInt()
//            val touchView = course.findItemUnderByXY(x, y)
//            removeLastTouchAffairViewWhenNextDown(touchView)
//            if (touchView == null) {
//                /*
//                * 如果为 null，直接拦截
//                * 长按整体移动与长按移动生成事务是互相排斥的，因为前者需要长按事务这个 View，而后者需要长按空白区域
//                * return true 后 isAdvanceIntercept() 就不会再被调用
//                * */
//                return true
//            }
//            /*
//            * 如果 Down 事件没有拦截，就全权交给整体移动的帮助类处理
//            * */
//            mIsInEntityMove = true
//        }
//        return mEntityMoveHelper.isAdvanceIntercept(event, course)
//    }
//
//    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
//        if (mIsInEntityMove) {
//            mEntityMoveHelper.onTouchEvent(event, course)
//            return
//        }
//        val x = event.x.toInt()
//        val y = event.y.toInt()
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                mInitialX = x
//                mInitialY = y
//                mLastMoveX = x
//                mLastMoveY = y
//                mInitialRow = course.getRow(y)
//                mInitialColumn = course.getColumn(x)
//                mTopRow = mInitialRow // 重置
//                mBottomRow = mInitialRow // 重置
//
//                mUpperRow = 0 // 重置
//                mLowerRow = course.getRowCount() - 1 // 重置
//
//                mIsInLongPress = false // 重置
//                course.postDelayed(mLongPressRunnable, mLongPressTimeout)
//                // 禁止外面的 ScrollView 拦截事件
//                course.parent.requestDisallowInterceptTouchEvent(true)
//
//                // 计算上下限，并且会再次判断是否能生成 mTouchAffair，所以只能放在最后
//                calculateUpperLowerRow()
//            }
//            MotionEvent.ACTION_MOVE -> {
//                if (mIsInLongPress) { // 处于长按状态
//                    mLastMoveX = x
//                    mLastMoveY = y
//                    scrollIsNecessary()
//                    course.invalidate()
//                } else {
//                    if (abs(x - mLastMoveX) <= mTouchSlop
//                        && abs(y - mLastMoveY) <= mTouchSlop
//                    ) {
//                        /*
//                        * 走到该分支说明：
//                        * 1、不处于长按状态，即 mIsInLongPress = false
//                        * 2、移动距离小于认定是滑动的 mTouchSlop
//                        *
//                        * 下一次移动有下面三条路可以走
//                        * 1、可能仍然回到这里
//                        * 2、如果移动距离大于 mTouchSlop，则会走下面那个 else 分支
//                        * 3、如果 mIsInLongPress 被赋值为 true（说明达到长按时间）
//                        * */
//                    } else {
//                        // 走到该分支说明在判定为长按的时间内，移动的距离大于了 mTouchSlop
//                        // 接下来的一系列事件就不该自身处理，应该被 CourseScrollView 当成滚动而拦截
//                        course.parent.requestDisallowInterceptTouchEvent(false)
//                        course.removeCallbacks(mLongPressRunnable)
//                    }
//                }
//            }
//            MotionEvent.ACTION_UP -> {
//                if (mIsInLongPress) {
//                    mIsInLongPress = false // 重置
//                    mScrollRunnable.cancel()
//                } else {
//                    course.removeCallbacks(mLongPressRunnable)
//                    if (abs(x - mLastMoveX) <= mTouchSlop
//                        && abs(y - mLastMoveY) <= mTouchSlop
//                    ) {
//                        // 这里说明移动的距离小于 mTouchSlop，但还是得把点击的事务给绘制上，但是只有一格
//                        if (mAlphaValueAnimator == null) { // 防止之前已经被添加
//                            mTouchAffairView.show()
//                        }
//                    }
//                }
//                recoverFoldState(false)
//            }
//            MotionEvent.ACTION_CANCEL -> {
//                if (mIsInLongPress) {
//                    mIsInLongPress = false // 重置
//                    mScrollRunnable.cancel()
//                } else {
//                    course.removeCallbacks(mLongPressRunnable)
//                }
//            }
//        }
//    }
//
//    override fun onCancelDownEvent(event: MotionEvent, course: CourseLayout) {
//        // 这里是被前一个 onCourseTouchListener 拦截 Down 事件的时候
//        val x = event.x.toInt()
//        val y = event.y.toInt()
//        val touchView = course.findItemUnderByXY(x, y)
//        removeLastTouchAffairViewWhenNextDown(touchView)
//    }
//
//    /**
//     * 计算 [mUpperRow] 和 [mLowerRow]
//     */
//    private fun calculateUpperLowerRow() {
//        for (i in 0 until course.childCount) {
//            val child = course.getChildAt(i)
//            val lp = child.layoutParams as CourseLayoutParams
//            if (mInitialColumn in lp.startColumn..lp.endColumn) {
//                when {
//                    mInitialRow > lp.endRow -> mUpperRow = max(mUpperRow, lp.endRow + 1)
//                    mInitialRow < lp.startRow -> mLowerRow = min(mLowerRow, lp.startRow - 1)
//                    else -> {
//                        /*
//                        * 这一步按理说是永远不会出现的，但在测试中一位学弟还真的触发了，怀疑是 getRow() 中精度的问题
//                        * 虽然概率很低很低，但以防万一，直接在这里结束 mLongPressRunnable，允许父布局拦截事件即可解决
//                        * */
//                        course.removeCallbacks(mLongPressRunnable)
//                        course.parent.requestDisallowInterceptTouchEvent(false)
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 用于添加事务的 View，注意：并不是最终显示事务的那个 View，该 View 只是在长按后生成的带有加号的 View
//     * ```
//     * 可能你会觉得很奇葩，先写了一个自定义 ViewGroup，再把一个 ImageView 加到这个 ViewGroup 里面
//     * 原因如下：
//     * 1、在长按上下滑动时是一格一格的增加的，这里我又添加了一个增加时的生长动画，
//     *    为了方便处理，所以在外面再包了一层 ViewGroup。如果只用 ImageView，就会很难处理
//     *
//     * 里面直接使用 ImageView 的原因：
//     * 1、显示更方便，主要设置图片的 scaleType 这些，比直接在 canvas 上画图更简便
//     * ```
//     */
//    private val mTouchAffairView by lazyUnlock {
//        val imageView = ImageView(context).apply {
//            scaleType = ImageView.ScaleType.CENTER_INSIDE
//            background = GradientDrawable().apply {
//                val radius = R.dimen.course_course_item_radius.dimens()
//                // 设置圆角
//                cornerRadii =
//                    floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
//                // 背景颜色
//                setColor(R.color.course_affair_color.color())
//            }
//            // 设置 ImageView 的前景图片
//            setImageResource(R.drawable.course_ic_add_circle_white)
//        }
//
//        object : ViewGroup(context) {
//
//            init {
//                addView(imageView)
//                course.clipChildren = false // 请求父布局不要裁剪
//                layoutParams = CourseLayoutParams(0,0, 0, CourseType.AFFAIR_TOUCH)
//            }
//
//            // 扩展动画
//            private var mExpandValueAnimator: ValueAnimator? = null
//            // 下一次布局的回调
//            private var mOnNextLayoutCallback: ((View) -> Unit)? = null
//
//            /**
//             * 显示 mTouchAffairView（用于添加事务的 View）
//             */
//            fun show() {
//                val lp = layoutParams as CourseLayoutParams
//                lp.startRow = mTopRow
//                lp.endRow = mBottomRow
//                lp.startColumn = mInitialColumn
//                lp.endColumn = mInitialColumn
//                // 添加 mTouchAffairView
//                if (parent != null) {
//                    if (parent is CourseLayout) {
//                        layoutParams = lp
//                    } else {
//                        throw RuntimeException("为什么 mTouchAffairView 的父布局不是 CourseLayout?")
//                    }
//                } else {
//                    course.addView(this, lp)
//                }
//                // 添加一个入场动画
//                startAnimation(
//                    AlphaAnimation(0F, 1F).apply {
//                        duration = 200
//                    }
//                )
//                // 回调监听
//                mOnTouchAffairListener?.onCreateTouchAffair(this, lp)
//            }
//
//            /**
//             * 该方法作用：
//             * 1、计算当前 [mTouchAffairView] 的位置并刷新布局
//             * 2、启动一个生长的动画
//             */
//            fun refresh() {
//                val y = course.let {
//                    it.mCourseScrollView.mLastMoveY - it.getDistanceToScrollView()
//                }
//                mTouchRow = course.getRow(y) // 当前触摸的行数
//                var topRow: Int
//                var bottomRow: Int
//                // 根据当前触摸的行数与初始行数比较，得到 topRow、bottomRow
//                if (mTouchRow > mInitialRow) {
//                    topRow = mInitialRow
//                    bottomRow = mTouchRow
//                } else {
//                    topRow = mTouchRow
//                    bottomRow = mInitialRow
//                }
//                // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
//                unfoldNoonOrDuskIfNecessary(mTouchRow)
//                if (topRow < mUpperRow) topRow = mUpperRow // 根据上限再次修正 topRow
//                if (bottomRow > mLowerRow) bottomRow = mLowerRow // 根据下限再次修正 bottomRow
//                if (topRow != mTopRow || bottomRow != mBottomRow) { // 避免不必要的刷新
//                    val oldTopRow = mTopRow
//                    val oldBottomRow = mBottomRow
//                    mTopRow = topRow
//                    mBottomRow = bottomRow
//                    val lp = layoutParams as CourseLayoutParams
//                    lp.startRow = topRow
//                    lp.endRow = bottomRow
//                    layoutParams = lp // 设置属性，刷新布局
//                    mOnNextLayoutCallback = {
//                        // 在已经布局完毕后再调用动画，不然得到的高度值是有问题的
//                        startExpandValueAnimator(oldTopRow, oldBottomRow, topRow, bottomRow)
//                    }
//                }
//            }
//
//            /**
//             * 启动生长动画
//             */
//            private fun startExpandValueAnimator(
//                oldTopRow: Int,
//                oldBottomRow: Int,
//                topRow: Int,
//                bottomRow: Int
//            ) {
//                mExpandValueAnimator?.cancel() // 取消之前的动画
//                mExpandValueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
//                    addUpdateListener {
//                        val now = animatedValue as Float
//                        val oldTop = course.getRowsHeight(0, oldTopRow - 1)
//                        val newTop = course.getRowsHeight(0, topRow - 1)
//                        val nowTop = ((oldTop - newTop) * (1 - now)).toInt()
//                        val oldBottom = course.getRowsHeight(0, oldBottomRow)
//                        val newBottom = course.getRowsHeight(0, bottomRow)
//                        val nowBottom = ((newBottom - oldBottom) * now).toInt() + oldBottom - newTop
//                        imageView.layout(0, nowTop, width, nowBottom) // 手动调用布局
//                    }
//                    doOnEnd {
//                        mExpandValueAnimator = null
//                    }
//                    interpolator = DecelerateInterpolator()
//                    duration = 120
//                    start()
//                }
//            }
//
//            /**
//             * 这里并没有直接给 imageView 布局，而是在前面手动调用 layout
//             */
//            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//                mOnNextLayoutCallback?.invoke(this)
//                mOnNextLayoutCallback = null
//                if (mExpandValueAnimator == null) {
//                    imageView.layout(0, 0, r - l, b - t)
//                }
//            }
//        }
//    }
//
//    /**
//     * 判断下一次 Down 时点击的 View 是否需要让之前显示的 mTouchAffairView 消失，
//     * 如果又处于长按整体移动的动画，则也会在合适的时间取消长按整体移动的动画
//     */
//    private fun removeLastTouchAffairViewWhenNextDown(touchView: View?) {
//        if (mTouchAffairView.parent != null
//            && touchView !== mTouchAffairView
//            && !mEntityMoveHelper.isSubstituteView(touchView) // 注意这里要包括长按整体移动的替身 View
//        ) {
//            recoverFoldState(true)
//            // 此时说明点击的是其他地方，不是 mTouchAffairView，则 remove 掉 mTouchAffairView
//            if (mAlphaValueAnimator == null) {
//                mAlphaValueAnimator = ValueAnimator.ofFloat(1F, 0F).apply {
//                    addUpdateListener { mTouchAffairView.alpha = it.animatedValue as Float }
//                    doOnEnd {
//                        mAlphaValueAnimator = null
//                        when (mEntityMoveHelper.getLongPressState()) {
//                            // 如果都结束了长按移动的动画还没结束，就只能强制取消长按移动动画了
//                            ANIM_TRAVEL, ANIM_RESTORE -> mEntityMoveHelper.forceEnd()
//                            else -> {}
//                        }
//                        course.removeView(mTouchAffairView) // 结束时把 View 给删掉
//                        mTouchAffairView.alpha = 1F
//                    }
//                    doOnCancel {
//                        mAlphaValueAnimator = null
//                        when (mEntityMoveHelper.getLongPressState()) {
//                            // 如果该动画被取消时长按移动的动画还没结束，就只能强制取消长按移动动画了
//                            ANIM_TRAVEL, ANIM_RESTORE -> mEntityMoveHelper.forceEnd()
//                            else -> {}
//                        }
//                        // 被取消说明立马就得使用 mTouchAffairView，所以可以不用删除掉
//                        mTouchAffairView.alpha = 1F
//                    }
//                    start()
//                }
//            }
//        }
//    }
//
//    /**
//     * 如果你手指滑到屏幕显示边缘区域时，则可能需要调用 [CourseScrollView] 滚动
//     * ```
//     * 该方法作用：
//     * 1、计算当前触摸位置与 ScrollView 的距离来判断是否需要让 ScrollView 滚动
//     * 2、计算让 ScrollView 滚动的速度
//     * ```
//     */
//    private fun scrollIsNecessary() {
//        mScrollRunnable.startIfCan()
//    }
//
//    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
//    private val mScrollRunnable = object : Runnable {
//
//        var isInScrolling = false // 是否处于滚动状态
//            private set
//
//        private var velocity = 0 // 滚动的速度
//
//        override fun run() {
//            if (isAllowScrollAndCalculateVelocity()) {
//                course.mCourseScrollView.scrollBy(0, velocity)
//                course.invalidate()
//                ViewCompat.postOnAnimation(course, this)
//            } else {
//                isInScrolling = false
//            }
//        }
//
//        /**
//         * 如果能开启滚动的话就开启滚动，注意：滚动是会自己取消的
//         */
//        fun startIfCan() {
//            if (!isInScrolling) { // 防止重复允许 Runnable
//                isInScrolling = true
//                run()
//            }
//        }
//
//        /**
//         * 取消滚动
//         */
//        fun cancel() {
//            if (isInScrolling) {
//                isInScrolling = false
//                course.removeCallbacks(this)
//            }
//        }
//
//        /**
//         * 是否允许滚动，如果允许，则计算滚动速度给 [velocity] 变量
//         */
//        private fun isAllowScrollAndCalculateVelocity(): Boolean {
//            val scroll = course.mCourseScrollView
//            val nowHeight = course.mCourseScrollView.mLastMoveY
//            val moveBoundary = 100 // 移动的边界值
//            // 向上滚动，即手指移到底部，需要显示下面的内容
//            val isNeedScrollUp =
//                nowHeight > scroll.height - moveBoundary
//                        && mTouchRow <= mLowerRow // 当前触摸的行数在下限以上
//                        && scroll.height + scroll.scrollY != scroll.getChildAt(0).height // 是否滑到底
//
//            // 向下滚动，即手指移到顶部，需要显示上面的内容
//            val isNeedScrollDown =
//                nowHeight < moveBoundary
//                        && mTouchRow >= mUpperRow // 当前触摸的行数在上限以下
//                        && scroll.scrollY != 0 // 是否滑到顶
//            val isAllowScroll = isNeedScrollUp || isNeedScrollDown
//            if (isAllowScroll) {
//                velocity = if (isNeedScrollUp) {
//                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
//                    min((nowHeight - (scroll.height - moveBoundary)) / 10 + 6, 20)
//                } else {
//                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
//                    -min(((moveBoundary - nowHeight) / 10 + 6), 20)
//                }
//            }
//            return isAllowScroll
//        }
//    }
//
//    /**
//     * 判断当前滑动中是否需要自动展开中午或者傍晚时间段
//     * @param touchRow 当前触摸的行数
//     */
//    private fun unfoldNoonOrDuskIfNecessary(touchRow: Int) {
//        if (mInitialRow < NOON_TOP && mLowerRow >= NOON_TOP - 1 && touchRow >= NOON_TOP
//            || mInitialRow > NOON_BOTTOM && mUpperRow <= NOON_BOTTOM + 1 && touchRow <= NOON_BOTTOM
//            || touchRow in NOON_TOP..NOON_BOTTOM
//        ) {
//            when (course.getNoonRowState()) {
//                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldNoonForce()
//                else -> {}
//            }
//        }
//        if (mInitialRow < DUSK_TOP && mLowerRow >= DUSK_TOP - 1 && touchRow >= DUSK_TOP
//            || mInitialRow > DUSK_BOTTOM && mUpperRow <= DUSK_BOTTOM + 1 && touchRow <= NOON_BOTTOM
//            || touchRow in DUSK_TOP..DUSK_BOTTOM
//        ) {
//            when (course.getDuskRowState()) {
//                RowState.FOLD, RowState.FOLD_ANIM -> course.unfoldDuskForce()
//                else -> {}
//            }
//        }
//    }
//
//    /**
//     * 用于恢复折叠状态
//     *
//     * @param isIgnoreLp 是否忽略 layoutParams，直接还原到开始的状态
//     */
//    private fun recoverFoldState(isIgnoreLp: Boolean) {
//        var hasViewInNoon = false
//        var hasViewInDusk = false
//        for (i in 0 until course.childCount) {
//            // 判断中午和傍晚时间段是否存在 View
//            val child = course.getChildAt(i)
//            if (child === mTouchAffairView) continue
//            val lp = child.layoutParams as CourseLayoutParams
//            when (lp.type) {
//                // 目前只知道时间轴上的那几个类型不算
//                CourseType.TIME, CourseType.ARROW_NOON, CourseType.ARROW_DUSK -> {}
//                else -> {
//                    if (!hasViewInNoon && CourseLayout.isContainNoon(lp)) {
//                        hasViewInNoon = true
//                    }
//                    if (!hasViewInDusk && CourseLayout.isContainDusk(lp)) {
//                        hasViewInDusk = true
//                    }
//                }
//            }
//            if (hasViewInNoon && hasViewInDusk) break
//        }
//        val lp = mTouchAffairView.layoutParams as CourseLayoutParams
//        if (mIsNoonFoldedWhenLongPress && (!CourseLayout.isContainNoon(lp) || isIgnoreLp) && !hasViewInNoon) {
//            when (course.getNoonRowState()) {
//                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldNoonForce()
//                else -> {}
//            }
//        }
//        if (mIsDuskFoldedWhenLongPress && (!CourseLayout.isContainDusk(lp) || isIgnoreLp) && !hasViewInDusk) {
//            when (course.getDuskRowState()) {
//                RowState.UNFOLD, RowState.UNFOLD_ANIM -> course.foldDuskForce()
//                else -> {}
//            }
//        }
//    }
//
//    override fun onDrawAbove(canvas: Canvas, course: CourseLayout) {
//        /*
//        * 在每次刷新时修改 mTouchAffairView 的位置
//        * */
//        if (mTouchAffairView.parent != null && !mIsInEntityMove && mIsInLongPress) {
//            mTouchAffairView.refresh()
//        }
//    }
//
//    override fun onSaveInstanceState(): Bundle? {
//        // mTouchAffairView 没有被添加时不用保存信息
//        if (mTouchAffairView.parent == null) return null
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
//            course.mCourseScrollView.scrollY =
//                mTouchAffairView.top + course.getDistanceToScrollView() - 60
//        }
//    }
//
//    interface OnTouchAffairListener {
//        /**
//         * 创建 TouchAffairView 的监听
//         *
//         * 在 addView 之前回调，可以直接设置一些属性
//         */
//        fun onCreateTouchAffair(view: View, lp: CourseLayoutParams)
//    }
//
//    companion object {
//        /**
//         * 换成一个静态方法来 attach 到 [CourseLayout]，
//         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的
//         *
//         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
//         */
//        fun attach(course: CourseLayout): CourseCreateAffairHelper {
//            return CourseCreateAffairHelper(course).apply {
//                course.addCourseTouchListener(this) // 给 CourseLayout 设置触摸监听
//                course.addCourseDecoration(this) // 给 CourseLayout 设置绘图监听
//                course.addSaveBundleListener(this) // 设置 course 被摧毁重建的监听，主要是保存一些重要信息
//            }
//        }
//    }
//}