package com.fesskiev.mediacenter.widgets.guide;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;


public class TourGuide {

    /**
     * This describes the allowable motion, for example if you want the users to learn about clicking,
     * but want to stop them from swiping, then use CLICK_ONLY
     */
    public enum MotionType {
        ALLOW_ALL, CLICK_ONLY, SWIPE_ONLY
    }

    private View targetView;
    private Activity activity;
    private MotionType motionType;
    private FrameLayoutWithHole frameLayout;
    private View toolTipViewGroup;
    private ToolTip toolTip;
    private Pointer pointer;
    private Overlay overlay;


    public static TourGuide init(Activity activity) {
        return new TourGuide(activity);
    }

    /* Constructor */
    public TourGuide(Activity activity) {
        this.activity = activity;
    }


    /**
     * Sets which motion type is motionType
     *
     * @param motionType
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide motionType(MotionType motionType) {
        this.motionType = motionType;
        return this;
    }

    /**
     * Sets the targeted view for TourGuide to play on
     *
     * @param targetView the view in which the tutorial button will be placed on top of
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide playOn(View targetView) {
        this.targetView = targetView;
        setupView();
        return this;
    }

    /**
     * Sets the overlay
     *
     * @param overlay this overlay object should contain the attributes of the overlay, such as background color, animation, Style, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setOverlay(Overlay overlay) {
        this.overlay = overlay;
        return this;
    }

    /**
     * Set the toolTip
     *
     * @param toolTip this toolTip object should contain the attributes of the ToolTip, such as, the title text, and the description text, background color, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setToolTip(ToolTip toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    /**
     * Set the Pointer
     *
     * @param pointer this pointer object should contain the attributes of the Pointer, such as the pointer color, pointer gravity, etc, refer to @Link{pointer}
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setPointer(Pointer pointer) {
        this.pointer = pointer;
        return this;
    }

    /**
     * Clean up the tutorial that is added to the activity
     */
    public void cleanUp() {
        frameLayout.cleanUp();
        if (toolTipViewGroup != null) {
            ((ViewGroup) activity.getWindow().getDecorView()).removeView(toolTipViewGroup);
        }
    }

    /**
     * @return FrameLayoutWithHole that is used as overlay
     */
    public FrameLayoutWithHole getOverlay() {
        return frameLayout;
    }

    /**
     * @return the ToolTip container View
     */
    public View getToolTip() {
        return toolTipViewGroup;
    }

    private int getXBasedOnGravity(int width) {
        int[] pos = new int[2];
        targetView.getLocationOnScreen(pos);
        int x = pos[0];
        if ((pointer.gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return x + targetView.getWidth() - width;
        } else if ((pointer.gravity & Gravity.LEFT) == Gravity.LEFT) {
            return x;
        } else { // this is center
            return x + targetView.getWidth() / 2;
        }
    }

    private int getYBasedOnGravity(int height) {
        int[] pos = new int[2];
        targetView.getLocationInWindow(pos);
        int y = pos[1];
        if ((pointer.gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return y + targetView.getHeight() - height;
        } else if ((pointer.gravity & Gravity.TOP) == Gravity.TOP) {
            return y;
        } else { // this is center
            return y + targetView.getHeight() / 2;
        }
    }

    protected void setupView() {
        // TourGuide can only be setup after all the views is ready and obtain it's position/measurement
        // so when this is the 1st time TourGuide is being added,
        // else block will be executed, and ViewTreeObserver will make TourGuide setup process to be delayed until everything is ready
        // when this is run the 2nd or more times, if block will be executed
        if (ViewCompat.isAttachedToWindow(targetView)) {
            startView();
        } else {
            final ViewTreeObserver viewTreeObserver = targetView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startView();
                }
            });
        }
    }

    private void startView() {
        /* Initialize a frame layout with a hole */
        frameLayout = new FrameLayoutWithHole(activity, targetView, motionType, overlay);
        /* handle click disable */
        handleDisableClicking(frameLayout);

        /* setup floating action button */
        if (pointer != null) {
            FloatingActionButton fab = setupAndAddFABToFrameLayout(frameLayout);
            performAnimationOn(fab);
        }
        setupFrameLayout();
        /* setup tooltip view */
        setupToolTip();
    }

    private void handleDisableClicking(FrameLayoutWithHole frameLayoutWithHole) {
        // 1. if user provides an overlay listener, use that as 1st priority
        if (overlay != null && overlay.onClickListener != null) {
            frameLayoutWithHole.setClickable(true);
            frameLayoutWithHole.setOnClickListener(overlay.onClickListener);
        }
        // 2. if overlay listener is not provided, check if it's disabled
        else if (overlay != null && overlay.disableClick) {
            frameLayoutWithHole.setViewHole(targetView);
            frameLayoutWithHole.setSoundEffectsEnabled(false);
            frameLayoutWithHole.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                } // do nothing, disabled.
            });
        }
    }

    private void setupToolTip() {
        final FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

        if (toolTip != null) {
            /* inflate and get views */
            ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView();
            LayoutInflater layoutInflater = activity.getLayoutInflater();

            toolTipViewGroup = layoutInflater.inflate(R.layout.tooltip_layout, null);
            TextView toolTipTitleTV = (TextView) toolTipViewGroup.findViewById(R.id.title);
            TextView toolTipDescriptionTV = (TextView) toolTipViewGroup.findViewById(R.id.description);

            if (toolTip.title == null || toolTip.title.isEmpty()) {
                toolTipTitleTV.setVisibility(View.GONE);
            } else {
                toolTipTitleTV.setVisibility(View.VISIBLE);
                toolTipTitleTV.setText(toolTip.title);
            }

            if (toolTip.description == null || toolTip.description.isEmpty()) {
                toolTipDescriptionTV.setVisibility(View.GONE);
            } else {
                toolTipDescriptionTV.setVisibility(View.VISIBLE);
                toolTipDescriptionTV.setText(toolTip.description);
            }

            if (toolTip.width != -1) {
                layoutParams.width = toolTip.width;
            }

            toolTipViewGroup.startAnimation(toolTip.enterAnimation);

            /* position and size calculation */
            int[] pos = new int[2];
            targetView.getLocationOnScreen(pos);
            int targetViewX = pos[0];
            final int targetViewY = pos[1];

            // get measured size of tooltip
            toolTipViewGroup.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int toolTipMeasuredWidth = toolTip.width != -1 ? toolTip.width : toolTipViewGroup.getMeasuredWidth();
            int toolTipMeasuredHeight = toolTipViewGroup.getMeasuredHeight();

            Point resultPoint = new Point(); // this holds the final position of tooltip
            float density = activity.getResources().getDisplayMetrics().density;
            final float adjustment = 10 * density; //adjustment is that little overlapping area of tooltip and targeted button

            // calculate x position, based on gravity, tooltipMeasuredWidth, parent max width, x position of target view, adjustment
            if (toolTipMeasuredWidth > parent.getWidth()) {
                resultPoint.x = getXForTooTip(toolTip.gravity, parent.getWidth(), targetViewX, adjustment);
            } else {
                resultPoint.x = getXForTooTip(toolTip.gravity, toolTipMeasuredWidth, targetViewX, adjustment);
            }

            resultPoint.y = getYForTooTip(toolTip.gravity, toolTipMeasuredHeight, targetViewY, adjustment);

            // add view to parent
            parent.addView(toolTipViewGroup, layoutParams);

            // 1. width < screen check
            if (toolTipMeasuredWidth > parent.getWidth()) {
                toolTipViewGroup.getLayoutParams().width = parent.getWidth();
                toolTipMeasuredWidth = parent.getWidth();
            }
            // 2. x left boundary check
            if (resultPoint.x < 0) {
                toolTipViewGroup.getLayoutParams().width = toolTipMeasuredWidth + resultPoint.x; //since point.x is negative, use plus
                resultPoint.x = 0;
            }
            // 3. x right boundary check
            int tempRightX = resultPoint.x + toolTipMeasuredWidth;
            if (tempRightX > parent.getWidth()) {
                toolTipViewGroup.getLayoutParams().width = parent.getWidth() - resultPoint.x; //since point.x is negative, use plus
            }

            // pass toolTip onClickListener into toolTipViewGroup
            if (toolTip.onClickListener != null) {
                toolTipViewGroup.setOnClickListener(toolTip.onClickListener);
            }

            // TODO: no boundary check for height yet, this is a unlikely case though
            // height boundary can be fixed by user changing the gravity to the other size, since there are plenty of space vertically compared to horizontally

            // this needs an viewTreeObserver, that's because TextView measurement of it's vertical height is not accurate (didn't take into account of multiple lines yet) before it's rendered
            // re-calculate height again once it's rendered
            toolTipViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    toolTipViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int fixedY;
                    int toolTipHeightAfterLayouted = toolTipViewGroup.getHeight();
                    fixedY = getYForTooTip(toolTip.gravity, toolTipHeightAfterLayouted, targetViewY, adjustment);
                    layoutParams.setMargins((int) toolTipViewGroup.getX(), fixedY, 0, 0);
                }
            });

            // set the position using setMargins on the left and top
            layoutParams.setMargins(resultPoint.x, resultPoint.y, 0, 0);
        }

    }

    private int getXForTooTip(int gravity, int toolTipMeasuredWidth, int targetViewX, float adjustment) {
        int x;
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = targetViewX - toolTipMeasuredWidth + (int) adjustment;
        } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            x = targetViewX + targetView.getWidth() - (int) adjustment;
        } else {
            x = targetViewX + targetView.getWidth() / 2 - toolTipMeasuredWidth / 2;
        }
        return x;
    }

    private int getYForTooTip(int gravity, int toolTipMeasuredHeight, int targetViewY, float adjustment) {
        int y;
        if ((gravity & Gravity.TOP) == Gravity.TOP) {

            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y = targetViewY - toolTipMeasuredHeight + (int) adjustment;
            } else {
                y = targetViewY - toolTipMeasuredHeight - (int) adjustment;
            }
        } else { // this is center
            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y = targetViewY + targetView.getHeight() - (int) adjustment;
            } else {
                y = targetViewY + targetView.getHeight() + (int) adjustment;
            }
        }
        return y;
    }

    private FloatingActionButton setupAndAddFABToFrameLayout(final FrameLayoutWithHole frameLayoutWithHole) {

        // fab is the real fab that is going to be added
        final FloatingActionButton fab = new FloatingActionButton(activity);
        fab.setVisibility(View.VISIBLE);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setClickable(true);
        fab.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}},
                new int[]{ContextCompat.getColor(fab.getContext(), R.color.red)}));
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getXBasedOnGravity(fab.getWidth()), getYBasedOnGravity(fab.getHeight()), 0, 0);
        frameLayoutWithHole.addView(fab, params);

        return fab;
    }

    private void setupFrameLayout() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup contentArea = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        int[] pos = new int[2];
        contentArea.getLocationOnScreen(pos);
        // frameLayoutWithHole's coordinates are calculated taking full screen height into account
        // but we're adding it to the content area only, so we need to offset it to the same Y value of contentArea

        layoutParams.setMargins(0, -pos[1], 0, 0);
        contentArea.addView(frameLayout, layoutParams);
    }

    private void performAnimationOn(final View view) {

        final AnimatorSet animatorSet = new AnimatorSet();
        final AnimatorSet animatorSet2 = new AnimatorSet();
        Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setTranslationX(0);
                animatorSet2.start();
            }
        };
        Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setTranslationX(0);
                animatorSet.start();
            }
        };

        long fadeInDuration = 800;
        long scaleDownDuration = 800;
        long fadeOutDuration = 800;
        long delay = 1000;

        final ValueAnimator delayAnim = ObjectAnimator.ofFloat(view, "translationX", 0);
        delayAnim.setDuration(delay);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeInAnim.setDuration(fadeInDuration);
        final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
        scaleDownX.setDuration(scaleDownDuration);
        final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
        scaleDownY.setDuration(scaleDownDuration);
        final ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
        scaleUpX.setDuration(scaleDownDuration);
        final ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
        scaleUpY.setDuration(scaleDownDuration);
        final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOutAnim.setDuration(fadeOutDuration);

        final ValueAnimator delayAnim2 = ObjectAnimator.ofFloat(view, "translationX", 0);
        delayAnim2.setDuration(delay);
        final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeInAnim2.setDuration(fadeInDuration);
        final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
        scaleDownX2.setDuration(scaleDownDuration);
        final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
        scaleDownY2.setDuration(scaleDownDuration);
        final ObjectAnimator scaleUpX2 = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
        scaleUpX2.setDuration(scaleDownDuration);
        final ObjectAnimator scaleUpY2 = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
        scaleUpY2.setDuration(scaleDownDuration);
        final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOutAnim2.setDuration(fadeOutDuration);
        view.setAlpha(0);
        animatorSet.setStartDelay(toolTip != null ? toolTip.enterAnimation.getDuration() : 0);
        animatorSet.play(fadeInAnim);
        animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
        animatorSet.play(scaleUpX).with(scaleUpY).with(fadeOutAnim).after(scaleDownY);
        animatorSet.play(delayAnim).after(scaleUpY);

        animatorSet2.play(fadeInAnim2);
        animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
        animatorSet2.play(scaleUpX2).with(scaleUpY2).with(fadeOutAnim2).after(scaleDownY2);
        animatorSet2.play(delayAnim2).after(scaleUpY2);

        animatorSet.addListener(lis1);
        animatorSet2.addListener(lis2);
        animatorSet.start();

            /* these animatorSets are kept track in FrameLayout,
            so that they can be cleaned up when FrameLayout is detached from window */
        frameLayout.addAnimatorSet(animatorSet);
        frameLayout.addAnimatorSet(animatorSet2);
    }

    public View getTargetView() {
        return targetView;
    }
}
