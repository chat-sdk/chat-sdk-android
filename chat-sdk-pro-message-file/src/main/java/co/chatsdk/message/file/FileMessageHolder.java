package co.chatsdk.message.file;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.chat.model.MessageHolder;

public class FileMessageHolder extends MessageHolder implements MessageContentType {

    public FileMessageHolder(Message message) {
        super(message);
    }

    public String getIcon() {
        return ChatSDK.fileMessage().getImageURL(message);
//        String imageURL = message.imageURL();
//        if (imageURL != null && !imageURL.isEmpty()) {
//            return imageURL;
//        } else {
//
//
//            Context context = ChatSDK.shared().context();
//            Resources resources = context.getResources();
//
//            final String mimeType = message.stringForKey(Keys.MessageMimeType);
//            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
//
//            int resID = context.getResources().getIdentifier("file_type_" + extension, "drawable", context.getPackageName());
//            resID = resID > 0 ? resID : R.drawable.file;
//
//            Uri uri = new Uri.Builder()
//                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
//                    .authority(resources.getResourcePackageName(resID))
//                    .appendPath(resources.getResourceTypeName(resID))
//                    .appendPath(resources.getResourceEntryName(resID))
//                    .build();
//
//            return uri.toString();
//        }
    }

//    public Single<String> downloadFile (String url, String name) {
//        return getDownloadDirectory().flatMap((Function<File, SingleSource<String>>) directory -> Single.create(emitter -> {
//            File file = new File(directory.getPath(), name);
//            if (file.exists() && file.length() > 0) {
//                emitter.onSuccess(file.getPath());
//            } else {
//                AndroidNetworking.download(url, directory.getPath(), name)
//                        .build()
//                        .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
//                            // Progress
//                            Logger.debug("Progress: " + bytesDownloaded);
//                        }).startDownload(new DownloadListener() {
//                    @Override
//                    public void onDownloadComplete() {
//                        emitter.onSuccess(file.getPath());
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        emitter.onError(anError);
//                    }
//                });
//            }
//        }));
//    }

//    public Single<File> getDownloadDirectory () {
//        return PermissionRequestHandler.requestWriteExternalStorage().andThen(Single.create(emitter -> {
//            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ChatSDK");
//            if (!f.exists()) {
//                if (f.mkdirs()) {
//                    emitter.onSuccess(f);
//                } else {
//                    emitter.onError(new Throwable("Unable to create directory"));
//                }
//            } else {
//                emitter.onSuccess(f);
//            }
//        }));
//    }

}
