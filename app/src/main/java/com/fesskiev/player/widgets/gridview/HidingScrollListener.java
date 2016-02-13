package com.fesskiev.player.widgets.gridview;


import android.widget.AbsListView;

public abstract class HidingScrollListener implements AbsListView.OnScrollListener {

    private int lastFirstVisibleItem;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (firstVisibleItem > lastFirstVisibleItem) {
            onHide();
        } else if (firstVisibleItem < lastFirstVisibleItem) {
            onShow();
        }
        lastFirstVisibleItem = firstVisibleItem;
    }

    public abstract void onHide();
    public abstract void onShow();


}
