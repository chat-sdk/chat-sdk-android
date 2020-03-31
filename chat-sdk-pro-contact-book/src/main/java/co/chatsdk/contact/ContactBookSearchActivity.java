package co.chatsdk.contact;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.chatsdk.contact.databinding.ActivitySearchContactBookBinding;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.adapters.UsersListAdapter;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookSearchActivity extends BaseActivity {

    String contactsHeader;
    String inviteHeader;

    protected UsersListAdapter adapter;

    protected ActivitySearchContactBookBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        contactsHeader = getString(R.string.contacts);
        inviteHeader = getString(R.string.invite_more_friends);

        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_search_contact_book;
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter = new UsersListAdapter(true);
        b.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerView.setAdapter(adapter);
        b.recyclerView.setHasFixedSize(true);
        b.recyclerView.setDrawingCacheEnabled(true);
        b.recyclerView.setItemViewCacheSize(30);

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.searching));
        dialog.show();

        // Clear the list of users
        adapter.clear();

        // Add the two header threads
//        adapter.addHeader(contactsHeader);
//        adapter.addHeader(inviteHeader);

        // TODO: Invite user

        dm.add(adapter.onClickObservable().subscribe(item -> {
//            if(item instanceof User) {
//                adapter.toggleSelection(item);
//            }
            if (item instanceof ContactBookUser) {
                inviteUser((ContactBookUser) item);
            }
        }));

        hideKeyboard();
        dialog.dismiss();

        dm.add(loadUsersFromContactBook().observeOn(AndroidSchedulers.mainThread()).subscribe(contactBookUsers -> {

            adapter.notifyDataSetChanged();

            final ProgressDialog dialog1 = new ProgressDialog(ContactBookSearchActivity.this);
            dialog1.setMessage(getString(R.string.fetching));
            dialog1.show();

            dm.add(ContactBookManager.searchServer(contactBookUsers)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> {
                        // Remove any phone book users where the entity id is set (because they already exist on the server...)
                        ArrayList<Object> copy = new ArrayList<>(adapter.getItems());
                        Iterator<Object> iterator = copy.iterator();

                        while(iterator.hasNext()) {
                            Object o = iterator.next();
                            if(o instanceof ContactBookUser) {
                                ContactBookUser user = (ContactBookUser) o;
                                if(user.getEntityID() != null) {
                                    adapter.getItems().remove(o);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        dialog1.dismiss();
                    })
                    .subscribe(value -> {
                        if(value.user != null) {
                            // Add the user just before the invite header
                            int indexOfHeader = adapter.getItems().indexOf(inviteHeader);
                            adapter.addUser(value.user, indexOfHeader, true);
                            dialog1.dismiss();
                        }
                    }, this));
        }));

        dialog.setOnCancelListener(dialog1 -> dm.dispose());

        b.button.setOnClickListener(v -> {

            if (adapter.getSelectedCount() == 0) {
                showToast(getString(R.string.no_contacts_selected));
                return;
            }

            ArrayList<Completable> completables = new ArrayList<>();

            for(UserListItem u : adapter.getSelectedUsers()) {
                if(u instanceof User) {
                    completables.add(ChatSDK.contact().addContact((User) u, ConnectionType.Contact));
                }
            }

            final ProgressDialog dialog12 = new ProgressDialog(ContactBookSearchActivity.this);
            dialog12.setMessage(getString(R.string.alert_save_contact));
            dialog12.show();

            dm.add(Completable.merge(completables)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        dialog12.dismiss();
                        finish();
                    }, toastOnErrorConsumer()));
        });


    }

    private void sendEmail (String emailAddress, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_mail)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS (String number, String text) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        intent.putExtra("sms_body", text);
        startActivity(intent);
    }

    public void inviteUser (final ContactBookUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final ArrayList<String> titles = new ArrayList<>();
        final ArrayList<Runnable> runnables = new ArrayList<>();

        if(user.getEmailAddresses().size() > 0) {
            titles.add(getString(R.string.email));
            runnables.add(() -> sendEmail(
                    user.getEmailAddresses().get(0),
                    ContactBookModule.config().contactBookInviteContactEmailSubject,
                    ContactBookModule.config().contactBookInviteContactEmailBody
            ));
        }

        if(user.getPhoneNumbers().size() > 0) {
            titles.add(getString(R.string.sms));
            runnables.add(() -> sendSMS(user.getPhoneNumbers().get(0), ContactBookModule.config().contactBookInviteContactSmsBody));
        }

        String [] items = new String [titles.size()];
        int i = 0;

        for(String title : titles) {
            items[i++] = title;
        }

        builder.setTitle("Invite Contact").setItems(items, (dialogInterface, i1) -> {
            // Launch the appropriate context
            runnables.get(i1).run();
        });

        builder.show();
    }

    private Single<ArrayList<ContactBookUser>> loadUsersFromContactBook () {
        return Single.create((SingleOnSubscribe<ArrayList<ContactBookUser>>) e -> {
            dm.add(PermissionRequestHandler.requestReadContact(ContactBookSearchActivity.this).subscribe(() -> {
                ArrayList<ContactBookUser> contactBookUsers = ContactBookManager.getContactList(ContactBookSearchActivity.this);
                for(ContactBookUser u : contactBookUsers) {
                    adapter.addUser(u);
                }
                e.onSuccess(contactBookUsers);
            }, toastOnErrorConsumer()));
        }).subscribeOn(Schedulers.single());
    }

}
