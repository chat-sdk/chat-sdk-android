package co.chatsdk.contact;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.CompletableSource;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.adapters.UsersListAdapter;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.core.utils.StringChecker;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookSearchActivity extends BaseActivity {

    protected UsersListAdapter adapter;
    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;

    @Override
    protected int getLayout() {
        return R.layout.activity_search_contact_book;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        setActionBarTitle(R.string.add_user_from_contacts);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.searching));
        dialog.show();

        // Clear the list of users
        adapter.clear();

        // TODO: Invite user

        dm.add(adapter.onClickObservable().subscribe(item -> {
            if (item.getEntityID() == null) {
                inviteUser((ContactBookUser) item);
            } else {
                User user = ChatSDK.core().getUserNowForEntityID(item.getEntityID());
                ChatSDK.contact().addContact(user, ConnectionType.Contact)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(() -> {
                            showToast(R.string.contact_added);
                            adapter.getItems().remove(item);
                            adapter.notifyDataSetChanged();
                        })
                        .subscribe(this);
            }
        }));

        hideKeyboard();
        dialog.dismiss();

        loadUsersFromContactBook().observeOn(AndroidSchedulers.mainThread()).flatMapCompletable(contactBookUsers -> {
            return ContactBookManager.searchServer(contactBookUsers).observeOn(AndroidSchedulers.mainThread()).doOnNext(value -> {
                sortList();
            }).ignoreElements();
        }).subscribe(this);

        dialog.setOnCancelListener(dialog1 -> dm.dispose());

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
        adapter.notifyDataSetChanged();
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
        return PermissionRequestHandler.requestReadContact(ContactBookSearchActivity.this).andThen(Single.defer((Callable<SingleSource<List<ContactBookUser>>>) () -> {
            return ContactBookManager.getContactList(getApplicationContext()).map(contactBookUsers -> {
                List<User> contacts = ChatSDK.contact().contacts();

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
                        adapter.addUser(u);
                    }
                }
                adapter.notifyDataSetChanged();

                return contactBookUsers;
            });
        })).subscribeOn(Schedulers.io());
    }

}
