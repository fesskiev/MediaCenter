package com.fesskiev.player.widgets.recycleview;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemTouchClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {

        void onItemClick(View childView, int position);

        void onItemLongPress(View childView, int position);

    }

    protected OnItemClickListener listener;
    private GestureDetector gestureDetector;
    private View childView;
    private int childViewPosition;

    public RecyclerItemTouchClickListener(Context context, OnItemClickListener listener) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent event) {
        childView = view.findChildViewUnder(event.getX(), event.getY());
        childViewPosition = view.getChildAdapterPosition(childView);

        return childView != null && gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent event) {
    }


    protected class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (childView != null) {
                listener.onItemClick(childView, childViewPosition);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if (childView != null) {
                listener.onItemLongPress(childView, childViewPosition);
            }
        }



        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
