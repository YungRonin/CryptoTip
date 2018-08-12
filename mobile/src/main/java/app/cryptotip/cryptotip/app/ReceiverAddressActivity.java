package app.cryptotip.cryptotip.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.alert.ToastUtils;
import com.gani.lib.ui.view.GTextView;

import org.web3j.crypto.WalletUtils;

import app.cryptotip.cryptotip.app.components.BorderFactory;
import app.cryptotip.cryptotip.app.components.QrScanner;
import app.cryptotip.cryptotip.app.view.MyScreenView;

public class ReceiverAddressActivity extends GActivity {
    private QrScanner scanner;
    private LinearLayout layout;
    private TextInputLayout addressInputLayout;
    private TextInputEditText addressInput;
    private GTextView sendButton;
    private boolean validAddressEntered;
    public static String RECIEVER_ADDRESS = "recieverAddress";

    public Intent intent(Context context) {
        return new Intent(context, this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateForScreen(savedInstanceState, new MyScreenView(this));
        validAddressEntered = false;
        layout = (LinearLayout) View.inflate(this, R.layout.receiver_layout, null);
        addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sendButton = layout.findViewById(R.id.start_transaction_button);
        sendButton.text("Please enter valid address").bold();
        addressInputLayout = layout.findViewById(R.id.address_input_layout);
        addressInputLayout.setHint("Enter receiver address here");
        addressInput = layout.findViewById(R.id.address_input_field);
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validAddressEntered = WalletUtils.isValidAddress(s.toString());
                if(validAddressEntered){
                    sendButton.text("Start Transaction").bold();
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = SendActivity.intent(ReceiverAddressActivity.this);
                            intent.putExtra(RECIEVER_ADDRESS, addressInput.getText().toString());
                            startActivity(intent, null);
                        }
                    });
                }
                else{
                    sendButton.text("Please enter valid address").bold();
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ToastUtils.showNormal("Address entered is not a valid Ethereum address.");
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        scanner = new QrScanner(this, layout);
        scanner.init();
    }
}
