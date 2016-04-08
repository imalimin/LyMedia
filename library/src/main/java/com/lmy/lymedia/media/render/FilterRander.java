package com.lmy.lymedia.media.render;

import android.util.Log;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

/**
 * Created by lifeix on 2016/4/8.
 */
public class FilterRander implements Render {
    private FFmpegFrameFilter mFilter;

    public FilterRander(int width, int height) {
        mFilter = new FFmpegFrameFilter("transpose=clock", width, height);
        try {
            mFilter.start();
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Frame render(Frame frame) {
        try {
            mFilter.push(frame);
            while ((frame = mFilter.pull()) != null) {
                Log.v("FilterRander", "wait render");
            }
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
        return frame;
    }
}
