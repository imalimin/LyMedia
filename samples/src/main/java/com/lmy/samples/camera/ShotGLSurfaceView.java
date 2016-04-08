package com.lmy.samples.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;

import com.lmy.lycommon.camera.CameraInstance;
import com.lmy.lycommon.gles.widget.CameraGLSurfaceView;
import com.lmy.samples.camera.recorder.Util;

import org.bytedeco.javacpp.opencv_core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;

/**
 * Created by 李明艺 on 2016/3/28.
 *
 * @author lrlmy@foxmail.com
 *         <p/>
 *         拍照控件
 */
public class ShotGLSurfaceView extends CameraGLSurfaceView {
    private final static int RESIZE_FIT = 0x0000;
    private int mWidth, mHeight;
    private float scale;
    private String path;
    private ShotListener shotListener;

    public ShotGLSurfaceView(Context context) {
        super(context);
    }

    public ShotGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(float scale) {
        this.scale = scale;
        this.path = Util.root() + "/" + System.currentTimeMillis() + ".jpg";
        Display d = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        mWidth = d.getWidth();
        mHeight = (int) (mWidth / scale);
    }

    @Override
    protected void init() {
        this.setEGLContextClientVersion(2);
        this.setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        this.getHolder().setFormat(-3);
        this.setRenderer(this);
        this.setRenderMode(0);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RESIZE_FIT)
                reSize(mWidth, mHeight);
        }
    };

    public void switchCamera() {
        int cameraID = 0;
        if (cameraInstance().getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK)
            cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
            @Override
            public void cameraReady() {
                if (!cameraInstance().isPreviewing()) {
                    cameraInstance().stopPreview();
                }
                cameraInstance().startPreview(getSurfaceTexture());
            }
        }, cameraID);
    }

    @Override
    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance(1920, 1080);
    }

    private void fitView() {
        mHandler.sendEmptyMessage(RESIZE_FIT);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        super.onFrameAvailable(surfaceTexture);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        fitView();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if (take)
            takeShotFunc();
    }

    private boolean take = false;
    private opencv_core.IplImage mCacheImage;

    public void takeShot() {
        this.take = true;
    }

    private void takeShotFunc() {
        this.take = false;
        mCacheImage = cvCreateImage(new opencv_core.CvSize(mWidth, mHeight), opencv_core.IPL_DEPTH_8U, 4);
        GLES20.glReadPixels(getDrawViewport().x, (getDrawViewport().height - mHeight) / 2 + getDrawViewport().y, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCacheImage.getByteBuffer());
        new AsyncTask<Void, Integer, String>() {
            @Override
            protected String doInBackground(Void... params) {
                cvFlip(mCacheImage, mCacheImage, 0);
                Bitmap bmp = Bitmap.createBitmap(mCacheImage.cvSize().width(), mCacheImage.cvSize().height(), Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(mCacheImage.getByteBuffer());
                ImageUtil.saveBitmap(bmp, path);
                return path;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (shotListener != null)
                    shotListener.onShot(s);
            }
        }.execute();
    }

    public String getPath() {
        return path;
    }

    public void setShotListener(ShotListener shotListener) {
        this.shotListener = shotListener;
    }

    public interface ShotListener {
        void onShot(String path);
    }
}
