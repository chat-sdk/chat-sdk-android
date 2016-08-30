package com.braunster.greendao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.ToOne;

/**
 * Created by itzik on 6/8/2014.
 */
public class Generator {

    // TODO set not null attribute to the properties that needs it.
    private static String outputDir = "sdk/src/main/java";

    private static Entity user, linkedAccount, thread, message, threadUsers, linkedContact, follower;

    public static void main(String args[]) throws Exception{
//        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(54,"com.braunster.chatsdk.dao");

        schema.enableKeepSectionsByDefault();

        addUser(schema);
        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMessages(schema);
        addThread(schema);
        addThreadUsers(schema);
        addFollower(schema);

        setProperties();

//        setImplementation();
        setSuperClass();

        new DaoGenerator().generateAll(schema, outputDir);
    }

    //region Add Objects
    private static void addUser(Schema schema) {
        user = schema.addEntity(EntityProperties.BUser);
        user.addIdProperty();
        user.addStringProperty(EntityProperties.EntityID);
        user.addIntProperty(EntityProperties.AuthenticationType);
        user.addStringProperty(EntityProperties.MessageColor);
        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);
        user.addBooleanProperty(EntityProperties.Online);
        user.addStringProperty(EntityProperties.MetaData).columnName(EntityProperties.MetaData.toLowerCase());
    }

    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccount.addIdProperty();
        linkedAccount.addStringProperty(EntityProperties.Token);
        linkedAccount.addIntProperty(EntityProperties.Type);
    }

    private static void addLinkedContact(Schema schema) {
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addIdProperty();
        linkedContact.addStringProperty(EntityProperties.EntityID);
    }

    private static void addFollower(Schema schema) {
        follower = schema.addEntity(EntityProperties.BFollower);
        follower.addIdProperty();
        follower.addStringProperty(EntityProperties.EntityID);
        follower.addIntProperty(EntityProperties.Type);
        
        Property followerPropOwner = follower.addLongProperty(EntityProperties.OwnerId).getProperty();
        ToOne toOneUserPropOwner = follower.addToOne(user, followerPropOwner);
        toOneUserPropOwner.setName(EntityProperties.Owner);

        Property followerPropUser = follower.addLongProperty(EntityProperties.BUserId).getProperty();
        ToOne toOneUserPropUser = follower.addToOne(user, followerPropUser);
        toOneUserPropUser.setName(EntityProperties.User);
    }

    private static void addThread(Schema schema) {
        thread = schema.addEntity(EntityProperties.BThread);
        thread.addIdProperty();
        thread.addStringProperty(EntityProperties.EntityID);
        thread.addDateProperty(EntityProperties.CreationDate);
        thread.addBooleanProperty(EntityProperties.HasUnreadMessaged);
        thread.addBooleanProperty(EntityProperties.BDeleted);
        thread.addStringProperty(EntityProperties.Name);
        thread.addDateProperty(EntityProperties.LastMessageAdded);
        thread.addIntProperty(EntityProperties.Type);
        thread.addStringProperty(EntityProperties.CreatorEntityID);
        thread.addStringProperty(EntityProperties.BThreadImageUrl);
        thread.addStringProperty(EntityProperties.RootKey).columnName(EntityProperties.C_RootKey);
        thread.addStringProperty(EntityProperties.ApiKey).columnName(EntityProperties.C_ApiKey);
    }

    private static void addThreadUsers(Schema schema){
        threadUsers = schema.addEntity(EntityProperties.BLinkData);
        threadUsers.addIdProperty().primaryKey();
    }

    private static void addMessages(Schema schema){
        message = schema.addEntity(EntityProperties.BMessage);
        message.addIdProperty();
        message.addStringProperty(EntityProperties.EntityID);
        message.addDateProperty(EntityProperties.Date);
        message.addBooleanProperty(EntityProperties.isRead);
        message.addStringProperty(EntityProperties.Resource);
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addStringProperty(EntityProperties.ImageDimensions);
        message.addIntProperty(EntityProperties.Type);
        message.addIntProperty(EntityProperties.Status);
        message.addIntProperty(EntityProperties.Delivered);
    }
    //endregion

    private static void setProperties(){


        // LinkedContact, LinkedAccount and MetaData - START
        Property linkedContactPropUser = linkedContact.addLongProperty(EntityProperties.Owner).getProperty();
        ToOne linkedContactToOneUser = linkedContact.addToOne(user, linkedContactPropUser);
        linkedContactToOneUser.setName("Contact");

        Property linkedAccountPropUser2 = linkedAccount.addLongProperty(EntityProperties.User).getProperty();
        linkedAccount.addToOne(user, linkedAccountPropUser2);

        {
            // Add a thread owner to the message
            Property messagePropOwnerThread = message.addLongProperty("OwnerThread").getProperty();
            ToOne messageToOneOwnerThread = message.addToOne(thread, messagePropOwnerThread);
            messageToOneOwnerThread.setName("BThreadOwner");

            // The sender ID
            Property messagePropSender = message.addLongProperty("Sender").getProperty();
            ToOne messageToOneSender = message.addToOne(user, messagePropSender);
            messageToOneSender.setName("BUserSender");

            ToMany threadPropMessages = thread.addToMany(message, messagePropOwnerThread);
            threadPropMessages.setName(EntityProperties.Messages);
        }
        {
            // Link data for user and thread.
            Property threadUsersPropUserId = threadUsers.addLongProperty("UserID").getProperty();
            Property threadUsersPropThreadId = threadUsers.addLongProperty("ThreadID").getProperty();
            threadUsers.addToOne(user, threadUsersPropUserId);
            threadUsers.addToOne(thread, threadUsersPropThreadId);

            ToMany linkToThread = user.addToMany(threadUsers, threadUsersPropUserId);
            linkToThread.setName(EntityProperties.BLinkData);

            ToMany threadPropUsers = thread.addToMany(threadUsers, threadUsersPropThreadId);
            threadPropUsers.setName(EntityProperties.BLinkData);
            //LinkedContact, LinkedAccount and MetaData - END
        }
        // Threads - START
        {
            Property threadPropCreator = thread.addLongProperty(EntityProperties.CreatorID).getProperty();
            ToOne threadToOneCreator = thread.addToOne(user, threadPropCreator);
            threadToOneCreator.setName(EntityProperties.Creator);




        }
        // Threads - END
        {
//      // Users - START
            ToMany contacts = user.addToMany(linkedContact, linkedContactPropUser);
            contacts.setName(EntityProperties.BLinkedContacts);

            ToMany followers = user.addToMany(follower, linkedContactPropUser);
            followers.setName(EntityProperties.BFollowers);

            ToMany accounts = user.addToMany(linkedAccount, linkedAccountPropUser2);
            accounts.setName(EntityProperties.BLinkedAccounts);

//        // Users - END
        }
    }

    private static void setKeepSection(){
        user.setHasKeepSections(true);
    }

    private static void setSuperClass(){
        user.setSuperclass("BUserEntity");
        message.setSuperclass("BMessageEntity");
        thread.setSuperclass("BThreadEntity");
        linkedAccount.setSuperclass("BLinkedAccountEntity");
        linkedContact.setSuperclass("Entity");
        threadUsers.setSuperclass("Entity");
        follower.setSuperclass("BFollowerEntity");
    }
}
