package com.braunster.chatsdk;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.facebook.android.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
