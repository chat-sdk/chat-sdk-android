package co.chatsdk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import java.util.List;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.pmw.tinylog.Logger;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by ben on 9/28/17.
 */

public class PermissionRequestHandler {

    public static boolean recordPermissionGranted() {
        return permissionGranted(Manifest.permission.RECORD_AUDIO);
    }

    public static boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(ChatSDK.shared().context(), permission) != PERMISSION_DENIED;
    }

    public static Completable requestRecordAudio(Activity activity) {
        return requestPermissions(activity, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static Completable requestWriteExternalStorage(Activity activity) {
        return requestPermissions(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static Completable requestManageDocumentsStorage(Activity activity) {
        return requestPermissions(activity, Manifest.permission.MANAGE_DOCUMENTS);
    }

    public static Completable requestReadExternalStorage(Activity activity) {
        return requestPermissions(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static Completable requestImageMessage(Activity activity) {
        return requestPermissions(activity, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    public static Completable requestCameraAccess(Activity activity) {
        return requestPermissions(activity, Manifest.permission.CAMERA);
    }

    public static Completable requestVideoAccess(Activity activity) {
        return requestPermissions(activity, Manifest.permission.CAPTURE_VIDEO_OUTPUT);
    }

    public static Completable requestReadContact(Activity activity) {
        return requestPermissions(activity, Manifest.permission.READ_CONTACTS);
    }

    public static Completable requestLocationAccess(Activity activity) {
        return requestPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static Completable requestPermissions(final Activity activity, String... permissions) {
        return Completable.create(emitter -> {
            Logger.debug("Start Dexter");
            try {
                Dexter.withActivity(activity)
                        .withPermissions(permissions)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    emitter.onComplete();
                                } else {
                                    emitter.onError(new Throwable(activity.getString(R.string.permission_denied)));
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions1, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            } catch (Exception e) {
                Logger.error(e);
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
    }

}
