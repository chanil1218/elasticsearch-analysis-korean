package org.apache.lucene.analysis.kr.utils;

public class UnhandledException extends RuntimeException {

    /**
     * Required for serialization support.
     * 
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1832101364842773720L;

    /**
     * Constructs the exception using a cause.
     *
     * @param cause  the underlying cause
     */
    public UnhandledException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs the exception using a message and cause.
     *
     * @param message  the message to use
     * @param cause  the underlying cause
     */
    public UnhandledException(String message, Throwable cause) {
        super(message, cause);
    }

    
}
