package app.cryptotip.cryptotip.app;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gani.lib.logging.GLog;
import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.Ui;
import com.gani.lib.ui.alert.ToastUtils;
import com.gani.lib.ui.view.GTextView;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.satoshilabs.trezor.lib.TrezorManager;
import com.satoshilabs.trezor.lib.protobuf.TrezorMessage;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.view.MyScreenView;

import static app.cryptotip.cryptotip.app.Home.FIAT_PRICE;
import static app.cryptotip.cryptotip.app.Home.SELECTED_CRYPTO_CURRENCY;
import static app.cryptotip.cryptotip.app.Home.SELECTED_FIAT_CURRENCY;
import static app.cryptotip.cryptotip.app.Home.WALLET_FILE_PATH;
import static app.cryptotip.cryptotip.app.ReceiverAddressActivity.RECIEVER_ADDRESS;
import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

//todo switch to fragment
public class SendActivity extends GActivity {
    private LinearLayout layout;
    private LinearLayout createLayout;
    private LinearLayout confirmlayout;
    private GTextView addressTextView;
    private GTextView priceTextView;
    private TextInputLayout cryptoInputLayout;
    private TextInputEditText cryptoAmountEditText;
    private TextInputLayout fiatInputLayout;
    private TextInputEditText fiatAmountEditText;
    private GTextView sendButton;
    private CardView trezorCardView;
    private GTextView trezorButtonTextView;
    private boolean fiatSelected;
    private Double cryptoAmount;
    private Double fiatValue;
    private Home.TrezorDevice trezorDevice;
    private String trezorPublicKey;

    public static Intent intent(Context context) {
        return new Intent(context, SendActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateForScreen(savedInstanceState, new MyScreenView(this));
        fiatSelected = false;
        layout = (LinearLayout) View.inflate(this, R.layout.send_layout, null);
        createLayout = layout.findViewById(R.id.create_layout);
        confirmlayout = layout.findViewById(R.id.confirm_layout);
        confirmlayout.setVisibility(View.GONE);
        addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        priceTextView = layout.findViewById(R.id.current_price_view);
        cryptoAmountEditText = layout.findViewById(R.id.crypto_amount_input);
        cryptoInputLayout = layout.findViewById(R.id.crypto_input_layout);
        fiatAmountEditText = layout.findViewById(R.id.fiat_amount_input);
        fiatInputLayout = layout.findViewById(R.id.fiat_input_layout);
        addressTextView = layout.findViewById(R.id.address_text_view);
        addressTextView.setText(getIntent().getExtras().getString(RECIEVER_ADDRESS));
        sendButton = layout.findViewById(R.id.send_transaction_button);
        trezorCardView = layout.findViewById(R.id.trezor_card_view);
        trezorButtonTextView = layout.findViewById(R.id.trezor_send_transaction_button);

        final String currency = DbMap.get(SELECTED_FIAT_CURRENCY);
        final String price = DbMap.get(FIAT_PRICE);
        priceTextView.setText("1 ".concat(DbMap.get(SELECTED_CRYPTO_CURRENCY)));//.concat(" = " + price + " " + currency)));

        fiatInputLayout.setHint(currency);
        fiatAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!fiatSelected && count > 0) {
                    fiatValue = Double.valueOf(s.toString());
                    Double fiatPrice = Double.valueOf(price);
                    cryptoAmount = fiatValue / fiatPrice;
                    cryptoAmountEditText.setText(String.valueOf(cryptoAmount));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fiatAmountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fiatSelected = false;
                fiatAmountEditText.performClick();
                return false;
            }
        });

        String selectedCryptoCurrency = DbMap.get(SELECTED_CRYPTO_CURRENCY);
        cryptoInputLayout.setHint(selectedCryptoCurrency);
        cryptoAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(fiatSelected && count > 0) {
                    cryptoAmount = Double.valueOf(s.toString());
                    Double fiatPrice = Double.valueOf(price);
                    fiatValue = cryptoAmount * fiatPrice;
                    fiatAmountEditText.setText(String.valueOf(fiatValue));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cryptoAmountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fiatSelected = true;
                cryptoAmountEditText.performClick();
                return false;
            }
        });


        sendButton.text("Confirm & Send");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cryptoAmount != null && cryptoAmount > 0){

                    InputMethodManager imm = (InputMethodManager) SendActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    }

                    String selectedCrypto = DbMap.get(SELECTED_CRYPTO_CURRENCY);
                    if(selectedCrypto != null && !selectedCrypto.contentEquals("ETH")){

                        //todo refactor
                        try {
                            Credentials creds = WalletUtils.loadCredentials("atestpasswordhere", DbMap.get(WALLET_FILE_PATH));
                            String from = creds.getAddress();
                            String to = addressTextView.getText().toString();
                            String contractAddress = Home.getContractAddress(selectedCrypto);
                            if(!SendActivity.this.isDestroyed() || !SendActivity.this.isFinishing()){
                                //createErc20TransactionAmoutDialog(from, to, cryptoAmount.toString(), contractAddress).show();
                            }
                        } catch (CipherException e) {
                            Log.e("fail", "exception " + e);
                        } catch (IOException e) {
                            Log.e("fail", "exception " + e);
                        }
                    }
                    else{
                        if(!SendActivity.this.isDestroyed() || !SendActivity.this.isFinishing()) {
                            //createTransactionAmoutDialog(addressTextView.getText().toString(), String.valueOf(cryptoAmount), currency.concat(" : " + fiatValue), "ETH : ".concat(cryptoAmount.toString())).show();
                        }
                    }
                }
            }
        });

        trezorButtonTextView.text("Send with Trezor");
        trezorCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cryptoAmount != null && cryptoAmount > 0){

                    InputMethodManager imm = (InputMethodManager) SendActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    }

                    String selectedCrypto = DbMap.get(SELECTED_CRYPTO_CURRENCY);
                    if(selectedCrypto != null && !selectedCrypto.contentEquals("ETH")){

                        //todo add erc20 support
                    }
                    else{
                        if(!SendActivity.this.isDestroyed() || !SendActivity.this.isFinishing()) {
                            initTrezor(addressTextView.getText().toString(), cryptoAmount.toString());
                        }
                    }
                }
            }
        });
    }

    private void handleTransactionReceipt(final TransactionReceipt transactionReceipt){
        Ui.run(new Runnable() {
            @Override
            public void run() {
                String hash = String.valueOf("Hash : " + transactionReceipt.getTransactionHash());
                String from = String.valueOf("From : " + transactionReceipt.getFrom() + "\n");
                String to = String.valueOf("To : " + transactionReceipt.getTo() + "\n");
                AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
                builder.setTitle("Transaction Receipt");
                builder.setMessage(String.valueOf(from + to + hash));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SendActivity.this.finish();
                    }
                });
                builder.create().show();
            }
        });
    }


    private AlertDialog createTransactionAmoutDialog(final String address, final String ethAmount, String fiatAmountString, String ethAmountString){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Trasaction Amount");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50,50,50,50);
        GTextView ethAmountField = new GTextView(this);
        ethAmountField.text(ethAmountString).bold();
        layout.addView(ethAmountField);
        GTextView fiatAmountField = new GTextView(this);
        fiatAmountField.text(fiatAmountString).bold();
        layout.addView(fiatAmountField);
        GTextView addressTextView = new GTextView(this);
        addressTextView.text("To : ".concat(address)).bold();
        layout.addView(addressTextView);
        builder.setView(layout);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncSendTask(SendActivity.this).execute(address, ethAmount, DbMap.get(WALLET_FILE_PATH));
//                confirmlayout.setVisibility(View.VISIBLE);
                createLayout.setVisibility(View.GONE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private AlertDialog createErc20TransactionAmoutDialog(final String from, final String to, final String amount, final String contractAddress){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Trasaction Amount");
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);
            GTextView tokenAmount = new GTextView(this);
            tokenAmount.text(amount.concat(" " + DbMap.get(SELECTED_CRYPTO_CURRENCY))).bold(); //todo avoid using room to get value
            layout.addView(tokenAmount);
            GTextView addressTextView = new GTextView(this);
            addressTextView.text("To : ".concat(to)).bold();
            layout.addView(addressTextView);
            builder.setView(layout);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AsyncErc20SendTask(SendActivity.this).execute(from, to, contractAddress, amount, DbMap.get(WALLET_FILE_PATH));
//                    confirmlayout.setVisibility(View.VISIBLE);
                    createLayout.setVisibility(View.GONE);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            return builder.create();
    }

    private void createTrezorTransactionAmoutDialog(final String to, final String amount, final BigInteger noOnce){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Trasaction Amount");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        GTextView tokenAmount = new GTextView(this);
        tokenAmount.text(amount.concat(" " + DbMap.get(SELECTED_CRYPTO_CURRENCY))).bold(); //todo avoid using room to get value
        layout.addView(tokenAmount);
        GTextView addressTextView = new GTextView(this);
        addressTextView.text("To : ".concat(to)).bold();
        layout.addView(addressTextView);
        builder.setView(layout);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendFromTrezor(to, amount, noOnce);
                confirmlayout.setVisibility(View.VISIBLE);
                createLayout.setVisibility(View.GONE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Ui.run(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    public static class AsyncSendTask extends AsyncTask<String, String, Exception> {
        SendActivity context;

        public AsyncSendTask(SendActivity context){
            super();
            this.context = context;
        }

        @Override
        protected Exception doInBackground(String... params) {


            return send(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e != null && context != null) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public Exception send(String address, String amount, String walletPath) {

            //todo allow the user to switch between generating an erc681 intent, sending straight from app or hardware wallet
            try {
                context.generateERC681Url(address, null, amount);
                return null;
            }
            catch (ActivityNotFoundException anfE) {

                Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));  // defaults to http://localhost:8545/
                try {
                    Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
                    try {

                        Credentials credentials = WalletUtils.loadCredentials("atestpasswordhere", walletPath);

                        try {
                            TransactionReceipt transactionReceipt = Transfer.sendFunds(
                                    web3, credentials, address,
                                    new BigDecimal(amount), Convert.Unit.ETHER)
                                    .send();

                            context.handleTransactionReceipt(transactionReceipt);
                            GLog.t(getClass(), "TxHash: " + transactionReceipt.getTransactionHash());
                        } catch (Exception e) {
                            GLog.e(getClass(), "Failed", e);
                            return e;
                        }

                    } catch (IOException e) {
                        GLog.e(getClass(), "Failed", e);
                    } catch (CipherException e) {
                        GLog.e(getClass(), "Failed", e);
                    }

                } catch (InterruptedException e) {
                    GLog.e(getClass(), "Failed", e);
                } catch (ExecutionException e) {
                    GLog.e(getClass(), "Failed", e);
                }
                return null;
            }
        }
    }

    public static class AsyncErc20SendTask extends AsyncTask<String, String, Exception> {
        SendActivity context;

        public AsyncErc20SendTask(SendActivity context){
            super();
            this.context = context;
        }

        @Override
        protected Exception doInBackground(String... params) {


            return sendErc20Token(params[0], params[1], params[2], params[3], params[4]);
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e != null && context != null) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public Exception sendErc20Token(String from, String to, String contractAddress, String amount, String walletPath) {


            BigDecimal weiValue = Convert.toWei(amount, Convert.Unit.ETHER);

            final Function function = new Function(
                    "transfer",
                    Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(to),
                            new org.web3j.abi.datatypes.generated.Uint256(weiValue.toBigInteger())),
                    Collections.<TypeReference<?>>emptyList());

            String encodedFunction = FunctionEncoder.encode(function);

            Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));

            try {
                EthGetTransactionCount transactionCount = web3
                        .ethGetTransactionCount(from, DefaultBlockParameterName.LATEST)
                        .sendAsync()
                        .get();

                TransactionReceiptProcessor processor = new PollingTransactionReceiptProcessor(web3, 1000, 200);

                BigInteger nonce = transactionCount.getTransactionCount();

                RawTransaction rawTransaction = RawTransaction.createTransaction(
                        nonce,
                        GAS_PRICE,
                        GAS_LIMIT,
                        contractAddress,
                        encodedFunction);

                Credentials credentials = WalletUtils.loadCredentials("atestpasswordhere", walletPath);

                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);


                EthSendTransaction transactionResponse = web3.ethSendRawTransaction(hexValue)
                        .sendAsync()
                        .get();

                String transactionHash = transactionResponse.getTransactionHash();

                try {
                    TransactionReceipt receipt = processor.waitForTransactionReceipt(transactionHash);
                    context.handleTransactionReceipt(receipt);
                }
                catch (TransactionException e){
                    ToastUtils.showNormal("Transaction Failed ".concat(e.getMessage()));
                }

                return null;
            }
            catch(ExecutionException e){
                GLog.e(getClass(), "concurrent execution exception " + e);
            }
            catch (InterruptedException e){
                GLog.e(getClass(), "interrupted exception " + e);
            }
            catch(IOException e){
                GLog.e(getClass(), "IO exception " + e);
            }
            catch (CipherException e){
                GLog.e(getClass(), "Cipher exception " + e);
            }
            return null;
        }
    }

    public String generateERC681Url(String address, Long chainId, String value){

        String url = "ethereum:";

        if (address != null) {
            url = url.concat(address);
            //url = url.concat("?");
        }

//        if (chainId != null && chainId != 1L) {
//            url = url.concat(chainId);
//        }

        url = url.concat("?amount=" + value);

//        url = url.concat("?gas=45&");
//
//        if(value != null){
//            url = url.concat("?value="+value);
//        }



        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        this.startActivity(intent);
        return url;
    }

    private void initTrezor(String to, String amount) {

        UsbManager usbManager = (UsbManager) this.getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        UsbDevice deviceWithoutPermission = null;

        for (UsbDevice usbDevice : deviceList.values()) {
            // check if the device is TREZOR
            Boolean deviceIsTrezor = Home.isDeviceTrezor(usbDevice);
            if (deviceIsTrezor == null || !deviceIsTrezor){

            }
            else{
                if (!usbManager.hasPermission(usbDevice)) {
                    usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(this, 0, new Intent(TrezorManager.UsbPermissionReceiver.ACTION), 0));
                    deviceWithoutPermission = usbDevice;
                }
                else{
                    setUpTrezor(usbDevice, usbManager, to, amount);
                }
            }
        }

        if(deviceWithoutPermission != null){
            setUpTrezor(deviceWithoutPermission, usbManager, to, amount);
        }
    }

    private void setUpTrezor(UsbDevice device, UsbManager usbManager, String to, String amount){
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
                trezorDevice = new Home.TrezorDevice(device.getDeviceName(), connection.getSerial(), connection, usbInterface, readEndpoint, writeEndpoint);
                setTrezorPublicKey(to, amount);
            }
        }
    }

    private void setTrezorPublicKey(String to, String amount){
        if(trezorDevice != null) {


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

                    if (!map.isEmpty()) {

                        ByteString ethAddress = (ByteString) map.entrySet().iterator().next().getValue();

                        String hex = "";
                        for (int j = 0; j < ethAddress.size(); j++) {
                            hex += String.format("%02x", ethAddress.byteAt(j) & 0xFF);
                        }

                        trezorPublicKey = "0x".concat(hex);
                        new TezorAsyncTask(this).execute(trezorPublicKey, to, amount);
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                ToastUtils.showNormal("Failed to retrieve address ".concat(e.getMessage()), Toast.LENGTH_LONG);
            }
        }
    }

    private void sendFromTrezor(String to, String amount, BigInteger noOnce) {

        if (noOnce != null) {

//            new android.support.v7.app.AlertDialog.Builder(this).setTitle("No Once Set")
//                    .setMessage(noOnce.toString())
//                    .create()
//                    .show();

//            String message = "to ".concat(toByteString + "\n from ")
//                    .concat(trezorPublicKey + "\n weiValue")
//                    .concat(weiValueByteString.toString() + "\n gas Price ")
//                    .concat(gasPriceByteString.toString() + "\n gas Limit ")
//                    .concat(gasLimitByteString.toString() + "\n noOnce ")
//                    .concat(noOnceByteString.toString());


            try {
//                TrezorMessage.Initialize req = TrezorMessage.Initialize.newBuilder().build();
//                trezorDevice.sendMessage(req);
//
//                TrezorMessage.EthereumTxAck ack = TrezorMessage.EthereumTxAck.newBuilder().build();
//                trezorDevice.sendMessage(ack);

                BigDecimal weiValue = Convert.toWei(amount, Convert.Unit.ETHER);
                ByteString toByteString = ByteString.copyFrom(to.getBytes());
                ByteString weiValueByteString = ByteString.copyFrom(weiValue.toBigInteger().toByteArray());
                ByteString gasPriceByteString = ByteString.copyFrom(GAS_PRICE.toByteArray());
                ByteString gasLimitByteString = ByteString.copyFrom(GAS_LIMIT.toByteArray());
                ByteString noOnceByteString = ByteString.copyFrom(noOnce.toByteArray());

                TrezorMessage.EthereumSignTx.Builder builder = TrezorMessage.EthereumSignTx.newBuilder();
                builder.clear();

                TrezorMessage.EthereumSignTx ethReq2 = builder
                        .setTo(toByteString)
                        .setValue(weiValueByteString)
                        .setGasLimit(gasLimitByteString)
                        .setGasPrice(gasPriceByteString)
                        .setNonce(noOnceByteString)
                        .setChainId(4) //rinkerby
//                        .setDataInitialChunk(ByteString.EMPTY)
//                        .setDataLength(0)
                        .setTxType(1)
                        .build();

//                ByteString gasLimit = ethReq2.getGasLimit();
//                ByteString gasPrice = ethReq2.getGasPrice();
//                int size = ethReq2.getSerializedSize();
//                int gasSize = gasLimit.size() + gasPrice.size();
//                String anotherMessage = "gaslimit : ";
//                anotherMessage = gasLimit.toStringUtf8().concat("\n gas price : ")
//                        .concat(gasPrice.toStringUtf8()).concat("\n request size")
//                        .concat(String.valueOf(size)).concat("\n gas Size : ")
//                        .concat(String.valueOf(gasSize));

                Message result = trezorDevice.sendMessage(ethReq2);
//                Iterator<Map.Entry<Descriptors.FieldDescriptor, Object>> it = result.getAllFields().entrySet().iterator();
//                String aString = "";
//                while (it.hasNext()){
//                    Map.Entry<Descriptors.FieldDescriptor, Object> entry = it.next();
//                    aString = aString.concat(entry.getKey().getFullName()).concat("\n" + entry.getValue().toString());
//                }

                new android.support.v7.app.AlertDialog.Builder(this).setTitle("Success")
                        .setMessage(result.toByteString().toStringUtf8())
                        .create()
                        .show();
            }
            catch (InvalidProtocolBufferException e) {
                ToastUtils.showNormal("BufferException : ".concat(e.getMessage()), Toast.LENGTH_LONG);

//                ToastUtils.showNormal("BufferException : ".concat(e.getMessage()), Toast.LENGTH_LONG);
//                new android.support.v7.app.AlertDialog.Builder(this).setTitle("failure")
//                        .setMessage(message)
//                        .create()
//                        .show();
            }
        }
        else{
            ToastUtils.showNormal("no once is null farrrk", Toast.LENGTH_LONG);
        }
    }

    public static class TezorAsyncTask extends AsyncTask<String, String, Exception> {
        SendActivity context;

        public TezorAsyncTask(SendActivity context){
            super();
            this.context = context;
        }

        @Override
        protected Exception doInBackground(String... params) {


            return doAsyncTrezorThings(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e != null && context != null) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public Exception doAsyncTrezorThings(String from, String to, String amount) {

            try{
                Web3j web3j = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));  // defaults to http://localhost:8545/

                EthGetTransactionCount count = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).send();
                BigInteger transCount = count.getTransactionCount();
                if(transCount.intValue() < 1){
                    transCount = new BigInteger("1");
                }

                context.createTrezorTransactionAmoutDialog(to, amount, transCount);
            }
            catch (IOException e){
                ToastUtils.showNormal("IO exception : ".concat(e.getMessage()), Toast.LENGTH_LONG);
            }
            return null;
        }
    }
}
