package com.fesskiev.mediacenter.utils;


import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

public class AppGlideModule implements GlideModule {


    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setBitmapPool(new LruBitmapPool(52428800));
        builder.setMemoryCache(new LruResourceCache(52428800 * 2));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
