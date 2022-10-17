package sdk.chat.contact;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import io.reactivex.Single;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.activities.BaseActivity;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.guru.common.RX;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookSearchActivity extends BaseActivity {

    protected UsersListAdapter adapter;
    protected RecyclerView recyclerView;
    protected ProgressBar progressBar;

    @Override
    protected int getLayout() {
        return R.layout.activity_search_contact_book;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        initViews();
        setActionBarTitle(R.string.add_user_from_contacts);

        adapter = new UsersListAdapter(null, false, user -> {
            if (user.getEntityID() != null) {
                return getString(R.string.add_contacts);
            } else {
                return getString(R.string.send_invite);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(30);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear the list of users
//        adapter.clear();

        // TODO: Invite user

        dm.add(adapter.onClickObservable().subscribe(item -> {
            // Search for the user
            if (item instanceof ContactBookUser) {
                ContactBookUser contactUser = (ContactBookUser) item;
                showProgressIndicator();

                ContactBookManager.searchServer(contactUser).observeOn(RX.main()).doOnSuccess(searchResult -> {
                    if (searchResult.user != null) {
                        ChatSDK.contact().addContact(searchResult.user, ConnectionType.Contact)
                                .observeOn(RX.main())
                                .doOnComplete(() -> {
                                    showToast(R.string.contact_added);
                                    adapter.getItems().remove(item);
                                    adapter.notifyDataSetChanged();
                                })
                                .subscribe(this);
                    } else {
                        inviteUser(contactUser);
                    }
                }).doOnComplete(() -> {
                    inviteUser(contactUser);
                }).doFinally(this::hideProgressIndicator)
                        .ignoreElement()
                        .subscribe(this);
            }
        }));

        hideKeyboard();

        if (adapter.getItems().isEmpty()) {

            showProgressIndicator();

            loadUsersFromContactBook().doFinally(() -> {
                hideProgressIndicator();
            }).doOnError(throwable -> finish())
                    .ignoreElement()
                    .subscribe(this);
        }

    }

    protected void sortList() {
        Collections.sort(adapter.getItems(), (o1, o2) -> {
            Boolean b1 = o1.getEntityID() != null;
            Boolean b2 = o2.getEntityID() != null;
            int result = b2.compareTo(b1);
            if (result == 0) {
                String n1 = o1.getName();
                n1 = n1 != null ? n1 : "";
                String n2 = o2.getName();
                n2 = n2 != null ? n2 : "";
                result = n1.compareTo(n2);
            }
            return result;
        });
        RX.main().scheduleDirect(() -> adapter.notifyDataSetChanged());
    }

    private void sendEmail(String emailAddress, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_mail)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void showProgressIndicator() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressIndicator() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void sendSMS(String number, String text) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        intent.putExtra("sms_body", text);
        startActivity(intent);
    }

    public void inviteUser(final ContactBookUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final ArrayList<String> titles = new ArrayList<>();
        final ArrayList<Runnable> runnables = new ArrayList<>();

        if (user.getEmailAddresses().size() > 0) {
            titles.add(getString(R.string.email));
            runnables.add(() -> sendEmail(
                    user.getEmailAddresses().get(0),
                    ContactBookModule.config().contactBookInviteContactEmailSubject,
                    ContactBookModule.config().contactBookInviteContactEmailBody
            ));
        }

        if (user.getPhoneNumbers().size() > 0) {
            titles.add(getString(R.string.sms));
            runnables.add(() -> sendSMS(user.getPhoneNumbers().get(0), ContactBookModule.config().contactBookInviteContactSmsBody));
        }

        String[] items = new String[titles.size()];
        int i = 0;

        for (String title : titles) {
            items[i++] = title;
        }

        builder.setTitle(getString(R.string.invite_contact)).setItems(items, (dialogInterface, i1) -> {
            // Launch the appropriate context
            runnables.get(i1).run();
        });

        builder.show();
    }

    private Single<List<ContactBookUser>> loadUsersFromContactBook() {
        return PermissionRequestHandler.requestReadContact(this)
                .andThen(ContactBookManager.getContactList(getApplicationContext())
                        .map(contactBookUsers -> {
                        List<User> contacts = ChatSDK.contact().contacts();

                        final List<ContactBookUser> toAdd = new ArrayList<>();

                        for (ContactBookUser u : contactBookUsers) {
                            // Check to see if this user is already in our contacts
                            boolean exists = false;
                            for (User user: contacts) {
                                if (u.isUser(user)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                toAdd.add(u);
                            }
                        }

                        RX.main().scheduleDirect(() -> {
                            for (ContactBookUser u: toAdd) {
                                adapter.addUser(u);
                            }
                            adapter.notifyDataSetChanged();
                        });

                        return contactBookUsers;
                    }).subscribeOn(RX.computation()));
    }
}
