package com.lmy.lymedia.media.render;

import android.util.Log;

import com.lmy.lymedia.utils.Util;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;

/**
 * Created by lifeix on 2016/4/8.
 */
public class FilterRander implements Render {
    private FFmpegFrameFilter mFilter;

    public FilterRander(int width, int height) {
        mFilter = new FFmpegFrameFilter("curves=psfile='" + Util.getSdcardPath() + "/FA_Curves2.acv'", width, height);
        mFilter.setPixelFormat(AV_PIX_FMT_RGBA);
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
            while ((frame = mFilter.pull()) == null) {
                Log.v("FilterRander", "wait render");
            }
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
        return frame;
    }
}
