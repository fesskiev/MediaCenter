package com.fesskiev.mediacenter.widgets.layouts;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fesskiev.mediacenter.R;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DragFrameLayout extends FrameLayout {

    private static final String TAG = DragFrameLayout.class.getSimpleName();

    private List<View> views;

    private float dragDistance = 800;
    private float dragScale = 0.7f;
    private float totalDrag;

    private boolean draggingDown = false;
    private boolean draggingUp = false;

    public DragFrameLayout(Context context) {
        this(context, null, 0, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    int x;
    int y;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();

        x = getMeasuredWidth();
        y = getMeasuredHeight() / 2;
    }

    private void init() {
        views = getAllChild(this);
        Log.d(TAG, "child size " + views.size());
        for (View view : views) {
            Log.d(TAG, "child: " + view.toString());
        }
    }

    private List<View> getAllChild(View v) {

        if (!(v instanceof ViewGroup)) {
            List<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        List<View> result = new ArrayList<>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            List<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChild(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
//        Log.d(TAG, "onNestedPreScroll:x: " + dx + " y: " + dy + " cons: " + Arrays.toString(consumed));
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(dy);
            consumed[1] = dy;
        }
    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        Log.d(TAG, "onNestedScroll:x: " + dxConsumed + " y: " + dyConsumed + " dxUn: " + dxUnconsumed
//                + " dyUnc: " + dyUnconsumed);
        dragScale(dyUnconsumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
//        Log.d(TAG, "onStopNestedScroll");
    }

    private void dragScale(int scroll) {
        if (scroll == 0) {
            return;
        }

        totalDrag += scroll;

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse, keep tracking as
        // dragging down until they reach the 'natural' position
        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true;
            setPivotY(getHeight());
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true;
            setPivotY(0f);

        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we approach the limit
        float dragFraction = (float) Math.log10(1 + (Math.abs(totalDrag) / dragDistance));

        // calculate the desired translation given the drag fraction
        float dragTo = dragFraction * dragDistance;

        if (draggingUp) {
            // as we use the absolute magnitude when calculating the drag fraction, need to
            // re-apply the drag direction
            dragTo *= -1;
        }


        Log.e(TAG, "drag to: " + dragTo);

        setTranslationY(dragTo);

        float scale = (1 - ((1 - dragScale) * dragFraction));
        Log.w(TAG, "scale: " + scale);
        if (Math.abs(totalDrag) >= dragDistance) {
            for (View view : views) {
                if (view.getId() == R.id.controlContainer) {
                    view.animate()
                            .alpha(0f)
                            .setDuration(100L)
                            .start();
                }
            }
        } else {
            for (View view : views) {
                if (view.getId() == R.id.controlContainer) {
                    view.animate()
                            .alpha(1f)
                            .setDuration(100L)
                            .start();
                }
            }
        }

        for (View view : views) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }

        // if we've reversed direction and gone past the settle point then clear the flags to
        // allow the list to get the scroll events & reset any transforms
        if ((draggingDown && totalDrag >= 0)
                || (draggingUp && totalDrag <= 0)) {
            totalDrag = dragTo = dragFraction = 0;
            draggingDown = draggingUp = false;
        }
    }
}
