package com.marchukdmytro.android.gifanimator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ViewerActivity extends AppCompatActivity {
    private static final String TAG = "viewer-activity";

    public static Intent newInstance(Context c, String path) {
        Intent intent = new Intent(c, ViewerActivity.class);
        intent.putExtra(TAG, path);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        String path = getIntent().getStringExtra(TAG);
        try {
            GifDrawable gifDrawable = new GifDrawable(path);
            gifDrawable.setLoopCount(65535);
            GifImageView gif = (GifImageView) findViewById(R.id.gifImageView);
            gif.setImageDrawable(gifDrawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
