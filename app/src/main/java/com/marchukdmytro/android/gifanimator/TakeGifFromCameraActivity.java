package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class TakeGifFromCameraActivity extends AppCompatActivity implements Camera.PreviewCallback {

    private int maxFrames;
    private SurfaceView surfaceView;
    private Camera camera;
    private int currentFrameIndex = 0;
    private File fileToSave;
    private TextView tvTookPhotos;
    private ImageButton btSwitch;
    private SurfaceHolder holder;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private ImageButton btStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takegif);
        maxFrames = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.COUNT_OF_FRAMES, SettingsActivity.DEFAULT_COUNT_OF_FRAMES));

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        tvTookPhotos = (TextView) findViewById(R.id.tvTookPhotos);

        btStart = (ImageButton) findViewById(R.id.btStart);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileToSave = new File(Constants.TEMPORARY_FOLDER_PATH);
                fileToSave.mkdir();
                currentFrameIndex = 0;
                btStart.setEnabled(false);
                btSwitch.setEnabled(false);
                camera.setPreviewCallback(TakeGifFromCameraActivity.this);
            }
        });
        btSwitch = (ImageButton) findViewById(R.id.btSwitch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.release();
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);

                setCameraDisplayOrientation(TakeGifFromCameraActivity.this, currentCameraId, camera);
                try {

                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
            }
        });

        assert surfaceView != null;
        holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror

        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(currentCameraId);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (currentFrameIndex >= maxFrames) {
            camera.setPreviewCallback(null);
            currentFrameIndex = 0;
            tvTookPhotos.setText("");
            btStart.setEnabled(true);
            btSwitch.setEnabled(true);
            Intent intent = GifPreviewActivity.newInstance(this, Constants.TEMPORARY_FOLDER_PATH);
            startActivity(intent);
            return;
        }
        tvTookPhotos.setText(String.valueOf(currentFrameIndex + 1));
        takeImage(data, currentFrameIndex);
        currentFrameIndex++;
    }

    private void takeImage(byte[] data, int i) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] rawImage = null;

        // Decode image from the retrieved buffer to JPEG
        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        yuv.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 90, baos);
        rawImage = baos.toByteArray();

        // This is the same image as the preview but in JPEG and not rotated
        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        ByteArrayOutputStream rotatedStream = new ByteArrayOutputStream();

        // Rotate the Bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        // We rotate the same Bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, previewSize.width, previewSize.height, matrix, false);

        // We dump the rotated Bitmap to the stream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, rotatedStream);

        rawImage = rotatedStream.toByteArray();

        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Bitmap bmp;
            bmp = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
            bmp = rotate(bmp, 180);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            rawImage = stream.toByteArray();
        }

        File file = new File(fileToSave + "/" + i + "out.jpg");
        try {
            FileUtils.writeByteArrayToFile(file, rawImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.preScale(-1, 1);
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}
