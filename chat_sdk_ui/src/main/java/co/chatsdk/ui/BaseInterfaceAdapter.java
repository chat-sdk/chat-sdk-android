package co.chatsdk.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.ui.activities.MainActivity;
import co.chatsdk.ui.activities.SearchActivity;
import co.chatsdk.ui.activities.SelectContactActivity;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.login.LoginActivity;
import co.chatsdk.ui.profile.EditProfileActivity;
import co.chatsdk.ui.profile.ProfileActivity;
import co.chatsdk.ui.profile.ProfileFragment;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.threads.PublicThreadsFragment;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    public static String USER_ENTITY_ID = "USER_ENTITY_ID";
    public static final String THREAD_ENTITY_ID = "THREAD_ENTITY_ID";
    public static final String ATTEMPT_CACHED_LOGIN = "ATTEMPT_CACHED_LOGIN";

    public static final int REQUEST_CODE_GET_CONTACTS = 101;

    @Override
    public List<Tab> defaultTabs() {

        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(privateThreadsTab());
        tabs.add(publicThreadsTab());
        tabs.add(contactsTab());
        tabs.add(profileTab());

        return tabs;
    }

    @Override
    public Tab privateThreadsTab() {
        return new Tab(R.string.conversations, R.drawable.ic_action_private, privateThreadsFragment());
    }

    @Override
    public Tab publicThreadsTab() {
        return new Tab(R.string.chat_rooms, R.drawable.ic_action_public, publicThreadsFragment());
    }

    @Override
    public Tab contactsTab() {
        return new Tab(R.string.contacts, R.drawable.ic_action_contacts, contactsFragment());
    }

    @Override
    public Tab profileTab() {
        return new Tab (R.string.profile, R.drawable.ic_action_user, ProfileFragment.newInstance(NM.currentUser()));
    }

    @Override
    public Activity profileActivity(User user) {
        return null;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return PrivateThreadsFragment.newInstance();
    }

    @Override
    public Fragment publicThreadsFragment() {
        return PublicThreadsFragment.newInstance();
    }

    @Override
    public Fragment contactsFragment() {
        return ContactsFragment.newInstance();
    }

    @Override
    public Fragment profileFragment(User user) {
        return ProfileFragment.newInstance(user);
    }

    @Override
    public Class getLoginActivity() {
        return LoginActivity.class;
    }

    @Override
    public Class getMainActivity() {
        return MainActivity.class;
    }

    @Override
    public Class getChatActivity() {
        return ChatActivity.class;
    }

    @Override
    public Class getThreadDetailsActivity() {
        return ThreadDetailsActivity.class;
    }

    @Override
    public Class getSelectContactActivity() {
        return SelectContactActivity.class;
    }

    @Override
    public Class getSearchActivity() {
        return SearchActivity.class;
    }

    @Override
    public Class getEditProfileActivity() {
        return EditProfileActivity.class;
    }

    @Override
    public Class getProfileActivity() {
        return ProfileActivity.class;
    }

    public void startActivity(Class activity){
        Intent intent = new Intent(AppContext.shared().context(), activity);
        startActivity(intent);
    }

    public void startActivity (Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppContext.shared().context().startActivity(intent);
    }

    public void startChatActivityForID(String threadEntityID) {
        Intent intent = new Intent(AppContext.shared().context(), getChatActivity());
        intent.putExtra(THREAD_ENTITY_ID, threadEntityID);
        startActivity(intent);
    }

    public void startLoginActivity(boolean attemptCachedLogin){
        Intent intent = new Intent(AppContext.shared().context(), getLoginActivity());
        intent.putExtra(ATTEMPT_CACHED_LOGIN, attemptCachedLogin);
        startActivity(intent);
    }

    public void startEditProfileActivity(String userEntityID){
        Intent intent = new Intent(AppContext.shared().context(), getEditProfileActivity());
        intent.putExtra(USER_ENTITY_ID, userEntityID);
        startActivity(intent);
    }

    public void startMainActivity () {
        startActivity(getMainActivity());
    }

    public void startSearchActivity () {
        startActivity(getSearchActivity());
    }

    public void startSelectContactsActivity() {
        startActivity(getSelectContactActivity());
    }

    public void startProfileActivity(String userEntityID) {
        Intent intent = new Intent(AppContext.shared().context(), getProfileActivity());
        intent.putExtra(USER_ENTITY_ID, userEntityID);
        startActivity(intent);
    }


}
