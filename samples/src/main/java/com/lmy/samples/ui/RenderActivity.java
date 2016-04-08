package com.lmy.samples.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lmy.lymedia.utils.FrameUtil;
import com.lmy.lymedia.utils.Util;
import com.lmy.samples.R;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB24;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB32;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB4_BYTE;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB555;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB565;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB8;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_imgproc.CV_HSV2RGB;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2RGBA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2RGB;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.filter2D;

public class RenderActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageView renderView;

    private SeekBar mSeekBarRR;
    private TextView mTextViewRR;
    private SeekBar mSeekBarRG;
    private TextView mTextViewRG;
    private SeekBar mSeekBarRB;
    private TextView mTextViewRB;

    private SeekBar mSeekBarGR;
    private TextView mTextViewGR;
    private SeekBar mSeekBarGG;
    private TextView mTextViewGG;
    private SeekBar mSeekBarGB;
    private TextView mTextViewGB;

    private SeekBar mSeekBarBR;
    private TextView mTextViewBR;
    private SeekBar mSeekBarBG;
    private TextView mTextViewBG;
    private SeekBar mSeekBarBB;
    private TextView mTextViewBB;

    private Button defaultBtn;
    private Button oldBtn;
    //    private FFmpegFrameGrabber mFrameGrabber;
    private AndroidFrameConverter mFrameConverter;
    private FFmpegFrameFilter mFilter;
    private OpenCVFrameConverter.ToIplImage converter;
    private Frame cacheFrame;
    private Bitmap srcBitmap;
    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);
        initView();
    }

    private void initView() {
        df = new DecimalFormat("0.000");
        imageView = (ImageView) findViewById(R.id.image);
        renderView = (ImageView) findViewById(R.id.image1);
        defaultBtn = (Button) findViewById(R.id.default_btn);
        oldBtn = (Button) findViewById(R.id.old_btn);

        mSeekBarRR = (SeekBar) findViewById(R.id.seek_bar_rr);
        mTextViewRR = (TextView) findViewById(R.id.text_rr);
        mSeekBarRG = (SeekBar) findViewById(R.id.seek_bar_rg);
        mTextViewRG = (TextView) findViewById(R.id.text_rg);
        mSeekBarRB = (SeekBar) findViewById(R.id.seek_bar_rb);
        mTextViewRB = (TextView) findViewById(R.id.text_rb);

        mSeekBarGR = (SeekBar) findViewById(R.id.seek_bar_gr);
        mTextViewGR = (TextView) findViewById(R.id.text_gr);
        mSeekBarGG = (SeekBar) findViewById(R.id.seek_bar_gg);
        mTextViewGG = (TextView) findViewById(R.id.text_gg);
        mSeekBarGB = (SeekBar) findViewById(R.id.seek_bar_gb);
        mTextViewGB = (TextView) findViewById(R.id.text_gb);

        mSeekBarBR = (SeekBar) findViewById(R.id.seek_bar_br);
        mTextViewBR = (TextView) findViewById(R.id.text_br);
        mSeekBarBG = (SeekBar) findViewById(R.id.seek_bar_bg);
        mTextViewBG = (TextView) findViewById(R.id.text_bg);
        mSeekBarBB = (SeekBar) findViewById(R.id.seek_bar_bb);
        mTextViewBB = (TextView) findViewById(R.id.text_bb);

        mFrameConverter = new AndroidFrameConverter();
        converter = new OpenCVFrameConverter.ToIplImage();
        srcBitmap = BitmapFactory.decodeFile(Util.getSdcardPath() + "/test.jpg");
        cacheFrame = mFrameConverter.convert(srcBitmap);
        imageView.setImageBitmap(srcBitmap);
        renderView.setImageBitmap(srcBitmap);

        mSeekBarRR.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarRG.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarRB.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarGR.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarGG.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarGB.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarBR.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarBG.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSeekBarBB.setOnSeekBarChangeListener(onSeekBarChangeListener);

        defaultBtn.setOnClickListener(onClickListener);
        oldBtn.setOnClickListener(onClickListener);
        imageView.setImageBitmap(srcBitmap);
        renderView.setImageBitmap(srcBitmap);

//
//        renderView.setImageBitmap(filter(srcBitmap, value));
//        try {
//            mFrameGrabber = FFmpegFrameGrabber.createDefault(Util.getSdcardPath() + "/test.mp4");
//            mFrameGrabber.setPixelFormat(AV_PIX_FMT_RGBA);
//            mFrameGrabber.start();
//            mFrameGrabber.setFrameNumber(597);
//            Bitmap bmp = mFrameConverter.convert(FrameUtil.copy(mFrameGrabber.grabImage()));
//            imageView.setImageBitmap(bmp.copy(bmp.getConfig(), bmp.isMutable()));
//            cacheFrame = FrameUtil.copy(mFrameGrabber.grabImage());
//            renderView.setImageBitmap(mFrameConverter.convert(filter(FrameUtil.copy(cacheFrame), 1, 1, 1)));
//        } catch (FrameGrabber.Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            mFilter = new FFmpegFrameFilter("crop=w=200:h=100:x=100:y=100", mFrameGrabber.getImageWidth(), mFrameGrabber.getImageHeight());
//            mFilter.start();
//        } catch (FrameFilter.Exception e) {
//            e.printStackTrace();
//        }
    }

    private Bitmap filter(Bitmap bmp) {
        float[] value = new float[9];
        value[0] = mSeekBarRR.getProgress() / (float)mSeekBarRR.getMax();
        value[1] = mSeekBarRG.getProgress() / (float)mSeekBarRR.getMax();
        value[2] = mSeekBarRB.getProgress() / (float)mSeekBarRR.getMax();

        value[3] = mSeekBarGR.getProgress() / (float)mSeekBarRR.getMax();
        value[4] = mSeekBarGG.getProgress() / (float)mSeekBarRR.getMax();
        value[5] = mSeekBarGB.getProgress() / (float)mSeekBarRR.getMax();

        value[6] = mSeekBarBR.getProgress() / (float)mSeekBarRR.getMax();
        value[7] = mSeekBarBG.getProgress() / (float)mSeekBarRR.getMax();
        value[8] = mSeekBarBB.getProgress() / (float)mSeekBarRR.getMax();

        mTextViewRR.setText(String.valueOf(df.format(value[0])));
        mTextViewRG.setText(String.valueOf(df.format(value[1])));
        mTextViewRB.setText(String.valueOf(df.format(value[2])));

        mTextViewGR.setText(String.valueOf(df.format(value[3])));
        mTextViewGG.setText(String.valueOf(df.format(value[4])));
        mTextViewGB.setText(String.valueOf(df.format(value[5])));

        mTextViewBR.setText(String.valueOf(df.format(value[6])));
        mTextViewBG.setText(String.valueOf(df.format(value[7])));
        mTextViewBB.setText(String.valueOf(df.format(value[8])));

        return filter(srcBitmap, value);
    }

    private Bitmap filter(Bitmap bmp, float[] value) {// 速度测试
        long start = System.currentTimeMillis();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (value[0] * pixR + value[1] * pixG + value[2] * pixB);
                newG = (int) (value[3] * pixR + value[4] * pixG + value[5] * pixB);
                newB = (int) (value[6] * pixR + value[7] * pixG + value[8] * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        Log.d("may", "used time=" + (end - start));
        return bitmap;
    }

    private Frame filter(Frame frame) {
        float[] value = new float[9];
        value[0] = mSeekBarRR.getProgress() / (float)mSeekBarRR.getMax();
        value[1] = mSeekBarRG.getProgress() / (float)mSeekBarRR.getMax();
        value[2] = mSeekBarRB.getProgress() / (float)mSeekBarRR.getMax();

        value[3] = mSeekBarGR.getProgress() / (float)mSeekBarRR.getMax();
        value[4] = mSeekBarGG.getProgress() / (float)mSeekBarRR.getMax();
        value[5] = mSeekBarGB.getProgress() / (float)mSeekBarRR.getMax();

        value[6] = mSeekBarBR.getProgress() / (float)mSeekBarRR.getMax();
        value[7] = mSeekBarBG.getProgress() / (float)mSeekBarRR.getMax();
        value[8] = mSeekBarBB.getProgress() / (float)mSeekBarRR.getMax();

        mTextViewRR.setText(String.valueOf(df.format(value[0])));
        mTextViewRG.setText(String.valueOf(df.format(value[1])));
        mTextViewRB.setText(String.valueOf(df.format(value[2])));

        mTextViewGR.setText(String.valueOf(df.format(value[3])));
        mTextViewGG.setText(String.valueOf(df.format(value[4])));
        mTextViewGB.setText(String.valueOf(df.format(value[5])));

        mTextViewBR.setText(String.valueOf(df.format(value[6])));
        mTextViewBG.setText(String.valueOf(df.format(value[7])));
        mTextViewBB.setText(String.valueOf(df.format(value[8])));

        return filter(frame, value);
    }

    private Frame filter(Frame frame, float[] value) {
        opencv_core.IplImage src = converter.convertToIplImage(frame);
        opencv_core.IplImage rgb = cvCreateImage(cvGetSize(src), 8, 3);//给rgb色系的图像申请空间
        opencv_core.IplImage hsv = cvCreateImage(cvGetSize(src), 8, 3);//给hsv色系的图像申请空间

        cvCvtColor(src, rgb, CV_RGBA2RGB);
//        cvCvtColor(rgb, hsv, CV_RGB2HSV);//将RGB色系转为HSV色系
        ByteBuffer buffer = rgb.getByteBuffer();
        for (int i = 0; i < buffer.capacity(); i++) {
            byte r = buffer.get(i);
            byte g = buffer.get(i);
            byte b = buffer.get(i);
            if (i % 3 == 0)//R
                buffer.put(i, (byte) (buffer.get(i) * value[0] + buffer.get(i + 1) * value[1] + buffer.get(i + 2) * value[2]));

            if (i % 3 == 1)//G
                buffer.put(i, (byte) (buffer.get(i - 1) * value[3] + buffer.get(i) * value[4] + buffer.get(i + 1) * value[5]));

            if (i % 3 == 2)//B
                buffer.put(i, (byte) (buffer.get(i - 2) * value[6] + buffer.get(i - 1) * value[7] + buffer.get(i) * value[8]));
        }
//        cvCvtColor(hsv, rgb, CV_HSV2RGB);//将RGB色系转为HSV色系
        cvCvtColor(rgb, src, CV_RGB2RGBA);
        return converter.convert(src);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            float[] value = new float[9];
            switch (v.getId()) {
                case R.id.default_btn:
                    mSeekBarRR.setProgress((int) value[0] * mSeekBarRR.getMax());
                    mSeekBarRG.setProgress((int) value[1] * mSeekBarRR.getMax());
                    mSeekBarRB.setProgress((int) value[2] * mSeekBarRR.getMax());

                    mSeekBarGR.setProgress((int) value[3] * mSeekBarRR.getMax());
                    mSeekBarGG.setProgress((int) value[4] * mSeekBarRR.getMax());
                    mSeekBarGB.setProgress((int) value[5] * mSeekBarRR.getMax());

                    mSeekBarBR.setProgress((int) value[6] * mSeekBarRR.getMax());
                    mSeekBarBG.setProgress((int) value[7] * mSeekBarRR.getMax());
                    mSeekBarBB.setProgress((int) value[8] * mSeekBarRR.getMax());

                    mTextViewRR.setText(String.valueOf(df.format(value[0])));
                    mTextViewRG.setText(String.valueOf(df.format(value[1])));
                    mTextViewRB.setText(String.valueOf(df.format(value[2])));

                    mTextViewGR.setText(String.valueOf(df.format(value[3])));
                    mTextViewGG.setText(String.valueOf(df.format(value[4])));
                    mTextViewGB.setText(String.valueOf(df.format(value[5])));

                    mTextViewBR.setText(String.valueOf(df.format(value[6])));
                    mTextViewBG.setText(String.valueOf(df.format(value[7])));
                    mTextViewBB.setText(String.valueOf(df.format(value[8])));

                    imageView.setImageBitmap(srcBitmap);
                    renderView.setImageBitmap(srcBitmap);
                    break;
                case R.id.old_btn:
                    value = new float[]{0.393f, 0.769f, 0.189f, 0.349f, 0.686f, 0.168f, 0.272f, 0.534f, 0.131f};
                    mSeekBarRR.setProgress((int) value[0] * mSeekBarRR.getMax());
                    mSeekBarRG.setProgress((int) value[1] * mSeekBarRR.getMax());
                    mSeekBarRB.setProgress((int) value[2] * mSeekBarRR.getMax());

                    mSeekBarGR.setProgress((int) value[3] * mSeekBarRR.getMax());
                    mSeekBarGG.setProgress((int) value[4] * mSeekBarRR.getMax());
                    mSeekBarGB.setProgress((int) value[5] * mSeekBarRR.getMax());

                    mSeekBarBR.setProgress((int) value[6] * mSeekBarRR.getMax());
                    mSeekBarBG.setProgress((int) value[7] * mSeekBarRR.getMax());
                    mSeekBarBB.setProgress((int) value[8] * mSeekBarRR.getMax());

                    mTextViewRR.setText(String.valueOf(df.format(value[0])));
                    mTextViewRG.setText(String.valueOf(df.format(value[1])));
                    mTextViewRB.setText(String.valueOf(df.format(value[2])));

                    mTextViewGR.setText(String.valueOf(df.format(value[3])));
                    mTextViewGG.setText(String.valueOf(df.format(value[4])));
                    mTextViewGB.setText(String.valueOf(df.format(value[5])));

                    mTextViewBR.setText(String.valueOf(df.format(value[6])));
                    mTextViewBG.setText(String.valueOf(df.format(value[7])));
                    mTextViewBB.setText(String.valueOf(df.format(value[8])));
                    renderView.setImageBitmap(filter(srcBitmap, value));
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            renderView.setImageBitmap(mFrameConverter.convert(filter(mFrameConverter.convert(srcBitmap))));
            renderView.setImageBitmap(filter(srcBitmap));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
