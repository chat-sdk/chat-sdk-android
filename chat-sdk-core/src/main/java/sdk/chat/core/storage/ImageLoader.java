package sdk.chat.core.storage;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;

import org.pmw.tinylog.Logger;


public class ImageLoader {

    public static void load(ImageView imageView, String url, Bitmap preview, int width, int height) {
        load(imageView, url, 0, preview, width, height, false);
    }

    public static void load(ImageView imageView, String url, int placeholder, int width, int height) {
        load(imageView, url, placeholder, null, width, height, false);
    }

    protected static void load(ImageView imageView, String url, int placeholder, Bitmap preview, int width, int height, boolean isAnimated) {

        if (url == null) {
            Logger.debug("Stop here");
            return;
        }

        RequestManager request = Glide.with(imageView);
        RequestBuilder<?> builder;
        if (isAnimated) {
            builder = request.asGif();
        } else {
            builder = request.asDrawable().dontAnimate();
        }

        // If this is a local image
        Uri uri = Uri.parse(url);
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("android.resource")) {
            builder = builder.load(uri);
        } else {
            builder = builder.load(url);
        }

        if (placeholder != 0) {
            builder = builder.placeholder(placeholder);
        }
        if (preview != null) {
            builder = builder.placeholder(new BitmapDrawable(imageView.getResources(), preview));
        }

        builder.override(width, height)
//                .error(ilp.error)
                .centerCrop()
                .into(imageView);

    }

}
