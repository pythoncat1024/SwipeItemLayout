package com.pythoncat.itemscroll.view;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.apkfuns.logutils.LogUtils;

/**
 * Created by pythoncat on 2017/5/11.
 */

public class MenuLayout extends FrameLayout {


    private View mLeftView;
    private View mRightView;
    private View mCenterView;
    private boolean hasLeft;
    private boolean hasRight;
    private int downX;
    private int downY;
    private LayoutParams centerP;
    private Point mCenterPonit;
    private int mLeftWidth;
    private int mLeftHeight;
    private int mRightWidth;
    private int mRightHeight;
    private int downLeft;

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = this.getChildCount();
        if (count < 2 || count > 3) {
            throw new RuntimeException("你麻痹，你就是一个畜生。你配用我这个控件？");
        }
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            switch (getLayoutParams(v).gravity) {
                case Gravity.START:
                case Gravity.LEFT:
                    this.mLeftView = v;
                    break;
                case Gravity.END:
                case Gravity.RIGHT:
                    this.mRightView = v;
                    break;
                case Gravity.CENTER:
                default:
                    this.mCenterView = v;
                    break;
            }
        }
        this.hasLeft = mLeftView != null;
        this.hasRight = mRightView != null;
        if (mCenterView == null) {
            throw new RuntimeException("你是畜生吗？你是不是傻逼啊？");
        }
        LogUtils.e("mCenter = " + mCenterView);
        LogUtils.e("mLeftView = " + mLeftView + " \\\\ " + hasLeft);
        LogUtils.e("mRightView = " + mRightView + " \\\\ " + hasRight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerP = getLayoutParams(mCenterView);
        mCenterPonit = new Point();
        if (hasLeft) {
            mLeftWidth = mLeftView.getMeasuredWidth();
            mLeftHeight = mLeftView.getMeasuredHeight();
        }
        if (hasRight) {
            mRightWidth = mRightView.getMeasuredWidth();
            mRightHeight = mRightView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int x = mCenterView.getLeft();
        int y = mCenterView.getTop();
        mCenterPonit.x = x;
        mCenterPonit.y = y;
        LogUtils.e("mCenterP--- " + mCenterPonit);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                downLeft = mCenterView.getLeft();
                break;
            case MotionEvent.ACTION_MOVE:

                int moveX = (int) event.getX();
                int moveY = (int) event.getY();
                int dx = moveX - downX;
                int dy = moveY - downY;
//                LogUtils.e("xx --- " + getScrollX() + " .. " + mCenterView.getScrollX() + " ... " + dx + " ## " + mCenterView.getLeft());
                if (hasLeft) {
                    int width = getLayoutParams(mLeftView).width;
                    LogUtils.e("yy ==== " + mCenterView.getLeft() + " ||||| " + width);
                    LayoutParams params = getLayoutParams(mLeftView);
                    if (mCenterView.getLeft() + dx > params.width + params.leftMargin + params.rightMargin) {
                        dx = 0;
                    }
                } else {
                    if (mCenterView.getLeft() + dx > centerP.leftMargin) {
                        dx = 0;
                    }
                }
                if (hasRight) {
                    LayoutParams params = getLayoutParams(mRightView);
//                    LogUtils.e("zzz ====== " + -(params.width + params.leftMargin + params.rightMargin));
                    if (mCenterView.getLeft() + dx < -(params.width
                            + params.leftMargin + params.rightMargin
                            - centerP.leftMargin
                            - centerP.rightMargin)) {
                        dx = 0;
                    }
                } else {
                    if (mCenterView.getLeft() + dx < centerP.leftMargin) {
                        dx = 0;
                    }
                }
                mCenterView.offsetLeftAndRight(dx);
                mLeftView.setPivotX(mLeftView.getLeft());
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                // 放手后的回归
                if (hasLeft && hasRight) {
                    if (mCenterView.getLeft() > mCenterPonit.x) {
                        // 准备显示左边的 view（可能显示不成功）
                        fastShowLeft();
                    } else {
                        // 准备显示右边的 view （可能显示不成功）
                        fastShowRight();
                    }
                } else if (hasLeft) {
                    // 准备显示左边的 view（可能显示不成功）
                    fastShowLeft();
                } else if (hasRight) {
                    // 准备显示右边的 view （可能显示不成功）
                    fastShowRight();
                }

                break;
        }
        return true;
    }

    void fastShowLeft() {
        LayoutParams leftP = getLayoutParams(mLeftView);
        int halfLeft = (mLeftView.getMeasuredWidth() + leftP.rightMargin) / 2;
        int b = mCenterPonit.y;
        int d = b + mCenterView.getMeasuredHeight();
        if (mCenterView.getLeft() < halfLeft) {
            int a = mCenterPonit.x;
            int c = a + mCenterView.getMeasuredWidth();
            LogUtils.e("--- " + a + " , " + b + " , " + c + " , " + d);
            mCenterView.layout(a, b, c, d); // 回到 centerView 在中间完全显示的状态
        } else {
            int a = mCenterPonit.x + halfLeft * 2;
            int c = a + mCenterView.getMeasuredWidth();
            mCenterView.layout(a, b, c, d); // 完全显示 leftView。centerView在leftView 右边
        }
    }

    void fastShowRight() {
        LayoutParams rightP = getLayoutParams(mRightView);
        int b = mCenterPonit.y;
        int d = b + mCenterView.getMeasuredHeight();
        int half = (int) (-0.5 * (mRightView.getMeasuredWidth() + rightP.leftMargin));
        LogUtils.e("half === " + half + " ..... " + mCenterView.getLeft());
        if (mCenterView.getLeft() > half) {
            int a = mCenterPonit.x;
            int c = a + mCenterView.getMeasuredWidth();
            LogUtils.e("--- " + a + " , " + b + " , " + c + " , " + d);
            mCenterView.layout(a, b, c, d); // 回到 centerView 在中间完全显示的状态
        } else {
            int a = mCenterPonit.x + half * 2;
            int c = a + mCenterView.getMeasuredWidth();
            mCenterView.layout(a, b, c, d); // 完全显示右边的View
        }
    }


    private LayoutParams getLayoutParams(@NonNull View child) {
        return (LayoutParams) child.getLayoutParams();
    }

}
