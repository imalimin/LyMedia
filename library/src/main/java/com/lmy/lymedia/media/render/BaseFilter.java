package com.lmy.lymedia.media.render;

/**
 * Created by lmy on 2016/4/8.
 */
public abstract class BaseFilter implements Filter {
    protected int width, height;

    @Override
    public void onCreate(int width, int height) {
        this.width = width;
        this.height = height;
        onStart();
    }
}
