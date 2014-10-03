package com.braunster.chatsdk.object;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.braunster.chatsdk.network.BDefines;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropImageActivity;

/**
 * Created by braunster on 04/09/14.
 */
public class Cropper extends Crop {
    private Uri source;

    static interface Extra {
        String ASPECT_X = "aspect_x";
        String ASPECT_Y = "aspect_y";
        String MAX_X = "max_x";
        String MAX_Y = "max_y";
        String ERROR = "error";
    }

    public Cropper(Uri source) {
        super(source);
        this.source = source;
    }

    public Intent getIntent(Context context, Uri output){
        Intent cropIntent = new Intent();
        cropIntent.setData(source);
        cropIntent.setClass(context, CropImageActivity.class);
        cropIntent.putExtra(Extra.ASPECT_X, BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);
        cropIntent.putExtra(Extra.ASPECT_Y, BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);

        return cropIntent;
    }

    public Intent getIntent(Context context, Uri output, int boxWidth, int boxHeight){
        Intent cropIntent = new Intent();
        cropIntent.setData(source);
        cropIntent.setClass(context, CropImageActivity.class);
        cropIntent.putExtra(Extra.ASPECT_X, boxWidth);
        cropIntent.putExtra(Extra.ASPECT_Y, boxHeight);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);

        return cropIntent;
    }
}
