package co.chatsdk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by ben on 9/28/17.
 */

public class PermissionRequestHandler {

    Map<Integer, CompletableEmitter> completableMap = new HashMap<>();

    private static int WRITE_EXTERNAL_STORAGE_REQUEST = 100;
    private static int READ_EXTERNAL_STORAGE_REQUEST = 101;
    private static int RECORD_AUDIO_REQUEST = 102;

    public Completable requestRecordAudio (Activity activity) {
        return requestPermission(activity, Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_REQUEST);
    }

    public Completable requestWriteExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_REQUEST);
    }

    public Completable requestReadExternalStorage (Activity activity) {
        return requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_REQUEST);
    }

    public Completable requestPermission (final Activity activity, final String permission, final int result) {
        if(completableMap.containsKey(result)) {
            return null;
        }
        final Completable completable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {

                completableMap.put(result, e);

                int permissionCheck = ContextCompat.checkSelfPermission(AppContext.shared().context(), permission);

                if(permissionCheck == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{permission},
                            result);
                }
                else {
                    e.onComplete();
                }
            }
        });
        completable.doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                completableMap.remove(result);
            }
        });
//        completable.doOnError(new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                completableMap.remove(result);
//            }
//        });

        return completable;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CompletableEmitter e = completableMap.get(requestCode);
        if(e != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                e.onComplete();
            }
            else {
                e.onError(new Throwable("Permission not granted"));
            }
        }
    }

}
