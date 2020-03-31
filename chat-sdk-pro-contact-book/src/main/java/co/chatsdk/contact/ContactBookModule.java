package co.chatsdk.contact;

import android.content.Context;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;

/**
 * Created by ben on 10/9/17.
 */

public class ContactBookModule implements Module {

    public static final ContactBookModule instance = new ContactBookModule();

    public static ContactBookModule shared() {
        return instance;
    }

    public static ContactBookModule shared(Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.ui().addSearchActivity(ContactBookSearchActivity.class, ChatSDK.shared().context().getString(R.string.contact_book));
    }

    @Override
    public String getName() {
        return "ContactBookModule";
    }

    public static class Config {

        // Contact Book
        public String contactBookInviteContactEmailSubject;
        public String contactBookInviteContactEmailBody;
        public String contactBookInviteContactSmsBody;

        public Config contactBook(String inviteEmailSubject, String inviteEmailBody, String inviteSmsBody) {
//            this.contactBookInviteContactEmailSubject = inviteEmailSubject;
//            this.contactBookInviteContactEmailBody = inviteEmailBody;
//            this.contactBookInviteContactSmsBody = inviteSmsBody;
            return this;
        }

    }

    public Config config = new Config();

    public static Config config() {
        return shared().config;
    }

}
