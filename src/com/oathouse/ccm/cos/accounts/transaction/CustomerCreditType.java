/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)CustomerCreditType.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

/**
 * The {@code CustomerCreditType} Enumeration providers a mechanism to identify the
 * different types of CustomerCredit
 *
 * @author Darryl Oatridge
 * @version 1.00 03-Mar-2011
 */
public enum CustomerCreditType {
    UNDEFINED,
    NO_VALUE,
    RECEIPT_CREDIT, // credit from a customerReceipt
    INVOICE_CREDIT, // an invoice where the total is a credit
    INVOICE_DEBIT,  // debit from an invoice
    PAYMENT_DEBIT,  // debit from a payment to an account
}
