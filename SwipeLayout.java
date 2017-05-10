package com.python.cat.animatebutton.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.apkfuns.logutils.LogUtils;
import com.python.cat.animatebutton.R;


/**
 * packageName: com.python.cat.animatebutton.view
 * Created on 2017/5/6.
 * 左右有菜单的item
 *
 * @author cat
 */
public class SwipeLayout extends FrameLayout {
    static {
        LogUtils.getLogConfig().configShowBorders(false);
    }

    private static final int SHOW_LEFT = 0;
    private static final int SHOW_CENTER = 1;
    private static final int SHOW_RIGHT = 2;
    private int downX;
    private int mWidth;
    private View leftMenu;
    private View centerView;
    private View rightMenu;
    private MarginLayoutParams centerParams;
    private MarginLayoutParams leftParams;
    private MarginLayoutParams rightParams;
    private Point initSlide;

    @IntDef({SHOW_LEFT, SHOW_CENTER, SHOW_RIGHT})
    public @interface ShowStyle {
    }

    @ShowStyle
    private int showStyle;

    @ShowStyle
    public int getShowStyle() {
        return showStyle;
    }

    public SwipeLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeLayout(@NonNull Context context,
                       @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(@NonNull Context context,
                       @Nullable AttributeSet attrs,
                       @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SwipeLayout, defStyleAttr, 0);
        //noinspection WrongConstant
        showStyle = a.getInt(R.styleable.SwipeLayout_showStyle, SHOW_CENTER);
        LogUtils.e("showStyle = " + showStyle);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        LogUtils.e("mw = " + mWidth);
        LogUtils.e("mwXXX = " + getPaddingLeft() + getPaddingRight() + getChildWidthWithMargin(centerView));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        leftMenu = getChildAt(0);
        centerView = getChildAt(1);
        rightMenu = getChildAt(2);
        centerParams = getChildLayoutParams(centerView);
        leftParams = getChildLayoutParams(leftMenu);
        rightParams = getChildLayoutParams(rightMenu);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int w = view.getMeasuredWidth();
            int h = view.getMeasuredHeight();
            MarginLayoutParams params = getChildLayoutParams(view);
            LogUtils.e("index = %d , w = %d , h = %d " +
                            ",leftMargin = %d , rightMargin = %d " +
                            ", topMargin = %d , bottomMargin = %d", i, w, h,
                    params.leftMargin, params.rightMargin,
                    params.topMargin, params.bottomMargin);
        }
        if (showStyle == SHOW_CENTER) {
            initSlide = initCenterLayout(leftMenu, centerView, rightMenu, centerParams, leftParams, rightParams);

        } else if (showStyle == SHOW_LEFT) {
            initSlide = initLeftLayout(leftMenu, centerView, rightMenu, centerParams, leftParams, rightParams);

        } else if (showStyle == SHOW_RIGHT) {
            initSlide = initRightLayout(leftMenu, centerView, rightMenu, centerParams, leftParams, rightParams);
        } else {
            throw new RuntimeException("current showStyle is unknown... " + showStyle);
        }
        LogUtils.e("showStyle = " + showStyle + " ## " + initSlide);
        LogUtils.e("right--- " + getChildWidthWithMargin(rightMenu)
                + " ... " + rightMenu.getMeasuredWidth());
        // showStyle = center : ## Point(-348, 0) ## Point(882, 0)
        // showStyle = left : ## Point(12, 0) ## Point(1242, 0)
        // showStyle = right : ## Point(-708, 0) ## Point(522, 0)
//
        //// FIXME: 2017/5/10 -----> super 用来测试的
//        super.onLayout(changed, left, top, right, bottom);
    }

    private Point initRightLayout(View leftMenu, View center, View rightMenu,
                                  MarginLayoutParams centerParams,
                                  MarginLayoutParams leftParams,
                                  MarginLayoutParams rightParams) {
        // -------
        int rightRight = mWidth - getPaddingEnd() - rightParams.rightMargin;
        int rightLeft = rightRight - rightMenu.getMeasuredWidth();
        int rightTop = getPaddingTop() + rightParams.topMargin;
        int rightBottom = computeBottomFromTop(rightTop, rightMenu);
        rightMenu.layout(rightLeft, rightTop, rightRight, rightBottom);
        // -------
        int centerRight = rightLeft - rightParams.leftMargin - centerParams.rightMargin;
        int centerLeft = centerRight - center.getMeasuredWidth();
        int centerTop = getPaddingTop() + centerParams.topMargin;
        int centerBottom = computeBottomFromTop(centerTop, center);
        center.layout(centerLeft, centerTop, centerRight, centerBottom);
        // -------
        int leftRight = centerLeft - centerParams.leftMargin - leftParams.rightMargin;
        int leftLeft = leftRight - leftMenu.getMeasuredWidth();
        int leftTop = getPaddingTop() + leftParams.topMargin;
        int leftBottom = computeBottomFromTop(leftTop, leftMenu);
        leftMenu.layout(leftLeft, leftTop, leftRight, leftBottom);
        return new Point(leftLeft, leftTop);
    }

    private Point initLeftLayout(View leftMenu, View center, View rightMenu,
                                 MarginLayoutParams centerParams,
                                 MarginLayoutParams leftParams,
                                 MarginLayoutParams rightParams) {
        // ------
        int leftLeft = getPaddingStart() + leftParams.leftMargin;
        int leftRight = computeRightFromLeft(leftLeft, leftMenu);
        int leftTop = getPaddingTop() + leftParams.topMargin;
        int leftBottom = computeBottomFromTop(leftTop, leftMenu);
        leftMenu.layout(leftLeft, leftTop, leftRight, leftBottom);
        // ------
        int centerLeft = leftRight + centerParams.leftMargin + leftParams.rightMargin;
        int centerTop = getPaddingTop() + centerParams.topMargin;
        int centerRight = computeRightFromLeft(centerLeft, center);
        int centerBottom = computeBottomFromTop(centerTop, center);
        center.layout(centerLeft, centerTop, centerRight, centerBottom);
        // -------
        int rightLeft = centerRight + rightParams.leftMargin + centerParams.rightMargin;
        int rightRight = computeRightFromLeft(rightLeft, rightMenu);
        int rightTop = getPaddingTop() + rightParams.topMargin;
        int rightBottom = computeBottomFromTop(rightTop, rightMenu);
        rightMenu.layout(rightLeft, rightTop, rightRight, rightBottom);
        return new Point(leftLeft, leftTop);
    }

    private Point initCenterLayout(View leftMenu, View center, View rightMenu,
                                   MarginLayoutParams centerParams,
                                   MarginLayoutParams leftParams,
                                   MarginLayoutParams rightParams) {
        // -----
        int centerLeft = getPaddingStart() + centerParams.leftMargin;
        int centerTop = getPaddingTop() + centerParams.topMargin;
        int centerRight = computeRightFromLeft(centerLeft, center);
        int centerBottom = computeBottomFromTop(centerTop, center);
        center.layout(centerLeft, centerTop, centerRight, centerBottom);
        // ------
        int leftRight = getPaddingStart() - leftParams.rightMargin;
        int leftLeft = leftRight - leftMenu.getMeasuredWidth();
        int leftTop = getPaddingTop() + leftParams.topMargin;
        int leftBottom = computeBottomFromTop(leftTop, leftMenu);
        leftMenu.layout(leftLeft, leftTop, leftRight, leftBottom);
        // -----
        int rightLeft = centerRight + centerParams.rightMargin + rightParams.leftMargin;
        int rightRight = computeRightFromLeft(rightLeft, rightMenu);
        int rightTop = getPaddingTop() + rightParams.topMargin;
        int rightBottom = computeBottomFromTop(leftTop, leftMenu);
        rightMenu.layout(rightLeft, rightTop, rightRight, rightBottom);
        return new Point(leftLeft, leftTop);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = Math.round(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = Math.round(event.getX());
                int dx = moveX - downX;

                if (!isLeftSlide(dx) && !isRightSlide(dx)) {
                    scrollBy(-dx, 0);
                }
                downX = Math.round(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    /**
     * 是否是左边界
     *
     * @param dx 触摸x方向的差值
     * @return true or false
     */
    private boolean isLeftSlide(int dx) {
        // showStyle = 0 ## Point(22, 0)
        // showStyle = 1 ## Point(-258, 0)
        // showStyle = 2 ## Point(-516, 0)
        // 258 ... 240
        LogUtils.e("" + getScrollX() + " .. " + (getScrollX() - dx));
        if (getScrollX() - dx < initSlide.x - getPaddingLeft() - leftParams.leftMargin) {
            openLeft();
            return true;
        }
        return false;
    }

    /**
     * 是否是右边界
     *
     * @param dx 触摸x方向的差值
     * @return true or false
     */
    private boolean isRightSlide(int dx) {

        if (getScrollX() - dx > +initSlide.x
                // initSlide.x 里面已经包含了对 child0的leftMargin的计算了
                + leftParams.rightMargin
                + mWidth
                + rightParams.leftMargin
                + rightParams.rightMargin) {
            openRight();
            return true;
        }
        return false;
    }

    public void openLeft() {
        scrollTo(initSlide.x - getPaddingLeft() - leftParams.leftMargin, initSlide.y);
        this.showStyle = SHOW_LEFT;
    }

    public void openRight() {
        scrollTo(+initSlide.x
                // initSlide.x 里面已经包含了对 child0的leftMargin的计算了
                + leftParams.rightMargin
                + mWidth
                + rightParams.leftMargin
                + rightParams.rightMargin, initSlide.y);
        this.showStyle = SHOW_RIGHT;
    }

    public void openCenter() {
        scrollTo(initSlide.x - getPaddingLeft() - leftParams.leftMargin
                        + getChildWidthWithMargin(leftMenu)
                , initSlide.y);
        this.showStyle = SHOW_CENTER;
    }


    // ###############----###################
    private MarginLayoutParams getChildLayoutParams(@NonNull View child) {
        ViewGroup.LayoutParams pa = child.getLayoutParams();
        if (pa instanceof MarginLayoutParams) {
            return (MarginLayoutParams) pa;
        } else {
            throw new RuntimeException(pa == null ? null : pa.toString());
        }
    }

    private int getChildWidthWithMargin(@NonNull View child) {
        MarginLayoutParams params = getChildLayoutParams(child);
        return child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
    }

    private int computeRightFromLeft(int left, @NonNull View target) {
        return left + target.getMeasuredWidth();
    }

    private int computeBottomFromTop(int top, @NonNull View target) {
        return top + target.getMeasuredHeight();
    }
}
