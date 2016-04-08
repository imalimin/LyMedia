package com.lmy.lymedia.media;

import android.os.AsyncTask;
import android.util.Log;

import com.lmy.lymedia.media.render.Render;
import com.lmy.lymedia.utils.FrameUtil;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB565;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;

/**
 * Created by Administrator on 2016/3/28.
 */
public class VideoRender {
    private FFmpegFrameGrabber mFrameGrabber;
    private FFmpegFrameRecorder mFrameRecorder;
    private String srcPath;
    private String dstPath;
    private Frame mFrame;
    private RenderTask mRenderTask;
    private RenderListener renderListener;
    private Render mRender;

    public VideoRender(String srcPath, String dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.mRenderTask = new RenderTask();
    }

    public boolean init() {
        return initGrabber() && initRecorder(getWidth(),getHeight());
    }

    private boolean initGrabber() {
        try {
            if (mFrameGrabber != null) {//如果已经有实例，则先释放资源再初始化
                mFrameGrabber.stop();
                mFrameGrabber.release();
                mFrameGrabber = null;
            }
            mFrameGrabber = FFmpegFrameGrabber.createDefault(srcPath);
            mFrameGrabber.setPixelFormat(AV_PIX_FMT_RGBA);
            mFrameGrabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setPixelFormat(int fmt) {
        mFrameGrabber.setPixelFormat(fmt);
    }

    private boolean initRecorder(int width,int height) {
        try {
            if (mFrameRecorder != null) {//如果已经有实例，则先释放资源再初始化
                mFrameRecorder.stop();
                mFrameRecorder.release();
                mFrameRecorder = null;
            }
            mFrameRecorder = FFmpegFrameRecorder.createDefault(dstPath, mFrameGrabber.getImageWidth(), mFrameGrabber.getImageHeight());
            mFrameRecorder.setFormat(mFrameGrabber.getFormat());
            mFrameRecorder.setSampleRate(mFrameGrabber.getSampleRate());
            mFrameRecorder.setFrameRate(mFrameGrabber.getFrameRate());
            mFrameRecorder.setVideoCodec(mFrameGrabber.getVideoCodec());
//            mFrameRecorder.setVideoQuality(1);
//            mFrameRecorder.setAudioQuality(1);
            mFrameRecorder.setAudioCodec(mFrameGrabber.getAudioCodec());
            mFrameRecorder.setVideoBitrate(mFrameGrabber.getVideoBitrate());
            mFrameRecorder.setAudioBitrate(mFrameGrabber.getAudioBitrate());
            mFrameRecorder.setAudioChannels(mFrameGrabber.getAudioChannels());
            mFrameRecorder.setImageWidth(width);
            mFrameRecorder.setImageHeight(height);
            mFrameRecorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void start() {
        mRenderTask.execute();
    }

    public void stop() {
        mRenderTask.stopFunc();
    }

    private void release() {
        try {
            if (mFrameGrabber != null) {//如果已经有实例，则先释放资源再初始化
                mFrameGrabber.stop();
                mFrameGrabber.release();
                mFrameGrabber = null;
            }

            if (mFrameRecorder != null) {//如果已经有实例，则先释放资源再初始化
                mFrameRecorder.stop();
                mFrameRecorder.release();
                mFrameRecorder = null;
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void setRenderListener(RenderListener renderListener) {
        this.renderListener = renderListener;
    }

    public void setRender(Render mRender) {
        this.mRender = mRender;
    }

    private class RenderTask extends AsyncTask<Void, Integer, Integer> {
        private boolean run = false;

        private Frame render(Frame frame) {
            if (mRender != null)
                return mRender.render(frame);
            else return frame;
        }

        private void stopFunc() {
            this.run = false;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            run = true;
            int mFrameNumber = 0;
            publishProgress(mFrameNumber * 100 / mFrameGrabber.getLengthInFrames());
            Log.w("VideoRender", "Frame lenght: " + mFrameGrabber.getLengthInFrames());
            while (run && mFrameNumber <= mFrameGrabber.getLengthInFrames()) {
                try {
                    mFrame = mFrameGrabber.grab();
                    int type = FrameUtil.frameType(mFrame);
                    if (type == 0) {
                        mFrame = render(mFrame);
                        mFrameRecorder.setTimestamp(mFrameGrabber.getTimestamp());
                        mFrameRecorder.record(mFrame);
                        ++mFrameNumber;
                        publishProgress(mFrameNumber * 100 / mFrameGrabber.getLengthInFrames());
                        Log.w("VideoRender", "Frame num=" + mFrameNumber + ", time: " + mFrameGrabber.getTimestamp());
                    } else if (type == 1) {
                        mFrameRecorder.recordSamples(mFrame.samples);
                    } else break;
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                    ++mFrameNumber;
                    Log.w("VideoRender", "Frame missed!");
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
            Log.w("VideoRender", "release");
            release();
            publishProgress(100);
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (renderListener != null)
                renderListener.onProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    public int getWidth() {
        return mFrameGrabber.getImageWidth();
    }

    public int getHeight() {
        return mFrameGrabber.getImageHeight();
    }

    public interface RenderListener {
        void onProgress(int progress);
    }
}
