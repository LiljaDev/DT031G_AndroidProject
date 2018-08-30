package se.miun.joli1407.bathingsites;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Singleton for Room database access
 */
@Database(entities = {BathingSite.class}, version = 16)
public abstract class AppDataBase extends RoomDatabase {
    private static AppDataBase mInstance;

    public abstract BathingSiteDao bathingSiteDao();

    public static AppDataBase getDatabase(Context context){
        if(mInstance == null){
            mInstance = Room.databaseBuilder(context, AppDataBase.class, "bathingsitedatabase")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return mInstance;
    }

    public static void destroyInstance(){
        mInstance = null;
    }
}
