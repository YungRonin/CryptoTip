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
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendRawTransaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.transaction.Transaction;
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
    private boolean fiatSelected;
    private Double cryptoAmount;
    private Double fiatValue;
    private Home.TrezorDevice trezorDevice;


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

        final String currency = DbMap.get(SELECTED_FIAT_CURRENCY);
        final String price = DbMap.get(FIAT_PRICE);
        priceTextView.setText("1 ".concat(DbMap.get(SELECTED_CRYPTO_CURRENCY).concat(" = " + price + " " + currency)));

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

        cryptoInputLayout.setHint(DbMap.get(SELECTED_CRYPTO_CURRENCY));
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
                                createErc20TransactionAmoutDialog(from, to, cryptoAmount.toString(), contractAddress).show();
                            }
                        } catch (CipherException e) {
                            Log.e("fail", "exception " + e);
                        } catch (IOException e) {
                            Log.e("fail", "exception " + e);
                        }
                    }
                    else{
                        if(!SendActivity.this.isDestroyed() || !SendActivity.this.isFinishing()) {
                            createTransactionAmoutDialog(addressTextView.getText().toString(), String.valueOf(cryptoAmount), currency.concat(" : " + fiatValue), "ETH : ".concat(cryptoAmount.toString())).show();
                        }
                    }
                }
            }
        });
    }

    private void detectTransactionStatus(){

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

            return builder.create();
    }

    private void accessTrezor() {

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
                    setUpTrezor(usbDevice, usbManager);
                }
            }
        }

        if(deviceWithoutPermission != null){
            setUpTrezor(deviceWithoutPermission, usbManager);
        }
    }

    private void setUpTrezor(UsbDevice device, UsbManager usbManager){
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
            }
        }
    }

    private void sendFromTrezor(String from, String amount, String to){
        Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));  // defaults to http://localhost:8545/

        BigDecimal weiValue = Convert.toWei(amount, Convert.Unit.ETHER);
        try {
            //todo getnoonce is asyc wait for result before continuing execution
//            org.web3j.protocol.core.methods.request.Transaction tx = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(from, getNonce(), GAS_PRICE, GAS_LIMIT, to, weiValue.toBigInteger());
            RawTransaction rawtrans = RawTransaction.createEtherTransaction(getNonce(from, web3), GAS_PRICE, GAS_LIMIT, to, weiValue.toBigInteger());

            byte[] trans = TransactionEncoder.encode(rawtrans);

            TrezorMessage.Initialize req = TrezorMessage.Initialize.newBuilder().build();

            try {
                trezorDevice.sendMessage(req);

                TrezorMessage.EthereumTxRequest ethReq = TrezorMessage.EthereumTxRequest.parseFrom(trans);
            }
            catch (InvalidProtocolBufferException e){
                ToastUtils.showNormal("BufferException : ".concat(e.getMessage()), Toast.LENGTH_LONG);
            }
        }
        catch(InterruptedException e){
            ToastUtils.showNormal("Interupted : ".concat(e.getMessage()), Toast.LENGTH_LONG);
        }
        catch(ExecutionException e){
            ToastUtils.showNormal("Execution Failed : ".concat(e.getMessage()), Toast.LENGTH_LONG);
        }
    }

    protected BigInteger getNonce(String address, Web3j web3j) throws InterruptedException, ExecutionException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                address, DefaultBlockParameterName.LATEST).sendAsync().get();

        return ethGetTransactionCount.getTransactionCount();
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
}
