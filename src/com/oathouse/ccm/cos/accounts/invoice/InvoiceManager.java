/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)InvoiceManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code InvoiceManager} Class extends the methods of the parent class.
 *
 * key = accountId
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Jan-2011
 */
public class InvoiceManager extends ObjectMapStore<InvoiceBean> {

    /**
     * Constructs a {@code InvoiceManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public InvoiceManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Returns all invoices with a tax date (ywd) between the dates specified
     * @param isDueDate boolean indicating the date to be referenced.  true = due date, false = invoice issue date
     * @param firstYwd
     * @param lastYwd
     * @return
     * @throws PersistenceException
     */
    public List<InvoiceBean> getAllObjects(boolean isDueDate, int firstYwd, int lastYwd) throws PersistenceException {
        List<InvoiceBean> rtnList = new LinkedList<>();
        for(InvoiceBean i : getAllObjects()) {
            if(i.isType(InvoiceType.STANDARD_VOID)) {
                continue;
            }
            if(isDueDate) {
                if(i.getDueYwd() >= firstYwd && i.getDueYwd() <= lastYwd) {
                    rtnList.add(i);
                }
            } else {
                if(i.getYwd() >= firstYwd && i.getYwd() <= lastYwd) {
                    rtnList.add(i);
                }
            }
        }
        return rtnList;
    }

    /**
     * Helper method to set an invoice void. This sets the invoice type to STANDARD_VOID
     *
     * @param accountId
     * @param invoiceId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setInvoiceVoid(int accountId, int invoiceId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        InvoiceBean invoice = super.getObject(accountId, invoiceId);
        invoice.setInvoiceType(InvoiceType.STANDARD_VOID, owner);
        super.setObject(accountId, invoice);
    }
}
