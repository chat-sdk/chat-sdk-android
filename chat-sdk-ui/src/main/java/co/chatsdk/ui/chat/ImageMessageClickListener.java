package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ImageBuilder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class ImageMessageClickListener implements View.OnClickListener {

    private String url;
    private Activity activity;
    private Bitmap bitmap;

    public ImageMessageClickListener (Activity activity, String  url) {
        this.url = url;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        BaseActivity.hideSoftKeyboard(activity);

        if (StringUtils.isNotBlank(url)) {

            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.chat_sdk_popup_touch_image, null);

            final PopupWindow imagePopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

            imagePopup.setContentView(popupView);
            imagePopup.setBackgroundDrawable(new BitmapDrawable());
            imagePopup.setOutsideTouchable(true);
            imagePopup.setAnimationStyle(R.style.ImagePopupAnimation);

            final PhotoView imageView = popupView.findViewById(R.id.photo_view);
            final ProgressBar progressBar = popupView.findViewById(R.id.chat_sdk_popup_image_progressbar);
            final FloatingActionButton saveButton = popupView.findViewById(R.id.floating_button);

            saveButton.setOnClickListener(v1 -> {
                PermissionRequestHandler.shared().requestWriteExternalStorage(activity).subscribe(() -> {
                    if (bitmap != null) {
                        String url = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "" , "");
                        if (url != null) {
                            ToastHelper.show(activity, activity.getString(R.string.image_saved));
                        }
                        else {
                            ToastHelper.show(activity, activity.getString(R.string.image_save_failed));
                        }
                    }
                }, throwable -> ToastHelper.show(activity, throwable.getLocalizedMessage()));

            });

            saveButton.setVisibility(View.INVISIBLE);

            progressBar.setVisibility(View.VISIBLE);

            ImageBuilder.bitmapForURL(activity, url)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> progressBar.setVisibility(View.INVISIBLE))
            .subscribe(bitmap -> {
                imageView.setImageBitmap(bitmap);
                this.bitmap = bitmap;
                saveButton.setVisibility(View.VISIBLE);
            }, throwable -> {
                ToastHelper.show(activity, R.string.unable_to_fetch_image);
                imagePopup.dismiss();
            });

            imagePopup.showAtLocation(v, Gravity.CENTER, 0, 0);

        }
    }


}
