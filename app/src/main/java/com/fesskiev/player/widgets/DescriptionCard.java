package com.fesskiev.player.widgets;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.fesskiev.player.R;


public class DescriptionCard extends CardView {

    private enum ANIMATION {
        NEXT, PREVIOUS;
    }

    public interface OnCardAnimationListener {
        void animationStart();

        void animationEnd();
    }

    private ANIMATION animation;
    private OnCardAnimationListener listener;


    public DescriptionCard(Context context) {
        super(context);
    }

    public DescriptionCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DescriptionCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnCardAnimationListener(OnCardAnimationListener listener) {
        this.listener = listener;
    }

    public void next() {
        animation = ANIMATION.NEXT;
        cardAnimate();
    }

    public void previous() {
        animation = ANIMATION.PREVIOUS;
        cardAnimate();
    }

    private void cardAnimate() {
        float value = animation == ANIMATION.NEXT ? getWidth() +
                getResources().getDimension(R.dimen.activity_horizontal_margin) :
                -(getWidth() - getResources().getDimension(R.dimen.activity_horizontal_margin));

        animate().
                x(value).
                setDuration(500).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (listener != null) {
                            listener.animationStart();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animate().x(getResources().getDimension(R.dimen.activity_horizontal_margin)).
                                setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (listener != null) {
                                            listener.animationEnd();
                                        }
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

}
