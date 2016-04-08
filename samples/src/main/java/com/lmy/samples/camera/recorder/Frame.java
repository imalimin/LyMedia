package com.lmy.samples.camera.recorder;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by 李明艺 on 2016/3/1.
 *
 * @author lrlmy@foxmail.com
 */
public class Frame {
    public opencv_core.IplImage image;
    public long frameTimeMillis;
    public long frameNanoTime;

    public Frame(opencv_core.IplImage img, long timeMill, long timeNano) {
        image = img;
        frameTimeMillis = timeMill;
        frameNanoTime = timeNano;
    }
}
