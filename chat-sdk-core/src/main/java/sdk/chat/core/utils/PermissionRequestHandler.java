package sdk.chat.core.utils;

import android.Manifest;
import android.app.Activity;

import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.pmw.tinylog.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.R;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by ben on 9/28/17.
 */

public class PermissionRequestHandler {

    public static boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(ChatSDK.ctx(), permission) != PERMISSION_DENIED;
    }

    public static boolean permissionGranted(String... permissions) {
        for (String permission: permissions) {
            if (!permissionGranted(permission)) {
                return false;
            }
        }
        return true;
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

    public static Completable requestPermissions(final Activity activity, List<String> permissions) {
        return Completable.create(emitter -> {
            Logger.debug("Start Dexter " + new Date().getTime());
            try {
                Dexter.withActivity(activity)
                        .withPermissions(permissions)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    Logger.debug("Dexter Complete" + new Date().getTime());
                                    emitter.onComplete();
                                } else {
                                    Logger.debug("Dexter Error" + new Date().getTime());
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
        }).subscribeOn(RX.main()).observeOn(RX.main());    }

    public static Completable requestPermissions(final Activity activity, String... permissions) {
        return requestPermissions(activity, Arrays.asList(permissions));
    }

}
