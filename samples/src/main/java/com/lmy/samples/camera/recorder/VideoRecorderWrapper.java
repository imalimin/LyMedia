package com.lmy.samples.camera.recorder;

import android.os.Build;
import android.util.Log;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

import java.nio.Buffer;
import java.nio.ShortBuffer;

/**
 * Created by 李明艺 on 2016/3/1.
 *
 * @author lrlmy@foxmail.com
 */
public class VideoRecorderWrapper implements AudioRecorder.DateFeedback {
    //视频文件宽高
    private int videoWidth = -1, videoHeight = -1;
    //是否正在录制
    private boolean recording = false;
    //是否已经开始录制
    private boolean starting = false;
    //帧率
    private int frameRate = 30;
    private AudioRecorder audioRecorder;

    private FFmpegFrameRecorder mFrameEncoder;
    private String path;

//    static {
//        System.loadLibrary("checkneon");
//    }
//
//    public native static int checkNeonFromJNI();

    public VideoRecorderWrapper(int videoWidth, int videoHeight, int frameRate, String path) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.frameRate = frameRate;
        this.path = path;
        init();
    }

    private void init() {
        audioRecorder = new AudioRecorder();
        audioRecorder.setDateFeedback(this);
        cacheFrame = new org.bytedeco.javacv.Frame(videoWidth, videoHeight, org.bytedeco.javacv.Frame.DEPTH_UBYTE, 4);
        initVideoRecorder(videoWidth, videoHeight);
    }

    public void initVideoRecorder(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;
//        String path = Util.getSdcardPath() + "/lava/" + System.currentTimeMillis() + ".mp4";
        RecorderParameters recorderParameters = Util.getRecorderParameter(CONSTANTS.RESOLUTION_MEDIUM_VALUE);
        recorderParameters.setVideoFrameRate(frameRate);
        //TODO 视频录制分辨率
        try {
            mFrameEncoder = FFmpegFrameRecorder.createDefault(path, width, height);
            mFrameEncoder.setFormat(recorderParameters.getVideoOutputFormat());
            mFrameEncoder.setSampleRate(recorderParameters.getAudioSamplingRate());
            mFrameEncoder.setFrameRate(recorderParameters.getVideoFrameRate());
            mFrameEncoder.setVideoCodec(recorderParameters.getVideoCodec());
            mFrameEncoder.setVideoQuality(recorderParameters.getVideoQuality());
            mFrameEncoder.setAudioQuality(recorderParameters.getVideoQuality());
            mFrameEncoder.setAudioCodec(recorderParameters.getAudioCodec());
            mFrameEncoder.setVideoBitrate(recorderParameters.getVideoBitrate());
            mFrameEncoder.setAudioBitrate(recorderParameters.getAudioBitrate());
            mFrameEncoder.setAudioChannels(recorderParameters.getAudioChannel());
            mFrameEncoder.setImageWidth(width);
            mFrameEncoder.setImageHeight(height);
            mFrameEncoder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        Log.v("000", "initVideoRecorder width=" + width + ", height=" + height);
    }

    private org.bytedeco.javacv.Frame cacheFrame;

    public void write(Frame frame) {
        mFrameEncoder.setTimestamp(frame.frameTimeMillis);
        try {
            cacheFrame.image[0] = frame.image.getByteBuffer();
            mFrameEncoder.record(cacheFrame);
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        recording = true;
        if (!starting) {
            starting = true;
        }
        audioRecorder.startRecording();
    }

    public void pauseRecording() {
        recording = false;
        audioRecorder.pauseRecording();
    }

    public void stopRecording() {
        recording = false;
        audioRecorder.stopRecording();
        release();
    }

    public void release() {
        if (mFrameEncoder != null) {
            try {
                mFrameEncoder.stop();
                mFrameEncoder.release();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
            mFrameEncoder = null;
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean isStarting() {
        return starting;
    }

    @Override
    public void feedback(ShortBuffer buffer) {
        try {
            mFrameEncoder.recordSamples(new Buffer[]{buffer});
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void setTimestampUpdate(AudioRecorder.TimestampUpdate timestampUpdate) {
        if (audioRecorder != null)
            audioRecorder.setTimestampUpdate(timestampUpdate);
    }

    public static class RecorderParameters {
        private static boolean AAC_SUPPORTED = Build.VERSION.SDK_INT >= 10;
        //        private int videoCodec = avcodec.AV_CODEC_ID_H264;
        private int videoCodec = avcodec.AV_CODEC_ID_MPEG4;
        private int videoFrameRate = 30;
        //private int videoBitrate = 500 *1000;
        private int videoQuality = 2;
        private int audioCodec = AAC_SUPPORTED ? avcodec.AV_CODEC_ID_AAC : avcodec.AV_CODEC_ID_AMR_NB;
        private int audioChannel = 1;
        private int audioBitrate = 96000;//192000;//AAC_SUPPORTED ? 96000 : 12200;
        private int videoBitrate = 3500000;
        private int audioSamplingRate = AAC_SUPPORTED ? 44100 : 8000;
        private String videoOutputFormat = AAC_SUPPORTED ? "mp4" : "3gp";


        public static boolean isAAC_SUPPORTED() {
            return AAC_SUPPORTED;
        }

        public static void setAAC_SUPPORTED(boolean aAC_SUPPORTED) {
            AAC_SUPPORTED = aAC_SUPPORTED;
        }

        public String getVideoOutputFormat() {
            return videoOutputFormat;
        }

        public void setVideoOutputFormat(String videoOutputFormat) {
            this.videoOutputFormat = videoOutputFormat;
        }

        public int getAudioSamplingRate() {
            return audioSamplingRate;
        }

        public void setAudioSamplingRate(int audioSamplingRate) {
            this.audioSamplingRate = audioSamplingRate;
        }

        public int getVideoCodec() {
            return videoCodec;
        }

        public void setVideoCodec(int videoCodec) {
            this.videoCodec = videoCodec;
        }

        public int getVideoFrameRate() {
            return videoFrameRate;
        }

        public void setVideoFrameRate(int videoFrameRate) {
            this.videoFrameRate = videoFrameRate;
        }


        public int getVideoQuality() {
            return videoQuality;
        }

        public void setVideoQuality(int videoQuality) {
            this.videoQuality = videoQuality;
        }

        public int getAudioCodec() {
            return audioCodec;
        }

        public void setAudioCodec(int audioCodec) {
            this.audioCodec = audioCodec;
        }

        public int getAudioChannel() {
            return audioChannel;
        }

        public void setAudioChannel(int audioChannel) {
            this.audioChannel = audioChannel;
        }

        public int getAudioBitrate() {
            return audioBitrate;
        }

        public void setAudioBitrate(int audioBitrate) {
            this.audioBitrate = audioBitrate;
        }

        public int getVideoBitrate() {
            return videoBitrate;
        }

        public void setVideoBitrate(int videoBitrate) {
            this.videoBitrate = videoBitrate;
        }
    }
}
