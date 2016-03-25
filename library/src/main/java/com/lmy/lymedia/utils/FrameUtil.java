package com.lmy.lymedia.utils;

import android.util.Log;

import org.bytedeco.javacv.Frame;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/3/21.
 */
public class FrameUtil {
    public static int[] RGB242RGB565(int[] rgb) {
        rgb[0] = (rgb[0] << 8) & 0xF800;
        rgb[1] = (rgb[1] << 3) & 0x7E0;
        rgb[2] = rgb[2] >> 3;
        return rgb;
    }

    public static ByteBuffer RGB242RGB565(ByteBuffer buffer, int lenght) {
        for (int i = 0; i < lenght; i++) {
            if (i % 3 == 0)
                buffer.put(i, (byte) ((buffer.get(i) << 8) & 0xF800));
            if (i % 3 == 1)
                buffer.put(i, (byte) ((buffer.get(i) << 3) & 0x7E0));
            if (i % 3 == 2)
                buffer.put(i, (byte) (buffer.get(i) >> 3));
        }
        return buffer;
    }


    public static int frameType(Frame frame) {
        if (frame == null) return -1;
        if (frame.image != null) return 0;
        else if (frame.samples != null) return 1;
        else return -1;
    }

    public static Frame copy(Frame frame) {
        Frame tmp = new Frame();
        int type = frameType(frame);
        if (type == 0) {
//            int width = frame.imageWidth;
//            int height = frame.imageHeight;
//            tmp = new Frame(width, height, frame.imageDepth, frame.imageChannels);
//            tmp.image = new Buffer[1];
//            ByteBuffer in = (ByteBuffer) frame.image[0];
//            in.position(0);
//            ByteBuffer buffer = ByteBuffer.allocate(in.capacity());
//
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    if (x == width-1)
//                        x = width-1;
//                    int i0 = y  + 3 * x;
//                    int i1 = y  + 3 * x + 1;
//                    int i2 = y  + 3 * x + 2;
//                    byte r = in.get(i0);
//                    byte g = in.get(i1);
//                    byte b = in.get(i2);
//                    buffer.put(i0, r);
//                    buffer.put(i0, g);
//                    buffer.put(i2, b);
//                }
//            }
//            tmp.image[0] = buffer.position(0);
            tmp = new Frame(frame.imageWidth, frame.imageHeight, frame.imageDepth, frame.imageChannels);
            tmp.image = frame.image.clone();
        } else {
            tmp.samples = frame.samples.clone();
        }
        return tmp;
    }
}
