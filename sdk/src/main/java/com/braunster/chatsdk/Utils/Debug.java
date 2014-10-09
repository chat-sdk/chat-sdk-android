package com.braunster.chatsdk.Utils;

/**
 * Created by braunster on 10/08/14.
 */
public class Debug {

    /*Network*/
    public static final boolean BFirebaseInterface = false;
    public static final boolean AbstractNetworkAdapter = false;
    public static final boolean BFacebookManager = false;
    public static final boolean BNetworkManager = false;
    public static final boolean TwitterManager = false;
    public static final boolean EventManager = false;
    public static final boolean BPath = false;
    public static final boolean BFirebaseNetworkAdapter = false;

    /*Fragments*/
    private static final boolean FRAGMENTS = false;
    public static final boolean ContactsFragment = FRAGMENTS || false;
    public static final boolean ConversationsFragment = FRAGMENTS || true;
    public static final boolean ProfileFragment = FRAGMENTS || true;
    public static final boolean ThreadsFragment = FRAGMENTS || false;
    public static final boolean ExpandableContactsFragment = FRAGMENTS || false;

    /*DaoCore*/
    public static final boolean DaoCore = false;
    public static final boolean BMessage = false;
    public static final boolean BUser = false;
    public static final boolean BThread = false;

    /*Adapters*/
    public static final boolean FBFriendsListVolleyAdapter = false;
    public static final boolean MessagesListAdapter = false;
    public static final boolean ThreadsListAdapter = false;
    public static final boolean UsersWithStatusListAdapter = false;

    /*Activities*/
    public static final boolean BaseActivity = false;
    public static final boolean ChatActivity = false;
    public static final boolean LocationActivity = false;
    public static final boolean LoginActivity = false;
    public static final boolean MainActivity = false;
    public static final boolean PickFriendsActivity = false;
    public static final boolean SearchActivity = false;
    public static final boolean ShareWithContactsActivity = false;

    /*Utils*/
    public static final boolean ImageUtils = false;
    public static final boolean UiUtils = false;

    /*Views*/
    public static final boolean ChatBubbleImageView = false;
    public static final boolean ChatBubbleTextView = false;

    /*Firebase - Listeners */
    public static final boolean IncomingMessagesListener = true;
    public static final boolean ThreadDetailsChangeListener = false;
    public static final boolean UserAddedToThreadListener = false;
    public static final boolean UserDetailsChangeListener = false;
    public static final boolean FollowerAddedListener = true;
    public static final boolean UserToFollowAddedListener = true;
}
