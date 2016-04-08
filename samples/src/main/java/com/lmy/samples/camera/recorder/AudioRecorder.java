package com.lmy.samples.camera.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ShortBuffer;

/**
 * Created by 李明艺 on 2015/12/29.
 *
 * @author 李明艺
 */
public class AudioRecorder {
    //调用系统的录制音频类
    private AudioRecord audioRecord;
    //录制音频的线程
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    //开启和停止录制音频的标记
    volatile boolean runAudioThread = true;

    //音频的采样率，recorderParameters中会有默认值
    private int sampleRate = 44100;
    //音频时间戳
    private volatile long mAudioTimestamp = 0L;
    private volatile long mAudioTimeRecorded;
    private boolean recording = false;

    private DateFeedback dateFeedback;
    private TimestampUpdate timestampUpdate;

    public AudioRecorder() {
        init();
    }

    private void init() {
        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
        audioThread.start();
    }

    /**
     * 录制音频的线程
     *
     * @author QD
     */
    class AudioRecordRunnable implements Runnable {
        int bufferSize;
        short[] audioData;
        int bufferReadResult;
        private final AudioRecord audioRecord;
        public volatile boolean isInitialized;
        private int mCount = 0;

        private AudioRecordRunnable() {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioData = new short[bufferSize];
        }

        /**
         * shortBuffer包含了音频的数据和起始位置
         *
         * @param shortBuffer
         */
        private void record(ShortBuffer shortBuffer) {
            this.mCount += shortBuffer.limit();
            feedback(shortBuffer);
        }

        /**
         * 更新音频的时间戳
         */
        private void updateTimestamp() {
            int i = Util.getTimeStampInNsFromSampleCounted(this.mCount);
            if (mAudioTimestamp != i) {
                mAudioTimestamp = i;
                mAudioTimeRecorded = System.nanoTime();
                timestampUpdate(mAudioTimestamp, mAudioTimeRecorded);
            }
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            this.isInitialized = false;
            if (audioRecord != null) {
                //判断音频录制是否被初始化
                while (this.audioRecord.getState() == 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException localInterruptedException) {
                    }
                }
                this.isInitialized = true;
                this.audioRecord.startRecording();
                while (runAudioThread) {
                    updateTimestamp();
                    bufferReadResult = this.audioRecord.read(audioData, 0, audioData.length);
                    if (recording && bufferReadResult > 0)
                        record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
                }
                this.audioRecord.stop();
                this.audioRecord.release();
            }
        }
    }

    public void startRecording() {
        recording = true;
    }

    public void pauseRecording() {
        recording = false;
    }

    public void stopRecording() {
        recording = false;
        runAudioThread = false;
    }

    public void destroy() {
        if (this.audioRecord != null) {
            this.audioRecord.stop();
            this.audioRecord.release();
            this.audioRecord = null;
        }
    }

    public void feedback(ShortBuffer buffer) {
        if (dateFeedback == null) return;
        dateFeedback.feedback(buffer);
    }

    public void timestampUpdate(long timestamp, long timeRecorded) {
        if (timestampUpdate == null) return;
        timestampUpdate.update(timestamp, timeRecorded);
    }

    public DateFeedback getDateFeedback() {
        return dateFeedback;
    }

    public void setDateFeedback(DateFeedback dateFeedback) {
        this.dateFeedback = dateFeedback;
    }

    public TimestampUpdate getTimestampUpdate() {
        return timestampUpdate;
    }

    public void setTimestampUpdate(TimestampUpdate timestampUpdate) {
        this.timestampUpdate = timestampUpdate;
    }

    public interface DateFeedback {
        void feedback(ShortBuffer buffer);
    }

    public interface TimestampUpdate {
        void update(long timestamp, long timeRecorded);
    }
}
