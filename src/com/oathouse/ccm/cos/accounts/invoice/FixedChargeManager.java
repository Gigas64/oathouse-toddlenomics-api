/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)FixedChargeManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code FixedChargeManager} Class extends the methods of the parent class.
 *
 * Key = AccountId
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Jan-2011
 */
public class FixedChargeManager extends ObjectMapStore<FixedChargeBean> {

    /**
     * Constructs a {@code FixedChargeManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public FixedChargeManager(String managerName) {
        super(managerName);
    }

    /**
     * Returns a list of {@code FixedChargeBean} Object belonging to an accountId that have not yet been
     * allocated to an invoice.
     *
     * @param accountId
     * @return List of FixedChargeBean Objects
     * @throws PersistenceException
     */
    public List<FixedChargeBean> getOutstandingFixedCharges(int accountId) throws PersistenceException {
        LinkedList<FixedChargeBean> rtnList = new LinkedList<FixedChargeBean>();
        for(FixedChargeBean charge : getAllObjects(accountId)) {
            if(charge.getInvoiceId() == ObjectEnum.INITIALISATION.value()) {
                rtnList.add(charge);
            }
        }
        return rtnList;
    }

    /**
     * Returns a list of {@code FixedChargeBean} Object that belong to an invoice.
     *
     * @param accountId
     * @param invoiceId
     * @return a list of FixedChargeBean Objects
     * @throws PersistenceException
     */
    public List<FixedChargeBean> getAllObjectsForInvoice(int accountId, int invoiceId) throws PersistenceException {
        LinkedList<FixedChargeBean> rtnList = new LinkedList<FixedChargeBean>();
        for(FixedChargeBean charge : getAllObjects(accountId)) {
            if(charge.getInvoiceId() == invoiceId) {
                rtnList.add(charge);
            }
        }
        return rtnList;
    }

    /**
     * returns an array size three of the Invoice charges. the array order is
     * <p>
     * [0] - valueIncTax<br>
     * [1] - value<br>
     * [2] - tax<br>
     * </p>
     *
     *
     * @param accountId
     * @param invoiceId
     * @return a long array size 3
     * @throws PersistenceException
     */
    public long[] getInvoiceCharges(int accountId, int invoiceId) throws PersistenceException {
        long[] rtnValues = {0, 0, 0};
        for(FixedChargeBean charge : getAllObjects(accountId)) {
            if(charge.getInvoiceId() == invoiceId) {
                rtnValues[0] += charge.getInvoiceValues()[0];
                rtnValues[1] += charge.getInvoiceValues()[1];
                rtnValues[2] += charge.getInvoiceValues()[2];
            }
        }
        return rtnValues;
    }

    /**
     * Sets the invoiceId taxRate and discountRate for the {@code FixedChargeBean} referenced byt the
     * fixedChargeId
     *
     * @param accountId
     * @param fixedChargeId
     * @param invoiceId
     * @param taxRate
     * @param discountRate
     * @param owner
     * @return a FixedChargeBean Object
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public FixedChargeBean setInvoiceId(int accountId, int fixedChargeId, int invoiceId, int taxRate, int discountRate,
            String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        FixedChargeBean charge = getObject(accountId, fixedChargeId);
        charge.setInvoiceId(invoiceId, owner);
        charge.setTaxRate(taxRate, owner);
        charge.setDiscountRate(discountRate, owner);
        return setObject(accountId, charge);
    }
}
