/*
 * @(#)ChildBookingService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.*;
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
import com.oathouse.ccm.cos.config.TimetableBean;
import com.oathouse.ccm.cos.config.TimetableManager;
import com.oathouse.ccm.cos.profile.*;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.collections.CollectionUtils;

/**
 * The {@code ChildBookingService} Class is a encapsulated API, service level singleton that
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
 * @version 1.00 30-Nov-2011
 */
public class ChildBookingService {
    // Singleton Instance

    private volatile static ChildBookingService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final BookingManager childBookingManager = new BookingManager(VT.CHILD_BOOKING.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final BookingTypeManager bookingTypeManager = new BookingTypeManager(VT.BOOKING_TYPE.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private ChildBookingService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code ChildBookingService} class
     *
     * @return instance of the {@code ChildBookingService}
     * @throws PersistenceException
     */
    public static ChildBookingService getInstance() throws PersistenceException {
        if (INSTANCE == null) {
            synchronized (ChildBookingService.class) {
                // Check again just incase before we synchronised an instance was created
                if (INSTANCE == null) {
                    INSTANCE = new ChildBookingService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code ChildBookingService} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying elements of the
     * initialisation are not available.
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
     * initialise all the managed classes in the {@code ChildBookingService}. The method returns an
     * instance of the {@code ChildBookingService} so it can be chained. This must be called before
     * a the {@code ChildBookingService} is used. e.g  {@code ChildBookingService myChildBookingService =
     * }
     *
     * @return instance of the {@code ChildBookingService}
     * @throws PersistenceException
     */
    public synchronized ChildBookingService init() throws PersistenceException {
        if (initialise) {
            childBookingManager.init();
            bookingTypeManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code ChildBookingService}. The method returns
     * an instance of the {@code ChildBookingService} so it can be chained.
     *
     * @return instance of the {@code ChildBookingService}
     * @throws PersistenceException
     */
    public ChildBookingService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code ChildBookingService}. This is generally used for
     * testing. If you wish to refresh the object store reInitialise() should be used.
     *
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = childBookingManager.clear() ? success : false;
        success = bookingTypeManager.clear() ? success : false;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory. <p> Used to reset the {@code ChildBookingService}
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
     * manager instances should be referenced through the Service and not instanciated directly.
     * **************************************************
     */
    public BookingManager getBookingManager() {
        return childBookingManager;
    }

    public BookingTypeManager getBookingTypeManager() {
        return bookingTypeManager;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">
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
            case BOOKING_TYPE:
                return bookingTypeManager.isIdentifier(id);
            case CHILD_BOOKING:
                return childBookingManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    /**
     * Determines whether a booking meets the requirements of the appropriate timetable given the
     * booking parameters
     *
     * @param booking
     * @param includeChildEducationSd
     * @return
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws RoomClosedException
     */
    public boolean isValid(BookingBean booking, boolean includeChildEducationSd) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException {
        return (booking.getBookingSd() == this.getValidBookingSd(booking, includeChildEducationSd, 0));
    }

    /**
     * tests a set of children to see if any are eligible for charging. Looks through all the booking where a childId
     * in the set of ChildId has a booking that has the ChargeBit set to anything other than NO_CHARGE
     *
     * @param childIdSet a set of child ids
     * @return true if a child has a booking that is chargeable
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isChildChargeable(Set<Integer> childIdSet) throws PersistenceException, NoSuchIdentifierException {
        for (int ywd : childBookingManager.getAllKeys()) {
            for(BookingBean booking : childBookingManager.getAllObjects(ywd)) {
                if(childIdSet.contains(booking.getProfileId()) && booking.getBookingTypeChargeBit() != BTBits.NO_CHARGE_BIT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * higher level check to test a booking action bit is on
     *
     * @param ywd
     * @param bookingId
     * @param actionBit
     * @return true if the action bit is found
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public boolean isBookingActionBitOn(int ywd, int bookingId, int actionBit) throws NoSuchIdentifierException, PersistenceException {
        return childBookingManager.getObject(ywd, bookingId).isActionOn(actionBit);
    }

    /**
     * Checks bookings in the bookingManager to match the user's settings with regard to timetables
     * and childEducation. Only looks at ATTENDING bookings.
     *
     * If the room turns out to be closed now, The new bookingTypeId will be BTBits.UNDEFINED and a
     * BookingExceptionDTO.Type.ROOM_MOVE_IMPOSSIBLE_CLOSED_ROOM
     *
     * Returns a list of BookingExceptionBeans for each booking that requires change.
     *
     * Disregards bookings with actuals set
     *
     * @param args variable length argument lists of ParamterEnums. This can be none
     * @return a list of BookingExceptionDTO objects
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public List<BookingExceptionDTO> validateBookings(VABoolean... args) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException {

        boolean includeChildEducationSd = INCLUDE_EDUCATION.isIn(args);
        List<BookingExceptionDTO> rtnList = new LinkedList<>();
        int day = getTodayYwd(0);
        int validSd;

        while (day < CalendarStatic.getRelativeYW(PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks())) {
            for (BookingBean booking : childBookingManager.getYwdBookings(day, BookingManager.ALL_PERIODS, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF)) {
                // restrict bookings within notice period is not necessary because this is a change made by the nursery, not a liable contact
                if (booking.isStateType(BookingState.TYPE_BOOKING)) {
                    try {
                        validSd = this.getValidBookingSd(booking, includeChildEducationSd, 0);
                        if (validSd != booking.getBookingSd()) {
                            rtnList.add(new BookingExceptionDTO(day, booking.getBookingId(), booking.getProfileId(),
                                    booking.getBookingTypeId(), booking.getBookingSd(), booking.getSpanSd(),
                                    booking.getBookingTypeId(), validSd, BookingExceptionDTO.Type.BOOKING_SD_CHANGE));
                        }
                    }
                    catch (RoomClosedException e) {
                        if(e.getMessage().equals("RoomClosed")) {
                            if(!bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                                rtnList.add(new BookingExceptionDTO(day, booking.getBookingId(), booking.getProfileId(),
                                        booking.getBookingTypeId(), booking.getBookingSd(), booking.getSpanSd(),
                                        BTIdBits.UNDEFINED, -1, BookingExceptionDTO.Type.BOOKING_CLOSED_ROOM));
                            }
                        }
                    }
                }
            }
            day = YWDHolder.add(day, 1);
        }
        return (rtnList);
    }

    /**
     * Checks the effect of re-booking a child to reflect their scheduled rooms - this method is designed for use after a child's
     * rooms have been rearranged.  Looks at all bookings from today forwards that are of type booking and that have yet to start.
     * Carries out a validation of the bookingSd in the new room and returns a list of all proposed changes
     * @param childId
     * @param args only INCLUDE_EDUCATION is considered
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public List<RoomMoveRebookingDTO> validateRoomMoveRebookings(int childId, VABoolean... args)
            throws PersistenceException, IllegalActionException, NoSuchIdentifierException, NoSuchKeyException {

        boolean includeChildEducationSd = INCLUDE_EDUCATION.isIn(args);
        List<RoomMoveRebookingDTO> rtnList = new LinkedList<>();
        int ywd = getTodayYwd(0);
        int validSd;
        int postBookingSpanSd;
        int postBookingRoomId;
        RoomConfigBean postBookingRoom;
        BookingTypeBean postBookingType;
        BookingBean postBooking;
        RoomMoveRebookingDTO.Type type = RoomMoveRebookingDTO.Type.ROOM_MOVE_OK;
        AgeRoomService ageRoomService = AgeRoomService.getInstance();

        while (ywd < CalendarStatic.getRelativeYW(PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks())) {
            // get the default room for the ywd
            try {
                postBookingRoomId = ageRoomService.getRoomForChild(childId, ywd).getRoomId();
                for (BookingBean preBooking : childBookingManager.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF)) {
                    // too late for this process if the booking has started
                    if(preBooking.isEqualOrAfterState(BookingState.STARTED)) {
                        continue;
                    }
                    // child is already booked into the required room
                    if(preBooking.getRoomId() == postBookingRoomId) {
                        continue;
                    }
                    if (preBooking.isStateType(BookingState.TYPE_BOOKING)) {
                        // the new new booking will look like this
                        postBooking = new BookingBean(preBooking.getBookingId(), ywd, postBookingRoomId, childId, preBooking.getBookingSd(), preBooking.getSpanSd(),
                                -1, -1, preBooking.getLiableContactId(), preBooking.getBookingDropOffId(), preBooking.getBookingPickupId(),
                                -1, -1, preBooking.getBookingTypeId(), preBooking.getActionBits(), preBooking.getState(), preBooking.getRequestedYwd(),
                                preBooking.getNotes(), preBooking.getOwner());
                        try {
                            validSd = this.getValidBookingSd(postBooking, includeChildEducationSd, 0);
                            if(validSd != preBooking.getBookingSd()) {
                                // new spanSd is the longer of the new bookingSd and the old spanSd
                                postBookingSpanSd = SDHolder.spanSD(validSd, preBooking.getSpanSd());
                                postBooking = new BookingBean(preBooking.getBookingId(), ywd, postBookingRoomId, childId, validSd, postBookingSpanSd,
                                    -1, -1, preBooking.getLiableContactId(), preBooking.getBookingDropOffId(), preBooking.getBookingPickupId(),
                                    -1, -1, preBooking.getBookingTypeId(), preBooking.getActionBits(), preBooking.getState(), preBooking.getRequestedYwd(),
                                    preBooking.getNotes(), preBooking.getOwner());
                                type = RoomMoveRebookingDTO.Type.ROOM_MOVE_SD_CHANGE;
                            }
                        }
                        catch (RoomClosedException e) {
                            // if the booking's type enables booking on a closed day, this is fine as is, but...
                            if (e.getMessage().equals("RoomClosed") && !bookingTypeManager.getObject(preBooking.getBookingTypeId()).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                                // cannot change the booking to anything
                                postBooking = null;
                                type = RoomMoveRebookingDTO.Type.ROOM_MOVE_IMPOSSIBLE_CLOSED_ROOM;
                            }
                        }
                        postBookingRoom = postBooking != null? ageRoomService.getRoomConfigManager().getObject(postBooking.getRoomId()) : null;
                        postBookingType = postBooking != null? bookingTypeManager.getObject(postBooking.getBookingTypeId()) : null;
                        rtnList.add(new RoomMoveRebookingDTO(preBooking,
                                ageRoomService.getRoomConfigManager().getObject(preBooking.getRoomId()),
                                bookingTypeManager.getObject(preBooking.getBookingTypeId()),
                                postBooking,
                                postBookingRoom,
                                postBookingType,
                                type));
                    }
                }
            }
            catch (NoSuchIdentifierException e) {
                // child does not have a room allocated on this day - continue
            }
            ywd = YWDHolder.add(ywd, 1);
        }
        return rtnList;
    }


    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Level Get Methods">
    /*
     * ***************************************************
     * S E R V I C E L E V E L G E T M E T H O D S
     *
     * This section is for all get methods that require information outside their own data store.
     * All other get methods can be found up the tree in the manager or bean of the bean type.
     * **************************************************
     */
    /**
     * This method retrieves occupancy variants across a ywd and room based on the occupancy
     * periodSd set that is in the SystemProperties. For more information on the variant see the
     * Booking
     *
     * @param ywd
     * @param roomId
     * @param bookingTypeIdMask
     * @return a map of periodSd-occupancy number
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     */
    public Map<Integer, Integer> getYwdRoomOccupancyDefaultVariant(int ywd, int roomId, int bookingTypeIdMask) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, NullObjectException {
        return (ChildBookingModel.getYwdRoomOccupancyDefaultVariant(childBookingManager, ywd, roomId, bookingTypeIdMask));
    }

    /**
     * This method retrieves occupancy variants across a ywd based on the occupancy periodSd set
     * that is in the SystemProperties. Each room is taken in turn and added. Rooms that are closed
     * are ignored. For more information on the variant see the Booking
     *
     * @param ywd
     * @param bookingTypeIdMask
     * @return a map of periodSd-occupancy number
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     */
    public Map<Integer, Integer> getYwdOccupancyDefaultVariant(int ywd, int bookingTypeIdMask) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, NullObjectException {
        return (ChildBookingModel.getYwdOccupancyDefaultVariant(childBookingManager, ywd, bookingTypeIdMask));
    }

    /**
     * This method retrieves occupancy variants across a ywd and room based on the timetable for
     * that room. See Booking for a fuller explanation.
     *
     * @param ywd
     * @param roomId
     * @param bookingTypeIdMask
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public Map<Integer, Integer> getYwdRoomOccupancyVariant(int ywd, int roomId, int bookingTypeIdMask) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        // validate roomId
        if (!AgeRoomService.getInstance().isId(roomId, VT.ROOM_CONFIG)) {
            throw new NoSuchIdentifierException("The roomId [" + roomId + "] does not exist");
        }

        Set<Integer> periodSdSet = TimetableService.getInstance().getTimetable(ywd, roomId).getAllPeriodSd();
        return (childBookingManager.getYwdRoomOccupancyVariant(ywd, roomId, periodSdSet, bookingTypeIdMask));
    }

    /**
     * gets the room occupancy trend as a percentage which is made up of the average room occupancy
     * over a day divided by the capacity of that room
     *
     * @param ywd
     * @param roomId
     * @param bookingTypeIdMask
     * @return the occupancy trend as a percentage
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public int getYwdRoomOccupancyTrend(int ywd, int roomId, int bookingTypeIdMask) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        // validate roomId
        if (!AgeRoomService.getInstance().isId(roomId, VT.ROOM_CONFIG)) {
            throw new NoSuchIdentifierException("The roomId [" + roomId + "] does not exist");
        }
        Set<Integer> periodSdSet = TimetableService.getInstance().getTimetable(ywd, roomId).getAllPeriodSd();
        int roomCapacity = AgeRoomService.getInstance().getRoomConfigManager().getObject(roomId).getCapacity();
        return (childBookingManager.getYwdRoomOccupancyTrend(ywd, roomId, roomCapacity, periodSdSet, bookingTypeIdMask));
    }

    /**
     * gets the occupancy trend of the nursery as a percentage which is made up of the average
     * nursery occupancy over a day divided by the capacity of that nursery
     *
     * @param ywd
     * @param bookingTypeIdMask
     * @return the trend of the nursery as a whole
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public int getYwdOccupancyTrend(int ywd, int bookingTypeIdMask) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        int trend = 0;
        int trendTotal = 0;
        int roomCount = 0;

        for (RoomConfigBean room : AgeRoomService.getInstance().getRoomConfigManager().getAllObjects()) {
            trendTotal += getYwdRoomOccupancyTrend(ywd, room.getRoomId(), bookingTypeIdMask);
            roomCount++;
        }
        if (roomCount > 0) {
            trend = trendTotal / roomCount;
        }
        return trend;
    }

    /**
     * Returns the maximum child occupancy of the nursery over a period of weeks. This looks at each
     * day over the defined period, counting the number of physical booking on that day for a single
     * child (multiple bookings for one child is counted as 1) and returning the largest number
     * found.
     *
     * @param numberOfWeeks the number of weeks to look for the Max over
     * @return the max child occupancy
     * @throws PersistenceException
     */
    public int getChildOccupancyMax(int numberOfWeeks) throws PersistenceException {
        int childMax = 0;
        Set<Integer> childSet = new ConcurrentSkipListSet<>();
        int bookingTypeMask = BTIdBits.TYPE_PHYSICAL_BOOKING;
        for (int day = 0; day < (numberOfWeeks * 7); day++) {
            int ywd = CalendarStatic.getRelativeYWD(day);
            for (BookingBean booking : childBookingManager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, bookingTypeMask, ActionBits.TYPE_FILTER_OFF)) {
                childSet.add(booking.getProfileId());
            }
            if (childSet.size() > childMax) {
                childMax = childSet.size();
            }
            childSet.clear();
        }
        return childMax;
    }

    /**
     * Calculates the mean number of minutes booked for this child between the start of the current
     * week and the future confirmed period
     *
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getCurrentBookingsAverageWeeklyMinutes(int childId) throws NoSuchIdentifierException, PersistenceException {
        // Business rule: only these statuses count towards time booked
        int bookingTypeMask = BTIdBits.TYPE_PHYSICAL_BOOKING;
        int yw0;
        int duration = 0;
        for (int w = 0; w < PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks(); w++) {
            yw0 = CalendarStatic.getRelativeYW(w);
            for (BookingBean b : childBookingManager.getYwBookingsForProfile(yw0, childId, bookingTypeMask, ActionBits.TYPE_FILTER_OFF)) {
                duration += b.getBookingDuration();
            }
        }
        return (duration / PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks());
    }

    /**
     * Finds a valid bookingSd from the bean provided where the periodSd is valid when considering
     * the appropriate timetable's valid starts and valid endPeriod options. If
     * includeChildEducationSd, the timetable is merged with the child's education requirements for
     * the day if appropriate. The method takes the provided BookingBean bookindSd
     *
     * @param booking
     * @param includeChildEducationSd
     * @param earliestStart
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws NoSuchKeyException
     */
    public int getValidBookingSd(BookingBean booking, boolean includeChildEducationSd, int earliestStart) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException {
        // throw exception if unmatched bean contains any actuals data
        if (booking.isEqualOrAfterState(BookingState.STARTED)) {
            throw new IllegalActionException("BookingContainsActuals");
        }
        // find the right timetable
        if (includeChildEducationSd) {
            return getValidBookingSd(booking.getYwd(), booking.getBookingSd(), booking.getRoomId(), booking.getProfileId(), earliestStart);
        }
        return getValidBookingSd(booking.getYwd(), booking.getBookingSd(), booking.getRoomId(), earliestStart);
    }

    /**
     * Finds a valid bookingSd from the bean provided where the periodSd is valid when considering
     * the appropriate timetable's valid starts and valid endPeriod options. If
     * includeChildEducationSd, the timetable is merged with the child's education requirements for
     * the day if appropriate. The method takes the provided BookingBean bookindSd
     *
     * @param ywd
     * @param periodSd
     * @param roomId
     * @param childId
     * @param earliestStart
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws RoomClosedException
     * @throws NoSuchKeyException
     */
    public int getValidBookingSd(int ywd, int periodSd, int roomId, int childId, int earliestStart) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, RoomClosedException {
        TimetableService timetableService = TimetableService.getInstance();
        // find the required timetable
        TimetableBean timetable = timetableService.getTimetableWithChildEducationSd(ywd, roomId, childId);
        // if room closed, throw exception
        if (timetable.getTimetableId() == TimetableManager.CLOSED_ID) {
            throw new RoomClosedException("RoomClosed");
        }
        return (timetable.getValidSd(periodSd, earliestStart));
    }

    /**
     * Finds a valid bookingSd from the bean provided where the periodSd is valid when considering
     * the appropriate timetable's valid starts and valid endPeriod options. If
     * includeChildEducationSd, the timetable is merged with the child's education requirements for
     * the day if appropriate. The method takes the provided BookingBean bookindSd
     *
     * @param ywd
     * @param periodSd
     * @param roomId
     * @param earliestStart
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws RoomClosedException
     * @throws NoSuchKeyException
     */
    public int getValidBookingSd(int ywd, int periodSd, int roomId, int earliestStart) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException, RoomClosedException {
        TimetableService timetableService = TimetableService.getInstance();
        // find the required timetable
        TimetableBean timetable = timetableService.getTimetable(ywd, roomId);
        // check if the room is closed
        if (timetable.getTimetableId() == TimetableManager.CLOSED_ID) {
            throw new RoomClosedException("RoomClosed");
        }
        return (timetable.getValidSd(periodSd, earliestStart));
    }

    /**
     * Finds the chargeable bookings for a single account
     *
     * @param accountId
     * @param firstYwd
     * @param lastYwd
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<BookingBean> getAllBookingsForAccount(int accountId, int firstYwd, int lastYwd) throws PersistenceException, NoSuchIdentifierException {
        ChildService childService = ChildService.getInstance();

        List<BookingBean> rtnList = new LinkedList<>();
        for (int ywd : childBookingManager.getAllKeys()) {
            if(ywd < firstYwd || ywd > lastYwd) {
                continue;
            }
            // get the children from this account
            for(ChildBean child : childService.getChildrenForAccount(accountId)) {
                rtnList.addAll(childBookingManager.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, child.getChildId(), BTIdBits.TYPE_CHARGE, ActionBits.TYPE_FILTER_OFF));
            }
        }
        return (rtnList);
    }

    /**
     * Finds the waiting list in the bookingManager for the next daysAhead number of days, including
     * today
     *
     * @param daysAhead the number of days from today to get the waiting list
     * @return list of BookingBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<BookingBean> getWaitingList(int daysAhead) throws NoSuchIdentifierException, PersistenceException {
        List<BookingBean> rtnList = new LinkedList<>();
        int ywd;
        for (int i = 0; i < daysAhead; i++) {
            ywd = getTodayYwd(i);
            rtnList.addAll(childBookingManager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF));
        }
        return (rtnList);
    }

    /**
     * Finds the entire waiting list from today to the confirmed period weeks
     *
     * @return a list of BookingBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<BookingBean> getWaitingList() throws NoSuchIdentifierException, PersistenceException {
        PropertiesService ps = PropertiesService.getInstance();
        // remaining days this week, including this one
        int daysCurrentWeek = 1 + YWDHolder.add(CalendarStatic.getRelativeYW(1), -1) - getTodayYwd(0);
        int daysRemainingConfirmedWeeks = (ps.getSystemProperties().getConfirmedPeriodWeeks() - 1) * 7;
        return (getWaitingList(daysCurrentWeek + daysRemainingConfirmedWeeks));
    }

    /**
     * An overload of the getAllObjects() method to allow detail filtering on booking state of all
     * BookingBean objects. Additionally includes BookingBean.
     *
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @param stateType a list of Booking State
     * @return List of BookingBean objects that are filtered on the masks
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<BookingBean> getAllBookingsWithState(int bookingTypeIdMask, int bookingTypeFlagMask, BookingState... stateType) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        List<BookingState> stateList = new LinkedList<>(Arrays.asList(stateType));
        for (int ywd : childBookingManager.getAllKeys()) {
            for(BookingBean booking: this.getYwdBookings(ywd, BookingManager.ALL_PERIODS, bookingTypeIdMask, bookingTypeFlagMask)) {
                if(stateList.isEmpty() || stateList.contains(booking.getState())) {
                    rtnList.add(booking);
                }
            }
        }
        return rtnList;
    }

    public List<BookingBean> getAllBookingsAuthorised(int bookingTypeIdMask, int bookingTypeFlagMask, BookingState... stateType) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        List<BookingState> stateList = new LinkedList<>(Arrays.asList(stateType));
        for (int ywd : childBookingManager.getAllKeys()) {
            for(BookingBean booking: this.getYwdBookings(ywd, BookingManager.ALL_PERIODS, bookingTypeIdMask, bookingTypeFlagMask)) {
                if(stateList.isEmpty() || stateList.contains(booking.getState())) {
                    rtnList.add(booking);
                }
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}) and
     * periodSd ({@code SDHolder}). The List is filtered by bookingTypeId where the booking contains
     * a BTBit that is in the BookingTypeMask and the bookingTypeFlagMask where the bookingTypeBean
     * flags match the bookingTypeFlagMask.
     *
     * <p>If the periodSd ({@code SDHolder} key value) is set to -1 then the whole day is taken </p>
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     * <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the behaviour is
     * unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value, -1 for whole day
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @see BTBits
     * @see BTFlagBits
     */

    public List<BookingBean> getYwdBookings(int ywd, int periodSd, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwdBookings(ywd, periodSd, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a week ({@code YWDHolder}). The List is
     * filtered by bookingTypeId where the booking contains a BTBit that is in the BookingTypeMask
     * and the bookingTypeFlagMask where the bookingTypeBean flags match the bookingTypeFlagMask.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     * <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the behaviour is
     * unpredictable</p>
     *
     * @param yw0 a YWDHolder key value
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @see BTBits
     */
    public List<BookingBean> getYwBookings(int yw0, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwBookings(yw0, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}) and
     * periodSd ({@code SDHolder}) for the specified profile. The List is filtered by bookingTypeId
     * where the booking contains a BTBit that is in the BookingTypeMask and the bookingTypeFlagMask
     * where the bookingTypeBean flags match the bookingTypeFlagMask.
     *
     * <p>If the periodSd ({@code SDHolder} key value) is set to -1 then the whole day is taken </p>
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     * <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the behaviour is
     * unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value, -1 for whole day
     * @param profileId the profileId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @see BTBits
     */

    public List<BookingBean> getYwdBookingsForProfile(int ywd, int periodSd, int profileId, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwdBookingsForProfile(ywd, periodSd, profileId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for week ({@code YWDHolder}) for the
     * specified profile. The List is filtered by bookingTypeId where the booking contains a BTBit
     * that is in the BookingTypeMask and the bookingTypeFlagMask where the bookingTypeBean flags
     * match the bookingTypeFlagMask.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     * <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the behaviour is
     * unpredictable</p>
     *
     * @param yw0 a YWDHolder key value for year week with day ignored
     * @param profileId the profileId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @see BTBits
     */

    public List<BookingBean> getYwBookingsForProfile(int yw0, int profileId, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwBookingsForProfile(yw0, profileId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}),
     * periodSd ({@code SDHolder}) and roomId. The List is filtered by bookingTypeId where the
     * booking contains a BTBit that is in the BookingTypeMask and the bookingTypeFlagMask where the
     * bookingTypeBean flags match the bookingTypeFlagMask.
     *
     * <p>If the time range ({@code SDHolder} key value) is set to -1 then the whole day is taken
     * </p> <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is
     * unpredictable</p> <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the
     * behaviour is unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value
     * @param roomId the roomId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */

    public List<BookingBean> getYwdBookingsForRoom(int ywd, int periodSd, int roomId, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwdBookingsForRoom(ywd, periodSd, roomId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a week ({@code YWDHolder}) for the
     * specified room. The List is filtered by bookingTypeId where the booking contains a BTBit that
     * is in the BookingTypeMask and the bookingTypeFlagMask where the bookingTypeBean flags match
     * the bookingTypeFlagMask.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     * <p>bookingTypeFlagMask must be a static taken from BTFlagBits or the behaviour is
     * unpredictable</p>
     *
     * @param yw0 a YWDHolder key value for year week with day ignored
     * @param roomId the roomId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param bookingTypeFlagMask a booking type flag mask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */

    public List<BookingBean> getYwBookingsForRoom(int yw0, int roomId, int bookingTypeIdMask, int bookingTypeFlagMask) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        for (BookingBean booking : childBookingManager.getYwBookingsForRoom(yw0, roomId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
            if (bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(bookingTypeFlagMask)) {
                rtnList.add(booking);
            }
        }
        return rtnList;
    }


    /**
     * Shortcut method to retrieve the bookingType flags from a booking
     *
     * @param ywd
     * @param bookingId
     * @return the booking type flags
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getBookingTypeFlagBits(int ywd, int bookingId) throws NoSuchIdentifierException, PersistenceException {
        int bookingTypeId = childBookingManager.getObject(ywd, bookingId).getBookingTypeId();
        return bookingTypeManager.getObject(bookingTypeId).getFlagBits();
    }

    /**
     * Shortcut method to test if a set of bookingType flag bits are on for a booking
     *
     * @param ywd
     * @param bookingId
     * @param btFlags
     * @return true if the BTFlag bit is set
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public boolean isBookingTypeFlagOn(int ywd, int bookingId, int btFlags) throws NoSuchIdentifierException, PersistenceException {
        int bookingTypeId = childBookingManager.getObject(ywd, bookingId).getBookingTypeId();
        return bookingTypeManager.getObject(bookingTypeId).isFlagOn(btFlags);
    }

    /**
     * Shortcut method to test the bookingTypeId in a booking
     *
     * @param ywd
     * @param bookingId
     * @param bookingTypeId
     * @return true if this is the same bookingTypeId
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public boolean isBookingType(int ywd, int bookingId, int bookingTypeId) throws NoSuchIdentifierException, PersistenceException {
        int currentBookingTypeId = childBookingManager.getObject(ywd, bookingId).getBookingTypeId();
        return bookingTypeManager.getObject(currentBookingTypeId).isType(bookingTypeId);
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Level Set Methods">

    /*
     * **************************************************
     * S E R V I C E L E V E L S E T M E T H O D S
     *
     * All set methods should be interfaced at the Service level as this allows consistency across
     * the API and security for the underlying sets. If we have sets at the Service level, the
     * Manager level and ObjectStore, the inconsistency might cause a set to be made to the manager
     * when a set in the service adds additional logic and thus is bypassed.
     * ************************************************
     */
    /**
     * Creates a new booking in the booking manager. If there is already an existing booking within
     * the start/end periodSd then the underlying booking will be replaced following the rules of
     * Booking setReplaceBooking() method. If there is more than one booking then all the bookings
     * will be consolidated into a single booking following the rules of Booking
     * setConsolidatedBooking() method. Changes to ATTENDING is checked against capacity unless
     * EXCEED_CAPACITY is passed as a VABoolean argument or the system properties is set to exceed
     * capacity.
     *
     * <p> All parameters are validated and integrity checked. </p>
     *
     * @param ywd a YWDHolder key value
     * @param roomId the roomId of the booking
     * @param bookingSd the periodSd for this booking
     * @param childId the childId the booking is for
     * @param liableContactId the liability contact on the child account
     * @param bookingDropOffId a drop off custodian contact
     * @param bookingPickupId a pickup custodian contact
     * @param bookingTypeId the type of booking
     * @param notes any notes for the booking
     * @param requestedYwd the ywd from which to check the notice period
     * @param owner the owner of this booking request
     * @param args variable length argument lists of ParamterEnums. This can be none
     * @return the newly created booking
     *
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     * @throws ExceedCapacityException
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     * @see BTBits
     */
    public BookingBean setBooking(int ywd, int roomId, int bookingSd, int childId, int liableContactId, int bookingDropOffId,
                                  int bookingPickupId, int bookingTypeId, String notes, int requestedYwd, String owner, VABoolean... args)
            throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, NullObjectException, MaxCountReachedException,
            IllegalActionException, ExceedCapacityException, IllegalArgumentException, IllegalValueException {

        int actionBits = ActionBits.ALL_OFF;
        return setCreateBooking(ywd, roomId, bookingSd, childId, liableContactId, bookingDropOffId, bookingPickupId,
                bookingTypeId, actionBits, notes, requestedYwd, owner, args);
    }

    /**
     * An overload method to reduce parameter entry when only the bookingTypeId is to be changed in
     * a booking. Booking history is still recorded and change to ATTENDING is checked against
     * capacity unless EXCEED_CAPACITY is passed as a VABoolean argument or the system properties is
     * set to exceed capacity.
     *
     * @param ywd
     * @param bookingId
     * @param bookingTypeId
     * @param args variable length argument lists of ParamterEnums. This can be none
     * @param owner
     * @return the changed booking
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws ExceedCapacityException
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     */
    public BookingBean setBooking(int ywd, int bookingId, int bookingTypeId, String owner, VABoolean... args)
            throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException,
            MaxCountReachedException, ExceedCapacityException, IllegalArgumentException, IllegalValueException {
        boolean exceedCapacity = EXCEED_CAPACITY.isIn(args);
        // if system exceed capacity is true then this overrides whatever is passed
        if (PropertiesService.getInstance().getSystemProperties().isToExceedCapacityWhenInsertingRequests()) {
            exceedCapacity = true;
        }
        // test if this is a transition back to an ATTENDING bookingType and we have capacity
        BookingBean booking = childBookingManager.getObject(ywd, bookingId);
        if (!exceedCapacity && BTIdBits.isNotBits(booking.getBookingTypeId(), BTBits.ATTENDING_BIT) && BTIdBits.isBits(bookingTypeId, BTBits.ATTENDING_BIT)) {
            int confirmed = childBookingManager.getBookingCount(ywd, booking.getRoomId(), booking.getBookingSd(), BTBits.ATTENDING_BIT);
            int capacity = AgeRoomService.getInstance().getRoomConfigManager().getObject(booking.getRoomId()).getCapacity();
            // as the original booking was not ATTENDING we don't have to adjust the confirmed count
            if (confirmed >= capacity) {
                //show aspecial exception as over capacity (user must book a WAITING booking)
                throw new ExceedCapacityException("CapacityExceeded");
            }
        }

        // check holiday allowance available if being asked for
        if (bookingTypeManager.getObject(bookingTypeId).isFlagOn(BTFlagBits.ALLOWANCE_FLAG)) {
            List<BookingBean> bookingList = childBookingManager.getYwdBookingsForProfile(booking.getYwd(), booking.getBookingSd(), booking.getProfileId(), BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
            checkAllowance(booking.getProfileId(), booking.getYwd(), bookingList);
        }

        BookingBean rtnBooking = childBookingManager.setObjectBookingTypeId(ywd, bookingId, bookingTypeId, owner);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(bookingTypeId).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(rtnBooking);
        }
        return rtnBooking;
    }

    /**
     * Sets all the bookings associated with a child from today forwards (as long as they are not authorised)
     * to be BTIdBits.CANCELLED_NOCHARGE. If the original booking BTFlagBits.HISTORY_FLAG is set then the
     * booking change is recorded in history
     *
     * @param profileId the profileId to be changed
     * @param owner
     * @return list of the changed bookings
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     * @throws IllegalArgumentException
     */
    public List<BookingBean> setAllBookingsCancelledNoChargeForProfile(int profileId, String owner) throws PersistenceException, NoSuchIdentifierException, MaxCountReachedException, NullObjectException, IllegalActionException {
        List<BookingBean> rtnList = new LinkedList<>();
        for(BookingBean booking : childBookingManager.getBookingsFromYwdForProfile(this.getTodayYwd(0), profileId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF)) {
            // check it is not authorised
            if(booking.isBeforeState(BookingState.AUTHORISED)) {
                BookingBean cancelledBooking = childBookingManager.setObjectBookingTypeId(booking.getYwd(), booking.getBookingId(), BTIdBits.CANCELLED_NOCHARGE, owner);
                // check the bookingType and see if we need to add this to HISTORY
                if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(cancelledBooking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                    ChildBookingHistory.getInstance().setBookingHistory(cancelledBooking);
                }
                rtnList.add(cancelledBooking);
            }
        }
        return rtnList;
    }

    /**
     * Creates a new booking in the booking manager. If there is already an existing booking within
     * the start/end periodSd then the underlying booking will be replaced following the rules of
     * Booking setReplaceBooking() method. If there is more than one booking then all the bookings
     * will be consolidated into a single booking following the rules of Booking
     * setConsolidatedBooking() method.
     *
     * <p> If you wish to set an object to immutable it must not already exist. You can only set
     * newly created bookings to immutable. If a booking exists and immutable is set to true then it
     * will be ignored. </p>
     *
     * <p> All parameters are validated and integrity checked. </p>
     *
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
     * @param args variable length argument lists of ParamterEnums. This can be none
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
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     * @see BTBits
     */
    protected BookingBean setCreateBooking(int ywd, int roomId, int bookingSd, int childId, int liableContactId,
                                           int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, String notes,
                                           int requestedYwd, String owner, VABoolean... args)
            throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, NullObjectException, MaxCountReachedException,
            IllegalActionException, ExceedCapacityException, IllegalArgumentException, IllegalValueException {

        // if this is an adhoc booking turn the ActionBits.ADHOC_BOOKING_BIT on
        if(ADHOC_BOOKING.isIn(args)) {
            actionBits = ActionBits.turnOn(actionBits, ActionBits.ADHOC_BOOKING_BIT);
        }
        // if this is an immediate recalculation then swet the action bits
        if(IMMEDIATE_RECALC.isIn(args)) {
            actionBits = ActionBits.turnOn(actionBits, ActionBits.IMMEDIATE_RECALC_BIT);
        }
        // get the booleans from the var args
        boolean exceedCapacity = EXCEED_CAPACITY.isIn(args);
        boolean includeChildEducation = INCLUDE_EDUCATION.isIn(args);
        boolean adminRebook = RESET_SPANSD.isIn(args);

        // if system exceed capacity is true then this overrides whatever is passed
        if (PropertiesService.getInstance().getSystemProperties().isToExceedCapacityWhenInsertingRequests()) {
            exceedCapacity = true;
        }

        int chkBookingTypeId = bookingTypeId;

        // validate passed parameters
        this.validateSetParameters(ywd, roomId, childId, liableContactId, bookingPickupId, chkBookingTypeId);

        // Don't validate if the BTFlagBits.BOOKING_CLOSED_FLAG or  is on or
        if (!bookingTypeManager.getObject(bookingTypeId).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)
                && !ADHOC_BOOKING.isIn(args)) {
            TimetableService.getInstance().validatePeriodSd(ywd, bookingSd, roomId, childId, includeChildEducation);
        }

        // make sure that all the bookingState values are correctly updated from the base notice period
        this.setNoticePeriodRestrictions(ywd, requestedYwd, childId);

        // check the bookingType and see if we need to add this to HISTORY
        boolean isHistory = bookingTypeManager.getObject(bookingTypeId).isFlagOn(BTFlagBits.HISTORY_FLAG);
        // there is only a single BookingBean to return and set history
        BookingBean rtnBooking;
        List<BookingBean> bookingList = childBookingManager.getYwdBookingsForProfile(ywd, bookingSd, childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
        // check if we are going to exceed capacity
        if (!exceedCapacity && BTIdBits.isFilter(bookingTypeId, BTBits.ATTENDING_BIT)) {
            int confirmed = childBookingManager.getBookingCount(ywd, roomId, bookingSd, BTBits.ATTENDING_BIT);
            int capacity = AgeRoomService.getInstance().getRoomConfigManager().getObject(roomId).getCapacity();
            // TODO Test this works. cater for this child booking exists and will be repleaced
            if (!bookingList.isEmpty()) {
                confirmed--;
            }
            if (confirmed >= capacity) {
                //show aspecial exception as over capacity (user must book a WAITING booking)
                throw new ExceedCapacityException("CapacityExceeded");
            }
        }

        // check holiday allowance available if being asked for
        if (bookingTypeManager.getObject(bookingTypeId).isFlagOn(BTFlagBits.ALLOWANCE_FLAG)) {
            checkAllowance(childId, ywd, bookingList);
        }

        if (bookingList.isEmpty()) {
            rtnBooking = childBookingManager.setCreateBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, chkBookingTypeId, actionBits, requestedYwd, notes, owner);
        }
        else {
            rtnBooking = childBookingManager.setReplaceBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, chkBookingTypeId, actionBits, requestedYwd, notes, owner);
        }

        // if the RESET_SPANSD_FLAG is on then set the SpanSD to be the bookingSd
        if (bookingTypeManager.getObject(bookingTypeId).isFlagOn(BTFlagBits.RESET_SPANSD_FLAG) || adminRebook) {
            childBookingManager.setSpanSdToBookingSd(ywd, rtnBooking.getBookingId(), owner);
        }
        if (isHistory) { // add history
            ChildBookingHistory.getInstance().setBookingHistory(rtnBooking);
        }
        return rtnBooking;
    }

    /**
     * Determines whether a child can use holiday allowance on a particular ywd
     * @param childId
     * @param ywd
     * @return
     * @throws PersistenceException
     */
    public boolean isAbleToUseHolAllowance(int childId, int ywd) throws PersistenceException {
        List<BookingBean> bookingList = childBookingManager.getYwdBookingsForProfile(ywd, -1, childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
        try {
            checkAllowance(childId, ywd, bookingList);
            return true;
        }
        catch (PersistenceException | IllegalValueException | IllegalArgumentException | NoSuchIdentifierException e) {
            return false;
        }
    }

    /**
     * Determines whether there is sufficient holiday allocation available
     *
     * @param childId
     * @param ywd
     * @param bookingList
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws IllegalArgumentException
     * @throws NoSuchIdentifierException
     */
    protected void checkAllowance(int childId, int ywd, List<BookingBean> bookingList)
            throws PersistenceException, IllegalValueException, IllegalArgumentException, NoSuchIdentifierException {
        ChildService childService = ChildService.getInstance();
        int allowanceUsed = childService.getHolUsedYWDs(childId).size();
        int allowanceAvailable = PropertiesService.getInstance().getHolidayConcession().isLimitProRata()?
                                 childService.getRoundedProRataHolAllocation(childId, ywd)
                                 : childService.getChildManager().getObject(childId).getHolidayAllocation();

        // first booking on a ywd
        if (CollectionUtils.isEmpty(bookingList) && allowanceUsed >= allowanceAvailable) {
            throw new IllegalArgumentException("HolidayAllowanceNotAvailable");
        }
        // there are already bookings on this ywd for this child
        else if (allowanceUsed > allowanceAvailable) {
            throw new IllegalArgumentException("HolidayAllowanceNotAvailable");
        }
        else if(allowanceUsed == allowanceAvailable) {
            boolean isExistingHolidayFromAllocationOnDay = false;
            for (BookingBean existing : bookingList) {
                if (bookingTypeManager.getObject(existing.getBookingTypeId()).isFlagOn(BTFlagBits.ALLOWANCE_FLAG)) {
                    isExistingHolidayFromAllocationOnDay = true;
                    break;
                }
            }
            if(!isExistingHolidayFromAllocationOnDay) {
                throw new IllegalArgumentException("HolidayAllowanceNotAvailable");
            }
        }
    }

    /**
     * Forces a booking into where an already existing booking resides. The Underlying booking will
     * have its spanSD cut to force in the new booking. The new booking will have its bookingTypeId
     * set to newBookingTypeId and the original booking given the cutBookingTypeId. If the cut
     * bookingTypeId is set to BTBits.UNDEFINED then the underlying bookingTypeId is taken.
     *
     * @param ywd a YWDHolder key value
     * @param roomId the roomId of the booking
     * @param bookingSd the periodSd for this booking
     * @param childId the childId the booking is for
     * @param liableContactId the liability contact on the child account
     * @param bookingDropOffId a drop off custodian contact
     * @param bookingPickupId a pickup custodian contact
     * @param newBookingTypeId the booking type for the newly created booking
     * @param cutBookingTypeId the booking type for the original cut booking
     * @param actionBits the value of actionBits
     * @param notes any notes for the booking
     * @param includeChildEducation if child education, where applicable, should be included in
     * timetable
     * @param requestedYwd the ywd the booking was requested
     * @param owner the owner of this booking request
     * @return a set of changed bookings with the new booking being the last in the list
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     */
    public List<BookingBean> setForceBooking(int ywd, int roomId, int bookingSd, int childId, int liableContactId,
                                             int bookingDropOffId, int bookingPickupId, int newBookingTypeId, int cutBookingTypeId, int actionBits,
                                             String notes, boolean includeChildEducation, int requestedYwd, String owner) throws PersistenceException, NoSuchKeyException, NullObjectException, NoSuchIdentifierException, MaxCountReachedException, IllegalActionException, IllegalValueException {
        // TODO add bookingDropOffId
        // validate passed parameters
        this.validateSetParameters(ywd, roomId, childId, -1, -1, newBookingTypeId);
        // validate the currentBookingTypeId
        if (BTIdBits.isFilter(cutBookingTypeId, BTIdBits.TYPE_IDENTIFIER)) {
            throw new NoSuchIdentifierException("The bookingTypeId " + newBookingTypeId + " is not valid");
        }
        // validate periodSd
        TimetableService.getInstance().validatePeriodSd(ywd, bookingSd, roomId, childId, includeChildEducation);

        // before we change anything make sure that all the bookingState values are correctly updated
        this.setNoticePeriodRestrictions(ywd, requestedYwd, childId);

        List<BookingBean> rtnList = childBookingManager.setCutBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, newBookingTypeId, cutBookingTypeId, actionBits, requestedYwd, notes, owner);

        // check the bookingType and see if we need to add this to HISTORY
        for (BookingBean booking : rtnList) {
            if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(newBookingTypeId).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                ChildBookingHistory.getInstance().setBookingHistory(booking);
            }
        }
        return rtnList;
    }

    /**
     * Used to add a booking without changing the underlying existing booking. The new booking must
     * have a bookingType with the BTBits.LAYER bit set.
     *
     * @param ywd a YWDHolder key value
     * @param roomId the roomId of the booking
     * @param bookingSd the periodSd for this booking
     * @param childId the childId the booking is for
     * @param liableContactId the liability contact on the child account
     * @param bookingDropOffId the value of bookingDropOffId
     * @param bookingPickupId a pickup custodian contact
     * @param bookingTypeId the type of booking
     * @param actionBits the value of actionsBits
     * @param bookingState the booking state
     * @param notes any notes for the booking
     * @param includeChildEducation if child education, where applicable, should be included in
     * timetable
     * @param requestedYwd the ywd the booking was requested
     * @param owner the owner of this booking request
     * @return newly created BookingBean
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     */
    public BookingBean setLayerBooking(int ywd, int roomId, int bookingSd, int childId, int liableContactId,
                                       int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, BookingState bookingState,
                                       String notes, boolean includeChildEducation, int requestedYwd, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, NoSuchKeyException, MaxCountReachedException, IllegalActionException, IllegalValueException {
        // TODO add bookingDropOffId
        // validate passed parameters
        this.validateSetParameters(ywd, roomId, childId, liableContactId, bookingPickupId, bookingTypeId);
        TimetableService.getInstance().validatePeriodSd(ywd, bookingSd, roomId, childId, includeChildEducation);

        // before we change anything make sure that all the bookingState values are correctly updated
        this.setNoticePeriodRestrictions(ywd, requestedYwd, childId);

        BookingBean rtnBooking = childBookingManager.setLayerBooking(ywd, bookingSd, childId, roomId, liableContactId, bookingDropOffId, bookingPickupId, bookingTypeId, actionBits, bookingState, requestedYwd, notes, owner);

        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(bookingTypeId).getFlagBits(), BTFlagBits.HISTORY_FLAG)) { // add history
            ChildBookingHistory.getInstance().setBookingHistory(rtnBooking);
        }
        return rtnBooking;

    }

    /**
     * Revalidates the bookingSd of all bookings that are OPEN or RESTRICTED and changes those that
     * do not match the current timetable. only bookings where the bookingSd does not fit into the
     * current booking are adjusted. Bookings that are on a day that is closed have their ActionBits
     * set with the additional bit ADHOC_BOOKING_BIT if it is not already set and if the booking is
     * not required, must be removed manually.
     *
     * @param owner
     * @param args variable length argument lists of ParamterEnums. This can be none
     * @return a list of the bookings that have exceptions
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws ExceedCapacityException
     * @throws MaxCountReachedException
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     */
    public List<BookingExceptionDTO> setRevalidatedSdBookings(String owner, VABoolean... args)
            throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NoSuchKeyException, NullObjectException,
            ExceedCapacityException, MaxCountReachedException, IllegalArgumentException, IllegalValueException {
        List<BookingExceptionDTO> rtnList = this.validateBookings(args);
        // reset any BookingExceptionBeans that have a BookingExceptionDTO.Type = ROOM_MOVE_SD_CHANGE
        BookingBean existingBooking;
        for (BookingExceptionDTO bookingException : rtnList) {
            existingBooking = childBookingManager.getObject(bookingException.getYwd(), bookingException.getBookingId());
            if (bookingException.getType().equals(BookingExceptionDTO.Type.BOOKING_SD_CHANGE)) {
                int bookingSd = bookingException.getPostBookingSd();
                this.setBooking(existingBooking.getYwd(), existingBooking.getRoomId(), bookingSd, existingBooking.getProfileId(), existingBooking.getLiableContactId(),
                        existingBooking.getBookingDropOffId(), existingBooking.getBookingPickupId(), existingBooking.getBookingTypeId(),
                        existingBooking.getNotes(), existingBooking.getRequestedYwd(), owner, addToArray(args, RESET_SPANSD));
            }
            else if (bookingException.getType().equals(BookingExceptionDTO.Type.BOOKING_CLOSED_ROOM)) {
                // if the booking type is available on a closed day, change to adhoc booking
                if(bookingTypeManager.getObject(existingBooking.getBookingTypeId()).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG)) {
                    this.setBookingActionBits(existingBooking.getYwd(), existingBooking.getBookingId(), ActionBits.ADHOC_BOOKING_BIT, true, owner);
                    // no need to record history because the booking has not changed from user's perspective
                }
                // otherwise cancel no charge (nursery's change to timetable)
                this.setBooking(existingBooking.getYwd(), existingBooking.getRoomId(), existingBooking.getBookingSd(), existingBooking.getProfileId(), existingBooking.getLiableContactId(),
                        existingBooking.getBookingDropOffId(), existingBooking.getBookingPickupId(), BTIdBits.CANCELLED_NOCHARGE,
                        existingBooking.getNotes(), existingBooking.getRequestedYwd(), owner, addToArray(args, RESET_SPANSD, ADHOC_BOOKING, IMMEDIATE_RECALC));
            }
        }
        return (rtnList);
    }

    /**
     * Changes all a child's bookings to reflect the room schedule defined in the AgeRoomService for the child, returning
     * a list of changes made
     * @param childId
     * @param owner
     * @param args
     * @return
     * @throws ExceedCapacityException
     * @throws IllegalActionException
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public List<RoomMoveRebookingDTO> setRebookedRoomMoveBookings(int childId, String owner, VABoolean... args)
        throws ExceedCapacityException, IllegalActionException, IllegalArgumentException, IllegalValueException, MaxCountReachedException,
            NoSuchIdentifierException, NoSuchKeyException, NullObjectException, PersistenceException {
        List<RoomMoveRebookingDTO> rtnList = this.validateRoomMoveRebookings(childId, args);
        for (RoomMoveRebookingDTO move : rtnList) {
            if (move.getType().equals(RoomMoveRebookingDTO.Type.ROOM_MOVE_IMPOSSIBLE_CLOSED_ROOM)) {
                // nothing can be booked here - cancel no charge
                BookingBean originalBooking = move.getPreBooking();
                this.setBooking(originalBooking.getYwd(), originalBooking.getRoomId(), originalBooking.getBookingSd(), originalBooking.getProfileId(), originalBooking.getLiableContactId(),
                        originalBooking.getBookingDropOffId(), originalBooking.getBookingPickupId(), BTIdBits.CANCELLED_NOCHARGE,
                        originalBooking.getNotes(), originalBooking.getRequestedYwd(), owner, addToArray(args, RESET_SPANSD, ADHOC_BOOKING, IMMEDIATE_RECALC));
            }
            else {
                BookingBean newBooking = move.getPostBooking();
                this.setBooking(newBooking.getYwd(), newBooking.getRoomId(), newBooking.getBookingSd(), newBooking.getProfileId(), newBooking.getLiableContactId(),
                        newBooking.getBookingDropOffId(), newBooking.getBookingPickupId(), newBooking.getBookingTypeId(),
                        newBooking.getNotes(), newBooking.getRequestedYwd(), owner);
            }
        }
        return (rtnList);
    }

    /**
     * Changes all children's bookings to reflect the room schedule defined in the AgeRoomService for each child, returning
     * a list of changes made
     * @param owner
     * @param args
     * @return
     * @throws ExceedCapacityException
     * @throws IllegalActionException
     * @throws IllegalArgumentException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public List<RoomMoveRebookingDTO> setRebookedRoomMoveBookings(String owner, VABoolean... args)
        throws ExceedCapacityException, IllegalActionException, IllegalArgumentException, IllegalValueException, MaxCountReachedException,
            NoSuchIdentifierException, NoSuchKeyException, NullObjectException, PersistenceException {
        ChildService childService = ChildService.getInstance();
        List<RoomMoveRebookingDTO> rtnList = new LinkedList<>();
        for(ChildBean child : childService.getChildManager().getAllObjects()) {
            rtnList.addAll(setRebookedRoomMoveBookings(child.getChildId(), owner, args));
        }
        return rtnList;
    }

    /**
     * Finds all current bookings which are not in a child's allocated room.
     * Looks at all bookings from today forwards that are of type booking and that have yet to start.
     * Carries out a validation of the bookingSd in the new room and returns a list of all proposed changes
     * @param args: only INCLUDE_EDUCATION is considered
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public List<RoomMoveRebookingDTO> validateRoomMoveRebookings(VABoolean... args)
        throws ExceedCapacityException, IllegalActionException, IllegalArgumentException, IllegalValueException, MaxCountReachedException,
            NoSuchIdentifierException, NoSuchKeyException, NullObjectException, PersistenceException {
        ChildService childService = ChildService.getInstance();
        List<RoomMoveRebookingDTO> rtnList = new LinkedList<>();
        for(ChildBean child : childService.getChildManager().getAllObjects()) {
            rtnList.addAll(validateRoomMoveRebookings(child.getChildId(), args));
        }
        return rtnList;
    }

    /**
     * This ensures all bookings on a ywd have been set to RESTRICTED if their booking is within the
     * notice period based on the requestedYwd provided.
     *
     * @param ywd a YWDHolder key value
     * @param requestedYwd the ywd the booking was requested
     * @param childId the childId the booking is for
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     */
    public void setNoticePeriodRestrictions(int ywd, int requestedYwd, int childId) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, NullObjectException, IllegalActionException, IllegalValueException, MaxCountReachedException {
        if (!ChildService.getInstance().isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        // only look at BookingTypeId that are of type booking
        int btIdMask = BTIdBits.TYPE_BOOKING;
        // exclude the ALWAYS_OPEN_Falg
        int btFlagMask = BTFlagIdBits.TYPE_ALL;
        //get all the bookings for thischild on this day with restrictions
        List<BookingBean> allBookings = this.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId, btIdMask, btFlagMask);
        for (BookingBean b : allBookings) {
            // we don't need to consider NO_CHARGE bookings as they do not have a notice period
            if (BTIdBits.isFilter(b.getBookingTypeId(), BTBits.NO_CHARGE_BIT)) {
                continue;
            }
            // these are restricted with regard to the notice day
            if ((bookingTypeManager.getObject(b.getBookingTypeId()).isFlagOn(BTFlagBits.REGULAR_BOOKING_FLAG)
                    && AgeRoomService.getInstance().isWithinSpecialNoticePeriod(ywd, requestedYwd, b.getRoomId()))
                    || AgeRoomService.getInstance().isWithinStandardNoticePeriod(ywd, requestedYwd, b.getRoomId())) {
                if (b.isState(BookingState.OPEN)) {
                    childBookingManager.setObjectState(ywd, b.getBookingId(), BookingState.RESTRICTED, ObjectBean.SYSTEM_OWNED);
                    // check the bookingType and see if we need to add this to HISTORY
                    if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(b.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                        ChildBookingHistory.getInstance().setBookingHistory(b);
                    }
                }
            }
        }
    }

    /**
     * Helper method to prep non reconciled bookings ready for pre-charge invoicing. The method looks for bookings
     * within the invoice period that are still set to booking state OPEN and sets them to RESTRICTED if they are
     * chargeable. The method returns any pre reconciled bookings that have their PreCharge flag set and have the
     * action flag BOOKING_PRE_BILLED_BIT set to false(indicating they have not yet been pre billed
     *
     * @param lastYwd
     * @param accountId
     * @return a list of BookingBean where the pre charge flag is set.
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     * @throws IllegalValueException
     */
    public List<BookingBean> setInvoiceRestrictions(int lastYwd, int accountId) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, NoSuchKeyException, MaxCountReachedException, IllegalValueException {
        ChildService childService = ChildService.getInstance();

        if (!childService.isId(accountId, VT.ACCOUNT)) {
            throw new NoSuchIdentifierException("The accountId " + accountId + " does not exist");
        }
        List<BookingBean> rtnList = new LinkedList<>();
        // get all the children from this account
        Set<Integer> accountChildIds = childService.getAllChildIdsForAccount(accountId);
        //get all the bookings for this child on this day with restrictions
        for(int ywd : childBookingManager.getAllKeys()) {
            if(ywd > lastYwd) {
                continue;
            }
            // because we don'tknow how many children are in the account don't get by profile
            for(BookingBean booking : childBookingManager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_CHARGE, ActionBits.TYPE_FILTER_OFF)) {
                // we need to check the booking is for the account by looking at the children
                if(accountChildIds.contains(booking.getProfileId())) {
                    // we only care about OPEN bookings when changing the state
                    if(booking.isState(BookingState.OPEN)) {
                        childBookingManager.setObjectState(ywd, booking.getBookingId(), BookingState.RESTRICTED, ObjectBean.SYSTEM_OWNED);
                        // check the bookingType and see if we need to add this to HISTORY
                        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                            ChildBookingHistory.getInstance().setBookingHistory(booking);
                        }
                    }
                    // if the booking state is not Authorisedand it has already been pre-billed
                    if(booking.isBeforeState(BookingState.AUTHORISED)) {
                        if(!this.isBookingTypeFlagOn(ywd, booking.getBookingId(), BTFlagBits.PRECHARGE_FLAG)) {
                            continue;
                        }
                        //if this has already been pre-charged
                        if(booking.isActionOn(ActionBits.BOOKING_PRE_BILLED_BIT)) {
                            // if the booking type is not immediate recalc reject
                            if(!this.isBookingTypeFlagOn(ywd, booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG)
                                    && !booking.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT)) {
                                continue;
                            }
                        }
                    }
                    //If this is booking is before ARCHIVE and it has got this far then add it to be billed
                    if(booking.isBeforeState(BookingState.ARCHIVED)) {
                        rtnList.add(booking);
                    }
                }
            }
        }
        return(rtnList);
    }

    /**
     * Sets an actual drop off id for a booking
     *
     * @param ywd
     * @param bookingId
     * @param actualDropOffId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws com.oathouse.oss.storage.exceptions.MaxCountReachedException
     */
    public void setBookingActualDropOff(int ywd, int bookingId, int actualDropOffId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException, MaxCountReachedException {
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        int childId = booking.getProfileId();
        ChildService.getInstance().checkProfile(-1, childId, -1, actualDropOffId);
        this.validateBookingActuals(ywd, bookingId, owner);
        childBookingManager.setObjectActualDropOffId(ywd, bookingId, actualDropOffId, owner);
        this.setBookingActualState(ywd, bookingId, owner);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * sets the actualPickupId for a booking
     *
     * @param ywd
     * @param bookingId
     * @param actualPickupId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws com.oathouse.oss.storage.exceptions.MaxCountReachedException
     */
    public void setBookingActualPickup(int ywd, int bookingId, int actualPickupId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException, MaxCountReachedException {
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        int childId = booking.getProfileId();
        ChildService.getInstance().checkProfile(-1, childId, -1, actualPickupId);
        this.validateBookingActuals(ywd, bookingId, owner);
        childBookingManager.setObjectActualPickupId(ywd, bookingId, actualPickupId, owner);
        this.setBookingActualState(ywd, bookingId, owner);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * Sets the actual booking start time subject to business rules
     *
     * @param ywd
     * @param bookingId
     * @param startTime
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws PersistenceException
     * @throws com.oathouse.oss.storage.exceptions.MaxCountReachedException
     */
    public void setBookingActualStart(int ywd, int bookingId, int startTime, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException, MaxCountReachedException {
        this.validateBookingActuals(ywd, bookingId, owner);
        childBookingManager.setObjectActualStart(ywd, bookingId, startTime, owner);
        this.setBookingActualState(ywd, bookingId, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * Sets the actual booking end time and the actual pickupId. WARNING the endTimeIn is an true
     * endTime + 1.
     *
     * @param ywd
     * @param bookingId
     * @param endTimeIn the end time + 1
     * @param owner
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws com.oathouse.oss.storage.exceptions.MaxCountReachedException
     */
    public void setBookingActualEnd(int ywd, int bookingId, int endTimeIn, String owner) throws PersistenceException, IllegalActionException, NullObjectException, NoSuchIdentifierException, MaxCountReachedException {
        // TIME ADJUSTMENT: remove one from the endTime
        int endTime = endTimeIn - 1;
        this.validateBookingActuals(ywd, bookingId, owner);
        childBookingManager.setObjectActualEnd(ywd, bookingId, endTime, owner);
        this.setBookingActualState(ywd, bookingId, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * Corrects changes to Booking State when Drop Off and Pickup Mandatory presets change.
     *
     * @param actualDropOffMandatory
     * @param actualPickupMandatory
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setBookingStateForMandatoryChange(boolean actualDropOffMandatory, boolean actualPickupMandatory) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException {
        for (int ywd : childBookingManager.getAllKeys()) {
            for (BookingBean booking : childBookingManager.getAllObjects(ywd)) {
                if (actualDropOffMandatory && booking.getActualDropOffId() == -1 && booking.isState(BookingState.COMPLETED)) {
                    this.resetBookingToStarted(ywd, booking.getBookingId(), ObjectBean.SYSTEM_OWNED);
                }
                else if (actualPickupMandatory && booking.getActualPickupId() == -1 && booking.isState(BookingState.COMPLETED)) {
                    this.resetBookingToStarted(ywd, booking.getBookingId(), ObjectBean.SYSTEM_OWNED);
                }
                else if (!actualDropOffMandatory && !actualPickupMandatory
                        && booking.getActualStart() > -1 && booking.getActualEnd() > -1
                        && booking.isState(BookingState.STARTED)) {
                    childBookingManager.setObjectState(ywd, booking.getBookingId(), BookingState.COMPLETED, ObjectBean.SYSTEM_OWNED);
                    // check the bookingType and see if we need to add this to HISTORY
                    if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                        ChildBookingHistory.getInstance().setBookingHistory(booking);
                    }
                }
            }
        }
    }

    /**
     * Removes actual start and end times from a booking, and sets the booking state to restricted (
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws PersistenceException
     */
    public void resetBookingActuals(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException, MaxCountReachedException {
        childBookingManager.resetObjectActualStartEnd(ywd, bookingId, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * reset method allowing a booking to be changed from COMPLETED back to STARTED. This method is
     * used when a change to the completed rules changes.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void resetBookingToStarted(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException, MaxCountReachedException {
        childBookingManager.resetObjectStateToStarted(ywd, bookingId, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * reset method allowing a booking to be changed from RECONCILED or ARCHIVED back to AUTHORISED.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void resetBookingToAuthorised(int ywd, int bookingId, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException  {
        childBookingManager.resetObjectStateToAuthorised(ywd, bookingId, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * Sets the booking to AUTHORISED, indicating that it is ready for final invoice action.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void setBookingAuthorised(int ywd, int bookingId, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException  {
        childBookingManager.setObjectState(ywd, bookingId, BookingState.AUTHORISED, owner);
        BookingBean booking = this.getBookingManager().getObject(bookingId);
        // check the bookingType and see if we need to add this to HISTORY
        if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
            ChildBookingHistory.getInstance().setBookingHistory(booking);
        }
    }

    /**
     * Sets a a booking to be reconciled if is authorised. Not only is the booking state set but BOOKING_PRE_BILLED_BIT is
     * automatically set and the IMMEDIATE_RECALC_BIT set to false.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setBookingReconciled(int ywd, int bookingId, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException  {
        // set booking action flag to indicate this booking has been processed (We don't care if this is an estimate or reconciled)
        childBookingManager.setObjectActionBits(ywd, bookingId, ActionBits.BOOKING_PRE_BILLED_BIT, true, owner);
        // set booking action flag immediate recalculation to off (irrelevant if this was off already)
        childBookingManager.setObjectActionBits(ywd, bookingId, ActionBits.IMMEDIATE_RECALC_BIT, false, owner);
        // finally set the booking to RECONCILED if it is AUTHORISED
        if(childBookingManager.getObject(ywd, bookingId).isState(BookingState.AUTHORISED)){
            childBookingManager.setObjectState(ywd, bookingId, BookingState.RECONCILED, owner);
            BookingBean booking = this.getBookingManager().getObject(bookingId);
            // check the bookingType and see if we need to add this to HISTORY
            if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.HISTORY_FLAG)) {
                ChildBookingHistory.getInstance().setBookingHistory(booking);
            }
        }
    }

    /**
     * Sets a booking actionBits. This replaces the bits that were there before
     *
     * @param ywd
     * @param bookingId
     * @param actionBits
     * @param isOn
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setBookingActionBits(int ywd, int bookingId, int actionBits, boolean isOn, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        childBookingManager.setObjectActionBits(ywd, bookingId, actionBits, isOn, owner);
    }

    /**
     * Allows the dynamic changing of a BookingTypeBean flagBits value. This values will be saved to
     * file but can be set back to default with setObjectToDefault()
     *
     * @param bookingTypeId
     * @param flagBits the flag bits to change
     * @param isOn if the flag bits should be turned on if true or off if false
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setBookingTypeFlag(int bookingTypeId, int flagBits, boolean isOn, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        bookingTypeManager.setObjectFlagBits(bookingTypeId, flagBits, isOn, owner);
    }

    /**
     * Allows the dynamic changing of a BookingTypeBean chargePercent value. This values will be
     * saved to file but can be set back to default with setObjectToDefault()
     *
     * @param bookingTypeId
     * @param chargePercent the percentage to charge
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setBookingTypeChargePercent(int bookingTypeId, int chargePercent, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        bookingTypeManager.setObjectChargePercent(bookingTypeId, chargePercent, owner);
    }

    /**
     * Allows a BookingTypeBean objects to be set back to their default values. The BookingTypeBean
     * objects to reset should be listed
     *
     * @param bookingTypeId
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setBookingTypeToDefault(int bookingTypeId) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        bookingTypeManager.setObjectToDefault(bookingTypeId);
    }

    /**
     * Removes all bookings form the booking manager that relate to an account.
     *
     * @param childId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<BookingBean> removeAllBookingsForChild(int childId) throws PersistenceException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();

        for (int ywd : childBookingManager.getAllKeys()) {
            for(BookingBean booking : childBookingManager.getAllObjects(ywd)) {
                if(booking.getProfileId() == childId) {
                    rtnList.add(childBookingManager.removeObject(ywd, booking.getBookingId()));
                }
            }
        }

        return rtnList;

    }



    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Private Class Methods">
    /*
     * ***************************************************
     * P R I V A T E C L A S S M E T H O D S
     *
     * This section is for all private methods for the class. Private methods should be carefully
     * used so as to avoid multi jump calls and as a general rule of thumb, only when multiple
     * methods use a common algorithm. **************************************************
     */

    /*
     * test method to validate an actual being set
     */
    private void validateBookingActuals(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        // actuals can only be set now or in the past
        if (ywd > this.getTodayYwd(0)) {
            throw new IllegalActionException("CannotSetActualsInAdvance");
        }
        // get the booking
        BookingBean booking = childBookingManager.getObject(ywd, bookingId);
        // if the bookingTypeId is not ATTENDING then throw illegal action exception
        if (BTBits.isNotBits(booking.getBookingTypeId(), BTBits.ATTENDING_BIT)) {
            throw new IllegalActionException("MustBeAttendingToSetActuals");
        }
        if (BookingState.isStateType(booking.getState(), BookingState.TYPE_BILLING)) {
            throw new IllegalActionException("BookingHasEnded");
        }
    }

    /*
     * sets the state of a booking dependant on which of the actuals have been set
     */
    private void setBookingActualState(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        // get the booking
        BookingBean booking = childBookingManager.getObject(ywd, bookingId);
        // check the values in the actuals the check if we are STARTED or COMPLETED
        SystemPropertiesBean systemProperties = PropertiesService.getInstance().getSystemProperties();
        if (booking.getActualStart() < 0 || booking.getActualEnd() < 0) {
            childBookingManager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        }
        else if (systemProperties.isActualPickupIdMandatory() && booking.getActualPickupId() < 0) {
            childBookingManager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        }
        else if (systemProperties.isActualDropOffIdMandatory() && booking.getActualDropOffId() < 0) {
            childBookingManager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        }
        else { // if we get here, all relevant actuals must be set
            childBookingManager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        }
    }

    /*
     * multi used set of methods to test the parameters when creating or setting a booking
     */
    protected void validateSetParameters(int ywd, int roomId, int childId, int liableContactId, int bookingPickupId,
                                         int bookingTypeId) throws PersistenceException, NoSuchIdentifierException, NullObjectException, NoSuchKeyException, IllegalActionException {
        // Business rule - The room must exist
        AgeRoomService.getInstance().isId(roomId, VT.ROOM_CONFIG);

        // Integrity rule - the child and contacts must exist and the relation be correct
        ChildService.getInstance().checkProfile(-1, childId, liableContactId, bookingPickupId);

        // Integrity rule - BookingType must exist
        if (!BTIdBits.isFilter(bookingTypeId, BTIdBits.TYPE_IDENTIFIER)) {
            throw new NoSuchIdentifierException("The bookingTypeId " + bookingTypeId + " is not valid");
        }

        // Business rule - The account must be Active
        if (ChildService.getInstance().isAccountSuspended(childId)) {
            throw new IllegalActionException("AccountSuspended");
        }

        // Integrity rule - the ywd must be within the period defined by System Properties
        // - actualsPeriodWeek -> confirmedPeriodWeeks if this is a "real" booking
        PropertiesService ps = PropertiesService.getInstance();
        if (ywd >= CalendarStatic.getRelativeYW(ps.getSystemProperties().getConfirmedPeriodWeeks())) {
            throw new IllegalActionException("BookingYwdAfterConfirmedPeriod");
        }
        if (ywd < CalendarStatic.getRelativeYW(-ps.getSystemProperties().getActualsPeriodWeeks())) {
            throw new IllegalActionException("BookingYwdBeforeActualsPeriod");
        }

        // Integrity rule - child must have a room assignment to determine start date
        if (AgeRoomService.getInstance().getChildStartYwd(childId) == -1) {
            throw new IllegalActionException("ChildHasNoRoomsAssigned");
        }
        // Integrity rule - ywd must be on or after the child startYwd
        if (ywd < AgeRoomService.getInstance().getChildStartYwd(childId)) {
            throw new IllegalActionException("BookingYwdBeforeChildStartYwd");
        }
        // Integrity rule - ywd must be on or before the child departYwd (if there is one)
        int departYwd = ChildService.getInstance().getChildManager().getObject(childId).getDepartYwd();
        if (departYwd > -1 && ywd > departYwd) {
            throw new IllegalActionException("BookingYwdAfterChildDepartYwd");
        }
    }

    /*
     * reduced name for commonly used get todays date as a ywd
     */
    private int getTodayYwd(int plusDays) throws PersistenceException {
        return CalendarStatic.getRelativeYWD(plusDays);
    }
    //</editor-fold>
}
