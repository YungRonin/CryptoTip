package app.cryptotip.cryptotip.app.transaction;

import java.math.BigInteger;

import app.cryptotip.cryptotip.app.BalanceHelper;
import app.cryptotip.cryptotip.app.database.DbMap;

import static app.cryptotip.cryptotip.app.Home.FIAT_PRICE;

public class Transaction {

    private String to;
    private String ethValue;
    private String transactionHash;
    private String fiatValue;

    public Transaction(String to, String ethValue, String transactionHash){
       this.to = to;
       this.ethValue =  new BalanceHelper().convertWeiToEth(BigInteger.valueOf(Long.valueOf(ethValue)));
       this.transactionHash = transactionHash;
       String value = String.valueOf(Double.valueOf(this.ethValue) * Double.valueOf(DbMap.get(FIAT_PRICE)));
       String[] split = value.split("\\.");
       fiatValue = split[0].concat("." + split[1].charAt(0) + split[1].charAt(1));
    }

    public String getTo() {
        return to;
    }

    public String getEthValue() {
        return ethValue;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getFiatValue() {
        return fiatValue;
    }
}
