package co.chatsdk.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookManager {

    public static ArrayList<ContactBookUser> getContactList(Context context) {

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        ArrayList<ContactBookUser> users = new ArrayList<>();

        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            while (cursor.moveToNext()) {

                ContactBookUser user = new ContactBookUser();

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                addNamesToUser(resolver, id, user);
                addPhoneNumbersToUser(resolver, id, user);
                addEmailsToUser(resolver, id, user);

                if(user.isContactable()) {
                    users.add(user);
                }
            }
        }

        if(cursor != null){
            cursor.close();
        }

        Comparator<ContactBookUser> comparator = (u1, u2) -> u1.getName().compareToIgnoreCase(u2.getName());
        Collections.sort(users, comparator);

        return users;
    }

    private static void addNamesToUser (ContentResolver resolver, String id, ContactBookUser user) {

        String where = ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " =?";
        String[] whereParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, id };

        Cursor nameCur = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                where,
                whereParams,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        );
        while (nameCur.moveToNext()) {
            String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            user.getNames().add(new Name(given, family));
        }
        nameCur.close();
    }

    private static void addPhoneNumbersToUser(ContentResolver resolver, String id, ContactBookUser user) {
        Cursor phoneCursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{id},
                null
        );
        while (phoneCursor.moveToNext()) {
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            user.getPhoneNumbers().add(phoneNumber);
        }
        phoneCursor.close();
    }

    private static void addEmailsToUser(ContentResolver resolver, String id, ContactBookUser user) {
        Cursor emailCursor = resolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id},
                null
        );
        while (emailCursor.moveToNext()) {
            String emailAddress = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            user.getEmailAddresses().add(emailAddress);

        }
        emailCursor.close();
    }

    public static Observable<SearchResult> searchServer(final ArrayList<ContactBookUser> contactBookUsers) {
        return Observable.create((ObservableOnSubscribe<SearchResult>) e -> {

            ArrayList<Observable<SearchResult>> observables = new ArrayList<>();

            // Loop over all the contacts and then each search index
            for(int i = 0; i < contactBookUsers.size(); i++) {
                final ContactBookUser finalContactBookUser = contactBookUsers.get(i);

                for(SearchIndex index : finalContactBookUser.getSearchIndexes()) {

                    Logger.debug("Index: " + index.key + ", value: " + index.value);

                    // Search on search for each index in turn then map these results onto
                    // the search result property so we have access to both the user and the
                    // contact book user
                    observables.add(ChatSDK.search().usersForIndex(index.value, 1, index.key).map(user -> {
                        finalContactBookUser.setEntityID(user.getEntityID());
                        return new SearchResult(user, finalContactBookUser);
                    }));

                    e.onNext(new SearchResult(null, finalContactBookUser));
                }
            }

            // Connect the merged observables to the outer observable. This will retain the benefit
            // of lazy initialization
            new ObservableConnector().connect(Observable.merge(observables), e);
        }).subscribeOn(Schedulers.single());
    }

    public static Observable<SearchResult> searchServer(Context context) {
        return searchServer(getContactList(context));
    }

    public static class SearchResult {
        User user;
        ContactBookUser contactBookUser;

        public SearchResult(User user, ContactBookUser contactBookUser) {
            this.user = user;
            this.contactBookUser = contactBookUser;
        }
    }

}
