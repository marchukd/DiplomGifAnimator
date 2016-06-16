package com.marchukdmytro.android.gifanimator.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.marchukdmytro.android.gifanimator.Constants;
import com.marchukdmytro.android.gifanimator.R;
import com.marchukdmytro.android.gifanimator.view_pager.ViewPagerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends Activity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        recyclerView = (RecyclerView) findViewById(R.id.rvGallery);
        GridLayoutManager rvManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(rvManager);
        recyclerView.setHasFixedSize(true);

        File folderGallery = new File(Constants.GALLERY_FOLDER);
        if (!folderGallery.exists())
            folderGallery.mkdir();
        File[] files = new File(Constants.GALLERY_FOLDER).listFiles();
        List<String> pictures = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory())
                continue;
            pictures.add(file.getPath());
        }
        GalleryRecyclerAdapter adapter = new GalleryRecyclerAdapter(pictures, new GalleryItemListener() {
            @Override
            public void onClick(int index) {
                Intent intent = ViewPagerActivity.newInstance(GalleryActivity.this, index);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
