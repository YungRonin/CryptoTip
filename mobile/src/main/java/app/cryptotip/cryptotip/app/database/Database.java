package app.cryptotip.cryptotip.app.database;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@android.arch.persistence.room.Database(entities = {Wallet.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    private static Database INSTANCE;
    private static final String DATABASE_NAME = "Wallet_DB";


    public static Database getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, Database.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public abstract Dao dao() ;
}
