package com.lmy.samples.camera.recorder;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;

import java.util.LinkedList;
import java.util.Queue;

import static org.bytedeco.javacpp.opencv_core.cvCreateImage;

/**
 * Created by 李明艺 on 2016/3/2.
 *
 * @author lrlmy@foxmail.com
 *         视频录制缓存层封装
 */
public class VideoRecordManager {
    private final static String TAG = "VideoRecordManager";
    private static final int MAX_CACHED_FRAMES = 15;
    private static final int MAX_ENCODE_FRAMES = 5;
    private final static int RECORDING_PROGRESS = 0x0001;
    private final static int RECORDING_CONTROL = 0x0002;
    private int taskPoolSize = 0;
    public int[] mRecordStateLock = new int[0];
    public boolean mShouldRecord = false;
    private LinkedList<Frame> mImageList;//帧回收队列
    private Queue<Frame> mFrameQueue;//帧队列

    public RecordingThread mRecordingThread;
    private VideoRecorderWrapper mVideoRecorder;
    public int mWidth = 1080, mHeight = 720;
    private String path;
    private EncodeListener encodeListener;
    private RecordingLintener recordingLintener;
    private boolean hasInit = false;

    public VideoRecordManager(int width, int height, String path) {
        this.mWidth = width;
        this.mHeight = height;
        this.path = path;
        init();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RECORDING_PROGRESS) {
                if (recordingLintener != null)
                    recordingLintener.onProgress(msg.arg1);
            } else if (msg.what == RECORDING_CONTROL) {
                if (recordingLintener != null)
                    recordingLintener.onControl(msg.arg1);
            }
        }
    };

    public void init() {
        this.hasInit = false;
        if (mVideoRecorder != null) {
            mVideoRecorder.stopRecording();
            mVideoRecorder = null;
        }
        initCache();
        mRecordingThread = new RecordingThread();
        mRecordingThread.start();
        //NOTE 初始化录像器
        mVideoRecorder = new VideoRecorderWrapper(mWidth, mHeight, 30, path);

    }

    private void initCache() {
        mImageList = new LinkedList<>();
        mFrameQueue = new LinkedList<>();
        for (int i = 0; i != MAX_CACHED_FRAMES; ++i) {
            mImageList.add(Frame.create(mWidth, mHeight, opencv_core.IPL_DEPTH_8U, 4));
        }

        this.hasInit = true;
    }

    public void pushCachedFrame(Frame frame) {
        synchronized (mFrameQueue) {
            mHandler.sendMessage(createMessage(RECORDING_PROGRESS, (int) frame.frameTimeMillis));//时间回调
            mFrameQueue.offer(frame);
        }
    }

    public Frame getCachedFrame() {
        synchronized (mFrameQueue) {
            return mFrameQueue.poll();
        }
    }

    public long getCachedSize() {
        synchronized (mFrameQueue) {
            if (mFrameQueue.size() <= 0) return 0;
            return mFrameQueue.size() * mFrameQueue.peek().image.imageSize();
        }
    }

    public long getRecycleCachedSize() {
        synchronized (mImageList) {
            if (mImageList.size() <= 0) return 0;
            return mImageList.size() * mImageList.peek().image.imageSize();
        }
    }

    //回收使用过的缓存帧
    public void recycleCachedFrame(Frame frame) {
        synchronized (mImageList) {
            if (mShouldRecord)
                mImageList.offer(frame);
            else {
                if (mImageList.size() < MAX_CACHED_FRAMES)
                    mImageList.offer(frame);
                else {
                    frame.image.release();
                }
            }
        }
    }

    //获取空闲的缓存帧
    public Frame getImageCache() {
        synchronized (mImageList) {
            return mImageList.poll();
        }
    }

    private int totalFrameTemp = 0;

    public class RecordingThread extends Thread {
        private boolean isStart = true;

        public void stopRun() {
            isStart = false;
        }

        public boolean isStart() {
            return isStart;
        }

        @Override
        public void run() {
            super.run();
            while (isStart) {
                //等待
//                while (taskPoolSize >= MAX_ENCODE_FRAMES) {
////                    stopRun();
////                    return;
//                    synchronized (this) {
//                        try {
//                            this.wait(30);
//                        } catch (InterruptedException e) {
//                            Log.e(TAG, "Recording runnable wait() : " + e.getMessage());
//                        }
//                    }
//                }
                if (mShouldRecord) totalFrameTemp = mFrameQueue.size();
                Frame frame = getCachedFrame();

                if (frame == null) {
                    synchronized (this) {
                        try {
                            this.wait(30);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Recording runnable wait() : " + e.getMessage());
                        }
                    }
                    continue;
                }
                //NOTE 帧录制
                if (frame != null) {
//                    fiveTimeCount = System.currentTimeMillis();
//                    ++taskPoolSize;
//                    new EncodeThread(frame).start();
//                    Log.v(TAG, "task start, frame:" + frame.frameTimeMillis);
//                    new EncodeTask().execute(frame);
                    if (mVideoRecorder != null && mVideoRecorder.isStarting()) {
                        long time = System.currentTimeMillis();
                        mVideoRecorder.write(frame);
                        if (totalFrameTemp > 0)
                            onEncodeProgress((totalFrameTemp - mFrameQueue.size()) * 100 / totalFrameTemp, false);
                        Log.v(TAG, String.format("frame:" + frame.frameTimeMillis + ", end. taskCount: %d, consume: %d, cacheSize: %d, recycleCachedSize: %d", taskPoolSize, (System.currentTimeMillis() - time), getCachedSize(), getRecycleCachedSize()));
                    }
//                        frame.image.release();
                    recycleCachedFrame(frame);
                }
            }
        }
    }

    private long fiveTimeCount = 0;

    private class EncodeTask extends AsyncTask<Frame, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Frame... params) {
            //NOTE 帧录制
            if (params != null) {
                if (mVideoRecorder != null && mVideoRecorder.isStarting()) {
//                    Log.v(TAG, "frame:" + params[0].frameTimeMillis + ", start");
//                    long time = System.currentTimeMillis();
                    mVideoRecorder.write(params[0]);
//                    Log.v(TAG, String.format("frame:" + params[0].frameTimeMillis + ", end. taskCount: %d, consume: %d, cacheSize: %d, recycleCachedSize: %d", taskPoolSize, (System.currentTimeMillis() - time), getCachedSize(), getRecycleCachedSize()));
                }
//                        frame.image.release();
                recycleCachedFrame(params[0]);
            }
            --taskPoolSize;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (taskPoolSize == 0)
                Log.v(TAG, String.format("5 consume: %d", System.currentTimeMillis() - fiveTimeCount));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private Message createMessage(int what, int arg1) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        return msg;
    }

    public synchronized void startRecording() {
        if (!hasInit) return;
        if (!mShouldRecord) {
            synchronized (mRecordStateLock) {
                mShouldRecord = true;
                mVideoRecorder.startRecording();
                mHandler.sendMessage(createMessage(RECORDING_CONTROL, 1));
            }
        }
    }

    public synchronized void pauseRecording() {
        if (!hasInit) return;
        if (mShouldRecord) {
            synchronized (mRecordStateLock) {
                mShouldRecord = false;
                mVideoRecorder.pauseRecording();
                mHandler.sendMessage(createMessage(RECORDING_CONTROL, 0));
            }
        }
    }

    public synchronized void endRecording() {
        if (!hasInit) return;
        Log.i(TAG, "notify quit...");
        synchronized (mRecordStateLock) {
            mShouldRecord = false;
            mVideoRecorder.stopRecording();
        }

        synchronized (mRecordingThread) {
            mRecordingThread.stopRun();
        }

        Log.i(TAG, "joining thread...");
        try {
            mRecordingThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Join recording thread err: " + e.getMessage());
        }

        mRecordingThread = null;

        Log.i(TAG, "saving recoring...");

        mImageList.clear();
        mImageList = null;
        mFrameQueue.clear();
        mFrameQueue = null;
        mVideoRecorder = null;
        onEncodeProgress(100, true);
        this.hasInit = false;

        Log.i(TAG, "recording OK");
    }

    public RecordingThread getRecordingThread() {
        return mRecordingThread;
    }

    //是否已经开始录制
    public boolean isStarting() {
        if (!hasInit) return false;
        return mVideoRecorder.isStarting();
    }

    //获取当前编码进度，进度值不一定是从小到大的顺序
    public boolean encodeCompeleted() {
        return getCachedSize() <= 0 ? true : false;
    }

    //更新编码进度
    private void onEncodeProgress(int progress, boolean saved) {
        if (encodeListener == null) return;
        encodeListener.onProgress(progress, saved);
    }

    public void setEncodeListener(EncodeListener encodeListener) {
        this.encodeListener = encodeListener;
    }

    public void setRecordingLintener(RecordingLintener recordingLintener) {
        this.recordingLintener = recordingLintener;
    }

    public interface EncodeListener {
        void onProgress(int progress, boolean saved);
    }

    public interface RecordingLintener {
        void onProgress(long time);

        void onControl(int state);
    }
}
