package co.chatsdk.core.avatar;

import android.graphics.Bitmap;
import android.graphics.Canvas;



import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.image.ImageUploadResult;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.storage.FileManager;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class HashAvatarGenerator implements AvatarGenerator {

    @Override
    public String getAvatarURL(User user) {
        String hash = String.valueOf(new Random().nextInt(1000));
        if (user != null && user.getEntityID() != null) {
            hash = user.getName();
        }
        if (user != null) {
            hash = user.getEntityID();
        }
        return String.format(ChatSDK.config().identiconBaseURL, hash);
    }

//    public Single<String> downloadAvatar(User user) {
//        return Single.defer(() -> {
//            final String url = getAvatarURL(user);
//            if (!url.contains("svg")) {
//                return Single.just(url);
//            } else {
//                return Single.create((SingleOnSubscribe<File>) emitter -> {
//                    FileManager fm = new FileManager(ChatSDK.ctx());
//                    File file = fm.newDatedFile(fm.imageCache(), "avatar", "svg");
//                    AndroidNetworking.download(url, fm.imageCache().getPath(), file.getName()).build().setDownloadProgressListener(new DownloadProgressListener() {
//                        @Override
//                        public void onProgress(long bytesDownloaded, long totalBytes) {
//                            Logger.debug(bytesDownloaded);
//                        }
//                    }).startDownload(new DownloadListener() {
//                        @Override
//                        public void onDownloadComplete() {
//                            try {
//                                SVG svg = SVG.getFromInputStream(new FileInputStream(file));
//                                if (svg.getDocumentWidth() != -1) {
//                                    Bitmap newBM = Bitmap.createBitmap((int) Math.ceil(svg.getDocumentWidth()),
//                                            (int) Math.ceil(svg.getDocumentHeight()),
//                                            Bitmap.Config.ARGB_8888);
//                                    Canvas canvas = new Canvas(newBM);
//
//                                    // Clear background to white
//                                    canvas.drawRGB(255, 255, 255);
//
//                                    // Render our document onto our canvas
//                                    svg.renderToCanvas(canvas);
//
//                                    File file = ImageUtils.saveBitmapToFile(newBM);
//                                    emitter.onSuccess(file);
//                                }
//                                if (file.exists()) {
//
//                                }
//                            } catch (Exception e) {
//                                emitter.onError(e);
//                            }
//                            Logger.debug("Ok");
//                        }
//                        @Override
//                        public void onError(ANError anError) {
//                            emitter.onError(anError);
//                        }
//                    });
//                }).flatMap((Function<File, SingleSource<ImageUploadResult>>) ImageUtils::uploadImageFile)
//                        .map(imageUploadResult -> imageUploadResult.url);
//            }
//        }).subscribeOn(Schedulers.io());
//    }

}
