/*
 * @(#)ChildBookingRequestService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.*;
import static com.oathouse.ccm.cma.VABoolean.*;
import static com.oathouse.ccm.cma.VT.ACCOUNT_REQUEST;
import com.oathouse.ccm.cma.config.*;
import com.oathouse.ccm.cma.exceptions.*;
import com.oathouse.ccm.cma.profile.*;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.config.education.*;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.ContactBean;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code ChildBookingRequestService} Class is a encapsulated API, service level singleton that
 * provides access to related underlying manager instances. The class provides three distinct
 * sections: <h2>Manager Retrieval</h2> <p> This section allows the retrieval of the instance of the
 * manager to use as part of this service. For ease of reference each manager has both a long and a
 * short name method call. ALL manager instances should be referenced through the Service and not
 * instantiated directly. </p>
 *
 * <h2>Service Level get Methods</h2> <p> This section is for all get methods that require
 * information outside their own data store. All other get methods can be found up the tree in the
 * manager or bean of the bean type. </p>
 *
 * <h2>Service Level set Methods</h2> <p> All set methods must be implemented up to the Service
 * level as this allows consistency across the API and security for the underlying sets. If we have
 * sets at the Service level, the Manager level and ObjectStore, the inconsistency might cause a set
 * to be made to the manager when a set in the service adds additional logic and thus is bypassed.
 * </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Dec-2011
 */
public class ChildBookingRequestService {
    // Singleton Instance

    private volatile static ChildBookingRequestService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final BookingRequestManager bookingRequestManager = new BookingRequestManager(VT.ACCOUNT_REQUEST.manager());
    private final BookingRequestManager globalRequestManager = new BookingRequestManager(VT.GLOBAL_REQUEST.manager());
    private final DayRangeManager bookingRequestRangeManager = new DayRangeManager(VT.CHILD_REQUEST_RANGE.manager());

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private ChildBookingRequestService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code ChildBookingRequestService} class
     *
     * @return instance of the {@code ChildBookingRequestService}
     * @throws PersistenceException
     */
    public static ChildBookingRequestService getInstance() throws PersistenceException {
        if (INSTANCE == null) {
            synchronized (ChildBookingRequestService.class) {
                // Check again just incase before we synchronised an instance was created
                if (INSTANCE == null) {
                    INSTANCE = new ChildBookingRequestService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code ChildBookingRequestService} class has been initialised. This is
     * used mostly for testing to avoid initialisation of managers when the underlying elements of
     * the initialisation are not available.
     *
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if (INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code ChildBookingRequestService}. The method
     * returns an instance of the {@code ChildBookingRequestService} so it can be chained. This must
     * be called before a the {@code ChildBookingRequestService} is used. e.g  {@code ChildBookingRequestService myChildBookingRequestService =
     * }
     *
     * @return instance of the {@code ChildBookingRequestService}
     * @throws PersistenceException
     */
    public synchronized ChildBookingRequestService init() throws PersistenceException {
        if (initialise) {
            bookingRequestManager.init();
            globalRequestManager.init();
            bookingRequestRangeManager.init(ChildService.getInstance().getChildManager().getAllIdentifier());
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code ChildBookingRequestService}. The method
     * returns an instance of the {@code ChildBookingRequestService} so it can be chained.
     *
     * @return instance of the {@code ChildBookingRequestService}
     * @throws PersistenceException
     */
    public ChildBookingRequestService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code ChildBookingRequestService}. This is generally
     * used for testing. If you wish to refresh the object store reInitialise() should be used.
     *
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = bookingRequestManager.clear() ? success : false;
        success = bookingRequestRangeManager.clear() ? success : false;
        success = globalRequestManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory. <p> Used to reset the {@code ChildBookingRequestService}
     * class instance by setting the INSTANCE reference to null. This is mostly used for testing to
     * clear and reset internal memory stores when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval">
    /*
     * ***************************************************
     * M A N A G E R R E T R I E V A L
     *
     * This section allows the retrieval of the instance of the manager to use as part of this
     * service. For ease of reference each manager has both a long and a short name method call. ALL
     * manager instances should be referenced through the Service and not instantiated directly.
     * **************************************************
     */
    public BookingRequestManager getBookingRequestManager() {
        return bookingRequestManager;
    }

    public DayRangeManager getBookingRequestRangeManager() {
        return bookingRequestRangeManager;
    }

    public BookingRequestManager getGlobalRequestManager() {
        return globalRequestManager;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expand" desc="Service Validation Methods">
    /*
     * ***************************************************
     * S E R V I C E V A L I D A T I O N M E T H O D S
     *
     * This section is for all validation methods so that other services can check blind id values
     * are legitimate and any associated business rules are adhere to.
     * **************************************************
     */
    /**
     * Validation Method to check pure id values exist. This method ONLY checks an ID exists in a
     * manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch (validationType) {
            case ACCOUNT_REQUEST:
                return bookingRequestManager.isIdentifier(id);
            case GLOBAL_REQUEST:
                return globalRequestManager.isIdentifier(id);
            default:
                return (false);
        }
    }


    /**
     * Validation Method used to both clean and validate an YWDHolder yw value. The validation
     * ensures the yw is not before the current yw end.
     *
     * @param yw the YWDHolder yw value to be validated
     * @return a clean yw0
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    protected int validateYw(int yw) throws PersistenceException, IllegalValueException {
        int yw0 = YWDHolder.getYW(yw);
        // yw0 cannot be earlier than the current week
        if (yw0 < CalendarStatic.getRelativeYW(0)) {
            throw new IllegalValueException("yw " + yw0 + " is before current week");
        }
        return yw0;
    }

    /**
     * Tests to see if a booking request is eligible for removal
     *
     * @param accountId the account the request belongs too
     * @param requestId
     * @return true if is removable
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isBookingRequestRemovable(int accountId, int requestId) throws PersistenceException, NoSuchIdentifierException {
        // get the children from the account
        Set<Integer> childIdSet = ChildService.getInstance().getAllChildIdsForAccount(accountId);
        // booking requests are right refs for children
        for (int childId : bookingRequestRangeManager.getLeftRefForRightRef(requestId)) {
            if(!childIdSet.contains(childId)) {
                continue;
            }
            // all booking request day ranges for the child must be in the past
            if (!bookingRequestRangeManager.getAllObjects(childId, this.getThisYw(0), 9999010).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expand" desc="Service Level Get Methods">
    /*
     * ***************************************************
     * S E R V I C E L E V E L G E T M E T H O D S
     *
     * This section is for all get methods that require information outside their own data store.
     * All other get methods can be found up the tree in the manager or bean of the bean type.
     * **************************************************
     */
    /**
     * Finds the appropriate booking request to be applied for a child on a ywd from the booking
     * request layers in the BRRM
     *
     * @param ywd
     * @param childId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public BookingRequestBean getBookingRequest(int ywd, int childId) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        int accountId = ChildService.getInstance().getAccountForChild(childId).getAccountId();
        return (bookingRequestManager.getObject(accountId, bookingRequestRangeManager.getDayRange(childId, ywd).getRightRef()));
    }

    /**
     * returns the rightRef for each of the days of the week for a leftRef. If no DayRangeBean is
     * found The default value BookingRequestBean.
     *
     * @param childId
     * @param yw0
     * @return a List of rightRef integers for the week
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<BookingRequestBean> getBookingRequestsForWeekChild(int childId, int yw0) throws PersistenceException, NoSuchIdentifierException {
        LinkedList<BookingRequestBean> rtnList = new LinkedList<>();

        int accountId = ChildService.getInstance().getAccountForChild(childId).getAccountId();
        for (int requestId : bookingRequestRangeManager.getRightRefForWeekLeftRef(childId, yw0)) {
            rtnList.add(bookingRequestManager.getObject(accountId, requestId));
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week.
     *
     * @param yw0
     * @return a Map of the week
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public Map<Integer, List<BookingRequestBean>> getAllBookingRequestsForWeek(int yw0) throws PersistenceException, NoSuchIdentifierException {
        Map<Integer, List<BookingRequestBean>> rtnMap = new ConcurrentSkipListMap<>();
        for (int childId : bookingRequestRangeManager.getAllKeys()) {
            rtnMap.put(childId, getBookingRequestsForWeekChild(childId, yw0));
        }
        return (rtnMap);
    }

    //</editor-fold>

    //<editor-fold defaultstate="expand" desc="Service Level Set Methods">
    /*
     * **************************************************
     * S E R V I C E L E V E L S E T M E T H O D S
     *
     * All set methods should be interfaced at the Service level as this allows consistency across
     * the API and security for the underlying sets. If we have sets at the Service level, the
     * Manager level and ObjectStore, the inconsistency might cause a set to be made to the manager
     * when a set in the service adds additional logic and thus is bypassed.
     * *************************************************
     */
    /**
     * Creates a new booking request
     *
     * @param name
     * @param label
     * @param accountId
     * @param liableContactId
     * @param dropOffContactId
     * @param pickupContactId
     * @param requestSdInSet set of "in" sds
     * @param bookingTypeId
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public BookingRequestBean createBookingRequest(String name, String label, int accountId, int liableContactId,
                     int dropOffContactId, int pickupContactId, Set<Integer> requestSdInSet, int bookingTypeId, String owner)
            throws PersistenceException, IllegalActionException, NoSuchIdentifierException, NullObjectException, MaxCountReachedException {

        return setBookingRequest(-1, name, label, accountId, liableContactId, dropOffContactId, pickupContactId, requestSdInSet, bookingTypeId, owner);
    }

    /**
     * Updates a booking request as long as it is not scheduled in the bookingRequestRangeManager
     *
     * @param requestId
     * @param name
     * @param label
     * @param accountId
     * @param liableContactId
     * @param dropOffContactId
     * @param pickupContactId
     * @param requestSdInSet set of SDHolder "in" sds (e.g 13:00 not 12:59 as SDHolder)
     * @param bookingTypeId
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     */
    public BookingRequestBean setBookingRequest(int requestId, String name, String label, int accountId,
                    int liableContactId, int dropOffContactId, int pickupContactId, Set<Integer> requestSdInSet, int bookingTypeId, String owner)
            throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException {

        validateParameters(accountId, liableContactId, dropOffContactId, pickupContactId, bookingTypeId);
        int _requestId = requestId > 0 ? requestId : bookingRequestManager.generateIdentifier();
        return (bookingRequestManager.setObject(accountId,
                    new BookingRequestBean(_requestId, name, label, accountId, liableContactId, dropOffContactId, pickupContactId,
                                           cleanSds(requestSdInSet), bookingTypeId, owner)));
    }

    /**
     * Creates a global booking request
     *
     * @param name
     * @param label
     * @param requestSdInSet set of SDHolder "in" sds (e.g 13:00 not 12:59 as SDHolder)
     * @param bookingTypeId
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     */
    public BookingRequestBean setGlobalBookingRequest(String name, String label, Set<Integer> requestSdInSet, int bookingTypeId, String owner) throws PersistenceException, MaxCountReachedException, NullObjectException {
        int requestId = globalRequestManager.generateIdentifier();
        return globalRequestManager.setObject(ObjectEnum.DEFAULT_KEY.value(), new BookingRequestBean(requestId, name, label, ObjectEnum.DEFAULT_KEY.value(), -1, -1, -1, cleanSds(requestSdInSet), bookingTypeId, owner));
    }

    /**
     * Sets the Global Requests to all accounts. If an account already has a booking request
     * with the same PeriodSd and bookingTypeId then the global request is ignored.
     *
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public List<BookingRequestBean> setGlobalRequestsToAllAccounts(String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException, MaxCountReachedException {
        ChildService childService = ChildService.getInstance();

        List<BookingRequestBean> rtnList = new LinkedList<>();
        for(int accountId : childService.getAccountManager().getAllIdentifier()) {
            rtnList.addAll(this.setGlobalRequestsToAccount(accountId, owner));
        }
        return rtnList;
    }

    /**
     * Sets the current Global Booking Requests to the specified account. If an account already has a booking request
     * with the same PeriodSd and bookingTypeId then the global request is ignored.
     *
     * @param accountId
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public List<BookingRequestBean> setGlobalRequestsToAccount(int accountId, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException, MaxCountReachedException {
        List<BookingRequestBean> rtnList = new LinkedList<>();

        for(BookingRequestBean globalRequest : globalRequestManager.getAllObjects()) {
            // check there isn't already a booking request with these times and booking type
            String nameDup = "";
            boolean exists = false;
            for(BookingRequestBean currentRequest : bookingRequestManager.getAllObjects(accountId)) {
                if(globalRequest.getRequestSdSet().size() == currentRequest.getRequestSdSet().size()
                        && globalRequest.getRequestSdSet().containsAll(currentRequest.getRequestSdSet())) {
                    exists = true;
                    break;
                }
                if(globalRequest.getName().equalsIgnoreCase(currentRequest.getName())) {
                    nameDup = " (Global)";
                }
            }
            if(!exists) {
                ChildService childService = ChildService.getInstance();
                // get the first liable contact from the account
                int liableId = childService.getLiableContactsForAccount(accountId).get(0).getContactId();
                int requestId = bookingRequestManager.generateIdentifier();
                //create a booking request for this account from the global booking request
                rtnList.add(bookingRequestManager.setObject(accountId, new BookingRequestBean(requestId, globalRequest.getName() + nameDup,
                    globalRequest.getLabel(), accountId, liableId, liableId, liableId, globalRequest.getRequestSdSet(), globalRequest.getBookingTypeId(), owner)));
            }
        }
        return rtnList;
    }

    /**
     * Validates the parameters used in the previous two methods
     * @param accountId
     * @param liableContactId
     * @param dropOffContactId
     * @param pickupContactId
     * @param bookingTypeId
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    protected void validateParameters(int accountId, int liableContactId, int dropOffContactId, int pickupContactId, int bookingTypeId)
            throws NoSuchIdentifierException, PersistenceException, IllegalActionException {
        // validate parameters
        ChildService.getInstance().checkProfile(accountId, -1, liableContactId, -1);
        //we can only check they are contacts as we don't know the childId
        ChildService.getInstance().isId(dropOffContactId, VT.CONTACT);
        ChildService.getInstance().isId(pickupContactId, VT.CONTACT);
        BookingTypeBean bookingType = ChildBookingService.getInstance().getBookingTypeManager().getObject(bookingTypeId);
        if (!BTFlagIdBits.isFlag(bookingType.getFlagBits(), BTFlagBits.PREBOOKING_FLAG)) {
            throw new IllegalActionException("The bookingType " + bookingType.getName() + " does not have the Preebooking flag set");
        }
    }

    /**
     * Cleans an set of input Sds so as to remove 1 from the duration of each
     * @param setSdIn
     * @return
     */
    private Set<Integer> cleanSds(Set<Integer> setSdIn) {
        Set<Integer> cleanedSdSet = new ConcurrentSkipListSet<>();
        for (int sd : setSdIn) {
            cleanedSdSet.add(cleanSd(sd));
        }
        return cleanedSdSet;
    }

    /**
     * Cleans an input sd so as to reduce 1 from the duration
     *
     * @param sd
     * @return
     */
    private int cleanSd(int sd) {
        if (sd > 0) {
            int _start = SDHolder.getStart(sd);
            int _duration = SDHolder.getDuration(sd);
            return SDHolder.getSDIn(_start, _duration);
        }
        return (sd);
    }

    /**
     * Sets a child's requirements for bookings
     *
     * @param childId : leftRef
     * @param requestId : rightRef
     * @param startYwd
     * @param endYwd
     * @param mask can be null. Indicates the name of the mask to be applied
     * @param days
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws MaxCountReachedException
     */
    public void createBookingRequestDayRange(int childId, int requestId, int startYwd, int endYwd, String mask,
                                             boolean[] days, String owner) throws PersistenceException, MaxCountReachedException, NoSuchIdentifierException, NullObjectException {

        TimetableService ts = TimetableService.getInstance();

        // ranges are masked by "term time only" == ranges programmed in cs.educationRangeManager
        if (mask != null && mask.equals("term")) {
            List<DayRangeBean> educationPeriods = ts.getEducationRangeManager().getAllObjects(ChildEducationTimetableManager.ALL_ROOMS, startYwd, endYwd);
            int start;
            int end;
            boolean[] maskDays;
            for (DayRangeBean ed : educationPeriods) {
                start = ed.getStartYwd() < startYwd ? startYwd : ed.getStartYwd();
                end = ed.getEndYwd() > endYwd ? endYwd : ed.getEndYwd();
                maskDays = new boolean[7];
                for (int d = 0; d < 7; d++) {
                    maskDays[d] = days[d] & ed.getDays()[d];
                }
                bookingRequestRangeManager.setObject(childId,
                        new DayRangeBean(bookingRequestRangeManager.generateIdentifier(), childId, requestId, start, end, maskDays, owner));
            }
            return;
        }
        bookingRequestRangeManager.setObject(childId,
                new DayRangeBean(bookingRequestRangeManager.generateIdentifier(), childId, requestId, startYwd, endYwd, days, owner));
    }

    /**
     * Takes all the booking requests for a child over the ConfirmedPeriodWeeks and converts them
     * into bookings according to the booking request attributes. The method returns a {@code BookingExceptionDTO}
     * List of all bookingRequests where the reuestSd had to be altered due to a mismatch between
     * the requested period and the timetable periods. The booking is still created but with the
     * altered bookingSd
     *
     * @param childId
     * @param includeChildEducationSd
     * @param owner
     * @return list of BookingExceptionDTO
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws ExceedCapacityException
     * @throws IllegalValueException
     */
    public List<BookingExceptionDTO> setAllBookingRequestsForChild(int childId, boolean includeChildEducationSd, String owner) throws NoSuchIdentifierException, PersistenceException, MaxCountReachedException, NoSuchKeyException, NullObjectException, IllegalActionException, RoomClosedException, ExceedCapacityException, IllegalValueException {
        List<BookingExceptionDTO> rtnList = new LinkedList<>();
        Set<Integer> childIdList = asSet(childId);
        for (int i = 0; i < PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks(); i++) {
            int yw0 = CalendarStatic.getRelativeYW(i);
            rtnList.addAll(setYwBookingRequestsForChildren(yw0, childIdList, includeChildEducationSd, owner));
        }

        return rtnList;
    }

    /**
     * Takes all the booking requests in the system for a whole week, defined by the yw parameter,
     * and converts them to bookings according to the booking request attributes. The method returns
     * a {@code BookingExceptionDTO} List of all bookingRequests where the reuestSd had to be
     * altered due to a mismatch between the requested period and the timetable periods. The booking
     * is still created but with the altered bookingSd
     *
     * @param yw
     * @param includeChildEducationSd
     * @param owner
     * @return list of BookingExceptionDTO
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws ExceedCapacityException
     * @throws IllegalValueException
     */
    public List<BookingExceptionDTO> setYwBookingRequests(int yw, boolean includeChildEducationSd, String owner) throws MaxCountReachedException, NoSuchIdentifierException, NoSuchKeyException, PersistenceException, NullObjectException, IllegalActionException, RoomClosedException, ExceedCapacityException, IllegalValueException {
        Set<Integer> childIdSet = bookingRequestRangeManager.getAllKeys();
        return setYwBookingRequestsForChildren(yw, childIdSet, includeChildEducationSd, owner);
    }

    /**
     * Removes all booking requests and booking request schedules from an account
     *
     * @param accountId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void removeAllBookingRequests(int accountId) throws PersistenceException, NoSuchIdentifierException {
        ChildService childService = ChildService.getInstance();
        for(ChildBean child : childService.getChildrenForAccount(accountId)) {
            bookingRequestRangeManager.removeKey(child.getChildId());
        }
        bookingRequestManager.removeKey(accountId);
    }

    /**
     * Removes ALL booking requests and booking request schedules from ALL accounts cleaning out
     * the accounts of booking requests. This can be used when there has been a change in the timetable
     * and a new set of booking requests is required.
     *
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void removeAllBookingRequestsFromAllAccounts() throws PersistenceException, NoSuchIdentifierException {
        ChildService childService = ChildService.getInstance();
        for(int accountId : childService.getAccountManager().getAllIdentifier()) {
            for(ChildBean child : childService.getChildrenForAccount(accountId)) {
                bookingRequestRangeManager.removeKey(child.getChildId());
            }
            bookingRequestManager.removeKey(accountId);
        }
    }

    /**
     * Removes all the bookingRequestDayRange for a child
     *
     * @param childId
     * @throws PersistenceException
     */
    public void removeAllBookingRequestDayRangeForChild(int childId) throws PersistenceException {
        bookingRequestRangeManager.removeKey(childId);
    }

    /**
     * Terminates a child's booking request day range early, if the endYwd required is before the
     * existing one. If the range starts after the new endYwd, it is removed entirely. Allows old
     * ranges to be retained for ref integrity although the user cannot see them.
     *
     * @param leftRef
     * @param rangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws PersistenceException
     */
    public void terminateBookingRequestDayRange(int leftRef, int rangeId, int endYwd, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException {
        if (endYwd < bookingRequestRangeManager.getObject(leftRef, rangeId).getStartYwd()) {
            bookingRequestRangeManager.removeObject(leftRef, rangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if (endYwd < bookingRequestRangeManager.getObject(leftRef, rangeId).getEndYwd()) {
            bookingRequestRangeManager.setObjectEndYwd(leftRef, rangeId, endYwd, owner);
        }
    }

    public void terminateAllBookingRequestDayRange(int childId, int endYwd, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException {
        for(DayRangeBean dayRange : bookingRequestRangeManager.getAllObjects(childId)) {
            terminateBookingRequestDayRange(childId, dayRange.getRangeId(), endYwd, owner);
        }
    }

    /**
     * Moves one of a child's booking request day ranges up or down in the layers
     *
     * @param childId
     * @param rangeId
     * @param viewYw0
     * @param startYwd
     * @param moveUp
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public List<BookingRequestBean> moveBookingRequestDayRange(int childId, int rangeId, int viewYw0, int startYwd,
                                                               boolean moveUp, String owner)
            throws NoSuchIdentifierException, PersistenceException,
            NoSuchKeyException, NullObjectException, MaxCountReachedException {
        LinkedList<BookingRequestBean> rtnList = new LinkedList<>();
        for (int id : bookingRequestRangeManager.moveObject(childId, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(bookingRequestManager.getObject(id));
        }
        return (rtnList);
    }

    /**
     * Removes a booking request if it is not scheduled in the bookingRequestRangeManager
     *
     * @param accountId
     * @param requestId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void removeBookingRequest(int accountId, int requestId) throws PersistenceException, NoSuchIdentifierException {
        if (this.isBookingRequestRemovable(accountId, requestId)) {
            bookingRequestManager.removeObject(accountId, requestId);
        }
    }

    /**
     * Removes a global booking request.
     *
     * @param requestId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void removeGlobalBookingRequest(int requestId) throws PersistenceException, NoSuchIdentifierException {
        //global booking requests are always removable
        globalRequestManager.removeObject(ObjectEnum.DEFAULT_KEY.value(), requestId);

    }

    //</editor-fold>

    //<editor-fold defaultstate="expand" desc="Private Class Methods">
    /*
     * ***************************************************
     * P R I V A T E C L A S S M E T H O D S
     *
     * This section is for all private methods for the class. Private methods should be carefully
     * used so as to avoid multi jump calls and as a general rule of thumb, only when multiple
     * methods use a common algorithm.
     * **************************************************
     */
    private List<BookingExceptionDTO> setYwBookingRequestsForChildren(int yw, Set<Integer> childIdSet, boolean includeChildEducationSd, String owner)
            throws MaxCountReachedException, NoSuchIdentifierException, NoSuchKeyException,
            PersistenceException, NullObjectException, IllegalActionException, RoomClosedException, ExceedCapacityException, IllegalValueException {
        ChildService childService = ChildService.getInstance();
        AgeRoomService ageRoomService = AgeRoomService.getInstance();

        int yw0 = this.validateYw(yw);
        int requestYwd = CalendarStatic.getRelativeYWD(0);
        // take the opportunity to tidy up the booking requests
        Set<Integer> tidyChildIdSet = new ConcurrentSkipListSet<>();
        for(int childId : childIdSet) {
            // check the child exists
            if(!childService.isId(childId, VT.CHILD)) {
                continue;
            }
            // disregard accounts that are suspended
            if(childService.isAccountSuspended(childId)) {
                continue;
            }
            // MAINTENANCE:
            // if child has left remove all booking requests and remove from child room start
            int childDepartYwd = childService.getChildManager().getObject(childId).getDepartYwd();
            if(CalendarStatic.getToday() > childDepartYwd) {
                this.removeAllBookingRequestDayRangeForChild(childId);
                ageRoomService.removeAllChildRoomStart(childId);
                continue;
            }
            // now run through this week and check we don't have to terminate a booking request
            for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
                int ywd = yw0 + day;
                if(ywd > childDepartYwd) {
                    // this is a correct use of terminate not remove.
                    this.terminateAllBookingRequestDayRange(childId, childDepartYwd, owner);
                }
            }
            tidyChildIdSet.add(childId);
        }
        // set the VABoolean list
        List<VABoolean> args = new ArrayList<>();
        if (PropertiesService.getInstance().getSystemProperties().isToExceedCapacityWhenInsertingRequests()) {
            args.add(EXCEED_CAPACITY);
        }
        if (includeChildEducationSd) {
            args.add(INCLUDE_EDUCATION);
        }

        List<BookingExceptionDTO> rtnList = new LinkedList<>();
        /*
         * A map of bookingRequests sorted in Modify order so that first come first served is
         * applied. The map key points to a set of ChildIds. BookingRequests are Account based so
         * could apply to more than one child
         */
        Map<BookingRequestBean, Set<Integer>> requestList = new ConcurrentSkipListMap<>(ObjectBean.MODIFIED_ORDER);
        // for each day of the week
        for (int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            requestList.clear();
            int ywd = yw0 + day;
            for (int childId : tidyChildIdSet) {
                if(ywd > childService.getChildManager().getObject(childId).getDepartYwd()) {
                    continue;
                }
                BookingRequestBean request = getBookingRequest(ywd, childId);
                if (request.getRequestId() != BookingRequestManager.NO_REQUEST) {
                    if (!requestList.containsKey(request)) {
                        requestList.put(request, new ConcurrentSkipListSet<Integer>());
                    }
                    requestList.get(request).add(childId);
                }
            }
            // the requests are sorted so now make the bookings
            for (BookingRequestBean request : requestList.keySet()) {
                for (int childId : requestList.get(request)) {
                    int roomId = AgeRoomService.getInstance().getRoomForChild(childId, ywd).getRoomId();
                    for (int requestSd : request.getAllSd()) {
                        // check there is not a booking for this child already (that takes precedence over a booking request)
                        List<BookingBean> existingList = ChildBookingService.getInstance().getYwdBookingsForProfile(ywd, requestSd, childId, BTIdBits.TYPE_PHYSICAL_BOOKING, BTFlagIdBits.TYPE_ALL);
                        if (!existingList.isEmpty()) {
                            // we aren't going to make a booking so add to exception list
                            for (BookingBean booking : existingList) {
                                rtnList.add(new BookingExceptionDTO(ywd, booking.getBookingId(), childId,
                                        request.getBookingTypeId(), requestSd, requestSd, booking.getBookingTypeId(),
                                        booking.getBookingSd(), BookingExceptionDTO.Type.BOOKING_EXISTING));
                            }
                            continue;
                        }
                        // initially set the bookingSd to the requestSd
                        int bookingSd = requestSd;
                        // validate the requestSd against the timetable
                        try {
                            bookingSd = includeChildEducationSd
                                    ? ChildBookingService.getInstance().getValidBookingSd(ywd, requestSd, roomId, childId, 0)
                                    : ChildBookingService.getInstance().getValidBookingSd(ywd, requestSd, roomId, 0);
                        } catch(RoomClosedException rce) {
                            // if the booking type isn't BOOKING_CLOSED_FLAG then don't process
                            request.getBookingTypeId();
                            BookingTypeBean bookingType = ChildBookingService.getInstance().getBookingTypeManager().getObject(request.getBookingTypeId());
                            if(!bookingType.isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                                // add an booking exception
                                rtnList.add(new BookingExceptionDTO(ywd, -1, childId,
                                        request.getBookingTypeId(), requestSd, requestSd, -1,
                                        -1, BookingExceptionDTO.Type.BOOKING_CLOSED_ROOM));
                                continue;
                            }
                        }
                        // add the booking
                        boolean hasException = false;
                        BookingBean booking;
                        try {
                            booking = ChildBookingService.getInstance().setBooking(ywd, roomId, bookingSd, childId,
                                    request.getLiableContactId(), request.getDropOffContactId(),
                                    request.getPickupContactId(), request.getBookingTypeId(), "",
                                    requestYwd, owner, toArray(args));
                        }
                        catch (ExceedCapacityException ece) {
                            // place this on the waiting list
                            int bookingTypeId = BTIdBits.setIdentifier(request.getBookingTypeId(), BTBits.WAITING_BIT);
                            booking = ChildBookingService.getInstance().setBooking(ywd, roomId, bookingSd, childId,
                                    request.getLiableContactId(), request.getDropOffContactId(),
                                    request.getPickupContactId(), bookingTypeId, "",
                                    requestYwd, owner, toArray(args));
                            hasException = true;

                        }
                        // add a BookingRequestException if the bookingSd has changed or an exception is thrown due to waiting list
                        if (hasException || bookingSd != requestSd) {
                            rtnList.add(new BookingExceptionDTO(ywd, booking.getBookingId(), childId,
                                    request.getBookingTypeId(), requestSd, requestSd, booking.getBookingTypeId(),
                                    bookingSd, BookingExceptionDTO.Type.BOOKING_SD_CHANGE));
                        }
                     }
                }
            }
        }
        return (rtnList);
    }

    /*
     * reduced name for commonly used get todays date as a ywd
     */
    private int getThisYw(int plusWeeks) throws PersistenceException {
        return CalendarStatic.getRelativeYW(plusWeeks);
    }
    //</editor-fold>
}
