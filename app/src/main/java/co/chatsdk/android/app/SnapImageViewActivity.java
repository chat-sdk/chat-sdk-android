package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class SnapImageViewActivity extends BaseActivity {

    private String imageURL;
    private String messageEntityID;
    private int lifetime;
    CircleProgressBar circleTimer;
    TextView textTimer;
    Message message;
    Boolean saveMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapimageview);

        final PhotoView imageView = findViewById(co.chatsdk.ui.R.id.photo_view);
        final ProgressBar progressBar = findViewById(R.id.chat_sdk_popup_image_progressbar);
        final FloatingActionButton saveButton = findViewById(R.id.floating_button);
        circleTimer = (CircleProgressBar) findViewById(R.id.barTimer);
        textTimer = (TextView) findViewById(R.id.textTimer);
        saveMessage = false;

        Intent i = getIntent();
        imageURL = (String)i.getSerializableExtra("imageURL");
        lifetime = (int)i.getSerializableExtra("lifetime");
        messageEntityID = (String)i.getSerializableExtra("messageEntityID");
        message = ChatSDK.db().fetchEntityWithEntityID(messageEntityID, Message.class);

        if (lifetime == 0) {
            circleTimer.setVisibility(View.GONE);
            textTimer.setVisibility(View.GONE);
        }

        saveButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Disposable d = ImageBuilder.bitmapForURL(this, imageURL)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> progressBar.setVisibility(View.INVISIBLE))
                .subscribe(bitmap -> {
                    imageView.setImageBitmap(bitmap);
                    saveButton.setVisibility(View.VISIBLE);
                    saveButton.setOnClickListener(v1 -> PermissionRequestHandler.shared().requestWriteExternalStorage(this).subscribe(() -> {
                        if (bitmap != null) {
                            String bitmapURL = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "" , "");
                            saveMessage = true;
                            if (bitmapURL != null) {
                                ToastHelper.show(this, this.getString(co.chatsdk.ui.R.string.image_saved));
                            }
                            else {
                                ToastHelper.show(this, this.getString(co.chatsdk.ui.R.string.image_save_failed));
                            }
                        }
                    }, throwable -> ToastHelper.show(this, throwable.getLocalizedMessage())));

                }, throwable -> {
                    ToastHelper.show(this, co.chatsdk.ui.R.string.unable_to_fetch_image);
                });
        if (lifetime != 0) {
            RotateAnimation animation = new RotateAnimation(0.0f, 0.0f, 100f, 100f);
            animation.setFillAfter(true);
            circleTimer.setMax(lifetime * 1000);
            startTimer(lifetime);
            circleTimer.startAnimation(animation);
        }
    }

    private void startTimer(final int secondsInput) {
        CountDownTimer countDownTimer = new CountDownTimer(secondsInput * 1000, 10) {
            //500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                circleTimer.setProgress((int)leftTimeInMilliseconds);
                textTimer.setText(String.format("%02d", leftTimeInMilliseconds / 60000) + ":" + String.format("%02d", (leftTimeInMilliseconds / 1000) % 60));
                //format the textview to show the easily readable format
            }

            @Override
            public void onFinish() {
                onBackPressed();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (lifetime != 0 && !saveMessage) {
            disposableList.add(ChatSDK.thread().deleteMessage(message).subscribe(super::onBackPressed, toastOnErrorConsumer()));
        }
        else {
            super.onBackPressed();
        }
    }
}
