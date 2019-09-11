package co.chatsdk.firebase.file_storage;

import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import co.chatsdk.core.base.AbstractUploadHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ChatError;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.firebase.FirebaseCoreHandler;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Erk on 26.07.2016.
 */
public class FirebaseUploadHandler extends AbstractUploadHandler {

    public Observable<FileUploadResult> uploadFile(final byte[] data, final String name, final String mimeType) {
        return Observable.create((ObservableOnSubscribe<FileUploadResult>) e -> {

            StorageReference filesRef = storage().getReference().child("files");
            final String fullName = getUUID() + "_" + name;
            StorageReference fileRef = filesRef.child(fullName);

            final FileUploadResult result = new FileUploadResult();

            UploadTask uploadTask = fileRef.putBytes(data);

            uploadTask.addOnProgressListener(taskSnapshot -> {
                result.progress.set(taskSnapshot.getTotalByteCount(), taskSnapshot.getBytesTransferred());

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.print("Progress: " + progress);

                // TODO: With Firebase this appears to be brokenProfileFragment.newInstance
                e.onNext(result);
            }).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    result.name = name;
                    result.mimeType = mimeType;
                    result.url = uri.toString();
                    result.progress.set(taskSnapshot.getTotalByteCount(), taskSnapshot.getTotalByteCount());
                    e.onNext(result);
                    e.onComplete();
                });
            }).addOnFailureListener(error -> e.onError(ChatError.getError(ChatError.Code.FIREBASE_STORAGE_EXCEPTION, error.getMessage())));

        }).subscribeOn(Schedulers.single());
    }

    public boolean shouldUploadAvatar () {
        return true;
    }

    public static FirebaseStorage storage () {
        if (ChatSDK.config().firebaseStorageUrl != null) {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app(), ChatSDK.config().firebaseStorageUrl);
        } else {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app());
        }

    }

}
