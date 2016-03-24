package com.lmy.lymedia.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.LinkedList;
import java.util.Queue;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB565;

/**
 * Created by lmy on 2016/3/24.
 */
public class VideoPlayer extends Player {
    private final static String TAG = "VideoPlayer";
    private SurfaceHolder mHolder;
    private FFmpegFrameGrabber mFrameGrabber;//解码器
    private AudioDevice audioDevice;
    private AndroidFrameConverter mFrameConverter;
    private DecodeThread mDecodeThread;
    private PlayImageThread mPlayImageThread;
    private PlaySampleThread mPlaySampleThread;
    private String sourcePath;
    //状态相关
    private boolean hasInit = false;

    public static VideoPlayer create(SurfaceHolder mHolder) {
        return new VideoPlayer(mHolder);
    }

    public static VideoPlayer create(SurfaceHolder mHolder, String path) {
        return new VideoPlayer(mHolder, path);
    }

    public VideoPlayer(SurfaceHolder mHolder) {
        this.mHolder = mHolder;
    }

    public VideoPlayer(SurfaceHolder mHolder, String path) {
        this.mHolder = mHolder;
        setDataSource(path);
    }

    public void setDataSource(String path) {
        this.sourcePath = path;
        this.curFrameNumber = 0;
        this.hasInit = false;
        this.mFrameConverter = new AndroidFrameConverter();
        try {
            if (mFrameGrabber != null) {//如果已经有实例，则先释放资源再初始化
                mFrameGrabber.stop();
                mFrameGrabber.release();
                mFrameGrabber = null;
            }
            if (audioDevice != null) {
                audioDevice.release();
                audioDevice = null;
            }
            mFrameGrabber = FFmpegFrameGrabber.createDefault(path);
            mFrameGrabber.setPixelFormat(AV_PIX_FMT_RGB565);
            mFrameGrabber.start();
            //开始初始化一些信息
            this.rate = Math.round(1000d / mFrameGrabber.getFrameRate());

        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        audioDevice = new AudioDevice(mFrameGrabber.getSampleRate(), mFrameGrabber.getAudioChannels());
        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
        mPlayImageThread = new PlayImageThread();
        mPlaySampleThread = new PlaySampleThread();
        this.hasInit = true;//标记初始化完成状态
        if (isAutoPlay()) play();//如果允许自动播放
    }

    @Override
    public void play() {
        if (!this.hasInit) return;
        super.play();
    }

    @Override
    public void pause() {
        if (!this.hasInit) return;
        super.pause();
    }

    @Override
    public void stop() {
        if (!this.hasInit) return;
        super.stop();
        mDecodeThread.stopRun();
        mPlayImageThread.stopRun();
        mPlaySampleThread.stopRun();
        try {
            synchronized (mFrameGrabber) {
                if (mFrameGrabber != null) {
                    mFrameGrabber.stop();
                    mFrameGrabber.release();
                    mFrameGrabber = null;
                }
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        if (imageQueue != null) {
            imageQueue.clear();
            imageQueue = null;
        }
        if (sampleQueue != null) {
            sampleQueue.clear();
            sampleQueue = null;
        }
    }

    @Override
    public void seek(int number) {
        if (number > mFrameGrabber.getLengthInFrames()) return;
        super.seek(number);
        synchronized (mFrameGrabber) {
            try {
                mFrameGrabber.setFrameNumber(curFrameNumber);
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int getWidth() {
        if (!this.hasInit) return -1;
        return mFrameGrabber.getImageWidth();
    }

    @Override
    public int getHeight() {
        if (!this.hasInit) return -1;
        return mFrameGrabber.getImageHeight();
    }

    private boolean draw(Frame frame) {
        if (frame == null || frame.image == null) {
            return false;
        }
        Bitmap bmp = mFrameConverter.convert(frame);
        if (bmp == null) return false;
        synchronized (mHolder) {
            Canvas canvas = mHolder.lockCanvas();
            if (canvas == null) return true;
            canvas.drawBitmap(bmp, null, new Rect(0, 0, canvas.getWidth(), frame.imageHeight * canvas.getWidth() / frame.imageWidth), null);
            mHolder.unlockCanvasAndPost(canvas);
        }
        return true;
    }

    private Frame copy(Frame frame) {
        Frame tmp = new Frame();
        int type = frameType(frame);
        if (type == 0) {
            tmp = new Frame(frame.imageWidth, frame.imageHeight, frame.imageDepth, frame.imageChannels);
            tmp.image = frame.image.clone();
        } else {
            tmp.samples = frame.samples.clone();
        }
        return tmp;
    }

    private class BaseThread extends Thread {
        protected boolean run = false;

        protected void sleepFunc(long time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            run = true;
        }

        public boolean isRun() {
            return run;
        }

        public void stopRun() {
            this.run = false;
        }
    }

    private final static int FRAME_CACHE_LIMIT = 15;
    private final static int SAMPLE_CACHE_LIMIT = 50;
    private Queue<Frame> imageQueue;
    private Queue<Frame> sampleQueue;

    private boolean offerImage(Frame frame) {
        if (imageQueue == null)
            imageQueue = new LinkedList<>();
        synchronized (imageQueue) {
            if (imageQueue.size() >= FRAME_CACHE_LIMIT) return false;
            imageQueue.offer(frame);
        }
        return true;
    }

    private Frame pollImage() {
        synchronized (imageQueue) {
            return imageQueue.poll();
        }
    }

    private int imageQueueSize() {
        synchronized (imageQueue) {
            return imageQueue.size();
        }
    }

    private boolean offerSample(Frame frame) {
        if (sampleQueue == null)
            sampleQueue = new LinkedList<>();
        synchronized (sampleQueue) {
            if (sampleQueue.size() >= SAMPLE_CACHE_LIMIT) return false;
            sampleQueue.offer(frame);
        }
        return true;
    }

    private Frame pollSample() {
        synchronized (sampleQueue) {
            return sampleQueue.poll();
        }
    }

    private int sampleQueueSize() {
        synchronized (sampleQueue) {
            return sampleQueue.size();
        }
    }

    private int frameType(Frame frame) {
        if (frame == null) return -1;
        if (frame.image != null) return 0;
        else if (frame.samples != null) return 1;
        else return -1;
    }

    private void tryPlay() {
        if (!mPlayImageThread.isRun())
            mPlayImageThread.start();
        if (!mPlaySampleThread.isRun())
            mPlaySampleThread.start();
    }

    private class DecodeThread extends BaseThread {
        @Override
        public void run() {
            super.run();
            synchronized (mFrameGrabber) {
                sampleQueue = new LinkedList<>();
                seek(0);
                try {
                    while (run && curFrameNumber < mFrameGrabber.getLengthInFrames() - 5) {
                        if (!play) {
                            sleepFunc(rate);
                            continue;
                        }
                        Frame frame = mFrameGrabber.grab();
                        int type = frameType(frame);
                        if (type == 0) {
                            frame = copy(frame);
                            while (isRun() && !offerImage(frame)) {
//                                if (sampleQueueSize() >= SAMPLE_CACHE_LIMIT)
                                tryPlay();
                                Log.v(TAG, "try offer image!");
                                sleepFunc(2);
                            }
                        } else if (type == 1) {
                            frame = copy(frame);
                            while (isRun() && !offerSample(frame)) {
//                                if (imageQueueSize() >= FRAME_CACHE_LIMIT)
                                tryPlay();
//                                Log.v(TAG, "try offer sample!");
                                sleepFunc(2);
                            }
                        }
                    }
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    long lastTime = 0;

    private class PlayImageThread extends BaseThread {
        @Override
        public void run() {
            super.run();
            while (run && curFrameNumber < mFrameGrabber.getLengthInFrames() - 5) {
                if (!play) {
                    lastTime = 0;
                    sleepFunc(rate);
                    continue;
                }
//                long wait = rate - System.currentTimeMillis() + lastTime;
//                Log.v(TAG, "wait=" + wait);
//                if (lastTime == 0 || (wait > -1000000 && wait < 10)) {
//                    sleepFunc(wait < 0 ? 0 : wait);
//                    lastTime = System.currentTimeMillis();
//                    if (imageQueueSize() > 0)
//                        if (draw(pollImage())) {
////                        Log.v(TAG, "draw image");
////                        long wait = rate - System.currentTimeMillis() + lastTime;
////                        sleepFunc(wait < 0 ? 0 : wait);
//                        }
//                }
//                if (sampleQueueSize() > 0)
//                    audioDevice.writeSamples(pollSample().samples);

                long time = System.currentTimeMillis();
                if (imageQueueSize() > 0)
                    if (draw(pollImage())) {
                        ++curFrameNumber;
                        Log.v(TAG, "draw image");
                        long wait = rate - System.currentTimeMillis() + time;
                        sleepFunc(wait < 0 ? 0 : wait);
                    }
            }
        }
    }

    private class PlaySampleThread extends BaseThread {

        @Override
        public void run() {
            super.run();
            while (run && curFrameNumber < mFrameGrabber.getLengthInFrames() - 5) {
                if (!play) {
                    sleepFunc(rate);
                    continue;
                }
                if (sampleQueueSize() > 0)
                    audioDevice.writeSamples(pollSample().samples);
//                else
//                    sleepFunc(5);
            }
        }
    }
}
