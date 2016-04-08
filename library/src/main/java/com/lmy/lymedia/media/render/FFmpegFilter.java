package com.lmy.lymedia.media.render;

import org.bytedeco.javacv.FFmpegFrameFilter;

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
    public void onStart() {

    }
}
