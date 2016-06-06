package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Dmytro on 06.06.2016.
 */
public class GifPreviewActivity extends Activity {
    private static String TAG = "gif-preview-activity-tag";
    private String path;
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
        try {
            GifImageView view = ((GifImageView) findViewById(R.id.gifView));
            GifDrawable drawable = new GifDrawable(path);
            drawable.setLoopCount(32768);
            view.setImageDrawable(drawable);;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
