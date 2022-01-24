package sdk.chat.firebase.upload;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.chat.core.base.AbstractUploadHandler;
import sdk.chat.core.storage.UploadStatus;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.guru.common.RX;

/**
 * Created by Erk on 26.07.2016.
 */
public class FirebaseUploadHandler extends AbstractUploadHandler {

    public List<UploadTask> tasks = new ArrayList<>();

    public FirebaseUploadHandler() {

        // If we lose connection, pause tasks
//        ChatSDK.events().source().filter(NetworkEvent.filterType(EventType.NetworkStateChanged)).doOnNext(networkEvent -> {
//            for (UploadTask task: tasks) {
//                if (networkEvent.getIsOnline()) {
//                    task.resume();
//                } else {
//                    task.pause();
//                }
//            }
//        }).ignoreElements().subscribe(ChatSDK.events());

        // Cancel tasks if the app is sent to the background
        AppBackgroundMonitor.shared().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {

            }

            @Override
            public void didStop() {
                for (UploadTask task: tasks) {
                    task.cancel();
                }
                tasks.clear();
            }
        });
    }

    public Observable<FileUploadResult> uploadFile(final byte[] data, final String name, final String mimeType) {
        return Observable.create((ObservableOnSubscribe<FileUploadResult>) e -> {

            StorageReference filesRef = storage().getReference().child("files");
            final String fullName = getUUID() + "_" + name;
            StorageReference fileRef = filesRef.child(fullName);

            final FileUploadResult result = new FileUploadResult();

            UploadTask uploadTask = fileRef.putBytes(data);
            tasks.add(uploadTask);

            uploadTask.addOnProgressListener(taskSnapshot -> {
                result.progress.set(taskSnapshot.getTotalByteCount(), taskSnapshot.getBytesTransferred());
                result.status = UploadStatus.InProgress;

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Logger.debug("Progress: " + progress);

                e.onNext(result);
            }).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    result.name = name;
                    result.mimeType = mimeType;
                    result.url = uri.toString();
                    result.progress.set(taskSnapshot.getTotalByteCount(), taskSnapshot.getTotalByteCount());
                    result.status = UploadStatus.Complete;

                    e.onNext(result);

                    e.onComplete();
                });
                tasks.remove(uploadTask);

            }).addOnFailureListener(err -> {
                //
                result.status = UploadStatus.Failed;
                e.onNext(result);

                tasks.remove(uploadTask);

                e.onError(err);
            }).addOnCanceledListener(() -> {
                result.status = UploadStatus.Failed;
                e.onNext(result);
               tasks.remove(uploadTask);
                e.onError(new Exception("Upload Failed"));
            });

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
