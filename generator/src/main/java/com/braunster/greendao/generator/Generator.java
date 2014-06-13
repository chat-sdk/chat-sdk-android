package com.braunster.greendao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

/**
 * Created by itzik on 6/8/2014.
 */
public class Generator {

    // ASK Some variables is not set and for some im not sure of their type.
    private static String outputDir = "../sdk/src/main/java";

    private static Entity user, linkedAccount, threads, messages, linkedContact;

    public static void main(String args[]) throws Exception{
        Schema schema = new Schema(1000 , "com.braunster.dao");

        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMessage(schema);
        addThread(schema);
        addUser(schema);
        new DaoGenerator().generateAll(schema, outputDir);
    }

    private static void addUser(Schema schema) {
        user = schema.addEntity(EntityProperties.BUser);
        user.addIdProperty();
        user.addStringProperty(EntityProperties.AuthenticationID).notNull();
        user.addStringProperty(EntityProperties.EntityID).notNull();
        user.addStringProperty(EntityProperties.FacebookID).notNull();

        user.addBooleanProperty(EntityProperties.Dirty);
        user.addStringProperty(EntityProperties.Name);

        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);

//        user.addIntProperty(EntityProperties.PictureExists);
//        user.addStringProperty(EntityProperties.PictureURL);
        user.addIntProperty(EntityProperties.FontSize);
        user.addStringProperty(EntityProperties.FontName);
        user.addStringProperty(EntityProperties.TextColor);

        Property property = user.addStringProperty(EntityProperties.EntityID).getProperty();
        user.addToOne(linkedAccount, property);
    }


    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccount.addIdProperty();
        linkedAccount.addStringProperty(EntityProperties.EntityID).notNull();
        linkedAccount.addDateProperty(EntityProperties.AuthenticationID).notNull();

        ToMany
        // TODO need to add owner?
    }

    private static void addLinkedContact(Schema schema) {
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addIdProperty();
        linkedContact.addStringProperty(EntityProperties.EntityID).notNull();
        linkedContact.addDateProperty(EntityProperties.AuthenticationID).notNull();

        // TODO need to add owner?
    }

    private static void addMessage(Schema schema) {
        Entity message = schema.addEntity(EntityProperties.BMessage);
        message.addIdProperty();
        message.addStringProperty(EntityProperties.EntityID).notNull();
        message.addIntProperty(EntityProperties.Color);
        message.addDateProperty(EntityProperties.Date);
        message.addStringProperty(EntityProperties.FontName);
        message.addIntProperty(EntityProperties.FontSize);
        message.addDateProperty(EntityProperties.LastUpdated);
        /* Resource not sure what type*/
        message.addStringProperty(EntityProperties.ResourcesPath);
        message.addStringProperty(EntityProperties.Text);
        message.addIntProperty(EntityProperties.TextColor);
        message.addIntProperty(EntityProperties.Type);
        /*Thread not sure if actual thread or id*/
        /*User not sure if actual user or id*/
    }

    private static void addThread(Schema schema) {
        Entity thread = schema.addEntity(EntityProperties.BThread);
        thread.addIdProperty();
        thread.addStringProperty(EntityProperties.EntityID).notNull();
        thread.addDateProperty(EntityProperties.CreationDate).notNull();
        thread.addIntProperty(EntityProperties.HasUnreadMessaged);
        thread.addDateProperty(EntityProperties.LastUpdated);
        thread.addStringProperty(EntityProperties.Name);
        thread.addIntProperty(EntityProperties.Type);
        /* Messages Users and Creators*/
    }
}
