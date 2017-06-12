/*
 * @(#)ChildBookingModel.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cma.exceptions.ExceedCapacityException;
import com.oathouse.ccm.cma.exceptions.RoomClosedException;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.RoomConfigBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@literal ChildBookingModel} Class is a Static API, service level Helper to create model
 * Booking Objects for a single week.
 *
 * @author Darryl Oatridge
 * @version 1.00 13-Dec-2011
 */
public final class ChildBookingModel {
    // private Method to avoid instantiation externally
    private ChildBookingModel() {
        // this should be empty
    }

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
     * Returns a MEMORY_ONLY Booking model populated according to the parameters passed. This is an
     * overloaded method that only includes the booking requests for a single account.
     *
     * @param accountId a single account from which to take the children
     * @param yw the yw to model
     * @param bookingTypeMask the bookingType filter
     * @param bookingTypeFlagMask the bookingFlag filter
     * @param includeArgs include child education and include live bookings
     * @return Model Booking populated.
     *
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    public static BookingManager getModelBookingManager(int accountId, int yw, int bookingTypeMask, int bookingTypeFlagMask, VABoolean... includeArgs) throws PersistenceException, NullObjectException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException {
        BookingManager bookingModel = new BookingManager();
        bookingModel.init();

        if(VABoolean.INCLUDE_LIVE_BOOKINGS.isIn(includeArgs)) {
            ChildBookingModel.setLiveBookings(bookingModel, yw, bookingTypeMask, bookingTypeFlagMask);
        }
        ChildBookingModel.setBookingRequests(bookingModel, accountId, yw, VABoolean.INCLUDE_EDUCATION.isIn(includeArgs));
        return bookingModel;
    }

    /**
     * Returns a MEMORY_ONLY Booking model populated according to the parameters passed. This is an
     * overloaded method that includes ALL booking requests.
     *
     * @param yw the yw to model
     * @param bookingTypeMask the bookingType filter
     * @param bookingTypeFlagMask the bookingFlag filter
     * @param includeArgs include live bookings and include education
     * @return Model Booking populated.
     *
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    public static BookingManager getModelBookingManager(int yw, int bookingTypeMask, int bookingTypeFlagMask,
            VABoolean... includeArgs) throws PersistenceException, NullObjectException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException {
        BookingManager bookingModel = new BookingManager("bookingManagerModel", ObjectDataOptionsEnum.MEMORY);
        bookingModel.init();

        if(VABoolean.INCLUDE_LIVE_BOOKINGS.isIn(includeArgs)) {
            ChildBookingModel.setLiveBookings(bookingModel, yw, bookingTypeMask, bookingTypeFlagMask);
        }
        ChildBookingModel.setBookingRequests(bookingModel, yw, VABoolean.INCLUDE_EDUCATION.isIn(includeArgs));
        return bookingModel;
    }

    /**
     * This method retrieves occupancy variants across a ywd and room based on the occupancy periodSd set that is
     * in the SystemProperties.
     * For more information on the variant see the Booking
     *
     * @param manager a Booking instance
     * @param ywd
     * @param roomId
     * @param bookingTypeIdMask
     * @return a map of periodSd-occupancy number
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     */
    public static Map<Integer, Integer> getYwdRoomOccupancyDefaultVariant(BookingManager manager, int ywd, int roomId, int bookingTypeIdMask) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, NullObjectException {
        // validate bookingManager
        if(manager == null) {
            throw new NullObjectException("BookingManager is null");
        }
        // validate roomId
        if(!AgeRoomService.getInstance().isId(roomId, VT.ROOM_CONFIG)) {
            throw new NoSuchIdentifierException("The roomId [" + roomId + "] does not exist");
        }
        Set<Integer> periodSdSet = PropertiesService.getInstance().getSystemProperties().getOccupancySdSet();
        return (manager.getYwdRoomOccupancyVariant(ywd, roomId, periodSdSet, bookingTypeIdMask));
    }

    /**
     * This method retrieves occupancy variants across a ywd based on the occupancy periodSd set that is
     * in the SystemProperties. Each room is taken in turn and added. Rooms that are closed are ignored.
     * For more information on the variant see the Booking
     *
     * @param manager a Booking instance
     * @param ywd
     * @param bookingTypeIdMask
     * @return a map of periodSd-occupancy number
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     */
    public static Map<Integer, Integer> getYwdOccupancyDefaultVariant(BookingManager manager, int ywd, int bookingTypeIdMask) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, NullObjectException {
        Map<Integer, Integer> rtnMap = new ConcurrentSkipListMap<>();
        Map<Integer, Integer> roomMap = new ConcurrentSkipListMap<>();
        for(RoomConfigBean room : AgeRoomService.getInstance().getOpenRooms(ywd)) {
            roomMap.putAll(getYwdRoomOccupancyDefaultVariant(manager, ywd, room.getRoomId(), bookingTypeIdMask));
            for(int periodSd : roomMap.keySet()) {
                if(!rtnMap.containsKey(periodSd)) {
                    rtnMap.put(periodSd, 0);
                }
                rtnMap.put(periodSd, rtnMap.get(periodSd) + roomMap.get(periodSd));
            }
        }

        return rtnMap;
    }

    public static List<Boolean> getCapacityLimitForWeek(BookingManager manager, int yw0, int roomId) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        List<Boolean> rtnList = new LinkedList<>();

        if(manager == null) {
            throw new NullObjectException("BookingManager is null");
        }
        // validate roomId
        if(!AgeRoomService.getInstance().isId(roomId, VT.ROOM_CONFIG)) {
            throw new NoSuchIdentifierException("The roomId [" + roomId + "] does not exist");
        }
        int bookingTypeMask = 0;
        int actionBits = 0;
        manager.getYwBookingsForRoom(yw0, roomId, bookingTypeMask, actionBits);
        return rtnList;
    }

    /**
     * Gets a list of billing beans (booking charges and fixed charges) for the period specified.  Will use "real" confirmed bookings
     * if the period required is within the confirmed period, then uses predicted bookings based on booking requests.  Only uses
     * fixed charges for manual items on the account.  Takes into account any previous invoicing where applicable.
     *
     * @param accountId
     * @param startYwd
     * @param endYwd
     * @param includeArgs include live bookings, include education, include adjustments and include loyalty
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    public static List<BillingBean> getPredictedBilling(int accountId, int startYwd, int endYwd, VABoolean... includeArgs) throws PersistenceException, NullObjectException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException{
        BillingService billingService = BillingService.getInstance();
        List<BillingBean> rtnList = new LinkedList<>();

        // get all the bookings within this year week period
        int startYw0 = YWDHolder.getYW(startYwd);
        int endYw0 = YWDHolder.getYW(endYwd);
        int yw0 = startYw0;
        while(yw0 <= endYw0) {
            BookingManager modelBookingManager = getModelBookingManager(accountId, yw0, BTIdBits.TYPE_PHYSICAL_BOOKING, BTFlagIdBits.TYPE_FLAG, includeArgs);
            for(int d = 0; d < YWDHolder.DAYS_IN_WEEK; d++) {
                // don't include any in the week that are before or after our yw range.
                if(yw0+d < startYwd || yw0+d > endYwd) {
                    continue;
                }
                for(BookingBean booking : modelBookingManager.getAllObjects(yw0+d)) {
                    rtnList.addAll(billingService.getPredictedBillingsForBooking(accountId, booking, includeArgs));
                }
            }
            yw0 = YWDHolder.add(yw0, 10);
        }
        // get the billings from the bookings
        return rtnList;
    }

    /**
     * Gets a list of billing beans (booking charges and fixed charges) for the period specified for the entire nursery.
     *
     * Will use "real" confirmed bookings if the period required is within the confirmed period, then uses predicted bookings based on booking requests.
     * Only uses fixed charges for manual items on accounts.  Takes into account any previous invoicing where applicable.
     *
     * @param accountIds
     * @param startYwd
     * @param endYwd
     * @param includeArgs include live bookings, include education, include adjustments and include loyalty
     * @return a list of BillingBean objects modelled for this period
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    public static List<BillingBean> getPredictedBilling(Set<Integer> accountIds, int startYwd, int endYwd, VABoolean... includeArgs) throws PersistenceException, NullObjectException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException {
        List<BillingBean> rtnList = new LinkedList<>();
        for(int accountId : accountIds) {
            rtnList.addAll(getPredictedBilling(accountId, startYwd, endYwd, includeArgs));
        }
        return rtnList;
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
     * resets the live bookings from the given SDHolder YW value, removing all existing modelled bookings and
     * remodelling the current booking manager of live bookings. The BookingType filter allows selected
     * booking types to be included.
     *
     * @param bookingModel the modelled bookingManager instance to propagate
     * @param yw the yw to model
     * @param bookingTypeMask the bookingType filter
     * @param bookingTypeFlagMask the bookingTypeFlag filter
     * @return the changed bookingManager
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     */
    protected static BookingManager setLiveBookings(BookingManager bookingModel, int yw, int bookingTypeMask, int bookingTypeFlagMask) throws PersistenceException, NullObjectException, IllegalValueException, NoSuchIdentifierException, IllegalActionException, MaxCountReachedException {
        ChildBookingService bookingService = ChildBookingService.getInstance();
        if(bookingModel == null) {
            throw new NullObjectException("the bookingModel is null");
        }
        // validate the yw and get the start ywd
        int yw0 = ChildBookingRequestService.getInstance().validateYw(yw);

        if(!bookingModel.clear()) {
            throw new PersistenceException("Unable to clear the the child booking model data");
        }
        for(BookingBean b: bookingService.getYwBookings(yw0, bookingTypeMask, bookingTypeFlagMask)) {
            bookingModel.setCreateBooking(b.getYwd(), b.getBookingSd(), b.getProfileId(), b.getRoomId(), b.getLiableContactId(), b.getBookingDropOffId(), b.getBookingPickupId(), b.getBookingTypeId(), b.getActionBits(), b.getRequestedYwd(), b.getNotes(), BookingBean.SYSTEM_OWNED);
        }
        return bookingModel;
    }

    /**
     * Takes any booking requests currently held in ALL accounts for the given period of time and converts them
     * to bookings. This is an addition to the existing model booking manager so if a clean model is required,
     * ensure the method clear() is used to remove all previous data if any exists. This method can be used in
     * conjunction with resetLiveBookings() but must be called after that method. Live bookings take precedence
     * over booking requests and any booking for a child found on a ywd, irrelevant of bookingSd, where a booking
     * request is being made, will result in the booking request being rejected.
     *
     * @param bookingModel the modelled bookingManager instance to propagate
     * @param yw the yw to model
     * @param includeChildEducationSd
     * @return Booking with any additions
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    protected static BookingManager setBookingRequests(BookingManager bookingModel, int yw, boolean includeChildEducationSd) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalValueException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException {
        Set<Integer> childIdSet = ChildBookingRequestService.getInstance().getBookingRequestRangeManager().getAllKeys();
        return ChildBookingModel.setBookingFromRequests(bookingModel, yw, childIdSet, includeChildEducationSd);
    }

    /**
     * Takes any booking requests currently held in a SINGLE account for the given period of time and converts them
     * to bookings. This is an addition to the existing model booking manager so if a clean model is required,
     * ensure the method clear() is used to remove all previous data if any exists. This method can be used in
     * conjunction with resetLiveBookings() but must be called after that method. Live bookings take precedence
     * over booking requests and any booking for a child found on a ywd, irrelevant of bookingSd, where a booking
     * request is being made, will result in the booking request being rejected.
     *
     * @param bookingModel the modelled bookingManager instance to propagate
     * @param accountId a single account from which to take the children
     * @param yw the yw to model
     * @param includeChildEducationSd
     * @return Booking with any additions
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalValueException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     */
    protected static BookingManager setBookingRequests(BookingManager bookingModel, int accountId, int yw, boolean includeChildEducationSd) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalValueException, NoSuchKeyException, IllegalActionException, RoomClosedException, MaxCountReachedException, ExceedCapacityException {

        // validate the accountId
        if(!ChildService.getInstance().isId(accountId, VT.ACCOUNT)) {
            throw new NoSuchIdentifierException("The account id '" + accountId + "' does not exist");
        }
        Set<Integer> childIdSet = ChildService.getInstance().getAllChildIdsForAccount(accountId);
        return ChildBookingModel.setBookingFromRequests(bookingModel, yw, childIdSet, includeChildEducationSd);
    }

    /**
     * Creates a new booking in the model booking manager. This is an addition to the existing model booking manager
     * so if a clean model is required, ensure the method clear() is used to remove all previous data if any exists.
     * This method can be used in conjunction with resetLiveBookings() but must be called after that method.If there
     * is already an existing booking within the start/end periodSd then the underlying booking will be replaced
     * following the rules of Booking setReplaceBooking() method. If there is more than one booking then
     * all the bookings will be consolidated into a single booking following the rules of Booking
     * setConsolidatedBooking() method.
     *
     * <p> If you wish to set an object to immutable it must not already exist. You can only set newly created bookings
     * to immutable. If a booking exists and immutable is set to true then it will be ignored. </p>
     *
     * <p> All parameters are validated and integrity checked. </p>
     * <p> All parameters are validated and integrity checked. </p>
     *
     * @param bookingModel the modelled bookingManager instance to propagate
     * @param ywd a YWDHolder key value
     * @param roomId the roomId of the booking
     * @param bookingSd the periodSd for this booking
     * @param childId the childId the booking is for
     * @param liableContactId the liability contact on the child account
     * @param bookingDropOffId a drop off custodian contact
     * @param bookingPickupId a pickup custodian contact
     * @param bookingTypeId the type of booking
     * @param actionBits the value of actionBits
     * @param notes any notes for the booking
     * @param exceedCapacity true if the booking can exceed capacity
     * @param includeChildEducation if child education, where applicable, should be included in timetable
     * @param requestedYwd the ywd from which to check the notice period
     * @param owner the owner of this booking request
     * @return the newly created booking
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     * @throws ExceedCapacityException
     * @see BTBits
     */
    protected static BookingBean setBooking(BookingManager bookingModel, int ywd, int roomId, int bookingSd, int childId, int liableContactId,
            int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, String notes,
            boolean exceedCapacity, boolean includeChildEducation, int requestedYwd, String owner) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, NullObjectException, MaxCountReachedException, IllegalActionException, ExceedCapacityException {

        int chkBookingTypeId = bookingTypeId;

        // Don't validate if the BOOKING_CLOSED_FLAG is on
        if(!ChildBookingService.getInstance().getBookingTypeManager().getObject(bookingTypeId).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
            TimetableService.getInstance().validatePeriodSd(ywd, bookingSd, roomId, childId, includeChildEducation);
        }
        bookingModel = ChildBookingModel.setNoticePeriodRestrictions(bookingModel, ywd, requestedYwd, childId);

        // there is only a single BookingBean to return
        BookingBean rtnBooking;
        List<BookingBean> bookingList = bookingModel.getYwdBookingsForProfile(ywd, bookingSd, childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
        // check if we are going to exceed capacity
        if(!exceedCapacity && BTIdBits.isFilter(bookingTypeId, BTBits.ATTENDING_BIT)) {
            int confirmed = bookingModel.getBookingCount(ywd, roomId, bookingSd, BTBits.ATTENDING_BIT);
            int capacity = AgeRoomService.getInstance().getRoomConfigManager().getObject(roomId).getCapacity();
            // TODO Test this works. cater for this child booking exists and will be repleaced
            if(!bookingList.isEmpty()) {
                confirmed--;
            }
            if(confirmed >= capacity) {
                //show aspecial exception as over capacity (user must book a WAITING booking)
                throw new ExceedCapacityException("CapacityExceeded");
            }
        }

        if(bookingList.isEmpty()) {
            rtnBooking = bookingModel.setCreateBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, chkBookingTypeId, actionBits, requestedYwd, notes, owner);
        } else {
            rtnBooking = bookingModel.setReplaceBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, chkBookingTypeId, actionBits, requestedYwd, notes, owner);
        }
        // if the RESET_SPANSD_FLAG is on then set the SpanSD to be the bookingSd
        if(ChildBookingService.getInstance().getBookingTypeManager().getObject(bookingTypeId).isFlagOn(BTFlagBits.RESET_SPANSD_FLAG)) {
            bookingModel.setSpanSdToBookingSd(ywd, rtnBooking.getBookingId(), owner);
        }
        return rtnBooking;
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

    private static BookingManager setBookingFromRequests(BookingManager bookingModel, int yw, Set<Integer> childIdSet, boolean includeChildEducationSd) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException, NullObjectException, MaxCountReachedException, ExceedCapacityException, IllegalValueException {
        // validate the yw and get the start ywd
        int yw0 = ChildBookingRequestService.getInstance().validateYw(yw);

        int requestYwd = CalendarStatic.getRelativeYWD(0);
        boolean exceeedCapacity = PropertiesService.getInstance().getSystemProperties().isToExceedCapacityWhenInsertingRequests();

        Map<BookingRequestBean, Set<Integer>> requestList = new ConcurrentSkipListMap<>(ObjectBean.MODIFIED_ORDER);
        // for each day
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            int ywd = YWDHolder.add(yw0, day);
            requestList.clear();
            for(int childId : childIdSet) {
                // disregard accounts that are suspended
                if(ChildService.getInstance().isAccountSuspended(childId)) {
                    continue;
                }
                // Only include the request if there are no bookings for this child
                if(bookingModel.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId, BTIdBits.TYPE_BOOKING, ActionBits.ALL_OFF).isEmpty()) {
                    BookingRequestBean request = ChildBookingRequestService.getInstance().getBookingRequest(ywd, childId);
                    if(request.getRequestId() != BookingRequestManager.NO_REQUEST) {
                        if(!requestList.containsKey(request)) {
                            requestList.put(request, new ConcurrentSkipListSet<Integer>());
                        }
                        requestList.get(request).add(childId);
                    }
                }
            }
            // the requests are sorted so now make the bookings
            for(BookingRequestBean request : requestList.keySet()) {
                for(int childId : requestList.get(request)) {

                    int roomId = AgeRoomService.getInstance().getRoomForChild(childId, ywd).getRoomId();
                    for(int requestSd : request.getAllSd()) {
                        // initially set the bookingSd to the requestSd
                        int bookingSd = requestSd;
                        // validate the booking against the timetable
                        try {
                            bookingSd = includeChildEducationSd
                                    ? ChildBookingService.getInstance().getValidBookingSd(ywd, requestSd, roomId, childId, 0)
                                    : ChildBookingService.getInstance().getValidBookingSd(ywd, requestSd, roomId, 0);
                        } catch(RoomClosedException rce) {
                            // if the booking type isn't BOOKING_CLOSED_FLAG then move on
                            request.getBookingTypeId();
                            BookingTypeBean bookingType = ChildBookingService.getInstance().getBookingTypeManager().getObject(request.getBookingTypeId());
                            if(!bookingType.isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                                continue;
                            }
                        }
                        // set the booking
                        try {
                            ChildBookingModel.setBooking(bookingModel, ywd, roomId, bookingSd, childId,
                                    request.getLiableContactId(), request.getDropOffContactId(),
                                    request.getPickupContactId(), request.getBookingTypeId(), ActionBits.ALL_OFF, "",
                                    exceeedCapacity, includeChildEducationSd, requestYwd, ObjectBean.SYSTEM_OWNED);
                        } catch(ExceedCapacityException ece) {
                            // place this on the waiting list
                            int bookingTypeId = BTIdBits.setIdentifier(request.getBookingTypeId(), BTBits.WAITING_BIT);
                            ChildBookingModel.setBooking(bookingModel, ywd, roomId, bookingSd, childId,
                                    request.getLiableContactId(), request.getDropOffContactId(),
                                    request.getPickupContactId(), bookingTypeId, ActionBits.ALL_OFF, "",
                                    exceeedCapacity, includeChildEducationSd, requestYwd, ObjectBean.SYSTEM_OWNED);
                        }
                    }
                }
            }
        }
        return bookingModel;
    }

    /**
     * This ensures all bookings on a ywd have been set to RESTRICTED if their booking is within the notice period based
     * on the requestedYwd provided.
     */
    private static BookingManager setNoticePeriodRestrictions(BookingManager bookingModel, int ywd, int requestedYwd, int childId) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, NullObjectException, IllegalActionException {
        if(!ChildService.getInstance().isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        //only look at BookingTypeId that are of type booking
        int btIdMask = BTIdBits.TYPE_BOOKING;
        // ignore ActionBits
        int actionInclude = ActionBits.TYPE_FILTER_OFF;
        //get all the bookings for thischild on this day with restrictions
        List<BookingBean> allBookings = bookingModel.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId, btIdMask, actionInclude);
        for(BookingBean b : allBookings) {
            // we don't need to consider NO_CHARGE bookings as they do not have a notice period
            if(BTIdBits.isFilter(b.getBookingTypeId(), BTBits.NO_CHARGE_BIT)) {
                continue;
            }
            // these are restricted with regard to the notice day
            if((ChildBookingService.getInstance().getBookingTypeManager().getObject(b.getBookingTypeId()).isFlagOn(BTFlagBits.REGULAR_BOOKING_FLAG)
                    && AgeRoomService.getInstance().isWithinSpecialNoticePeriod(ywd, requestedYwd, b.getRoomId()))
                    || AgeRoomService.getInstance().isWithinStandardNoticePeriod(ywd, requestedYwd, b.getRoomId())) {
                if(b.isState(BookingState.OPEN)) {
                    bookingModel.setObjectState(ywd, b.getBookingId(), BookingState.RESTRICTED, ObjectBean.SYSTEM_OWNED);
                }
            }
        }
        return bookingModel;
    }
    //</editor-fold>
}
