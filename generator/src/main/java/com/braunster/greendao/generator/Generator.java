package com.braunster.greendao.generator;


import java.util.Iterator;

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

        setBidirectionalRelationships();

//        setImplementation();
        setSuperClass();

        new DaoGenerator().generateAll(schema, outputDir);
    }

    //region Add Objects
    private static Property addUser(Schema schema) {
        Property userId;
        user = schema.addEntity(EntityProperties.BUser);
        user.addIdProperty();
        userId = user.addStringProperty(EntityProperties.EntityID).getProperty();
        user.addIntProperty(EntityProperties.AuthenticationType);
        user.addStringProperty(EntityProperties.MessageColor);
        user.addDateProperty(EntityProperties.LastOnline);
        user.addDateProperty(EntityProperties.LastUpdated);
        user.addBooleanProperty(EntityProperties.Online);
        user.addStringProperty(EntityProperties.MetaData).columnName(EntityProperties.MetaData.toLowerCase());

        Property userPropContacts =  user.addLongProperty(EntityProperties.Contacts + "DaoId")
                .getProperty();
        ToMany contacts = user.addToMany(user, userPropContacts);
        contacts.setName(EntityProperties.Contacts + "List");

        return userId;
    }

    private static Property addLinkedAccounts(Schema schema) {
        Property linkedAccountId;
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccountId = linkedAccount.addIdProperty().getProperty();
        linkedAccount.addStringProperty(EntityProperties.Token);
        linkedAccount.addIntProperty(EntityProperties.Type);

        return linkedAccountId;
    }

    private static Property addLinkedContact(Schema schema) {
        Property linkedContactId;
        linkedContact = schema.addEntity(EntityProperties.BLinkedContact);
        linkedContact.addIdProperty();
        linkedContactId = linkedContact.addStringProperty(EntityProperties.EntityID).getProperty();

        return linkedContactId;
    }

    private static Property addFollower(Schema schema) {
        Property followerId;
        follower = schema.addEntity(EntityProperties.BFollower);
        follower.addIdProperty();
        followerId = follower.addStringProperty(EntityProperties.EntityID).getProperty();
        follower.addIntProperty(EntityProperties.Type);
        
        Property followerPropOwner = follower.addLongProperty(EntityProperties.OwnerId).getProperty();
        ToOne toOneUserPropOwner = follower.addToOne(user, followerPropOwner);
        toOneUserPropOwner.setName(EntityProperties.Owner);

        Property followerPropUser = follower.addLongProperty(EntityProperties.BUserId).getProperty();
        ToOne toOneUserPropUser = follower.addToOne(user, followerPropUser);
        toOneUserPropUser.setName(EntityProperties.User);
        return followerId;
    }

    private static Property addThread(Schema schema) {
        Property threadId;

        thread = schema.addEntity(EntityProperties.BThread);
        thread.addIdProperty();
        threadId = thread.addStringProperty(EntityProperties.EntityID).getProperty();
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

        Property threadPropCreator = thread.addLongProperty(EntityProperties.CreatorID).getProperty();
        ToOne threadToOneCreator = thread.addToOne(user, threadPropCreator);
        threadToOneCreator.setName(EntityProperties.Creator);

        return threadId;
    }

    private static Property addThreadUsers(Schema schema){
        Property threadUsersId;
        threadUsers = schema.addEntity(EntityProperties.BLinkData);
        threadUsersId = threadUsers.addIdProperty().primaryKey().getProperty();

        return threadUsersId;
    }

    private static Property addMessages(Schema schema){
        Property messageId;
        message = schema.addEntity(EntityProperties.BMessage);
        message.addIdProperty();
        messageId = message.addStringProperty(EntityProperties.EntityID).getProperty();
        message.addDateProperty(EntityProperties.Date);
        message.addBooleanProperty(EntityProperties.isRead);
        message.addStringProperty(EntityProperties.Resource);
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addStringProperty(EntityProperties.ImageDimensions);
        message.addIntProperty(EntityProperties.Type);
        message.addIntProperty(EntityProperties.Status);
        message.addIntProperty(EntityProperties.Delivered);

        // The sender ID
        Property messagePropSender = message.addLongProperty("Sender").getProperty();
        ToOne messageToOneSender = message.addToOne(user, messagePropSender);
        messageToOneSender.setName("BUserSender");

        return messageId;
    }
    //endregion

    private static void setBidirectionalRelationships(){

        {
            setBidirectionalToMany(thread, message, EntityProperties.Thread, EntityProperties.Messages);
            setBidirectionalToMany(user, linkedAccount, null, EntityProperties.BLinkedAccounts);
            // Link data for user and thread.
            setManyToMany(threadUsers, user, thread);
        }
        {
            // TODO: replace linkedContact and follower with BUser if possible -- Kyle
            // LinkedContact, LinkedAccount and MetaData - START
            Property linkedContactPropUser = linkedContact.addLongProperty(EntityProperties.Owner).getProperty();
            ToOne linkedContactToOneUser = linkedContact.addToOne(user, linkedContactPropUser);
            linkedContactToOneUser.setName("Contact");

            ToMany followers = user.addToMany(follower, linkedContactPropUser);
            followers.setName(EntityProperties.BFollowers);
        }
    }

    /**
     * Adds a toMany relationship to oneEntity, and a toOne relationship to manyEntity.
     * The property names will be the names of the respective entities, and the Ids will have the
     * suffix of "DaoId"
     *
     * @param oneEntity entity that only will be linking to manyEntities
     * @param manyEntity entity that will be linking to oneEntity
     */
    private static void setBidirectionalToMany(Entity oneEntity, Entity manyEntity ){
        setBidirectionalToMany(oneEntity, manyEntity, null, null);
    }
    /**
     * Adds a toMany relationship to oneEntity, and a toOne relationship to manyEntity.
     * The property names will be the names specified, and the Ids will be the names of the
     * respective entities with the suffix of "DaoId"
     *
     *
     * @param oneEntity entity that only will be linking to manyEntities
     * @param manyEntity entity that will be linking to oneEntity
     * @param toOnePropertyName to specify the property name, otherwise null works
     * @param toManyPropertyName to specify the property name, otherwise null works
     *
     */
    private static void setBidirectionalToMany(Entity oneEntity, Entity manyEntity,
                                               String toOnePropertyName,
                                               String toManyPropertyName){
        Property manyIdProp;
        if (toOnePropertyName == null) {
            manyIdProp = manyEntity.addLongProperty(oneEntity.getClassName() + "DaoId").getProperty();
        }else{
            manyIdProp = manyEntity.addLongProperty(toOnePropertyName + "DaoId").getProperty();
        }
        ToOne toOne = manyEntity.addToOne(oneEntity, manyIdProp);
        ToMany toMany = oneEntity.addToMany(manyEntity, manyIdProp);

        if(toOnePropertyName == null){
            toOnePropertyName = oneEntity.getClassName();
            toOnePropertyName = toOnePropertyName.substring(0, 1).toLowerCase() +
                    toOnePropertyName.substring(1);
        }

        if(toManyPropertyName == null){
            toManyPropertyName = oneEntity.getClassName();
            toManyPropertyName = toManyPropertyName.substring(0, 1).toLowerCase() +
                    toManyPropertyName.substring(1) + "List";
        }

        toOne.setName(toOnePropertyName);
        toMany.setName(toManyPropertyName);
    }

    private static void setManyToMany(Entity linkEntity, Entity entityOne, Entity entityTwo){
        Property entityOneProp = linkEntity.addLongProperty(entityOne.getClassName()+"DaoId").getProperty();
        Property entityTwoProp = linkEntity.addLongProperty(entityTwo.getClassName()+"DaoId").getProperty();
        linkEntity.addToOne(entityOne, entityOneProp);
        linkEntity.addToOne(entityTwo, entityTwoProp);


        String linkEntityName = linkEntity.getClassName();
        linkEntityName = linkEntityName.substring(0, 1).toLowerCase() +
                linkEntityName.substring(1) + "List";

        ToMany linkFromEntityOne = entityOne.addToMany(linkEntity, entityOneProp);
        linkFromEntityOne.setName(linkEntityName);

        ToMany linkFromEntityTwo = entityTwo.addToMany(linkEntity, entityTwoProp);
        linkFromEntityTwo.setName(linkEntityName);
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
