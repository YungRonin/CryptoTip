package app.cryptotip.cryptotip.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.view.GTextView;

import app.cryptotip.cryptotip.app.components.BorderFactory;
import app.cryptotip.cryptotip.app.components.QrScanner;
import app.cryptotip.cryptotip.app.view.MyScreenView;

public class ReceiverAddressActivity extends GActivity {
    private QrScanner scanner;
    private LinearLayout layout;
    private TextInputEditText addressInput;
    private GTextView sendButton;
    public static String RECIEVER_ADDRESS = "recieverAddress";

    public Intent intent(Context context) {
        return new Intent(context, this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateForScreen(savedInstanceState, new MyScreenView(this));
        layout = (LinearLayout) View.inflate(this, R.layout.receiver_layout, null);
        addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addressInput = layout.findViewById(R.id.address_input_field);
        addressInput.setHint("test");
        sendButton = layout.findViewById(R.id.start_transaction_button);
        sendButton.setBackground(BorderFactory.createBorders(this.getResources().getColor(android.R.color.white, null), this.getResources().getColor(android.R.color.black, null), 3,3,3,3));
        sendButton.bold();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = SendActivity.intent(ReceiverAddressActivity.this);
                intent.putExtra(RECIEVER_ADDRESS, addressInput.getText().toString());
                startActivity(intent, null);
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
