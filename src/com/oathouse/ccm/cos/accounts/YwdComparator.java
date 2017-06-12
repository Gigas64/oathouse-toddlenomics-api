/*
 * @(#)YwdTransactionComparator.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts;

import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean;
import com.oathouse.ccm.cos.accounts.transaction.TransactionBean;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.Comparator;

/**
 * The {@code YwdTransactionComparator} Class used to compare a collection
 * that contains different types of ObjectBean extended objects
 * that deal with ywd and more specifically have getYwd()
 *
 * @author Darryl Oatridge
 * @version 1.00 17-May-2011
 */
public class YwdComparator implements Comparator<ObjectBean> {

    @Override
    public int compare(ObjectBean o1, ObjectBean o2) {
        if(o1 == null && o2 == null) {
            return 0;
        }
        // just in case there are null object values show them last
        if(o1 != null && o2 == null) {
            return -1;
        }
        if(o1 == null && o2 != null) {
            return 1;
        }
        // compare by year
        int primaryComp = getYwd(o1) < getYwd(o2) ? -1 : (getYwd(o1) == getYwd(o2) ? 0 : 1);
        if(primaryComp != 0) {
            return primaryComp;
        }
        // this sorts out billingSd values or create timestamp
        int secondaryComp = getSecondary(o1) < getSecondary(o2) ? -1 : (getSecondary(o1) == getSecondary(o2) ? 0 : 1);
        if(secondaryComp != 0) {
            return secondaryComp;
        }
        // Modify not unique so violates the equals comparability. Can cause disappearing objects in Sets
        return (o1.getIdentifier() < o2.getIdentifier() ? -1 : (o1.getIdentifier() == o2.getIdentifier() ? 0 : 1));
    }

    private int getYwd(ObjectBean o) {
        if(o instanceof BillingBean) {
           return((BillingBean) o).getYwd();
        }
        if(o instanceof InvoiceBean) {
           return((InvoiceBean) o).getYwd();
        }
        if(o instanceof TransactionBean) {
           return((TransactionBean) o).getYwd();
        }
        return(0);
    }

    private long getSecondary(ObjectBean o) {
        if(o instanceof BillingBean) {
           return((BillingBean) o).getBillingSd();
        }
        // anything else return the create timestamp
        return (o.getCreated());
    }

}
