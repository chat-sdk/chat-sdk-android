package co.chatsdk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by ben on 9/28/17.
 */

public class PermissionRequestHandler {

    private static final PermissionRequestHandler instance = new PermissionRequestHandler();

    public static PermissionRequestHandler shared () {
        return instance;
    }

    Map<Integer, CompletableEmitter> completableMap = new HashMap<>();

    private static int WRITE_EXTERNAL_STORAGE_REQUEST = 100;
    private static int READ_EXTERNAL_STORAGE_REQUEST = 101;
    private static int RECORD_AUDIO_REQUEST = 102;
    private static int RECORD_VIDEO_REQUEST = 103;
    private static int CAMERA_REQUEST = 104;
    private static int READ_CONTACTS_REQUEST = 105;

//    HashMap<Integer, Boolean> requested = new HashMap<>();

    public Completable requestRecordAudio (Activity activity) {
        return requestPermission(activity, Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_REQUEST);
    }

    public boolean recordPermissionGranted () {
        return ContextCompat.checkSelfPermission(ChatSDK.shared().context(), Manifest.permission.RECORD_AUDIO) != PERMISSION_DENIED;
    }

    public Completable requestWriteExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_REQUEST);
    }

    public Completable requestReadExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_REQUEST);
    }

    public Completable requestCameraAccess (Activity activity) {
        return requestPermission(activity, Manifest.permission.CAMERA, CAMERA_REQUEST);
    }

    public Completable requestVideoAccess (Activity activity) {
        return requestCameraAccess(activity);
//        return requestPermission(activity, Manifest.permission.CAPTURE_VIDEO_OUTPUT, RECORD_VIDEO_REQUEST);
    }

    public Completable requestReadContact (Activity activity) {
        return requestPermission(activity, Manifest.permission.READ_CONTACTS, READ_CONTACTS_REQUEST);
    }

    public Completable requestPermission (final Activity activity, final String permission, final int result) {
        if(completableMap.containsKey(result)) {
            return Completable.complete();
        }
        return Completable.create(e -> {

            completableMap.put(result, e);

            int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission);

            if(permissionCheck == PERMISSION_DENIED) {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                    e.onError(new Throwable(String.format(activity.getString(R.string.__permission_not_granted), permission.replace("android.permission.",""))));
                }
            }
            else {
                e.onComplete();
            }
        }).doOnError(throwable -> ChatSDK.logError(throwable)).doOnTerminate(() -> completableMap.remove(result));
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CompletableEmitter e = completableMap.get(requestCode);
        if(e != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                e.onComplete();
            }
            else {
                // TODO: this is being called for the contact book (maybe it's better to request contacts
                // from inside the contact book module
                e.onError(new Throwable("Permission not granted: " + requestCode));
            }
        }
    }

//    private boolean hasRequested (Integer permission) {
//        return requested.containsKey(permission) && requested.get(permission);
//    }
//
//    private void setHasRequested (Integer permission) {
//        requested.put(permission, true);
//    }

}
