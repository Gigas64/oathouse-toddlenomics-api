/*
 * @(#)BookingRequestManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code BookingRequestManager} Class extends the methods of the parent class.
 *
 * Key - accountId
 * Id  - bookingRequestId
 *
 * @author Darryl Oatridge
 * @version 1.00 19-Nov-2010
 */
public class BookingRequestManager extends ObjectMapStore<BookingRequestBean> {

    public static final int NO_REQUEST = ObjectEnum.DEFAULT_ID.value();
    protected static final String NO_REQUEST_LABEL = "#ffffff";
    protected static final String NO_REQUEST_NAME = "request.none";
    protected static final int NO_ROOM_ID = ObjectEnum.DEFAULT_ID.value();

    /**
     * Constructs a {@code BookingRequestManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public BookingRequestManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        BookingRequestBean q = new BookingRequestBean(NO_REQUEST, NO_REQUEST_NAME, NO_REQUEST_LABEL, -1, -1, -1, -1,
                new ConcurrentSkipListSet<Integer>(), 0, ObjectBean.SYSTEM_OWNED);
        super.resetDefaultObject(q);
    }

    /**
     * Returns a BookingRequestBean with the given name. This allows a BookingRequestBean to be
     * reference by name as well as by id. Because different accounts can use the same name, you
     * must specify the accountId.
     *
     * @param accountId the account the name belongs to
     * @param name the name of the BookingRequestBean
     * @return The BookingRequestBean with the given name
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public BookingRequestBean getObject(int accountId, String name) throws NoSuchIdentifierException, PersistenceException {
        for(BookingRequestBean request : getAllObjects(accountId)) {
            if(request.getName().equalsIgnoreCase(name)) {
                return (request);
            }
        }
        throw new NoSuchIdentifierException("A BookingRequestBean with name '" + name + "' does not exist");
    }

    /**
     * this method looks through ALL booking requests to find if a liableContactId exists.
     * If the liableContactId is found within a booking request it returns true, otherwise
     * returns false
     *
     * @param liableContactId
     * @return true if found, false if not
     * @throws PersistenceException
     */
    public boolean hasLiability(int liableContactId) throws PersistenceException {
        for(int accountId : getAllKeys()) {
            for(BookingRequestBean request : getAllObjects(accountId)) {
                if(request.getLiableContactId() == liableContactId) {
                    return (true);
                }
            }
        }
        return (false);
    }

    /**
     * validation method to check if an account already has a booking request where the
     * key value elements match. In a booking request the key value elements are the bookingTypeId
     * and the requestSdSet. If an existing BookingRequestBean exists with the same values as passed
     * then true is returned, else false.
     *
     *
     * @param accountId
     * @param requestSdSet
     * @param bookingTypeId
     * @return true if all values passed match an existing BookingRequestBean
     * @throws PersistenceException
     */
    public boolean hasBookingRequestValues(int accountId, Set<Integer> requestSdSet, int bookingTypeId) throws PersistenceException {
        for(BookingRequestBean currentRequest : this.getAllObjects(accountId)) {
            if(currentRequest.getBookingTypeId() == bookingTypeId
                        && currentRequest.getRequestSdSet().size() == requestSdSet.size()
                        && currentRequest.getRequestSdSet().containsAll(requestSdSet)) {
                return true;
            }
        }
        return(false);
    }

    /**
     * Allows the bookingTypeId of a Booking Request to be changed without having to create a new BookingRequestBean
     *
     * @param accountId
     * @param requestId
     * @param bookingTypeId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectBookingTypeId(int accountId, int requestId, int bookingTypeId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        if(requestId == NO_REQUEST) {
            throw new PersistenceException("The id passed is the default id. You can not change the default BookingRequestBean");
        }
        BookingRequestBean request = getObject(accountId, requestId);
        request.setBookingTypeId(bookingTypeId, owner);
        super.setObject(accountId, request);
    }

    /**
     * saves an object to the store and persists the object to disk. If the object
     * identifier exists, the object replaces the existing one and modifies the
     * ObjectBean modified parameter.
     *
     * @param ob the object bean
     * @return the object set
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public BookingRequestBean setObject(int accountId, BookingRequestBean ob) throws PersistenceException, NullObjectException {
        if(ob.getBookingRequestId() != NO_REQUEST) {
            return super.setObject(accountId, ob);
        }
        throw new PersistenceException("The id passed is the default id. You can not change the default BookingRequestBean");
    }

    /**
     * Removes a object with the provided key and identifier from the memory store and
     * the persistence store.
     *
     * @return the object T that was deleted
     * @throws PersistenceException
     */
    @Override
    public BookingRequestBean removeObject(int accountId, int requestId) throws PersistenceException {
        if(requestId != NO_REQUEST) {
            return super.removeObject(accountId, requestId);
        }
        return null;
    }

}
