package app.xmpp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.xmpp.adapter.utils.PresenceHelper;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.types.KeyValue;
import sdk.chat.core.utils.StringChecker;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;


/**
 * Created by kykrueger on 2016-10-23.
 */

public class XMPPUserManager {

    public static String Voice = "VOICE";
    public static String Locality = "LOCALITY";
    public static String Name = "FN";

    public static String ContactGroupName = "Contacts";

    private WeakReference<XMPPManager> manager;
    private DisposableMap disposables = new DisposableMap();

    public XMPPUserManager(XMPPManager manager){
        this.manager = new WeakReference<>(manager);
    }

    protected Map<String, VCard> vCardCache = new HashMap<>();

    private Observable<RosterEntry> getRosterEntries () {
        return Observable.create((ObservableOnSubscribe<RosterEntry>) e -> {
            Roster roster = manager.get().roster();
            for(RosterEntry entry : roster.getEntries()) {
                e.onNext(entry);
            }
            e.onComplete();
        }).subscribeOn(RX.io());
    }

//    public Observable<User> getAllAddedUsers() {
//        return getRosterEntries().flatMap(rosterEntry -> updateUserFromVCard(rosterEntry.getJid()).toObservable());
//    }

    public Completable addUserToRoster (final User user) {
        return Completable.defer(() -> {
            Roster roster = manager.get().roster();
            String [] groups = {ContactGroupName};
            roster.createEntry(JidCreate.bareFrom(user.getEntityID()), user.getName(), groups);
            return subscribeToUser(user);
        }).subscribeOn(RX.io());
    }

    public Completable removeUserFromRoster (final User user) {
        return Completable.defer(() -> {
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
            return unsubscribeFromUser(user);
        }).subscribeOn(RX.io());
    }

    public Single<List<KeyValue>> getAvailableSearchFields () {
        return getSearchService().map(jid -> {
            Form searchForm = manager.get().userSearchManager().getSearchForm(jid.asDomainBareJid());
            List<FormField> fields = searchForm.getFields();
            List<KeyValue> stringFields = new ArrayList<>();
            for(FormField f : fields) {
                stringFields.add(new KeyValue(f.getVariable(), f.getLabel()));
            }
            return stringFields;
        }).subscribeOn(RX.io());
    }

    public Single<Jid> getSearchService () {
        return Single.create((SingleOnSubscribe<Jid>) emitter -> {
            UserSearchManager userSearchManager = manager.get().userSearchManager();
            List<DomainBareJid> jids = userSearchManager.getSearchServices();

            if(jids.size() > 0) {

                DomainBareJid searchJID = null;
                for (DomainBareJid jid : jids) {
                    // Get the information for each one
                    List<DiscoverInfo.Identity> infos = ServiceDiscoveryManager.getInstanceFor(manager.get().getConnection()).discoverInfo(jid).getIdentities();
                    for (DiscoverInfo.Identity info : infos) {
                        if (info.getCategory().equals("directory") && info.getType().equals("user")) {
                            searchJID = jid;
                            break;
                        }
                    }
                }
                emitter.onSuccess(searchJID);
            }
            else {
                emitter.onError(new Throwable(ChatSDK.shared().context().getString(R.string.search_not_available)));
            }
        }).subscribeOn(RX.io());
    }

    public Observable<Jid> searchUser(final String searchIndex, final String searchValue) {
        return getSearchService().flattenAsObservable(jid -> {

            UserSearchManager userSearchManager = manager.get().userSearchManager();
            Form searchForm = userSearchManager.getSearchForm(jid.asDomainBareJid());

            Form answerForm = searchForm.createAnswerForm();

            // There are two ways to do search:
            // OpenFire
            // Username => true
            // search => user-name
            //
            // eJabberd
            // user => user-name

            // If the search field is mandatory then we know that it's the OpenFire style
            if(searchForm.hasField("search") && searchForm.getField("search").isRequired()) {
                FormField username = searchForm.getField("Username");
                username.addValue("1");

//                builder.addField(FormField.textSingleBuilder("Username").setValue(1).build());
//                builder.addField(FormField.textSingleBuilder("search").setValue(searchValue).build());

                // For some reason, 1 does work but true doesn't ^^
                //                answerForm.setAnswer("Username", true);

//                answerForm.setAnswer("Name", true);
//                answerForm.setAnswer("Email", true);
                answerForm.setAnswer("search", searchValue);
            }
            else {
                answerForm.setAnswer(searchIndex, searchValue);
            }

            ReportedData data = userSearchManager.getSearchResults(answerForm, jid.asDomainBareJid());
//            userSearchManager.getSearchResults(answerForm)

            List<Jid> jids = new ArrayList<>();
            for(ReportedData.Row row : data.getRows()) {
                List<CharSequence> values = row.getValues("jid");
                for(CharSequence value : values) {
                    jids.add(JidCreate.bareFrom(value));
                }
            }
            return jids;
        }).subscribeOn(RX.io());
    }

    public Single<User> updateUserFromVCard (final Jid jid) {
        return Single.defer(() -> {
            boolean blocked = ChatSDK.blocking().isBlocked(jid.asBareJid().toString());
            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());

            if(blocked) {
                Localpart local = jid.getLocalpartOrNull();
                String username = local != null ? local.toString() : "";
                user.setName(username);
            }
            else {
                try {
                    VCard vCard = vCardForUser(user.getEntityID());
                    user = vCardToUser(vCard, jid);
                    updateUserFromRoster(user).subscribe(ChatSDK.events());
                }
                catch (Exception e) {
                    return Single.error(e);
                }
            }
            return Single.just(user);
        }).subscribeOn(RX.io());
    }

    protected VCard vCardForUser(String userEntityID) throws Exception {
        VCard vCard = vCardCache.get(userEntityID);
        if (vCard == null) {
            EntityBareJid jid = JidCreate.entityBareFrom(userEntityID);
            VCardManager vCardManager = manager.get().vCardManager();
            vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());
            vCardCache.put(userEntityID, vCard);
        }
        return vCard;
    }

//    private Single<VCard> vCardForUser(final Jid jid) {
//        return Single.create((SingleOnSubscribe<VCard>) e -> {
//            VCardManager vCardManager = manager.get().vCardManager();
//            VCard vCard = vCardManager.loadVCard(jid.asEntityBareJidIfPossible());
//
//            e.onSuccess(vCard);
//        }).subscribeOn(RX.io());
//    }

    public Completable subscribeToUser (final User user) {
        return Completable.create(e -> {
            Presence request = new Presence(Presence.Type.subscribe);
            request.setTo(user.getEntityID());
            manager.get().sendStanza(request);
            e.onComplete();
        }).subscribeOn(RX.io());
    }

    public Completable unsubscribeFromUser (final User user) {
        return Completable.create(e -> {
            Presence request = new Presence(Presence.Type.unsubscribe);
            request.setTo(user.getEntityID());
            manager.get().sendStanza(request);
            e.onComplete();
        }).subscribeOn(RX.io());
    }

    /**
     * We pass in the jid because some servers don't populate the vCard from field (OpenFire)
     * @param vCard
     * @param jid
     * @return
     * @throws Exception
     */
    public User vCardToUser(VCard vCard, Jid jid) throws Exception {

        if(jid == null) {
            jid = vCard.getFrom();
        }
        if(jid == null) {
            throw new Exception("Cannot get user for null JID");
        }

        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());

        Map<String, Object> oldMeta = new HashMap<>(user.metaMap());

        String name = vCard.getNickName();
//        name = vCard.getFirstName() + vCard.getLastName();

        if(StringChecker.isNullOrEmpty(name)) {
            if(!StringChecker.isNullOrEmpty(vCard.getFirstName()) && !StringChecker.isNullOrEmpty(vCard.getLastName())) {
                name = vCard.getFirstName() + " " + vCard.getLastName();
            }
            if(!StringChecker.isNullOrEmpty(vCard.getFirstName())) {
                name = vCard.getFirstName();
            }
        }
        if (StringChecker.isNullOrEmpty(name)) {
            name = vCard.getField(Name);
        }

        if(StringChecker.isNullOrEmpty(name)) {
            name = jid.getLocalpartOrNull() != null ? jid.getLocalpartOrNull().toString() : "";
        }

        if(!StringChecker.isNullOrEmpty(name)) {
            user.setName(name, false);
        }

        String email = vCard.getEmailHome();
        email = email == null ? vCard.getEmailWork() : email;

        if(!StringChecker.isNullOrEmpty(email)) {
            user.setEmail(email, false);
        }

        String locality = vCard.getAddressFieldHome(Locality);
        locality = locality == null ? vCard.getAddressFieldWork(Locality) : locality;

        if(!StringChecker.isNullOrEmpty(locality)) {
            user.setLocation(locality, false);
        }

        String phone = vCard.getPhoneHome(Voice);
        phone = phone == null ? vCard.getPhoneWork(Voice) : phone;

        if(!StringChecker.isNullOrEmpty(phone)) {
            user.setPhoneNumber(phone, false);
        }

        byte[] avatarData = vCard.getAvatar();

        if(avatarData != null && avatarData.length > 0 && !vCard.getAvatarHash().equals(user.getAvatarHash())) {
            Bitmap bmp = BitmapFactory.decodeByteArray(avatarData, 0, avatarData.length);

            File imageFile = ImageUtils.saveBitmapToFile(bmp);

            user.setAvatarURL(Uri.fromFile(imageFile).toString(), vCard.getAvatarHash(), false);
        }

        // Update the UI if necessary
        if (!oldMeta.entrySet().equals(user.metaMap().entrySet())) {
            ChatSDK.events().source().accept(NetworkEvent.userMetaUpdated(user));
        }

        return user;
    }

    public Completable updateMyvCardWithUser(final User user) {
        return Completable.create(e -> {

            VCardManager vCardManager = manager.get().vCardManager();

            VCard vCard = vCardManager.loadVCard();

            String name = user.getName();
            if (name != null && !name.isEmpty()) {
                vCard.setField(Name, name);
                vCard.setNickName(name);
            }

            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                vCard.setEmailHome(email);
            }

            String locality = user.getLocation();
            if (locality != null && !locality.isEmpty()) {
                vCard.setAddressFieldHome(Locality, locality);
            }

            String phone = user.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                vCard.setPhoneHome(Voice, phone);
            }

            final Runnable updateVCard = () -> {
                try {
                    vCardManager.saveVCard(vCard);
                    ChatSDK.events().source().accept(NetworkEvent.userMetaUpdated(ChatSDK.currentUser()));
                    e.onComplete();
                }
                catch (Exception ex) {
                    e.onError(ex);
                }
            };

            String pictureURL = user.getAvatarURL();

            // Has the image changed?
            String avatarHash = user.getAvatarHash();
            if((StringChecker.isNullOrEmpty(avatarHash) || !user.getAvatarHash().equals(vCard.getAvatarHash())) && !StringChecker.isNullOrEmpty(pictureURL)) {

                // This will be a URL so we need to download it
                ChatSDK.events().disposeOnLogout(ImageUtils.bitmapForURL(pictureURL, 400, 400).subscribe(bitmap -> {
                    File imageFile = ImageUtils.saveBitmapToFile(bitmap);
                    // Check to see if the picture has changed
                    File compress = new Compressor(ChatSDK.shared().context())
                            .setMaxHeight(400)
                            .setMaxWidth(400)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToFile(imageFile);

                    vCard.setAvatar(FileManager.fileToBytes(compress));
                    user.setAvatarHash(vCard.getAvatarHash());

                    updateVCard.run();

                }, ChatSDK.events()));
            } else {
                updateVCard.run();
            }


        }).subscribeOn(RX.io());
    }

    public Completable loadContactsFromRoster () {
        return Completable.defer(() -> {

            // Add the contacts from the roster
            List<User> contacts = ChatSDK.contact().contacts();
            final List<User> rosterContacts = new ArrayList<>();

            List<Completable> completables = new ArrayList<>();

            Set<RosterEntry> entries = manager.get().roster().getEntries();
            for(RosterEntry entry : entries) {

                if (entry.getType() == RosterPacket.ItemType.from || entry.getType() == RosterPacket.ItemType.none) {
                    continue;
                }

                // Get the entity ID and try to get the user
                String entityID = entry.getJid().asBareJid().toString();

                User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
                completables.add(updateUserFromVCard(entry.getJid()).ignoreElement());

                if(user != null) {
                    rosterContacts.add(user);
                }
            }

            contacts.removeAll(rosterContacts);

            // These are contacts that exist in out current contacts but
            // aren't in the roster so they should be deleted
            for(User user : contacts) {
                ChatSDK.contact().deleteContactLocal(user, ConnectionType.Contact);
            }

            return Completable.concat(completables).doOnComplete(() -> {
                // Now all the users have been updated from their vCards so we can
                // Add them as contacts and update the Contact List
                for(User u: rosterContacts) {
                    if(ChatSDK.currentUser() != null) {
                        ChatSDK.currentUser().addContact(u);
                    }
                }
            });
        }).subscribeOn(RX.io());
    }

    public Completable updateUserFromRoster (final User user) {
        return Completable.create(e -> {
            Roster roster = manager.get().roster();

            Jid jid = JidCreate.bareFrom(user.getEntityID());
            RosterEntry entry = roster.getEntry(jid.asBareJid());
            if(entry != null) {
                user.setPresenceSubscription(entry.getType().toString(), false);
            }
            Presence presence = roster.getPresence(jid.asBareJid());
            PresenceHelper.updateUserFromPresence(user, presence);

        }).subscribeOn(RX.io());
    }

    public Completable addContact (Jid jid) {
        return updateUserFromVCard(jid).flatMapCompletable(user -> ChatSDK.contact().addContact(user, ConnectionType.Contact));
    }

//    public void clearContacts () {
//        List<User> contacts = ChatSDK.contact().contacts();
//        for(User user : contacts) {
//            // Delete straight from the user because otherwise we
//            // would also remove them from the roster
//            ChatSDK.currentUser().deleteContact(user, ConnectionType.Contact);
//        }
//    }

    public void dispose () {
        disposables.dispose();
    }


}
