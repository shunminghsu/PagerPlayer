package com.project.henry_hsu.pagerplayer;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

public class AppApplication extends Application {
    private ThumbnailCache mThumbnails;

    public static ThumbnailCache getThumbnailsCache(Context context) {
        final AppApplication app = (AppApplication) context.getApplicationContext();
        final ThumbnailCache thumbnails = app.mThumbnails;
        return thumbnails;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;

        mThumbnails = new ThumbnailCache(memoryClassBytes / 4);

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_MODERATE) {
            mThumbnails.evictAll();
        } else if (level >= TRIM_MEMORY_BACKGROUND) {
            mThumbnails.trimToSize(mThumbnails.size() / 2);
        }
    }
}