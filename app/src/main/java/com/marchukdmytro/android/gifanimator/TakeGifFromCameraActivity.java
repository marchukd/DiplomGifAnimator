package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TakeGifFromCameraActivity extends AppCompatActivity implements Camera.PreviewCallback {

    SurfaceView surfaceView;
    Camera camera;
    private int i = 0;
    private File fileToSave;
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private ProgressDialog dialog;
    private String destinationPath;
    private TextView tvTookPhotos;
    private ImageButton btSwitch;
    private SurfaceHolder holder;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String folderToSave = Environment.getExternalStorageDirectory().getPath()
            + "/animation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takegif);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        tvTookPhotos = (TextView) findViewById(R.id.tvTookPhotos);

        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileToSave = new File(folderToSave);
                fileToSave.mkdir();
                i = 0;
                paths.clear();
                bitmaps.clear();
                //takeScreenshot(fileToSave + "/111.jpg");
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
        if (i >= 15) {
            camera.setPreviewCallback(null);
            //encodeGIF();
            new GifCreatorTask().execute();
            return;
        }
        tvTookPhotos.setText(String.valueOf(i + 1));
        takeImage(data, i);
        i++;
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
        // Do something we this byte array

        File file = new File(fileToSave + "/" + i + "out.jpg");
        try {
            FileUtils.writeByteArrayToFile(file, rawImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        paths.add(file.getPath());
    }
    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.preScale(-1, 1);
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public class GifCreatorTask extends AsyncTask<Context, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            destinationPath = Environment.getExternalStorageDirectory().getPath()
                    + "/" + "2.gif";
            dialog = ProgressDialog.show(TakeGifFromCameraActivity.this, "loading",
                    "loading", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            startActivity(GifPreviewActivity.newInstance(TakeGifFromCameraActivity.this, folderToSave));
        }

        @Override
        protected Void doInBackground(Context... params) {
            try {
                //encodeGIF();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
