package co.chatsdk.core.rigs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.R;
import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.FileUtils;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MessageSendRig {

    public interface FileCompressAction {
        File compress (File file) throws IOException;
    }

    public interface MessageDidUploadUpdateAction {
        void update (Message message, FileUploadResult result);
    }

    public interface MessageDidCreateUpdateAction {
        void update (Message message);
    }

    public class FileItem {

        public File file;
        public String name;
        public String mimeType;
        public boolean compress;

        public FileItem (File file, String name, String mimeType) {
            this(file, name, mimeType, false);
        }
        public FileItem (File file, String name, String mimeType, boolean compress) {
            this.file = file;
            this.name = name;
            this.mimeType = mimeType;
            this.compress = compress;
        }
    }

    protected DisposableList disposableList = new DisposableList();

    protected MessageType messageType;
    protected Thread thread;
    protected Message message;
    protected ArrayList<FileItem> fileItems = new ArrayList<>();

    protected FileCompressAction fileCompressAction;

    // This is called after the message has been created. Use it to set the message's payload
    protected MessageDidCreateUpdateAction messageDidCreateUpdateAction;

    // This is called after the message had been uploaded. Use it to update the message payload's url data
    protected MessageDidUploadUpdateAction messageDidUploadUpdateAction;

    public MessageSendRig (MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
        this.messageType = type;
        this.thread = thread;
        this.messageDidCreateUpdateAction = action;
    }

    public MessageSendRig setFiles (List<FileItem> files, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        fileItems.addAll(files);
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig setFile (File file, String name, String mimeType, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        fileItems.add(new FileItem(file, name, mimeType));
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig setFileCompressAction (FileCompressAction compressor) {
        fileCompressAction = compressor;
        return this;
    }

    public Completable run () {
        if (fileItems.isEmpty()) {
            return Single.just(createMessage()).ignoreElement().concatWith(send()).subscribeOn(Schedulers.single());
        } else {
            return Single.just(createMessage()).flatMapCompletable(new Function<Message, CompletableSource>() {
                @Override
                public CompletableSource apply(Message message) throws Exception {
                    // First pass back an empty result so that we add the cell to the table view
                    message.setMessageStatus(MessageSendStatus.Uploading);
                    ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                    if (fileCompressAction != null) {
                        for (FileItem item : fileItems) {
                            if (item.compress || fileItems.size() == 1) {
                                item.file = fileCompressAction.compress(item.file);
                            }
                        }
                    }
                    return Completable.complete();
                }
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
        }).andThen(ChatSDK.thread().sendMessage(message));
    }

    protected Completable uploadFiles () {
        ArrayList<Completable> completables = new ArrayList<>();

        message.setMessageStatus(MessageSendStatus.WillUpload);
        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

        message.setMessageStatus(MessageSendStatus.Uploading);
        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

        for (FileItem item : fileItems) {
            completables.add(ChatSDK.upload().uploadFile(FileUtils.fileToBytes(item.file), item.name, item.mimeType).flatMapMaybe((Function<FileUploadResult, MaybeSource<Message>>) result -> {

                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                if (result.urlValid() && messageDidUploadUpdateAction != null) {
                    messageDidUploadUpdateAction.update(message, result);
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
