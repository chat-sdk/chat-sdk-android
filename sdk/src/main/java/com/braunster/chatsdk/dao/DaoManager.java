/*
package com.braunster.chatsdk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

*/
/**
 * Created by itzik on 6/8/2014.
 *//*

public class DaoManager {

    private static final String DB_NAME = "android_chat_sdk_database";

    private static Context context;
    private static DaoMaster.DevOpenHelper helper;
    private static SQLiteDatabase db;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static void init(Context c){
        context = c;
    }

    private static void setUpDB(){
        helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private static DaoSession getSession(){
        return daoSession;
    }
}
*/
