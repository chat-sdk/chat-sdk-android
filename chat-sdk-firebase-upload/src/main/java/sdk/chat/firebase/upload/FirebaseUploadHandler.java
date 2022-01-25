package sdk.chat.firebase.upload;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Map<String, UploadTask> taskMap = new HashMap<>();

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
                clearTasks();
            }
        });
    }

    public Observable<FileUploadResult> uploadFile(final byte[] data, final String name, final String mimeType, String identifier) {
        return Observable.create((ObservableOnSubscribe<FileUploadResult>) e -> {

            StorageReference filesRef = storage().getReference().child("files");
            final String fullName = getUUID() + "_" + name;
            StorageReference fileRef = filesRef.child(fullName);

            final FileUploadResult result = new FileUploadResult();

            UploadTask uploadTask = fileRef.putBytes(data);

            // Get the identifier for the data

            addTask(identifier, uploadTask);

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
                removeTask(uploadTask);

            }).addOnFailureListener(err -> {
                //
                result.status = UploadStatus.Failed;
                e.onNext(result);

                removeTask(uploadTask);

                e.onError(err);
            }).addOnCanceledListener(() -> {
                result.status = UploadStatus.Failed;
                e.onNext(result);
                removeTask(uploadTask);
                e.onError(new Exception("Upload Failed"));
            });

        }).subscribeOn(RX.io());
    }

    public void addTask(String id, UploadTask task) {
        tasks.add(task);
        if (id != null) {
            taskMap.put(id, task);
        }
    }

    public void removeTask(UploadTask task) {
        tasks.remove(task);
        Set<String> keys = taskMap.keySet();
        String foundKey = null;
        for (String key: keys) {
            UploadTask existingTask = taskMap.get(key);
            if (existingTask.equals(task)) {
                foundKey = key;
                break;
            }
        }
        if (foundKey != null) {
            taskMap.remove(foundKey);
        }
    }

    public void clearTasks() {
        tasks.clear();
    }

    public boolean shouldUploadAvatar () {
        return true;
    }

    public UploadStatus uploadStatus(String identifier) {
        UploadTask task = taskMap.get(identifier);
        if (task != null) {
            if (task.isInProgress()) {
                return UploadStatus.InProgress;
            }
            if (task.isComplete()) {
                return UploadStatus.Complete;
            }
            if (task.isCanceled()) {
                return UploadStatus.Failed;
            }
        }
        return UploadStatus.None;
    }

    public static FirebaseStorage storage () {
        if (FirebaseUploadModule.config().firebaseStorageUrl != null) {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app(), FirebaseUploadModule.config().firebaseStorageUrl);
        } else {
            return FirebaseStorage.getInstance(FirebaseCoreHandler.app());
        }
    }

}
