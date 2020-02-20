/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.squareup.picasso.Picasso;

import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.databinding.ActivityEditThreadBinding;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import co.chatsdk.ui.utils.ImagePickerUploader;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;

/**
 * Created by Pepe Becker on 09/05/18.
 */
public class EditThreadActivity extends BaseActivity {

    /** Set true if you want slide down animation for this context exit. */
    protected boolean animateExit = false;

    protected ActionBar actionBar;
    protected String threadEntityID;

    protected Thread thread;
    protected ArrayList<User> users = new ArrayList<>();

    protected String threadImageURL;
    protected ImagePickerUploader pickerUploader = new ImagePickerUploader(MediaSelector.CropType.Circle);

    protected ActivityEditThreadBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        threadEntityID = getIntent().getStringExtra(Keys.IntentKeyThreadEntityID);
        if (threadEntityID != null) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        }

        List<String> userEntityIDs = getIntent().getStringArrayListExtra(Keys.IntentKeyUserEntityIDList);
        if (userEntityIDs != null) {
            for (String userEntityID : userEntityIDs) {
                User user = ChatSDK.db().fetchUserWithEntityID(userEntityID);
                if (user != null) {
                    users.add(user);
                }
            }
        }

        initViews();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_thread;
    }

    protected void initViews() {
        super.initViews();

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        b.nameTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.fab.setOnClickListener(v -> didClickOnSaveButton());
        b.fab.setImageDrawable(Icons.get(Icons.choose().check, R.color.fab_icon_color));

        b.threadImageView.setOnClickListener(view -> {
            dm.add(pickerUploader.choosePhoto(this).subscribe(files -> {
                if (!files.isEmpty()) {
                    showProgressDialog(EditThreadActivity.this.getString(R.string.uploading));
                    dm.add(pickerUploader.uploadImageFile(files.get(0)).subscribe(result -> {
                        if (result != null) {
                            updateThreadImageURL(result.url);
                            refreshView();
                        }
                        dismissProgressDialog();
                    }, this));
                }
            }, this));
        });

        refreshView();
    }

    protected void updateSaveButtonState () {
        b.fab.setEnabled(!b.nameTextInput.getText().toString().isEmpty());
    }

    protected void updateThreadImageURL (String url) {
        threadImageURL = url;
    }

    protected void refreshView () {
        if (thread != null) {
            String name = thread.getName();
            actionBar.setTitle(name);
            b.nameTextInput.setText(name);
        } else {
            b.fab.setEnabled(false);
        }
        if (threadImageURL != null) {
            Picasso.get().load(threadImageURL).into(b.threadImageView);
        } else if (thread != null) {
            ThreadImageBuilder.load(b.threadImageView, thread);
        }
        else {
            b.threadImageView.setImageDrawable(Icons.getLarge(Icons.choose().publicChat, R.color.thread_default_icon_color));
        }
        updateSaveButtonState();
    }

    protected void didClickOnSaveButton() {
        final String threadName = b.nameTextInput.getText().toString();

        // There are several ways this view can be used:
        // 1. Create a Public Thread
        // 2. Create a Private Group
        // 3. Update a thread
        if (thread == null) {
            showOrUpdateProgressDialog(getString(R.string.add_public_chat_dialog_progress_message));

            BiConsumer<Thread, Throwable> consumer = (thread, throwable) -> {
                dismissProgressDialog();
                if (throwable == null) {
                    // Finish this activity before opening the new thread to prevent the
                    // user from going back to the creation screen by pressing the back button
                    finish();
                    ChatSDK.ui().startChatActivityForID(ChatSDK.shared().context(), thread.getEntityID());
                } else {
                    showToast(throwable.getLocalizedMessage());
                }
            };

            // If we aren't adding users then this is a public thread
            if (users.isEmpty()) {
                dm.add(ChatSDK.publicThread().createPublicThreadWithName(threadName, null, null, threadImageURL)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
            } else {
                dm.add(ChatSDK.thread().createThread(threadName, users, ThreadType.PrivateGroup, null, threadImageURL)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
            }
        } else {
            thread.setName(threadName);
            if (threadImageURL != null) {
                thread.setImageUrl(threadImageURL);
            }
            thread.update();
            dm.add(ChatSDK.thread().pushThread(thread).subscribe(this::finish, this));
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);

        finish();
        if (animateExit) {
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

}
