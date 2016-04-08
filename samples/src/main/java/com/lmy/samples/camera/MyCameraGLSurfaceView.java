package com.lmy.samples.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.lmy.lycommon.camera.CameraInstance;
import com.lmy.lycommon.gles.widget.CameraGLSurfaceView;
import com.lmy.samples.camera.recorder.Frame;
import com.lmy.samples.camera.recorder.Util;
import com.lmy.samples.camera.recorder.VideoRecordManager;

import org.bytedeco.javacpp.opencv_core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;

/**
 * Created by 李明艺 on 2016/2/29.
 *
 * @author lrlmy@foxmail.com
 *         <p/>
 *         用OpenGL渲染的摄像头采集控件
 */
public class MyCameraGLSurfaceView extends CameraGLSurfaceView {
    private final static String TAG = "MyCameraGLSurfaceView";
    private final static int RESIZE_FIT = 0x0000;

    public int videoWidth = 720, videoHeight = 480;
    public int mWidth = 1080, mHeight = 720;
    private opencv_core.IplImage srcImage;//帧缓存
    private long maxLenght = 30000000;
    private VideoRecordManager recoderManager;
    private boolean fitVideoSize = false;
    private String path;
    private String coverPath;

    public MyCameraGLSurfaceView(Context context) {
        super(context);
    }

    public MyCameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance(1080, 720);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RESIZE_FIT)
                reSize(mWidth, mHeight);
        }
    };

    public void initecoder() {
        initecoder(Util.root() + "/test.mp4");
    }

    public void initecoder(String path) {
        this.path = path;
        recoderManager = new VideoRecordManager(videoWidth, videoHeight, path);
    }

    private void fitVideo() {
        mWidth = viewWidth;
        mHeight = (int) (mWidth * videoHeight / (float) videoWidth);
        if (fitVideoSize)
            mHandler.sendEmptyMessage(RESIZE_FIT);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        super.onFrameAvailable(surfaceTexture);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.v(TAG, "onSurfaceCreated...");
        super.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged...");
        super.onSurfaceChanged(gl, width, height);
        fitVideo();
        //分辨率改变，重新初始化缓存帧
        this.srcImage = cvCreateImage(new opencv_core.CvSize(mWidth, mHeight), opencv_core.IPL_DEPTH_8U, 4);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    private long htime = 0;

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
//        Log.i(TAG, "onDrawFrame..., time=" + (System.currentTimeMillis() - htime));
//        htime = System.currentTimeMillis();
        recordFrame();
        mLastStamp = getSurfaceTexture().getTimestamp();
//        mTextureDrawer.draw(mtx);
    }

    private long mLastStamp = 0;
    private long mStampCount = -1;

    private void recordFrame() {
        synchronized (recoderManager.mRecordStateLock) {
            if (recoderManager.mShouldRecord) {// && mVideoRecorder != null && mVideoRecorder.isRecording()
                mStampCount += (mStampCount > -1 ? (getSurfaceTexture().getTimestamp() - mLastStamp) : 1);//计算时间戳
                long time = System.currentTimeMillis();
                Frame frame = recoderManager.getImageCache();
                if (frame == null)
                    frame = Frame.create(videoWidth, videoHeight, opencv_core.IPL_DEPTH_8U, 4);
                if (frame != null) {
                    GLES20.glReadPixels(getDrawViewport().x, (getDrawViewport().height - mHeight) / 2 + getDrawViewport().y, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, srcImage.getByteBuffer());
                    cvResize(srcImage, frame.image);
                    cvFlip(frame.image, frame.image, 0);
//                    takeShot(frame.image);//保存封面
//                    frame.image = cvEncodeImage(".jpg",flippedImage.asCvMat()).asIplImage();
                    frame.frameTimeMillis = mStampCount / 1000;
                    recoderManager.pushCachedFrame(frame);
                    Log.i("000", String.format("frame: time=%d, timestamp=%d, frameSize=%d", System.currentTimeMillis() - time, frame.frameTimeMillis, frame.image.imageSize()));
                    if (recoderManager.getRecordingThread() != null) {
                        synchronized (recoderManager.getRecordingThread()) {
                            try {
                                recoderManager.getRecordingThread().notifyAll();
                            } catch (Exception e) {
                                Log.e(TAG, "Notify failed: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Frame loss...");
                }
                if (frame.frameTimeMillis >= maxLenght)
                    pauseRecording();
            }
        }
    }

    public void takeShot(final opencv_core.IplImage image) {
        if (coverPath == null) {
            this.coverPath = path + ".jpg";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    opencv_core.IplImage t;
                    synchronized (image) {
                        t = cvCreateImage(new opencv_core.CvSize(videoWidth, videoHeight), opencv_core.IPL_DEPTH_8U, 4);
                    }
                    cvCopy(image, t);
                    Bitmap bmp = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(t.getByteBuffer());
                    ImageUtil.saveBitmap(bmp, coverPath);
                }
            }).start();
        }
    }

    public synchronized void startRecording() {
        if (mStampCount < maxLenght * 1000)
            recoderManager.startRecording();
    }

    public synchronized void pauseRecording() {
        recoderManager.pauseRecording();
    }

    public synchronized void stopRecording() {
        recoderManager.endRecording();
    }

    public synchronized boolean isRecording() {
        return recoderManager.mShouldRecord;
    }

    public String getPath() {
        return path;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public VideoRecordManager getRecoderManager() {
        return recoderManager;
    }

    public long getMaxLenght() {
        return maxLenght;
    }

    public void setMaxLenght(long maxLenght) {
        if (mStampCount < 0)
            this.maxLenght = maxLenght;
    }

    public boolean isFitVideoSize() {
        return fitVideoSize;
    }

    public void setFitVideoSize(boolean fitVideoSize) {
        this.fitVideoSize = fitVideoSize;
    }

}
