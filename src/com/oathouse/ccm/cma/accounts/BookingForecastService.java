/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)BookingForecastService.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.*;
import com.oathouse.ccm.cma.booking.ChildBookingRequestService;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.exceptions.RoomClosedException;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BookingForecastBean;
import com.oathouse.ccm.cos.accounts.finance.BookingForecastManager;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingRequestBean;
import com.oathouse.ccm.cos.bookings.BookingRequestManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.config.RoomConfigManager;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NoSuchKeyException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@literal BookingForecastService} Class is a encapsulated API, service level singleton that provides
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
 * @version 1.00 20-Mar-2013
 */
public class BookingForecastService {
    // Singleton Instance
    private volatile static BookingForecastService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final BookingForecastManager bookingForecastManager = new BookingForecastManager();

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private BookingForecastService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@literal BookingForecastService} class
     * @return instance of the {@literal BookingForecastService}
     * @throws PersistenceException
     */
    public static BookingForecastService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (BookingForecastService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new BookingForecastService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@literal BookingForecastService} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying
     * elements of the initialisation are not available.
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return(true);
        }
        return(false);
    }

    /**
     * initialise all the managed classes in the {@literal BookingForecastService}. The
     * method returns an instance of the {@literal BookingForecastService} so it can be chained.
     * This must be called before a the {@literal BookingForecastService} is used. e.g  {@literal BookingForecastService myBillingModel = }
     * @return instance of the {@literal BookingForecastService}
     * @throws PersistenceException
     */
    public synchronized BookingForecastService init() throws PersistenceException {
        if(initialise) {
            bookingForecastManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@literal BookingForecastService}. The
     * method returns an instance of the {@literal BookingForecastService} so it can be chained.
     * @return instance of the {@literal BookingForecastService}
     * @throws PersistenceException
     */
    public BookingForecastService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@literal BookingForecastService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = bookingForecastManager.clear()?success:false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@literal BookingForecastService} class instance by setting the INSTANCE reference
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
    public BookingForecastManager getBookingForecastManager() {
        return bookingForecastManager;
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
     *
     * @param yw0
     * @param roomId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public int[][] getYwOccupancySdCount(int yw0, int roomId) throws PersistenceException, NoSuchIdentifierException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingRequestService bookingRequestService = ChildBookingRequestService.getInstance();
        AgeRoomService ageRoomService = AgeRoomService.getInstance();

        Set<Integer> occupancySdSet = propertiesService.getSystemProperties().getOccupancySdSet();

        int[][] rtnYwOccupancyArray = new int[7][occupancySdSet.size()];
        // run through all the children
        for(int childId : bookingRequestService.getBookingRequestRangeManager().getAllKeys()) {
            int day = 0;
            // get the weeks booking requests
            for(BookingRequestBean bookingRequest : bookingRequestService.getBookingRequestsForWeekChild(childId, yw0)) {
                // check the child is in this room
                if(ageRoomService.getRoomForChild(childId, yw0 + day).getRoomId() == roomId) {
                    int index = 0;
                    // check each of the occupancy Sd
                    for(int periodSd : occupancySdSet) {
                        int periodSd0 = SDHolder.getSD0(periodSd);
                        // ignore default as no booking request
                        if(!bookingRequest.isDefaultId()) {
                            // check each of the occupancy Sd values agaist the booking request.
                            for(int requestSd : bookingRequest.getRequestSdSet()) {
                                if(SDHolder.inRange(requestSd, periodSd0)) {
                                    rtnYwOccupancyArray[day][index]++;
                                    break;
                                }
                            }
                        }
                        index++;
                    }
                }
                day++;
            }
        }
        return rtnYwOccupancyArray;
    }

    /**
     * returns a total value, including VAT, for the all booking charges for a range of days.
     * If the range falls outside the confirmed period then Bookings from BookingRequests are
     * generated and calculated.
     *
     * @param accountId
     * @param startYwd
     * @param endYwd
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     */
    public long[] getForecastForRange(int accountId, int startYwd, int endYwd) throws PersistenceException, NoSuchIdentifierException, NullObjectException, NoSuchKeyException, IllegalValueException, MaxCountReachedException {
        BillingService billingService = BillingService.getInstance();

        VABoolean[] options = { INCLUDE_EDUCATION, INCLUDE_ADJUSTMENTS, INCLUDE_LOYALTY };

        // add all live bookings
        long[] rtnValue = this.getBookingValueFromLiveRange(accountId, startYwd, endYwd, options);
        // add booking requests
        rtnValue = BillingBean.addValueArray(rtnValue, this.getBookingValueFromRequestRange(accountId, startYwd, endYwd, options));

        // add any fixed items
        for(BillingBean billing : billingService.getPredictedFixedChargeBilling(accountId, endYwd)) {
            if(billing.getYwd() < startYwd) {
                continue;
            }
            rtnValue = BillingBean.addValueArray(rtnValue, billing.getSignedValueArray());
        }
        return rtnValue;
    }

    /**
     * returns a total value, including VAT, for the all booking charges for a month offset. ONLY live
     * bookings are included.
     *
     * @param account
     * @param monthOffset
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     */
    public long[] getForecastForMonth(int account, int monthOffset) throws PersistenceException, IllegalValueException, NoSuchIdentifierException, NullObjectException, NoSuchKeyException, MaxCountReachedException {

        int[] startEnd = CalendarStatic.getRelativeMonthStartEnd(monthOffset);
        return this.getForecastForRange(account, startEnd[0], startEnd[1]);
    }

    /**
     * returns the Account Holder charge and monthly statistics based on the value of bookings requests during a month period.
     * The charge is calculated as the percentage chargeRate, set in AccountHolderBean, of the billing
     * SESSION value.
     *
     * @param monthOffset
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     */
    public long getAccountHolderChargeForMonth(int monthOffset) throws PersistenceException, IllegalValueException, NullObjectException, MaxCountReachedException, NoSuchKeyException, NoSuchIdentifierException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();
        ChildService childService = ChildService.getInstance();
        BillingService reconciliationService = BillingService.getInstance();

        int[] startEnd = CalendarStatic.getRelativeMonthStartEnd(monthOffset);
        int startYwd = startEnd[0];
        int endYwd = startEnd[1];
        long totalValue = 0;
        for(int accountId : childService.getAccountManager().getAllIdentifier()) {
            for(ChildBean child : childService.getChildrenForAccount(accountId)) {
                for(int ywd = startYwd; ywd <= endYwd; ywd = YWDHolder.add(ywd, 1)) {
                    long value = 0L;
                    for(BookingBean booking : bookingService.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, child.getChildId(), BTIdBits.TYPE_PHYSICAL_BOOKING, BTFlagIdBits.TYPE_ALL)) {
                        BillingBean billing = reconciliationService.createBookingSessionCharge(accountId, booking, ObjectBean.SYSTEM_OWNED);
                        value += billing == null ? 0 : billing.getValue();
                    }
                    if(value == 0){
                        for(BookingBean booking : this.getYwdBookingFromRequest(ywd, accountId, child.getChildId())) {
                            BillingBean billing = reconciliationService.createBookingSessionCharge(accountId, booking, ObjectBean.SYSTEM_OWNED);
                            value += billing == null ? 0 : billing.getValue();
                        }
                    }
                    totalValue += value;
                }
            }
        }
        // now calculate the charge
        int chargeRate = propertiesService.getAccountHolderProperties().getChargeRate();
        return TDCalc.getDiscountValue(totalValue, chargeRate);
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
     * Changes made to configuration files might affect memory stored forecasts
     * with the booking not being modified. These changes might affect the value
     * of the forecast so reset removes all currently held forecast beans.
     * This method should be called in conjunction with price tariff changes and
     * education reduction changes.
     *
     * @throws PersistenceException
     */
    public void resetBookingForcast() throws PersistenceException {
        bookingForecastManager.clear();
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

    /*
     * get all bookings for a range making sure it is within the live range.
     * The range is expanded to be a full week
     */
    protected long[] getBookingValueFromLiveRange(int accountId, int startYwd, int endYwd, VABoolean... options) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, MaxCountReachedException, NullObjectException, IllegalValueException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();
        // set up the return value
        long[] rtnValue ={ 0, 0, 0 };
        // get confirmed weeks
        int confirmedWeeks = propertiesService.getSystemProperties().getConfirmedPeriodWeeks();

        // sort out all the dates to be begining and end of weeks
        int todayYwd = CalendarStatic.getRelativeYWD(0);
        int liveEndYwd = CalendarStatic.getRelativeYW(confirmedWeeks) + 6;
        // do maintenance
        bookingForecastManager.doHousekeeping(todayYwd);
        // check the dates are within the live range
        if(startYwd > liveEndYwd || endYwd < todayYwd) {
            return rtnValue;
        }
        // trim the dates if they are outside the live window
        int startChkYwd = startYwd < todayYwd ? todayYwd : startYwd;
        int endChkYwd = endYwd > liveEndYwd ? liveEndYwd : endYwd;

        // work through each booking
        for(BookingBean booking : bookingService.getAllBookingsForAccount(accountId, startChkYwd, endChkYwd)) {
            long _value[];
            // if the forcast exists and the modified dates are correct then return the forecast value
            if(bookingForecastManager.isIdentifier(booking.getYwd(), booking.getBookingId())
                    && booking.getModified() == bookingForecastManager.getObject(booking.getYwd(), booking.getBookingId()).getBookingModified()) {
                 _value =  bookingForecastManager.getObject(booking.getYwd(), booking.getBookingId()).getValueArray();
            } else {
                _value = this.getBookingBillingValue(accountId, booking, options);
                bookingForecastManager.setObject(booking.getYwd(), new BookingForecastBean(booking.getBookingId(),
                        accountId, booking.getYwd(), _value[BillingBean.VALUE_ONLY], _value[BillingBean.VALUE_INC_TAX],
                        booking.getProfileId(), booking.getModified(), BillingBean.SYSTEM_OWNED));
            }
            // add the value to the return value
            rtnValue = BillingBean.addValueArray(rtnValue, _value);
        }
        return rtnValue;
    }


    /*
     * get all bookings generated from the booking requests after the live confirmed period weeks
     */
    protected long[] getBookingValueFromRequestRange(int accountId, int startYwd, int endYwd, VABoolean... options) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, MaxCountReachedException, NullObjectException, IllegalValueException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildService childService = ChildService.getInstance();

        // set up the return value
        long[] rtnValue ={ 0, 0, 0 };

        int confirmedWeeks = propertiesService.getSystemProperties().getConfirmedPeriodWeeks();
        int requestStartYwd = CalendarStatic.getRelativeYW(confirmedWeeks);
        // check the end date is after the liveEndYwd
        if(endYwd < requestStartYwd) {
            return rtnValue;
        }
        // trim the start if it is before requestStartYwd
        int startChkYwd = startYwd < requestStartYwd ? requestStartYwd : startYwd;
        for(int ywd = startChkYwd; ywd <= endYwd; ywd = YWDHolder.add(ywd, 1)) {
            for(ChildBean child : childService.getChildrenForAccount(accountId)) {
                for(BookingBean booking : this.getYwdBookingFromRequest(ywd, accountId, child.getChildId())) {
                    // get the value of the booking into a local scope variable for readability
                    long _value[] = this.getBookingBillingValue(accountId, booking, options);
                    // add the value to the return value
                    rtnValue = BillingBean.addValueArray(rtnValue, _value);
                }
            }
        }
        return rtnValue;
    }

    /*
     * calculate a value of a booking
     */
    protected long[] getBookingBillingValue(int accountId, BookingBean booking, VABoolean... options) throws NoSuchKeyException, MaxCountReachedException, NullObjectException, IllegalValueException, NoSuchIdentifierException, PersistenceException {
        BillingService billingService = BillingService.getInstance();
        TransactionService transactionService = TransactionService.getInstance();

        List<BillingBean> billingList = billingService.getPredictedBillingsForBooking(accountId, booking, options);
        long[] rtnValues = transactionService.getBillingValues(billingList);

        return rtnValues;
    }

    /*
     * gets a list of bookings from a request for a child on a ywd. NOTE: the booking Id is the request Id
     */
    protected List<BookingBean> getYwdBookingFromRequest(int ywd, int accountId, int childId) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException {
        ChildBookingRequestService bookingRequestService = ChildBookingRequestService.getInstance();
        AgeRoomService roomService = AgeRoomService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        List<BookingBean> rtnList = new LinkedList<>();
        BookingRequestBean request = bookingRequestService.getBookingRequest(ywd , childId);
        if(request.getRequestId() == BookingRequestManager.NO_REQUEST) {
            return rtnList;
        }
        int roomId = roomService.getRoomForChild(childId, ywd).getRoomId();
        if(roomId == RoomConfigManager.NO_ROOM_ID) {
            return rtnList;
        }
        for(int requestSd : request.getAllSd()) {
            // initially set the bookingSd to the requestSd
            int bookingSd = requestSd;
            // validate the booking against the timetable
            try {
                bookingSd = bookingService.getValidBookingSd(ywd, requestSd, roomId, childId, 0);
            } catch(RoomClosedException rce) {
                // if the booking type isn't BOOKING_CLOSED_FLAG then move on
                request.getBookingTypeId();
                BookingTypeBean bookingType = bookingService.getBookingTypeManager().getObject(request.getBookingTypeId());
                if(!bookingType.isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                    continue;
                }
            }
            // variable constants for ease of reading the BookingBean contruct
            final int actualStart = -1;
            final int actualEnd = -1;
            final int actualDropoffId = -1;
            final int actualPickupId = -1;
            final String notes = "";
            // set the booking
            BookingBean booking = new BookingBean(request.getRequestId(), ywd, roomId, childId, bookingSd, bookingSd,
                    actualStart, actualEnd, request.getLiableContactId(), request.getDropOffContactId(),
                    request.getPickupContactId(), actualDropoffId, actualPickupId, request.getBookingTypeId(),
                    ActionBits.ALL_OFF, BookingState.RESTRICTED, ywd, notes, ObjectBean.SYSTEM_OWNED);
            rtnList.add(booking);
        }
        return rtnList;
    }

}
