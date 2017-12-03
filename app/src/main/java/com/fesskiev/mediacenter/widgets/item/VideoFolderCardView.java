package com.fesskiev.mediacenter.widgets.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;

import java.util.List;


public class VideoFolderCardView extends CardView {

    public interface OnVideoFolderCardViewListener {

        void onPopupMenuButtonCall(View view);

        void onOpenVideoListCall(View view);

    }

    private OnVideoFolderCardViewListener listener;
    private GestureDetector detector;
    private ImageView popupMenu;
    private ImageView[] frameViews;
    private View frameContainer;
    private TextView description;

    public VideoFolderCardView(Context context) {
        super(context);
        init(context);
    }

    public VideoFolderCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoFolderCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_folder_card_view, this, true);

        popupMenu = view.findViewById(R.id.popupMenu);

        frameContainer = view.findViewById(R.id.frameContainer);
        frameViews = new ImageView[]{
                view.findViewById(R.id.frameView1),
                view.findViewById(R.id.frameView2),
                view.findViewById(R.id.frameView3),
                view.findViewById(R.id.frameView4)
        };
        description = view.findViewById(R.id.fileDescription);

        detector = new GestureDetector(getContext(), new GestureListener());

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isPointInsideView(e.getRawX(), e.getRawY(), popupMenu)) {
                if (listener != null) {
                    listener.onPopupMenuButtonCall(popupMenu);
                }
            }

            if (isPointInsideView(e.getRawX(), e.getRawY(), frameContainer)) {
                if (listener != null) {
                    listener.onOpenVideoListCall(frameContainer);
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

    public void clearFrames() {
        for(ImageView imageView : frameViews){
            imageView.setImageBitmap(null);
        }
    }

    public void setFrameBitmaps(List<Bitmap> bitmaps) {
        for (int i = 0; i < bitmaps.size(); i++) {
            if (i == frameViews.length) {
                break;
            }
            ImageView frameView = frameViews[i];
            Bitmap bitmap = bitmaps.get(i);
            frameView.setImageBitmap(bitmap);
        }
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    public void setOnVideoFolderCardViewListener(OnVideoFolderCardViewListener l) {
        this.listener = l;
    }
}
