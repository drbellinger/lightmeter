package com.houseplantjournal.plantlightmeter;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Dave on 4/2/2017.
 */

public class ImageSaver implements Runnable {

    private final Image mImage;
    private final File mFile;

    public ImageSaver(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {

        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(mFile);
            output.write(bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mImage.close();
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}