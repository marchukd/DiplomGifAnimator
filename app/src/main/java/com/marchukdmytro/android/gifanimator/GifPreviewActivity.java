package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifDrawableBuilder;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends Activity {

    private static String TAG = "gif-preview-activity-tag";
    private AnimationDrawable animation;

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
                animation.addFrame(new BitmapDrawable(Environment.getExternalStorageDirectory().getPath()
                        + "/animation/" + i + "out.jpg"), 100);
            }
            animation.setOneShot(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setImageDrawable(animation);
        animation.start();
    }
}
