/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.dao;

public class DaoDefines {

    /**
     * Currently there is no reason to disable following but maybe in the future it would be needed to keep track of it.
     * */
    public static boolean EnableFollowers = true;

    public static final class Defaults {
        public static final String BubbleDefaultColorMe = "#abcff4";
        public static final String BubbleDefaultColorReply = "#d7d4d3";
        public static final int SDKExitMode = Exit.EXIT_MODE_DOUBLE_BACK;
    }

    public static final class Exit {
        public static final int EXIT_MODE_NONE = 1990;
        public static final int EXIT_MODE_DOUBLE_BACK = 1991;
        public static final int EXIT_MODE_DIALOG = 1992;

        public static final int DOUBLE_CLICK_INTERVAL = 2000;
    }


    public static final class Keys{
        /*Metadata*/
        public static final String Email = "email";
        public static final String Key = "key";
        public static final String Value = "value";
        public static final String Phone = "phone";
        public static final String AvatarURL = "pictureURL";
        public static final String AvatarHash = "avatarHash";
        public static final String AvatarThumbnailURL = "pictureURLThumbnail";
        public static final String Availability = "availability";
        public static final String State = "state";

        public static final String LastMessageAdded = "last-message-added";
        public static final String UserFirebaseId = "user-firebase-id";
        public static final String Color = "color";
        public static final String Name = "name";
        public static final String Null = "null";
        public static final String CreationDate = "creation-date";
        public static final String Payload = "payload";
        public static final String JSON = "JSON";
        public static final String Type = "type";
        public static final String Type_v4 = "type_v4";
        public static final String Online = "online";
        public static final String Date = "date";
        public static final String LastOnline = "last-online";
        public static final String Version = "version";
        public static final String Meta = "meta";
        public static final String Threads = "threads";
        public static final String Users = "users";

        public static final String ImageUrl = "image-url";
        public static final String CreatorEntityId = "creator-entity-id";
        public static final String Deleted = "deleted";
        public static final String Leaved = "leaved";
        public static final String UserId = "user-id";
        public static final String MessageText = "text";
        public static final String MessageLongitude = "longitude";
        public static final String MessageLatitude = "latitude";
        public static final String MessageImageURL = "image-url";
        public static final String MessageThumbnailURL = "thumbnail-url";
        public static final String MessageImageWidth = "image-width";
        public static final String MessageImageHeight = "image-height";
        public static final String MessageVideoURL = "video-url";
        public static final String MessageAudioURL = "audio-url";
        public static final String MessageAudioLength = "audio-length";

        // For pushes
        public static final String ACTION = "action";
        public static final String ALERT = "alert";
        public static final String BADGE = "badge", INCREMENT = "Increment";
        public static final String CONTENT = "text";
        public static final String MESSAGE_ENTITY_ID = "message_entity_id";
        public static final String THREAD_ENTITY_ID = "thread_entity_id";
        public static final String MESSAGE_DATE ="message_date";
        public static final String MESSAGE_SENDER_ENTITY_ID ="message_sender_entity_id";
        public static final String MESSAGE_SENDER_NAME ="message_sender_name";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String MESSAGE_PAYLOAD= "message_payload";

        public static final String SOUND = "sound";
        public static final String Default = "default";

        public static final String DeviceType = "deviceType";
        public static final String iOS = "ios";
        public static final String Android = "android";

        public static final String Channels = "channels";
        public static final String Channel = "channel";

        // CoreUser details
        public static final String Gender = "gender";
        public static final String CountryCode = "country-code";
        public static final String Location = "location";
        public static final String DateOfBirth = "date-of-birth";
        public static final String Status = "status";

        public static final class ThirdPartyData{
            public static final String ID = "id";
            public static final String Name = "name";
            public static final String ImageURL = "profile_image_url";
            public static final String AccessToken = "accessToken";
            public static final String DisplayName = "displayName";
            public static final String EMail = "email";
        }
    }



}
