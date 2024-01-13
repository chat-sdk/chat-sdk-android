package sdk.chat.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class PopupImageView extends RelativeLayout {


    protected PhotoView photoView;
    protected ProgressBar progressBar;
    protected FloatingActionButton fab;
    protected RelativeLayout popupView;

    protected DisposableMap dm = new DisposableMap();

    protected Runnable onDismiss;

    public PopupImageView(Context context) {
        super(context);
        initViews();
    }

    public PopupImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PopupImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    @LayoutRes
    public int getLayout() {
        return R.layout.view_popup_image;
    }

    public void initViews() {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(getLayout(), this);

        photoView = findViewById(R.id.photoView);
        progressBar = findViewById(R.id.progressBar);
        fab = findViewById(R.id.fab);
        popupView = findViewById(R.id.popupView);

        fab.setImageDrawable(ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().save, R.color.fab_icon_color));

        fab.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setBitmap(Activity activity, Bitmap bitmap) {
        photoView.setImageBitmap(bitmap);
        progressBar.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v1 -> PermissionRequestHandler.requestWriteExternalStorage(activity).subscribe(() -> {
            if (bitmap != null) {
                String bitmapURL = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "", "");
                if (bitmapURL != null) {
                    ToastHelper.show(activity, activity.getString(R.string.image_saved));
                } else {
                    ToastHelper.show(activity, activity.getString(R.string.image_save_failed));
                }
            }
        }, throwable -> ToastHelper.show(activity, throwable.getLocalizedMessage())));
    }

    public void setUrl(Activity activity, String url, Runnable dismiss) {

        Single<Bitmap> onLoad = Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            emitter.onSuccess(Glide.with(this).asBitmap().load(url).submit().get());
        }).subscribeOn(RX.io()).observeOn(RX.main());

        dm.add(onLoad.subscribe(bitmap -> {
            this.setBitmap(activity, bitmap);
        }, throwable -> {
            ToastHelper.show(activity, throwable.getLocalizedMessage());
            dismiss.run();
        }));
    }

    public void dispose() {
        dm.dispose();
    }

    public void setOnDismiss(Runnable onDismiss) {
        this.onDismiss = onDismiss;
    }
}
