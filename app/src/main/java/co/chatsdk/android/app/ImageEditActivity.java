package co.chatsdk.android.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import co.chatsdk.ui.main.BaseActivity;

public class ImageEditActivity extends BaseActivity {

    private File theImage;
    private String theImagePath;
    private Bitmap picBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageedit);
        Intent i = getIntent();
        theImage = (File) i.getSerializableExtra("imageToEdit");
        theImagePath = theImage.getPath();
        picBitmap = BitmapFactory.decodeFile(theImagePath);
        setTheImage();

        TextView minutesText = (TextView) findViewById(R.id.minutes_text);
        TextView secondsText = (TextView) findViewById(R.id.seconds_text);
        Spinner minutesSpinner = (Spinner) findViewById(R.id.minutes_spinner);
        Spinner secondsSpinner = (Spinner) findViewById(R.id.seconds_spinner);
        Button confirmViewingTimeButton = (Button) findViewById(R.id.confirm_viewing_time);
        Button noTimeLimitButton = (Button) findViewById(R.id.no_time_limit);

        minutesText.setVisibility(View.GONE);
        secondsText.setVisibility(View.GONE);
        minutesSpinner.setVisibility(View.GONE);
        secondsSpinner.setVisibility(View.GONE);
        confirmViewingTimeButton.setVisibility(View.GONE);
        noTimeLimitButton.setVisibility(View.GONE);

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.mainFloatingActionButton);
        FloatingActionButton mAddCardButton = (FloatingActionButton) findViewById(R.id.addCardButton);
        FloatingActionButton mReloadCardsButton = (FloatingActionButton) findViewById(R.id.reloadCardsButton);
        FloatingActionButton mLogOutButton = (FloatingActionButton) findViewById(R.id.logoutButton);
        FloatingActionButton mUserProfileButton = (FloatingActionButton) findViewById(R.id.userProfileButton);
        FloatingActionButton mViewingTimeButton = (FloatingActionButton) findViewById(R.id.viewingTimeButton);
        final LinearLayout mLogOutLayout = (LinearLayout) findViewById(R.id.logoutLayout);
        final LinearLayout mAddCardLayout = (LinearLayout) findViewById(R.id.addCardLayout);
        final LinearLayout mReloadCardsLayout = (LinearLayout) findViewById(R.id.reloadCardsLayout);
        final LinearLayout mUserProfileLayout = (LinearLayout) findViewById(R.id.userProfileLayout);
        final LinearLayout mViewingTimeLayout = (LinearLayout) findViewById(R.id.viewingTimeLayout);

        mFab.setVisibility(View.VISIBLE);

        mAddCardLayout.setVisibility(View.GONE);
        mReloadCardsLayout.setVisibility(View.GONE);
        mLogOutLayout.setVisibility(View.GONE);
        mUserProfileLayout.setVisibility(View.GONE);
        mViewingTimeLayout.setVisibility(View.GONE);



        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAddCardLayout.getVisibility() == View.VISIBLE && mReloadCardsLayout.getVisibility() == View.VISIBLE) {
                    mAddCardLayout.setVisibility(View.GONE);
                    mReloadCardsLayout.setVisibility(View.GONE);
                    mLogOutLayout.setVisibility(View.GONE);
                    mUserProfileLayout.setVisibility(View.GONE);
                    mViewingTimeLayout.setVisibility(View.GONE);
                }
                else {
                    mAddCardLayout.setVisibility(View.VISIBLE);
                    mReloadCardsLayout.setVisibility(View.VISIBLE);
                    mLogOutLayout.setVisibility(View.VISIBLE);
                    mUserProfileLayout.setVisibility(View.VISIBLE);
                    mViewingTimeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mViewingTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minutesText.getVisibility() == View.VISIBLE && secondsText.getVisibility() == View.VISIBLE) {
                    minutesText.setVisibility(View.GONE);
                    secondsText.setVisibility(View.GONE);
                    minutesSpinner.setVisibility(View.GONE);
                    secondsSpinner.setVisibility(View.GONE);
                    confirmViewingTimeButton.setVisibility(View.GONE);
                    noTimeLimitButton.setVisibility(View.GONE);
                }
                else {
                    minutesText.setVisibility(View.VISIBLE);
                    secondsText.setVisibility(View.VISIBLE);
                    minutesSpinner.setVisibility(View.VISIBLE);
                    secondsSpinner.setVisibility(View.VISIBLE);
                    confirmViewingTimeButton.setVisibility(View.VISIBLE);
                    noTimeLimitButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setTheImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        imageView.setImageBitmap(picBitmap);
    }

    public void onSendImageButtonClicked(View v) {
        Intent i = new Intent(ImageEditActivity.this, SelectSnapRecipientActivity.class);
        i.putExtra("theImagePath", theImagePath);
        startActivity(i);

    }
}

//https://github.com/leinardi/FloatingActionButtonSpeedDial