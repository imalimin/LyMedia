package com.lmy.samples.camera;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangyang on 15/7/27.
 */


// Camera 仅适用单例
public class CameraInstance {
    public static final String TAG = "CameraInstance";

    private static final String ASSERT_MSG = "检测到CameraDevice 为 null! 请检查";

    private Camera mCameraDevice;
    private Camera.Parameters mParams;

    public static final int DEFAULT_PREVIEW_RATE = 30;
    public static final int MAX_PREVIEW_RATE = 60;
    public static final int MIN_PREVIEW_RATE = 30;


    private boolean mIsPreviewing = false;

    private int mDefaultCameraID = -1;

    private static CameraInstance mThisInstance;
    private int mPreviewWidth;
    private int mPreviewHeight;

    private int mPictureWidth = 1000;
    private int mPictureHeight = 1000;

    private int mPreferPreviewWidth = 640;
    private int mPreferPreviewHeight = 640;

    private int mFacing = 0;

    private CameraInstance() {
    }

    public static synchronized CameraInstance getInstance() {
        if (mThisInstance == null) {
            mThisInstance = new CameraInstance();
        }
        return mThisInstance;
    }

    public boolean isPreviewing() {
        return mIsPreviewing;
    }

    public int previewWidth() {
        return mPreviewWidth;
    }

    public int previewHeight() {
        return mPreviewHeight;
    }

    public int pictureWidth() {
        return mPictureWidth;
    }

    public int pictureHeight() {
        return mPictureHeight;
    }

    public void setPreferPreviewSize(int w, int h) {
        mPreferPreviewHeight = w;
        mPreferPreviewWidth = h;
    }

    public interface CameraOpenCallback {
        void cameraReady();
    }

    public boolean tryOpenCamera(CameraOpenCallback callback) {
        return tryOpenCamera(callback, Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public int getFacing() {
        return mFacing;
    }

    public synchronized boolean tryOpenCamera(CameraOpenCallback callback, int facing) {
        Log.i(TAG, "try open camera...");

        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                int numberOfCameras = Camera.getNumberOfCameras();

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == facing) {
                        mDefaultCameraID = i;
                        mFacing = facing;
                    }
                }
            }
            stopPreview();
            if (mCameraDevice != null)
                mCameraDevice.release();

            if (mDefaultCameraID >= 0)
                mCameraDevice = Camera.open(mDefaultCameraID);
            else
                mCameraDevice = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "Open Camera Failed!");
            e.printStackTrace();
            mCameraDevice = null;
            return false;
        }

        if (mCameraDevice != null) {
            Log.i(TAG, "Camera opened!");

            try {
                initCamera(DEFAULT_PREVIEW_RATE);
            } catch (Exception e) {
                mCameraDevice.release();
                mCameraDevice = null;
                return false;
            }

            if (callback != null) {
                callback.cameraReady();
            }

            return true;
        }

        return false;
    }

    public synchronized void stopCamera() {
        if (mCameraDevice != null) {
            mIsPreviewing = false;
            mCameraDevice.stopPreview();
            mCameraDevice.setPreviewCallback(null);
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    public boolean isCameraOpened() {
        return mCameraDevice != null;
    }

    public synchronized void startPreview(SurfaceTexture texture) {
        Log.i(TAG, "Camera startPreview...");
        if (mIsPreviewing) {
            Log.e(TAG, "Err: camera is previewing...");
//            stopPreview();
            return;
        }

        if (mCameraDevice != null) {
            try {
                mCameraDevice.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCameraDevice.startPreview();
            mIsPreviewing = true;
        }
    }

    public synchronized void stopPreview() {
        if (mIsPreviewing && mCameraDevice != null) {
            Log.i(TAG, "Camera stopPreview...");
            mIsPreviewing = false;
            mCameraDevice.stopPreview();
        }
    }

    public synchronized Camera.Parameters getParams() {
        if (mCameraDevice != null)
            return mCameraDevice.getParameters();
        assert mCameraDevice != null : ASSERT_MSG;
        return null;
    }

    public synchronized void setParams(Camera.Parameters param) {
        if (mCameraDevice != null) {
            mParams = param;
            mCameraDevice.setParameters(mParams);
        }
        assert mCameraDevice != null : ASSERT_MSG;
    }

    public Camera getCameraDevice() {
        return mCameraDevice;
    }

    //保证从大到小排列
    private Comparator<Camera.Size> comparatorBigger = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            int w = rhs.width - lhs.width;
            if (w == 0)
                return rhs.height - lhs.height;
            return w;
        }
    };

    //保证从小到大排列
    private Comparator<Camera.Size> comparatorSmaller = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            int w = lhs.width - rhs.width;
            if (w == 0)
                return lhs.height - rhs.height;
            return w;
        }
    };

    public void initCamera(int previewRate) {
        if (mCameraDevice == null) {
            Log.e(TAG, "initCamera: Camera is not opened!");
            return;
        }

        mParams = mCameraDevice.getParameters();
        List<Integer> supportedPictureFormats = mParams.getSupportedPictureFormats();

        for (int fmt : supportedPictureFormats) {
            Log.i(TAG, String.format("Picture Format: %x", fmt));
        }

        mParams.setPictureFormat(PixelFormat.JPEG);

        List<Camera.Size> picSizes = mParams.getSupportedPictureSizes();
        Camera.Size picSz = null;

        Collections.sort(picSizes, comparatorBigger);

        for (Camera.Size sz : picSizes) {
            Log.i(TAG, String.format("Supported picture size: %d x %d", sz.width, sz.height));
            if (picSz == null || (sz.width >= mPictureWidth && sz.height >= mPictureHeight)) {
                picSz = sz;
            }
        }

        List<Camera.Size> prevSizes = mParams.getSupportedPreviewSizes();
        Camera.Size prevSz = null;

        Collections.sort(prevSizes, comparatorBigger);

        for (Camera.Size sz : prevSizes) {
            Log.i(TAG, String.format("Supported preview size: %d x %d", sz.width, sz.height));
            if (prevSz == null || (sz.width >= mPreferPreviewWidth && sz.height >= mPreferPreviewHeight)) {
                prevSz = sz;
            }
        }
        //设置相机预览帧率
        int frameRatesRang[] = new int[2];
        mParams.getPreviewFpsRange(frameRatesRang);
        int maxRate = MAX_PREVIEW_RATE * 1000;
        int minRate = MIN_PREVIEW_RATE * 1000;
        Log.i(TAG, "Supported max frame rate: " + frameRatesRang[1] + ",min frame rate: " + frameRatesRang[0]);
        if (minRate < frameRatesRang[0])
            minRate = frameRatesRang[0];
        if (maxRate > frameRatesRang[1])
            maxRate = frameRatesRang[1];
        mParams.setPreviewFpsRange(minRate, maxRate);

//        List<Integer> frameRates = mParams.getSupportedPreviewFrameRates();
//        int fpsMax = 0;
//
//        for (Integer n : frameRates) {
//            Log.i(TAG, "Supported frame rate: " + n);
//            if (fpsMax < n) {
//                fpsMax = n;
//            }
//        }
//        previewRate = fpsMax;
//        mParams.setPreviewFrameRate(previewRate); //设置相机预览帧率
//        mParams.setPreviewFpsRange(20, 60);

        mParams.setPreviewSize(prevSz.width, prevSz.height);
        mParams.setPictureSize(picSz.width, picSz.height);

        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        try {
            mCameraDevice.setParameters(mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }


        mParams = mCameraDevice.getParameters();

        Camera.Size szPic = mParams.getPictureSize();
        Camera.Size szPrev = mParams.getPreviewSize();

        mPreviewWidth = szPrev.width;
        mPreviewHeight = szPrev.height;

        mPictureWidth = szPic.width;
        mPictureHeight = szPic.height;

        Log.i(TAG, String.format("Camera Picture Size: %d x %d", szPic.width, szPic.height));
        Log.i(TAG, String.format("Camera Preview Size: %d x %d", szPrev.width, szPrev.height));
        Log.i(TAG, String.format("Camera Preview Min Rate: %d Max Rate: %d", minRate,maxRate));
    }

    public synchronized void setFocusMode(String focusMode) {

        if (mCameraDevice == null)
            return;

        mParams = mCameraDevice.getParameters();
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(focusMode)) {
            mParams.setFocusMode(focusMode);
        }
    }

    public synchronized void setPictureSize(int width, int height, boolean isBigger) {

        if (mCameraDevice == null) {
            mPictureWidth = width;
            mPictureHeight = height;
            return;
        }

        mParams = mCameraDevice.getParameters();


        List<Camera.Size> picSizes = mParams.getSupportedPictureSizes();
        Camera.Size picSz = null;

        if (isBigger) {
            Collections.sort(picSizes, comparatorBigger);
            for (Camera.Size sz : picSizes) {
                if (picSz == null || (sz.width >= width && sz.height >= height)) {
                    picSz = sz;
                }
            }
        } else {
            Collections.sort(picSizes, comparatorSmaller);
            for (Camera.Size sz : picSizes) {
                if (picSz == null || (sz.width <= width && sz.height <= height)) {
                    picSz = sz;
                }
            }
        }

        mPictureWidth = picSz.width;
        mPictureHeight = picSz.height;

        try {
            mParams.setPictureSize(mPictureWidth, mPictureHeight);
            mCameraDevice.setParameters(mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void focusAtPoint(float x, float y, final Camera.AutoFocusCallback callback) {
        focusAtPoint(x, y, 0.2f, callback);
    }

    public synchronized void focusAtPoint(float x, float y, float radius, final Camera.AutoFocusCallback callback) {
        if (mCameraDevice == null) {
            Log.e(TAG, "Error: focus after release.");
            return;
        }

        mParams = mCameraDevice.getParameters();

        if (mParams.getMaxNumMeteringAreas() > 0) {

            int focusRadius = (int) (radius * 1000.0f);
            int left = (int) (x * 2000.0f - 1000.0f) - focusRadius;
            int top = (int) (y * 2000.0f - 1000.0f) - focusRadius;

            Rect focusArea = new Rect();
            focusArea.left = Math.max(left, -1000);
            focusArea.top = Math.max(top, -1000);
            focusArea.right = Math.min(left + focusRadius, 1000);
            focusArea.bottom = Math.min(top + focusRadius, 1000);
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(focusArea, 800));

            try {
                mCameraDevice.cancelAutoFocus();
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mParams.setFocusAreas(meteringAreas);
                mCameraDevice.setParameters(mParams);
                mCameraDevice.autoFocus(callback);
            } catch (Exception e) {
                Log.e(TAG, "Error: focusAtPoint failed: " + e.toString());
            }
        } else {
            Log.i(TAG, "The device does not support metering areas...");
            try {
                mCameraDevice.autoFocus(callback);
            } catch (Exception e) {
                Log.e(TAG, "Error: focusAtPoint failed: " + e.toString());
            }
        }

    }
}
