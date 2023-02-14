package sdk.chat.core.rigs;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.guru.common.RX;

public class MessageSendRig {

    public interface MessageDidUploadUpdateAction {
        void update (Message message, FileUploadResult result);
    }

    public interface MessageDidCreateUpdateAction {
        void update (Message message);
    }

    protected MessageType messageType;
    protected Thread thread;
    protected Message message;
    protected ArrayList<Uploadable> uploadables = new ArrayList<>();
    protected boolean local = false;

    // This is called after the text has been created. Use it to set the text's payload
    protected MessageDidCreateUpdateAction messageDidCreateUpdateAction;

    // This is called after the text had been uploaded. Use it to update the text payload's url data
    protected MessageDidUploadUpdateAction messageDidUploadUpdateAction;

    public MessageSendRig(MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
        this.messageType = type;
//        this.messageType = new MessageType(MessageType.Silent);
        this.thread = thread;
        this.messageDidCreateUpdateAction = action;
    }

    public MessageSendRig(Message message, Thread thread) {
        this.thread = thread;
        this.message = message;
    }

    public MessageSendRig(Message message) {
        this.thread = message.getThread();
        this.message = message;

        // If upload failed
        if (message.uploadFailed()) {

            // Re-upload any files that failed before
            List<CachedFile> files = ChatSDK.uploadManager().getFiles(message.getEntityID());
            for (CachedFile file: files) {
                if (!file.completeAndValid()) {
                    Uploadable uploadable = file.getUploadable();
                    if (uploadable != null) {
                        uploadables.add(uploadable);
                        file.setTransferStatus(TransferStatus.Initial);
                        file.setStartTime(new Date());
                        ChatSDK.db().update(file);
                    }
                }
            }
            message.setMessageStatus(MessageSendStatus.Initial, true);
        }
    }

    public static MessageSendRig create(MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
        return new MessageSendRig(type, thread, action);
    }

    public static MessageSendRig create(Message message, Thread thread) {
        return new MessageSendRig(message, thread);
    }

    public static MessageSendRig create(Message message) {
        return new MessageSendRig(message);
    }

    public MessageSendRig setUploadables(MessageDidUploadUpdateAction messageDidUploadUpdateAction, Uploadable... uploadables) {
        return this.setUploadables(Arrays.asList(uploadables), messageDidUploadUpdateAction);
    }

    public MessageSendRig setUploadables(List<Uploadable> uploadables, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        this.uploadables.addAll(uploadables);
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig setUploadable(Uploadable uploadable, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        uploadables.add(uploadable);
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig localOnly() {
        local = true;
        return this;
    }

    public Completable run() {
        return ChatSDK.messageSender().run(this);
    }

    public Completable doRun() {
        return Completable.defer(() -> {
            if (message == null) {
                createMessage();
            }
            message.setMessageStatus(MessageSendStatus.Initial, true);

            Logger.debug("ImageSend: Add Message");

            if (uploadables.isEmpty()) {
                return send();
            } else {
                // First pass back an empty result so that we add the cell to the table view
//                message.setMessageStatus(MessageSendStatus.WillUpload);

                ArrayList<Uploadable> compressedUploadables = new ArrayList<>();

                for (Uploadable item : uploadables) {
                    compressedUploadables.add(item.compress());
                }

                uploadables.clear();
                uploadables.addAll(compressedUploadables);

                return uploadFiles().andThen(send());
            }
        }).subscribeOn(RX.quick());
    }

    protected Message createMessage() {

        Logger.debug("ImageSend: Create Message");

        message = ChatSDK.thread().newMessage(messageType.value(), thread, false);
        if (messageDidCreateUpdateAction != null) {
            messageDidCreateUpdateAction.update(message);
        }
        ChatSDK.db().update(message, false);
        ChatSDK.events().source().accept(NetworkEvent.messageAdded(message));
        if (message.getPreviousMessage() != null) {
            ChatSDK.events().source().accept(NetworkEvent.messageUpdated(message.getPreviousMessage()));
        }
        return message;
    }

    protected Completable send() {
        return Completable.defer(() -> {
            // TODO: This was used in the thread wrapper to stop the message from being handled twice (with local first)
//            message.setMessageStatus(MessageSendStatus.WillSend);
            return ChatSDK.hook().executeHook(HookEvent.MessageWillSend, new HashMap<String, Object>() {{
                put(HookEvent.Message, message);
            }}).andThen(Completable.defer(() -> {
                if (local) {
                    return Completable.complete();
                } else {
                    return ChatSDK.thread().sendMessage(message);
                }
            }));
        }).subscribeOn(RX.quick()).andThen(ChatSDK.hook().executeHook(HookEvent.MessageSent, new HashMap<String, Object>() {{
            put(HookEvent.Message, message);
        }})).doOnComplete(() -> {
            message.setMessageStatus(MessageSendStatus.Sent);
        }).doOnError(throwable -> {
            message.setMessageStatus(MessageSendStatus.Failed);
        });
    }

    protected Completable uploadFiles() {
        return Completable.defer((Callable<CompletableSource>) () -> {

            ArrayList<Completable> completables = new ArrayList<>();

            List<Uploadable> toUpload = new ArrayList<>();

            // Add the files to the upload manager
            for (Uploadable item : uploadables) {
                CachedFile file = ChatSDK.uploadManager().add(item, message);

                // TODO:  If the file is already uploaded, we can use the existing path
                if (file.getRemotePath() != null) {
                    Logger.debug("Good, we've uploaded it before");
//                    message.setValueForKey(file.getRemotePath(), item.messageKey);
//                    continue;
                }
                if (!file.completeAndValid()) {
                    file.setTransferStatus(TransferStatus.Initial);
                    toUpload.add(item);
                }
            }

            message.setMessageStatus(MessageSendStatus.Uploading);

            for (Uploadable item : toUpload) {
                completables.add(ChatSDK.upload().uploadFile(item.getBytes(), item.name, item.mimeType, item.hash()).flatMapMaybe(result -> {

                    String hash = item.hash();
                    CachedFile cf = null;
                    if (hash != null) {
                        cf = ChatSDK.uploadManager().getFile(hash, message.getEntityID());
                        if (cf != null) {
                            cf.setTransferStatus(result.status);
                            ChatSDK.db().update(cf);
                        }
                    } else {
                        Logger.debug("");
                    }

                    ChatSDK.uploadManager().setStatus(item.hash(), result.status, message.getEntityID());

                    message.setMessageStatus(MessageSendStatus.Uploading, item.reportProgress);
                    if (item.reportProgress) {
                        Logger.info("Progress - Upload Manager - " + result.progress.asFraction() + " - Message: " + message.getEntityID() + " ID: " + hash);
                        ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, result.progress));
                    }

                    // Success
                    if (result.urlValid()) {

                        if (messageDidUploadUpdateAction != null) {
                            messageDidUploadUpdateAction.update(message, result);
                        }

                        message.setValueForKey(result.url, item.messageKey);

                        for (String key : result.meta.keySet()) {
                            message.setValueForKey(result.meta.get(key), key);
                        }

                        if (cf != null) {
                            cf.setRemotePath(result.url);
                            cf.setFinishTime(new Date());
                            ChatSDK.db().update(cf);
                        }

                        return Maybe.just(message);
                    } else {
                        return Maybe.empty();
                    }
                }).doOnError(throwable -> {
                    message.setMessageStatus(MessageSendStatus.Failed);
                }).firstElement().ignoreElement());
            }

            return Completable.merge(completables);

        }).subscribeOn(RX.io());

//        return Single.create((SingleOnSubscribe<List<Completable>>) emitter -> {
//            ArrayList<Completable> completables = new ArrayList<>();
//
//            List<Uploadable> toUpload = new ArrayList<>();
//
//            // Add the files to the upload manager
//            for (Uploadable item : uploadables) {
//                CachedFile file = ChatSDK.uploadManager().add(item, message);
//
//                // TODO:  If the file is already uploaded, we can use the existing path
//                if (file.getRemotePath() != null) {
//                    Logger.debug("Good, we've uploaded it before");
////                    message.setValueForKey(file.getRemotePath(), item.messageKey);
////                    continue;
//                }
//                if (!file.completeAndValid()) {
//                    file.setTransferStatus(TransferStatus.Initial);
//                    toUpload.add(item);
//                }
//            }
//
//            message.setMessageStatus(MessageSendStatus.Uploading);
//
//            for (Uploadable item : toUpload) {
//                completables.add(ChatSDK.upload().uploadFile(item.getBytes(), item.name, item.mimeType, item.hash()).flatMapMaybe(result -> {
//
//                    String hash = item.hash();
//                    CachedFile cf = null;
//                    if (hash != null) {
//                        cf = ChatSDK.uploadManager().getFile(hash, message.getEntityID());
//                        if (cf != null) {
//                            cf.setTransferStatus(result.status);
//                            ChatSDK.db().update(cf);
//                        }
//                    } else {
//                        Logger.debug("");
//                    }
//
//                    ChatSDK.uploadManager().setStatus(item.hash(), result.status, message.getEntityID());
//
//                    message.setMessageStatus(MessageSendStatus.Uploading, item.reportProgress);
//                    if (item.reportProgress) {
//                        Logger.info("Progress - Upload Manager - " + result.progress.asFraction());
//                        ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, result.progress));
//                    }
//
//                    // Success
//                    if (result.urlValid()) {
//
//                        if (messageDidUploadUpdateAction != null) {
//                            messageDidUploadUpdateAction.update(message, result);
//                        }
//
//                        message.setValueForKey(result.url, item.messageKey);
//
//                        for (String key : result.meta.keySet()) {
//                            message.setValueForKey(result.meta.get(key), key);
//                        }
//
//                        if (cf != null) {
//                            cf.setRemotePath(result.url);
//                            cf.setFinishTime(new Date());
//                            ChatSDK.db().update(cf);
//                        }
//
//                        return Maybe.just(message);
//                    } else {
//                        return Maybe.empty();
//                    }
//                }).doOnError(throwable -> {
//                    message.setMessageStatus(MessageSendStatus.Failed);
//                }).firstElement().ignoreElement());
//            }
//            emitter.onSuccess(completables);
//        }).subscribeOn(RX.quick()).flatMapCompletable(Completable::concat);
    }

    public void finalize() {
        System.out.println("Finalize");
    }

}
