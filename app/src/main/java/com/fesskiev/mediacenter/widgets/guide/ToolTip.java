package com.fesskiev.mediacenter.widgets.guide;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;


public class ToolTip {

    public Animation enterAnimation, exitAnimation;

    public String title;
    public String description;
    public int width;
    public int gravity;

    public View.OnClickListener onClickListener;

    public ToolTip(){
        title = "";
        description = "";

        enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(1000);
        enterAnimation.setFillAfter(true);
        enterAnimation.setInterpolator(new BounceInterpolator());
        width = -1;

        gravity = Gravity.CENTER;
    }
    /**
     * Set title text
     * @param title
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setTitle(String title){
        this.title = title;
        return this;
    }

    /**
     * Set description text
     * @param description
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setDescription(String description){
        this.description = description;
        return this;
    }
    /**
     * Set enter animation
     * @param enterAnimation
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setEnterAnimation(Animation enterAnimation){
        this.enterAnimation = enterAnimation;
        return this;
    }
    /**

    /**
     * Set the gravity, the setGravity is centered relative to the targeted button
     * @param gravity Gravity.CENTER, Gravity.TOP, Gravity.BOTTOM, etc
     * @return return ToolTip instance for chaining purpose
     */
    public ToolTip setGravity(int gravity){
        this.gravity = gravity;
        return this;
    }

    /**
     * Method to set the width of the ToolTip
     * @param px desired width of ToolTip in pixels
     * @return ToolTip instance for chaining purposes
     */
    public ToolTip setWidth(int px){
        if(px >= 0) width = px;
        return this;
    }

    public ToolTip setOnClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
        return this;
    }
}
