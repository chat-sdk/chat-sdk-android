/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.core.dao;

public class Keys {

    /* Metadata */
    public static final String Email = "email";
    public static final String Phone = "phone";
    public static final String AvatarURL = "pictureURL";
    public static final String HeaderURL = "header-url";
    public static final String AvatarHash = "avatarHash";
    public static final String Availability = "availability";
    public static final String State = "state";
    public static final String PresenceSubscription = "presence-subscription";

    public static final String Name = "name";
    public static final String NameLowercase = "name-lowercase";
    public static final String InvitedBy = "invitedBy";
    public static final String CreationDate = "creation-date";
    public static final String To = "to";
    public static final String From = "from";
    public static final String Subject = "subject";
    public static final String Weight = "weight";

    // Deprecated in favour of type
    public static final String Type = "type";
    public static final String Date = "date";
    public static final String LastOnline = "last-online";
    public static final String Meta = "meta";
    public static final String Users = "users";
    public static final String UID = "uid";
    public static final String Read = "read";
    public static final String Reply = "reply";

    public static final String Owner = "owner";
    public static final String Member = "member";

    public static final String ImageUrl = "image-url";

    public static final String Mute = "mute";

    // Deprecated in favour of Creator
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
    public static final String Location = "location";
    public static final String Status = "status";
    public static final String Permission = "permission";

    public static final String Id = "id";
    public static final String IntentKeyUserEntityID = "user-entity-id";
    public static final String IntentKeyUserEntityIDList = "user-entity-id-list";
    public static final String IntentKeyMessageEntityIDs = "text-entity-ids";
    public static final String IntentKeyThreadEntityID = "thread-entity-id";
    public static final String IntentKeyErrorMessage = "error-text";
    public static final String IntentKeyFilePath = "file-path";

    // For push notifications
    public static final String PushKeyUserEntityID = "chat_sdk_user_entity_id";
    public static final String PushKeyThreadEntityID = "chat_sdk_thread_entity_id";
    public static final String PushKeyTitle = "chat_sdk_push_title";
    public static final String PushKeyBody = "chat_sdk_push_body";

    public static final String CurrentUserID = "current_user_entity_id";

    //Admin Thread
    public static final String ReadOnly = "read-only";

}
