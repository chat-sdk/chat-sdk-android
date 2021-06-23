package sdk.chat.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookManager {

    public static Single<List<ContactBookUser>> getContactList(Context context) {
        return Single.create((SingleOnSubscribe<List<ContactBookUser>>) emitter -> {
            Logger.debug("Get contact list");

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

            emitter.onSuccess(users);

        }).subscribeOn(RX.computation());

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

    public static Maybe<SearchResult> searchServer(final ContactBookUser contactBookUser) {
        return Maybe.defer(() -> {
            List<Maybe<User>> maybeList = new ArrayList<>();

            for(SearchIndex index : contactBookUser.getSearchIndexes()) {
                maybeList.add(ChatSDK.search().userForIndex(index.value, index.key));
            }
            return Maybe.concat(maybeList).map(user -> {
                return new SearchResult(user, contactBookUser);
            }).firstElement();
       }).subscribeOn(RX.computation());
    }

//    public static Observable<SearchResult> searchServer(final List<ContactBookUser> contactBookUsers) {
//        return Observable.defer(() -> {
//            ArrayList<Observable<SearchResult>> observables = new ArrayList<>();
//            Logger.debug("Search from contacts");
//
//            // Loop over all the contacts and then each search index
//            for(int i = 0; i < contactBookUsers.size(); i++) {
//                final ContactBookUser finalContactBookUser = contactBookUsers.get(i);
//
//                for(SearchIndex index : finalContactBookUser.getSearchIndexes()) {
//
//                    // Search on search for each index in turn then map these results onto
//                    // the search result property so we have access to both the user and the
//                    // contact book user
//                    observables.add(ChatSDK.search().usersForIndex(index.value, 1, index.key).map(user -> {
//                        finalContactBookUser.setUser(user);
//                        return new SearchResult(user, finalContactBookUser);
//                    }));
//                }
//            }
//
//            return Observable.merge(observables);
//        }).subscribeOn(RX.computation());
//    }

//    public static Observable<SearchResult> searchServer(Context context) {
//        return getContactList(context).flatMapObservable(ContactBookManager::searchServer).subscribeOn(RX.io());
//    }

    public static class SearchResult {
        User user;
        ContactBookUser contactBookUser;

        public SearchResult(User user, ContactBookUser contactBookUser) {
            this.user = user;
            this.contactBookUser = contactBookUser;
        }
    }

}
