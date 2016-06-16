package com.marchukdmytro.android.gifanimator.view_pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marchukdmytro.android.gifanimator.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PageFragment extends Fragment {

    private static final String ITEM_URL = "item_path";
    private String gifPath;

    static PageFragment newInstance(String path) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ITEM_URL, path);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gifPath = getArguments().getString(ITEM_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, null);
        GifImageView imageView = (GifImageView) view.findViewById(R.id.image_view_in_pagefragment);
        try {
            GifDrawable drawable = new GifDrawable(gifPath);
            drawable.setLoopCount(65535);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }
}