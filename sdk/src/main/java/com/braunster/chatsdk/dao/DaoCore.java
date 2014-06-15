package com.braunster.chatsdk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;


/**
 * Manage all creation, deletion and updating Entities.
 */
public class DaoCore {

    private static final String DB_NAME = "andorid-chatsdk-database";
    private static String dbName;

    private static Context context;

    private static DaoMaster.DevOpenHelper helper;
    private static SQLiteDatabase db;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static void init(Context ctx) {
        dbName = DB_NAME;
    }

    public static void init(Context ctx, String name){
        context = ctx;
        dbName = name;
        openDB();
    }

    private static void openDB(){
        if (context == null)
            throw new NullPointerException("Context is null, Did you initialized DaoCore?");

        helper = new DaoMaster.DevOpenHelper(context, dbName, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        List<BMetadata> list = fetchEntitiesWithProperty(new BMetadata().getClass(), BMetadataDao.Properties.Dirty, "ss");

    }

    public static <T extends Entity> T fetchEntityWithID(Class c, String entityID){
        return (T) daoSession.load(c, entityID);
    }

    public static <T extends Entity> List<T> fetchEntitiesWithProperty(Class c, Property property, String name){
        QueryBuilder qb = daoSession.queryBuilder(c);
        qb.where(property.eq(name));
        return qb.list();
    }

    public static <T extends Entity> List<T> fetchEntitiesWithProperties(Class c, Property properties[], String... values){
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

    private  static void  createEntity(AbstractEntity entity){
        daoSession.insert(entity);
    }

    private static void deleteEntity(AbstractEntity entity){
        daoSession.delete(entity);
    }
}
