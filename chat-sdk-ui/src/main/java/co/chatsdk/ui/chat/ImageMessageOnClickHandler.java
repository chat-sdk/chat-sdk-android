package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import androidx.databinding.DataBindingUtil;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import co.chatsdk.core.image.ImageBuilder;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.databinding.ViewPopupImageBinding;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class ImageMessageOnClickHandler {

    public static void onClick (Activity activity, View view, String url) {
        BaseActivity.hideKeyboard(activity);

        if (!url.replace(" ", "").isEmpty()) {

            LayoutInflater inflater = LayoutInflater.from(activity);

            ViewPopupImageBinding b = DataBindingUtil.inflate(inflater, R.layout.view_popup_image, null, false);

            final PopupWindow imagePopup = new PopupWindow(b.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

            imagePopup.setContentView(b.getRoot());
            imagePopup.setBackgroundDrawable(new BitmapDrawable());
            imagePopup.setOutsideTouchable(true);
            imagePopup.setAnimationStyle(R.style.ImagePopupAnimation);

            b.fab.setImageDrawable(Icons.get(Icons.choose().save, R.color.fab_icon_color));

            b.fab.setVisibility(View.INVISIBLE);
            b.progressBar.setVisibility(View.VISIBLE);

            Picasso.get().load(url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    b.photoView.setImageBitmap(bitmap);
                    b.progressBar.setVisibility(View.INVISIBLE);
                    b.fab.setVisibility(View.VISIBLE);
                    b.fab.setOnClickListener(v1 -> PermissionRequestHandler.requestWriteExternalStorage(activity).subscribe(() -> {
                        if (bitmap != null) {
                            String bitmapURL = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "" , "");
                            if (bitmapURL != null) {
                                ToastHelper.show(activity, activity.getString(R.string.image_saved));
                            }
                            else {
                                ToastHelper.show(activity, activity.getString(R.string.image_save_failed));
                            }
                        }
                    }, throwable -> ToastHelper.show(activity, throwable.getLocalizedMessage())));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    b.progressBar.setVisibility(View.INVISIBLE);
                    ToastHelper.show(activity, e.getLocalizedMessage());
                    imagePopup.dismiss();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

            imagePopup.showAtLocation(view, Gravity.CENTER, 0, 0);

        }
    }
}
