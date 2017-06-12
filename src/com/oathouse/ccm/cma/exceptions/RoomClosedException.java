/*
 * @(#)RoomClosedException.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.exceptions;

/**
 * The {@code RoomClosedException} extends BusinessRule exception
 *
 * @author Darryl Oatridge
 * @version 1.00 14-Oct-2010
 */
public class RoomClosedException extends BusinessRuleException {
    private static final long serialVersionUID = 20101014100L;

    /**
     * Constructs an instance of {@code RoomClosedException} with the specified detail message.
     * @param msg the detail message.
     */
    public RoomClosedException(String msg) {
        super(msg, false);
    }

    /**
     * Constructs an instance of <code>RoomClosedException</code> with the specified detail message.
     * @param msg the detail message.
     * @param isError
     */
    public RoomClosedException(String msg, boolean isError) {
        super(msg, isError);
    }
}
