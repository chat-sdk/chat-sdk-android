package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ImageBuilder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class ImageMessageClickListener implements View.OnClickListener {

    private String url;
    private Activity activity;

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

            final PhotoView imageView = (PhotoView) popupView.findViewById(R.id.photo_view);
            final ProgressBar progressBar = (ProgressBar) popupView.findViewById(R.id.chat_sdk_popup_image_progressbar);

            progressBar.setVisibility(View.VISIBLE);

            ImageBuilder.bitmapForURL(activity, url)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(new Action() {
                @Override
                public void run() throws Exception {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            })
            .subscribe(new Consumer<Bitmap>() {
                @Override
                public void accept(Bitmap bitmap) throws Exception {
                    imageView.setImageBitmap(bitmap);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    ToastHelper.show(activity, R.string.unable_to_fetch_image);
                    imagePopup.dismiss();
                }
            });

            imagePopup.showAtLocation(v, Gravity.CENTER, 0, 0);

        }
    }


}
