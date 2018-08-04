package app.cryptotip.cryptotip.app.transaction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.cryptotip.cryptotip.app.R;
import app.cryptotip.cryptotip.app.view.TransactionHolder;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {


    private Context context;
    private ArrayList<Transaction> transactions;
    public TransactionAdapter(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_layout, parent, false);
        return new TransactionHolder(view, context);
    }

    @Override
    public void onBindViewHolder(TransactionHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.setDetails(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
