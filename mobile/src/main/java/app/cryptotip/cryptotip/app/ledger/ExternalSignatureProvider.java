package app.cryptotip.cryptotip.app.ledger;

import com.mrd.bitlib.StandardTransactionBuilder;
import com.mrd.bitlib.model.Transaction;

import app.cryptotip.cryptotip.app.ledger.bip44.Bip44AccountExternalSignature;

/**
 * Hardware wallets provide signatures so accounts can work without the private keys themselves.
 */
public interface ExternalSignatureProvider {
    Transaction getSignedTransaction(StandardTransactionBuilder.UnsignedTransaction unsigned, Bip44AccountExternalSignature forAccount);
    int getBIP44AccountType();
}
