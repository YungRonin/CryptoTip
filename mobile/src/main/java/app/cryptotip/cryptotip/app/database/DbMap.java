package app.cryptotip.cryptotip.app.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Entity(indices = @Index(value = "name", unique = true))
public class DbMap {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    private String name;  // Don't use the name "key" because it's a reserved word
    private String value;

    DbMap(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private static DbMap row(String key) {
        return DbRoom.INSTANCE.json().where(key);
    }

    public static String get(String key) {
        DbMap record = row(key);
        if (record != null) {
            return record.getValue();
        }
        return null;
    }

    public static void put(String key, String value) {
        DbMap record = row(key);
        if (record == null) {
            DbRoom.INSTANCE.json().insert(new DbMap(key, value));
        }
        else {
            record.setValue(value);
            DbRoom.INSTANCE.json().update(record);
        }
    }



    @Dao
    public interface Access {
        @Query("SELECT * FROM DbMap")
        List<DbMap> all();

        @Query("SELECT * FROM DbMap where name LIKE  :key")
        DbMap where(String key);

        @Query("SELECT COUNT(*) from DbMap")
        int count();

        @Insert
        void insert(DbMap... records);

        @Update
        int update(DbMap record);

        @Delete
        void delete(DbMap record);
    }
}
