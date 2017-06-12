/*
 * @(#)ExceedCapacityException.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.exceptions;

/**
 * The {@code ExceedCapacityException} extends BusinessRule exception
 *
 * @author Darryl Oatridge
 * @version 1.00 14-Oct-2010
 */
public class ExceedCapacityException extends BusinessRuleException {
    private static final long serialVersionUID = 20101014100L;

    /**
     * Constructs an instance of {@code ExceedCapacityException} with the specified detail message.
     * @param msg the detail message.
     */
    public ExceedCapacityException(String msg) {
        super(msg, false);
    }

    /**
     * Constructs an instance of {@code ExceedCapacityException} with the specified detail message.
     * @param msg the detail message.
     * @param isError
     */
    public ExceedCapacityException(String msg, boolean isError) {
        super(msg, isError);
    }
}
