package co.chatsdk.xmpp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.KeyValue;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.xmpp.utils.JID;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


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
                return updateUserFromVCard(new JID(rosterEntry.getUser())).toObservable();
            }
        });
    }

    public Completable addUserToRoster (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Roster roster = manager.get().roster();
                String [] groups = {ContactGroupName};
                roster.createEntry(user.getEntityID(), user.getName(), groups);
                e.onComplete();
            }
        }).concatWith(subscribeToUser(user)).subscribeOn(Schedulers.single());
    }

    public Completable removeUserFromRoster (final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                Roster roster = manager.get().roster();
                RosterEntry entry = null;
                for(RosterEntry en : roster.getEntries()) {
                    if(en.getUser().equals(user.getEntityID())) {
                        entry = en;
                        break;
                    }
                }
                if(entry != null) {
                    roster.removeEntry(entry);
                }
                e.onComplete();
            }
        }).concatWith(unsubscribeFromUser(user)).subscribeOn(Schedulers.single());
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

    public Observable<JID> searchUser(final String searchIndex, final String searchValue) {
        return Observable.create(new ObservableOnSubscribe<JID>() {
            @Override
            public void subscribe(ObservableEmitter<JID> e) throws Exception {

                UserSearchManager userSearchManager = manager.get().userSearchManager();
                Form searchForm = userSearchManager.getSearchForm(manager.get().searchService);
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer(searchIndex, searchValue);

                ReportedData data = userSearchManager.getSearchResults(answerForm, manager.get().searchService);

                for(ReportedData.Row row : data.getRows()) {
                    List<String> values = row.getValues("jid");
                    for(String value : values) {
                        e.onNext(new JID(value));
                    }
                }

                e.onComplete();

            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<VCard> vCardForUser (final JID jid) {
        return Single.create(new SingleOnSubscribe<VCard>() {
            @Override
            public void subscribe(SingleEmitter<VCard> e) throws Exception {
                AbstractXMPPConnection conn = manager.get().getConnection();

                VCardManager vCardManager = VCardManager.getInstanceFor(conn);
                VCard vCard = vCardManager.loadVCard(jid.bare());

                e.onSuccess(vCard);
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<User> updateUserFromVCard (final JID jid) {
        return vCardForUser(jid).flatMap(new Function<VCard, SingleSource<? extends User>>() {
            @Override
            public SingleSource<? extends User> apply(VCard vCard) throws Exception {
                return Single.just(vCardToUser(vCard));
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public User vCardToUser(VCard vCard) {

        JID jid = new JID(vCard.getFrom());
        User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, jid.bare());

        String name = vCard.getField(Name);
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
            String url = ImageUtils.saveToInternalStorage(bmp, jid.bare());
            user.setAvatarURL(url, vCard.getAvatarHash());
        }

        DaoCore.createOrReplace(user);

        NM.events().source().onNext(NetworkEvent.userMetaUpdated(NM.currentUser()));

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
                changed = changed || date.equals(vCard.getField(DateOfBirth));
                if (date != null) {
                    vCard.setField(DateOfBirth, date);
                }

                // Has the image changed?
                if(!user.getAvatarHash().equals(vCard.getAvatarHash())) {
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void loadContactsFromRoster () {
        clearContacts();
        // Add the contacts from the roster

        RosterGroup group = manager.get().roster().getGroup(ContactGroupName);
        if(group != null) {
            List<RosterEntry> entries = group.getEntries();
            for(RosterEntry entry : entries) {
                Timber.d("RosterContactAdded");
                addContact(entry.getUser()).subscribe();
            }
        }
    }

    public void updateUserFromRoster (User user) {
        Roster roster = manager.get().roster();
        RosterEntry entry = roster.getEntry(user.getEntityID());
        if(entry != null) {
            user.setPresenceSubscription(entry.getType().toString());
        }
    }

    public Completable addContact (String jid) {
        return updateUserFromVCard(new JID(jid)).flatMapCompletable(new Function<User, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull User user) throws Exception {
                updateUserFromRoster(user);
                return NM.contact().addContact(user, ConnectionType.Contact);
            }
        });
    }

    public void deleteContact (String jid) {
        User user = StorageManager.shared().fetchUserWithEntityID(jid);
        NM.contact().deleteContact(user, ConnectionType.Contact).subscribe();
    }

    public void clearContacts () {
        List<User> contacts = NM.contact().contacts();
        for(User user : contacts) {
            // Delete straight from the user because otherwise we
            // would also remove them from the roster
            NM.currentUser().deleteContact(user, ConnectionType.Contact);
        }
    }


}
