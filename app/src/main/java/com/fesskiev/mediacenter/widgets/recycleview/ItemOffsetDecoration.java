package com.fesskiev.mediacenter.widgets.recycleview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private int spacing;

    public ItemOffsetDecoration(int itemOffset) {
        spacing = itemOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(spacing, spacing, spacing, spacing);
    }
}