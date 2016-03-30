/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.network.BDefines;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;


public class Utils {

    static final boolean DEBUG = false;

    public static String getSHA(Activity activity, String packageInfo){
        if (DEBUG) Timber.d("PackageName: %s", packageInfo);
        // Add code to print out the key hash
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    packageInfo,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                if (DEBUG) Timber.d(Base64.encodeToString(md.digest(), Base64.DEFAULT));
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return "NameNotFoundException";
        } catch (NoSuchAlgorithmException e) {
            return "NoSuchAlgorithmException";
        }

        return "Error";
    }

    public static String getRealPathFromURI(Context context, Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA , MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver()
                .query(uri, proj, null, null, null);

        int column_index;

        // some devices a non valid path so we load them to a temp file in the app cache dir.
        if (uri.toString().startsWith("content://com.sec.android.gallery3d.provider") ||
                uri.toString().startsWith("content://media/external/images/media/"))  {

            File cacheDir = context.getCacheDir();

            if(!cacheDir.exists())
                cacheDir.mkdirs();

            File old = new File(cacheDir, "ProfileImage.jpg");
            if (old.exists())
                old.delete();

            File f = new File(cacheDir, "ProfileImage.jpg");

            InputStream is;
            try {
                is = context.getContentResolver().openInputStream(uri);
                OutputStream os = new FileOutputStream(f);

                byte[] buffer = new byte[10240];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }

                os.close();

                return f.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        String path;
        try {
            column_index = cursor.getColumnIndexOrThrow(proj[0]);
            cursor.moveToFirst();

            path = cursor.getString(column_index);

            cursor.close();
        } catch (NullPointerException e) {
            
            // Closing the cursor if he isn't null.
            if (cursor != null) {
                cursor.close();
            }

            // If we cant get a cursor or there was an error we will try the uri default path.
            path = uri.getPath();
        }


        if (DEBUG) Timber.d("Path From URI: %s", path);
        
        return path;
    }

    public static File getFile(Context context, Uri uri) {
        return  new File(Uri.parse(getRealPathFromURI(context, uri)).getPath());
    }

    public static int getColorFromDec(String color){
        String[] split = color.split(" ");

        if (split.length != 4)
            return BMessage.randomColor();

        int bubbleColor = -1;

        bubbleColor = Color.argb(Integer.parseInt(split[3]), (int) (255 * Float.parseFloat(split[0])), (int) (255 * Float.parseFloat(split[1])), (int) (255 * Float.parseFloat(split[2])));

        return bubbleColor;
    }

    public static  class ImageSaver {
        public static final String IMAGE_DIR_NAME = BDefines.ImageDirName;

        public static final String IMG_FILE_ENDING = ".jpg";

        static boolean isExternalStorageWritable() {
            return Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() );
        }

        public static boolean createInternalDirIfNotExists(Context context, String name) {

            boolean ret = true;
            File mydir = context.getDir(name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                ret = false;
            }

            return ret;
        }

        static File getInternalDir(Context context, String name) {
            File mydir = context.getDir(name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                return null;
            }

            return mydir;
        }

        public static File saveFile(File dir, String name, String type, Bitmap image, boolean external) {
            return saveFile(dir, name, type, image, Bitmap.CompressFormat.JPEG, external);
        }

        private static File saveFile(File dir, String name, String type, Bitmap image, Bitmap.CompressFormat compressFormat, boolean external) {

            OutputStream stream = null;
            try {
                if (dir == null) {
                    return null;
                }

                String filePath = dir.getPath() + File.separator + name + type;
                if (DEBUG) Timber.d("FilePath: %s", filePath);

                File file = new File(filePath);
                if (file.createNewFile()) {
                    stream = new FileOutputStream(file);
                    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
                    image.compress(compressFormat, 80, stream);
                    stream.flush();
                    stream.close();
                    return file;
                }
                if (DEBUG) Timber.e("Unable to create file.");
                return null;

            } catch (FileNotFoundException e) {
                if (Utils.DEBUG) Timber.d("Unable to save file.");
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                if (Utils.DEBUG) Timber.d("Unable to save file.");
                e.printStackTrace();
                return null;
            }
        }

        public static String getPath(Context context, String name) {
            return getInternalDir(context, name).getPath();
        }

        public static File getAlbumStorageDir(Context context, String albumName) {
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

    public static class SystemChecks{
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

