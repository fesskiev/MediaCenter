package com.fesskiev.mediacenter.widgets.item;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;


public class AudioCardView extends CardView {

    public interface OnAudioCardViewListener {

        void onPopupMenuButtonCall(View view);

        void onOpenTrackListCall();
    }

    private OnAudioCardViewListener listener;
    private GestureDetector detector;
    private ImageView popupMenu;
    private ImageView coverView;
    private TextView albumName;
    private boolean menuVisible;

    public AudioCardView(Context context) {
        super(context);
        init(context);
    }

    public AudioCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.audio_card_view, this, true);

        popupMenu = view.findViewById(R.id.popupMenu);
        albumName = view.findViewById(R.id.audioName);
        coverView = view.findViewById(R.id.audioCover);


        detector = new GestureDetector(getContext(), new GestureListener());
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (listener != null) {
                if (isPointInsideView(e.getRawX(), e.getRawY(), popupMenu) && menuVisible) {
                    listener.onPopupMenuButtonCall(popupMenu);
                } else {
                    listener.onOpenTrackListCall();
                }
            }
            return true;
        }
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return true;
    }

    public ImageView getCoverView() {
        return coverView;
    }

    public void setAlbumName(String name) {
        albumName.setText(name);
    }

    public void setOnAudioCardViewListener(OnAudioCardViewListener l) {
        this.listener = l;
    }

    public void needMenuVisible(boolean visible) {
        if (visible) {
            popupMenu.setVisibility(VISIBLE);
        } else {
            popupMenu.setVisibility(INVISIBLE);
        }
        this.menuVisible = visible;
    }
}
