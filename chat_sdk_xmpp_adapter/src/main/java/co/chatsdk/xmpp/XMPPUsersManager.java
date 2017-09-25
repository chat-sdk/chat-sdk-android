package co.chatsdk.xmpp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.KeyValue;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.StringUtils;
import co.chatsdk.xmpp.utils.PresenceHelper;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by kykrueger on 2016-10-23.
 */

public class XMPPUsersManager {

    public static String Voice = "VOICE";
    public static String Locality = "LOCALITY";
    public static String Country = "CTRY";
    public static String DateOfBirth = "BDAY";
    public static String DateFormat = "yyyy-MM-dd";
    public static String Name = "FN";

    public static String ContactGroupName = "Contacts";

    private WeakReference<XMPPManager> manager;
    private DisposableList disposables = new DisposableList();

    public XMPPUsersManager(XMPPManager manager){
        this.manager = new WeakReference<>(manager);
    }


    private Observable<RosterEntry> getRosterEntries () {
        return Observable.create(new ObservableOnSubscribe<RosterEntry>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<RosterEntry> e) throws Exception {
                Roster roster = manager.get().roster();
                for(RosterEntry entry : roster.getEntries()) {
                    e.onNext(entry);
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    public Observable<User> getAllAddedUsers() {
        return getRosterEntries().flatMap(new Function<RosterEntry, ObservableSource<User>>() {
            @Override
            public ObservableSource<User> apply(RosterEntry rosterEntry) throws Exception {
                return updateUserFromVCard(rosterEntry.getJid()).toObservable();
            }
        });
    }

    public Completable addUserToRoster (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Roster roster = manager.get().roster();
                String [] groups = {ContactGroupName};
                roster.createEntry(JidCreate.bareFrom(user.getEntityID()), user.getName(), groups);
                e.onComplete();
            }
        }).concatWith(subscribeToUser(user));
    }

    public Completable removeUserFromRoster (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Roster roster = manager.get().roster();
                RosterEntry entry = null;
                for(RosterEntry en : roster.getEntries()) {
                    if(en.getJid().asBareJid().toString().equals(user.getEntityID())) {
                        entry = en;
                        break;
                    }
                }
                if(entry != null) {
                    roster.removeEntry(entry);
                }
                e.onComplete();
            }
        }).concatWith(unsubscribeFromUser(user));
    }

    public Single<List<KeyValue>> getAvailableSearchFields () {
        return Single.create(new SingleOnSubscribe<List<KeyValue>>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<List<KeyValue>> e) throws Exception {
                Form searchForm = manager.get().userSearchManager().getSearchForm(manager.get().searchService);
                List<FormField> fields = searchForm.getFields();
                ArrayList<KeyValue> stringFields = new ArrayList<>();
                for(FormField f : fields) {
                    stringFields.add(new KeyValue(f.getVariable(), f.getLabel()));
                }
                e.onSuccess(stringFields);
            }
        }).subscribeOn(Schedulers.single());
    }

    public Observable<Jid> searchUser(final String searchIndex, final String searchValue) {
        return Observable.create(new ObservableOnSubscribe<Jid>() {
            @Override
            public void subscribe(ObservableEmitter<Jid> e) throws Exception {

                UserSearchManager userSearchManager = manager.get().userSearchManager();
                Form searchForm = userSearchManager.getSearchForm(manager.get().searchService);
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer(searchIndex, searchValue);

                ReportedData data = userSearchManager.getSearchResults(answerForm, manager.get().searchService);

                for(ReportedData.Row row : data.getRows()) {
                    List<String> values = row.getValues("jid");
                    for(String value : values) {
                        e.onNext(JidCreate.bareFrom(value));
                    }
                }

                e.onComplete();

            }
        }).subscribeOn(Schedulers.single());
    }

    private Single<VCard> vCardForUser (final Jid jid) {
        return Single.create(new SingleOnSubscribe<VCard>() {
            @Override
            public void subscribe(final SingleEmitter<VCard> e) throws Exception {
                AbstractXMPPConnection conn = manager.get().getConnection();
                VCardManager vCardManager = VCardManager.getInstanceFor(conn);
                VCard vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());

                e.onSuccess(vCard);
            }
        }).subscribeOn(Schedulers.single());
    }

    public Single<Boolean> userBlocked (final Jid jid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                boolean blocked = XMPPManager.shared().blockingCommandManager().getBlockList().contains(jid);
                e.onSuccess(blocked);
            }
        }).subscribeOn(Schedulers.single());
    }

    public Single<User> updateUserFromVCard (final Jid jid) {
        return userBlocked(jid).flatMap(new Function<Boolean, SingleSource<? extends User>>() {
            @Override
            public SingleSource<? extends User> apply(@NonNull Boolean blocked) throws Exception {
                User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());
                if(blocked) {
                    Localpart local = jid.getLocalpartOrNull();
                    String username = local != null ? local.toString() : "";
                    user.setName(username + " ("+AppContext.shared().context().getString(co.chatsdk.ui.R.string.blocked)+")");
                    user.update();
                }
                else {
                    AbstractXMPPConnection conn = manager.get().getConnection();
                    VCardManager vCardManager = VCardManager.getInstanceFor(conn);
                    VCard vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());
                    user = vCardToUser(vCard);
                    disposables.add(updateUserFromRoster(user).subscribe());
                }
                return Single.just(user);
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable subscribeToUser (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Presence request = new Presence(Presence.Type.subscribe);
                request.setTo(user.getEntityID());
                manager.get().getConnection().sendStanza(request);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable unsubscribeFromUser (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Presence request = new Presence(Presence.Type.unsubscribe);
                request.setTo(user.getEntityID());
                manager.get().getConnection().sendStanza(request);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    public User vCardToUser(VCard vCard) {

        Jid jid = vCard.getFrom();
        User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());

        String name = vCard.getNickName();
//        name = vCard.getFirstName() + vCard.getLastName();

        if(StringUtils.isNullOrEmpty(name)) {
            if(!StringUtils.isNullOrEmpty(vCard.getFirstName()) && !StringUtils.isNullOrEmpty(vCard.getLastName())) {
                name = vCard.getFirstName() + " " + vCard.getLastName();
            }
            if(!StringUtils.isNullOrEmpty(vCard.getFirstName())) {
                name = vCard.getFirstName();
            }
        }

        if(StringUtils.isNullOrEmpty(name)) {
            name = jid.getLocalpartOrNull() != null ? jid.getLocalpartOrNull().toString() : "";
        }

        if(name != null && !name.isEmpty()) {
            user.setName(name);
        }

        String email = vCard.getEmailHome();
        email = email == null ? vCard.getEmailWork() : email;

        if(email != null && !email.isEmpty()) {
            user.setEmail(email);
        }

        String countryCode = vCard.getAddressFieldHome(Country);
        countryCode = countryCode == null ? vCard.getAddressFieldWork(Country) : countryCode;

        if(countryCode != null && !countryCode.isEmpty()) {
            user.setCountryCode(countryCode);
        }

        String locality = vCard.getAddressFieldHome(Locality);
        locality = locality == null ? vCard.getAddressFieldWork(Locality) : locality;

        if(locality != null && !locality.isEmpty()) {
            user.setLocation(locality);
        }

        String phone = vCard.getPhoneHome(Voice);
        phone = phone == null ? vCard.getPhoneWork(Voice) : phone;

        if(phone != null && !phone.isEmpty()) {
            user.setPhoneNumber(phone);
        }

        String dateOfBirth = vCard.getField(DateOfBirth);

        if(dateOfBirth != null && !dateOfBirth.isEmpty()) {
            user.setDateOfBirth(dateOfBirth);
        }

        byte[] avatarData = vCard.getAvatar();

        if(avatarData != null && avatarData.length > 0 && !vCard.getAvatarHash().equals(user.getAvatarHash())) {
            Bitmap bmp = BitmapFactory.decodeByteArray(avatarData, 0, avatarData.length);
            String url = ImageUtils.saveToInternalStorage(bmp, jid.asBareJid().toString());
            user.setAvatarURL(url, vCard.getAvatarHash());
        }

        DaoCore.createOrReplace(user);
        user.update();

        NM.events().source().onNext(NetworkEvent.userMetaUpdated(user));

        return user;
    }

    public Completable updateMyvCardWithUser(final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {

                VCardManager vCardManager = VCardManager.getInstanceFor(manager.get().getConnection());

                VCard vCard = vCardManager.loadVCard();

                boolean changed = false;

                String name = user.getName();
                if (name != null && !name.isEmpty()) {
                    vCard.setField(Name, name);
                    vCard.setNickName(name);
                    changed = changed || name.equals(vCard.getField(Name));
                }

                String email = user.getEmail();
                if (email != null && !email.isEmpty()) {
                    vCard.setEmailHome(email);
                    changed = changed || email.equals(vCard.getEmailHome());
                }

                String countryCode = user.getCountryCode();
                if (countryCode != null && !countryCode.isEmpty()) {
                    vCard.setAddressFieldHome(Country, countryCode);
                    changed = changed || countryCode.equals(vCard.getAddressFieldHome(Country));
                }

                String locality = user.getLocation();
                if (locality != null && !locality.isEmpty()) {
                    vCard.setAddressFieldHome(Locality, locality);
                    changed = changed || locality.equals(vCard.getAddressFieldHome(Locality));
                }

                String phone = user.getPhoneNumber();
                if (phone != null && !phone.isEmpty()) {
                    vCard.setPhoneHome(Voice, phone);
                    changed = changed || phone.equals(vCard.getPhoneHome(Voice));
                }

                String date = user.getDateOfBirth();
                if (date != null) {
                    changed = changed || date.equals(vCard.getField(DateOfBirth));
                    vCard.setField(DateOfBirth, date);
                }

                // Has the image changed?
                String avatarHash = user.getAvatarHash();
                if(avatarHash != null && !user.getAvatarHash().equals(vCard.getAvatarHash())) {
                    changed = true;

                    String pictureURL = user.getAvatarURL();
                    if(pictureURL != null && !pictureURL.isEmpty()) {
                        // Check to see if the picture has changed
                        File compress = new Compressor(AppContext.shared().context())
                                .setMaxHeight(50)
                                .setMaxWidth(50)
                                .compressToFile(new File(pictureURL));

                        vCard.setAvatar(new URL("file:" + compress.toString()));
                        user.setAvatarHash(vCard.getAvatarHash());
                    }
                }


                try {
                    if(changed) {
                        NM.events().source().onNext(NetworkEvent.userMetaUpdated(NM.currentUser()));
                        vCardManager.saveVCard(vCard);
                    }
                    e.onComplete();
                }
                catch (SmackException.NoResponseException exc) {
                    e.onError(exc);
                }
                catch (XMPPException.XMPPErrorException exc) {
                    e.onError(exc);
                }
                catch (SmackException.NotConnectedException exc) {
                    e.onError(exc);
                }

            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable loadContactsFromRoster () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                // Add the contacts from the roster
                List<User> contacts = NM.contact().contacts();
                final ArrayList<User> rosterContacts = new ArrayList<>();

//                RosterGroup group = manager.get().roster().getGroup(ContactGroupName);
                ArrayList<Completable> completables = new ArrayList<>();

//                if(group != null) {
                    Set<RosterEntry> entries = manager.get().roster().getEntries();
                    for(RosterEntry entry : entries) {

                        // Get the entity ID and try to get the user
                        String entityID = entry.getJid().asBareJid().toString();

//                        User user = StorageManager.shared().fetchUserWithEntityID(entityID);
                        User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, entityID);
                        completables.add(updateUserFromVCard(entry.getJid()).toCompletable());

                        // If the user doesn't already exist, add it
//                        if(user == null || !contacts.contains(user)) {
//                            completables.add(addContact(entry.getJid()));
//                        }
//                        else {
//                        }

                        if(user != null) {
                            rosterContacts.add(user);
                        }
                    }
//                }

                contacts.removeAll(rosterContacts);

                // These are contacts that exist in out current contacts but
                // aren't in the roster so they should be deleted
                for(User user : contacts) {
                    completables.add(deleteContact(user.getEntityID()));
                }

                disposables.add(Completable.concat(completables).subscribe(new Action() {
                    @Override
                    public void run() throws Exception {

                        // Now all the users have been updated from their vCards so we can
                        // Add them as contacts and update the Contact List
                        for(User u: rosterContacts) {
                            if(NM.currentUser() != null) {
                                NM.currentUser().addContact(u);
                            }
                        }

                        NM.events().source().onNext(NetworkEvent.contactsUpdated());

                        e.onComplete();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        e.onError(throwable);
                    }
                }));
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable updateUserFromRoster (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                Roster roster = manager.get().roster();

                Jid jid = JidCreate.bareFrom(user.getEntityID());
                RosterEntry entry = roster.getEntry(jid.asBareJid());
                if(entry != null) {
                    user.setPresenceSubscription(entry.getType().toString());
                }
                Presence presence = roster.getPresence(jid.asBareJid());
                PresenceHelper.updateUserFromPresence(user, presence);

            }
        }).subscribeOn(Schedulers.single());

    }

    public Completable addContact (Jid jid) {
        return updateUserFromVCard(jid).flatMapCompletable(new Function<User, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull User user) throws Exception {
                return NM.contact().addContact(user, ConnectionType.Contact);
            }
        });
    }

    public Completable deleteContact (final String jid) {
        return Single.create(new SingleOnSubscribe<User>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<User> e) throws Exception {
                e.onSuccess(StorageManager.shared().fetchUserWithEntityID(jid));
            }
        }).flatMapCompletable(new Function<User, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull User user) throws Exception {
                return NM.contact().deleteContact(user, ConnectionType.Contact);
            }
        });
    }

    public void clearContacts () {
        List<User> contacts = NM.contact().contacts();
        for(User user : contacts) {
            // Delete straight from the user because otherwise we
            // would also remove them from the roster
            NM.currentUser().deleteContact(user, ConnectionType.Contact);
        }
    }

    public void dispose () {
        disposables.dispose();
    }


}
