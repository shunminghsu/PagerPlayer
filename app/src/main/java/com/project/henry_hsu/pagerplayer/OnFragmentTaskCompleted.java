package com.project.henry_hsu.pagerplayer;

import android.graphics.Bitmap;

/**
 * Created by henry_hsu on 2016/12/16.
 */

public interface OnFragmentTaskCompleted {
    void onTaskStarted();
    void onTaskCompleted(Bitmap thumbnail);
}
