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

    private static String outputDir = "../sdk/src/main/java";

    private static Entity user, linkedAccount, threads, message, linkedContact, metaData;

    private static Property entityIDProp;

    public static void main(String args[]) throws Exception{
        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(6 , "com.braunster.chatsdk.dao");

        addUser(schema);
        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMetaData(schema);
        addMessages(schema);
        addThread(schema);

        setProperties();

        setKeepSection();

        new DaoGenerator().generateAll(schema, outputDir);
    }

    private static void addUser(Schema schema) {
        user = schema.addEntity(EntityProperties.BUser);
        entityIDProp = user.addStringProperty(EntityProperties.EntityID).primaryKey().getProperty();
        user.addStringProperty(EntityProperties.AuthenticationID).notNull();
        user.addStringProperty(EntityProperties.FacebookID).notNull();

        user.addBooleanProperty(EntityProperties.Dirty);
        user.addStringProperty(EntityProperties.Name);

        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);

        user.addIntProperty(EntityProperties.FontSize);
        user.addStringProperty(EntityProperties.FontName);
        user.addStringProperty(EntityProperties.TextColor);

        user.setSuperclass("Entity");
    }

    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccount.addStringProperty(EntityProperties.EntityID).primaryKey();
        linkedAccount.addStringProperty(EntityProperties.AuthenticationID).notNull();
        linkedAccount.setSuperclass("Entity");
    }

    private static void addLinkedContact(Schema schema) {
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addStringProperty(EntityProperties.EntityID).primaryKey();
        linkedContact.addStringProperty(EntityProperties.AuthenticationID).notNull();
        linkedContact.setSuperclass("Entity");
    }

    private static void addMetaData(Schema schema) {
        metaData = schema.addEntity(EntityProperties.BMetaData);
        metaData.addStringProperty(EntityProperties.AuthenticationID).notNull();
        metaData.addBooleanProperty(EntityProperties.Dirty);
        metaData.addStringProperty(EntityProperties.Type).notNull();
        metaData.addStringProperty(EntityProperties.Key).notNull();
        metaData.addStringProperty(EntityProperties.Value).notNull();
        metaData.setSuperclass("Entity");
    }

    private static void addThread(Schema schema) {
        threads = schema.addEntity(EntityProperties.BThread);
        threads.addStringProperty(EntityProperties.EntityID).primaryKey();
        threads.addDateProperty(EntityProperties.CreationDate);
        threads.addBooleanProperty(EntityProperties.Dirty);
        threads.addBooleanProperty(EntityProperties.HasUnreadMessaged);
        threads.addStringProperty(EntityProperties.Name);
        threads.addStringProperty(EntityProperties.Type);

        threads.setSuperclass("Entity");
    }

    private static void addMessages(Schema schema){
        message = schema.addEntity(EntityProperties.BMessage);
        message.addDateProperty(EntityProperties.Date);
        message.addStringProperty(EntityProperties.EntityID).primaryKey();
        message.addBooleanProperty(EntityProperties.Dirty);
        message.addStringProperty(EntityProperties.Resource);
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addStringProperty(EntityProperties.Type);

        message.setSuperclass("Entity");
    }

    private static void setProperties(){
        // LinkedContact, LinkedAccount and MetaData - START
        Property userProp = linkedContact.addStringProperty(EntityProperties.Owner).getProperty();
        linkedContact.addToOne(user, userProp);

        Property userProp2 = linkedAccount.addStringProperty(EntityProperties.User).getProperty();
        linkedAccount.addToOne(user, userProp2);

        Property userProp3 = metaData.addStringProperty(EntityProperties.Owner).getProperty();
        metaData.addToOne(user, userProp3);
//        LinkedContact, LinkedAccount and MetaData - END

        // Threads - START
        Property lastMessageProp = threads.addStringProperty(EntityProperties.LastMessageAdded+EntityProperties.EntityID).getProperty();
        ToOne lastMsgOne = threads.addToOne(message, lastMessageProp);
        lastMsgOne.setName(EntityProperties.LastMessageAdded);

        Property creatorProp = threads.addStringProperty(EntityProperties.Creator+EntityProperties.EntityID).getProperty();
        ToOne creatorOne  = threads.addToOne(user, creatorProp);
        creatorOne.setName(EntityProperties.Creator);

        Property messagesForThreadProp = threads.addStringProperty(EntityProperties.BMessage+EntityProperties.EntityID).getProperty();
        ToMany messagesProp = threads.addToMany(message, messagesForThreadProp);
        messagesProp.setName(EntityProperties.Messages);

        // Ask how to get users?From linked Contact?
        // Threads - END

        // Messages - START
//        ToMany threadForMessages = message.addToMany(threads, entityIDProp);
//        threadForMessages.setName(EntityProperties.Thread);
        // Messages - END
//
//        // Users - START
//        ToMany contacts = user.addToMany(linkedContact, userProp);
//        contacts.setName(EntityProperties.BLinkedContact);
//
//        ToMany accounts = user.addToMany(linkedAccount, userProp2);
//        accounts.setName(EntityProperties.BLinkedAccount);
//
//        ToMany metadata = user.addToMany(metaData, userProp3);
//        metadata.setName(EntityProperties.BMetaData);
//
//        ToMany messagesForUser = user.addToMany(message, userProp);
//        messagesForUser.setName(EntityProperties.Messages);
//
//        ToMany threadsForUser = user.addToMany(threads, userProp);
//        threadsForUser.setName(EntityProperties.Threads);
//
//        ToMany threadsCreatedForUser = user.addToMany(threads, userProp);
//        threadsCreatedForUser.setName(EntityProperties.ThreadsCreated);
//        // Users - END

    }

    private static void setKeepSection(){
        user.setHasKeepSections(true);
    }
}
