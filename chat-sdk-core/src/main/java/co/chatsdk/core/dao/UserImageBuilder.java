package co.chatsdk.core.dao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import co.chatsdk.core.R;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public class UserImageBuilder {

    public static Disposable loadAvatar(final User user, final ImageView imageView, int width, int height) {
        return user.getAvatarBitmap(width, height).subscribe(imageView::setImageBitmap);
    }

    public static Single<Bitmap> getAvatarBitmap(final User user, int width, int height) {
        return ImageUtils.bitmapForURL(user.getAvatarURL(), width, height).onErrorReturnItem(defaultAvatar());
    }

    public static Bitmap defaultAvatar() {
        return BitmapFactory.decodeResource(ChatSDK.shared().context().getResources(), ChatSDK.ui().getDefaultProfileImage());
    }

}
