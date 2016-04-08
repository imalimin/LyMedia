package com.lmy.lymedia.media.render;

import android.util.Log;

import com.lmy.lymedia.utils.Util;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;

/**
 * Created by lmy on 2016/4/8.
 */
public class AcvFilter extends FFmpegFilter {
    private final static String FILTER = "curves=psfile='%s'";
    private FFmpegFrameFilter mFilter;

    public AcvFilter(String filter) {
        super(filter);
    }

    @Override
    public Frame filter(Frame frame) {
        try {
            mFilter.push(frame);
            while ((frame = mFilter.pull()) == null) {
                Log.v("AcvFilter", "wait render");
            }
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
        return frame;
    }

    @Override
    public void onStart() {
        mFilter = new FFmpegFrameFilter(String.format(FILTER, filter), width, height);
        mFilter.setPixelFormat(AV_PIX_FMT_RGBA);
        try {
            mFilter.start();
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
    }

}
