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
import android.util.Log;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        String[] proj = { MediaStore.Images.Media.DATA , MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = activity.getContentResolver()
                .query(uri, proj, null, null, null);

        int column_index;

        // some devices a non valid path so we load them to a temp file in the app cache dir.
        if (uri.toString().startsWith("content://com.sec.android.gallery3d.provider") ||
                uri.toString().startsWith("content://media/external/images/media/"))  {

            File cacheDir = activity.getCacheDir();

            if(!cacheDir.exists())
                cacheDir.mkdirs();

            File old = new File(cacheDir, "ProfileImage.jpg");
            if (old.exists())
                old.delete();

            File f = new File(cacheDir, "ProfileImage.jpg");

            InputStream is;
            try {
                is = activity.getContentResolver().openInputStream(uri);
                OutputStream os = new FileOutputStream(f);
                IOUtils.copy(is, os);
                os.close();

                return f.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        column_index = cursor.getColumnIndexOrThrow(proj[0]);

        cursor.moveToFirst();

        String path = cursor.getString(column_index);

        cursor.close();
        return path;
    }

    public static File getFile(Activity activity, Uri uri) {
        return  new File(Uri.parse(getRealPathFromURI(activity, uri)).getPath());
    }

    public static int getColorFromDec(String color){
        String[] split = color.split(" ");

        if (split.length != 4)
            return BMessage.randomColor();

        int bubbleColor = -1;

        bubbleColor = Color.argb(Integer.parseInt(split[3]), (int) (255 * Float.parseFloat(split[0])), (int) (255 * Float.parseFloat(split[1])), (int) (255 * Float.parseFloat(split[2])));

        return bubbleColor;
    }

    public static  class ImageSaver{
        public static final String IMAGE_DIR_NAME = "AndroidChatSDKImage";
        public static final String THUMBNAILS_DIR_NAME = "thumbnails";
        public static final String LOCATION_DIR_NAME = "location_snapshots";
        public static final String MAIN_DIR_NAME = "AndroidChatSDKTTestAppDir";

        public static final String IMG_FILE_ENDING = ".jpg";
        public static final String PNG_FILE_ENDING = ".9.png";

        static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
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

        static File saveFile(File dir, String name, String type, Bitmap image, boolean external){
            return saveFile(dir, name, type, image, Bitmap.CompressFormat.JPEG, external);
        }

        private static File saveFile(File dir, String name, String type, Bitmap image, Bitmap.CompressFormat compressFormat, boolean external){

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
                    image.compress(compressFormat, 80, stream);
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

        private static final String BubbleDir = "Bubbles";
        public static Bitmap[] fetchOrCreateBubbleForColor(Context context, int color){
            File dir = getInternalDir(context, BubbleDir);

            Bitmap[] bubbles = new Bitmap[2];

            if (dir.exists())
            {
               String hexName = /*Integer.toHexString(color)*/"Names";
                Log.d(TAG, "HexName: " + hexName);

                Log.d(TAG, "Left not exist");
                bubbles[0] = ImageUtils.get_ninepatch(R.drawable.bubble_left, 26, 26, context);
                bubbles[0] = ImageUtils.replaceIntervalColor(bubbles[0], 40, 75, 130, 140, 190, 210, color);
                saveFile(dir, hexName + "L", PNG_FILE_ENDING, bubbles[0], Bitmap.CompressFormat.PNG, false);

                Log.d(TAG, "Right not exist");
                bubbles[1] = ImageUtils.get_ninepatch(R.drawable.bubble_right, 26, 26, context);
                bubbles[1] = ImageUtils.replaceIntervalColor(bubbles[1], 40, 75, 130, 140, 190, 210, color);
                saveFile(dir, hexName + "R", PNG_FILE_ENDING, bubbles[1], Bitmap.CompressFormat.PNG , false);

                return bubbles;
            }







            Log.d(TAG, "Dir not exist");
            return null;
        }
    }

    public static class LocationImageHandler extends ImageSaver{
        public static File saveLocationImage(Context context, Bitmap image, String name){
            if (!createInternalDirIfNotExists(context, LOCATION_DIR_NAME))
                return null;

            if (name == null) name = DaoCore.generateEntity();
            return saveFile(getInternalDir(context, LOCATION_DIR_NAME), name, IMG_FILE_ENDING, image, false);
            // TODO delete file after used.
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
            return saveFile(getInternalDir(context, THUMBNAILS_DIR_NAME), name, IMG_FILE_ENDING, image, false);
        }

        public static File getThumbnail(Context context, String fileEntityID){
            if (!createInternalDirIfNotExists(context, THUMBNAILS_DIR_NAME))
                return null;

            return new File(getInternalDir(context, THUMBNAILS_DIR_NAME).getPath(), fileEntityID + IMG_FILE_ENDING);
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

