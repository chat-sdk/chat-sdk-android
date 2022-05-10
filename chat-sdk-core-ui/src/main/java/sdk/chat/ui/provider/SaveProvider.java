package sdk.chat.ui.provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.ui.R;
import sdk.guru.common.RX;

public class SaveProvider {

    public Single<String> saveImage(Context context, String imageURL) {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            if (imageURL != null) {
                Bitmap bitmap = Glide.with(context).asBitmap().load(imageURL).submit().get();
                String bitmapURL = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "", "");
                if (bitmapURL != null) {
                    emitter.onSuccess(context.getString(R.string.image_saved));
                } else {
                    emitter.onError(new Throwable());
                }
            } else {
                emitter.onError(new Throwable());
            }
        }).subscribeOn(RX.computation());
    }

}
