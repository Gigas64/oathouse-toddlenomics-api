/*
 * @(#)InvoiceType.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

/**
 * The {@code InvoiceType} Enumeration allows an invoice to be categorised
 *
 * @author  Darryl Oatridge
 * @version 1.00 3-Mar-2011
 */
public enum InvoiceType {
    UNDEFINED,
    NO_VALUE,
    STANDARD,
    //the invoice has been voided
    STANDARD_VOID,
    RECEIPT_FEE,
    RECEIPT_FEE_VOID;
}
