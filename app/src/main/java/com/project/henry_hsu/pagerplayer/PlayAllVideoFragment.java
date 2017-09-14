package com.project.henry_hsu.pagerplayer;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;



public class PlayAllVideoFragment extends Fragment implements SurfaceHolder.Callback {
    int mPosition;
    private View.OnClickListener mVideoCickListener;
    public int getPosition() {
        return mPosition;
    }
    public void setViewClickListener(View.OnClickListener listener) {
        mVideoCickListener = listener;

        if (mThumbnail != null) { //for this memoried fragment
            mThumbnail.setOnClickListener(mVideoCickListener);
            mSurfaceView.setOnClickListener(mVideoCickListener);
        }
    }

    public OnFragmentTaskCompleted mOnFragmentTaskCompleted = new OnFragmentTaskCompleted() {
        @Override
        public void onTaskStarted() {
        }

        @Override
        public void onTaskCompleted(Bitmap thumbnail) {
            mThumbnail.setImageBitmap(thumbnail);
            mThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    };

    public interface SurfaceHolderFragment {
        void OnFragmentSurfaceCreated(BrowserSurfaceView mSurfaceView);
        void OnFragmentSurfaceDestroyed(BrowserSurfaceView mSurfaceView);
    }
    private SurfaceHolderFragment myInterface;

    public void setFragmentSurfaceListener(SurfaceHolderFragment listener) {
        this.myInterface = listener;
    }

    private ImageView mThumbnail;
    private ImageView mPlayImg;
    public boolean mIsShowImage;
    private BrowserSurfaceView mSurfaceView;

    public static PlayAllVideoFragment init(int position, boolean showImage, int width, int orientation) {
        PlayAllVideoFragment videoFragment = new PlayAllVideoFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putBoolean("showImage", showImage);
        args.putInt("width", width);
        args.putInt("orientation", orientation);
        videoFragment.setArguments(args);

        return videoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt("position");
        mIsShowImage = getArguments().getBoolean("showImage");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playall_video, container, false);
        mThumbnail = (ImageView) root.findViewById(R.id.thumbnail);
        mPlayImg = (ImageView) root.findViewById(R.id.play_img);
        mSurfaceView = (BrowserSurfaceView) root.findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
        int orientation = getArguments().getInt("orientation");
        setLayout(orientation);
        if (mIsShowImage) {
            mThumbnail.setVisibility(View.VISIBLE);
            mPlayImg.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
        } else {
            mThumbnail.setVisibility(View.GONE);
            mPlayImg.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.VISIBLE);
        }
        mThumbnail.setOnClickListener(mVideoCickListener);
        mSurfaceView.setOnClickListener(mVideoCickListener);
        return root;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayout(newConfig.orientation);
    }

    private void setLayout(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams layoutParams;
            layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mThumbnail.setLayoutParams(layoutParams);
            mSurfaceView.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams;
            int width = getArguments().getInt("width");
            layoutParams = new RelativeLayout.LayoutParams(width, width*9/16);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mThumbnail.setLayoutParams(layoutParams);
            mSurfaceView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onDestroy() {
        mSurfaceView.setOnClickListener(null);
        mThumbnail.setOnClickListener(null);
        super.onDestroy();
    }

    public void changeState(boolean isShowImage) {
        if (isShowImage) {
            mThumbnail.setVisibility(View.VISIBLE);
            mPlayImg.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            mIsShowImage = true;
        } else {
            mThumbnail.setVisibility(View.GONE);
            mPlayImg.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.VISIBLE);
            mIsShowImage = false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (myInterface != null)
            myInterface.OnFragmentSurfaceCreated(mSurfaceView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (myInterface != null)
            myInterface.OnFragmentSurfaceDestroyed(mSurfaceView);
    }
}
