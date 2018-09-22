package app.cryptotip.cryptotip.app.ledger.wapi;

public interface WapiLogger {
    void logError(String message, Exception e);

    void logError(String message);

    void logInfo(String message);

    WapiLogger NULL_LOGGER = new WapiLogger(){
        @Override
        public void logError(String message, Exception e) {
        }

        @Override
        public void logError(String message) {
        }

        @Override
        public void logInfo(String message) {
        }
    };
}
