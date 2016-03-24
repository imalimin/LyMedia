package com.lmy.lymedia.utils;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/3/21.
 */
public class ImageUtil {
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
}
