package app.cryptotip.cryptotip.app.ledger.bip44;

import com.mrd.bitlib.model.NetworkParameters;
import com.mrd.bitlib.model.hdpath.Bip44Account;

import app.cryptotip.cryptotip.app.ledger.wapi.Wapi;

public class Bip44PubOnlyAccount extends Bip44Account {
    public Bip44PubOnlyAccount(Bip44AccountContext context, Bip44AccountKeyManager keyManager, NetworkParameters network, Bip44AccountBacking backing, Wapi wapi) {
        super(context, keyManager, network, backing, wapi);
    }

    @Override
    public boolean canSpend() {
        return false;
    }
}
