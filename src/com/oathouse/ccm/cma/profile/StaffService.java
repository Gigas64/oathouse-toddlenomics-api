/*
 * @(#)StaffService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.profile;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.booking.StaffBookingService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.exceptions.BusinessRuleException;
import com.oathouse.ccm.cos.profile.StaffBean;
import com.oathouse.ccm.cos.profile.StaffManager;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.List;

/**
 * The {@code StaffService} Class is a encapsulated API, service level singleton that provides
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
 * @version 1.01 27-Sep-2011
 */
public class StaffService {

    // Singleton Instance
    private volatile static StaffService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Staff
    private final StaffManager staffManager = new StaffManager(VT.STAFF.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instanciation externally
    private StaffService() {
        // this should be empty
    }

    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * Singleton pattern to get the instance of the {@code StaffService} class
     * @return instance of the {@code StaffService}
     * @throws PersistenceException
     */
    public static StaffService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (StaffService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new StaffService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Reinitialises all the managed classes in the {@code StaffService}. The
     * method returns an instance of the {@code StaffService} so it can be chained.
     * @return instance of the {@code StaffService}
     * @throws PersistenceException
     */
    public StaffService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code StaffService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = staffManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * initialise all the managed classes in the {@code ${name}}. The
     * method returns an instance of the {@code ${name}} so it can be chained.
     * This must be called before a the {@code ${name}} is used. e.g  {@code ${name} my${name} = }
     * @return instance of the {@code ${name}}
     * @throws PersistenceException
     */
    public synchronized StaffService init() throws PersistenceException {
        if(initialise) {
            staffManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Mostly used for testing to reset the instance
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval Methods">
    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     *
     * This section allows the retrieval of the instance
     * of the manager to use as part of this service. For
     * ease of reference each manager has both a long and
     * a short name method call. ALL manager instances should
     * be referenced through the Service and not instanciated
     * directly.
     * ***************************************************/
    public StaffManager getStaffManager() {
        return staffManager;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Service Validation Methods">
    /* ***************************************************
     * S E R V I C E   V A L I D A T I O N   M E T H O D S
     *
     * This section is for all validation methods so that
     * other services can check blind id values are legitimate
     * and any associated business rules are adheared to.
     * ***************************************************/
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Service Level Get Methods">

    /* ***************************************************
     * S E R V I C E  L E V E L   G E T   M E T H O D S
     *
     * This section is for all get methods that require
     * information outside their own data store. All other
     * get methods can be found up the tree in the manager
     * or bean of the bean type.
     * ***************************************************/

    /**
     * Identifies if a staff member is able to be removed. For a staff member to be removed they must
     * have a leaving date in the past and not be allocated to any staff booking
     *
     * @param staffId
     * @return true if can be removed
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isStaffRemovable(int staffId) throws PersistenceException, NoSuchIdentifierException {
        StaffBookingService sbs = StaffBookingService.getInstance();
        int today = CalendarStatic.getRelativeYWD(0);
        // depart ywd must be in the past
        if(staffManager.getObject(staffId).getDepartYwd() >= today) {
            return false;
        }
        // no current bookings in the StaffBookingManager
        if(!sbs.getStaffBookingManager().hasBooking(staffId)) {
            return false;
        }
        return true;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Service Level Set Methods">
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
     * Create a new {@code StaffBean} object
     *
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param staffRef
     * @param notes
     * @param owner
     * @return the created StaffBean
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public StaffBean createStaff(String title, List<String> forenames, String surname, String commonName,
            String staffRef, String notes, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException {
        return staffManager.createStaff(title, forenames, surname, commonName, staffRef, notes, owner);
    }

    /**
     * Set the details in a {@code StaffBean} Object
     *
     * @param staffId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param staffRef
     * @param notes
     * @param owner
     * @return the modified StaffBean
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public StaffBean setStaffMainDetails(int staffId, String title, List<String> forenames, String surname,
            String commonName,
            String staffRef, String notes, String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        return staffManager.setStaffMainDetails(staffId, title, forenames, surname, commonName, staffRef, notes, owner);
    }

    /**
     * Set the staff address in a {@code StaffBean} object
     *
     * @param staffId
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param owner
     * @return the modified StaffBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public StaffBean setStaffAddress(int staffId, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        return staffManager.setStaffAddress(staffId, line1, line2, line3, city, province, postcode, country, owner);
    }

    /**
     * Set the contacts in a {@code StaffBean} Object
     *
     * @param staffId
     * @param phone
     * @param mobile
     * @param email
     * @param owner
     * @return the modified StaffBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public StaffBean setStaffContacts(int staffId, String phone, String mobile, String email, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        return staffManager.setStaffContacts(staffId, phone, mobile, email, owner);
    }

    /**
     * Set the status of a {@code StaffBean} object
     *
     * @param staffId
     * @param dateOfBirth
     * @param jobTitle
     * @param defaultRoom
     * @param startYwd
     * @param departYwd
     * @param noticeYwd
     * @param owner
     * @return the modified StaffBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public StaffBean setStaffStatus(int staffId, int dateOfBirth, String jobTitle, int defaultRoom, int startYwd,
            int departYwd, int noticeYwd, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        return staffManager.setStaffStatus(staffId, dateOfBirth, jobTitle, defaultRoom, startYwd, departYwd, noticeYwd, owner);
    }

    /**
     * Set the staff photo in a {@code StaffBean} Object
     *
     * @param staffId
     * @param photoId
     * @param owner
     * @return the modified StaffBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public StaffBean setStaffPhoto(int staffId, int photoId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        return staffManager.setStaffPhoto(staffId, photoId, owner);
    }

    /**
     * Remove a {@code StaffBean} Object but only if it is allowed to be removed (see isStaffRemovable())
     * If the {@code StaffBean} is removed then true is returned, if not, then false
     *
     * @param staffId
     * @param owner
     * @return true if successful
     * @throws BusinessRuleException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public boolean removeStaff(int staffId, String owner) throws BusinessRuleException, NoSuchIdentifierException, PersistenceException, NullObjectException {
        if(isStaffRemovable(staffId)) {
            // remove the staff bean
            staffManager.removeObject(staffId);
            return (true);
        }
        return (false);
    }


    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Private Class Methods">
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
