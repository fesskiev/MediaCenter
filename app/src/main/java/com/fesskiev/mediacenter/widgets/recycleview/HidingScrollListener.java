package com.fesskiev.mediacenter.widgets.recycleview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;

public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    public abstract void onHide();

    public abstract void onShow();

    public abstract void onItemPosition(int position);

    private static final int HIDE_THRESHOLD = 150;

    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            int completelyPosition = ((LinearLayoutManager)
                    recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

            onItemPosition(completelyPosition);
        }
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int firstVisibleItem = ((LinearLayoutManager)
                recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

        ContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);

        if (firstVisibleItem == 0) {
            if (!controlsVisible) {
                onShow();
                controlsVisible = true;
            }
        } else {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }
        }
        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }
}
