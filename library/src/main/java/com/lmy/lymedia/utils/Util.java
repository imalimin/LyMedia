package com.lmy.lymedia.utils;

/**
 * Created by 李明艺 on 2016/3/21.
 *
 * @author lrlmy@foxmail.com
 */
public class Util {

    public static String getSdcardPath() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return "/storage/sdcard0";
        }
    }
}
