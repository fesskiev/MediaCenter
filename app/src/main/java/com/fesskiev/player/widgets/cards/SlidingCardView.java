package com.fesskiev.player.widgets.cards;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.fesskiev.player.R;

public class SlidingCardView extends FrameLayout {

    public interface OnSlidingCardListener {
        void onDeleteClick();

        void onEditClick();

        void onClick();
    }

    private static final int MIN_DISTANCE = 100;

    private GestureDetector detector;
    private OnSlidingCardListener listener;
    private ImageView editButton;
    private ImageView deleteButton;
    private View slidingContainer;
    private float x1;
    private float x2;
    private boolean isOpen;

    public SlidingCardView(Context context) {
        super(context);
        init(context);
    }

    public SlidingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_sliding_layout, this, true);

        editButton = (ImageView) view.findViewById(R.id.editButton);
        deleteButton = (ImageView) view.findViewById(R.id.deleteButton);

        slidingContainer = view.findViewById(R.id.slidingContainer);

        detector = new GestureDetector(getContext(), new GestureListener());
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isOpen) {
                if (isPointInsideView(e.getRawX(), e.getRawY(), editButton)) {
                    if (listener != null) {
                        listener.onEditClick();
                    }
                }
                if (isPointInsideView(e.getRawX(), e.getRawY(), deleteButton)) {
                    if (listener != null) {
                        listener.onDeleteClick();
                    }
                }
            } else {
                if (listener != null) {
                    listener.onClick();
                }
            }
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (x2 > x1) {
                        isOpen = false;
                        animateSlidingContainer();
                    } else {
                        isOpen = true;
                        animateSlidingContainer();
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                return true;
        }
        return true;
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }

    private void animateSlidingContainer() {
        int marginInPixels = (int) getResources().getDimension(R.dimen.card_view_margin_start);
        float value = isOpen ? -slidingContainer.getWidth() / 3 : marginInPixels;
        slidingContainer.
                animate().
                x(value).
                setDuration(450);
    }

    public void setOnSlidingCardListener(OnSlidingCardListener listener) {
        this.listener = listener;
    }
}
