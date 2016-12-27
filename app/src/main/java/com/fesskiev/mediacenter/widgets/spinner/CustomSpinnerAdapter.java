package com.fesskiev.mediacenter.widgets.spinner;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;

import java.util.List;

public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private List<String> data;
    private Context context;
    private int itemColor;
    private int dropdownColor;
    private int gravity;

    public CustomSpinnerAdapter(Context context, List<String> data, int itemColor, int dropDownColor) {
        this.context = context;
        this.data = data;
        this.itemColor = itemColor;
        this.dropdownColor = dropDownColor;
        this.gravity = -1;
    }

    public void setItemGravity(int gravity) {
        this.gravity = gravity;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) View.inflate(context, R.layout.spinner_item, null);
        textView.setText(data.get(position));
        textView.setTextColor(ContextCompat.getColor(context, itemColor));
        if (gravity != -1) {
            textView.setGravity(gravity);
        }
        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) View.inflate(context, R.layout.spinner_item, null);
        textView.setText(data.get(position));
        textView.setTextColor(ContextCompat.getColor(context, dropdownColor));
        return textView;
    }
}
