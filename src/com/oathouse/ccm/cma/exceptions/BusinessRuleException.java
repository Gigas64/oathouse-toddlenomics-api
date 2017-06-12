/*
 * @(#)BusinessRuleException.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.exceptions;

import com.oathouse.oss.storage.exceptions.ObjectStoreException;

/**
 * The {@code BusinessRuleException} Enumeration
 *
 * @author Darryl Oatridge
 * @version 1.00 14-Oct-2010
 */
public class BusinessRuleException extends ObjectStoreException {
    private static final long serialVersionUID = 20101014100L;

    /**
     * Constructs an instance of {@code BusinessRuleException} with the specified detail message.
     * @param msg the detail message.
     */
    public BusinessRuleException(String msg) {
        super(msg, false);
    }

    /**
     * Constructs an instance of <code>BusinessRuleException</code> with the specified detail message.
     * @param msg the detail message.
     * @param isError
     */
    public BusinessRuleException(String msg, boolean isError) {
        super(msg, isError);
    }
}
