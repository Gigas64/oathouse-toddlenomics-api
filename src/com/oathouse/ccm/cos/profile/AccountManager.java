/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)AccountManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;

/**
 * The {@code AccountManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 03-Nov-2010
 */
public class AccountManager extends ObjectSetStore<AccountBean> {

    /**
     * Constructs a {@code AccountManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public AccountManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Gets the account using the accountRef
     *
     * @param accountRef
     * @return an AccountBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public AccountBean getObject(String accountRef) throws NoSuchIdentifierException, PersistenceException {
        for(AccountBean account : getAllObjects()) {
            if(account.getAccountRef().equalsIgnoreCase(accountRef)) {
                return (account);
            }
        }
        throw new NoSuchIdentifierException("The AccountRef '" + accountRef + "' does not exist");
    }

    /**
     * Checks to see if an accountRef exists.
     *
     * @param accountRef
     * @return true if it exists, false if not
     * @throws PersistenceException
     */
    public boolean isAccountRef(String accountRef) throws PersistenceException {
        for(AccountBean account : getAllObjects()) {
            if(account.getAccountRef().equalsIgnoreCase(accountRef)) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Allows the accountRef of an AccountBean to be changed
     * @param accountId
     * @param accountRef
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectAccountRef(int accountId, String accountRef, String owner) throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setAccountRef(accountRef, owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the notes of an AccountBean to be changed
     * @param accountId
     * @param notes
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectNotes(int accountId, String notes, String owner) throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setNotes(notes, owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the owner to be set so when an underlying change is made to a bean
     * relating to an account the change owner can be displayed at account level.
     *
     * @param accountId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectOwner(int accountId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(accountId).setOwner(owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the status of an AccountBean to be changed
     * @param accountId
     * @param status
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectStatus(int accountId, AccountStatus status, String owner) throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setStatus(status, owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the fixedItemDiscountRate of an AccountBean to be changed
     * @param accountId
     * @param fixedItemDiscountRate
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectFixedItemDiscountRate(int accountId, int fixedItemDiscountRate, String owner) throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setFixedItemDiscountRate(fixedItemDiscountRate, owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the contract boolean of an AccountBean to be changed
     * @param accountId
     * @param hasContract
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectContract(int accountId, boolean hasContract, String owner) throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setContract(hasContract, owner);
        setObject(getObject(accountId));
    }

    /**
     * Allows the payment period
     * @param accountId
     * @param paymentPeriod
     * @param regularPaymentInstruction
     * @param paymentPeriodValue
     * @param owner
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectPaymentPeriod(int accountId, PaymentPeriodEnum paymentPeriod,
                                           String regularPaymentInstruction, long paymentPeriodValue, String owner)
            throws NullObjectException, NoSuchIdentifierException, PersistenceException, NoSuchIdentifierException {
        getObject(accountId).setPaymentPeriod(paymentPeriod, owner);
        getObject(accountId).setRegularPaymentInstruction(regularPaymentInstruction, owner);
        getObject(accountId).setPaymentPeriodValue(paymentPeriodValue, owner);
        setObject(getObject(accountId));
    }

}
