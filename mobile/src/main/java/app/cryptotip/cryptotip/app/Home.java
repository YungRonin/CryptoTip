package app.cryptotip.cryptotip.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gani.lib.http.GRestCallback;
import com.gani.lib.http.GRestResponse;
import com.gani.lib.http.HttpAsyncGet;
import com.gani.lib.http.HttpHook;
import com.gani.lib.logging.GLog;
import com.gani.lib.ui.ProgressIndicator;
import com.gani.lib.ui.view.GTextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONException;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.http.MyImmutableParams;
import app.cryptotip.cryptotip.app.json.MyJsonObject;
import app.cryptotip.cryptotip.app.transaction.TransactionListActivity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static app.cryptotip.cryptotip.app.SettingsActivity.SELECTED_CRYPTO_CURRENCY;
import static app.cryptotip.cryptotip.app.SettingsActivity.SELECTED_FIAT_CURRENCY;

public class Home extends AppCompatActivity {
    private String pubKey;
    private String walletFilePath;
    private String fiatCurrency;
    private String cryptoCurrency;
    private String cryptoBalance;
    private static final int CURRENCY_CHANGE = 555;
    public static final String WALLET_FILE_PATH = "walletFilePath";
    public static final String FIAT_PRICE = "fiatPrice";
    private GTextView fiatBalanceTextView;
    private GTextView cryptoBalanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cryptoBalance = "0";

        walletFilePath = DbMap.get(WALLET_FILE_PATH);
        if(walletFilePath == null) {
            walletFilePath = getFilesDir().getPath().concat("/" + createWallet());

            DbMap.put(WALLET_FILE_PATH, walletFilePath);
        }

        Toolbar tbar = this.findViewById(R.id.toolbar);
        tbar.setTitle("cryptoTIP");

        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(tbar)
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(45).withName("Send"),
                        new PrimaryDrawerItem().withIdentifier(77).withName("Tip History"),
                        new PrimaryDrawerItem().withIdentifier(86).withName("Settings"))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position){
                            case 0: startActivity(new ReceiverAddressActivity().intent(Home.this)); break;
                            case 1: startActivity(new TransactionListActivity().intent(Home.this)); break;
                            case 2: startActivityForResult(new SettingsActivity().intent(Home.this), CURRENCY_CHANGE);
                        }
                        return false;
                    }
                })
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CURRENCY_CHANGE) {
            this.onPostCreate(null);
            //todo refactor to improve efficiency (only call necessary methods in onPostCreate)
        }
    }


    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        TextView pubKeyTview = findViewById(R.id.public_key_text_view);
        pubKeyTview.setTextIsSelectable(true);
        ImageView pubKeyimageView = findViewById(R.id.public_key_qr_code);
        cryptoBalanceTextView = findViewById(R.id.eth_balance_text_view);
        fiatBalanceTextView = findViewById(R.id.fiat_balance_text_view);

        pubKeyTview.setText(getStringFromFile(walletFilePath));
        try {
            Credentials creds = WalletUtils.loadCredentials("atestpasswordhere", walletFilePath);
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

        fiatCurrency = DbMap.get(SELECTED_FIAT_CURRENCY);
        if(fiatCurrency == null){
            fiatCurrency = "USD";
        }

        cryptoCurrency = DbMap.get(SELECTED_CRYPTO_CURRENCY);
        if(cryptoCurrency == null){
            cryptoCurrency = "ETH";
        }

        refresh();

        CardView refreshButton = findViewById(R.id.refresh_button_card_view);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

    }

    private void refresh(){

        Web3j weby = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));
        EthGetBalance ethGBalance = null;
        try {
            ethGBalance = weby.ethGetBalance(pubKey, DefaultBlockParameterName.LATEST).sendAsync().get();
        }
        catch(InterruptedException e){

        }
        catch (ExecutionException e){

        }

        cryptoBalanceTextView.setTextIsSelectable(true);
        if(cryptoCurrency.contentEquals("ETH")) {
            if (ethGBalance != null) {
                BigInteger bigIntBal = ethGBalance.getBalance();
                cryptoBalance = new BalanceHelper().convertWeiToEth(bigIntBal);
                cryptoBalanceTextView.setText(cryptoCurrency.concat(" Balance : " + cryptoBalance));
            } else {
                cryptoBalanceTextView.setText("failed to retrieve balance");
            }
        }
        else{
            String contractAddress = getContractAddress(cryptoCurrency);
            if(contractAddress != null) {
                try {

                    Function function = new Function(
                            "balanceOf", Arrays.<Type>asList(new Address(pubKey)), new ArrayList<TypeReference<?>>()); //Uint256
                    String encodedFunction = FunctionEncoder.encode(function);
                    org.web3j.protocol.core.methods.response.EthCall response = weby.ethCall(
                            Transaction.createEthCallTransaction(pubKey, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                            .sendAsync().get();

                    Address result = new Address(response.getResult());

                    cryptoBalance = new BalanceHelper().convertWeiToEth(result.toUint160().getValue());
                    cryptoBalanceTextView.setText(cryptoCurrency.concat(" Balance : " + cryptoBalance));

                } catch (InterruptedException e) {
                    cryptoBalanceTextView.setText("failed to retrieve balance");
                    GLog.e(getClass(), "interupted " + e);
                } catch (ExecutionException e) {
                    cryptoBalanceTextView.setText("failed to retrieve balance");
                    GLog.e(getClass(), "execution excptional " + e);
                }
            }
            else{
                cryptoBalanceTextView.setText("failed to retrieve balance");
            }
        }


        //todo support erc20 prices
        new HttpAsyncGet("https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=" + fiatCurrency, MyImmutableParams.EMPTY, HttpHook.DUMMY, new GRestCallback(this, new ProgressIndicator() {
            @Override
            public void showProgress() {
                // TODO: 11/06/18
            }

            @Override
            public void hideProgress() {
                // TODO: 11/06/18
            }
        }) {
            @Override
            protected void onRestResponse(GRestResponse r) throws JSONException {
                super.onRestResponse(r);
                MyJsonObject object = new MyJsonObject(r.getJsonString());
                String price = object.getString(fiatCurrency);
                DbMap.put(FIAT_PRICE, price);
                String text = fiatCurrency + " : " + price + "\nvalue : " + String.valueOf(Double.valueOf(cryptoBalance) * Double.valueOf(price));
                fiatBalanceTextView.setText(text);
            }
        }).execute();
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

    public static String getContractAddress(String tokenID){
        switch (tokenID){
            case "ALPH" : return "0xfb0fBFd118D25bBDB82fF6bFe9b08f3Ae9B68a64";
            case "BETA" : return "0xadADEef132DbE73cF951DD77C4cFcF87D682F543";
            case "OMEG" : return "0x9f2B11C377a6bBA0D4dFaC81d7A4768955AC5900";
            default: return null;
        }
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
