package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.marchukdmytro.android.gifanimator.filepicker.EnterStringDialog;
import com.marchukdmytro.android.gifanimator.filepicker.FileExplorerHelper;
import com.marchukdmytro.android.gifanimator.filepicker.FilePickerCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

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
    private SeekBar seekBr;
    private SeekBar seekCo;
    private LinearLayout containerView;
    private View menu;
    private View panelArrow;

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

        panelArrow = findViewById(R.id.panelArrow);
        panelArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerView.removeAllViews();
                containerView.addView(menu);
                panelArrow.setVisibility(View.GONE);
            }
        });
        containerView = (LinearLayout) findViewById(R.id.bottomMenuContainer);
        menu = getLayoutInflater().inflate(R.layout.filter_menu, null, false);
        containerView.addView(menu);
        menu.findViewById(R.id.brMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View contrast = getLayoutInflater().inflate(R.layout.filter_contrast, null, false);
                containerView.removeAllViews();
                containerView.addView(contrast);

                seekBr = (SeekBar) findViewById(R.id.seekBr);
                seekCo = (SeekBar) findViewById(R.id.seekCon);

                seekBr.setMax(100);
                seekCo.setMax(200);
                seekBr.setProgress(50);
                seekCo.setProgress(100);

                seekBr.setOnSeekBarChangeListener(GifPreviewActivity.this);
                seekCo.setOnSeekBarChangeListener(GifPreviewActivity.this);
                panelArrow.setVisibility(View.VISIBLE);
            }
        });

        imageView.setImageDrawable(animation);
        animation.start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setContrastBrightness(cm, seekCo.getProgress() / 100f, seekBr.getProgress() - 50);
        animation.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class GifSaveTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(GifPreviewActivity.this, "Making gif",
                    "Please, wait", true, false);
        }

        @Override
        protected Void doInBackground(String[] params) {
            try {
                savePicturesWithEffects(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Toast.makeText(GifPreviewActivity.this, "Animation was created", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePicturesWithEffects(String pathToSave) throws Exception {
        FileOutputStream fos = new FileOutputStream(pathToSave);
        GifEncoder encoder = new GifEncoder();
        encoder.start(fos);

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
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 1, stream);
            encoder.addFrame(resultBitmap);
        }
        encoder.finish();
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
                Intent intent = FileExplorerHelper.getFolderPickerIntent(GifPreviewActivity.this);
                startActivityForResult(intent, requestCode);
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
                                //savePicturesWithEffects(folder + "/" + filename + ".gif");
                                new GifSaveTask().execute(folder + "/" + filename + ".gif");
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
