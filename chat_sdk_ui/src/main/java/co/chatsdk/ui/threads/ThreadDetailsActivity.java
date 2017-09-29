/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import co.chatsdk.core.NM;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.utils.ImageUtils;

import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.core.dao.DaoCore;

import co.chatsdk.ui.utils.Cropper;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import co.chatsdk.ui.utils.Strings;
import timber.log.Timber;

/**
 * Created by braunster on 24/11/14.
 */
public class ThreadDetailsActivity extends BaseActivity {

    private static final boolean DEBUG = Debug.ThreadDetailsActivity;

    private static final int THREAD_PIC = 1991;

    /** Set true if you want slide down animation for this activity exit. */
    protected boolean animateExit = false;

    protected Thread thread;

    private CircleImageView threadImageView;

    private ContactsFragment contactsFragment;

    private ActionBar actionBar;

    private Cropper crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        }
        else {
            if (getIntent().getExtras() != null) {
                getDataFromBundle(getIntent().getExtras());
            }
            else {
                finish();
            }
        }

        setContentView(R.layout.chat_sdk_activity_thread_details);

        initViews();

        loadData();
    }

    private void initViews() {

        actionBar = getSupportActionBar();
        actionBar.setTitle(Strings.nameForThread(thread));
        actionBar.setHomeButtonEnabled(true);

        final View actionBarView = getLayoutInflater().inflate(R.layout.chat_sdk_activity_thread_details, null);

        // Allow the thread name to be modified by a long click
        actionBarView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                DialogUtils.ChatSDKEditTextDialog textDialog = DialogUtils.ChatSDKEditTextDialog.getInstace();
                textDialog.setTitleAndListen(getString(R.string.thread_details_activity_change_name_dialog_title), new DialogUtils.ChatSDKEditTextDialog.EditTextDialogInterface() {
                    @Override
                    public void onFinished(String s) {
                        actionBar.setTitle(s);
                        thread.setName(s);
                        DaoCore.updateEntity(thread);

                        NM.thread().pushThread(thread);
                    }
                });

                textDialog.show(getSupportFragmentManager(), DialogUtils.ChatSDKEditTextDialog.class.getSimpleName());
                return true;
            }
        });

        threadImageView = (CircleImageView) findViewById(R.id.chat_sdk_thread_image_view);
    }

    private void loadData () {

        threadImageView.setVisibility(View.INVISIBLE);

        ThreadImageBuilder.getBitmapForThread(this, thread)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<Bitmap, Throwable>() {
            @Override
            public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                    threadImageView.setImageBitmap(bitmap);
                    threadImageView.setVisibility(View.VISIBLE);
            }
        });

        // CoreThread users bundle
        contactsFragment = new ContactsFragment();
        contactsFragment.setInflateMenu(false);
        contactsFragment.setLoadingMode(ContactsFragment.MODE_LOAD_THREAD_USERS);
        contactsFragment.setExtraData(thread.getEntityID());
        contactsFragment.withUpdates(true);
        contactsFragment.setClickMode(ContactsFragment.CLICK_MODE_NONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_thread_users, contactsFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only if the current user is the admin of this thread.
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(NM.currentUser().getEntityID())) {
            //threadImageView.setOnClickListener(ChatSDKIntentClickListener.getPickImageClickListener(this, THREAD_PIC));
            threadImageView.setOnClickListener(new ProfilePictureChooserOnClickListener(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(AppCompatActivity.RESULT_OK);

        finish();
        if (animateExit) {
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (DEBUG) Timber.v("onActivityResult");

        if (requestCode == THREAD_PIC)
        {
            if (resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(this.getCacheDir(), "cropped_thread_image.jpg"));
                crop = new Cropper(uri);

                Intent cropIntent = crop.getIntent(this, outputUri);
                int request = Crop.REQUEST_CROP + THREAD_PIC;
                startActivityForResult(cropIntent, request);
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP + THREAD_PIC) {
            if (resultCode == Crop.RESULT_ERROR)
            {
                showToast(getString(R.string.unable_to_fetch_image));
            }

            try
            {
                File image;
                Uri uri = Crop.getOutput(data);

                if (DEBUG) Timber.d("Fetch image URI: %s", uri.toString());
                image = new File(this.getCacheDir(), "cropped_thread_image.jpg");

                Bitmap b = ImageUtils.loadBitmapFromFile(image.getPath());

                if (b == null)
                {
                    b = ImageUtils.loadBitmapFromFile(getCacheDir().getPath() + image.getPath());
                    if (b == null)
                    {
                        showToast(getString(R.string.unable_to_save_file));
                        if (DEBUG) Timber.e("Cant save image to backendless file path is invalid: %s",
                                getCacheDir().getPath() + image.getPath());
                        return;
                    }
                }

                threadImageView.setImageBitmap(b);

//                Bitmap imageBitmap = ImageUtils.getCompressed(image.getPath());
//
//                NM.upload().uploadImage(imageBitmap).subscribe(new Observer<FileUploadResult>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(FileUploadResult value) {
//                        if(value.urlsAreSet()) {
//                            thread.setImageURL(value.url);
//                            DaoCore.updateEntity(thread);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        showToast(getString(R.string.unable_to_save_file));
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        NM.thread().pushThread(thread);
//                    }
//                });

            }
            catch (NullPointerException e){
                if (DEBUG) Timber.e("Null pointer when getting file.");
                showToast(getString(R.string.unable_to_fetch_image));
            }
        }


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBundle(intent.getExtras());
    }

    private void getDataFromBundle(Bundle bundle){
        if (bundle == null) {
            return;
        }

        animateExit = bundle.getBoolean(ChatActivity.ANIMATE_EXIT, animateExit);

        String threadEntityID = bundle.getString(BaseInterfaceAdapter.THREAD_ENTITY_ID);

        if(threadEntityID != null && threadEntityID.length() > 0) {
            thread = StorageManager.shared().fetchThreadWithEntityID(threadEntityID);
        }
        else {
            finish();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BaseInterfaceAdapter.THREAD_ENTITY_ID, thread.getEntityID());
        outState.putBoolean(ChatActivity.ANIMATE_EXIT, animateExit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }


}
