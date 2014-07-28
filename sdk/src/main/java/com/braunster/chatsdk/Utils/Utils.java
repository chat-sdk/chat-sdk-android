package com.braunster.chatsdk.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.braunster.chatsdk.dao.core.DaoCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by itzik on 6/9/2014.
 */
public class Utils {

    static final String TAG = Utils.class.getSimpleName();
    static final boolean DEBUG = true;

    public static String getSHA(Activity activity, String packageInfo){
        if (DEBUG) Log.d(TAG, "PackageName: " + packageInfo);
        // Add code to print out the key hash
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                  packageInfo,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                if (DEBUG) Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return "NameNotFoundException";
        } catch (NoSuchAlgorithmException e) {
            return "NoSuchAlgorithmException";
        }

        return "Error";
    }

    public static String getRealPathFromURI(Activity activity, Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.getContentResolver()
                .query(uri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(proj[0]);

        cursor.moveToFirst();

        String path = cursor.getString(column_index);

        cursor.close();
        return path;
    }

    public static File getFile(Activity activity, Uri uri) throws NullPointerException{
        return  new File(Uri.parse(getRealPathFromURI(activity, uri)).getPath());
    }

    static  class ImageSaver{

        public static final String IMAGE_DIR_NAME = "AndroidChatSDKTestImage";
        public static final String THUMBNAILS_DIR_NAME = "thumbnails";
        public static final String LOCATION_DIR_NAME = "location_snapshots";
        public static final String MAIN_DIR_NAME = "AndroidChatSDKTTestAppDir";

        public static final String IMG_FILE_ENDING = ".jpg";

        static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        public static boolean createAppDirIfNotExists(Context context) {
            if (DEBUG) Log.v(TAG, "createAppDirIfNotExists");
            boolean ret = true;
            File mydir = context.getDir(MAIN_DIR_NAME, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                ret = false;
            }

            return ret;
        }

        public static boolean createInternalDirIfNotExists(Context context, String name) {
            if (DEBUG) Log.v(TAG, "createAppDirIfNotExists");
            boolean ret = true;
            File mydir = context.getDir(name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                ret = false;
            }

            return ret;
        }

        static File getInternalDir(Context context, String name){
            File mydir = context.getDir(name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                return null;
            }

            return mydir;
        }

        static File saveFile(Context context,File dir, String name, String type, Bitmap image, boolean external){

            if (external)
            {
                if (!isExternalStorageWritable())
                {
                    if (Utils.DEBUG) Log.d(Utils.TAG, "No External storage.");
                    return null;
                }
            }
            // TODO check for internal dir
//            else if (!createAppDirIfNotExists(path, name))
//                return null;

            OutputStream stream = null;
            try {
                if (dir == null) {
                    return null;
                }

                String filePath = dir.getPath()+ File.separator + name + type;
                if (DEBUG) Log.d(TAG, "FilePath: " + filePath);

                File file = new File(filePath);
                if (file.createNewFile())
                {
                    stream = new FileOutputStream(file);
                    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
                    image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    stream.flush();
                    stream.close();
                    return file;
                }
                if (DEBUG) Log.e(TAG, "Unable to create file.");
                return null;

            } catch (FileNotFoundException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            }
        }

        public static String getPath(Context context, String name){
           return getInternalDir(context, name).getPath();
        }
    }

    public static class LocationImageHandler extends ImageSaver{
        public static File saveLocationImage(Context context, Bitmap image, String name){
            if (!createInternalDirIfNotExists(context, LOCATION_DIR_NAME))
                return null;

            if (name == null) name = DaoCore.generateEntity();
            return saveFile(context,getInternalDir(context, LOCATION_DIR_NAME), name, IMG_FILE_ENDING, image, false);
        }

        public static File getLocationFile(Context context, String fileEntityID){
            if (!createInternalDirIfNotExists(context, LOCATION_DIR_NAME))
                return null;

            return new File(getInternalDir(context, LOCATION_DIR_NAME).getPath(), fileEntityID + IMG_FILE_ENDING);
        }
    }

    public static class ThumbnailsHandler extends ImageSaver{
        public static File saveImageThumbnail(Context context, Bitmap image, String name){
            if (!createInternalDirIfNotExists(context, THUMBNAILS_DIR_NAME))
                return null;

            if (name == null) name = DaoCore.generateEntity();
            return saveFile(context, getInternalDir(context, THUMBNAILS_DIR_NAME), name, IMG_FILE_ENDING, image, false);
        }

        public static File getThumbnail(Context context, String fileEntityID){
            if (!createInternalDirIfNotExists(context, THUMBNAILS_DIR_NAME))
                return null;

            return new File(getInternalDir(context, THUMBNAILS_DIR_NAME).getPath(), fileEntityID + IMG_FILE_ENDING);
        }
    }

    public static class FileSaver{

        public static final String IMAGE_DIR_NAME = "AndroidChatSDK";
        public static final String THUMBNAILS_DIR_NAME = "thumbnails";
        public static final String LOCATION_DIR_NAME = "location_snapshots";

        public static final String appDireName = "AndroidChatSDK";

        public static final String filePath = Environment.getExternalStorageDirectory()
                + File.separator + appDireName + File.separator ;

        public static final String IMG_FILE_ENDING = ".jpg";

        public static File saveImage(Context context, Bitmap image, String name){
            if (name == null) name = DaoCore.generateEntity();
            File file =  saveFile(getAlbumStorageDir(IMAGE_DIR_NAME).getAbsolutePath(),
                   name, IMG_FILE_ENDING, image, true);

            if (file != null)
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                if(DEBUG) Log.i(TAG, "Scanned " + path + ":");
                                if(DEBUG) Log.i(TAG, "-> uri=" + uri);
                            }
                        });

            return file;
        }

        public static File saveLocationImage(Context context, Bitmap image, String name){
            if (!createAppDirIfNotExists(context))
                return null;

            if (name == null) name = DaoCore.generateEntity();
            return saveFile(context,getInternalDir(context, LOCATION_DIR_NAME), name, IMG_FILE_ENDING, image, false);
        }

        public static File saveImageThumbnail(Context context, Bitmap image, String name){
            if (!createInternalDirIfNotExists(context, THUMBNAILS_DIR_NAME))
                return null;

            if (name == null) name = DaoCore.generateEntity();
            return saveFile(context, getInternalDir(context, THUMBNAILS_DIR_NAME), name, IMG_FILE_ENDING, image, false);
        }

        private static File saveFile(String path,String name, String type, Bitmap image, boolean external){

            if (external)
            {
                if (!isExternalStorageWritable())
                {
                    if (Utils.DEBUG) Log.d(Utils.TAG, "No External storage.");
                    return null;
                }
            }
            // TODO check for internal dir
//            else if (!createAppDirIfNotExists(path, name))
//                return null;

            OutputStream stream = null;
            try {
                // Save the file to the internal/external directory.jpg"
                // Path == null writing to the internal else to the path specified.
                String filePath = File.separator+ path + File.separator + name + type;
                if (DEBUG) Log.d(TAG, "FilePath: " + filePath);
                File file = new File("/data" + filePath);
                if (file.createNewFile())
                {
                    stream = new FileOutputStream(file);
                    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
                    image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    stream.flush();
                    stream.close();
                    return file;
                }
                return null;

            } catch (FileNotFoundException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            }
        }

        private static File saveFile(Context context,File dir, String name, String type, Bitmap image, boolean external){

            if (external)
            {
                if (!isExternalStorageWritable())
                {
                    if (Utils.DEBUG) Log.d(Utils.TAG, "No External storage.");
                    return null;
                }
            }
            // TODO check for internal dir
//            else if (!createAppDirIfNotExists(path, name))
//                return null;

            OutputStream stream = null;
            try {
                if (dir == null) {
                    return null;
                }

                String filePath = dir.getPath()+ File.separator + name + type;
                if (DEBUG) Log.d(TAG, "FilePath: " + filePath);

                File file = new File(filePath);
                if (file.createNewFile())
                {
                    stream = new FileOutputStream(file);
                    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
                    image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    stream.flush();
                    stream.close();
                    return file;
                }
                if (DEBUG) Log.e(TAG, "Unable to create file.");
                return null;

            } catch (FileNotFoundException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
                e.printStackTrace();
                return null;
            }
        }




        /* Checks if external storage is available for read and write */
        private static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        public static File getAlbumStorageDir(String albumName) {
            if (!isExternalStorageWritable())
            {
                if (Utils.DEBUG) Log.e(Utils.TAG, "External Storage is not writable.");
                return null;
            }

            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), albumName);

            if (file.exists())
            {
                if (Utils.DEBUG) Log.e(Utils.TAG, "public picture album exist");
                return file;
            }
            if (!file.mkdirs()) {
                if (Utils.DEBUG) Log.e(Utils.TAG, "Directory not created");
                return null;
            }else if (!file.isDirectory())
            {
                if (Utils.DEBUG) Log.e(Utils.TAG, "saveImage, getAlbumDir file is not a Directory");
                return null;
            }
            if (Utils.DEBUG) Log.d(Utils.TAG, "Album dir fetched successfully.");

            return file;
        }

        public static boolean createAppDirIfNotExists(Context context) {
            if (DEBUG) Log.v(TAG, "createAppDirIfNotExists");
            boolean ret = true;
            File mydir = context.getDir(appDireName, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                ret = false;
            }

            return ret;
        }

        public static boolean createInternalDirIfNotExists(Context context, String name) {
            if (DEBUG) Log.v(TAG, "createAppDirIfNotExists");
            boolean ret = true;
            File mydir = context.getDir(appDireName + File.separator + name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                ret = false;
            }

            return ret;
        }

        private static File getInternalDir(Context context, String name){
            File mydir = context.getDir(appDireName + File.separator + name, Context.MODE_PRIVATE);

            if (!mydir.exists()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                return null;
            }

            return mydir;
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
/*


    public static boolean createDirIfNotExists(String path, String name) {
        if (DEBUG) Log.v(TAG, "createAppDirIfNotExists, Path: " + path + ", Name: " + name);
        boolean ret = true;

        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(Utils.TAG, "Problem creating Image folder");
                ret = false;
            }
        }

        return ret;
    }*/
