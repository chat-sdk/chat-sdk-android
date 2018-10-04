package co.chatsdk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
    private static int MANAGE_DOCUMENTS_REQUEST = 106;

//    HashMap<Integer, Boolean> requested = new HashMap<>();

    public Completable requestRecordAudio (Activity activity) {
        return requestPermission(activity, Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_REQUEST, R.string.permission_record_audio_title, R.string.permission_record_audio_message);
    }

    public boolean recordPermissionGranted () {
        return ContextCompat.checkSelfPermission(ChatSDK.shared().context(), Manifest.permission.RECORD_AUDIO) != PERMISSION_DENIED;
    }

    public Completable requestWriteExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_REQUEST, R.string.permission_write_external_storage_title, R.string.permission_write_external_storage_message);
    }

    public Completable requestManageDocumentsStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.MANAGE_DOCUMENTS, MANAGE_DOCUMENTS_REQUEST, R.string.permission_manage_documents_storage_title, R.string.permission_manage_documents_message);
    }

    public Completable requestReadExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_REQUEST, R.string.permission_read_external_storage_title, R.string.permission_read_external_storage_message);
    }

    public Completable requestCameraAccess (Activity activity) {
        return requestPermission(activity, Manifest.permission.CAMERA, CAMERA_REQUEST, R.string.permission_camera_title, R.string.permission_camera_message);
    }

    public Completable requestVideoAccess (Activity activity) {
//        return requestCameraAccess(activity);
        return requestPermission(activity, Manifest.permission.CAPTURE_VIDEO_OUTPUT, RECORD_VIDEO_REQUEST, R.string.permission_video_output_title, R.string.permission_video_output_message);
    }

    public Completable requestReadContact (Activity activity) {
        return requestPermission(activity, Manifest.permission.READ_CONTACTS, READ_CONTACTS_REQUEST, R.string.permission_read_contacts_title, R.string.permission_read_contacts_message);
    }

    public Completable requestPermission (final Activity activity, final String permission, final int result, final int dialogTitle, final int dialogMessage) {
        if(completableMap.containsKey(result)) {
            return Completable.complete();
        }
        return Completable.create(e -> {

            completableMap.put(result, e);

            int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission);

            if(permissionCheck == PERMISSION_DENIED) {
                String [] permissions = {permission};
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                            .setTitle(dialogTitle)
                            .setMessage(dialogMessage)
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                ActivityCompat.requestPermissions(activity, permissions,  result);
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                String message = String.format(activity.getString(R.string.__permission_not_granted), permission.replace("android.permission.",""));
                                completableMap.remove(result);
                                e.onError(new Throwable(message));
                            });
                    builder.show();
                }
                else {
                    ActivityCompat.requestPermissions(activity, permissions,  result);
                }
            }
            else {
                completableMap.remove(result);
                e.onComplete();
            }
        }).doOnError(throwable -> ChatSDK.logError(throwable));
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CompletableEmitter e = completableMap.get(requestCode);
        if(e != null) {
            completableMap.remove(requestCode);
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

}
