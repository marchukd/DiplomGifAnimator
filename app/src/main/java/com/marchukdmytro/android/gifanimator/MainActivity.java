package com.marchukdmytro.android.gifanimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.marchukdmytro.android.gifanimator.filepicker.FileExplorerHelper;
import com.marchukdmytro.android.gifanimator.filepicker.FilePickerCallback;
import com.marchukdmytro.android.gifanimator.gallery.GalleryActivity;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private int requestCode = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_new).setOnClickListener(this);
        findViewById(R.id.bt_gallery).setOnClickListener(this);
        findViewById(R.id.bt_viewer).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.bt_new:
                intent = new Intent(MainActivity.this, TakeGifFromCameraActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_gallery:
                intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_viewer:
                intent = FileExplorerHelper.getFilePickerIntent(MainActivity.this);
                startActivityForResult(intent, requestCode);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            FileExplorerHelper.onActivityResult(new FilePickerCallback() {
                @Override
                public void pick(String path) {
                    Intent intent = ViewerActivity.newInstance(MainActivity.this, path);
                    startActivity(intent);
                }
            }, data);
        }
    }
}
