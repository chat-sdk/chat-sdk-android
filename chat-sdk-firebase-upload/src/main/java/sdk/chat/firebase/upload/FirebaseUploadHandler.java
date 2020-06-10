package sdk.chat.firebase.upload;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.pmw.tinylog.Logger;

import sdk.chat.core.base.AbstractUploadHandler;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.guru.common.RX;

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
                Logger.debug("Progress: " + progress);

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
            }).addOnFailureListener(e::onError);

        }).subscribeOn(RX.io());
    }

    public boolean shouldUploadAvatar () {
        return true;
    }

    public static FirebaseStorage storage () {
        if (FirebaseUploadModule.config().firebaseStorageUrl != null) {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app(), FirebaseUploadModule.config().firebaseStorageUrl);
        } else {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app());
        }
    }

}
