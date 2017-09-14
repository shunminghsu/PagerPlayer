package com.project.henry_hsu.pagerplayer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by henry on 2016/12/13.
 */

public class PlayAllActivity extends AppCompatActivity implements  View.OnClickListener {
    private static String TAG = "henry";
    //name your folder, videoloader will parse the videos in the folder
    private String TEST_VIDEO_FOLDER_NAME = "";
    private LoaderManager.LoaderCallbacks<ArrayList<String>> mCallbacks;
    private PlayAllAdapter mAdapter;
    private int mOrientation = 0;
    private ViewPager mPager;
    private ArrayList<String> mList;
    private int mPosition;
    private Toolbar mHeaderBar;
    private TextView mToolbarTitle;
    private TextView mDownload, mDelete, mShare;
    private RelativeLayout mBottomLayout;

    private static final boolean SHOW_THUMBNAIL = true;
    private static final boolean SHOW_VIDEO = false;
    private View mPlayView;
    private Context mContext;
    int mScreenH, mScreenW;
    private SeekBar mSeekBar;
    private LinearLayout mTimeLineLayout;

    //Define event when touch the video view
    private static final int VIDEO_STATE_STOP = 1;
    private static final int VIDEO_STATE_HALF_PLAY = 2;
    private static final int VIDEO_STATE_HALF_PAUSE = 3;
    private static final int VIDEO_STATE_FULL_PLAY = 4;
    private static final int VIDEO_STATE_FULL_PAUSE = 5;
    private int mVideoState = VIDEO_STATE_STOP;
    private View.OnClickListener mOnVideoClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (mVideoState == VIDEO_STATE_STOP) {
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTimeLineLayout.setVisibility(View.VISIBLE);
                    ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(mPager.getCurrentItem())).changeState(SHOW_VIDEO);
                }
                return;
            }

            switch (mVideoState) {
                case VIDEO_STATE_STOP:
                    ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(mPager.getCurrentItem())).changeState(SHOW_VIDEO);
                    mHeaderBar.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                    mTimeLineLayout.setVisibility(View.INVISIBLE);
                    break;
                case VIDEO_STATE_FULL_PAUSE:
                    mHeaderBar.setVisibility(View.VISIBLE);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTimeLineLayout.setVisibility(View.VISIBLE);
                    mVideoState = VIDEO_STATE_HALF_PAUSE;
                    break;
                case VIDEO_STATE_FULL_PLAY:
                    mHeaderBar.setVisibility(View.VISIBLE);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTimeLineLayout.setVisibility(View.VISIBLE);
                    mVideoState = VIDEO_STATE_HALF_PLAY;
                    break;
                case VIDEO_STATE_HALF_PLAY:
                    mHeaderBar.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                    mTimeLineLayout.setVisibility(View.INVISIBLE);
                    mVideoState = VIDEO_STATE_FULL_PLAY;
                    break;
                case VIDEO_STATE_HALF_PAUSE:
                    mHeaderBar.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                    mTimeLineLayout.setVisibility(View.INVISIBLE);
                    mVideoState = VIDEO_STATE_FULL_PAUSE;
                    break;
            }
        }
    };

    ////player////start////
    IjkMediaPlayer mediaPlayer;
    private void onPlaying() {
        beginTimer();
        mSeekBar.setEnabled(true);
        ((TextView)findViewById(R.id.video_total_time)).setText(setFormatTimes((int) mediaPlayer.getDuration()));
        mPlayView.setBackgroundResource(R.mipmap.ic_playback_pause);
        mPlayView.setVisibility(View.VISIBLE);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT)
            mVideoState = VIDEO_STATE_HALF_PLAY;
        else if (mVideoState == VIDEO_STATE_HALF_PAUSE) {
            mVideoState = VIDEO_STATE_HALF_PLAY;
        } else if (mVideoState == VIDEO_STATE_STOP) {
            mVideoState = VIDEO_STATE_FULL_PLAY;
        }
    }

    private void onStoped() {
        endTimer();
        mSeekBar.setProgress(1);
        mSeekBar.setEnabled(false);
        ((TextView)findViewById(R.id.video_current_time)).setText(setFormatTimes(0));
        mPlayView.setBackgroundResource(R.mipmap.ic_playback_resume);
        mVideoState = VIDEO_STATE_STOP;
        mPlayView.setVisibility(View.INVISIBLE);
    }

    IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "onInfo " + i +", " + i1);
            if (i == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                onPlaying();
            }
            return true;
        }
    };

    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {


        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "onError " + i);
            return true;
        }
    };

    IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

        }
    };

    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            Log.d(TAG, "onCompletion ");
            onStoped();
            ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(mPager.getCurrentItem())).changeState(SHOW_THUMBNAIL);
            mVideoState = VIDEO_STATE_STOP;
            mPlayView.setVisibility(View.INVISIBLE);
        }
    };

    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            Log.d(TAG, "onPrepared ");
            iMediaPlayer.start();
        }
    };

    private void setVideoSource(String path)
    {
        Log.d(TAG, "setDataSource " + path);
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    PlayAllVideoFragment.SurfaceHolderFragment mSurfaceHolderFragmentListener = new PlayAllVideoFragment.SurfaceHolderFragment() {

        @Override
        public void OnFragmentSurfaceCreated(BrowserSurfaceView surfaceView) {
            Log.d(TAG, "OnFragmentSurfaceCreated");
            mediaPlayer = new IjkMediaPlayer();

            if (mOrientation == Configuration.ORIENTATION_PORTRAIT)
                surfaceView.setSurfaceViewSize(mScreenW, mScreenH);

            String path = mList.get(mPager.getCurrentItem());
            setVideoSource(path);

            mediaPlayer.setOnInfoListener(mOnInfoListener);
            mediaPlayer.setOnErrorListener(mOnErrorListener);
            mediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            mediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.prepareAsync();
        }

        @Override
        public void OnFragmentSurfaceDestroyed(BrowserSurfaceView mSurfaceView) {
            Log.d(TAG, "OnFragmentSurfaceDestroyed");
            if (mediaPlayer != null) {
                onStoped();
                mediaPlayer.stop();
                mediaPlayer.setDisplay(null);
                mediaPlayer.release();
                Log.d(TAG, "release player");
            }
        }
    };

    SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        int user_seekto = 0;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                user_seekto = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            float new_pos;
            if (user_seekto >= 99)
                new_pos = 0.99f;
            else
                new_pos = (float)user_seekto/100;
            mediaPlayer.seekTo((long) (mediaPlayer.getDuration()*new_pos));
            mTimeHandle.removeMessages(1);
            Message msg = mTimeHandle.obtainMessage(2);
            mTimeHandle.sendMessage(msg);
        }
    };
    ////player////end////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("henry","onCreate ");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playall);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenH = displaymetrics.heightPixels;
        mScreenW = displaymetrics.widthPixels;
        mOrientation = getResources().getConfiguration().orientation;
        mContext = this;
        initData();
        initHeaderBar();
        mCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<String>>() {
            @Override
            public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
                return new VideoLoader(mContext, TEST_VIDEO_FOLDER_NAME);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
                mList = data;
                initPager();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<String>> loader) {
            }
        };
        getSupportLoaderManager().restartLoader(477, null, mCallbacks);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        toolBarVisibleForVideo();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (mVideoState) {
                case VIDEO_STATE_FULL_PAUSE:
                    mVideoState = VIDEO_STATE_HALF_PAUSE;
                    break;
                case VIDEO_STATE_FULL_PLAY:
                    mVideoState = VIDEO_STATE_HALF_PLAY;
                    break;
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        mCallbacks = null;
        super.onDestroy();
    }

    private void initData() {
        mPosition = 0;

        mPlayView = (View)findViewById(R.id.play_icon);
        mPlayView.setOnClickListener(this);
        mDelete = (TextView) findViewById(R.id.playall_delete);
        mDelete.setOnClickListener(this);
        mDownload = (TextView) findViewById(R.id.playall_download);
        mDownload.setOnClickListener(this);
        mShare = (TextView) findViewById(R.id.playall_share);
        mShare.setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
        mTimeLineLayout = (LinearLayout) findViewById(R.id.video_timeline);

        mDelete.setVisibility(View.VISIBLE);
        mDownload.setVisibility(View.GONE);
        mShare.setVisibility(View.VISIBLE);
    }

    private void initHeaderBar() {
        mHeaderBar = (Toolbar) findViewById(R.id.playall_header_bar);
        mHeaderBar.setTitle("");
        mToolbarTitle = (TextView) mHeaderBar.findViewById(R.id.playall_toolbar_title);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
    }

    private void initPager() {
        mAdapter = new PlayAllAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager_play_all);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPosition);
        mPager.setOffscreenPageLimit(2);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int last_position = mPosition;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mToolbarTitle.setText(mList.get(position));
                ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(last_position)).changeState(SHOW_THUMBNAIL);
                last_position = position;

                ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(position)).setFragmentSurfaceListener(mSurfaceHolderFragmentListener);
                ((PlayAllVideoFragment) mAdapter.getRegisteredFragment(position)).setViewClickListener(mOnVideoClickListener);
                //((TextView) findViewById(R.id.video_total_time)).setText(setFormatTimes(mList.get(mPager.getCurrentItem()).duration));
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    mTimeLineLayout.setVisibility(View.INVISIBLE);
                    mBottomLayout.setVisibility(View.VISIBLE);
                } else {
                    mHeaderBar.setVisibility(View.INVISIBLE);
                    mTimeLineLayout.setVisibility(View.INVISIBLE);
                    mBottomLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void toolBarVisibleForVideo() {
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHeaderBar.setVisibility(View.INVISIBLE);
            mBottomLayout.setVisibility(View.INVISIBLE);
            mTimeLineLayout.setVisibility(View.INVISIBLE);
        } else {
            if(mVideoState != VIDEO_STATE_STOP ){
                mHeaderBar.setVisibility(View.VISIBLE);
                mBottomLayout.setVisibility(View.VISIBLE);
                mTimeLineLayout.setVisibility(View.VISIBLE);
            }else {
                mHeaderBar.setVisibility(View.VISIBLE);
                mBottomLayout.setVisibility(View.INVISIBLE);
                mTimeLineLayout.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mDelete)) {

        } else if (v.equals(mDownload)) {

        } else if (v.equals(mShare)) {

        } else if(v.equals(mPlayView) && (mAdapter.getRegisteredFragment(mPager.getCurrentItem()) instanceof PlayAllVideoFragment)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mPlayView.setBackgroundResource(R.mipmap.ic_playback_resume);
                mVideoState = VIDEO_STATE_HALF_PAUSE;
            } else {
                mediaPlayer.start();
                onPlaying();
                mVideoState = VIDEO_STATE_HALF_PLAY;
            }
        }
    }

    private String setFormatTimes(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public class PlayAllAdapter extends FragmentStatePagerAdapter {
        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public PlayAllAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            String path = mList.get(position);
            if (mPager.getCurrentItem() == position) {//when current page create or re-create after delete
                mToolbarTitle.setText(mList.get(position));
                mTimeLineLayout.setVisibility(View.INVISIBLE);
                mBottomLayout.setVisibility(View.VISIBLE);
            }
            PlayAllVideoFragment fragment = PlayAllVideoFragment.init(position, true, mScreenW, mOrientation);//isShowImage = true
            new BackgroundFragmentTask(fragment.mOnFragmentTaskCompleted).execute(path);
            return fragment;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);                
            super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        // Register the fragment when the item is instantiated
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            if (position == mPager.getCurrentItem()) {
                ((PlayAllVideoFragment) fragment).setViewClickListener(mOnVideoClickListener);
                ((PlayAllVideoFragment) fragment).setFragmentSurfaceListener(mSurfaceHolderFragmentListener);
            }
            return fragment;
        }

        // Returns the fragment for the position (if instantiated)
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    class BackgroundFragmentTask extends AsyncTask<String, Void, Bitmap> {
        public OnFragmentTaskCompleted listener;
        private Boolean isNotCached = false;
        private String path;

        public BackgroundFragmentTask(OnFragmentTaskCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onTaskStarted();
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... arg) {
            path = arg[0];
            if (AppApplication.getThumbnailsCache(mContext).get(path) == null) {
                isNotCached = true;
                return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            return AppApplication.getThumbnailsCache(mContext).get(path);
        }
        @Override
        protected void onPostExecute(Bitmap thumbnail) {
            if (isNotCached) {
                if (thumbnail == null)
                    Log.d(TAG, "bitmap is null");
                else
                    AppApplication.getThumbnailsCache(mContext).put(path, thumbnail);
            }
            listener.onTaskCompleted(thumbnail);
        }
    }

    public void beginTimer() {
        Message msg = mTimeHandle.obtainMessage(1);
        mTimeHandle.sendMessage(msg);
    }

    public void endTimer() {
        Message msg = mTimeHandle.obtainMessage(0);
        mTimeHandle.sendMessage(msg);
        mTimeHandle.removeMessages(1);
    }

    private Handler mTimeHandle = new Handler() {

        long duration = 0;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://End reach
                    ((TextView)findViewById(R.id.video_current_time)).setText(setFormatTimes(0));
                    mSeekBar.setProgress(0+1);
                    break;
                case 1:// update time
                    duration = mediaPlayer.getDuration();
                    if (mediaPlayer.isPlaying()) {
                        ((TextView)findViewById(R.id.video_current_time)).setText(setFormatTimes((int)mediaPlayer.getCurrentPosition()));
                        int pos = (int)(100*mediaPlayer.getCurrentPosition()) / (int)duration;
                        mSeekBar.setProgress(pos+1);
                        if (mediaPlayer.getCurrentPosition() > duration) {
                            endTimer();
                            break;
                        }
                    }
                    sendMessageDelayed(obtainMessage(1), 1000);
                    break;
                case 2://update time for seekto
                    ((TextView)findViewById(R.id.video_current_time)).setText(setFormatTimes((int)mediaPlayer.getCurrentPosition()));
                    mTimeHandle.removeMessages(1);
                    //waiting 2000 for player seek to the time
                    sendMessageDelayed(obtainMessage(1), 2000);
            }
        }
    };
}
