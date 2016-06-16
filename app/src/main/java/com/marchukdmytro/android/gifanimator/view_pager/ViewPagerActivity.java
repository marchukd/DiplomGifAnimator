package com.marchukdmytro.android.gifanimator.view_pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.marchukdmytro.android.gifanimator.Constants;
import com.marchukdmytro.android.gifanimator.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends FragmentActivity {
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private static String TAG = "tag";
    private int position;

    public static Intent newInstance(Context c, int position) {
        Intent intent = new Intent(c, ViewPagerActivity.class);
        intent.putExtra(TAG, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_activity);

        position = getIntent().getIntExtra(TAG, 0);
        pager = (ViewPager) findViewById(R.id.pager);

        List<String> pictures = new ArrayList<>();
        File[] files = new File(Constants.GALLERY_FOLDER).listFiles();
        for (File f : files) {
            if (f.isFile())
                pictures.add(f.getPath());
        }

        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), pictures);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(position);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<String> data;

        public MyFragmentPagerAdapter(FragmentManager fm, List<String> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(data.get(position));
        }

        @Override
        public int getCount() {
            return data.size();
        }

    }

}