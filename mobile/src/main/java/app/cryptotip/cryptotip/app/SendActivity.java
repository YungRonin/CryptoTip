package app.cryptotip.cryptotip.app;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gani.lib.logging.GLog;
import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.Ui;
import com.gani.lib.ui.view.GTextView;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.view.MyScreenView;

import static app.cryptotip.cryptotip.app.Home.FIAT_PRICE;
import static app.cryptotip.cryptotip.app.Home.WALLET_FILE_PATH;
import static app.cryptotip.cryptotip.app.ReceiverAddressActivity.RECIEVER_ADDRESS;
import static app.cryptotip.cryptotip.app.SettingsActivity.SELECTED_CURRENCY;

public class SendActivity extends GActivity {
    private LinearLayout layout;
    private LinearLayout createLayout;
    private LinearLayout confirmlayout;
    private GTextView addressTextView;
    private GTextView priceTextView;
    private TextInputLayout ethInputLayout;
    private TextInputEditText ethAmountEditText;
    private TextInputLayout fiatInputLayout;
    private TextInputEditText fiatAmountEditText;
    private GTextView sendButton;
    private boolean fiatSelected;
    private Double ethAmount;
    private Double fiatValue;

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
        ethAmountEditText = layout.findViewById(R.id.eth_amount_input);
        ethInputLayout = layout.findViewById(R.id.eth_input_layout);
        fiatAmountEditText = layout.findViewById(R.id.fiat_amount_input);
        fiatInputLayout = layout.findViewById(R.id.fiat_input_layout);
        addressTextView = layout.findViewById(R.id.address_text_view);
        addressTextView.setText(getIntent().getExtras().getString(RECIEVER_ADDRESS));
        sendButton = layout.findViewById(R.id.send_transaction_button);

        final String currency = DbMap.get(SELECTED_CURRENCY);
        final String price = DbMap.get(FIAT_PRICE);
        priceTextView.setText("1 ETH = ".concat(price + " " + currency));

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
                    ethAmount = fiatValue / fiatPrice;
                    ethAmountEditText.setText(String.valueOf(ethAmount));
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

        ethInputLayout.setHint("ETH");
        ethAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(fiatSelected && count > 0) {
                    ethAmount = Double.valueOf(s.toString());
                    Double fiatPrice = Double.valueOf(price);
                    fiatValue = ethAmount * fiatPrice;
                    fiatAmountEditText.setText(String.valueOf(fiatValue));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ethAmountEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fiatSelected = true;
                ethAmountEditText.performClick();
                return false;
            }
        });


        sendButton.text("Confirm & Send");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ethAmount != null && ethAmount > 0){
                    createTransactionAmoutDialog(addressTextView.getText().toString(), String.valueOf(ethAmount), currency.concat(" : " + fiatValue), "ETH : ".concat(ethAmount.toString())).show();
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

            //todo allow the user to switch between generating a erc681 intent and sending straight from app
            try {
                context.generateERC681Url(address, null, amount);
                return null;
            }
            catch (ActivityNotFoundException anfE) {

                Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/tQmR2iidoG7pjW1hCcCf"));  // defaults to http://localhost:8545/
                try {
                    Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
//            Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
                    String clientVersion = web3ClientVersion.getWeb3ClientVersion();
                    GLog.t(getClass(), "CLIENT: " + clientVersion);

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
        GLog.e(getClass(), "ehter url === \n" + url);
        return url;
    }
}
