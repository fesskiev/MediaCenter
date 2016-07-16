package com.fesskiev.player.widgets.surfaces;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

public class VideoTextureView extends TextureView {

    public interface OnVideoTextureListener {

        void onZoom();

        void onDrag();

        void onTouch();
    }

    private OnVideoTextureListener listener;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float[] lastEvent;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;

    private int mode = NONE;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnVideoTextureListener(OnVideoTextureListener l) {
        this.listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getTransform(matrix);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onTouch();
                }

                savedMatrix.set(matrix);

                start.set(event.getX(), event.getY());

                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:

//                if (mode == DRAG) {
//                    if (listener != null) {
//                        listener.onDrag();
//                    }
//
//                    matrix.set(savedMatrix);
//                    float dx = event.getX() - start.x;
//                    float dy = event.getY() - start.y;
//
//
//                    matrix.postTranslate(dx, dy);
//                    setTransform(matrix);
//
//                } else
                if (mode == ZOOM) {
                    if (listener != null) {
                        listener.onZoom();
                    }

                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);

                    }
//                    if (lastEvent != null && event.getPointerCount() == 3) {
//                        newRot = rotation(event);
//                        float r = newRot - d;
//                        float[] values = new float[9];
//                        matrix.getValues(values);
//                        float tx = values[2];
//                        float ty = values[5];
//                        float sx = values[0];
//                        float xc = (getWidth() / 2) * sx;
//                        float yc = (getHeight() / 2) * sx;
//                        matrix.postRotate(r, tx + xc, ty + yc);
//
//                    }
                    setTransform(matrix);
                }
                break;
        }
        return true;
    }


    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double deltaX = (event.getX(0) - event.getX(1));
        double deltaY = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }
}
