/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)PaymentManager.java
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
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code PaymentManager} Class extends the methods of the parent class.
 *
 * Key = accountId
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Jan-2011
 */
public class PaymentManager extends ObjectMapStore<PaymentBean> {

    /**
     * Constructs a {@code PaymentManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public PaymentManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Helper method to set a payment void by setting payment type to ADMIN_VOID. This method is used when an
     * administration error needs to void a payment. The value is not changed to that it can be traced back
     * for audit.
     *
     * @param accountId
     * @param paymentId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setPaymentVoid(int accountId, int paymentId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        PaymentBean payment = this.getObject(accountId, paymentId);
        payment.setPaymentType(PaymentType.ADMIN_VOID, owner);
        this.setObject(accountId, payment);
    }

    /**
     * Returns a list of PaymentBean that have a PaymentType of ADMIN_VOID. The method getAllPayments()
     * is the reverse of this method.
     *
     * @param accountId
     * @return a list of all void objects.
     * @throws PersistenceException
     */
    public List<PaymentBean> getAllVoidPayments(int accountId) throws PersistenceException {
        List<PaymentBean> rtnList = new LinkedList<>();
        for(PaymentBean payment : super.getAllObjects(accountId)) {
            if(payment.getPaymentType() == PaymentType.ADMIN_VOID) {
                rtnList.add(payment);
            }
        }
        return rtnList;
    }

    /**
     * Returns a list of CustomerReceipts that exclude any PaymentType of ADMIN_VOID.
     *
     * @param accountId
     * @return all PaymentBean Objects excluding VOID
     * @throws PersistenceException
     */
    public List<PaymentBean> getAllPayments(int accountId) throws PersistenceException {
        List<PaymentBean> rtnList = new LinkedList<>();
        for(PaymentBean payment : super.getAllObjects(accountId)) {
            if(payment.getPaymentType() != PaymentType.ADMIN_VOID) {
                rtnList.add(payment);
            }
        }
        return rtnList;
    }

    /**
     * Override of the getAllObjects so as to exclude all VOID PaymentBean
     *
     * @return all PaymentBean Objects excluding VOID
     * @throws PersistenceException
     */
    public List<PaymentBean> getAllPayments() throws PersistenceException {
        List<PaymentBean> rtnList = new LinkedList<>();
        for(int accountId : super.getAllKeys()){
            rtnList.addAll(this.getAllPayments(accountId));
        }
        return rtnList;
    }

}
