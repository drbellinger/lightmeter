package com.houseplantjournal.plantlightmeter;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dave on 4/9/2017.
 */

public class GalleryFragment extends Fragment implements View.OnClickListener {

    private GridView gridView;
    private GalleryGridviewAdapter gridviewAdapter;

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.gallery_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "created gallery fragment", Toast.LENGTH_LONG).show();
                }
            });
        }

        gridView = (GridView) view.findViewById(R.id.gallery_gridview);
        gridviewAdapter = new GalleryGridviewAdapter(this.getContext(), R.layout.gallery_entry, getData());
        gridView.setAdapter(gridviewAdapter);

    }

    @Override
    public void onClick(View v) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "click on gallery fragment", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Prepare some dummy data for gridview
     */
    private List<GalleryImage> getData() {

        final List<GalleryImage> imageItems = new ArrayList<>();
        /*TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new GalleryImage(bitmap));
        }*/
        return imageItems;
    }

}
