package sdk.chat.contact;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.guru.common.BaseConfig;


/**
 * Created by ben on 10/9/17.
 */

public class ContactBookModule extends AbstractModule {

    public static final ContactBookModule instance = new ContactBookModule();

    public static ContactBookModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<ContactBookModule> builder() {
        return instance.config;
    }

    public static ContactBookModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    @Override
    public void activate(Context context) {
        Report.shared().add(getName());
        ChatSDK.ui().addSearchActivity(ContactBookSearchActivity.class, context.getString(R.string.contact_book));

    }

    public static class Config<T> extends BaseConfig<T> {


        // Contact Book
        public String contactBookInviteContactEmailSubject;
        public String contactBookInviteContactEmailBody;
        public String contactBookInviteContactSmsBody;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Define custom messages for the contact book module invite
         * @param inviteEmailSubject
         * @param inviteEmailBody
         * @param inviteSmsBody
         * @return
         */
        public Config<T> contactBook(String inviteEmailSubject, String inviteEmailBody, String inviteSmsBody) {
            this.contactBookInviteContactEmailSubject = inviteEmailSubject;
            this.contactBookInviteContactEmailBody = inviteEmailBody;
            this.contactBookInviteContactSmsBody = inviteSmsBody;
            return this;
        }

    }

    public Config<ContactBookModule> config = new Config<>(this);

    public static Config config() {
        return shared().config;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
