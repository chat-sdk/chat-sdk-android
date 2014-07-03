package com.braunster.chatsdk.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private static String getRealPathFromURI(Activity activity, Uri uri){
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

    public static Bitmap decodeFrom64(byte[] bytesToDecode){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        byte[] bytes = Base64.decode(bytesToDecode, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    public static Bitmap loadBitmapFromFile(String photoPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        return bitmap;
    }


    public static class FileSaver{

        public static final String filePath = Environment.getExternalStorageDirectory()
                + "/AndroidChatSdk/" ;

        public static final String appDireName = "AndroidChatSDK";
        public static final String imageDirName = "AndroidChatSDK";
        public static final String IMG_FILE_ENDING = ".jpg";

        public static File saveImage(Context context, Bitmap image, String name){
            if (name == null) name = DaoCore.generateEntity();
            File file =  saveFile(getAlbumStorageDir(imageDirName).getAbsolutePath(),
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

        public static File saveImage(Context context, Bitmap image, String name, String compiledPath){
            if (name == null) name = DaoCore.generateEntity();
            if(DEBUG) Log.d(TAG, getAlbumStorageDir(imageDirName).getAbsolutePath());
            compiledPath = getAlbumStorageDir(imageDirName).getAbsolutePath()  + File.separator + name + IMG_FILE_ENDING;
            if(DEBUG) Log.d(TAG, "FilePAthCompiled: " + compiledPath);
            return  saveImage(context, image, name);
        }
/*
        public static boolean saveLocationImage(Bitmap image, String name){
            if (name == null) name = DaoCore.generateEntity();
            return  saveFile(appDireName,name, IMG_FILE_ENDING, image, false);
        }

        public static boolean saveLocationImage(Bitmap image, String name, String filePathCompiled){
            if (name == null) name = DaoCore.generateEntity();
            filePathCompiled = appDireName + name + IMG_FILE_ENDING;
            return  saveFile(appDireName, name, IMG_FILE_ENDING, image, false);
        }*/

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
//            else if (!createDirIfNotExists(path))
//                return false;

            OutputStream stream = null;
            try {
                // Save the file to the internal/external directory.jpg"
                // Path == null writing to the internal else to the path specified.
                stream = new FileOutputStream(path + File.separator + name + type);
                    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
                image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                stream.flush();
                stream.close();
                return new File(path + File.separator + name + type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                if (Utils.DEBUG) Log.d(Utils.TAG, "Unable to save file.");
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

        public static boolean createDirIfNotExists(Context context, String path, String name) {
            boolean ret = true;

            File file = new File(path, name);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e(Utils.TAG, "Problem creating Image folder");
                    ret = false;
                }
            }

            return ret;
        }
    }
}

