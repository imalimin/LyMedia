package com.lmy.samples.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lmy.lymedia.media.VideoRender;
import com.lmy.lymedia.media.render.FilterRander;
import com.lmy.lymedia.media.render.OldRender;
import com.lmy.lymedia.utils.Util;
import com.lmy.samples.R;

public class RenderActivity2 extends AppCompatActivity {
    private Button mStartBtn;
    private TextView mTextView;
    private VideoRender mVideoRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render2);
        initView();
    }

    private void initView() {
        mStartBtn = (Button) findViewById(R.id.start);
        mTextView = (TextView) findViewById(R.id.text);
        mVideoRender = new VideoRender(Util.getSdcardPath() + "/test.mp4", Util.getSdcardPath() + "/test_render.mp4");
        if (mVideoRender.init()) {
            mVideoRender.setRender(new FilterRander(mVideoRender.getWidth(), mVideoRender.getHeight()));
            mVideoRender.setRenderListener(new VideoRender.RenderListener() {
                @Override
                public void onProgress(int progress) {
                    mTextView.setText(String.valueOf(progress));
                }
            });
            mStartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoRender.start();
                }
            });
        } else {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoRender != null)
            mVideoRender.stop();
    }
}
