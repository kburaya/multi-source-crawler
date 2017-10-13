package net.bbridge.crawler.exception;

public class DataRetrievalException extends Exception {

    public DataRetrievalException(String message) {
        super(message);
    }

    public DataRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataRetrievalException(Throwable cause) {
        super(cause);
    }
}
