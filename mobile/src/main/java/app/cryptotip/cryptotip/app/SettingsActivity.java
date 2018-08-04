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
    private String selectedFiatCurrency;
    private String selectedCryptoCurrency;
    public static final String SELECTED_FIAT_CURRENCY = "selectedFiatCurrency";
    public static final String SELECTED_CRYPTO_CURRENCY = "selectedCryptoCurrency";

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
        GTextView fiatCurrencyTview = new GTextView(this);
        layout.addView(fiatCurrencyTview);
        fiatCurrencyTview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        fiatCurrencyTview.text("Fiat Currency")
                .padding(0,20,0,20)
                .bold()
                .gravity(Gravity.CENTER)
                .textSize(25.0f)
                .setBackgroundDrawable(BorderFactory.createBorders(getResources().getColor(android.R.color.white, null), getResources().getColor(android.R.color.black, null), 0,3,0,3));

        fiatCurrencyTview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFiatCurrencySelectorDialog().show();
            }
        });

        GTextView cryptoCurrencyTview = new GTextView(this);
        layout.addView(cryptoCurrencyTview);
        cryptoCurrencyTview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        cryptoCurrencyTview.text("Crypto Currency")
                .padding(0,20,0,20)
                .bold()
                .gravity(Gravity.CENTER)
                .textSize(25.0f)
                .setBackgroundDrawable(BorderFactory.createBorders(getResources().getColor(android.R.color.white, null), getResources().getColor(android.R.color.black, null), 0,3,0,3));

        cryptoCurrencyTview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCryptoCurrencySelectorDialog().show();
            }
        });
    }

    private AlertDialog createFiatCurrencySelectorDialog(){
        final CharSequence[] fiatCurrencyCodes = {"AUD", "USD", "EUR", "GBP", "INR", "MYR", "IDR", "RUB", "NZD", "THB", "SGD", "JPY", "CNY", "CAD", "KRW", "SAR", "AED"};

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
                                    DbMap.put(SELECTED_FIAT_CURRENCY, selectedFiatCurrency);
                                    SettingsActivity.this.finish();
                                }
                            })
                            .setSingleChoiceItems(fiatCurrencyCodes, fiatCurrencyCodes.length, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedFiatCurrency = fiatCurrencyCodes[which].toString();
                                }
                            }).create();
        return dialog;
    }

    private AlertDialog createCryptoCurrencySelectorDialog(){
        final CharSequence[] cryptoCurrencyCodes = {"ETH", "OMEG", "BETA", "ALPH"};

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
                        DbMap.put(SELECTED_CRYPTO_CURRENCY, selectedCryptoCurrency);
                        SettingsActivity.this.finish();
                    }
                })
                .setSingleChoiceItems(cryptoCurrencyCodes, cryptoCurrencyCodes.length, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCryptoCurrency = cryptoCurrencyCodes[which].toString();
                    }
                }).create();
        return dialog;
    }
}
