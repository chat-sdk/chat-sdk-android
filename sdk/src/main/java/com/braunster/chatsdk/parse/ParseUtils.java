package com.braunster.chatsdk.parse;

import android.graphics.Bitmap;
import android.util.Log;

import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

/**
 * Created by braunster on 11/07/14.
 */
public class ParseUtils {
    public static final String TAG = ParseUtils.class.getSimpleName();
    public static final boolean DEBUG = true;

    public static void saveImageFileToParse(final String path, final SaveCompletedListener listener) {

        //  Loading the bitmap
        Bitmap b = ImageUtils.getCompressed(path);

        if (DEBUG) Log.d(TAG,
                "Saving bitmap, Size: " + String.valueOf(ImageUtils.byteSizeOf(b))
                + "Width: " + b.getWidth() + ", Height: " + b.getHeight() );

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));

        save(parseFile, listener);
    }

    public static void saveImageFileToParseWithThumbnail(final String path, final int thumbnailSize, MultiSaveCompletedListener listener){
        //  Loading the bitmap
        Bitmap b = ImageUtils.getCompressed(path);

        if (b == null) {
            listener.onSaved(new ParseException(ParseException.OTHER_CAUSE, "Image is null"));
            return;
        }

        Bitmap thumbnail = ImageUtils.getCompressed(path, thumbnailSize, thumbnailSize);

        if (DEBUG) Log.d(TAG,
                "Saving bitmap, Size: " + String.valueOf(ImageUtils.byteSizeOf(b))
                        + "Width: " + b.getWidth() + ", Height: " + b.getHeight() );

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));
        final ParseFile thumbnailFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(thumbnail, 50));

        String imageDimentions = ImageUtils.getDimensionString(b);

        save(parseFile, thumbnailFile, imageDimentions, listener);
    }

    public static void saveImageToParse(Bitmap b, int size, final SaveCompletedListener listener){
        b = ImageUtils.scaleImage(b, size);

        if (DEBUG) Log.d(TAG,
                "Saving bitmap, Size: " + String.valueOf(ImageUtils.byteSizeOf(b))
                        + "Width: " + b.getWidth() + ", Height: " + b.getHeight() );

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));

        // Save
        save(parseFile, listener);
    }

    private static void save(final ParseFile parseFile, final SaveCompletedListener listener){
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving: " + parseFile.getName() + " --- " + e.getMessage());
                    listener.onSaved(e, "");
                    return;
                }

                listener.onSaved(null, parseFile.getUrl());
            }
        });
    }

    private static void save(final ParseFile parseFile, final ParseFile thumnailFile, final String imageDimentions, final MultiSaveCompletedListener listener){
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving: " + parseFile.getName() + " --- " + e.getMessage());
                    listener.onSaved(e, "");
                    return;
                }
                else thumnailFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                        {
                            if (DEBUG) Log.e(TAG, "Parse Exception while saving: " + thumnailFile.getName() + " --- " + e.getMessage());
                            listener.onSaved(e, "");
                            return;
                        }
                        else listener.onSaved(null, parseFile.getUrl(), thumnailFile.getUrl(), imageDimentions);
                    }
                });
            }
        });
    }

    private static byte[] getByteArray(Bitmap bitmap){
        return getByteArray(bitmap, 80);
    }

    private static byte[] getByteArray(Bitmap bitmap, int quality){
        // Converting file to a JPEG and then to byte array.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    public interface SaveCompletedListener{
        public void onSaved(ParseException exception, String url);
    }

    public interface MultiSaveCompletedListener{
        public void onSaved(ParseException exception, String... data);
    }
}
