package app.cryptotip.cryptotip.app.database;


import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@android.arch.persistence.room.Dao
public interface Dao {

    @Insert
    void insertSingleWallet (Wallet wallet);
    @Insert
    void insertMultipleWallets (List<Wallet> walletList);
    @Query("SELECT * FROM Wallet WHERE walletFilePath = :walletFilePath")
    Wallet fetchOneWalletbyId (int walletFilePath);
    @Update
    void updateWallet (Wallet wallet);
    @Delete
    void deleteWallet (Wallet wallet);
    @Query("SELECT * FROM Wallet LIMIT 1")
    Wallet fetchFirstWallet();
}