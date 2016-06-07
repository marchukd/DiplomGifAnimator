package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends Activity {

    private static String TAG = "gif-preview-activity-tag";
    private AnimationDrawable animation;
    private BitmapDrawable frame;

    public static Intent newInstance(Context context, String gifPath) {
        Intent intent = new Intent(context, GifPreviewActivity.class);
        intent.putExtra(TAG, gifPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(TAG);
        setContentView(R.layout.preview_activity);
        ImageView view = (ImageView) findViewById(R.id.gifView);
        try {
            animation = new AnimationDrawable();
            for (int i = 0; i < 15; i++) {
                frame = new BitmapDrawable(Environment.getExternalStorageDirectory().getPath()
                        + "/animation/" + i + "out.jpg");
                animation.addFrame(frame, 100);
            }
            animation.setOneShot(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        animation.setColorFilter(filter);

        view.setImageDrawable(animation);
        animation.start();
    }
}
