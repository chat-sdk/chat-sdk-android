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
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.volley.ImageUtils;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import wanderingdevelopment.tk.sdkbaseui.Activities.BaseThreadActivity;
import wanderingdevelopment.tk.sdkbaseui.Fragments.AbstractContactsFragment;
import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;

import wanderingdevelopment.tk.sdkbaseui.Fragments.ContactsFragment;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.DialogUtils;
import wanderingdevelopment.tk.sdkbaseui.utils.ChatSDKIntentClickListener;
import co.chatsdk.core.utils.volley.VolleyUtils;
import co.chatsdk.core.dao.DaoCore;

import com.braunster.chatsdk.object.Cropper;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import wanderingdevelopment.tk.sdkbaseui.utils.Strings;
import wanderingdevelopment.tk.sdkbaseui.view.CircleImageView;
import timber.log.Timber;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.MakeThreadImage;

/**
 * Created by braunster on 24/11/14.
 */
public class ThreadDetailsActivity extends BaseThreadActivity {

    private static final String TAG = ThreadDetailsActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ThreadDetailsActivity;

    private static final int THREAD_PIC = 1991;

    private CircleImageView imageThread, imageAdmin;
    private TextView txtAdminName, txtThreadName;

    private ContactsFragment contactsFragment;

    private BUser admin;

    private Cropper crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_thread_details);

        initActionBar();

        initViews();

        loadData();
    }

    private void initViews() {
        txtAdminName = (TextView) findViewById(R.id.chat_sdk_txt_admin_name);

        txtThreadName = (TextView) findViewById(R.id.chat_sdk_txt_thread_name);

        imageAdmin = (CircleImageView) findViewById(R.id.chat_sdk_admin_image_view);
        imageThread = (CircleImageView) findViewById(R.id.chat_sdk_thread_image_view);
    }

    private void loadData(){

        // Admin bundle
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()))
        {
            admin = DaoCore.fetchEntityWithEntityID(BUser.class, thread.getCreatorEntityId());

            if (admin!=null)
            {
                if (StringUtils.isNotBlank(admin.getThumbnailPictureURL()))
                    VolleyUtils.getImageLoader().get(admin.getThumbnailPictureURL(), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                imageAdmin.setImageBitmap(response.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

                txtAdminName.setText(admin.getMetaName());
            }
        }

        //CoreThread image
        final String imageUrl = thread.threadImageUrl();
        if (StringUtils.isNotEmpty(imageUrl))
        {
            // Check if there is a image saved in the cahce for this thread.
//            if (thread.getType()== BThread.Type.Private)
                if (imageUrl.split(",").length > 1)
                {
                    int size = getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                    new MakeThreadImage(imageUrl.split(","), size, size, thread.getEntityID(), imageThread).setProgressBar((android.widget.ProgressBar) findViewById(R.id.chat_sdk_progress_bar));
                }
                else
                    VolleyUtils.getImageLoader().get(imageUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                imageThread.setImageBitmap(response.getBitmap());
                                findViewById(R.id.chat_sdk_progress_bar).setVisibility(View.INVISIBLE);
                                imageThread.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            imageThread.setImageResource(R.drawable.ic_users);
                            findViewById(R.id.chat_sdk_progress_bar).setVisibility(View.INVISIBLE);
                        }


                    });
        }
        else
        {
            findViewById(R.id.chat_sdk_progress_bar).setVisibility(View.INVISIBLE);
            imageThread.setImageResource(R.drawable.ic_users);
            imageThread.setVisibility(View.VISIBLE);
        }

        // CoreThread name
        txtThreadName.setText(Strings.nameForThread(thread));

        // CoreThread users bundle
        contactsFragment = new ContactsFragment();
        contactsFragment.setInflateMenu(false);

        contactsFragment.setOnItemClickListener(getItemClickListener());

        contactsFragment.setLoadingMode(AbstractContactsFragment.MODE_LOAD_THREAD_USERS);
        contactsFragment.setExtraData(thread.getEntityID());
        contactsFragment.withUpdates(true);
        contactsFragment.setClickMode(AbstractContactsFragment.CLICK_MODE_NONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_thread_users, contactsFragment).commit();
    }

    private AdapterView.OnItemClickListener getItemClickListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Intent intent = new Intent();

                showProgDialog("Opening thread.");

                BUser otherUser = contactsFragment.getAdapter().getItem(position).asBUser();
                BUser currentUser = NM.currentUser();

                NM.thread().createThread("", otherUser, currentUser)
                        .subscribe(new BiConsumer<BThread, Throwable>() {
                            @Override
                            public void accept(final BThread thread, Throwable throwable) throws Exception {
                                if(throwable == null) {
                                    if (thread == null) {
                                        if (DEBUG) Timber.e("thread added is null");
                                        return;
                                    }

                                    if (isOnMainThread()) {
                                        intent.putExtra(THREAD_ID, thread.getId());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                    else {
                                        ThreadDetailsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                intent.putExtra(THREAD_ID, thread.getId());
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }
                                        });
                                    }
                                }
                                else {
                                    if (isOnMainThread()) {
                                        showToast(getString(R.string.create_thread_with_users_fail_toast));
                                        dismissProgDialog();
                                    }
                                    else {
                                        ThreadDetailsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showToast(getString(R.string.create_thread_with_users_fail_toast));
                                                dismissProgDialog();
                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        };
    }

    protected void initActionBar(){
        ActionBar ab = getSupportActionBar();
        if (ab!=null)
        {
            ab.setTitle(getString(R.string.thread_details_activity_title));
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only if the current user is the admin of this thread.
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(NM.currentUser().getEntityID()))
        {
            imageThread.setOnClickListener(ChatSDKIntentClickListener.getPickImageClickListener(this, THREAD_PIC));

            txtThreadName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    DialogUtils.ChatSDKEditTextDialog textDialog = DialogUtils.ChatSDKEditTextDialog.getInstace();
                    textDialog.setTitleAndListen(getString(R.string.thread_details_activity_change_name_dialog_title), new DialogUtils.ChatSDKEditTextDialog.EditTextDialogInterface() {
                        @Override
                        public void onFinished(String s) {
                            txtThreadName.setText(s);
                            thread.setName(s);
                            DaoCore.updateEntity(thread);

                            NM.thread().pushThread(thread);
                        }
                    });

                    textDialog.show(getSupportFragmentManager(), DialogUtils.ChatSDKEditTextDialog.class.getSimpleName());
                    return true;
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        //BNetworkManager.getCoreInterface().getEventManager().removeEventByTag(this.getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
        setResult(AppCompatActivity.RESULT_OK);
        finish();

        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
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

                imageThread.setImageBitmap(b);

                Bitmap imageBitmap = ImageUtils.getCompressed(image.getPath());

                NM.upload().uploadImage(imageBitmap).subscribe(new Observer<FileUploadResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(FileUploadResult value) {
                        if(value.isComplete()) {
                            thread.setImageUrl(value.url);
                            DaoCore.updateEntity(thread);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(getString(R.string.unable_to_save_file));
                    }

                    @Override
                    public void onComplete() {
                        NM.thread().pushThread(thread);
                    }
                });

            }
            catch (NullPointerException e){
                if (DEBUG) Timber.e("Null pointer when getting file.");
                showToast(getString(R.string.unable_to_fetch_image));
            }
        }


    }


}
