/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)StaffBookingService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.profile.StaffService;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.MRHolder;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code StaffBookingService} Class is a encapsulated API, service level singleton that provides
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
 * @version 1.00 17-Dec-2011
 */
public class StaffBookingService {
    // reference constance for readability

    public final static int ROOM_TYPE = 0;
    public final static int ACTIVITY_TYPE = 1;
    // Singleton Instance
    private volatile static StaffBookingService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final BookingManager staffBookingManager = new BookingManager(VT.STAFF_BOOKING.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final ActivityManager activityManager = new ActivityManager(VT.STAFF_ACTIVITY.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private StaffBookingService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code StaffBookingService} class
     * @return instance of the {@code StaffBookingService}
     * @throws PersistenceException
     */
    public static StaffBookingService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (StaffBookingService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new StaffBookingService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code StaffBookingService} class has been initialised. This is used
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
     * initialise all the managed classes in the {@code StaffBookingService}. The
     * method returns an instance of the {@code StaffBookingService} so it can be chained.
     * This must be called before a the {@code StaffBookingService} is used. e.g  {@code StaffBookingService myStaffBookingService = }
     * @return instance of the {@code StaffBookingService}
     * @throws PersistenceException
     */
    public synchronized StaffBookingService init() throws PersistenceException {
        if(initialise) {
            staffBookingManager.init();
            activityManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code StaffBookingService}. The
     * method returns an instance of the {@code StaffBookingService} so it can be chained.
     * @return instance of the {@code StaffBookingService}
     * @throws PersistenceException
     */
    public StaffBookingService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code StaffBookingService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = staffBookingManager.clear() ? success : false;
        success = activityManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code StaffBookingService} class instance by setting the INSTANCE reference
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
    public BookingManager getStaffBookingManager() {
        return staffBookingManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
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
            case STAFF_BOOKING:
                return staffBookingManager.isIdentifier(id);
            case STAFF_ACTIVITY:
                return activityManager.isIdentifier(id);
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
     * Identifies if a staff activity can be removed. In order for an activity to be removable
     * there must be no staff Activity references.
     *
     * @param activityId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isActivityRemovable(int activityId)
            throws PersistenceException, NoSuchIdentifierException {

        for(BookingBean booking : staffBookingManager.getAllObjects()) {
            int roomId = booking.getRoomId();
            if(MRHolder.isType(ACTIVITY_TYPE, roomId) && MRHolder.getRef(roomId) == activityId) {
                return false;
            }
        }
        return true;
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
     * Creates a staff booking. If a booking already exists within the boundaries of the proposed periodSd
     * the old booking is removed and replaced with the new. If an existing booking overlaps by one then it
     * is considered linked and as such the end of the overlapping will be trimmed. Distinction between a
     * booking for a room and a booking for an activity is identified by the boolean isRoom.
     *
     * @param ywd the YWD of the booking
     * @param start the start of the period
     * @param end the end of the period
     * @param staffId the staff reference id the booking is for
     * @param refId a reference number for either a room or an activity
     * @param isRoom true if the booking is for a room, false if it is an activity
     * @param requestedYwd the ywd the booking was requested.
     * @param notes
     * @param owner
     * @return the created booking bean
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public BookingBean createStaffBooking(int ywd, int start, int end, int staffId, int refId, boolean isRoom,
            int requestedYwd, String notes, String owner) throws PersistenceException, NoSuchIdentifierException, MaxCountReachedException, IllegalActionException, NullObjectException {

        // Referential integrity
        if(!StaffService.getInstance().getStaffManager().isIdentifier(staffId)) {
            throw new NoSuchIdentifierException("The staffId [" + staffId + "] does not exist");
        }
        if(isRoom && !AgeRoomService.getInstance().getRoomConfigManager().isIdentifier(refId)) {
            throw new NoSuchIdentifierException("The roomId [" + refId + "] does not exist");
        }
        if(!isRoom && !this.getActivityManager().isIdentifier(refId)) {
            throw new NoSuchIdentifierException("The activityId [" + refId + "] does not exist");
        }
        // set up the BookingBean attributes
        int roomMr = MRHolder.getMR(isRoom ? ROOM_TYPE : ACTIVITY_TYPE, refId);
        int bookingSd = SDHolder.getSD(start, end - start);
        // Set the liableContactId and pickupId to -1 as not used for staff
        int liableContactId = -1;
        int bookingPickupId = -1;
        int bookingDropOffId = -1;

        List<BookingBean> existingBookings = staffBookingManager.getYwdBookingsForProfile(ywd, bookingSd, staffId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF);
        // check if there is a booking already
        if(existingBookings.isEmpty()) {
            // no booking so create
            return staffBookingManager.setCreateBooking(ywd, bookingSd, staffId, roomMr, liableContactId, bookingDropOffId, bookingPickupId, BTIdBits.ATTENDING_STANDARD, ActionBits.TYPE_FILTER_OFF, requestedYwd, notes, owner);
        }
        // booking exists but we need to check it is not just an overlap
        if(existingBookings.size() > 2) {
            // if there are more than 2 then there can't be any linking
            throw new IllegalActionException("More than two bookings exist. Remove a booking before creating a new one");
        }
        if(existingBookings.size() == 2) {
            // booking might fit between 2 others
            int leftSd = existingBookings.get(0).getBookingSd();
            int rightSd = existingBookings.get(1).getBookingSd();
            if(SDHolder.overlaps(leftSd, bookingSd) && SDHolder.overlaps(bookingSd, rightSd)) {
                // first reduce the bookingSd by one to trim the right overlap
                bookingSd--;
                LinkedList<BookingBean> rtnList = (LinkedList<BookingBean>) staffBookingManager.setCutBooking(ywd, bookingSd, staffId, roomMr, liableContactId, bookingDropOffId, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.UNDEFINED, ActionBits.ALL_OFF, requestedYwd, notes, owner);
                // last one is the cut into booking
                return (rtnList.getLast());
            } else {
                throw new IllegalActionException("Two bookings exist already where you are placing this booking");
            }
        }
        //must be only one
        int currentSd = existingBookings.get(0).getBookingSd();
        // check if there is an overlap and where the overlap might be
        if(SDHolder.overlaps(bookingSd, currentSd)) {
            bookingSd--;
            return staffBookingManager.setCreateBooking(ywd, bookingSd, staffId, roomMr, liableContactId, bookingDropOffId, bookingPickupId, BTIdBits.ATTENDING_STANDARD, ActionBits.ALL_OFF, requestedYwd, notes, owner);
        }
        if(SDHolder.overlaps(currentSd, bookingSd)) {
            LinkedList<BookingBean> rtnList = (LinkedList<BookingBean>) staffBookingManager.setCutBooking(ywd, bookingSd, staffId, roomMr, liableContactId, bookingDropOffId, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.UNDEFINED, 0, requestedYwd, notes, owner);
            return (rtnList.getLast());
        }
        // we have looked at every possibility so it must be to be replaced
        return staffBookingManager.setReplaceBooking(ywd, bookingSd, staffId, roomMr, liableContactId, bookingDropOffId, bookingPickupId, BTIdBits.ATTENDING_STANDARD, ActionBits.ALL_OFF, requestedYwd, notes, owner);
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
