package com.lmy.samples.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lmy.lymedia.utils.Util;
import com.lmy.lymedia.widget.VideoSurfaceView;
import com.lmy.samples.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intiView();
    }

    private void intiView() {
        findViewById(R.id.play).setOnClickListener(onClickListener);
        findViewById(R.id.record).setOnClickListener(onClickListener);
        findViewById(R.id.render).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.play:
                    startActivity(new Intent(MainActivity.this, VideoPlayActivity.class));
                    break;
                case R.id.record:
                    startActivity(new Intent(MainActivity.this, VideoRecordActivity.class));
                    break;
                case R.id.render:
                    startActivity(new Intent(MainActivity.this, RenderActivity2.class));
                    break;
            }
        }
    };
}
