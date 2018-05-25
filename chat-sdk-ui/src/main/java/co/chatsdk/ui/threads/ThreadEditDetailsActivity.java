/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Pepe Becker on 09/05/18.
 */
public class ThreadEditDetailsActivity extends BaseActivity {

    /** Set true if you want slide down animation for this context exit. */
    protected boolean animateExit = false;

    protected DisposableList disposableList = new DisposableList();

    protected ActionBar actionBar;
    protected String threadEntityID;
    protected Thread thread;
    protected EditText nameInput;
    protected Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        threadEntityID = getIntent().getStringExtra(InterfaceManager.THREAD_ENTITY_ID);
        if (threadEntityID != null) {
            thread = StorageManager.shared().fetchThreadWithEntityID(threadEntityID);
        }

        setContentView(R.layout.chat_sdk_activity_edit_thread_details);
        initViews();
    }

    protected void initViews() {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        nameInput = findViewById(R.id.chat_sdk_edit_thread_name_et);
        saveButton = findViewById(R.id.chat_sdk_edit_thread_update_b);

        if (thread != null) {
            actionBar.setTitle(Strings.nameForThread(thread));
            nameInput.setText(thread.getName());
            saveButton.setText(R.string.update_thread);

            // TODO: permanently move thread name into meta data
            ThreadMetaValue nameMetaValue = thread.metaValueForKey("name");
            if (nameMetaValue != null)
                nameInput.setText(nameMetaValue.getValue());
        } else {
            saveButton.setEnabled(false);
        }

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(!nameInput.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveButton.setOnClickListener(v -> {
            setSaveButtonOnClickListener();
        });

    }

    protected void setSaveButtonOnClickListener() {
        final String threadName = nameInput.getText().toString();
        if (thread == null) {
            showOrUpdateProgressDialog(getString(R.string.add_public_chat_dialog_progress_message));

            disposableList.add(ChatSDK.publicThread().createPublicThreadWithName(threadName)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((newThread, throwable) -> {
                        if (throwable == null) {
                            // TODO: permanently move thread name into meta data
                            newThread.setMetaValue("name", threadName);
                            disposableList.add(ChatSDK.thread().pushThreadMeta(newThread).subscribe(() -> {
                                dismissProgressDialog();
                                ToastHelper.show(ChatSDK.shared().context(), String.format(getString(R.string.thread__created), threadName));

                                ChatSDK.ui().startChatActivityForID(ChatSDK.shared().context(), newThread.getEntityID());
                            }));
                        } else {
                            ChatSDK.logError(throwable);
                            Toast.makeText(ChatSDK.shared().context(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();
                        }
                    }));
        } else {
            // TODO: permanently move thread name into meta data
            thread.setName(threadName);
            thread.update();
            thread.setMetaValue("name", threadName);
            // TODO: Update the thread name
            disposableList.add(ChatSDK.thread().pushThreadMeta(thread).subscribe(this::finish));
        }
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
    protected void onStop() {
        disposableList.dispose();
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
