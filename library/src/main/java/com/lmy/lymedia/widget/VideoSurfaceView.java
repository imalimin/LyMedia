package com.lmy.lymedia.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lmy.lymedia.media.FFmpegPlayer;
import com.lmy.lymedia.media.render.Filter;

/**
 * Created by Administrator on 2016/3/21.
 */
public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private FFmpegPlayer mPlayer;
    private OnPreparedListener onPreparedListener;

    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    public void initPlayer(String path) {
        mPlayer = FFmpegPlayer.create(getHolder(), path);
        mPlayer.setLooping(true);
    }

    public void play() {
        mPlayer.play();
    }

    public void setFilter(Filter filter) {
        mPlayer.setFilter(filter);
    }

    private void initLayout(int width, int height) {
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        float scale = width / (float) height;
        getLayoutParams().height = (int) (display.getWidth() / scale);
        requestLayout();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("SurfaceView", "surfaceCreated...");
        initLayout(mPlayer.getWidth(), mPlayer.getHeight());
        play();
        if (onPreparedListener != null)
            onPreparedListener.onPrepared();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("SurfaceView", "surfaceChanged...");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("SurfaceView", "surfaceDestroyed...");
        mPlayer.pause();
    }

    public void releasePlaer() {
        mPlayer.stop();
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public interface OnPreparedListener {
        void onPrepared();
    }
}
