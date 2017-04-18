package com.houseplantjournal.plantlightmeter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 18/04/17.
 */

public class GalleryGridviewAdapter extends ArrayAdapter<GalleryImage> {

    private Context context;
    private int layoutResourceId;
    private List<GalleryImage> data = new ArrayList<GalleryImage>();

    public GalleryGridviewAdapter(Context context, int layoutResourceId, List<GalleryImage> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) row.findViewById(R.id.gallery_image);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        GalleryImage item = data.get(position);
        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    static class ViewHolder {
        ImageView image;
    }
}