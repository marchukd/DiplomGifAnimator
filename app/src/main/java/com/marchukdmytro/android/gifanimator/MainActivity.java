package com.marchukdmytro.android.gifanimator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {

    SurfaceView surfaceView;
    Camera camera;
    private int i = 0;
    private File fileToSave;
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        findViewById(R.id.btnStartRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileToSave = new File(Environment.getExternalStorageDirectory().getPath()
                        + "/animation");
                fileToSave.mkdir();
                i = 0;
                paths.clear();
                bitmaps.clear();
                camera.setPreviewCallback(MainActivity.this);
            }
        });
        assert surfaceView != null;
        SurfaceHolder holder = surfaceView.getHolder();
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

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            if (i >= 5) {
                camera.setPreviewCallback(null);
                encodeGIF();
                return;
            }

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

            // Do something we this byte array

            File file = new File(fileToSave + "/" + i + "out.jpg");
            FileUtils.writeByteArrayToFile(file, rawImage);

            paths.add(file.getPath());
            i++;

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private void encodeGIF() throws Exception {
        for (String path : paths) {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            bitmaps.add(bmp);
        }
        String destinationPath = Environment.getExternalStorageDirectory().getPath()
                + "/" + "2.gif";
        FileOutputStream fos = new FileOutputStream(destinationPath);
        GifEncoder encoder = new GifEncoder();
        encoder.start(fos);
        for (Bitmap bmp : bitmaps) {
            encoder.addFrame(bmp);
        }
        encoder.finish();
        startActivity(GifPreviewActivity.newInstance(this, destinationPath));
    }
}
