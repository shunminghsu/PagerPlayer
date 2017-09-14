package com.project.henry_hsu.pagerplayer;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by henry_hsu on 2017/2/10.
 */

public class VideoLoader extends AsyncTaskLoader<ArrayList<String>> {
    private ArrayList<String> mFileList;
    private Context mContext;
    private String mQueryText;


    public VideoLoader(Context context, String query_text) {
        super(context);
        mQueryText = query_text;
        mFileList = new ArrayList<>();
        mContext = context;
    }

    @Override
    public ArrayList<String> loadInBackground() {
        mFileList.clear();
        return getAllVideos(mQueryText);
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged() || mFileList.size() == 0)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();
    }

    private ArrayList<String> getAllVideos(String testVideoFolderName) {
        try {
            String[] videoTypes = {MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_MODIFIED};

            Cursor videocursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoTypes, null, null, null);

            if (videocursor != null) {

                while (videocursor.moveToNext()) {
                    int pathColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    int nameColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                    int timeColumnIndex = videocursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);

                    String videoPath = videocursor.getString(pathColumnIndex);
                    String videoName = videocursor.getString(nameColumnIndex);
                    Long videoTime = 1000 * videocursor.getLong(timeColumnIndex);

                    File videoFile = new File(videoPath);
                    if (videoFile.exists()) {
                        if (testVideoFolderName == "")
                            mFileList.add(videoPath);
                        else if (videoPath.contains(testVideoFolderName)) {
                            mFileList.add(videoPath);
                        }

                    }
                }
            }
            videocursor.close();
            return mFileList;
        } catch (Exception e) {
            e.printStackTrace();
            return mFileList;
        }
    }
}
