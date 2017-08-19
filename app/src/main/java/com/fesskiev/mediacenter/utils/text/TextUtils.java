package com.fesskiev.mediacenter.utils.text;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableStringBuilder;


import com.fesskiev.mediacenter.R;


public class TextUtils {

    public static SpannableStringBuilder getTypefaceString(Context context, CharSequence string) {
        Typeface tf = ResourcesCompat.getFont(context, R.font.ubuntu);
        CustomTypeFaceSpan typefaceSpan = new CustomTypeFaceSpan(tf);

        SpannableStringBuilder builder = new SpannableStringBuilder(string);
        builder.setSpan(typefaceSpan, 0, builder.length(), 0);

        return builder;
    }
}
