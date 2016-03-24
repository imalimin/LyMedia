package com.lmy.samples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lmy.lymedia.widget.VideoSurfaceView;

public class MainActivity extends AppCompatActivity {
    private VideoSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (VideoSurfaceView) findViewById(R.id.surface);
    }
}
