/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)InvoiceCreditManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;

/**
 * The {@code InvoiceCreditManager} Class extends the methods of the parent class.
 *
 * key = account
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Feb-2011
 */
public class InvoiceCreditManager extends ObjectMapStore<InvoiceCreditBean> {

    /**
     * Constructs a {@code InvoiceCreditManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param dataOptions
     * @param managerName a unique name to identify the manager.
     */
    public InvoiceCreditManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * this returns an {@code InvoiceCreditBean} object for an accountId and an invoiceId.
     *
     * @param accountId
     * @param invoiceId
     * @return an InvoiceCreditBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public InvoiceCreditBean getObjectForInvoice(int accountId, int invoiceId) throws NoSuchIdentifierException, PersistenceException {
        for(InvoiceCreditBean invoiceCredit : getAllObjects(accountId)) {
            if(invoiceCredit.getInvoiceId() == invoiceId) {
                return invoiceCredit;
            }
        }
        throw new NoSuchIdentifierException("The accountId " + accountId + " or invoiceId " + invoiceId + " does not exist");
    }
}
