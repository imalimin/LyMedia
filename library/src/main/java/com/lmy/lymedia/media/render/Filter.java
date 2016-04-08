package com.lmy.lymedia.media.render;

import org.bytedeco.javacv.Frame;

/**
 * Created by lmy on 2016/4/8.
 */
public interface Filter {
    Frame filter(Frame frame);

    void onStart();
    void onStop();

    void onCreate(int width, int height);
}
