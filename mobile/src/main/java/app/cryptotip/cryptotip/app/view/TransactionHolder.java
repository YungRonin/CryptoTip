package app.cryptotip.cryptotip.app.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gani.lib.ui.alert.ToastUtils;
import com.gani.lib.ui.view.GTextView;

import app.cryptotip.cryptotip.app.R;
import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.transaction.Transaction;

import static app.cryptotip.cryptotip.app.Home.SELECTED_FIAT_CURRENCY;


public class TransactionHolder extends RecyclerView.ViewHolder{

    private GTextView fiatValueView;
    private GTextView ethValueView;
    private GTextView toView;
    private GTextView transactionHashView;

    public TransactionHolder(View template, final Context context){
        super(template);

        toView = template.findViewById(R.id.to_text_view);
        ethValueView = template.findViewById(R.id.eth_value_text_view);
        fiatValueView = template.findViewById(R.id.fiat_value_text_view);
        transactionHashView = template.findViewById(R.id.transaction_hash_view);
        LinearLayout transactionDetailsLayout = template.findViewById(R.id.transaction_details_layout);
        transactionDetailsLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                String txHash = transactionHashView.getText().toString();
                String[] split = txHash.split(" ");
                ClipData content = ClipData.newPlainText("clip", split[1]);
                clipboard.setPrimaryClip(content);
                ToastUtils.showNormal("Transaction Hash copied to Clipboard", Toast.LENGTH_LONG);
                return false;
            }
        });
    }

    public void setDetails(Transaction transaction){
        toView.text("To: ".concat(transaction.getTo())).bold();
        ethValueView.text("ETH: ".concat(transaction.getEthValue())).bold();
        transactionHashView.text("TxHash: ".concat(transaction.getTransactionHash())).bold();
        fiatValueView.text(DbMap.get(SELECTED_FIAT_CURRENCY).concat(": " + transaction.getFiatValue())).bold();
    }
}
