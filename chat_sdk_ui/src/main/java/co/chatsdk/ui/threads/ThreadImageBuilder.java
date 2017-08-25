package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.UserAvatarHelper;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    public static Single<Bitmap> getBitmapForThread (final Context context, final Thread thread) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final SingleEmitter<Bitmap> e) throws Exception {

                List<User> users = thread.getUsers();
                users.remove(NM.currentUser());

                // If the URL is empty
                if (users.size() == 0 && thread.getImageURL() == null) {
                    e.onSuccess(defaultBitmap(context, thread));
                }
                else {

                    final ArrayList<Bitmap> bitmaps = new ArrayList<>();
                    ArrayList<Single<Bitmap>> singles = new ArrayList<>();

                    if(thread.getImageURL() != null) {
                        singles.add(thread.image());
                    }
                    else {
                        for(User u : users) {
                            if(singles.size() >= 4) {
                                break;
                            }
                            singles.add(UserAvatarHelper.avatar(u));
                        }
                    }

                    Single.merge(singles).doOnNext(new Consumer<Bitmap>() {
                        @Override
                        public void accept(Bitmap bitmap) throws Exception {
                            bitmaps.add(bitmap);
                        }
                    }).doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            if(bitmaps.size() == 1) {
                                e.onSuccess(bitmaps.get(0));
                            }
                            else {
                                int size = context.getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                                Bitmap merged = ImageUtils.getMixImagesBitmap(size, size, bitmaps);
                                e.onSuccess(merged);
                            }
                        }
                    }).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            e.onSuccess(defaultBitmap(context, thread));
                        }
                    }).subscribe();
                }
            }
        });
    }

    public static Bitmap defaultBitmap (Context context, Thread thread) {
        if (thread.typeIs(ThreadType.Public)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_users);
        }
        else if (thread.getUsers().size() < 3 || thread.typeIs(ThreadType.Private1to1)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icn_32_profile_placeholder);
        }
        else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_users);
        }
    }


}

