package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.marchukdmytro.android.gifanimator.filepicker.EnterStringDialog;
import com.marchukdmytro.android.gifanimator.filepicker.FileExplorerHelper;
import com.marchukdmytro.android.gifanimator.filepicker.FilePickerCallback;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static String TAG = "gif-preview-activity-tag";
    private static ColorMatrix brightnessContrastColorMatrix = new ColorMatrix();
    private final int requestCode = 5;
    private AnimationDrawable animation;
    private ColorMatrix effectColorMatrix = new ColorMatrix();
    private String framesPath;
    private SeekBar seekBr;
    private SeekBar seekCo;
    private LinearLayout bottomContainer;
    private View menu;
    private View panelArrow;
    private View.OnClickListener filterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btEffectNone:
                    effectColorMatrix = new ColorMatrix();
                    break;
                case R.id.btEffectSepia:
                    effectColorMatrix = getSepiaMatrix();
                    break;
                case R.id.btEffectGrayScale:
                    effectColorMatrix = getGrayScaleMatrix();
                    break;
            }
            updateAnimationEffects();
            imageView.setImageDrawable(animation);
            animation.start();
        }
    };
    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.brMenu:
                    final View contrast = getLayoutInflater().inflate(R.layout.filter_contrast, null, false);
                    contrast.setLayoutParams(paramsMatchParent);
                    bottomContainer.removeAllViews();
                    bottomContainer.addView(contrast);
                    panelArrow.setVisibility(View.VISIBLE);

                    seekBr = (SeekBar) findViewById(R.id.seekBr);
                    seekCo = (SeekBar) findViewById(R.id.seekCon);

                    seekBr.setProgress(brightnessProgress);
                    seekCo.setProgress(contrastProgress);

                    seekBr.setOnSeekBarChangeListener(GifPreviewActivity.this);
                    seekCo.setOnSeekBarChangeListener(GifPreviewActivity.this);
                    break;
                case R.id.efMenu:
                    final View effects = getLayoutInflater().inflate(R.layout.filter_effects, null, false);
                    effects.setLayoutParams(paramsMatchParent);
                    bottomContainer.removeAllViews();
                    bottomContainer.addView(effects);
                    panelArrow.setVisibility(View.VISIBLE);

                    effects.findViewById(R.id.btEffectNone).setOnClickListener(filterListener);
                    effects.findViewById(R.id.btEffectSepia).setOnClickListener(filterListener);
                    effects.findViewById(R.id.btEffectGrayScale).setOnClickListener(filterListener);
                    break;
                case R.id.speedMenu:
                    final View speedView = getLayoutInflater().inflate(R.layout.filter_speed, null, false);
                    speedView.setLayoutParams(paramsMatchParent);
                    bottomContainer.removeAllViews();
                    bottomContainer.addView(speedView);
                    panelArrow.setVisibility(View.VISIBLE);
                    tvPlayingSpeed = (TextView) speedView.findViewById(R.id.tvPlayingSpeed);

                    SeekBar seekSpeed = (SeekBar) speedView.findViewById(R.id.seekSpeed);
                    seekSpeed.setOnSeekBarChangeListener(GifPreviewActivity.this);
                    seekSpeed.setProgress(playingSpeed);
                    break;
            }
        }
    };
    private View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:
                    animateFAB();
                    break;
                case R.id.fab1:
                    fabSaveToGallery.startAnimation(fabClick1);
                    new EnterStringDialog(GifPreviewActivity.this, new FilePickerCallback() {
                        @Override
                        public void pick(String path) {
                            File galleryFolder = new File(Constants.GALLERY_FOLDER);
                            if (!galleryFolder.exists())
                                galleryFolder.mkdir();
                            new GifSaveTask().execute(Constants.GALLERY_FOLDER + "/" + path + ".gif");
                        }
                    }).show();
                    break;
                case R.id.fab2:
                    fabSaveCustom.startAnimation(fabClick2);
                    Intent intent = FileExplorerHelper.getFolderPickerIntent(GifPreviewActivity.this);
                    startActivityForResult(intent, requestCode);
                    break;
            }
        }
    };
    private Boolean isFabOpen = false;
    private Animation fab_open;
    private Animation fab_close;
    private Animation fabClick1;
    private Animation fabClick2;
    private Animation mainFabOpen;
    private Animation mainFabClose;

    private int brightnessProgress = 50;
    private int contrastProgress = 100;
    private int playingSpeed = 1;
    private ImageView imageView;
    private LinearLayout.LayoutParams paramsMatchParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    private TextView tvPlayingSpeed;
    private FloatingActionButton fab, fabSaveToGallery, fabSaveCustom;
    private Animation fabClick;

    public static Intent newInstance(Context context, String framesPath) {
        Intent intent = new Intent(context, GifPreviewActivity.class);
        intent.putExtra(TAG, framesPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        framesPath = getIntent().getStringExtra(TAG);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fabClick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_item_click_open);
        fabClick1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_item_click_open);
        fabClick2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_item_click_open);
        mainFabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_fab_open);
        mainFabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.main_fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabSaveToGallery = (FloatingActionButton) findViewById(R.id.fab1);
        fabSaveCustom = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(fabListener);
        fabSaveToGallery.setOnClickListener(fabListener);
        fabSaveCustom.setOnClickListener(fabListener);
        fab.startAnimation(fabClick);

        loadAnimation();

        bottomContainer = (LinearLayout) findViewById(R.id.bottomMenuContainer);

        menu = getLayoutInflater().inflate(R.layout.filter_menu, null, false);
        menu.setLayoutParams(paramsMatchParent);
        panelArrow = findViewById(R.id.panelArrow);
        panelArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomContainer.removeAllViews();
                bottomContainer.addView(menu);
                panelArrow.setVisibility(View.GONE);
            }
        });
        bottomContainer.addView(menu);
        menu.findViewById(R.id.brMenu).setOnClickListener(menuClickListener);
        menu.findViewById(R.id.efMenu).setOnClickListener(menuClickListener);
        menu.findViewById(R.id.speedMenu).setOnClickListener(menuClickListener);

        animation.start();
    }

    private void loadAnimation() {
        try {
            imageView = (ImageView) findViewById(R.id.gifView);
            animation = getAnimationFromFolder(framesPath, playingSpeed * 100);
            imageView.setImageDrawable(animation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void animateFAB() {

        if (isFabOpen) {
            fab.startAnimation(mainFabClose);
            fabSaveToGallery.startAnimation(fab_close);
            fabSaveCustom.startAnimation(fab_close);
            fabSaveToGallery.setClickable(false);
            fabSaveCustom.setClickable(false);
            isFabOpen = false;

        } else {
            fab.startAnimation(mainFabOpen);
            fabSaveToGallery.startAnimation(fab_open);
            fabSaveCustom.startAnimation(fab_open);
            fabSaveToGallery.setClickable(true);
            fabSaveCustom.setClickable(true);
            isFabOpen = true;

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBr:
            case R.id.seekCon:
                brightnessProgress = seekBr.getProgress();
                contrastProgress = seekCo.getProgress();
                setContrastBrightness(contrastProgress / 100f, brightnessProgress - 50);
                imageView.setImageDrawable(animation);
                animation.start();
                break;
            case R.id.seekSpeed:
                playingSpeed = seekBar.getProgress() + 1;
                tvPlayingSpeed.setText(getString(R.string.seek_playing_speed)
                        + String.valueOf(playingSpeed * 100));
                updateAnimationEffects();
                imageView.setImageDrawable(animation);
                animation.start();
                break;
        }
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
            dialog = ProgressDialog.show(GifPreviewActivity.this, getString(R.string.alertAnimationCreating),
                    getString(R.string.alertAnimCreating), true, false);
        }

        @Override
        protected Void doInBackground(String[] params) {
            try {
                saveGifWithEffects(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Toast.makeText(GifPreviewActivity.this, getString(R.string.animation_was_created),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGifWithEffects(String pathToSave) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(pathToSave);
        GifEncoder encoder = new GifEncoder();
        encoder.start(outputStream);
        encoder.setDelay(playingSpeed);
        ColorMatrix resultColorMatrix = new ColorMatrix();
        resultColorMatrix.setConcat(brightnessContrastColorMatrix, effectColorMatrix);

        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            Drawable currentFrame = animation.getFrame(i);
            Bitmap bmp = ((BitmapDrawable) currentFrame).getBitmap();
            Bitmap resultBitmap = Bitmap.createBitmap(bmp, 0, 0,
                    bmp.getWidth() - 1, bmp.getHeight() - 1);
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(resultColorMatrix));
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(resultBitmap, 0, 0, paint);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 1, stream);

            encoder.addFrame(resultBitmap);
        }
        encoder.finish();
    }

    private AnimationDrawable getAnimationFromFolder(String framesPath, int duration) throws Exception {
        File[] files = new File(framesPath).listFiles();
        AnimationDrawable animation = new AnimationDrawable();
        for (int i = 0; i < files.length; i++) {
            BitmapDrawable oneFrame = new BitmapDrawable(files[i].getAbsolutePath());
            animation.addFrame(oneFrame, duration);
        }
        animation.setOneShot(false);
        return animation;
    }

    private void setContrastBrightness(float contrast, float brightness) {
        brightnessContrastColorMatrix.set(new float[]{
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0});
        updateAnimationEffects();
    }

    private void updateAnimationEffects() {
        AnimationDrawable updatedAnimation = new AnimationDrawable();
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            updatedAnimation.addFrame(animation.getFrame(i), playingSpeed * 100);
        }
        updatedAnimation.setOneShot(false);
        animation = updatedAnimation;

        ColorMatrix updatedMatrix = new ColorMatrix();
        updatedMatrix.setConcat(effectColorMatrix, brightnessContrastColorMatrix);
        animation.setColorFilter(new ColorMatrixColorFilter(updatedMatrix));
    }

    private ColorMatrix getGrayScaleMatrix() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        return matrix;
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.question_backdialog));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GifPreviewActivity.super.onBackPressed();
            }
        };
        builder.setPositiveButton(getString(R.string.yes), listener);
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            FileUtils.cleanDirectory(new File(Constants.TEMPORARY_FOLDER_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}