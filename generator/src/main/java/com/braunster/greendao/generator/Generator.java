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

    private static Entity user, linkedAccount, thread, message, userThreadLink, followerLink;

    public static void main(String args[]) throws Exception{
//        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(54,"com.braunster.chatsdk.dao");

        schema.enableKeepSectionsByDefault();

        addUser(schema);
        addMessages(schema);
        addThread(schema);
        addLinkedAccounts(schema);
        addContactLink(schema);
        addUserThreadLink(schema);
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

        return userId;
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

    private static Property addLinkedAccounts(Schema schema) {
        Property linkedAccountId;
        linkedAccount = schema.addEntity(EntityProperties.BLinkedAccount);
        linkedAccountId = linkedAccount.addIdProperty().getProperty();
        linkedAccount.addStringProperty(EntityProperties.Token);
        linkedAccount.addIntProperty(EntityProperties.Type);

        return linkedAccountId;
    }

    private static Property addContactLink(Schema schema) {
        Property contactLinkId;
        Entity contactLink = schema.addEntity(EntityProperties.ContactLink);
        contactLinkId = contactLink.addIdProperty().getProperty();
        setManyToMany(contactLink, user, user);

        contactLink.setSuperclass("Entity");

        return contactLinkId;
    }

    private static Property addUserThreadLink(Schema schema){
        Property userThreadLinkId;
        userThreadLink = schema.addEntity(EntityProperties.UserThreadLink);
        userThreadLinkId = userThreadLink.addIdProperty().primaryKey().getProperty();


        // Link data for user and thread.
        setManyToMany(userThreadLink, user, thread);

        return userThreadLinkId;
    }

    private static Property addFollower(Schema schema) {
        Property followerLinkId;
        followerLink = schema.addEntity(EntityProperties.FollowerLink);
        followerLinkId = followerLink.addIdProperty().primaryKey().getProperty();
        followerLink.addIntProperty(EntityProperties.Type); // leftover from follower

        // Link data for user and thread.
        setManyToMany(followerLink, user, user);

        return followerLinkId;
    }
    //endregion

    private static void setBidirectionalRelationships(){

            setBidirectionalToMany(thread, message, EntityProperties.Thread, EntityProperties.Messages);
            setBidirectionalToMany(user, linkedAccount, null, EntityProperties.BLinkedAccounts);
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
                    toManyPropertyName.substring(1) + "s";
        }

        toOne.setName(toOnePropertyName);
        toMany.setName(toManyPropertyName);
    }

    private static void setManyToMany(Entity linkEntity, Entity entityOne, Entity entityTwo){

        Property entityOneProp;
        Property entityTwoProp;
        String entityOneName = entityOne.getClassName();
        String entityTwoName = entityTwo.getClassName();

        // Distinguish entity links if they are to the same entity type
        if(entityOne.getClassName().equals(entityTwo.getClassName())) {
            entityTwoName = "linkOwner" + entityTwoName;
        }

        entityOneProp = linkEntity.addLongProperty(entityOneName + "DaoId").getProperty();
        entityTwoProp = linkEntity.addLongProperty(entityTwoName + "DaoId").getProperty();

        linkEntity.addToOne(entityOne, entityOneProp).setName(entityOneName);
        linkEntity.addToOne(entityTwo, entityTwoProp).setName(entityTwoName);

        String linkEntityName = linkEntity.getClassName();
        linkEntityName = linkEntityName.substring(0, 1).toLowerCase() +
                linkEntityName.substring(1) + "s";

        ToMany linkFromEntityOne = entityOne.addToMany(linkEntity, entityOneProp);
        linkFromEntityOne.setName(linkEntityName);

        // If the entities are not the same they each need a property
        if(!entityOne.getClassName().equals(entityTwo.getClassName())) {
            ToMany linkFromEntityTwo = entityTwo.addToMany(linkEntity, entityTwoProp);
            linkFromEntityTwo.setName(linkEntityName);
        }
    }

    private static void setSuperClass(){
        user.setSuperclass("BUserEntity");
        message.setSuperclass("BMessageEntity");
        thread.setSuperclass("BThreadEntity");
        linkedAccount.setSuperclass("BLinkedAccountEntity");
        userThreadLink.setSuperclass("Entity");
        followerLink.setSuperclass("FollowerLinkEntity");
    }
}
