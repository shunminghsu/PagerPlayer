package com.project.henry_hsu.pagerplayer;

import android.content.Context;
import android.view.ViewGroup;
import android.view.SurfaceView;
import android.util.AttributeSet;

/**
 * Created by henry_hsu on 2016/12/12.
 */

public class BrowserSurfaceView extends SurfaceView{

    public BrowserSurfaceView(Context context) {
        super(context);
    }
    public BrowserSurfaceView(Context context, int id) {
        super(context);
    }
    public BrowserSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public BrowserSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSurfaceViewSize(int width, int height) {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if(width < height) {
            lp.width = width;
            lp.height = width * 9 / 16;
        } else {
            lp.width = height;
            lp.height = height * 9 / 16;
        }
        this.setLayoutParams(lp);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
