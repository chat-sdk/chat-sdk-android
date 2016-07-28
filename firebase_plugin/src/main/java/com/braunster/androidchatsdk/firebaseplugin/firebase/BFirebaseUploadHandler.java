package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.support.annotation.NonNull;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.BUploadHandler;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.SaveImageProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import timber.log.Timber;

/**
 * Created by Erk on 26.07.2016.
 */
public class BFirebaseUploadHandler implements BUploadHandler {

    private static final String TAG = BFirebaseUploadHandler.class.getSimpleName();
    private static final boolean DEBUG = Debug.BFirebaseUploadHandler;

    @Override
    public Promise<String, BError, SaveImageProgress> uploadFile(byte[] data, String name, String mimeType) {
        final Deferred<String, BError, SaveImageProgress> deferred = new DeferredObject<>();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(BDefines.FirebaseStoragePath);
        StorageReference filesRef = storageRef.child("files");
        final String fullName = getUUID() + "_" + name;
        StorageReference fileRef = filesRef.child(fullName);

        UploadTask uploadTask = fileRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (DEBUG) Timber.e(e.getCause(), "Firebase storage exception while saving");
                deferred.reject(new BError(BError.Code.FIREBASE_STORAGE_EXCEPTION, e));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                deferred.resolve(taskSnapshot.getDownloadUrl().toString());
            }
        });

        return deferred.promise();
    }

    private String getUUID() {
        return DaoCore.generateEntity();
    }
}
