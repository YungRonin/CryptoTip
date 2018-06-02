package app.cryptotip.cryptotip.app;

import java.math.BigInteger;

public class BalanceHelper {

    public String convertWeiToEth(BigInteger weiBalance) {
        BigInteger threeDecimal = new BigInteger(weiBalance.toString()).divide(new BigInteger("1000000000000000"));
        String bString = String.valueOf(threeDecimal);
        //Log.e(getClass().getName(), "three decmimal == " + bString);
        if(bString.length() < 4){
            //todo decide on what size double to display to user and write more elegant solution
            int i = bString.length();
            while (i != 0){
                bString = String.valueOf("0").concat(bString);
                i--;
            }
        }
        if (bString.length() >= 4) {
            String aString = bString.substring(0, bString.length() - 3) + "." + bString.substring(bString.length() - 3, bString.length());
            return aString;
        }
        else {
            return "0.000";
        }
    }
}
