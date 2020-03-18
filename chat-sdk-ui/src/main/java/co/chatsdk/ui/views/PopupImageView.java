package co.chatsdk.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PopupImageView extends RelativeLayout {


    @BindView(R2.id.photoView) protected PhotoView photoView;
    @BindView(R2.id.progressBar) protected ProgressBar progressBar;
    @BindView(R2.id.fab) protected FloatingActionButton fab;
    @BindView(R2.id.popupView) protected RelativeLayout popupView;

    DisposableMap dm = new DisposableMap();

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

    public void initViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        inflater.inflate(R.layout.view_popup_image, this);
        ButterKnife.bind(this);

        fab.setImageDrawable(Icons.get(Icons.choose().save, R.color.fab_icon_color));

        fab.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setUrl(Activity activity, String url, Runnable dismiss) {

        Single<Bitmap> onLoad = Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            emitter.onSuccess(Glide.with(this).asBitmap().load(url).submit().get());
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        dm.add(onLoad.subscribe(bitmap -> {
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
        }, throwable -> {
            ToastHelper.show(activity, throwable.getLocalizedMessage());
            dismiss.run();
        }));
    }

    public void dispose() {
        dm.dispose();
    }

}
