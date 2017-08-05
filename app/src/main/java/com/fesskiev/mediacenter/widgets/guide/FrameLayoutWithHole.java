package com.fesskiev.mediacenter.widgets.guide;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.ArrayList;


public class FrameLayoutWithHole extends FrameLayout {

    private Activity activity;
    private TourGuide.MotionType motionType;
    private Paint eraser;

    private Bitmap eraserBitmap;
    private Canvas eraserCanvas;
    private View viewHole;
    private int radius;
    private int[] pos;
    private float density;
    private Overlay overlay;
    private RectF rectF;

    private boolean mCleanUpLock = false;

    private ArrayList<AnimatorSet> mAnimatorSetArrayList;

    public void setViewHole(View viewHole) {
        this.viewHole = viewHole;
        enforceMotionType();
    }

    public void addAnimatorSet(AnimatorSet animatorSet) {
        if (mAnimatorSetArrayList == null) {
            mAnimatorSetArrayList = new ArrayList<>();
        }
        mAnimatorSetArrayList.add(animatorSet);
    }

    private void enforceMotionType() {
        if (viewHole != null) {
            if (motionType != null && motionType == TourGuide.MotionType.CLICK_ONLY) {
                viewHole.setOnTouchListener((view, motionEvent) -> {
                    viewHole.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });
            } else if (motionType != null && motionType == TourGuide.MotionType.SWIPE_ONLY) {
                viewHole.setClickable(false);
            }
        }
    }

    public FrameLayoutWithHole(Activity context, View view) {
        this(context, view, TourGuide.MotionType.ALLOW_ALL);
    }

    public FrameLayoutWithHole(Activity context, View view, TourGuide.MotionType motionType) {
        this(context, view, motionType, new Overlay());
    }

    public FrameLayoutWithHole(Activity context, View view, TourGuide.MotionType motionType, Overlay overlay) {
        super(context);
        activity = context;
        viewHole = view;
        init(null, 0);
        enforceMotionType();
        this.overlay = overlay;

        int[] pos = new int[2];
        viewHole.getLocationOnScreen(pos);
        this.pos = pos;

        density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (20 * density);

        if (viewHole.getHeight() > viewHole.getWidth()) {
            radius = viewHole.getHeight() / 2 + padding;
        } else {
            radius = viewHole.getWidth() / 2 + padding;
        }
        this.motionType = motionType;

        // Init a RectF to be used in OnDraw for a ROUNDED_RECTANGLE Style Overlay
        if (this.overlay != null && this.overlay.style == Overlay.Style.ROUNDED_RECTANGLE) {
            int recfFPaddingPx = (int) (this.overlay.paddingDp * density);
            rectF = new RectF(this.pos[0] - recfFPaddingPx + this.overlay.holeOffsetLeft,
                    this.pos[1] - recfFPaddingPx + this.overlay.holeOffsetTop,
                    this.pos[0] + viewHole.getWidth() + recfFPaddingPx + this.overlay.holeOffsetLeft,
                    this.pos[1] + viewHole.getHeight() + recfFPaddingPx + this.overlay.holeOffsetTop);
        }
    }

    private void init(AttributeSet attrs, int defStyle) {

        setWillNotDraw(false);
        // Set up a default TextPaint object
        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        Point size = new Point();
        size.x = activity.getResources().getDisplayMetrics().widthPixels;
        size.y = activity.getResources().getDisplayMetrics().heightPixels;

        eraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        eraserCanvas = new Canvas(eraserBitmap);

        Paint mPaint = new Paint();
        mPaint.setColor(0xcc000000);
        Paint transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);

    }



    protected void cleanUp() {
        if (getParent() != null) {
            if (overlay != null && overlay.exitAnimation != null) {
                performOverlayExitAnimation();
            } else {
                ((ViewGroup) this.getParent()).removeView(this);
            }
        }
    }

    private void performOverlayExitAnimation() {
        if (!mCleanUpLock) {
            final FrameLayout pointerToFrameLayout = this;
            mCleanUpLock = true;
            overlay.exitAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ViewGroup) pointerToFrameLayout.getParent()).removeView(pointerToFrameLayout);
                }
            });
            this.startAnimation(overlay.exitAnimation);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (overlay != null && overlay.enterAnimation != null) {
            this.startAnimation(overlay.enterAnimation);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        eraserCanvas.setBitmap(null);
        eraserBitmap = null;

        if (mAnimatorSetArrayList != null && !mAnimatorSetArrayList.isEmpty()) {
            for (int i = 0; i < mAnimatorSetArrayList.size(); i++) {
                mAnimatorSetArrayList.get(i).end();
                mAnimatorSetArrayList.get(i).removeAllListeners();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        eraserBitmap.eraseColor(Color.TRANSPARENT);

        if (overlay != null) {
            eraserCanvas.drawColor(overlay.backgroundColor);
            int padding = (int) (overlay.paddingDp * density);

            if (overlay.style == Overlay.Style.RECTANGLE) {
                eraserCanvas.drawRect(
                        pos[0] - padding + overlay.holeOffsetLeft,
                        pos[1] - padding + overlay.holeOffsetTop,
                        pos[0] + viewHole.getWidth() + padding + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() + padding + overlay.holeOffsetTop, eraser);
            } else if (overlay.style == Overlay.Style.NO_HOLE) {
                eraserCanvas.drawCircle(
                        pos[0] + viewHole.getWidth() / 2 + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() / 2 + overlay.holeOffsetTop,
                        0, eraser);
            } else if (overlay.style == Overlay.Style.ROUNDED_RECTANGLE) {
                int roundedCornerRadiusPx;
                if (overlay.roundedCornerRadiusDp != 0) {
                    roundedCornerRadiusPx = (int) (overlay.roundedCornerRadiusDp * density);
                } else {
                    roundedCornerRadiusPx = (int) (10 * density);
                }
                eraserCanvas.drawRoundRect(rectF, roundedCornerRadiusPx, roundedCornerRadiusPx, eraser);
            } else {
                int holeRadius = overlay.holeRadius != Overlay.NOT_SET ? overlay.holeRadius : radius;
                eraserCanvas.drawCircle(
                        pos[0] + viewHole.getWidth() / 2 + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() / 2 + overlay.holeOffsetTop,
                        holeRadius, eraser);
            }
        }
        canvas.drawBitmap(eraserBitmap, 0, 0, null);

    }

}
