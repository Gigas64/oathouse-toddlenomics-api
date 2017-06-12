/*
 * @(#)ChildBookingHistory.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cma.config.*;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import java.util.*;

/**
 * The {@code ChildBookingHistory} Class is a encapsulated API, service level singleton that provides
 * access to related underlying manager instances. The class provides three distinct sections:
 * <h2>Manager Retrieval</h2>
 * <p>
 * This section allows the retrieval of the instance of the manager to use as part of this service. For
 * ease of reference each manager has both a long and a short name method call. ALL manager instances should
 * be referenced through the Service and not instantiated directly.
 * </p>
 *
 * <h2>Service Level get Methods</h2>
 * <p>
 * This section is for all get methods that require information outside their own data store. All other
 * get methods can be found up the tree in the manager or bean of the bean type.
 * </p>
 *
 * <h2>Service Level set Methods</h2>
 * <p>
 * All set methods must be implemented up to the Service level as this allows consistency across the API and
 * security for the underlying sets. If we have sets at the Service level, the Manager level and ObjectStore,
 * the inconsistency might cause a set to be made to the manager when a set in the service adds additional logic
 * and thus is bypassed.
 * </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 16-Dec-2011
 */
public class ChildBookingHistory {
    // Singleton Instance

    private volatile static ChildBookingHistory INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final ObjectMapStore<BookingBean> bookingHistory = new ObjectMapStore<>(VT.CHILD_BOOKING_HISTORY.manager(), ObjectDataOptionsEnum.PERSIST);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private ChildBookingHistory() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code ChildBookingHistory} class
     * @return instance of the {@code ChildBookingHistory}
     * @throws PersistenceException
     */
    public static ChildBookingHistory getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (ChildBookingHistory.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new ChildBookingHistory().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code ChildBookingHistory} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying
     * elements of the initialisation are not available.
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code ChildBookingHistory}. The
     * method returns an instance of the {@code ChildBookingHistory} so it can be chained.
     * This must be called before a the {@code ChildBookingHistory} is used. e.g  {@code ChildBookingHistory myChildBookingHistory = }
     * @return instance of the {@code ChildBookingHistory}
     * @throws PersistenceException
     */
    public synchronized ChildBookingHistory init() throws PersistenceException {
        if(initialise) {
            bookingHistory.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code ChildBookingHistory}. The
     * method returns an instance of the {@code ChildBookingHistory} so it can be chained.
     * @return instance of the {@code ChildBookingHistory}
     * @throws PersistenceException
     */
    public ChildBookingHistory reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code ChildBookingHistory}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = bookingHistory.clear()?success:false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code ChildBookingHistory} class instance by setting the INSTANCE reference
     * to null. This is mostly used for testing to clear and reset internal memory stores
     * when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval">

    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     *
     * This section allows the retrieval of the instance
     * of the manager to use as part of this service. For
     * ease of reference each manager has both a long and
     * a short name method call. ALL manager instances should
     * be referenced through the Service and not instantiated
     * directly.
     * ***************************************************/

    public ObjectMapStore<BookingBean> getBookingHistory() {
        return bookingHistory;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">

    /* ***************************************************
     * S E R V I C E   V A L I D A T I O N   M E T H O D S
     *
     * This section is for all validation methods so that
     * other services can check blind id values are legitimate
     * and any associated business rules are adhere to.
     * ***************************************************/
    /**
     * Validation Method to check pure id values exist. This method ONLY checks an ID
     * exists in a manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch(validationType) {
            // TODO add the case
            // case VT_TYPE:
            //    return someManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Level Get Methods">

    /* ***************************************************
     * S E R V I C E  L E V E L   G E T   M E T H O D S
     *
     * This section is for all get methods that require
     * information outside their own data store. All other
     * get methods can be found up the tree in the manager
     * or bean of the bean type.
     * ***************************************************/

    /**
     * returns a list of {@code BookingBean} objects that are a history of
     * a single booking.
     *
     * @param bookingId the booking you wish the history of
     * @return a list of booking history.
     * @throws PersistenceException
     */
    public List<BookingBean> getBookingHistory(int bookingId) throws PersistenceException {
        List<BookingBean> rtnList = bookingHistory.getAllObjects(bookingId);
        Collections.sort(rtnList, ObjectBean.MODIFIED_ORDER);
        return(rtnList);
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Level Set Methods">

    /* **************************************************
     * S E R V I C E   L E V E L   S E T   M E T H O D S
     *
     * All set methods should be interfaced at the Service
     * level as this allows consistency across the API and
     * security for the underlying sets. If we have sets at
     * the Service level, the Manager level and ObjectStore,
     * the inconsistency might cause a set to be made to the
     * manager when a set in the service adds additional logic
     * and thus is bypassed.
     * **************************************************/

    /**
     * saves a booking into a history list with a key of the original bookingId. The
     * history BookingBean as a unique id but all the other values are retained.
     *
     * @param booking the booking to get archived
     * @return the archived booking
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public BookingBean setBookingHistory(BookingBean booking) throws MaxCountReachedException, PersistenceException, NullObjectException {

        BookingBean history = bookingHistory.cloneObjectBean(bookingHistory.generateIdentifier(), booking);
        bookingHistory.setObject(booking.getBookingId(), history);
        return(history);
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private Class Methods">

    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     *
     * This section is for all private methods for the
     * class. Private methods should be carefully used
     * so as to avoid multi jump calls and as a general
     * rule of thumb, only when multiple methods use a
     * common algorithm.
     * ***************************************************/

    //</editor-fold>
}
