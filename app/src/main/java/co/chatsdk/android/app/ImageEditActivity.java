package co.chatsdk.android.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
