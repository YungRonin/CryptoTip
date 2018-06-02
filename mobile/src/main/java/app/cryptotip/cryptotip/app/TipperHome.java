package app.cryptotip.cryptotip.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.components.WalletPath;
import app.cryptotip.cryptotip.app.database.Database;
import app.cryptotip.cryptotip.app.database.Wallet;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class TipperHome extends AppCompatActivity {
    private String pubKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        String WalletFilePath;
        WalletFilePath = new WalletPath().getPath(this);
        if(WalletFilePath == null) {
            WalletFilePath = getFilesDir().getPath().concat("/" + createWallet());

            final String finalPath = WalletFilePath;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Wallet wallet =new Wallet();
                    wallet.setWalletFilePath(finalPath);
                    Database.getDatabase(TipperHome.this.getApplication()).dao().insertSingleWallet(wallet);
                }
            }) .start();
        }

        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(45).withName("Send"),
                        new PrimaryDrawerItem().withIdentifier(77).withName("Tip History"))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position){
                            case 0: startActivity(new SendActivity().intent(TipperHome.this)); break;
                            case 1: startActivity(new TransactionListActivity().intent(TipperHome.this));
                        }
                        return false;
                    }
                })
                .build();

        RelativeLayout layout = findViewById(R.id.main);
        //layout.addView(drawer);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        TextView pubKeyTview = findViewById(R.id.public_key_text_view);
        pubKeyTview.setTextIsSelectable(true);
        ImageView pubKeyimageView = findViewById(R.id.public_key_qr_code);
        TextView balanceTextView = findViewById(R.id.balance_text_view);

        pubKeyTview.setText(getStringFromFile(new WalletPath().getPath(this)));
        try {
            Credentials creds = WalletUtils.loadCredentials("atestpasswordhere", new WalletPath().getPath(this));
            pubKeyTview.setText(creds.getAddress());
            pubKey = creds.getAddress();
        } catch (CipherException e) {
            Log.e("fail", "exception " + e);
        } catch (IOException e) {
            Log.e("fail", "exception " + e);
        }

        if (pubKey != null) {
            try {
                Bitmap bmp = encodeAsBitmap(pubKey);
                pubKeyimageView.setImageBitmap(bmp);
            }
            catch(WriterException e){
                Log.e("fail", "exception " + e);
            }
        }

        Web3j weby = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));
        EthGetBalance ethGBalance = null;
        try {
            ethGBalance = weby.ethGetBalance(pubKey, DefaultBlockParameterName.LATEST).sendAsync().get();
        }
        catch(InterruptedException e){

        }
        catch (ExecutionException e){

        }

        balanceTextView.setTextIsSelectable(true);
        if(ethGBalance != null) {
            BigInteger bigIntBal = ethGBalance.getBalance();
            balanceTextView.setText("Balance : " + new BalanceHelper().convertWeiToEth(bigIntBal));
        }
        else{
            balanceTextView.setText("failed to retrieve balance");
        }
    }

    private String createWallet(){
        try {
            return WalletUtils.generateNewWalletFile("atestpasswordhere", getFilesDir(), false);
        }
        catch (IOException e){
            Log.e(getClass().getName(), "exception " + e);
        }
        catch(InvalidAlgorithmParameterException e){
            Log.e(getClass().getName(), "exception " + e);
        }
        catch (NoSuchAlgorithmException e){
            Log.e(getClass().getName(), "exception " + e);
        }
        catch (NoSuchProviderException e){
            Log.e(getClass().getName(), "exception " + e);
        }
        catch (CipherException e){
            Log.e(getClass().getName(), "exception " + e);
        }
        return null;
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {

        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 600, 600, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 600, 0, 0, w, h);
        return bitmap;
    }

    public static String getStringFromFile (String filePath){
        File fl = new File(filePath);
        try {
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            //Make sure you close all streams.
            fin.close();
            return ret;
        }catch(IOException e){
            Log.e("fail", "exception " + e);
        }
        return null;
    }

    public static String convertStreamToString(InputStream is){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        }
        catch(IOException e){
            Log.e("fail", "exception " + e);
        }
        return null;
    }

    private BigInteger generateBigInt(){
        String hex = "0x1000000000000000000000000000000000";
        Log.e(getClass().getName(), "length == " + hex.length());
        BigInteger bigInteger = new BigInteger("20000000");// uper limit
        BigInteger min = new BigInteger("10000");// lower limit
        BigInteger bigInteger1 = bigInteger.subtract(min);
        Random rnd = new Random();
        int maxNumBitLength = bigInteger.bitLength();

        BigInteger aRandomBigInt;

        Log.e(getClass().getName(), "midlle way here");

        aRandomBigInt = new BigInteger(maxNumBitLength, rnd);
        if (aRandomBigInt.compareTo(min) < 0)
            aRandomBigInt = aRandomBigInt.add(min);
        if (aRandomBigInt.compareTo(bigInteger) >= 0)
            aRandomBigInt = aRandomBigInt.mod(bigInteger1).add(min);

        Log.e(getClass().getName(), "return a value ");

        return aRandomBigInt;
    }
}
