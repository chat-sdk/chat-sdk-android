package com.braunster.chatsdk.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.asynctask.MakeThreadImage;
import com.braunster.chatsdk.Utils.helper.ChatSDKIntentClickListener;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractContactsFragment;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.interfaces.SaveCompletedListener;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Cropper;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by braunster on 24/11/14.
 */
public class ThreadDetailsActivity extends ChatSDKBaseThreadActivity {

    private static final String TAG = ThreadDetailsActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ThreadDetailsActivity;

    private static final int THREAD_PIC = 1991;

    private CircleImageView imageThread, imageAdmin;
    private TextView txtAdminName, txtThreadName;

    private ChatSDKContactsFragment contactsFragment;

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

        // Admin data
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

        //Thread image
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

        // Thread name
        txtThreadName.setText(thread.displayName());

        // Thread users data
        contactsFragment = new ChatSDKContactsFragment();
        contactsFragment.setInflateMenu(false);

        contactsFragment.setOnItemClickListener(getItemClickListener());

        contactsFragment.setLoadingMode(ChatSDKAbstractContactsFragment.MODE_LOAD_THREAD_USERS);
        contactsFragment.setExtraData(thread.getEntityID());
        contactsFragment.setClickMode(ChatSDKAbstractContactsFragment.CLICK_MODE_NONE);

        getFragmentManager().beginTransaction().replace(R.id.frame_thread_users, contactsFragment).commit();
    }

    private AdapterView.OnItemClickListener getItemClickListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Intent intent = new Intent();

                showProgDialog("Opening thread.");

                getNetworkAdapter().createThreadWithUsers("", new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, BError>() {

                    BThread thread = null;

                    @Override
                    public boolean onMainFinised(BThread bThread, BError o) {
                        if (o != null) {
                            if (isOnMainThread())
                            {
                                showAlertToast(getString(R.string.create_thread_with_users_fail_toast));
                                dismissProgDialog();
                            }
                            else ThreadDetailsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showAlertToast(getString(R.string.create_thread_with_users_fail_toast));
                                    dismissProgDialog();
                                }
                            });
                            return true;
                        }

                        if (DEBUG) Log.d(TAG, "New thread is created.");

                        thread = bThread;

                        return false;
                    }

                    @Override
                    public boolean onItem(BUser item) {
                        return false;
                    }

                    @Override
                    public void onDone() {
                        Log.d(TAG, "On done.");

                        dismissProgDialog();

                        if (thread == null)
                            if (DEBUG) Log.e(TAG, "thread added is null");

                        if (isOnMainThread()) {
                            if (thread != null) {
                                Log.d(TAG, "Stating chat for thread.");

                                intent.putExtra(THREAD_ID, thread.getId());
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else ThreadDetailsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (thread != null) {
                                    Log.d(TAG, "Stating chat for thread.");
                                    intent.putExtra(THREAD_ID, thread.getId());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onItemError(BUser user, BError o) {
                        if (DEBUG)
                            Log.d(TAG, "Failed to add user to thread, User name: " + user.getName());
                    }
                }, contactsFragment.getAdapter().getItem(position).asBUser(), getNetworkAdapter().currentUser());

            }
        };
    }

    protected void initActionBar(){
        ActionBar ab = getActionBar();
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
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(getNetworkAdapter().currentUser().getEntityID()))
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
                            thread.setDirty(true);
                            thread.setName(s);
                            DaoCore.updateEntity(thread);

                            getNetworkAdapter().pushThreadWithCallback(thread, null);
                        }
                    });

                    textDialog.show(getFragmentManager(), DialogUtils.ChatSDKEditTextDialog.class.getSimpleName());
                    return true;
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();

        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
        }

        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode == THREAD_PIC)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (DEBUG) Log.d(TAG, "Result OK");
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
                if (DEBUG) Log.e(TAG, "Result Error");
                showAlertToast("Unable to fetch image");
            }

            try
            {
                File image;
                Uri uri = Crop.getOutput(data);

                if (DEBUG) Log.d(TAG, "Fetch image URI: " + uri.toString());
                image = new File(this.getCacheDir(), "cropped_thread_image.jpg");

                Bitmap b = ImageUtils.loadBitmapFromFile(image.getPath());

                if (b == null)
                {
                    b = ImageUtils.loadBitmapFromFile(getCacheDir().getPath() + image.getPath());
                    if (b == null)
                    {
                        showAlertToast("Unable to save file...");
                        if (DEBUG) Log.e(TAG, "Cant save image to parse file path is invalid: " + getCacheDir().getPath() + image.getPath());
                        return;
                    }
                }

                imageThread.setImageBitmap(b);

                getNetworkAdapter().saveImage(image.getPath(), new SaveCompletedListener() {
                    @Override
                    public void onSaved(BError error, String url) {
                        if (error!=null)
                        {
                            showAlertToast("Unable to save file...");
                        }
                        else {
                            thread.setImageUrl(url);
                            thread.setDirty(true);
                            DaoCore.updateEntity(thread);
                            getNetworkAdapter().pushThreadWithCallback(thread, null);
                        }
                    }
                });
            }
            catch (NullPointerException e){
                if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                showAlertToast("Unable to fetch image");
            }
        }


    }


}
