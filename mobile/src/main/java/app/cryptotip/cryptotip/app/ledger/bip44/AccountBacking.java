package app.cryptotip.cryptotip.app.ledger.bip44;

import com.mrd.bitlib.model.OutPoint;
import com.mrd.bitlib.util.Sha256Hash;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import app.cryptotip.cryptotip.app.ledger.model.TransactionEx;
import app.cryptotip.cryptotip.app.ledger.model.TransactionOutputEx;

public interface AccountBacking {

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

    void clear();

    Collection<TransactionOutputEx> getAllUnspentOutputs();

    TransactionOutputEx getUnspentOutput(OutPoint outPoint);

    void deleteUnspentOutput(OutPoint outPoint);

    void putUnspentOutput(TransactionOutputEx output);

    void putParentTransactionOuputs(List<TransactionOutputEx> outputsList);

    void putParentTransactionOutput(TransactionOutputEx output);

    TransactionOutputEx getParentTransactionOutput(OutPoint outPoint);

    boolean hasParentTransactionOutput(OutPoint outPoint);

    void putTransaction(TransactionEx transaction);

    void putTransactions(List<TransactionEx> transactions);

    TransactionEx getTransaction(Sha256Hash hash);

    void deleteTransaction(Sha256Hash hash);

    List<TransactionEx> getTransactionHistory(int offset, int limit);

    List<TransactionEx> getTransactionsSince(long since);

    Collection<TransactionEx> getUnconfirmedTransactions();

    Collection<TransactionEx> getYoungTransactions(int maxConfirmations, int blockChainHeight);

    boolean hasTransaction(Sha256Hash txid);

    void putOutgoingTransaction(Sha256Hash txid, byte[] rawTransaction);

    Map<Sha256Hash, byte[]> getOutgoingTransactions();

    boolean isOutgoingTransaction(Sha256Hash txid);

    void removeOutgoingTransaction(Sha256Hash txid);

    void deleteTxRefersParentTransaction(Sha256Hash txId);

    Collection<Sha256Hash> getTransactionsReferencingOutPoint(OutPoint outPoint);

    void putTxRefersParentTransaction(Sha256Hash txId, List<OutPoint> refersOutputs);
}
