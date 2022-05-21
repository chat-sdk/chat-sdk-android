package co.chatsdk.ejabberd.file_storage;

import android.util.Base64;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jxmpp.jid.Jid;

import java.io.File;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sdk.chat.core.base.AbstractUploadHandler;
import sdk.chat.core.dao.Message;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.types.MessageType;

import sdk.chat.core.utils.ImageBuilder;
import sdk.chat.core.utils.ImageUtils;
import co.chatsdk.xmpp.XMPPManager;
import app.xmpp.adapter.iq.CustomIQ;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.guru.common.RX;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class EjabberdUploadHandler extends AbstractUploadHandler {

    OkHttpClient client;
    DisposableList disposableList = new DisposableList();
    FileTransferIQProvider fileTransferIQProvider = new FileTransferIQProvider();

    public EjabberdUploadHandler() {

        // Demonstration to show how file can be downloaded when message is received
        ChatSDK.hook().addHook(new Hook(data -> {
            return Completable.create(emitter -> {
                Object messageObject = data.get(HookEvent.Message);
                if(messageObject != null && messageObject instanceof Message) {
                    Message message = (Message) messageObject;

                    // Check to see if the message is a file message - could include more types
                    if(message.getMessageType().is(MessageType.Image)) {

                        // Get the file ID, md5 hash
                        String fileId = message.stringForKey(FileUploadIQ.FileId);
                        String md5 = message.stringForKey(FileUploadIQ.Md5);

                        // If they are set...
                        if(!StringUtils.isEmpty(fileId) && !StringUtils.isEmpty(md5)) {

                            // Add an IQ listener to get the URL
                            disposableList.add(fileTransferIQProvider.onIQ().observeOn(RX.main()).subscribe(iq -> {
                                // If we have a new file download to handle
                                if(iq instanceof FileDownloadIQ) {
                                    FileDownloadIQ downloadIQ = (FileDownloadIQ) iq;

                                    String url = URLDecoder.decode(downloadIQ.url);

                                    // Download the file
                                    disposableList.add(download(url, "file").subscribe((file, throwable) -> {
                                        if (throwable == null) {
                                            System.out.println("Ok!");
                                        } else {

                                        }
                                        emitter.onComplete();
                                    }));
                                }
                            }));

                            // Request the download URL using the file ID
                            requestDownload(fileId);
                        }
                    }
                }

            });
        }), HookEvent.MessageReceived);

            // Add a post auth hook
        ChatSDK.hook().addHook(new Hook(data -> {
            return Completable.create(emitter -> {


                ProviderManager.addIQProvider(FileTransferIQ.childElementName, FileTransferIQ.childElementNamespace, fileTransferIQProvider);

                // Send a IQ
                CustomIQ discoIQ = new CustomIQ("query", "http://jabber.org/protocol/disco#info");
                discoIQ.setTo(serverJID());

                try {
                    Object i = XMPPManager.shared().getConnection().sendIqRequestAndWaitForResponse(discoIQ);
                    System.out.println(((IQ) i).toXML("").toString());

                    String test = "testupload";
                    byte[] bytes = test.getBytes("UTF-8");

//                    disposableList.add(uploadFile(bytes, "test", "data").subscribe(new Consumer<FileUploadResult>() {
//                        @Override
//                        public void accept(FileUploadResult fileUploadResult) throws Exception {
//                            System.out.println("");
//                        }
//                    }));


                    Disposable d = ImageBuilder.bitmapForURL(ChatSDK.shared().context(), "http://chatsdk.co/wp-content/uploads/2019/03/ic_launcher_big.png")
                            .subscribe((bitmap, throwable) -> {
                                if (throwable == null) {
                                    byte[] file = ImageUtils.getImageByteArray(bitmap);

                                    disposableList.add(uploadFile(file, "test", "data").subscribe(new Consumer<FileUploadResult>() {
                                        @Override
                                        public void accept(FileUploadResult fileUploadResult) throws Exception {
                                            System.out.println("");
                                        }
                                    }));

                                } else {
                                   throwable.printStackTrace();
                                }
                            });

                    System.out.print("ok");

                } catch (Exception e) {
                    e.printStackTrace();
                }


                emitter.onComplete();
            });
        }), HookEvent.DidAuthenticate);

    }

    public Single<File> download(String url, String fileName) {
        return Single.create((SingleOnSubscribe<File>) emitter -> {
            File directory = ChatSDK.shared().context().getFilesDir();

            AndroidNetworking.download(url, directory.getPath(), fileName).build().startDownload(new DownloadListener() {
                @Override
                public void onDownloadComplete() {
                    emitter.onSuccess(new File(directory + "/" + fileName));
                }

                @Override
                public void onError(ANError anError) {
                    emitter.onError(anError);
                }
            });
        }).subscribeOn(RX.io());
    }

    public Completable upload (byte [] file, String md5, final String encodedURL, String fileId) throws Exception {
        return Completable.create(emitter -> {
            String url = URLDecoder.decode(encodedURL);

            client = new OkHttpClient().newBuilder()
                    .addNetworkInterceptor(new HeaderInterceptor())
                    .build();

            ANRequest request = AndroidNetworking.put(url)
                    .addHeaders("Content-MD5", md5)
                    .addHeaders("Date", "`date -R -u`")
                    .addHeaders("Content-Type", "")
                    .addByteBody(file)
                    .setOkHttpClient(client).build();

            request.getAsOkHttpResponse(new OkHttpResponseListener() {
                @Override
                public void onResponse(Response response) {
                    System.out.println("Response");

                    emitter.onComplete();
                }

                @Override
                public void onError(ANError anError) {
                    emitter.onError(anError);
                }
            });
        });
    }

    @Override
    public Observable<FileUploadResult> uploadFile(byte[] data, String name, String mimeType) {
        return Observable.create(emitter -> {
            String md5 = getFileMD5(data);

            // Complete
            Disposable d1 = fileTransferIQProvider.onIQ().observeOn(RX.main()).flatMapMaybe((Function<FileTransferIQ, MaybeSource<FileUploadIQ>>) fileTransferIQ -> {
                if (fileTransferIQ instanceof FileUploadIQ) {
                    FileUploadIQ uploadIQ = (FileUploadIQ) fileTransferIQ;

                    // Make sure that this is the same file...
                    if (md5.equals(uploadIQ.md5)) {
                        FileUploadResult result = new FileUploadResult();
                        result.name = name;
                        result.mimeType = mimeType;
                        result.url = uploadIQ.url;

                        // Add these to the result meta - they will automatically be added to the message meta
                        result.meta.put(FileUploadIQ.FileId, uploadIQ.fileId);
                        result.meta.put(FileUploadIQ.Md5, uploadIQ.md5);

                        emitter.onNext(result);

                        return Maybe.just(uploadIQ);
                    }
                }

                return Maybe.empty();

            }).flatMapCompletable(uploadIQ -> upload(data, uploadIQ.md5, uploadIQ.url, uploadIQ.fileId)).subscribe(emitter::onComplete, emitter::onError);

            CustomIQ uploadIQ = FileUploadIQ.build(serverJID(), md5);
            XMPPManager.shared().getConnection().sendStanza(uploadIQ);
        });
    }

    public String getFileMD5 (byte [] file) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(file);
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public void requestDownload (String fileId) {
        try {
            CustomIQ downloadIQ = FileDownloadIQ.build(serverJID(), fileId);
            XMPPManager.shared().getConnection().sendStanza(downloadIQ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Jid serverJID() {
        return XMPPManager.shared().getConnection().getXMPPServiceDomain();
    }

}
