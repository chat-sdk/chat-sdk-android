package co.chatsdk.core.handlers;

import co.chatsdk.core.types.FileUploadResult;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface UploadHandler {

    public Observable<FileUploadResult> uploadFile(byte[] data, String name, String mimeType);

//    -(RXPromise *) uploadFile:(NSData *)file withName: (NSString *) name mimeType: (NSString *) mimeType;
//
//    -(RXPromise *) uploadImage:(UIImage *)image thumbnail: (UIImage *) thumbnail;
}
