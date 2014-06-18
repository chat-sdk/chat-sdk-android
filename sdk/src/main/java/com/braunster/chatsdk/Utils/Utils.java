package com.braunster.chatsdk.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.core.Entity;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by itzik on 6/9/2014.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static final boolean DEBUG = false;

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

    public static void CopyStream(InputStream is, OutputStream os){
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
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


}
