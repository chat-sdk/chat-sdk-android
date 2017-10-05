/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;

import co.chatsdk.core.types.Defines;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import timber.log.Timber;


public class Utils {

    public static  class ImageSaver {

        static boolean isExternalStorageWritable() {
            return Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() );
        }

        public static File getAlbumStorageDir(String albumName) {
            if (isExternalStorageWritable()) {
//              Get the directory for the user's public pictures directory.
                
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), albumName);

                if (file.exists()) {
                    return file;
                }
                if (!file.mkdirs()) {
                    return null;
                } else if (!file.isDirectory()) {
                    return null;
                }

                return file;
            }
            else
            {
                File storage;
                
                // Try to get the image directory anyway, 
                // If fails try to create a directory path with a given name in the external storage.
                storage = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), albumName);;

                if (!storage.exists()) {
                    if (!storage.mkdirs()) {

                        storage =new File(Environment.getExternalStorageDirectory(), albumName);
                        
                        if (!storage.exists()) {
                            if (!storage.mkdirs()) {
                                return null;
                            }
                            else return storage;
                        }
                        return storage;
                    }
                    else return storage;
                }
                return storage;
            }
        }
    }

    public static class SystemChecks {
        /** Check if this device has a camera
         * @return <b>true</b> if the device has a camera.*/
        public static boolean checkCameraHardware(Context context) {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                // this device has a camera
                return true;
            } else {
                // no camera on this device
                return false;
            }
        }
    }
}

