package com.lmy.lymedia.media.render;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/3/28.
 */
public class OldFilter implements Filter {
    private AndroidFrameConverter mFrameConverter;
    private OpenCVFrameConverter.ToIplImage mCVFrameConverter;
    private Bitmap mBitmap;
    private opencv_core.IplImage mIplImage;

    public OldFilter() {
        mFrameConverter = new AndroidFrameConverter();
        mCVFrameConverter = new OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public Frame filter(Frame frame) {
//        mBitmap = mFrameConverter.convert(frame);
//        mBitmap = filter(mBitmap);
//        return mFrameConverter.convert(mBitmap);
        mIplImage = mCVFrameConverter.convert(frame);
        mIplImage = filter(mIplImage);
        return mCVFrameConverter.convert(mIplImage);
    }

    private opencv_core.IplImage filter(opencv_core.IplImage image) {
        float[] value = new float[]{0.393f, 0.769f, 0.189f, 0.349f, 0.686f, 0.168f, 0.272f, 0.534f, 0.131f};
        return filter(image, value);
    }

    private opencv_core.IplImage filter(opencv_core.IplImage image, float[] value) {
        ByteBuffer mBuffer = (ByteBuffer) image.getByteBuffer().position(0);
        byte pixR = 0;
        byte pixG = 0;
        byte pixB = 0;
        for (int i = 0; i < mBuffer.capacity() / image.nChannels(); i++) {
            pixR = mBuffer.get(i * 3);
            pixG = mBuffer.get(i * 3 + 1);
            pixB = mBuffer.get(i * 3 + 2);
            mBuffer.put(i * 3, (byte) (value[0] * pixR + value[1] * pixG + value[2] * pixB));
            mBuffer.put(i * 3 + 1, (byte) (value[0] * pixR + value[1] * pixG + value[2] * pixB));
            mBuffer.put(i * 3 + 2, (byte) (value[0] * pixR + value[1] * pixG + value[2] * pixB));
        }
        image.getByteBuffer().put(mBuffer);
        return image;
    }

    private Bitmap filter(Bitmap bmp) {
        float[] value = new float[]{0.393f, 0.769f, 0.189f, 0.349f, 0.686f, 0.168f, 0.272f, 0.534f, 0.131f};
        return filter(bmp, value);
    }

    private Bitmap filter(Bitmap bmp, float[] value) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (value[0] * pixR + value[1] * pixG + value[2] * pixB);
                newG = (int) (value[3] * pixR + value[4] * pixG + value[5] * pixB);
                newB = (int) (value[6] * pixR + value[7] * pixG + value[8] * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
