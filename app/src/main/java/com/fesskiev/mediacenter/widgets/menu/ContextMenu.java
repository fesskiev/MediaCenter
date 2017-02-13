package com.fesskiev.mediacenter.widgets.menu;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;


public class ContextMenu extends LinearLayout {

    public ContextMenu(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setBackgroundResource(R.drawable.bg_container_shadow);
        setOrientation(VERTICAL);

        setLayoutParams(new LayoutParams((int) Utils.dipToPixels(context, 200),
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void dismiss() {
        ((ViewGroup) getParent()).removeView(this);
    }

}
