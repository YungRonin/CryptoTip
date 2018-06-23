package app.cryptotip.cryptotip.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gani.lib.logging.GLog;
import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.Ui;
import com.google.android.gms.vision.text.Line;

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

import app.cryptotip.cryptotip.app.components.BorderFactory;
import app.cryptotip.cryptotip.app.components.QrScanner;
import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.view.MyScreenView;

import static app.cryptotip.cryptotip.app.Home.WALLET_FILE_PATH;

public class ReceiverAddressActivity extends GActivity {
    private QrScanner scanner;
    private LinearLayout layout;
    private EditText addressInput;
    private TextView sendButton;

    public Intent intent(Context context) {
        return new Intent(context, this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateForScreen(savedInstanceState, new MyScreenView(this));
        layout = (LinearLayout) View.inflate(this, R.layout.receiver_layout, null);
        addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addressInput = layout.findViewById(R.id.address_input_field);
        sendButton = layout.findViewById(R.id.start_transaction_button);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        scanner = new QrScanner(this, layout);
        scanner.init();
        sendButton.setBackground(BorderFactory.createBorders(this.getResources().getColor(android.R.color.white, null), this.getResources().getColor(android.R.color.black, null), 3,3,3,3));

    }
}
