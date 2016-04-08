package com.lmy.samples.camera.recorder;

import org.bytedeco.javacpp.opencv_core;

import static org.bytedeco.javacpp.opencv_core.cvCreateImage;

/**
 * Created by 李明艺 on 2016/3/1.
 *
 * @author lrlmy@foxmail.com
 */
public class Frame {
    public opencv_core.IplImage image;
    public long frameTimeMillis;
    public long frameNanoTime;

    public static Frame create(int width, int height, int depth, int channels) {
        return new Frame(cvCreateImage(new opencv_core.CvSize(width, height), depth, channels), 0, 0);
    }

    public Frame(opencv_core.IplImage img, long timeMill, long timeNano) {
        image = img;
        frameTimeMillis = timeMill;
        frameNanoTime = timeNano;
    }
}
