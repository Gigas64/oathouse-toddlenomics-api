/**
 * @(#)PaymentType.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:		Oathouse.com Ltd
 */

package com.oathouse.ccm.cos.accounts.transaction;

/**
 * The {@code PaymentType} Class provides types for transactions
 *
* @author 	Darryl Oatridge
 * @version 	2.00 09-April-2010
 */
public enum PaymentType {
        UNDEFINED,
        NO_VALUE,
        // when done in error this indicates a void
        ADMIN_VOID,
        // carried forward
        CF,
        // invoice Credit for negative invoice. Invoice value needs to be carried forward.
        INVOICE_CF,
        // Customer Credit. Where a customer has overpayed and is in credit. Can be carried forward to next invoice.
        CUSTOMER_CF,
        CASH,
        CHEQUE,
        BANK_TRANSFER,
        CREDITCARD,
        DEBITCARD,
        VOUCHER;

}
