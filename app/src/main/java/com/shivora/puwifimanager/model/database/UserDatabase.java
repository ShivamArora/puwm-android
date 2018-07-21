package com.shivora.puwifimanager.model.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {UserEntry.class},version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase{
    private static final String TAG = UserDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "userdb";

    //Single Instance
    private static UserDatabase sInstance;

    public static UserDatabase getInstance(Context context){
        if (sInstance == null){
            synchronized (LOCK){
                Log.d(TAG, "Creating new database instance");

                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                                                    UserDatabase.class,
                                                    UserDatabase.DATABASE_NAME)
                                                    .build();
            }
        }
        Log.d(TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract UserDao userDao();
}
