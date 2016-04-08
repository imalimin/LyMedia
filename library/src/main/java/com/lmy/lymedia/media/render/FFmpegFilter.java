package com.lmy.lymedia.media.render;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FrameFilter;

/**
 * Created by lmy on 2016/4/8.
 */
public abstract class FFmpegFilter extends BaseFilter {
    private FFmpegFrameFilter mFilter;
    protected String filter;

    public FFmpegFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public void onStop() {
        starting = false;
        try {
            if (mFilter != null) {
                mFilter.stop();
                mFilter.release();
                mFilter = null;
            }
        } catch (FrameFilter.Exception e) {
            e.printStackTrace();
        }
    }
}
