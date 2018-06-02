package app.cryptotip.cryptotip.app.components;

import android.app.Activity;

import app.cryptotip.cryptotip.app.database.Database;

public class WalletPath {
    private static String path;

    public static String getPath(final Activity activity){
        if(path == null){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    path = Database.getDatabase(activity.getApplication()).dao().fetchFirstWallet().getWalletFilePath();
                }
            }).start();
        }
        return path;
    }
}
