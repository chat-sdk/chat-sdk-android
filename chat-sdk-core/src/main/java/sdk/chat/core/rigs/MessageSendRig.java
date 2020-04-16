package sdk.chat.core.rigs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import sdk.chat.core.base.AbstractThreadHandler;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.types.MessageSendProgress;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
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
        this.thread = thread;
        this.messageDidCreateUpdateAction = action;
    }

    public MessageSendRig setUploadables (MessageDidUploadUpdateAction messageDidUploadUpdateAction, Uploadable... uploadables) {
        return this.setUploadables(Arrays.asList(uploadables), messageDidUploadUpdateAction);
    }

    public MessageSendRig setUploadables (List<Uploadable> uploadables, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
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
        return Completable.defer(() -> {
            createMessage();
            if (uploadables.isEmpty()) {
                return send();
            } else {
                // First pass back an empty result so that we add the cell to the table view
                message.setMessageStatus(MessageSendStatus.Uploading);

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
        message = AbstractThreadHandler.newMessage(messageType, thread);
        if (messageDidCreateUpdateAction != null) {
            messageDidCreateUpdateAction.update(message);
        }
        return message;
    }

    protected Completable send() {
        return Completable.defer(() -> {
            message.setMessageStatus(MessageSendStatus.WillSend);
            if (local) {
                return Completable.complete();
            } else {
                return ChatSDK.thread().sendMessage(message);
            }
        }).subscribeOn(RX.quick())
                .doOnComplete(() -> message.setMessageStatus(MessageSendStatus.Sent))
                .doOnError(throwable -> message.setMessageStatus(MessageSendStatus.Failed));
    }

    protected Completable uploadFiles() {
        return Single.create((SingleOnSubscribe<List<Completable>>) emitter -> {
            ArrayList<Completable> completables = new ArrayList<>();

            message.setMessageStatus(MessageSendStatus.WillUpload);
            message.setMessageStatus(MessageSendStatus.Uploading);

            for (Uploadable item : uploadables) {
                completables.add(ChatSDK.upload().uploadFile(item.getBytes(), item.name, item.mimeType).flatMapMaybe(result -> {

                    ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message, MessageSendStatus.Uploading, result.progress)));

                    if (result.urlValid() && messageDidUploadUpdateAction != null) {
                        messageDidUploadUpdateAction.update(message, result);

                        for (String key : result.meta.keySet()) {
                            message.setValueForKey(result.meta.get(key), key);
                        }

                        return Maybe.just(message);
                    } else {
                        return Maybe.empty();
                    }
                }).firstElement().ignoreElement());
            }
            emitter.onSuccess(completables);
        }).subscribeOn(RX.quick()).flatMapCompletable(Completable::merge).doOnComplete(() -> {
            message.setMessageStatus(MessageSendStatus.DidUpload);
        });
    }

}
