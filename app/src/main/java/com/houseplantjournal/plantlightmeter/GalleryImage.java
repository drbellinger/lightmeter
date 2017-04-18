package com.houseplantjournal.plantlightmeter;

import android.graphics.Bitmap;

/**
 * Created by dave on 18/04/17.
 */

public class GalleryImage {

    private Bitmap image;

    public GalleryImage(Bitmap image) {
        super();
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

}
