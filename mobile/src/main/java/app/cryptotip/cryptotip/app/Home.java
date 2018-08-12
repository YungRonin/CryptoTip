package app.cryptotip.cryptotip.app;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gani.lib.http.GRestCallback;
import com.gani.lib.http.GRestResponse;
import com.gani.lib.http.HttpAsyncGet;
import com.gani.lib.http.HttpHook;
import com.gani.lib.logging.GLog;
import com.gani.lib.ui.ProgressIndicator;
import com.gani.lib.ui.alert.ToastUtils;
import com.gani.lib.ui.view.GTextView;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.satoshilabs.trezor.lib.TrezorManager;
import com.satoshilabs.trezor.lib.protobuf.TrezorMessage;

import org.json.JSONException;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.exceptions.MessageDecodingException;
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
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.http.MyImmutableParams;
import app.cryptotip.cryptotip.app.json.MyJsonObject;
import app.cryptotip.cryptotip.app.transaction.TransactionListActivity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.satoshilabs.trezor.lib.TrezorManager.parseMessageFromBytes;

public class Home extends AppCompatActivity {
    private String pubKey;
    private String walletFilePath;
//    private String fiatCurrency;
//    private String cryptoCurrency;
    private String cryptoBalance;
    private String selectedFiatCurrency;
    private String selectedCryptoCurrency;
    private ImageView pubKeyimageView;
    private TextView pubKeyTview;
    private GTextView fiatBalanceTextView;
    private GTextView cryptoBalanceTextView;
    private Drawer activityDrawer;
    private Drawer settingsDrawer;
    private TrezorDevice trezorDevice;
    protected DrawerLayout mDrawerLayout;
    private static final int CURRENCY_CHANGE = 555;
    public static final String WALLET_FILE_PATH = "walletFilePath";
    public static final String FIAT_PRICE = "fiatPrice";
    public static final String SELECTED_FIAT_CURRENCY = "selectedFiatCurrency";
    public static final String SELECTED_CRYPTO_CURRENCY = "selectedCryptoCurrency";

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

        setUpToolbarAndDrawers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CURRENCY_CHANGE) {
            this.onPostCreate(null);
        }
    }


    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        pubKeyTview = findViewById(R.id.public_key_text_view);
        pubKeyTview.setTextIsSelectable(true);
        pubKeyimageView = findViewById(R.id.public_key_qr_code);
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

        selectedFiatCurrency = DbMap.get(SELECTED_FIAT_CURRENCY);
        if(selectedFiatCurrency == null){
            selectedFiatCurrency = "USD";
            DbMap.put(SELECTED_FIAT_CURRENCY, selectedFiatCurrency);
        }

        selectedCryptoCurrency = DbMap.get(SELECTED_CRYPTO_CURRENCY);
        if(selectedCryptoCurrency == null){
            selectedCryptoCurrency = "ETH";
            DbMap.put(SELECTED_CRYPTO_CURRENCY, selectedCryptoCurrency);
        }

        refresh();

        CardView refreshButton = findViewById(R.id.refresh_button_card_view);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        CardView trezorButton = findViewById(R.id.trezor_button_card_view);
        trezorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessTrezor();
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

        if(pubKey != null) {
            try {
                Bitmap bmp = encodeAsBitmap(pubKey);
                pubKeyimageView.setImageBitmap(bmp);
            } catch (WriterException e) {
                Log.e("fail", "exception " + e);
            }
            pubKeyTview.setText(pubKey);
        }

        cryptoBalanceTextView.setTextIsSelectable(true);
        if(selectedCryptoCurrency.contentEquals("ETH")) {
            try {
                if (ethGBalance != null) {
                    BigInteger bigIntBal = ethGBalance.getBalance();
                    cryptoBalance = new BalanceHelper().convertWeiToEth(bigIntBal);
                    cryptoBalanceTextView.setText(selectedCryptoCurrency.concat(" Balance : " + cryptoBalance));
                } else {
                    cryptoBalanceTextView.setText("failed to retrieve balance");
                }
            }
            catch(MessageDecodingException e){
                GLog.e(getClass(), "Error getting eth balance " + e);
            }
        }
        else{
            String contractAddress = getContractAddress(selectedCryptoCurrency);
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
                    cryptoBalanceTextView.setText(selectedCryptoCurrency.concat(" Balance : " + cryptoBalance));

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
        new HttpAsyncGet("https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=" + selectedFiatCurrency, MyImmutableParams.EMPTY, HttpHook.DUMMY, new GRestCallback(this, new ProgressIndicator() {
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
                String price = object.getString(selectedFiatCurrency);
                DbMap.put(FIAT_PRICE, price);
                String text = selectedFiatCurrency + " : " + price + "\nvalue : " + String.valueOf(Double.valueOf(cryptoBalance) * Double.valueOf(price));
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


    private void accessTrezor() {

        UsbManager usbManager = (UsbManager) this.getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        UsbDevice deviceWithoutPermission = null;

        for (UsbDevice usbDevice : deviceList.values()) {
            // check if the device is TREZOR
            Boolean deviceIsTrezor = isDeviceTrezor(usbDevice);
            if (deviceIsTrezor == null || !deviceIsTrezor){

            }
            else{
                if (!usbManager.hasPermission(usbDevice)) {
                    usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(this, 0, new Intent(TrezorManager.UsbPermissionReceiver.ACTION), 0));
                    deviceWithoutPermission = usbDevice;
                }
                else{
                    doTrezorThings(usbDevice, usbManager);
                }
            }
        }

        if(deviceWithoutPermission != null){
            doTrezorThings(deviceWithoutPermission, usbManager);
        }
    }

    private void doTrezorThings(UsbDevice device, UsbManager usbManager){

        UsbDeviceConnection connection = usbManager.openDevice(device);
        UsbInterface usbInterface = device.getInterface(0);
        UsbEndpoint readEndpoint = null, writeEndpoint = null;

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (readEndpoint == null && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT && ep.getAddress() == 0x81) { // number = 1 ; dir = USB_DIR_IN
                readEndpoint = ep;
                continue;
            }
            if (writeEndpoint == null && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT && (ep.getAddress() == 0x01 || ep.getAddress() == 0x02)) { // number = 1 ; dir = USB_DIR_OUT
                writeEndpoint = ep;
            }
        }
        if (readEndpoint == null) {
            ToastUtils.showNormal("tryGetDevice: Could not find read endpoint", Toast.LENGTH_LONG);
        }
        if (writeEndpoint == null) {
            ToastUtils.showNormal("tryGetDevice: Could not find write endpoint", Toast.LENGTH_LONG);
        }
        if (readEndpoint.getMaxPacketSize() != 64) {
            ToastUtils.showNormal("tryGetDevice: Wrong packet size for read endpoint", Toast.LENGTH_LONG);
        }
        if (writeEndpoint.getMaxPacketSize() != 64) {
            ToastUtils.showNormal("tryGetDevice: Wrong packet size for write endpoint", Toast.LENGTH_LONG);
        }


        if (connection == null) {
            ToastUtils.showNormal("tryGetDevice: could not open connection", Toast.LENGTH_LONG);
        } else {
            if (!connection.claimInterface(usbInterface, true)) {
                ToastUtils.showNormal("tryGetDevice: could not claim interface", Toast.LENGTH_LONG);

            } else {
                trezorDevice = new TrezorDevice(device.getDeviceName(), connection.getSerial(), connection, usbInterface, readEndpoint, writeEndpoint);
            }
        }

        if(trezorDevice != null){


                TrezorMessage.Initialize req = TrezorMessage.Initialize.newBuilder().build();
                TrezorMessage.EthereumGetAddress ethReq = TrezorMessage.EthereumGetAddress.newBuilder().build();
            try {
                Message resp = trezorDevice.sendMessage(req);
                if (resp != null) {
//                        ToastUtils.showNormal("success ".concat(resp.getClass().getSimpleName()), Toast.LENGTH_LONG);

                }
                Message ethResp = trezorDevice.sendMessage(ethReq);
                if (resp != null) {
                    Map<Descriptors.FieldDescriptor, Object> map = ethResp.getAllFields();

                    if(!map.isEmpty()){
//                        LinkedList<String> keysAndValues = new LinkedList();
//                        Iterator it = map.entrySet().iterator();
//                        while (it.hasNext()){
//                            Map.Entry pair = (Map.Entry)it.next();
//                            keysAndValues.add(pair.getKey().toString());
//                            keysAndValues.add(pair.getValue().toString());
//                        }
//
//                        String result ="Result : ";
//                        for(String value : keysAndValues){
//                            result = result.concat(", ".concat(value));
//                        }

                        ByteString ethAddress = (ByteString) map.entrySet().iterator().next().getValue();
                        byte[] byteArray = ethAddress.toByteArray();

                        String allTheBytes = "0x";
                        for(byte b : byteArray){
                            String byteString = Integer.toHexString(b & 0xFF);
                            allTheBytes = allTheBytes.concat(byteString);
                        }

                        new AlertDialog.Builder(this).setTitle("Status")
                                .setMessage(allTheBytes)
                                .create()
                                .show();

                        pubKey = allTheBytes;
                    }
                }
            }
            catch (InvalidProtocolBufferException e){
                ToastUtils.showNormal("Failed to retrieve address ".concat(e.getMessage()), Toast.LENGTH_LONG);
            }
        }
    }

    public static Boolean isDeviceTrezor(UsbDevice usbDevice){
        if (usbDevice.getVendorId() == 0x1209) {
            return usbDevice.getProductId() == 0x53c0 || usbDevice.getProductId() == 0x53c1;
        }
        return null;
    }

    private android.app.AlertDialog createFiatCurrencySelectorDialog(){
        final CharSequence[] fiatCurrencyCodes = {"AUD", "USD", "EUR", "GBP", "INR", "MYR", "IDR", "RUB", "NZD", "THB", "SGD", "JPY", "CNY", "CAD", "KRW", "SAR", "AED"};

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Currecny")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbMap.put(SELECTED_FIAT_CURRENCY, selectedFiatCurrency);
                        refresh();
                    }
                })
                .setSingleChoiceItems(fiatCurrencyCodes, fiatCurrencyCodes.length, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFiatCurrency = fiatCurrencyCodes[which].toString();
                    }
                }).create();
        return dialog;
    }

    private android.app.AlertDialog createCryptoCurrencySelectorDialog(){
        final CharSequence[] cryptoCurrencyCodes = {"ETH", "OMEG", "BETA", "ALPH"};

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Currecny")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbMap.put(SELECTED_CRYPTO_CURRENCY, selectedCryptoCurrency);
                        refresh();
                    }
                })
                .setSingleChoiceItems(cryptoCurrencyCodes, cryptoCurrencyCodes.length, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCryptoCurrency = cryptoCurrencyCodes[which].toString();
                    }
                }).create();
        return dialog;
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (activityDrawer != null && activityDrawer.isDrawerOpen()) {
            activityDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void setUpToolbarAndDrawers(){
        Toolbar tbar = this.findViewById(R.id.toolbar);
        tbar.setTitle("cryptoTIP");
        setSupportActionBar(tbar);
        ImageView iconView = new ImageView(this);
        iconView.setPadding(750,0,0,0); //todo icon will be off edge of screen on lower resolution screen
        iconView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_settings_20px, null));
        tbar.addView(iconView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100));

        activityDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(tbar)
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(45).withName("Send"),
                        new PrimaryDrawerItem().withIdentifier(77).withName("Tip History"))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position){
                            case 0: startActivity(new ReceiverAddressActivity().intent(Home.this)); break;
                            case 1: startActivity(new TransactionListActivity().intent(Home.this)); break;
                        }
                        return false;
                    }
                })
                .build();

        settingsDrawer = new DrawerBuilder()
                .withActivity(this)
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(45).withName("Fiat Currency"),
                        new PrimaryDrawerItem().withIdentifier(77).withName("Crypto Currency"))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position){
                            case 0: createFiatCurrencySelectorDialog().show(); break;
                            case 1: createCryptoCurrencySelectorDialog().show(); break;
                        }
                        return false;
                    }
                })
                .withDrawerGravity(Gravity.END)
                .append(activityDrawer);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDrawer.openDrawer();
            }
        });
    }

    public static class TrezorDevice {
        private static final String TAG = TrezorDevice.class.getSimpleName();

        private final String deviceName;
        private final String serial;

        // next fields are only valid until calling close()
        private UsbDeviceConnection usbConnection;
        private UsbInterface usbInterface;
        private UsbEndpoint readEndpoint;
        private UsbEndpoint writeEndpoint;

        TrezorDevice(String deviceName,
                     String serial,
                     UsbDeviceConnection usbConnection,
                     UsbInterface usbInterface,
                     UsbEndpoint readEndpoint,
                     UsbEndpoint writeEndpoint) {
            this.deviceName = deviceName;
            this.serial = serial;
            this.usbConnection = usbConnection;
            this.usbInterface = usbInterface;
            this.readEndpoint = readEndpoint;
            this.writeEndpoint = writeEndpoint;
        }

        @Override
        public String toString() {
            return "TREZOR(path:" + this.deviceName + " serial:" + this.serial + ")";
        }

        Message sendMessage(Message msg) throws InvalidProtocolBufferException {
            if (usbConnection == null)
                throw new IllegalStateException(TAG + ": sendMessage: usbConnection already closed, cannot send message");

            messageWrite(msg);
            return messageRead();
        }

        void close() {
            if (this.usbConnection != null) {
                try {
                    usbConnection.releaseInterface(usbInterface);
                }
                catch (Exception ex) {}
                try {
                    usbConnection.close();
                }
                catch (Exception ex) {}

                usbConnection = null;
                usbInterface = null;
                readEndpoint = null;
                writeEndpoint = null;
            }
        }

        //
        // PRIVATE
        //

        private void messageWrite(Message msg) {
            int msg_size = msg.getSerializedSize();
            String msg_name = msg.getClass().getSimpleName();
            int msg_id = TrezorMessage.MessageType.valueOf("MessageType_" + msg_name).getNumber();
            ToastUtils.showNormal(String.format("messageWrite: Got message: %s (%d bytes)", msg_name, msg_size), Toast.LENGTH_LONG);

            ByteBuffer data = ByteBuffer.allocate(msg_size + 1024); // 32768);
            data.put((byte) '#');
            data.put((byte) '#');
            data.put((byte) ((msg_id >> 8) & 0xFF));
            data.put((byte) (msg_id & 0xFF));
            data.put((byte) ((msg_size >> 24) & 0xFF));
            data.put((byte) ((msg_size >> 16) & 0xFF));
            data.put((byte) ((msg_size >> 8) & 0xFF));
            data.put((byte) (msg_size & 0xFF));
            data.put(msg.toByteArray());
            while (data.position() % 63 > 0) {
                data.put((byte) 0);
            }
            UsbRequest request = new UsbRequest();
            request.initialize(usbConnection, writeEndpoint);
            int chunks = data.position() / 63;
            ToastUtils.showNormal(String.format("messageWrite: Writing %d chunks", chunks), Toast.LENGTH_LONG);

            data.rewind();
            for (int i = 0; i < chunks; i++) {
                byte[] buffer = new byte[64];
                buffer[0] = (byte) '?';
                data.get(buffer, 1, 63);
                request.queue(ByteBuffer.wrap(buffer), 64);
                usbConnection.requestWait();
            }
        }

        private Message messageRead() throws InvalidProtocolBufferException {
            ByteBuffer data = null;//ByteBuffer.allocate(32768);
            ByteBuffer buffer = ByteBuffer.allocate(64);
            UsbRequest request = new UsbRequest();
            request.initialize(usbConnection, readEndpoint);
            TrezorMessage.MessageType type;
            int msg_size;
            int invalidChunksCounter = 0;

            for (; ; ) {
                request.queue(buffer, 64);
                usbConnection.requestWait();
                byte[] b = buffer.array();
                ToastUtils.showNormal(String.format("messageRead: Read chunk: %d bytes", b.length), Toast.LENGTH_LONG);

                if (b.length < 9 || b[0] != (byte) '?' || b[1] != (byte) '#' || b[2] != (byte) '#') {
                    if (invalidChunksCounter++ > 5)
                        throw new InvalidProtocolBufferException("messageRead: too many invalid chunks");
                    continue;
                }
                if (b[0] != (byte) '?' || b[1] != (byte) '#' || b[2] != (byte) '#')
                    continue;

                type = TrezorMessage.MessageType.valueOf((((int)b[3] & 0xFF) << 8) + ((int)b[4] & 0xFF));
                msg_size = (((int)b[5] & 0xFF) << 24)
                        + (((int)b[6] & 0xFF) << 16)
                        + (((int)b[7] & 0xFF) << 8)
                        + ((int)b[8] & 0xFF);
                data = ByteBuffer.allocate(msg_size + 1024);
                data.put(b, 9, b.length - 9);
                break;
            }

            invalidChunksCounter = 0;

            while (data.position() < msg_size) {
                request.queue(buffer, 64);
                usbConnection.requestWait();
                byte[] b = buffer.array();
                ToastUtils.showNormal(String.format("messageRead: Read chunk (cont): %d bytes", b.length), Toast.LENGTH_LONG);

                if (b[0] != (byte) '?') {
                    if (invalidChunksCounter++ > 5)
                        throw new InvalidProtocolBufferException("messageRead: too many invalid chunks (2)");
                    continue;
                }
                data.put(b, 1, b.length - 1);
            }

            byte[] msgData = Arrays.copyOfRange(data.array(), 0, msg_size);

            ToastUtils.showNormal(String.format("parseMessageFromBytes: Parsing %s (%d bytes):", type, msgData.length), Toast.LENGTH_LONG);
            return parseMessageFromBytes(type, msgData);
        }
    }

    public TrezorDevice getTrezorDevice() {
        return trezorDevice;
    }
}
