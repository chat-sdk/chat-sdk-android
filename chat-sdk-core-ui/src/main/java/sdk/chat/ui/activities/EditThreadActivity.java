/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.BiConsumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImagePickerUploader;
import sdk.guru.common.RX;

/**
 * Created by Pepe Becker on 09/05/18.
 */
public class EditThreadActivity extends BaseActivity {

    protected String threadEntityID;

    protected Thread thread;
    protected ArrayList<User> users = new ArrayList<>();

    protected String threadImageURL;
    protected ImagePickerUploader pickerUploader = new ImagePickerUploader();

    protected CircleImageView threadImageView;
    protected TextInputEditText nameTextInput;
    protected TextInputLayout nameTextInputLayout;
    protected FloatingActionButton fab;
    protected ConstraintLayout root;

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_thread;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        threadImageView = findViewById(R.id.threadImageView);
        nameTextInput = findViewById(R.id.nameTextInput);
        nameTextInputLayout = findViewById(R.id.nameTextInputLayout);
        fab = findViewById(R.id.fab);
        root = findViewById(R.id.root);

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

    protected void initViews() {
        super.initViews();

        nameTextInput.addTextChangedListener(new TextWatcher() {
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

        fab.setOnClickListener(v -> {
            fab.setEnabled(false);
            didClickOnSaveButton();
        });
        fab.setImageDrawable(ChatSDKUI.icons().get(this, ChatSDKUI.icons().check, R.color.fab_icon_color));

        if (UIModule.config().customizeGroupImageEnabled && ChatSDK.upload() != null) {
            threadImageView.setOnClickListener(view -> {
                threadImageView.setEnabled(false);
                showProgressDialog(EditThreadActivity.this.getString(R.string.uploading));
                dm.add(pickerUploader.chooseCircularPhoto(contract, ChatSDK.config().imageMaxThumbnailDimension).subscribe(results -> {
                    if (results != null && results.size() == 1) {
                        updateThreadImageURL(results.get(0).url);
                        refreshView();
                    }
                    dismissProgressDialog();
                }, this));
            });
        }

        refreshView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setEnabled(true);
        threadImageView.setEnabled(true);
    }

    protected void updateSaveButtonState() {
        if (!StringChecker.isNullOrEmpty(nameTextInput.getText())) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    protected void updateThreadImageURL(String url) {
        threadImageURL = url;
    }

    protected void refreshView() {
        if (thread != null) {
            String name = thread.getName();
            nameTextInput.setText(name);
        }

        if (thread == null) {
            ChatSDKUI.provider().imageLoader().loadThread(threadImageView, threadImageURL, users.size() > 1, R.dimen.large_icon_width);
        } else {
            if (threadImageURL != null) {
                ChatSDKUI.provider().imageLoader().loadThread(threadImageView, threadImageURL, thread.typeIs(ThreadType.Group), R.dimen.large_icon_width);
            } else {
                ChatSDKUI.provider().imageLoader().loadThread(threadImageView, thread, R.dimen.large_icon_width);
            }
        }

        updateSaveButtonState();
    }

    protected void didClickOnSaveButton() {
        final String threadName = nameTextInput.getText().toString();

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
                    ChatSDK.ui().startChatActivityForID(this, thread.getEntityID());
//                    finish();
                } else {
                    showToast(throwable.getLocalizedMessage());
                    fab.setEnabled(true);
                }
            };


            // If we aren't adding users then this is a public thread
            if (users.isEmpty()) {
                dm.add(ChatSDK.publicThread().createPublicThreadWithName(threadName, null, null, threadImageURL)
                        .observeOn(RX.main())
                        .subscribe(consumer));
            } else {

                dm.add(ChatSDK.thread().createThread(threadName, users, ThreadType.PrivateGroup, null, threadImageURL)
                        .observeOn(RX.main())
                        .subscribe(consumer));
            }
        } else {
            thread.setName(threadName);
            if (threadImageURL != null) {
                thread.setImageUrl(threadImageURL);
            }
            ChatSDK.db().update(thread);
            dm.add(ChatSDK.thread().pushThreadMeta(thread).subscribe(() -> {
                Intent intent = new Intent();
                intent.putExtra(Keys.IntentKeyRestartActivity, true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }, this));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
