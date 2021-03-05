package sdk.chat.ui.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class GlideWith {

    public static RequestBuilder<Drawable> load(Fragment with, String url) {
//        if (url != null) {
//            File file = new File(Uri.parse(url).getPath());
//            if (file.exists()) {
//                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//                if (bitmap != null) {
//                    return Glide.with(with).load(bitmap);
//                }
//            }
//        }


        return Glide.with(with).load(url).timeout(10).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        });
    }

}
