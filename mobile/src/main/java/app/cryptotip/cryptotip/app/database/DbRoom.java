package app.cryptotip.cryptotip.app.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.gani.lib.Res;

@Database(entities = {DbMap.class}, version = 1)
public abstract class DbRoom extends RoomDatabase {
    public static final DbRoom INSTANCE = Room.databaseBuilder(Res.context(), DbRoom.class, "main")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build();

    public abstract DbMap.Access json();
}