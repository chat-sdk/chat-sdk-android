/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.BiConsumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.MediaSelector;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.utils.ImagePickerUploader;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.guru.common.RX;

/**
 * Created by Pepe Becker on 09/05/18.
 */
public class EditThreadActivity extends BaseActivity {

    protected String threadEntityID;

    protected Thread thread;
    protected ArrayList<User> users = new ArrayList<>();

    protected String threadImageURL;
    protected ImagePickerUploader pickerUploader = new ImagePickerUploader(MediaSelector.CropType.Circle);

    @BindView(R2.id.threadImageView) protected CircleImageView threadImageView;
    @BindView(R2.id.nameTextInput) protected TextInputEditText nameTextInput;
    @BindView(R2.id.nameTextInputLayout) protected TextInputLayout nameTextInputLayout;
    @BindView(R2.id.fab) protected FloatingActionButton fab;
    @BindView(R2.id.root) protected ConstraintLayout root;

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_thread;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        fab.setImageDrawable(Icons.get(this, Icons.choose().check, R.color.fab_icon_color));

        threadImageView.setOnClickListener(view -> {
            threadImageView.setEnabled(false);
            dm.add(pickerUploader.choosePhoto(this).subscribe(files -> {
                if (!files.isEmpty()) {
                    showProgressDialog(EditThreadActivity.this.getString(R.string.uploading));
                    dm.add(ImageUtils.uploadImageFile(files.get(0)).subscribe(result -> {
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
        if (threadImageURL != null) {
            Glide.with(this).load(threadImageURL).dontAnimate().into(threadImageView);
        } else if (thread != null) {
            ThreadImageBuilder.load(threadImageView, thread);
        } else {
            threadImageView.setImageDrawable(Icons.getLarge(Icons.choose().publicChat, R.color.thread_default_icon_color));
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
                }
            };

            // If we aren't adding users then this is a public thread
            if (users.isEmpty()) {
                dm.add(ChatSDK.publicThread().createPublicThreadWithName(threadName, null, null, threadImageURL)
                        .observeOn(RX.main())
                        .subscribe(consumer));
            } else {

                Map<String, Object> testMap = new HashMap<>();
                testMap.put("test", 123);

                dm.add(ChatSDK.thread().createThread(threadName, users, ThreadType.PrivateGroup, null, threadImageURL, testMap)
                        .observeOn(RX.main())
                        .subscribe(consumer));
            }
        } else {
            thread.setName(threadName);
            if (threadImageURL != null) {
                thread.setImageUrl(threadImageURL);
            }
            thread.update();
            dm.add(ChatSDK.thread().pushThreadMeta(thread).subscribe(this::finish, this));
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
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
