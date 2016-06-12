package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.marchukdmytro.android.gifanimator.filepicker.EnterStringDialog;
import com.marchukdmytro.android.gifanimator.filepicker.FileExplorerHelper;
import com.marchukdmytro.android.gifanimator.filepicker.FilePickerCallback;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends AppCompatActivity {

    private static String TAG = "gif-preview-activity-tag";
    private final int requestCode = 5;
    private final String directoryAfter = Environment.getExternalStorageDirectory().getPath()
            + "/animation-after";
    private AnimationDrawable animation;
    private BitmapDrawable frame;
    private int i;
    private ColorMatrix cm = new ColorMatrix();
    private String framesPath;
    private ImageView imageView;
    private final List<Bitmap> bitmaps = new ArrayList<Bitmap>();

    public static Intent newInstance(Context context, String framesPath) {
        Intent intent = new Intent(context, GifPreviewActivity.class);
        intent.putExtra(TAG, framesPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        framesPath = getIntent().getStringExtra(TAG);
        imageView = (ImageView) findViewById(R.id.gifView);
        loadGif();

        findViewById(R.id.btGray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContrastBrightness(cm, 1, i--);
                animation.setColorFilter(new ColorMatrixColorFilter(cm));
            }
        });
        findViewById(R.id.btSepia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContrastBrightness(cm, 1, i++);
                animation.setColorFilter(new ColorMatrixColorFilter(cm));
            }
        });
        findViewById(R.id.btSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FileExplorerHelper.getFolderPickerIntent(GifPreviewActivity.this);
                startActivityForResult(intent, requestCode);
            }
        });
        imageView.setImageDrawable(animation);
        animation.start();
    }

    private void savePicturesWithEffects(String pathToSave) throws Exception {
        File fileDir = new File(directoryAfter);
        boolean isMkDir = fileDir.mkdir();
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            Drawable d = animation.getFrame(i);
            Bitmap bmp = ((BitmapDrawable) d).getBitmap();
            Bitmap resultBitmap = Bitmap.createBitmap(bmp, 0, 0,
                    bmp.getWidth() - 1, bmp.getHeight() - 1);
            Paint p = new Paint();
            p.setColorFilter(new ColorMatrixColorFilter(getSepiaMatrix()));
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(resultBitmap, 0, 0, p);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

            byte[] rawImage = stream.toByteArray();
            try {
                FileUtils.writeByteArrayToFile(new File(directoryAfter + "/" + i + ".jpg"),
                        rawImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        makeGif(pathToSave);
    }

    private void loadGif() {
        File[] files = new File(framesPath).listFiles();
        try {
            animation = new AnimationDrawable();
            for (int i = 0; i < files.length; i++) {
                frame = new BitmapDrawable(files[i].getAbsolutePath());
                animation.addFrame(frame, 100);
            }
            animation.setOneShot(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeGif(String pathToSave) throws Exception {
        File[] files = new File(directoryAfter).listFiles();
        FileOutputStream fos = new FileOutputStream(pathToSave);
        GifEncoder encoder = new GifEncoder();
        encoder.setQuality(1);
        encoder.start(fos);
        for (File f : files) {
            Bitmap bmp = BitmapFactory.decodeFile(f.getPath());
            encoder.addFrame(bmp);
        }
        encoder.finish();
    }

    private static void setContrastBrightness(ColorMatrix cm, float contrast, float brightness) {
        cm.set(new float[]{
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0});
    }

    private ColorMatrix getGrayScaleMatrix() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        return matrix;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private ColorMatrix getSepiaMatrix() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrix colorScale = new ColorMatrix();
        colorScale.setScale(1, 1, 0.8f, 1);

        matrix.postConcat(colorScale);

        return matrix;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            new EnterStringDialog(this, new FilePickerCallback() {
                @Override
                public void pick(final String filename) {
                    FileExplorerHelper.onActivityResult(new FilePickerCallback() {
                        @Override
                        public void pick(String folder) {
                            try {
                                savePicturesWithEffects(folder + "/" + filename + ".gif");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, data);
                }
            }).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
