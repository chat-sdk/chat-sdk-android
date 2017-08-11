/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.defines;

@Deprecated
public class Debug {

    /*Network*/
    public static final boolean FacebookManager = false;
    public static final boolean TwitterManager = true;

    public static final boolean FirebaseNetworkAdapter = true;
    public static final boolean FirebaseUploadHandler = true;
    public static final boolean BackendlessPushHandler = true;
    public static final boolean StateManager = true;

    public static final boolean FirebaseAuthenticationHandler = true;
    public static final boolean AbstractAuthenticationHandler = true;


    /*Fragments*/
    private static final boolean FRAGMENTS = false;
    public static final boolean ContactsFragment = FRAGMENTS || false;
    public static final boolean ConversationsFragment = FRAGMENTS || false;
    public static final boolean ProfileFragment = FRAGMENTS || false;
    public static final boolean ThreadsFragment = FRAGMENTS || false;

    /*DaoCore*/
    public static final boolean DaoCore = false;
    public static final boolean Message = false;
    public static final boolean User = false;
    public static final boolean Thread = false;

    /*Adapters*/
    public static final boolean MessagesListAdapter = false;
    public static final boolean ThreadsListAdapter = false;
    public static final boolean UsersWithStatusListAdapter = false;

    /*Activities*/
    public static final boolean BaseActivity = false;
    public static final boolean ChatActivity = false;
    public static final boolean LoginActivity = true;
    public static final boolean MainActivity = false;
    public static final boolean PickFriendsActivity = false;
    public static final boolean ShareWithContactsActivity = false;
    public static final boolean ThreadDetailsActivity = false;

    /*Utils*/
    public static final boolean ImageUtils = false;
    public static final boolean UiUtils = false;
    public static final boolean DialogUtils = false;

    /*Views*/
    public static final boolean ChatMessageBoxView = false;


    
    /*Receiver*/
    public static final boolean ChatSDKReceiver = true;



    public static final boolean NotificationUtils = true;
}
