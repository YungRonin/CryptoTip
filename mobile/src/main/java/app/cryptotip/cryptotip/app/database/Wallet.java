package app.cryptotip.cryptotip.app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Wallet {

    @NonNull
    @PrimaryKey
    private String walletFilePath;

    public String getWalletFilePath(){return walletFilePath;}
    public void setWalletFilePath(String args){walletFilePath = args;}
}
