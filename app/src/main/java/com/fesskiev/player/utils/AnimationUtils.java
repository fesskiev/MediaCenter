package com.fesskiev.player.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

import com.fesskiev.player.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AnimationUtils {

    public static void setupSlideWindowAnimations(Activity activity) {
        Slide slideTransition = getSlideTransaction(activity);
        activity.getWindow().setReenterTransition(slideTransition);
        activity.getWindow().setExitTransition(slideTransition);
    }

    public static Slide getSlideTransaction(Activity activity) {
        Slide slideTransition = new Slide(Gravity.START);
        slideTransition.setDuration(activity.getResources().getInteger(R.integer.anim_duration_long));
        return slideTransition;
    }

    public static Bundle createBundle(Activity activity){
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity,
                        AnimationUtils.createSafeTransitionParticipants(activity, false));
        return options.toBundle();
    }

    public static Pair<View, String>[] createSafeTransitionParticipants(@NonNull Activity activity,
                                                                        boolean includeStatusBar, @Nullable Pair... otherParticipants) {
        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        View decor = activity.getWindow().getDecorView();
        View statusBar = null;
        if (includeStatusBar) {
            statusBar = decor.findViewById(android.R.id.statusBarBackground);
        }
        View navBar = decor.findViewById(android.R.id.navigationBarBackground);

        // Create pair of transition participants.
        List<Pair> participants = new ArrayList<>(3);
        addNonNullViewToTransitionParticipants(statusBar, participants);
        addNonNullViewToTransitionParticipants(navBar, participants);
        // only add transition participants if there's at least one none-null element
        if (otherParticipants != null && !(otherParticipants.length == 1
                && otherParticipants[0] == null)) {
            participants.addAll(Arrays.asList(otherParticipants));
        }
        return participants.toArray(new Pair[participants.size()]);
    }

    private static void addNonNullViewToTransitionParticipants(View view, List<Pair> participants) {
        if (view == null) {
            return;
        }
        participants.add(new Pair<>(view, view.getTransitionName()));
    }

}
