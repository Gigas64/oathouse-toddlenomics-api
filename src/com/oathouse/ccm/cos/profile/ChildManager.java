/*
 * @(#)ChildManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import java.util.*;

/**
 * The {@code ChildManager} Class extends the Object manager to cater
 * for the change that can be made to the status within the bean
 *
 * @author Darryl Oatridge
 * @version 1.00 07-May-2010
 */
public class ChildManager extends ObjectSetStore<ChildBean> {

    /**
     * the constructor takes the root store path of the persistence store and
     * the name you would like to give the manager to distinguish it in persistence
     *
     * @param managerName a unique name as a sub store to the store path
     * @param dataOptions 
     */
    public ChildManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Allows a child's associated account to be changed
     * @param childId
     * @param accountId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectAccountId(int childId, int accountId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(childId).setAccountId(accountId, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for the child's personal details to be modified
     * @param childId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param gender
     * @param dateOfBirth
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectPersonalDetails(int childId, String title, List<String> forenames, String surname,
            String commonName, String gender, int dateOfBirth, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(childId).setPersonalDetails(title, forenames, surname, commonName, gender, dateOfBirth, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for the child's address to be modified
     * @param childId
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param phone
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectAddress(int childId, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String phone, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(childId).setProfileAddress(line1, line2, line3, city, province, postcode, country, owner);
        getObject(childId).setProfilePhone(phone, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for a child's discounts to be modified
     * @param childId
     * @param bookingDiscountRate
     * @param fixedItemDiscountRate
     * @param tariffDiscountOff
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectDiscounts(int childId, int bookingDiscountRate, int fixedItemDiscountRate, boolean tariffDiscountOff, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(childId).setBookingDiscountRate(bookingDiscountRate, owner);
        getObject(childId).setFixedItemDiscountRate(fixedItemDiscountRate, owner);
        getObject(childId).setTariffDiscountOff(tariffDiscountOff, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for a child's holiday allocation to be modified
     * @param childId
     * @param holidayAllocation
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectHolidayAllocation(int childId, int holidayAllocation, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(childId).setHolidayAllocation(holidayAllocation, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for a child's photoId to be modified
     * @param childId
     * @param photoId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectPhotoId(int childId, int photoId, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(childId).setPhotoId(photoId, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for a child's notes to be modified
     * @param childId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectNotes(int childId, String notes, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(childId).setProfileNotes(notes, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for the changing of the DepartYwd.
     *
     * @param childId
     * @param departYwd This is a YWDHolder key value
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectDepartYwd(int childId, int departYwd, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(childId).setDepartYwd(departYwd, owner);
        setObject(getObject(childId));
    }

    /**
     * Allows for the changing of the NoticeYwd.
     *
     * @param childId
     * @param noticeYwd This is a YWDHolder key value
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectNoticeYwd(int childId, int noticeYwd, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(childId).setNoticeYwd(noticeYwd, owner);
        setObject(getObject(childId));
    }

    /**
     * Allow for the changing of the medFormReceived
     *
     * @param childId
     * @param medFormReceived
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setObjectMedFormReceived(int childId, boolean medFormReceived, String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        getObject(childId).setMedFormReceived(medFormReceived, owner);
        setObject(getObject(childId));
    }


}
