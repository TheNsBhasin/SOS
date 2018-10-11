package com.nsbhasin.sos.chatmessages.messagefilter;

import lombok.Getter;

public class MessageException extends Exception {

    public static enum Reason {NULLMESSAGE, MESSAGETOOSHORT, MESSAGEBLACKLISTED}

    ;

    @Getter
    private Reason reason;

    public MessageException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message String message
     * @param cause   The throwable object
     */
    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param message String message
     */
    public MessageException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause String message
     */
    public MessageException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param reason Enumeration that represents the exception's reason.
     */
    public MessageException(Reason reason) {
        this.reason = reason;
    }
}