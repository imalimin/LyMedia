package com.lmy.samples.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.lmy.samples.R;
import com.lmy.samples.camera.MyCameraGLSurfaceView;

public class VideoRecordActivity extends AppCompatActivity {
    private MyCameraGLSurfaceView mGLSurfaceView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        initView();
    }

    private void initView() {
        mGLSurfaceView = (MyCameraGLSurfaceView) findViewById(R.id.glview);
        mButton = (Button) findViewById(R.id.button);

        mGLSurfaceView.setFitVideoSize(true);
        mGLSurfaceView.initecoder();
        mButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction() && !mGLSurfaceView.isRecording())
                    mGLSurfaceView.startRecording();
                else if (MotionEvent.ACTION_UP == event.getAction() && mGLSurfaceView.isRecording())
                    mGLSurfaceView.pauseRecording();
                return false;
            }
        });
    }

    private void waitForEncoding(final boolean shouldFinish) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mGLSurfaceView.getRecoderManager().encodeCompeleted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (shouldFinish) {
                            finish();
                        } else {
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mGLSurfaceView.getRecoderManager().isStarting() && !mGLSurfaceView.getRecoderManager().encodeCompeleted()) {
                waitForEncoding(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.stopRecording();
    }
}
