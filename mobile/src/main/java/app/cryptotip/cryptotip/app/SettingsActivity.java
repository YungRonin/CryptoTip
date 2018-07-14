package app.cryptotip.cryptotip.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gani.lib.screen.GActivity;
import com.gani.lib.ui.view.GTextView;

import app.cryptotip.cryptotip.app.components.BorderFactory;
import app.cryptotip.cryptotip.app.database.DbMap;
import app.cryptotip.cryptotip.app.view.MyScreenView;

public class SettingsActivity extends GActivity {
    private String selectedCurrency;
    public static final String SELECTED_CURRENCY = "selectedCurrency";

    public Intent intent(Context context) {
        return new Intent(context, this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateForScreen(savedInstanceState, new MyScreenView(this));
        addContentView(View.inflate(this, R.layout.settings_layout, null), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedinstaceState) {
        super.onPostCreate(savedinstaceState);
        LinearLayout layout = findViewById(R.id.settings_layout);
        GTextView currencyTview = new GTextView(this);
        layout.addView(currencyTview);
        currencyTview.setText("Currency");
        currencyTview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        currencyTview.setTextSize(25.0f);
        currencyTview.gravity(Gravity.CENTER);
        currencyTview.setPadding(0,20,0,20);
        currencyTview.setBackgroundDrawable(BorderFactory.createBorders(getResources().getColor(android.R.color.white, null), getResources().getColor(android.R.color.black, null), 0,3,0,3));
        currencyTview.bold();
        currencyTview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCurrencySelectorDialog().show();
            }
        });
    }

    private AlertDialog createCurrencySelectorDialog(){
        final CharSequence[] currencyCodes = {"AUD", "USD", "EUR", "GBP", "INR", "MYR", "IDR", "RUB", "NZD", "THB", "SGD", "JPY", "CNY", "CAD", "KRW", "SAR", "AED"};

        AlertDialog dialog = new AlertDialog.Builder(this)
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
                                    DbMap.put(SELECTED_CURRENCY, selectedCurrency);
                                    SettingsActivity.this.finish();
                                }
                            })
                            .setSingleChoiceItems(currencyCodes, currencyCodes.length, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedCurrency =currencyCodes[which].toString();
                                }
                            }).create();
        return dialog;
    }
}
