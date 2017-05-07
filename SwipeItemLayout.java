   
    import android.content.Context;
    import android.support.annotation.IntRange;
    import android.support.annotation.NonNull;
    import android.support.annotation.Nullable;
    import android.support.v4.view.ViewCompat;
    import android.support.v4.widget.ViewDragHelper;
    import android.util.AttributeSet;
    import android.view.MotionEvent;
    import android.view.View;
    import android.view.ViewConfiguration;
    import android.view.ViewGroup;
    import android.widget.FrameLayout;
    
    import com.python.cat.animatorbutton.BuildConfig;
    
    import java.util.ArrayList;
    import java.util.List;
    
    import static com.python.cat.animatebutton.view.SwipeItemLayout.State.Center;
    import static com.python.cat.animatebutton.view.SwipeItemLayout.State.Right;
    
    
    /**
     * packageName: com.python.cat.animatebutton.view
     * Created on 2017/5/6.
     * 右边有菜单的item
     *
     * @author cat
     */
    public class SwipeItemLayout extends FrameLayout {
    
        @SuppressWarnings("FieldCanBeLocal")
        private int mHeight;
        private int mWidth;
        private ViewDragHelper mDragHelper;
        private List<ItemPos> mItemSet;
        private OnOpenStatedListener mViewStateListener;
        private DragCallback mDragCallBack;
    
        public SwipeItemLayout(Context context) {
            this(context, null);
        }
    
        public SwipeItemLayout(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }
    
        public SwipeItemLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
    //        setOrientation(HORIZONTAL); // 验证说明ok
            initDragHelper();
        }
    
        private void initDragHelper() {
            mDragCallBack = new DragCallback();
            mDragHelper = ViewDragHelper.create(this, 1, mDragCallBack);
        }
    
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //        LogUtils.w("on measure....");
        }
    
        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
    //        LogUtils.w("on finish inflate....");
        }
    
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            this.mWidth = w;
            this.mHeight = h;
            int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    //        LogUtils.i("touchSlop =  %d", touchSlop);
        }
    
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    //        super.onLayout(changed, left, top, right, bottom);
            layoutChildren(0, 0);
    //        LogUtils.w("on layout.........");
        }
    
        private void layoutChildren(int dx, int dy) {
            mItemSet = new ArrayList<>();
            mItemSet.clear();
            int childCount = getChildCount();
            int cl, ct, cr, cb;
            int tempX = 0;
            boolean isFirstView;
            for (int i = 0; i < childCount; i++) {
                isFirstView = i == 0;
                View child = getChildAt(i);
                MarginLayoutParams params = getChildLayoutParams(child);
                cl = tempX + params.leftMargin + dx;
                ct = params.topMargin + get().getPaddingTop() + dy;
                if (isFirstView) {
                    // first view
                    cl += get().getPaddingLeft();
                }
                cr = cl + getChildWidth(child);
                cb = ct + getChildHeight(child);
                tempX += cr + params.rightMargin;
                //noinspection unused
                int bX = getChildWidth(child)
                        + params.leftMargin + params.rightMargin
                        + getPaddingLeft() + getPaddingRight();
    
                // 当子View完全填充时， ax == bx
    //            LogUtils.w("index ---ax= " + aX + " , bx=" + bX);
    //            LogUtils.w("index = %d, left= %d, top= %d, right=%d, bottom=%d", cl, ct, cr, cb);
                child.layout(cl, ct, cr, cb);
                ItemPos it = new ItemPos(i, cl, ct, cr, cb);
                mItemSet.add(it);
            }
        }
    
    
        private class ItemPos {
            int index;
            int left;
            int top;
            int right;
            int bottom;
    
            ItemPos(int index, int left, int top, int right, int bottom) {
                this.index = index;
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
            }
        }
    
        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
    
            return mDragHelper.shouldInterceptTouchEvent(event);
        }
    
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mDragHelper.processTouchEvent(event);
            return true;
        }
    
        private class DragCallback extends ViewDragHelper.Callback {
    
            State mCurrentOpen = Center; // default is center
            ScrollState scrollState = new ScrollState();
            @IntRange(from = 0, to = 2)
            private int mDragState = ViewDragHelper.STATE_IDLE; // default is idle
    
            class ScrollState {
                static final int LEFT = -1;
                static final int RIGHT = 1;
                int scrollDirection; // Left or Right
    
                @Override
                public String toString() {
                    return "ScrollState{" +
                            "scrollDirection=" + scrollDirection +
                            '}';
                }
            }
    
            @Override
            public int getViewHorizontalDragRange(View child) {
                int dx = getMeasuredWidth() - child.getMeasuredWidth();
    //            LogUtils.w("dx = " + dx);
                return dx;
            }
    
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }
    
            /**
             * @param left 父容器的左侧，到child的左侧的距离
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
    //            LogUtils.w("clampH : " + left + " , " + dx + " , " + child);
                getParent().requestDisallowInterceptTouchEvent(true); // 不给外层布局上下滑动
                View childAt1 = getChildAt(1);
                // 滑动第一个view
                if (child == getChildAt(0)) {
                    // left  [getPaddingLeft() + getChildLayoutParams(child).leftMargin,
                    int maxL = getPaddingLeft() + getChildLayoutParams(child).leftMargin;
    
                    if (left > maxL) {
                        // 向右 ok
                        left = maxL;
                    } else {
                        // 向左 ok
                        int min = mWidth - getChildWidthWithMargin(child) - getChildWidthWithMargin(childAt1);
                        if (left < min) {
                            left = min;
                        }
                    }
                }
                // 滑动第二个view
                else if (child == getChildAt(1)) {
                    int max = mItemSet.get(1).left;
                    int min = mWidth - getChildWidthWithMargin(child) - getPaddingRight();
                    if (left > max) {
                        // 向右 ok
                        left = max;
                    } else if (left < min) {
                        // 向左 ok
                        left = min;
                    }
                }
                if (dx > 0) {
                    scrollState.scrollDirection = ScrollState.RIGHT;
                } else if (dx < 0) {
                    scrollState.scrollDirection = ScrollState.LEFT;
                }
                return left;
            }
    
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
    //            LogUtils.w("onViewPositionChanged : " + left + " , " + top + " , " + dx + " , " + dy);
                View childAt0 = getChildAt(0);
                View childAt1 = getChildAt(1);
                if (changedView == childAt0) {
                    ItemPos it = mItemSet.get(1);
                    int moveX = left - mItemSet.get(0).left;
                    childAt1.layout(it.left + moveX, it.top, it.right + moveX, it.bottom + dy);
                } else if (changedView == childAt1) {
                    ItemPos it = mItemSet.get(0);
                    int moveX = left - mItemSet.get(1).left;
                    childAt0.layout(it.left + moveX, it.top, it.right + moveX, it.bottom + dy);
                }
            }
    
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                // 继续保持margin与padding
                return getPaddingTop() + getChildLayoutParams(child).topMargin;
            }
    
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
                this.mDragState = state;
    //            LogUtils.e("ViewDragState == " + state);
            }
    
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
    
                View childAt0 = getChildAt(0);
                View childAt1 = getChildAt(1);
    
                int a = getPaddingLeft();
                int d = getChildLayoutParams(childAt0).leftMargin;
                int L = getChildAt(0).getLeft();
                int k = getChildWidth(childAt1);
                if (BuildConfig.DEBUG) {
                    int b = getWidth();
                    int c = getPaddingRight();
                    int e = getChildLayoutParams(childAt0).rightMargin;
                    int f = getChildWidthWithMargin(childAt0);
                    int g = getChildWidth(childAt0);
                    int h = getChildLayoutParams(childAt1).leftMargin;
                    int i = getChildLayoutParams(childAt1).rightMargin;
                    int j = getChildWidthWithMargin(childAt1);
    //                LogUtils.e("release: " + a + " , " + b + " , "
    //                        + c + " , " + d + " , " + e + " , "
    //                        + f + " , " + g + " , " + h + " , "
    //                        + i + " , " + j + " , " + k + " , "
    //                        + L);
    //                // L == -34 , d==e==26 , a==c==80 , ---> k/2 == 140
    //                LogUtils.e("L-d-a = " + (L - d - a) + " k/2 = " + k / 2); // 中点真难找啊..
    
                }
                State temp = this.mCurrentOpen;
    //            LogUtils.e(scrollState);
                int left = L - d - a;
                int change; // 释放临界值：不要放中间，放1/4处就好了
                int absLeft = Math.abs(left);
                if (scrollState.scrollDirection == ScrollState.LEFT) {
                    // 左滑
                    change = k / 4;
                    if (absLeft > change) {
                        // 左滑到底
                        openRight(childAt0, childAt1);
    
                    } else {
                        // 右滑到底
                        openCenter(childAt0, childAt1);
    
                    }
    //                LogUtils.e("left = " + left + " absLeft = " + absLeft + " change = " + change);
                } else if (scrollState.scrollDirection == ScrollState.RIGHT) {
                    // 右滑
                    change = k * 3 / 4;
                    if (absLeft > change) {
                        // 左滑到底
                        openRight(childAt0, childAt1);
                        this.mCurrentOpen = Right;
                    } else {
                        // 右滑到底
                        openCenter(childAt0, childAt1);
                        this.mCurrentOpen = Center;
                    }
    //                LogUtils.e("left = " + left + " absLeft = " + absLeft + " change = " + change);
                }
    
    //            LogUtils.e("xv = " + xvel + " , yv = " + yvel);
    //            xvel<0 -->右滑  >0 左滑  | 缓慢滑就 == 0
    
    //          LogUtils.e("temp = " + temp + " , state = " + mCurrentOpen);
                if (mViewStateListener != null) {
                    mViewStateListener.onViewOpen(temp != this.mCurrentOpen, this.mCurrentOpen);
                }
                invalidate();
            }
    
            // ####################################
            private void openCenter(View childAt0, View childAt1) {
                mDragHelper.smoothSlideViewTo(childAt0, mItemSet.get(0).left, mItemSet.get(0).top);
                mDragHelper.smoothSlideViewTo(childAt1, mItemSet.get(1).left, mItemSet.get(1).top);
                this.mCurrentOpen = Center;
            }
    
            private void openRight(View childAt0, View childAt1) {
                mDragHelper.smoothSlideViewTo(childAt0,
                        mItemSet.get(0).left - getChildWidthWithMargin(childAt1),
                        mItemSet.get(0).top);
                mDragHelper.smoothSlideViewTo(childAt1,
                        mItemSet.get(1).left - getChildWidthWithMargin(childAt1),
                        mItemSet.get(1).top);
    
                this.mCurrentOpen = Right;
            }
        }
    
        @Override
        public void computeScroll() {
            if (mDragHelper.continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    
        public void setOnViewStatedChangedListener(OnOpenStatedListener listener) {
            this.mViewStateListener = listener;
        }
    
        public interface OnOpenStatedListener {
            @SuppressWarnings("unused")
            void onViewOpen(boolean changed, State state);
        }
    
        @SuppressWarnings("unused")
        public enum State {
            Left, Center, Right,
        }
    
        @SuppressWarnings("unused")
        public State getOpenState() {
            return mDragCallBack.mCurrentOpen;
        }
    
        @SuppressWarnings("unused")
        public int getDragState() {
            return mDragCallBack.mDragState;
        }
    
        public State openRight() {
            mDragCallBack.openRight(getChildAt(0), getChildAt(1));
            return mDragCallBack.mCurrentOpen;
        }
    
        public State openCenter() {
            mDragCallBack.openCenter(getChildAt(0), getChildAt(1));
            return mDragCallBack.mCurrentOpen;
        }
    
        @SuppressWarnings("unused")
        public State changeState() {
            switch (getOpenState()) {
                case Center:
                    openRight();
                    break;
                case Right:
                    openCenter();
                    break;
            }
            return mDragCallBack.mCurrentOpen;
        }
    
        // ######################---##########################
        private MarginLayoutParams getChildLayoutParams(@NonNull View child) {
            ViewGroup.LayoutParams pa = child.getLayoutParams();
            if (pa instanceof MarginLayoutParams) {
                return (MarginLayoutParams) pa;
            } else {
                throw new RuntimeException(pa == null ? null : pa.toString());
            }
        }
    
        private int getChildWidth(@NonNull View child) {
            return child.getMeasuredWidth();
        }
    
        private int getChildHeight(@NonNull View child) {
            return child.getMeasuredHeight();
        }
    
        private int getChildWidthWithMargin(@NonNull View child) {
            MarginLayoutParams params = getChildLayoutParams(child);
            return getChildWidth(child) + params.leftMargin + params.rightMargin;
        }
    
        @SuppressWarnings("unused")
        private int getChildHeightWithMargin(@NonNull View child) {
            MarginLayoutParams params = getChildLayoutParams(child);
            return getChildWidth(child) + params.topMargin + params.bottomMargin;
        }
    
        private ViewGroup get() {
            return this;
        }
    }
