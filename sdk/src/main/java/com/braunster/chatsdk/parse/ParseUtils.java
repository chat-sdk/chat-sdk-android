package com.braunster.chatsdk.parse;

import android.graphics.Bitmap;
import android.util.Log;

import com.braunster.chatsdk.Utils.Utils;
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
    public static final boolean DEBUG = false;

    public static void saveImageFileToParse(final String path, final SaveCompletedListener listener) {
        //  Loading the bitmap
        Bitmap b = Utils.loadBitmapFromFile(path);

        // Converting file to a JPEG and then to byte array.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", byteArray);

        // When save is done save the image url in the user metadata.
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving profile pic: " + parseFile.getName() + " --- " + e.getMessage());
                    listener.onSaved(e, "");
                    return;
                }

                listener.onSaved(null, parseFile.getUrl());
            }
        });
    }

    public interface SaveCompletedListener{
        public void onSaved(ParseException exception, String url);
    }
}
