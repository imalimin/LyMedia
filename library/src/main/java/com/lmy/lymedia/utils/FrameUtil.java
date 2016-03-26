package com.lmy.lymedia.utils;

import android.util.Log;

import org.bytedeco.javacv.Frame;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
            tmp = new Frame(frame.imageWidth, frame.imageHeight, frame.imageDepth, frame.imageChannels);
            tmp.image = new Buffer[1];
            ByteBuffer in = (ByteBuffer) frame.image[0].position(0);
            tmp.image[0] = copy(in);
        } else {
            tmp.samples = new Buffer[frame.samples.length];
            for (int i = 0; i < tmp.samples.length; i++)
                tmp.samples[i] = copy((FloatBuffer) frame.samples[i].position(0));
        }
        return tmp;
    }

    public static ByteBuffer copy(ByteBuffer src) {
        byte[] data = new byte[src.capacity()];
        src.get(data);
        return ByteBuffer.wrap(data);
    }

    public static FloatBuffer copy(FloatBuffer src) {
        float[] data = new float[src.capacity()];
        src.get(data);
        return FloatBuffer.wrap(data);
    }
}
