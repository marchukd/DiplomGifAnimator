package com.marchukdmytro.android.gifanimator.gallery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marchukdmytro.android.gifanimator.R;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Dmytro on 16.06.2016.
 */
public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ViewHolder> {
    private final GalleryItemListener listener;
    private List<String> pictures;

    public GalleryRecyclerAdapter(List<String> pictures, GalleryItemListener listener) {
        this.listener = listener;
        this.pictures = pictures;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final String item = pictures.get(i);
        String name = item.substring(item.lastIndexOf("/") + 1, item.lastIndexOf("."));
        viewHolder.name.setText(name);
        GifDrawable bmpDrawable = null;
        try {
            bmpDrawable = new GifDrawable(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bmpDrawable.setLoopCount(65535);
        viewHolder.icon.setImageDrawable(bmpDrawable);
        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(i);
            }
        });
    }

    public int getItemCount() {
        return pictures.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private GifImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.recyclerViewItemName);
            icon = (GifImageView) itemView.findViewById(R.id.recyclerViewItemIcon);
        }
    }
}
