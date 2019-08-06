/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.dao;

public class Keys {

    /* Metadata */
    public static final String Email = "email";
    public static final String Key = "key";
    public static final String Value = "value";
    public static final String Phone = "phone";
    public static final String AvatarURL = "pictureURL";
    public static final String AvatarHash = "avatarHash";
    public static final String Availability = "availability";
    public static final String State = "state";
    public static final String PresenceSubscription = "presence-subscription";
    public static final String Time = "time";

    public static final String UserName = "userName";

    // TODO: This will be deprecated in favour of the from variable
    public static final String UserFirebaseId = "user-firebase-id";
    public static final String Color = "color";
    public static final String Name = "name";
    public static final String NameLowercase = "name-lowercase";
    public static final String InvitedBy = "invitedBy";
    public static final String CreationDate = "creation-date";
    public static final String JSON = "json_v2";
    public static final String To = "to";
    public static final String From = "from";

    // Deprecated in favour of type
    public static final String Type_v4 = "type_v4";
    public static final String Type = "type";
    public static final String Online = "online";
    public static final String Date = "date";
    public static final String LastOnline = "last-online";
    public static final String Meta = "meta";
    public static final String Threads = "threads";
    public static final String Users = "users";
    public static final String UID = "uid";
    public static final String Read = "read";

    public static final String Owner = "owner";
    public static final String Member = "member";

    public static final String ImageUrl = "image-url";

    // Deprecated in favour of Creator
    public static final String CreatorEntityId = "creator-entity-id";
    public static final String Creator = "creator";
    public static final String Deleted = "deleted";
    public static final String UserId = "user-id";
    public static final String MessageText = "text";
    public static final String MessageLongitude = "longitude";
    public static final String MessageLatitude = "latitude";
    public static final String MessageImageURL = "image-url";
    public static final String MessageImageWidth = "image-width";
    public static final String MessageImageHeight = "image-height";
    public static final String MessageVideoURL = "video-url";
    public static final String MessageAudioURL = "audio-url";
    public static final String MessageAudioLength = "audio-length";
    public static final String MessageStickerName = "sticker";
    public static final String MessageMimeType = "mime-type";
    public static final String MessageFileURL = "file-url";

    // CoreUser details
    public static final String Gender = "gender";
    public static final String CountryCode = "country-code";
    public static final String Location = "location";
    public static final String DateOfBirth = "date-of-birth";
    public static final String Status = "status";

    public static final String IntentKeyUserEntityID = "user-entity-id";
    public static final String IntentKeyUserEntityIDList = "user-entity-id-list";
    public static final String IntentKeyMessageEntityID = "message-entity-id";
    public static final String IntentKeyThreadEntityID = "thread-entity-id";
    public static final String IntentKeyAnimateExit = "animate-exit";
    public static final String IntentKeyMultiSelectEnabled = "multi-select-enabled";
    public static final String IntentKeyErrorMessage = "error-message";
    public static final String IntentKeyListPosSelectEnabled = "list-pos";

    // For push notifications
    public static final String PushKeyUserEntityID = "chat_sdk_user_entity_id";
    public static final String PushKeyThreadEntityID = "chat_sdk_thread_entity_id";
    public static final String PushKeyTitle = "chat_sdk_push_title";
    public static final String PushKeyBody = "chat_sdk_push_body";

}
