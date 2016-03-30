/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.parse;

import android.graphics.Bitmap;
import android.os.Handler;

import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.SaveImageProgress;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.ByteArrayOutputStream;

import timber.log.Timber;

public class ParseUtils {
    public static final String TAG = ParseUtils.class.getSimpleName();
    public static final boolean DEBUG = true;

    public static Promise<String, BError, SaveImageProgress> saveImageToParse(final String path) {

        //  Loading the bitmap
        Bitmap b = ImageUtils.getCompressed(path);

        if (b == null) {
            return reject();
        }

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));

        SaveImageProgress saveImageProgress = new SaveImageProgress();

        saveImageProgress.savedImage = b;
        
        return save(parseFile, saveImageProgress);
    }

    public static Promise<String, BError, SaveImageProgress> saveImageToParse(Bitmap b, int size){

        if (b == null) {
            return reject();
        }
        
        b = ImageUtils.scaleImage(b, size);

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));

        SaveImageProgress saveImageProgress = new SaveImageProgress();

        saveImageProgress.savedImage = b;

        // Save
        return save(parseFile, saveImageProgress);
    }

    public static Promise<String[], BError, SaveImageProgress> saveImageFileToParseWithThumbnail(final String path, final int thumbnailSize){
        //  Loading the bitmap
        Bitmap b = ImageUtils.getCompressed(path);

        if (b == null) {
            return rejectMultiple();
        }

        Bitmap thumbnail = ImageUtils.getCompressed(path, thumbnailSize, thumbnailSize);

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));
        final ParseFile thumbnailFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(thumbnail, 50));

        String imageDimentions = ImageUtils.getDimensionAsString(b);

        if (DEBUG) Timber.d("dimensionsString: %s", imageDimentions);

        SaveImageProgress saveImageProgress = new SaveImageProgress();

        saveImageProgress.dimensionsString = imageDimentions;
        saveImageProgress.savedImage = b;
        saveImageProgress.savedImageThumbnail = thumbnail;

        return save(parseFile, thumbnailFile, imageDimentions, saveImageProgress);
    }

    public static Promise<String[], BError, SaveImageProgress> saveBMessageWithImage(BMessage message){
        //  Loading the bitmap
        Bitmap b = ImageUtils.getCompressed(message.getResourcesPath());

        if (b == null) {
            return rejectMultiple();
        }

        Bitmap thumbnail = ImageUtils.getCompressed(message.getResourcesPath(), 
                BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE, 
                BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

        // Saving the image to parse.
        final ParseFile parseFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(b));
        final ParseFile thumbnailFile = new ParseFile(DaoCore.generateEntity() + ".jpeg", getByteArray(thumbnail, 50));

        String imageDimentions = ImageUtils.getDimensionAsString(b);

        if (DEBUG) Timber.d("dimensionsString: %s", imageDimentions);

        SaveImageProgress saveImageProgress = new SaveImageProgress();

        saveImageProgress.dimensionsString = imageDimentions;
        saveImageProgress.savedImage = b;
        saveImageProgress.savedImageThumbnail = thumbnail;

        // Adding the image to the cache
        VolleyUtils.getBitmapCache().put(
                VolleyUtils.BitmapCache.getCacheKey(message.getResourcesPath()),
                saveImageProgress.savedImageThumbnail);

        message.setImageDimensions(saveImageProgress.dimensionsString);
        
        return save(parseFile, thumbnailFile, imageDimentions, saveImageProgress);
    }

    private static Promise<String, BError, SaveImageProgress> save(final ParseFile parseFile,  final SaveImageProgress saveImageProgress){
        final Deferred<String, BError, SaveImageProgress> deferred = new DeferredObject<>();
        
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Timber.e(e.getCause(), "Parse Exception while saving: %s", parseFile.getName());
                    deferred.reject(new BError(BError.Code.PARSE_EXCEPTION, e));
                    return;
                }

                deferred.resolve(parseFile.getUrl());
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                deferred.notify(saveImageProgress);
            }
        }, 100);
        
        return deferred.promise();
    }

    private static Promise<String[], BError, SaveImageProgress> save(final ParseFile parseFile, final ParseFile thumnailFile,
                                                                final String imageDimentions, final SaveImageProgress saveImageProgress){
        final Deferred<String[], BError, SaveImageProgress> deferred = new DeferredObject<>();
        
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Timber.e(e.getCause(), "Parse Exception while saving: %s", parseFile.getName());
                    deferred.reject(new BError(BError.Code.PARSE_EXCEPTION, e));
                }
                else thumnailFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                        {
                            if (DEBUG) Timber.e(e.getCause(), "Parse Exception while saving: %s", thumnailFile.getName());
                            deferred.reject(new BError(BError.Code.PARSE_EXCEPTION, e));
                            return;
                        }
                        else deferred.resolve(new String[]{parseFile.getUrl(), thumnailFile.getUrl(), imageDimentions} );
                    }
                });
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                deferred.notify(saveImageProgress);
            }
        }, 10);
        
        return deferred.promise();
    }

    private static byte[] getByteArray(Bitmap bitmap){
        return getByteArray(bitmap, 50);
    }

    private static byte[] getByteArray(Bitmap bitmap, int quality){
        // Converting file to a JPEG and then to byte array.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private static Promise<String, BError, SaveImageProgress> reject(){
        return new DeferredObject<String, BError, SaveImageProgress>().reject(new BError(BError.Code.NULL, "Image Is Null"));
    }

    private static Promise<String[], BError, SaveImageProgress> rejectMultiple(){
        return new DeferredObject<String[], BError, SaveImageProgress>().reject(new BError(BError.Code.NULL, "Image Is Null"));
    }
    

}
