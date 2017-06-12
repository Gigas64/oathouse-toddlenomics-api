/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)ContactManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code ContactManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 22-Aug-2010
 */
public class ContactManager extends ObjectSetStore<ContactBean> {

    /**
     * Constructs a {@code ContactManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public ContactManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * This Method overrides the super method so as to ensure any Contact
     * returned has the RelationType set to NON_PROFESSIONAL
     *
     * @param identifier
     * @return
     * @throws NoSuchIdentifierException
     */
    @Override
    public ContactBean getObject(int identifier, ObjectDataOptionsEnum... options) throws NoSuchIdentifierException, PersistenceException {
        final ContactBean bean = super.getObject(identifier, options);
        if(!bean.getType().isProfessional()) {
            bean.setType(RelationType.NON_PROFESSIONAL, bean.getOwner());
        }
        return (bean);
    }

    /**
     * This Method overrides the super method so as to ensure any Contact
     * returned has the RelationType set to NON_PROFESSIONAL
     *
     * @return
     * @see RelationType
     */
    @Override
    public List<ContactBean> getAllObjects(ObjectDataOptionsEnum... options) throws PersistenceException {
        final List<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean bean : super.getAllObjects(options)) {
            if(!bean.getType().isProfessional()) {
                bean.setType(RelationType.NON_PROFESSIONAL, bean.getOwner());
            }
            rtnList.add(bean);
        }
        return (rtnList);
    }

    /**
     * Professional Contacts retain their relationship, therfore this method returns contact that belong to
     * the requested profession and have a profile status found within the list.
     *
     * @param profession
     * @return a list of all the contacts with the given profession
     * @throws PersistenceException
     * @see RelationType
     */
    public List<ContactBean> getAllObjects(RelationType profession) throws PersistenceException {
        final List<ContactBean> rtnList = new LinkedList<>();
        if(profession.isProfessional()) {
            for(ContactBean contact : super.getAllObjects()) {
                if(contact.getType().equals(profession)) {
                    rtnList.add(contact);
                }
            }
        }
        return (rtnList);
    }

    /**
     * This allows all contacts that have an NON_PROFESSIONAL RelationType (e.g. are not professionals) that have a certain
     * profileStatus to be retrieved.
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @see RelationType
     * @see AccountStatus
     */
    public List<ContactBean> getAllCustodians() throws NoSuchIdentifierException, PersistenceException  {
        final List<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean contact : this.getAllObjects()) {
            if(contact.getType().equals(RelationType.NON_PROFESSIONAL)) {
                rtnList.add(contact);
            }
        }
        return (rtnList);
    }

    /**
     * this method is overridden to ensure the contact type is set to NON_PROFESSIONAL if the type is not a Professional
     * @param contact
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public ContactBean setObject(ContactBean contact) throws PersistenceException, NullObjectException {
        if(!contact.getType().isProfessional()) {
            contact.setType(RelationType.NON_PROFESSIONAL, contact.getOwner());
        }
        return super.setObject(contact);
    }

    /**
     * Allows a contact's personal details to be modified
     * @param contactId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param business
     * @param availableStart
     * @param availableEnd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectPersonalDetails(int contactId,
            String title, List<String> forenames, String surname, String commonName,
            String business, int availableStart, int availableEnd, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(contactId).setPersonalDetails(title, forenames, surname, commonName, business, availableStart, availableEnd, owner);
        setObject(getObject(contactId));
    }

    /**
     * Allows a contact's address to be modified
     * @param contactId
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectAddress(int contactId, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(contactId).setProfileAddress(line1, line2, line3, city, province, postcode, country, owner);
        setObject(getObject(contactId));
    }

    /**
     * Allows a contact's photoId to be modified
     * @param contactId
     * @param photoId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectPhotoId(int contactId, int photoId, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(contactId).setPhotoId(photoId, owner);
        setObject(getObject(contactId));
    }

    /**
     * Allows a contact's communications to be modified
     * @param contactId
     * @param phone
     * @param phone2
     * @param mobile
     * @param email
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectCommunications(int contactId, String phone, String phone2, String mobile, String email, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(contactId).setCommunications(phone, phone2, mobile, email, owner);
        setObject(getObject(contactId));
    }

    /**
     * Allows a contact's notes to be modified
     * @param contactId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setObjectNotes(int contactId, String notes, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        getObject(contactId).setProfileNotes(notes, owner);
        setObject(getObject(contactId));
    }

    /**
     * Allows the owner to be set so when an underlying change is made to a bean
     * relating to a contact the change owner can be displayed as a change to the contact.
     *
     * @param contactId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectOwner(int contactId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        getObject(contactId).setOwner(owner);
        setObject(getObject(contactId));
    }
}
