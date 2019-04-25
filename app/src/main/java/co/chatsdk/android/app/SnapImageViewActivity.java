package co.chatsdk.android.app;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import com.github.chrisbanes.photoview.PhotoView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class SnapImageViewActivity extends AppCompatActivity {

    private String imageURL;
    private String messageEntityID;
    private int lifetime;
    ProgressBar circleTimer;
    TextView textTimer;
    Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapimageview);

        final PhotoView imageView = findViewById(co.chatsdk.ui.R.id.photo_view);
        final ProgressBar progressBar = findViewById(co.chatsdk.ui.R.id.chat_sdk_popup_image_progressbar);
        final FloatingActionButton saveButton = findViewById(co.chatsdk.ui.R.id.floating_button);
        circleTimer = (ProgressBar) findViewById(R.id.barTimer);
        textTimer = (TextView) findViewById(R.id.textTimer);

        Intent i = getIntent();
        imageURL = (String)i.getSerializableExtra("imageURL");
        lifetime = (int)i.getSerializableExtra("lifetime");
        messageEntityID = (String)i.getSerializableExtra("messageEntityID");
        message = ChatSDK.db().fetchEntityWithEntityID(messageEntityID, Message.class);

        RotateAnimation animation = new RotateAnimation(0.0f, 0.0f, 100f, 100f);
        animation.setFillAfter(true);
        circleTimer.startAnimation(animation);

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
        circleTimer.setMax(lifetime);
        startTimer(lifetime);
    }

    private void startTimer(final int secondsInput) {
        CountDownTimer countDownTimer = new CountDownTimer(secondsInput * 1000, 10) {
            //500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                circleTimer.setProgress((int) seconds);
                textTimer.setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                //format the textview to show the easily readable format
            }

            @Override
            public void onFinish() {
                if (textTimer.getText().equals("00:00")) {
                    ChatSDK.thread().deleteMessage(message).subscribe();
                    onBackPressed();

                } else {
                    textTimer.setText("2:00");
                    circleTimer.setProgress(secondsInput);
                }
            }
        }.start();

    }
}
