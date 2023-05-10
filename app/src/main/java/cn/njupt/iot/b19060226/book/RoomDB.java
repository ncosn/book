package cn.njupt.iot.b19060226.book;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserData.class, TestData.class},version = 1,exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    private static RoomDB database;

    private static String DATABASE_NAME = "database";

    public synchronized static RoomDB getInstance(Context context) {
        //Check condition
        if (database == null) {
            //When database is null
            //Initialize database
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class,DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    //Craete Dao
    public abstract UserDao userDao();

    public abstract TestDao testDao();
}
