/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)CustomerReceiptManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code CustomerReceiptManager} Class extends the methods of the parent class.
 *
 * key = Account
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Jan-2011
 */
public class CustomerReceiptManager extends ObjectMapStore<CustomerReceiptBean> {

    /**
     * Constructs a {@code CustomerReceiptManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public CustomerReceiptManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Helper method to set a customer receipt void by setting customer receipt type to ADMIN_VOID. This method is
     * used when an administration error needs to void a customer receipt. The value is not changed so that it can
     * be traced to back for audit.
     *
     * @param accountId
     * @param customerReceiptId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setCustomerReceiptVoid(int accountId, int customerReceiptId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        CustomerReceiptBean customerReceipt = this.getObject(accountId, customerReceiptId);
        customerReceipt.setPaymentType(PaymentType.ADMIN_VOID, owner);
        this.setObject(accountId, customerReceipt);
    }

    /**
     * Returns a list of CustomerReceipts that have a PaymentType of VOID. The method getAllObjects()
     * does not include void objects so this is the only method to get void objects from the manager.
     *
     * @param accountId
     * @return a list of all void objects.
     * @throws PersistenceException
     */
    public List<CustomerReceiptBean> getAllVoidCustomerReceipts(int accountId) throws PersistenceException {
        List<CustomerReceiptBean> rtnList = new LinkedList<>();
        for(CustomerReceiptBean customerReceipt : super.getAllObjects(accountId)) {
            if(customerReceipt.getPaymentType() == PaymentType.ADMIN_VOID) {
                rtnList.add(customerReceipt);
            }
        }
        return rtnList;
    }

    /**
     * Override of the getAllObjects so as to exclude all VOID CustomerReceiptBean
     *
     * @param accountId
     * @param options
     * @return all CustomerReceiptBean Objects excluding VOID
     * @throws PersistenceException
     */
    @Override
    public List<CustomerReceiptBean> getAllObjects(int accountId, ObjectDataOptionsEnum... options) throws PersistenceException {
        List<CustomerReceiptBean> rtnList = new LinkedList<>();
        for(CustomerReceiptBean customerReceipt : super.getAllObjects(accountId, options)) {
            if(customerReceipt.getPaymentType() != PaymentType.ADMIN_VOID) {
                rtnList.add(customerReceipt);
            }
        }
        return rtnList;
    }

    /**
     * Override of the getAllObjects so as to exclude all VOID CustomerReceiptBean
     *
     * @param options
     * @return all CustomerReceiptBean Objects excluding VOID
     * @throws PersistenceException
     */
    @Override
    public List<CustomerReceiptBean> getAllObjects(ObjectDataOptionsEnum... options) throws PersistenceException {
        List<CustomerReceiptBean> rtnList = new LinkedList<>();
        for(int accountId : super.getAllKeys(options)){
            rtnList.addAll(this.getAllObjects(accountId, options));
        }
        return rtnList;
    }

}
