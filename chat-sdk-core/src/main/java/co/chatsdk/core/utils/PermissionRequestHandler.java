package co.chatsdk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

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
    private static int ACCESS_LOCATION_REQUEST = 107;
    private static int TAKE_PHOTOS = 108;

    public Permission recordAudio () {
        return new Permission(Manifest.permission.RECORD_AUDIO, R.string.permission_record_audio_title, R.string.permission_record_audio_message);
    }

    public Permission writeExternalStorage () {
        return new Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_write_external_storage_title, R.string.permission_write_external_storage_message);
    }

    public Permission manageDocuments () {
        return new Permission(Manifest.permission.MANAGE_DOCUMENTS, R.string.permission_manage_documents_storage_title, R.string.permission_manage_documents_message);
    }

    public Permission readExternalStorage () {
        return new Permission(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_read_external_storage_title, R.string.permission_read_external_storage_message);
    }

    public Permission camera () {
        return new Permission(Manifest.permission.CAMERA, R.string.permission_camera_title, R.string.permission_camera_message);
    }

    public Permission captureVideoOutput () {
        return new Permission(Manifest.permission.CAPTURE_VIDEO_OUTPUT, R.string.permission_video_output_title, R.string.permission_video_output_message);
    }

    public Permission readContacts () {
        return new Permission(Manifest.permission.READ_CONTACTS, R.string.permission_read_contacts_title, R.string.permission_read_contacts_message);
    }

    public Permission accessFineLocation () {
        return new Permission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location_title, R.string.permission_location_message);
    }

    public Permission accessCoarseLocation () {
        return new Permission(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_location_title, R.string.permission_location_message);
    }

    public Completable requestRecordAudio(Activity activity) {
        return requestPermissions(activity, RECORD_AUDIO_REQUEST, recordAudio(), writeExternalStorage());
    }

    public boolean recordPermissionGranted() {
        return ContextCompat.checkSelfPermission(ChatSDK.shared().context(), Manifest.permission.RECORD_AUDIO) != PERMISSION_DENIED;
    }

    public Completable requestWriteExternalStorage(Activity activity) {
        return requestPermission(activity, WRITE_EXTERNAL_STORAGE_REQUEST, writeExternalStorage());
    }

    public Completable requestManageDocumentsStorage(Activity activity) {
        return requestPermission(activity, MANAGE_DOCUMENTS_REQUEST, manageDocuments());
    }

    public Completable requestReadExternalStorage(Activity activity) {
        return requestPermission(activity, READ_EXTERNAL_STORAGE_REQUEST, readExternalStorage());
    }

    public Completable requestCameraAccess(Activity activity) {
        return requestPermissions(activity, CAMERA_REQUEST, camera(), writeExternalStorage());
    }

    public Completable requestVideoAccess(Activity activity) {
        return requestPermission(activity, RECORD_VIDEO_REQUEST, captureVideoOutput());
    }

    public Completable requestReadContact(Activity activity) {
        return requestPermission(activity, READ_CONTACTS_REQUEST, readContacts());
    }

    public Completable requestLocationAccess(Activity activity) {
        return requestPermissions(activity, ACCESS_LOCATION_REQUEST, accessFineLocation(), accessCoarseLocation());
    }

    public Completable requestPermission(final Activity activity, final String permission, final int result, final int dialogTitle, final int dialogMessage) {
        return requestPermissions(activity, result, new Permission(permission, dialogTitle, dialogMessage));
    }

    public Completable requestPermission(final Activity activity, final int result, Permission permission) {
        return requestPermissions(activity, result, permission);
    }

    public Completable requestPermissions(final Activity activity, final int result, Permission... permissions) {

        // If the method is called twice, just return success...
        if (completableMap.containsKey(result)) {
            return Completable.complete();
        }
        return Completable.create(e -> {

            // So we can handle multiple requests at the same time, we store the emitter and wait...
            completableMap.put(result, e);

            ArrayList<AlertDialog.Builder> dialogs = new ArrayList<>();
            ArrayList<String> toRequest = new ArrayList<>();

            boolean allGranted = true;
            for (Permission permission : permissions) {
                int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission.name);
                if (permissionCheck == PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.name)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                .setTitle(permission.title(activity))
                                .setMessage(permission.description(activity))
                                .setPositiveButton(R.string.ok, (dialog, which) -> {
                                    ActivityCompat.requestPermissions(activity, permission.permissions(), result);
                                })
                                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                    String message = String.format(activity.getString(R.string.__permission_not_granted), permission.description(activity));
                                    completableMap.remove(result);
                                    e.onError(new Throwable(message));
                                });
                        dialogs.add(builder);
                    } else {
                        toRequest.add(permission.name);
                    }
                }
                allGranted = allGranted && permissionCheck != PERMISSION_DENIED;
            }

            // If the name is denied, we request it
            if (!allGranted) {
                for (AlertDialog.Builder b : dialogs) {
                    b.show();
                }
                ActivityCompat.requestPermissions(activity, toRequest.toArray(new String[toRequest.size()]),  result);
            } else {
                // If the name isn't denied, we remove the emitter and return success
                completableMap.remove(result);
                e.onComplete();
            }
        });
    }

    public void onRequestPermissionsResult(Context context, int requestCode, String permissions[], int[] grantResults) {
        CompletableEmitter e = completableMap.get(requestCode);
        if (e != null) {
            completableMap.remove(requestCode);

            String errorCodes = "";
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_DENIED) {
                    errorCodes += new Permission(permissions[i]).name + ", ";
                }
            }
            if (!errorCodes.isEmpty()) {
                errorCodes = errorCodes.substring(0, errorCodes.length() - 2);
            }

            if (errorCodes.isEmpty()) {
                e.onComplete();
            }
            else {
                // TODO: this is being called for the contact book (maybe it's better to request contacts
                // from inside the contact book module
                Throwable throwable = new Throwable(context.getString(R.string.error_permission_not_granted, errorCodes));
                e.onError(throwable);
            }
        }
    }


}
