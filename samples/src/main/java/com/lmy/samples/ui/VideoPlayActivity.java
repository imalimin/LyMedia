package com.lmy.samples.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lmy.lymedia.media.render.AcvFilter;
import com.lmy.lymedia.utils.Util;
import com.lmy.lymedia.widget.VideoSurfaceView;
import com.lmy.samples.R;

public class VideoPlayActivity extends AppCompatActivity {
    private VideoSurfaceView mSurfaceView;
    private Button oneBtn;
    private Button twoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mSurfaceView = (VideoSurfaceView) findViewById(R.id.surface);
        oneBtn = (Button) findViewById(R.id.button1);
        twoBtn = (Button) findViewById(R.id.button2);
        mSurfaceView.initPlayer(Util.getSdcardPath() + "/test.f4v");
        mSurfaceView.setFilter(new AcvFilter(Util.getSdcardPath() + "/test_filters/FA_Curves3.acv"));
        mSurfaceView.play();
        oneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurfaceView.releasePlaer();
                mSurfaceView.initPlayer(Util.getSdcardPath() + "/test.mp4");
            }
        });
        twoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurfaceView.releasePlaer();
                mSurfaceView.initPlayer(Util.getSdcardPath() + "/test.f4v");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceView.releasePlaer();
    }
}
