package com.braunster.greendao.generator;

/**
 * Created by itzik on 6/8/2014.
 */
public class EntityProperties {

    /* Entities*/
    public static final String BUser = "BUser";
    public static final String BMessage = "BMessage";
    public static final String BThread = "BThread";
    public static final String BMetaData = "BMetadata";

    /* Link Entities */

    // Connection to accounts
    public static final String BLinkedAccount = "BLinkedAccount";
    // Connection between Users and Threads
    public static final String UserThreadLink = "UserThreadLink";
    // Linkage entity that contains information about the contact connection
    public static final String ContactLink = "ContactLink";
    public static final String FollowerLink = "FollowerLink";

    /* General*/

    public static final String EntityID = "entityID";
    public static final String Name = "name";
    public static final String Type = "type";
    public static final String LastUpdated = "lastUpdated";
    public static final String Messages = "messages";

    /* User*/
    public static final String Dirty = "dirty";
    public static final String LastOnline = "lastOnline";
    public static final String Online = "Online";
    public static final String AuthenticationType = "AuthenticationType";
    public static final String MessageColor = "messageColor";
    public static final String BLinkedAccounts = "BLinkedAccounts";

    /* Message*/
    public static final String Date = "date";
    public static final String Resource= "resources";
    public static final String ResourcePath = "resourcesPath";
    public static final String Text = "text";
    public static final String ImageDimensions= "imageDimensions";
    public static final String Status = "status";
    public static final String isRead = "isRead";
    public static final String Delivered = "delivered";
    public static final String Thread = "thread";

    /* Thread */
    public static final String CreationDate = "creationDate";
    public static final String HasUnreadMessaged = "hasUnreadMessages";
    public static final String LastMessageAdded = "LastMessageAdded";
    public static final String Creator = "creator";
    public static final String CreatorID = "creator_ID";
    public static final String CreatorEntityID = "creatorEntityId";
    public static final String BDeleted = "deleted";
    public static final String BThreadImageUrl = "imageUrl";
    public static final String RootKey= "rootKey";
    public static final String ApiKey = "apiKey";
    public static final String C_RootKey= "root_key";
    public static final String C_ApiKey = "api_key";

    /* Metadata*/
    public static final String MetaData = "Metadata";

    /*Contact*/

    /*Account*/
    public static final String Token = "Token";




}
