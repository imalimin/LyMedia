package com.lmy.lymedia.media.render;

/**
 * Created by lmy on 2016/4/8.
 */
public abstract class BaseFilter implements Filter {
    protected boolean starting = false;
    protected int width, height;

    @Override
    public void onCreate(int width, int height) {
        this.width = width;
        this.height = height;
        onStart();
        this.starting = true;
    }

    @Override
    public boolean isStarting() {
        return starting;
    }
}
