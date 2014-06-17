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
    private static String outputDir = "../sdk/src/main/java";

    private static Entity user, linkedAccount, thread, message, threadUsers, linkedContact, metaData;

    public static void main(String args[]) throws Exception{
//        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(16, "com.braunster.chatsdk.dao");

        schema.enableKeepSectionsByDefault();

        addUser(schema);
        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMetaData(schema);
        addMessages(schema);
        addThread(schema);
        addThreadUsers(schema);

        setProperties();



//        setImplementation();
        setSuperClass();

        new DaoGenerator().generateAll(schema, outputDir);
    }

    //region Add Objects
    private static void addUser(Schema schema) {
        user = schema.addEntity(EntityProperties.BUser);
        user.addStringProperty(EntityProperties.EntityID).primaryKey().getProperty();
        user.addStringProperty(EntityProperties.AuthenticationID);
        user.addStringProperty(EntityProperties.FacebookID);

        user.addBooleanProperty(EntityProperties.Dirty);
        user.addStringProperty(EntityProperties.Name);

        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);
        user.addBooleanProperty(EntityProperties.Online);

        user.addIntProperty(EntityProperties.FontSize);
        user.addStringProperty(EntityProperties.FontName);
        user.addStringProperty(EntityProperties.TextColor);
    }

    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccount.addStringProperty(EntityProperties.EntityID).primaryKey();
        linkedAccount.addStringProperty(EntityProperties.AuthenticationID);
    }

    private static void addLinkedContact(Schema schema) {
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addIdProperty();
        linkedContact.addStringProperty(EntityProperties.EntityID);
        linkedContact.addStringProperty(EntityProperties.AuthenticationID);
    }

    private static void addMetaData(Schema schema) {
        metaData = schema.addEntity(EntityProperties.BMetaData);
        metaData.addStringProperty(EntityProperties.AuthenticationID);
        metaData.addBooleanProperty(EntityProperties.Dirty);
        metaData.addStringProperty(EntityProperties.Type).notNull();
        metaData.addStringProperty(EntityProperties.Key).notNull();
        metaData.addStringProperty(EntityProperties.Value).notNull();
    }

    private static void addThread(Schema schema) {
        thread = schema.addEntity(EntityProperties.BThread);
        thread.addStringProperty(EntityProperties.EntityID).primaryKey();
        thread.addDateProperty(EntityProperties.CreationDate);
        thread.addBooleanProperty(EntityProperties.Dirty);
        thread.addBooleanProperty(EntityProperties.HasUnreadMessaged);
        thread.addStringProperty(EntityProperties.Name);
        thread.addIntProperty(EntityProperties.Type);
    }

    private static void addThreadUsers(Schema schema){
        threadUsers = schema.addEntity(EntityProperties.BLinkData);
        threadUsers.addIdProperty().primaryKey();
    }

    private static void addMessages(Schema schema){
        message = schema.addEntity(EntityProperties.BMessage);
        message.addDateProperty(EntityProperties.Date);
        message.addStringProperty(EntityProperties.EntityID).primaryKey();
        message.addBooleanProperty(EntityProperties.Dirty);
        message.addStringProperty(EntityProperties.Resource);
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addIntProperty(EntityProperties.Type);
    }
    //endregion

    private static void setProperties(){
        // LinkedContact, LinkedAccount and MetaData - START
        Property userProp = linkedContact.addStringProperty(EntityProperties.Owner).getProperty();
        ToOne toOneUserProp = linkedContact.addToOne(user, userProp);
        toOneUserProp.setName("Contact");

        Property userProp2 = linkedAccount.addStringProperty(EntityProperties.User).getProperty();
        linkedAccount.addToOne(user, userProp2);

        Property userProp3 = metaData.addStringProperty(EntityProperties.Owner).getProperty();
        metaData.addToOne(user, userProp3);

        // Add a thread owner to the message
        Property threadIDProp = message.addStringProperty("OwnerThread").getProperty();
        ToOne one1 = message.addToOne(thread, threadIDProp);
        one1.setName("BThreadOwner");

        // The sender ID
        Property senderIDProp = message.addStringProperty("Sender").getProperty();
        ToOne one = message.addToOne(user, senderIDProp);
        one.setName("BUserSender");

        // Link data for user and thread.
        Property userIdProp = threadUsers.addStringProperty("UserID").getProperty();
        Property threadIdProp = threadUsers.addStringProperty("ThreadID").getProperty();
        threadUsers.addToOne(user, userIdProp);
        threadUsers.addToOne(thread, threadIdProp);
        //LinkedContact, LinkedAccount and MetaData - END

        // Threads - START
        Property creatorProp = thread.addStringProperty(EntityProperties.Creator).getProperty();
        thread.addToOne(user, creatorProp);

        ToMany messagesProp = thread.addToMany(message, threadIDProp);
        messagesProp.setName(EntityProperties.Messages);

        ToMany linkToUsers = thread.addToMany(threadUsers, threadIdProp);
        linkToUsers.setName(EntityProperties.Users);
        // Threads - END

//      // Users - START
        ToMany contacts = user.addToMany(linkedContact, userProp);
        contacts.setName(EntityProperties.BLinkedContact);

        ToMany accounts = user.addToMany(linkedAccount, userProp2);
        accounts.setName(EntityProperties.BLinkedAccount);

        ToMany metadata = user.addToMany(metaData, userProp3);
        metadata.setName(EntityProperties.BMetaData);

        ToMany messagesForUser = user.addToMany(message, senderIDProp);
        messagesForUser.setName(EntityProperties.Messages);

        ToMany threadsCreatedForUser = user.addToMany(thread, creatorProp);
        threadsCreatedForUser.setName(EntityProperties.ThreadsCreated);

        ToMany linkToThread = user.addToMany(threadUsers, userIdProp);
        linkToThread.setName(EntityProperties.Threads);
//        // Users - END

    }

    private static void setKeepSection(){
        user.setHasKeepSections(true);
    }

    private static void setImplementation(){
        user.implementsInterface("Entity<BUser>");
        message.implementsInterface("Entity<BMessage>");
        thread.implementsInterface("Entity<BThread>");
        linkedAccount.implementsInterface("Entity<BLinkedAccount>");
        linkedContact.implementsInterface("Entity<BLinkedContact>");
        metaData.implementsInterface("Entity<BMetadata>");
        threadUsers.implementsInterface("Entity<BLinkData>");
    }

    private static void setSuperClass(){
        user.setSuperclass("Entity<BUser>");
        message.setSuperclass("Entity<BMessage>");
        thread.setSuperclass("Entity<BThread>");
        linkedAccount.setSuperclass("Entity<BLinkedAccount>");
        linkedContact.setSuperclass("Entity<BLinkedContact>");
        metaData.setSuperclass("Entity<BMetadata>");
        threadUsers.setSuperclass("Entity<BLinkData>");
    }
}
