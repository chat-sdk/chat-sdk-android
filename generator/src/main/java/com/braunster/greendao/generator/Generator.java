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

    private static Entity user, linkedAccount, thread, message, threadUsers, userConnection, installation;

    public static void main(String args[]) throws Exception{
//        System.out.print("Generating... " + args[0].toString());
        Schema schema = new Schema(52,"com.braunster.chatsdk.dao");

        schema.enableKeepSectionsByDefault();

        addUser(schema);
        addLinkedAccounts(schema);
        addLinkedContact(schema);
        addMessages(schema);
        addThread(schema);
        addThreadUsers(schema);
        addInstallation(schema);

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
        user.addStringProperty(EntityProperties.MessageColor);
        user.addBooleanProperty(EntityProperties.Online);
        user.addStringProperty(EntityProperties.MetaData).columnName(EntityProperties.MetaData.toLowerCase());
    }

    private static void addLinkedAccounts(Schema schema) {
        linkedAccount = schema.addEntity(EntityProperties.BUserAccount);
        linkedAccount.addIdProperty();
        linkedAccount.addStringProperty(EntityProperties.Token);
        linkedAccount.addIntProperty(EntityProperties.Type);
    }


    private static void addInstallation(Schema schema) {
        installation = schema.addEntity(EntityProperties.BInstallation);
        installation.addIdProperty();
        installation.addStringProperty(EntityProperties.ApiKey).columnName(EntityProperties.C_ApiKey);
        installation.addStringProperty(EntityProperties.RootPath).columnName(EntityProperties.C_RootPath);
    }

    private static void addLinkedContact(Schema schema) {
        userConnection = schema.addEntity(EntityProperties.BUserConnection);
        userConnection.addIdProperty();
        userConnection.addStringProperty(EntityProperties.EntityID);
        userConnection.addIntProperty(EntityProperties.Type);
    }

    private static void addThread(Schema schema) {
        thread = schema.addEntity(EntityProperties.BThread);
        thread.addIdProperty();
        thread.addStringProperty(EntityProperties.EntityID);
        thread.addDateProperty(EntityProperties.CreationDate);
        thread.addBooleanProperty(EntityProperties.BDeleted);
        thread.addStringProperty(EntityProperties.Name);
        thread.addStringProperty(EntityProperties.CreatorEntityID);

        thread.addStringProperty(EntityProperties.Type);
        thread.addBooleanProperty(EntityProperties.UserCreated);
        thread.addBooleanProperty(EntityProperties.InvitesEnabled);
        thread.addStringProperty(EntityProperties.Description);
        thread.addIntProperty(EntityProperties.Weight);
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
        message.addStringProperty(EntityProperties.ResourcePath);
        message.addStringProperty(EntityProperties.Text);
        message.addStringProperty(EntityProperties.ImageDimensions);
        message.addIntProperty(EntityProperties.Type);
        message.addIntProperty(EntityProperties.Delivered);
    }
    //endregion

    private static void setProperties(){

        // Adding a link between user connection and the user
        Property userProp = userConnection.addLongProperty(EntityProperties.OwnerId).getProperty();
        ToOne toOneUserProp = userConnection.addToOne(user, userProp);
        toOneUserProp.setName(EntityProperties.Owner);

        // Adding a link between user account and the user.
        Property userProp2 = linkedAccount.addLongProperty(EntityProperties.User).getProperty();
        linkedAccount.addToOne(user, userProp2);

        // Adding a link between user and installation
        Property installationIdUser = user.addLongProperty(EntityProperties.InstallationId).getProperty();
        ToOne installationToOneUser = user.addToOne(installation, installationIdUser);
        installationToOneUser.setName(EntityProperties.Installation);

        // Adding link between thread and installation
        Property installationIdThread = thread.addLongProperty(EntityProperties.InstallationId).getProperty();
        ToOne installationToOneThread = thread.addToOne(installation, installationIdThread);
        installationToOneThread.setName(EntityProperties.Installation);

        // Adding a link between installation and user
        ToMany usersToInstallation = installation.addToMany(user, installationIdUser);
        usersToInstallation.setName(EntityProperties.Users);

        // Adding a link between installation and thread
        ToMany threadsToInstallation = installation.addToMany(thread, installationIdThread);
        threadsToInstallation.setName(EntityProperties.Threads);

        // Adding a link to the thread
        Property threadIDProp = message.addLongProperty(EntityProperties.ThreadId).getProperty();
        ToOne one1 = message.addToOne(thread, threadIDProp);
        one1.setName(EntityProperties.Thread);

        // Adding a link to the message sender
        Property senderIDProp = message.addLongProperty(EntityProperties.SenderId).getProperty();
        ToOne one = message.addToOne(user, senderIDProp);
        one.setName(EntityProperties.Sender);






        // Link data for user and thread.
        Property userIdProp = threadUsers.addLongProperty(EntityProperties.UserId).getProperty();
        Property threadIdProp = threadUsers.addLongProperty(EntityProperties.ThreadId).getProperty();
        threadUsers.addToOne(user, userIdProp);
        threadUsers.addToOne(thread, threadIdProp);
        //LinkedContact, LinkedAccount and MetaData - END

        // Threads - START
        Property creatorProp = thread.addLongProperty(EntityProperties.CreatorID).getProperty();
        ToOne one2 = thread.addToOne(user, creatorProp);
        one2.setName(EntityProperties.Creator);

        ToMany messagesProp = thread.addToMany(message, threadIDProp);
        messagesProp.setName(EntityProperties.Messages);

        ToMany linkToUsers = thread.addToMany(threadUsers, threadIdProp);
        linkToUsers.setName(EntityProperties.BLinkData);
        // Threads - END

//      // Users - START
        ToMany contacts = user.addToMany(userConnection, userProp);
        contacts.setName(EntityProperties.BLinkedContacts);


        ToMany accounts = user.addToMany(linkedAccount, userProp2);
        accounts.setName(EntityProperties.BLinkedAccounts);

        ToMany messagesForUser = user.addToMany(message, senderIDProp);
        messagesForUser.setName(EntityProperties.Messages);

        ToMany threadsCreatedForUser = user.addToMany(thread, creatorProp);
        threadsCreatedForUser.setName(EntityProperties.ThreadsCreated);

        ToMany linkToThread = user.addToMany(threadUsers, userIdProp);
        linkToThread.setName(EntityProperties.BLinkData);
//        // Users - END
    }

    private static void setKeepSection(){
        user.setHasKeepSections(true);
    }

    private static void setSuperClass(){
        user.setSuperclass("BUserEntity");
        message.setSuperclass("BMessageEntity");
        thread.setSuperclass("BThreadEntity");
        linkedAccount.setSuperclass("BUserAccountEntity");
        userConnection.setSuperclass("Entity");
        threadUsers.setSuperclass("Entity");
    }
}
