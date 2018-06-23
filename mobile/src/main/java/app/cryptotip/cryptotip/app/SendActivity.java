package app.cryptotip.cryptotip.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.gani.lib.logging.GLog;
import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.Ui;

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

import static app.cryptotip.cryptotip.app.Home.WALLET_FILE_PATH;

public class SendActivity extends GActivity {





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
                    }
                });
                builder.create().show();
            }
        });
    }


    private AlertDialog createTransactionAmoutDialog(final String address){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trasaction Amount");
        final EditText inputField = new EditText(this);
        inputField.setHint("enter tip amount");
        inputField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(inputField);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncSendTask(SendActivity.this).execute(address, inputField.getText().toString(), DbMap.get(WALLET_FILE_PATH));
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
            context.generateERC681Url(address, null, amount);

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

            }
            catch (InterruptedException e) {
                GLog.e(getClass(), "Failed", e);
            } catch (ExecutionException e) {
                GLog.e(getClass(), "Failed", e);
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
        GLog.e(getClass(), "ehter url === \n" + url);
        return url;
    }
}