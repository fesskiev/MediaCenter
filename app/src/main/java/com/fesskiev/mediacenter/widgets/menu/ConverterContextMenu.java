package com.fesskiev.mediacenter.widgets.menu;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.mediacenter.R;

public class ConverterContextMenu extends ContextMenu {

    public ConverterContextMenu(Context context) {
        super(context);

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.converter_context_menu_layout, this, true);

        ((Animatable) ((ImageView) findViewById(R.id.convertCircularProgress)).getDrawable()).start();
    }
}
