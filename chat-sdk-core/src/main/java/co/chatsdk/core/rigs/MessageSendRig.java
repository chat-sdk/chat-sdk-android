package co.chatsdk.core.rigs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.utils.DisposableList;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MessageSendRig {

    public interface MessageDidUploadUpdateAction {
        void update (Message message, FileUploadResult result);
    }

    public interface MessageDidCreateUpdateAction {
        void update (Message message);
    }

    protected DisposableList disposableList = new DisposableList();

    protected MessageType messageType;
    protected Thread thread;
    protected Message message;
    protected ArrayList<Uploadable> uploadables = new ArrayList<>();

    // This is called after the message has been created. Use it to set the message's payload
    protected MessageDidCreateUpdateAction messageDidCreateUpdateAction;

    // This is called after the message had been uploaded. Use it to update the message payload's url data
    protected MessageDidUploadUpdateAction messageDidUploadUpdateAction;

    public MessageSendRig (MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
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

    public Completable run () {
        if (uploadables.isEmpty()) {
            return Single.just(createMessage()).ignoreElement().concatWith(send()).subscribeOn(Schedulers.single());
        } else {
            return Single.just(createMessage()).flatMapCompletable(message -> {
                // First pass back an empty result so that we add the cell to the table view
                message.setMessageStatus(MessageSendStatus.Uploading);
                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                ArrayList<Uploadable> compressedUploadables = new ArrayList<>();

                for (Uploadable item : uploadables) {
                    compressedUploadables.add(item.compress());
                }

                uploadables.clear();
                uploadables.addAll(compressedUploadables);

                return Completable.complete();
            }).concatWith(uploadFiles()).andThen(send()).subscribeOn(Schedulers.single());
        }
    }

    protected Message createMessage () {
        message = AbstractThreadHandler.newMessage(messageType, thread);
        if (messageDidCreateUpdateAction != null) {
            messageDidCreateUpdateAction.update(message);
        }
        message.update();
        // Message has been created and added to the thread
        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));
        return message;
    }

    protected Completable send () {
        return Completable.create(emitter -> {
            message.setMessageStatus(MessageSendStatus.WillSend);
            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));
            message.setMessageStatus(MessageSendStatus.Sending);
            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));
            emitter.onComplete();
        }).concatWith(ChatSDK.thread().sendMessage(message));
    }

    protected Completable uploadFiles () {
        ArrayList<Completable> completables = new ArrayList<>();

        message.setMessageStatus(MessageSendStatus.WillUpload);
        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

        message.setMessageStatus(MessageSendStatus.Uploading);
        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

        for (Uploadable item : uploadables) {
            completables.add(ChatSDK.upload().uploadFile(item.getBytes(), item.name, item.mimeType).flatMapMaybe((Function<FileUploadResult, MaybeSource<Message>>) result -> {

                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                if (result.urlValid() && messageDidUploadUpdateAction != null) {
                    messageDidUploadUpdateAction.update(message, result);

                    // Add the meta from file upload result to message
                    for (String key : result.meta.keySet()) {
                        message.setValueForKey(result.meta.get(key), key);
                    }

                    message.update();

                    return Maybe.just(message);
                } else {
                    return Maybe.empty();
                }
            }).firstElement().ignoreElement());
        }
        return Completable.merge(completables).doOnComplete(() -> {
            message.setMessageStatus(MessageSendStatus.DidUpload);
            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));
        });
    }

}
