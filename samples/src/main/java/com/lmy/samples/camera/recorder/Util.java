package com.lmy.samples.camera.recorder;

/**
 * Created by 李明艺 on 2016/3/1.
 *
 * @author lrlmy@foxmail.com
 */
public class Util {
    public static int getTimeStampInNsFromSampleCounted(int paramInt) {
        return (int) (paramInt / 0.0441D);
    }

    public static String root() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return "/storage/sdcard0";
    }

    public static VideoRecorderWrapper.RecorderParameters getRecorderParameter(int currentResolution) {
        VideoRecorderWrapper.RecorderParameters parameters = new VideoRecorderWrapper.RecorderParameters();
        if (currentResolution == CONSTANTS.RESOLUTION_HIGH_VALUE) {
            parameters.setAudioBitrate(128000);
            parameters.setVideoQuality(1);
        } else if (currentResolution == CONSTANTS.RESOLUTION_MEDIUM_VALUE) {
            parameters.setAudioBitrate(128000);
            parameters.setVideoQuality(10);
        } else if (currentResolution == CONSTANTS.RESOLUTION_LOW_VALUE) {
            parameters.setAudioBitrate(96000);
            parameters.setVideoQuality(20);
        }
        return parameters;
    }
}
