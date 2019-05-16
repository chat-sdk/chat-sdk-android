/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.ActionBar;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by Pepe Becker on 09/05/18.
 */
public class ThreadEditDetailsActivity extends BaseActivity {

    /** Set true if you want slide down animation for this context exit. */
    protected boolean animateExit = false;

    protected ActionBar actionBar;
    protected String threadEntityID;

    protected Thread thread;
    protected ArrayList<User> users = new ArrayList<>();

    protected TextInputEditText nameInput;
    protected MaterialButton saveButton;
    protected SimpleDraweeView threadImageView;
    protected String threadImageURL;
    protected ImagePickerUploader pickerUploader = new ImagePickerUploader(MediaSelector.CropType.Circle);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_edit_thread_details);
        initViews();

    }

    protected void initViews() {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        nameInput = findViewById(R.id.text_name);
        saveButton = findViewById(R.id.button_done);
        threadImageView = findViewById(R.id.image_thread);

        nameInput.addTextChangedListener(new TextWatcher() {
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

        saveButton.setOnClickListener(v -> {
            didClickOnSaveButton();
        });

        threadImageView.setOnClickListener(view -> {
                    showProgressDialog(ThreadEditDetailsActivity.this.getString(R.string.uploading));
                    disposableList.add(pickerUploader.choosePhoto(ThreadEditDetailsActivity.this).subscribe((url, throwable) -> {
                        if (throwable == null) {
                            updateThreadImageURL(url.url);
                        } else {
                            ToastHelper.show(ThreadEditDetailsActivity.this, throwable.getLocalizedMessage());
                        }
                        dismissProgressDialog();
                        refreshView();
                    }));
                });

        refreshView();
    }

    protected void updateSaveButtonState () {
        saveButton.setEnabled(!nameInput.getText().toString().isEmpty());
    }

    protected void updateThreadImageURL (String url) {
        threadImageURL = url;
    }

    protected void refreshView () {
        if (thread != null) {
            String name = thread.getName();
            actionBar.setTitle(name);
            nameInput.setText(name);
            saveButton.setText(R.string.update_thread);
        } else {
            saveButton.setEnabled(false);
        }
        if (threadImageURL != null) {
            threadImageView.setImageURI(threadImageURL);
        } else if (thread != null) {
            threadImageView.setImageURI(thread.getImageUrl());
        }
        updateSaveButtonState();
    }

    protected void didClickOnSaveButton() {
        final String threadName = nameInput.getText().toString();

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
                    ChatSDK.logError(throwable);
                    Toast.makeText(ChatSDK.shared().context(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            // If we aren't adding users then this is a public thread
            if (users.isEmpty()) {
                disposableList.add(ChatSDK.publicThread().createPublicThreadWithName(threadName, null, null, threadImageURL)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
            } else {
                disposableList.add(ChatSDK.thread().createThread(threadName, users, ThreadType.PrivateGroup, null, threadImageURL)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
            }
        } else {
            thread.setName(threadName);
            if (threadImageURL != null) {
                thread.setImageUrl(threadImageURL);
            }
            thread.update();
            disposableList.add(ChatSDK.thread().pushThread(thread).subscribe(this::finish, toastOnErrorConsumer()));
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
