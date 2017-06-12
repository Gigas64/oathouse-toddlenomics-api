/*
 * @(#)AccountHolderProperties.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.properties;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSingleStore;

/**
 * The {@code AccountHolderManager} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 12-Aug-2010
 */
public class AccountHolderManager extends ObjectSingleStore<AccountHolderBean> {

    /**
     * Constructs a {@code AccountHolderManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public AccountHolderManager(String managerName) {
        super(managerName);
    }

    /**
     * sets an ObjectAccountHolder
     * @param contact
     * @param businessName
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param phone
     * @param accountId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectAccountHolder(String contact, String businessName, String line1, String line2, String line3, String city,
                                 String province, String postcode, String country, String phone,
                                 String accountId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setAccountHolder(contact, businessName, line1, line2, line3, city, province, postcode, country, phone, accountId, owner);
        setObject(bean);
    }

    /**
     * changes the superuser and password
     * @param username
     * @param password
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectSuperUser(String username, String password, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setSuperUser(username, password, owner);
        setObject(bean);
    }

    /**
     * changes the return email
     * @param emailAddress
     * @param displayName
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectReturnEmail(String emailAddress, String displayName, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setReturnEmail(emailAddress, displayName, owner);
        setObject(bean);
    }

    /**
     * changes the Billing
     * @param validated
     * @param billingEmail
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectBilling(String priceUrl, boolean validated, String billingEmail, String owner)
            throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setBilling(priceUrl, validated, billingEmail, owner);
        setObject(bean);
    }

    /**
     * changes the suspended boolean
     * @param isSuspended
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectSuspended(boolean isSuspended, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setSuspended(isSuspended, owner);
        setObject(bean);
    }

    /**
     * sets the locale: timezone and language
     * @param timeZone
     * @param language
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectLocale(String timeZone, String language, String currency, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setLocale(timeZone, language, currency, owner);
        setObject(bean);
    }

    /**
     * sets the registered capacity of the nursery
     * @param registeredCapacity
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectRegisteredCapacity(int registeredCapacity, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setRegisteredCapacity(registeredCapacity, owner);
        setObject(bean);
    }

    /**
     * sets the charge rate of the nursery
     * @param chargeRate
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectChargeRate(int chargeRate, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        AccountHolderBean bean = getObject();
        bean.setChargeRate(chargeRate, owner);
        setObject(bean);
    }
}
