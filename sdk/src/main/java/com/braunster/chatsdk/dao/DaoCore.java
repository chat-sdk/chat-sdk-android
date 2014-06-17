package com.braunster.chatsdk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;


/**
 * Manage all creation, deletion and updating Entities.
 */
public class DaoCore {
    private static final String TAG = DaoCore.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String DB_NAME = "andorid-chatsdk-database";
    private static String dbName;

    private static Context context;

    private static DaoMaster.DevOpenHelper helper;
    private static SQLiteDatabase db;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static void init(Context ctx) {
        dbName = DB_NAME;
        context = ctx;
        openDB();
        test();
    }

    public static void init(Context ctx, String databaseName){
        context = ctx;
        dbName = databaseName;
        openDB();
    }

    private static void openDB(){
        if (context == null)
            throw new NullPointerException("Context is null, Did you initialized DaoCore?");

        helper = new DaoMaster.DevOpenHelper(context, dbName, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    //region Test
    private static void test(){
        clearTestData();
        createTestData();
        getTestData2();
    }

    private static void clearTestData(){
        daoSession.getBUserDao().deleteAll();
        daoSession.getBMessageDao().deleteAll();
        daoSession.getBThreadDao().deleteAll();
        daoSession.getBMetadataDao().deleteAll();
        daoSession.getBLinkedAccountDao().deleteAll();
        daoSession.getBLinkedContactDao().deleteAll();
    }

    private static void createTestData(){
        BUser user = null, user1 = null;
        try {
            user = new BUser();
            user.setName("Dan");
            user.setLastOnline(new Date(System.currentTimeMillis()));
            user.setEntityID("asdasdas54d5a");
            createEntity(user);

            user1 = new BUser();
            user1.setName("Alex");
            user1.setLastOnline(new Date(System.currentTimeMillis()));
            user1.setEntityID("54ads54fafs54a");
            createEntity(user1);

            BMetadata bMetadata, bMetadata1;

            String[] key = new String[]{"Country" , "Gender", "Age", "FootballTeam", "NationalTeam"};
            String[] values = new String[]{"Israel" , "Male", "23", "Juventus", "Italy"};

            String[] key1 = new String[]{"Country" , "Gender", "Age", "FootballTeam", "NationalTeam"};
            String[] values1 = new String[]{"Germany" , "Male", "26", "Herta Berlin", "Germany"};

            for (int i = 0 ; i < 5 ; i++)
            {
                bMetadata = new BMetadata();
                bMetadata.setOwner(user.getEntityID());
                bMetadata.setType("0");
                bMetadata.setKey(key[i]);
                bMetadata.setValue(values[i]);
                createEntity(bMetadata);

                bMetadata1 = new BMetadata();
                bMetadata1.setOwner(user1.getEntityID());
                bMetadata1.setType("1");
                bMetadata1.setKey(key1[i]);
                bMetadata1.setValue(values1[i]);
                createEntity(bMetadata1);
            }

            int t = 9;
            BThread thread;
            String [] threadNames = new String[] { "Work", "Family", "Party", "Serie A", "World Cup", "School", "Army Friends", "JCI"};
            int [] threadType = new int[] { 0, 0, 1, 1, 1, 0, 0, 0};
            BLinkData linkData;

            for (int i = 0; i < t; i++) {

                thread = new BThread();
                thread.setEntityID(generateEntity());
                thread.setType(threadType[i]);
                thread.setName(threadNames[i]);
                thread.setCreator(i % 2 == 0 ? user.getEntityID() : user1.getEntityID());
                createEntity(thread);

                //region LinkData
                linkData = new BLinkData();
                linkData.setEntityId(generateEntity());
                linkData.setThreadID(thread.getEntityID());
                linkData.setUserID(user.getEntityID());

                createEntity(linkData);

                linkData = new BLinkData();
                linkData.setEntityId(generateEntity());
                linkData.setThreadID(thread.getEntityID());
                linkData.setUserID(user1.getEntityID());

                createEntity(linkData);
                //endregion

                BMessage message;
                for (int j = 0; j < 7; j++) {
                    message = new BMessage();
                    message.setEntityID(generateEntity());
                    message.setOwnerThread(thread.getEntityID());
                    message.setText(generateEntity());
                    message.setDate(new Date(System.currentTimeMillis()));
                    message.setType(BMessage.Type.bText.ordinal());
                    message.setSender(j % 2 == 0 ? user.getEntityID() : user1.getEntityID());
                    createEntity(message);
                }
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static void getTestData(){
        //region Description
/*        QueryBuilder<BUser> queryBuilder = daoSession.queryBuilder(BUser.class);
        BUser bUser = queryBuilder.where(BUserDao.Properties.EntityID .eq("asdasdas54d5a")).unique();

        if (bUser == null)
        {
            Log.d(TAG, "user is null");
            bUser = queryBuilder.where(BUserDao.Properties.Name .eq("Dan")).unique();
        }

        if (bUser == null)
        {
            Log.d(TAG, "user is null");
        }
        else
        {
            Log.d(TAG, "has user");
        }*/
        //endregion

        printUsersData(daoSession.loadAll(BUser.class));
    }

    private static void printUsersData(List<BUser> users){
        for (BUser u : users)
        {
            if (u == null)
                Log.d(TAG, "user is null");
            else
            {
                Log.i(TAG, "User");
                Log.i(TAG, "entity: " + u.getEntityID());
                Log.i(TAG, "name: " + u.getName());
                Log.i(TAG, "Metadata Size: " + u.getBMetadata().size());
                Log.i(TAG, "Messages Amount: " + u.getMessages().size());
                for (BMetadata m : u.getBMetadata())
                {
                    Log.i(TAG, "Metadata of " + m.getBUser().getName() + " key: " + m.getKey() + ", Value: " + m.getValue());
                }
                Log.d(TAG, "ThreadsCreated Size: "  + u.getThreadsCreated().size());

                for (BThread t : u.getThreadsCreated())
                {
                    Log.d(TAG, "Thread, Name: " + t.getName() + ", Type: " + t.getType() + ", Messages Amount: " + t.getMessages().size());

                    for (BMessage m : t.getMessages())
                        Log.i(TAG, "Message, Sender: " + m.getBUserSender().getName() + ", Text: " + m.getText());
                }

            }
        }
    }

    private static void getTestData2(){
        List<BUser> list = fetchEntitiesWithProperty(BUser.class, BUserDao.Properties.Name, "Dan");
        printUsersData(list);
        BThread thread = fetchEntityWithID(BThread.class, "asdasda");
        List<BThread> threads = fetchEntitiesWithProperty(BThread.class, BThreadDao.Properties.Type, "0");
    }

    public static String generateEntity() {
        return new BigInteger(130, new Random()).toString(32);
    }
    //endregion

    public static <T extends Entity<T>> T fetchEntityWithID(Class c, String entityID){
        return (T) daoSession.load(c, entityID);
    }

    public static <T extends Entity<T>> T fetchEntityWithProperty(Class c, Property property,String value){
        QueryBuilder qb = daoSession.queryBuilder(c);
        qb.where(property.eq(value));
        return (T) qb.unique();
    }

    public static <T extends Entity<T>> List<T> fetchEntitiesWithProperty(Class c, Property property, Object value){
        if (DEBUG) Log.v(TAG, "fetchEntitiesWithProperty");
        QueryBuilder qb = daoSession.queryBuilder(c);
        qb.where(property.eq(value));
        return qb.list();
    }

    public static <T extends Entity<T>> List<T> fetchEntitiesWithProperties(Class c, Property properties[], String... values){
        if (values == null || properties == null)
            throw new NullPointerException("You must have at least one value and one property");

        if (values.length != properties.length)
            throw new IllegalArgumentException("Values size should match properties size");


        QueryBuilder qb = daoSession.queryBuilder(c);
        qb.where(properties[0].eq(values[0]));

        if (values.length > 1)
            for (int i = 0 ; i < values.length ; i++)
                qb.where(properties[i].eq(values[i]));

        return qb.list();
    }

    public static BUser fetchOrCreateUserWithEntityID(String entityId, String facebookID){

        List<BUser> users = new ArrayList<BUser>();

        if (entityId != null && facebookID != null){
            users  = fetchEntitiesWithProperties(BUser.class,
                    new Property[]{BUserDao.Properties.EntityID, BUserDao.Properties.FacebookID},
                    entityId, facebookID);
        }
        else
        {
            if (entityId != null)
            {
                users = fetchEntitiesWithProperty(BUser.class, BUserDao.Properties.EntityID, entityId);
            }
            else if (facebookID != null)
            {
                users = fetchEntitiesWithProperty(BUser.class, BUserDao.Properties.FacebookID, facebookID);
            }
        }

        BUser user = null;

        if (users.size() == 0)
        {
            createEntity(user);
        }
        else
        {
            // It's possible that we could get multiple user records some registered
            // with a Facebook ID and some with a Firebase ID so we'll merge the records here
            user = users.get(0);

            for (BUser u : users)
            {
                if (u.equals(user))
                {
                    user.updateFrom(u);
                    deleteEntity(u);
                }
            }
            daoSession.update(user);
        }

        return user;
    }

    public static void  createEntity(Entity entity){
        Log.v(TAG, "createEntity");

        // Generate an id for the object if needed
        if (entity.getEntityID() == null || entity.getEntityID().equals(""))
        {
            Log.d(TAG, "Creating id for entity.");
            entity.setEntityId(generateEntity());
            // FIXME entity id is not saved to the object
        }

        daoSession.insert(entity);
    }


    public static void deleteEntity(Entity entity){
        daoSession.delete(entity);
    }

    // TODO see if needed to check for bad values that return from the query and might wont get cast well.
/*    private void throwValidationError(){

    }*/


/*    public static <T extends Entity> T createEntity(){

   *//*      T t = null;

       switch (type)
        {
            case bEntityTypeUser:
                t = (T) new BUser();
                break;

            case bEntityTypeMessages:
                t = (T) new BMessage();
                break;

            case bEntityTypeThread:

                break;

            case bEntityTypeGroup:

                break;
        }*//*

        T t = (T) new Entity();

        if (t != null)
            t.setEntityId(generateEntity());

        return t;
    }*/
}
