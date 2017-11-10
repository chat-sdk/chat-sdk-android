/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.defines;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 *
 * This is the only trace in the core SDK for firebase, Needed for some stuff that cannot be removed from the core.
 *
 */
// TODO: Move this to the Firebase module
public class FirebaseDefines {

    // How many historic messages should we load this will
    // load the messages that were sent in the last x seconds
    public static final int NumberOfMessagesPerBatch = 30;

    public static final int NumberOfUserToLoadForIndex = 20;

}
