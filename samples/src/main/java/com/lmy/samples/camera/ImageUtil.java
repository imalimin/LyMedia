package com.lmy.samples.camera;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 李明艺 on 2016/2/29.
 *
 * @author lrlmy@foxmail.com
 */
public class ImageUtil {
    public static final String TAG = "ImageUtil";
    public static final File parentPath = Environment.getExternalStorageDirectory();
    public static String storagePath = null;
    public static final String DST_FOLDER = "lava";

    public static String getPath() {
        if (storagePath == null) {
            storagePath = parentPath.getAbsolutePath() + "/" + DST_FOLDER;
            File file = new File(storagePath);
            if (!file.exists()) {
                file.mkdir();
            }
        }

        return storagePath;
    }

    public static void saveBitmap(Bitmap bmp) {
        String path = getPath();
        long currentTime = System.currentTimeMillis();
        String filename = path + "/" + currentTime + ".jpg";
        saveBitmap(bmp, filename);
    }

    public static void saveBitmap(Bitmap bmp, String filename) {
        Log.i(TAG, "saving Bitmap : " + filename);
        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream);
            bufferOutStream.flush();
            bufferOutStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Err when saving bitmap...");
            e.printStackTrace();
            return;
        }
        Log.i(TAG, "Bitmap " + filename + " saved!");
    }
}
