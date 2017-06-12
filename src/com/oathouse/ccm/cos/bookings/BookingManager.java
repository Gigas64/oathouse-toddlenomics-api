/*
 * @(#)BookingManager.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NoSuchKeyException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDBits;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The {@code BookingManager} Class extends the methods of the parent class. It provides the functionality needed to
 * manage bookings for a particular year, week and day in a particular room. The Object key is a YWDHolder key value
 * with the roomId held within the booking bean allowing the extraction of children in rooms
 *
 * <p> Bookings<br>  Key - YWDHolder key value <br> Id - BookingId </p>
 * <p> BookingHistory<br>  Key - bookingId <br> Id - BookingHistoryId </p>
 *
 * @author Darryl Oatridge
 * @version 1.05 19-Oct-2011
 */
public class BookingManager extends ObjectMapStore<BookingBean> {

    // used for code readability and code consistency
    private static final int ALL_PROFILE = -1;
    private static final int ALL_ROOM = -1;
    // static for an SDHolder covering a full day
    public static final int ALL_PERIODS = 1441;
    // the maximum number of minutes a period can be
    public static final int MAX_PERIOD_LENGTH = 1439;
    // the maximum number of minutes in a day
    public static final int MAX_DAY_LENGTH = 1440;

    /**
     * Constructs a {@code BookingManager}, passing the manager name which is used to distinguish the persistently held
     * data from other managers stored. Normally the manager name would be the name of the class. Additionally the
     * data options can be specified to allow different combinations of persistence over and above the default.
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     * @see ObjectEnum
     */
    public BookingManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Constructs a {@code BookingManager} without persistence.
     */
    public BookingManager() {
        super();
    }

    // <editor-fold defaultstate="expanded" desc="get() special request methods">

    /*
     * ***************************************************************
     * S P E C I A L   R E Q U E S T   G E T   M E T H O D S
     * **************************************************************
     */
    /**
     * This is a special request method for retrieving the number of bookings for a particular bookingTypeId at a
     * specific time of a ywd a roomId defined by a zero duration periodSd values (SDHolder). The periodSd value should
     * be an SDHolder key value of zero duration, however the method 'cleans' any 'dirty' durations.
     *
     * @param ywd a YWDHolder key value
     * @param roomId a room identifier
     * @param periodSd zero duration SDHolder key value
     * @param bookingTypeIdMask a bit mask represented by BTBits constants
     * @return booking count for a ywd, roomId and time
     * @throws PersistenceException
     * @see BTBits
     */
    public int getBookingCount(int ywd, int roomId, int periodSd, int bookingTypeIdMask) throws PersistenceException {
        // Make duration 0 if it isn't already
        int periodSd0 = SDHolder.getSD0(periodSd);
        if(SDHolder.getStart(periodSd) > MAX_DAY_LENGTH || SDHolder.getStart(periodSd) < 0) {
            throw new PersistenceException("the time value '" + SDHolder.getStart(periodSd) + "' is not valid. Value must be between 0 and 1440");
        }
        // return the size of Set
        return (getYwdBookingsForRoom(ywd, periodSd0, roomId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF).size());
    }

    /**
     * Finds an periodSd representing the whole nursery's span of booking times for a ywd where
     * the booking type is Attending. This provides the nursery first-in-last-out values represented
     * as a SDHolder value.
     *
     * @param ywd the YWDHolder value of the day in question
     * @return SDHolder value of the start and duration of the first-in-last-out
     * @throws PersistenceException
     */
    public int getAllBookingSd(int ywd) throws PersistenceException {
        List<BookingBean> bookings = getYwdBookings(ywd, ALL_PERIODS, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF);
        if(!bookings.isEmpty()) {
            int start = 1440;
            int end = 0;
            for(BookingBean booking : bookings) {
                if(booking.getBookingStart() < start) {
                    start = booking.getBookingStart();
                }
                if(booking.getBookingEnd() > end) {
                    end = booking.getBookingEnd();
                }
            }
            if(start < end) {
                return (SDHolder.buildSD(start, end));
            }
        }
        return (-1);
    }

    /**
     * Returns all bookings that are of BookingType ATTENDING, in the past, and still have a booking
     * state of either OPEN, RESTRICTED or STARTED.
     *
     * @return List of active bookings from the past
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<BookingBean> getIncompleteBookings() throws NoSuchIdentifierException, PersistenceException {
        List<BookingBean> rtnList = new LinkedList<>();

        int today = CalendarStatic.getRelativeYWD(0);
        for(int ywd : getAllKeys()) {
            // only days in the past
            if(ywd >= today) {
                continue;
            }
            for (BookingBean booking : getAllFilteredObjectsForYwd(ywd, ALL_PERIODS, ALL_ROOM, ALL_PROFILE, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF, true)) {
                if (BookingState.isBefore(booking.getState(), BookingState.COMPLETED)) {
                    rtnList.add(booking);
                }
            }
        }
        return (rtnList);
    }

    /**
     * Returns all physical bookings that are in the past that have a state of COMPLETED. <p>
     * Additionally any bookings with a BookingTypeId with bit set: <br> HOLIDAY_BIT<br>
     * LEAVE_BIT<br> SICK_BIT<br> ABSENT_BIT<br> SUSPENDED_BIT<br> CANCELLED_BIT<br> will be
     * automatically set to COMPLETED if their state is OPEN or RESTRICTED.</p>
     *
     * <p> This method is a transition method, 'cleaning' the booking process ready for the
     * charging. Any transitional logic should be included in this method. This method must be
     * called and should be the only mechanism to transfer state from COMPLETED to AUTHORISED </p>
     *
     * @return all bookings that are in the past that have a state of COMPLETED.
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public List<BookingBean> getUncheckedBookings() throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        List<BookingBean> rtnList = new LinkedList<>();

        int today = CalendarStatic.getRelativeYWD(0);
        for(int ywd : getAllKeys()) {
            // only days in the past
            if(ywd >= today) {
                continue;
            }
            for(BookingBean booking : getAllFilteredObjectsForYwd(ywd, ALL_PERIODS, ALL_ROOM, ALL_PROFILE, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true)) {
                // remove any that are WAITING
                if (BTIdBits.isBits(booking.getBookingTypeId(), BTBits.WAITING_BIT)) {
                    removeObject(booking.getYwd(), booking.getBookingId());
                    continue;
                }
                // if the booking has a state of AUTHORISED, RECONCILED or ARCHIVED then it is already processed
                if (BookingState.isStateType(booking.getState(), BookingState.TYPE_BILLING)) {
                    continue;
                }
                // if the booking is of booking type ATTENDING and not COMPLETE then we don't want it
                if (BTBits.isBits(booking.getBookingTypeId(), BTBits.ATTENDING_BIT) && !booking.isState(BookingState.COMPLETED)) {
                    continue;
                }
                // Change the state if the non attending bookings are not COMPLETED
                int BookingTypeSelect = BTBits.turnOff(BTIdBits.TYPE_BOOKING, BTBits.ATTENDING_BIT);
                if (BTBits.isBits(BookingTypeSelect, booking.getBookingTypeId())) {
                    setBookingComplete(booking.getYwd(), booking.getBookingId(), booking.getOwner());
                }
                // add them to the return list
                rtnList.add(booking);
            }
        }
        return (rtnList);
    }

    /**
     * This is a special request method call for retrieving BookingBean Objects that are on or after a given ywd that
     * belong to a specific childId and fulfil the bookingTypeId mask. This method is based on getYwdBookingForProfile()
     * for each ywd on or after the given ywd
     *
     * @param fromYwd the ywd from which you want all bookings
     * @param childId the child id
     * @param bookingTypeIdMask the filter mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a List of BookingBean objects
     * @throws PersistenceException
     */
    public List<BookingBean> getBookingsFromYwdForProfile(int fromYwd, int childId, int bookingTypeIdMask,
            int actionInclude) throws PersistenceException {
        List<BookingBean> rtnList = new LinkedList<>();
        for(int ywd : getAllKeys()) {
            if(ywd < fromYwd) {
                continue;
            }
            rtnList.addAll(getYwdBookingsForProfile(ywd, ALL_PERIODS, childId, bookingTypeIdMask, actionInclude));
        }
        return (rtnList);
    }

    /**
     * This is a special request method call for retrieving occupancy variants across a ywd and room for a number of
     * times during a day defined by a set of zero duration periodSd values (SDHolder). The periodSd list should be
     * periods of zero duration, however the method 'cleans' 'dirty' durations. <p> The occupancy variant result is a
     * map of SDHolder key values mapped to Occupancy values. The map key is a subset of the original periodSdList,
     * defined by a change is the occupancy value. As an example imagine we have an occupancy distribution for the
     * passed periodSd values of <blockquote>
     * <pre>
     * 10  15  20  25  30  35  40  45  50  55  60  65  70  75  80  85
     * 0 - 0 - 1 - 1 - 2 - 3 - 3 - 3 - 2 - 1 - 4 - 5 - 1 - 2 - 0 - 0
     * </pre> </blockquote> the resulting Map would look like: <blockquote>
     * <pre>
     * 20  30  35  50  55  60  65  70  75  80
     * 1 - 2 - 3 - 2 - 1 - 4 - 5 - 1 - 2 - 0
     * </pre> </blockquote> Notice we don't start till we get a positive occupancy but at the end we show the zero
     * value. This validates the final occupancy of the day with zero being as legitimate as a positive value. 
     * <p>
     * The return map is: <br> Key - a list of zero duration SDHolder key values where there is a variant<br> Id -
     * the variant value associated with the change </p>
     *
     * @param ywd a YWDHolder key value
     * @param roomId a room identifier
     * @param periodSdSet zero duration SDHolder key value set
     * @param bookingTypeIdMask a bit mask represented by BTBits constants
     * @return SDHolder key value mapped to occupancy variant
     *
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @see BTBits
     */
    public Map<Integer, Integer> getYwdRoomOccupancyVariant(int ywd, int roomId, Set<Integer> periodSdSet,
            int bookingTypeIdMask) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {

        int bookingCount;
        ConcurrentSkipListMap<Integer, Integer> rtnMap = new ConcurrentSkipListMap<>();
        if(periodSdSet == null || periodSdSet.isEmpty()) {
            return (rtnMap);
        }

        for(int periodSd : periodSdSet) {
            int periodSd0 = SDHolder.getSD0(periodSd);
            bookingCount = this.getYwdBookingsForRoom(ywd, periodSd0, roomId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF).size();
            if(rtnMap.isEmpty()) {
                if(bookingCount > 0) {
                    //this is our first booking
                    rtnMap.put(periodSd0, bookingCount);
                }
                // get the next bookingCount
                continue;
            }
            if(bookingCount != rtnMap.lastEntry().getValue()) {
                rtnMap.put(periodSd0, bookingCount);
            }
        }
        return (rtnMap);
    }

    /**
     * This is a special request method call for retrieving the room occupancy trend single value. The returned value
     * represents the average room occupancy over a set periodSd values as a percentage of that room occupancy capacity.
     * If the periodSd set is empty or null or the room capacity is less then one then a trend of zero is returned.
     *
     * @param ywd a YWDHolder key value
     * @param roomId a room identifier
     * @param roomCapacity the occupancy capacity of the roomId
     * @param periodSdSet zero duration SDHolder key value set
     * @param bookingTypeIdMask a bit mask represented by BTBits constants
     * @return the occupancy trend as a percentage of he capacity
     *
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @see BTBits
     */
    public int getYwdRoomOccupancyTrend(int ywd, int roomId, int roomCapacity, Set<Integer> periodSdSet,
            int bookingTypeIdMask) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        double counter = 0;
        double periods = 0;
        double avgOccupancy;
        int trend;

        if(periodSdSet == null || periodSdSet.isEmpty() || roomCapacity < 1) {
            return (0);
        }
        // get count for each periodSd in the timetable
        for(int periodSd : periodSdSet) {
            // add each period count
            counter += this.getYwdBookingsForRoom(ywd, SDHolder.getSD0(periodSd), roomId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF).size();
            // could get this from getAllPeriodSd but this is safer
            periods++;
        }
        // divide the count by the number of periods
        avgOccupancy = counter / periods;
        //now get the trend based on capacity (the 0.5 rounds the double)
        trend = (int) (((avgOccupancy / roomCapacity) * 100) + 0.5);

        return (trend);
    }

    // </editor-fold>

    //<editor-fold defaultstate="expanded" desc="get() additional methods">
    /*
     * ***************************************************************
     * H E L P E R G E T M E T H O D S
     * **************************************************************
     */
    /**
     * Validation Method: looks through ALL bookings to find if a profileId exists. If the profleId is found within a booking
     * it returns true, otherwise returns false
     *
     * @param profileId
     * @return true if found, false if not
     * @throws PersistenceException
     */
    public boolean hasBooking(int profileId) throws PersistenceException {
        for(int key : getAllKeys()) {
            for(BookingBean booking : getAllObjects(key)) {
                if(booking.getProfileId() == profileId) {
                    return (true);
                }
            }
        }
        return (false);
    }

    /**
     * this method looks through ALL bookings to find if a liableContactId exists. If the liableContactId is found
     * within a booking it returns true, otherwise returns false
     *
     * @param liableContactId
     * @return true if found, false if not
     * @throws PersistenceException
     */
    public boolean hasLiability(int liableContactId) throws PersistenceException {
        for(int key : getAllKeys()) {
            for(BookingBean booking : getAllObjects(key)) {
                if(booking.getLiableContactId() == liableContactId) {
                    return (true);
                }
            }
        }
        return (false);
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="getAll() methods">
    /*
     * ***************************************************************
     * g e t A l l   m e t h o d s
     * **************************************************************
     */
    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}) and periodSd ({@code SDHolder}).
     * The List is filtered by bookingTypeId whereby a bookingTypeIdMask is passed and includes all booking types that
     * are included in the mask (see {@code BTBits}.
     *
     * <p>If the periodSd ({@code SDHolder} key value) is set to -1 then the whole day is taken </p>
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value, -1 for whole day
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @see BTBits
     */
    public List<BookingBean> getYwdBookings(int ywd, int periodSd, int bookingTypeIdMask, int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYwd(ywd, periodSd, ALL_ROOM, ALL_PROFILE, bookingTypeIdMask, actionInclude, false));
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a week ({@code YWDHolder}). The List is filtered by
     * bookingTypeId whereby a bookingTypeIdMask is passed and includes all booking types that are included in the mask
     * (see {@code BTBits}.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param yw0 a YWDHolder key value
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @see BTBits
     */
    public List<BookingBean> getYwBookings(int yw0, int bookingTypeIdMask, int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYw(yw0, ALL_ROOM, ALL_PROFILE, bookingTypeIdMask, actionInclude));
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}) and periodSd ({@code SDHolder})
     * for the specified profile. The List is filtered by bookingTypeId whereby a bookingTypeIdMask is passed and
     * includes all booking types that are included in the mask (see {@code BTBits}.
     *
     * <p>If the periodSd ({@code SDHolder} key value) is set to -1 then the whole day is taken </p>
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value, -1 for whole day
     * @param profileId the profileId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @see BTBits
     */
    public List<BookingBean> getYwdBookingsForProfile(int ywd, int periodSd, int profileId, int bookingTypeIdMask,
            int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYwd(ywd, periodSd, ALL_ROOM, profileId, bookingTypeIdMask, actionInclude, false));
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for week ({@code YWDHolder}) for the specified profile. The
     * List is filtered by bookingTypeId whereby a bookingTypeIdMask is passed and includes all booking types that are
     * included in the mask (see {@code BTBits}.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param yw0 a YWDHolder key value for year week with day ignored
     * @param profileId the profileId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     * @see BTBits
     */
    public List<BookingBean> getYwBookingsForProfile(int yw0, int profileId, int bookingTypeIdMask, int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYw(yw0, ALL_ROOM, profileId, bookingTypeIdMask, actionInclude));
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a single day ({@code YWDHolder}), periodSd ({@code SDHolder})
     * and roomId. The List is filtered by bookingTypeId whereby a bookingTypeIdMask is passed and includes all booking
     * types that are included in the mask (see {@code BTBits}.
     *
     * <p>If the time range ({@code SDHolder} key value) is set to -1 then the whole day is taken </p>
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param ywd a YWDHolder key value
     * @param periodSd a SDHolder key value
     * @param roomId the roomId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     */
    public List<BookingBean> getYwdBookingsForRoom(int ywd, int periodSd, int roomId, int bookingTypeIdMask,
            int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYwd(ywd, periodSd, roomId, ALL_PROFILE, bookingTypeIdMask, actionInclude, false));
    }

    /**
     * Returns a List of all {@code BookingBean} Objects for a week ({@code YWDHolder}) for the specified room. The List
     * is filtered by bookingTypeId whereby a bookingTypeIdMask is passed and includes all booking types that are
     * included in the mask (see {@code BTBits}.
     *
     * <p>bookingTypeIdMask must be a static taken from BTBits or the behaviour is unpredictable</p>
     *
     * @param yw0 a YWDHolder key value for year week with day ignored
     * @param roomId the roomId to filter
     * @param bookingTypeIdMask a booking type id mask
     * @param actionInclude ActionBits to include in addition to the bookingTypeIdMask
     * @return a list of {@code BookingBean} objects
     * @throws PersistenceException
     */
    public List<BookingBean> getYwBookingsForRoom(int yw0, int roomId, int bookingTypeIdMask, int actionInclude) throws PersistenceException {
        return (getAllFilteredObjectsForYw(yw0, roomId, ALL_PROFILE, bookingTypeIdMask, actionInclude));
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="set() new booking methods">

    /*
     * *****************************************************************
     * S E T N E W B O O K I N G M E T H O D S
     * ****************************************************************
     */
    /**
     * Override of setObject. Must use setConsolidateBooking(), setCreateBooking(), setReplaceBooking(), setCutBooking()
     * or setFitBooking() methods to to set a booking.
     *
     * @param ywd a YWDHolder key value
     * @param booking
     *
     * @return no return
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public BookingBean setObject(int ywd, BookingBean booking) throws PersistenceException, NullObjectException {
        throw new PersistenceException("You are not allowed to directly set a booking");
    }

    /**
     * setCreateBooking() allows a new booking to be created with all actuals attributes set to their initialisation
     * values, spanSD set to bookingSD and BookingState set to OPEN. The new BookingBean is returned. The methods
     * setCreateBooking(), setReplaceBooking(), setCutBooking(), setFitBooking() and setLayerBooking() replace
     * setObject(), which now throws an exception.
     *
     * <p> The rules and restrictions on this method are: <br> - You can not overwrite an existing booking where the
     * periodSd overlaps a bookingSd.<br> - If a booking already exists with a spanSD greater than the bookingSd and
     * the spanSd overlaps the periodSd then the booking can be created with the spanSd of the underlying cut to the
     * periodSd but only under the conditions that the bookingTypeIds are identical <br> - The new booking will
     * have a newly generated unique bookingId </p>
     *
     * @param ywd
     * @param periodSd
     * @param profileId
     * @param roomId
     * @param liableContactId
     * @param bookingDropOffId
     * @param bookingPickupId
     * @param bookingTypeId
     * @param actionBits
     * @param requestedYwd
     * @param notes
     * @param owner
     * @return the newly created BookingBean
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public BookingBean setCreateBooking(int ywd, int periodSd, int profileId, int roomId, int liableContactId,
            int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, int requestedYwd, String notes,
            String owner) throws PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException, NoSuchIdentifierException {
        if(!BTIdBits.isValid(bookingTypeId, BTIdBits.TYPE_BOOKING)) {
            throw new IllegalArgumentException("The BookingTypeId is not a valid booking type");
        }
        final int bookingId = this.generateIdentifier();
        // we need to check if there is a bookingSd (not spanSd) that exists at this time.
        if(!getAllFilteredObjectsForYwd(ywd, periodSd, -1, profileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, false).isEmpty()) {
            throw new PersistenceException("A booking already exists with a bookingSd within the range of the periodSd");
        }
        // check if we overlap a spanSd...
        List<BookingBean> chkList = getAllFilteredObjectsForYwd(ywd, periodSd, -1, profileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true);
        // if it is empty then create a new booking.
        if(chkList.isEmpty()) {
            BookingBean booking = BookingBean.create(bookingId, ywd, roomId, profileId, periodSd, liableContactId,
                    bookingDropOffId, bookingPickupId, bookingTypeId, actionBits, requestedYwd, notes, owner);
            return super.setObject(ywd, booking);
        }
        for(BookingBean cBooking : chkList) {
            // check the bookingTypeId
            if(cBooking.getBookingTypeId() != bookingTypeId) {
                throw new IllegalActionException("A booking already exists with a spanSD within the range of the periodSd and different bookingTypeId");
            }
        }
        // cut the booking into the existing spanSd(s)
        List<BookingBean> cutList = this.setCutBooking(ywd, periodSd, profileId, roomId, liableContactId, bookingDropOffId, bookingPickupId, bookingTypeId, bookingTypeId, requestedYwd, actionBits, notes, owner);
        //the first in the list is always the new booking
        return (cutList.get(0));
    }

    /**
     * setReplaceBooking() allows a new booking to replace an existing booking or bookings where the profileId is the
     * same as that those to be replaced within the periodSd. The methods setCreateBooking(), setReplaceBooking(),
     * setCutBooking(), setFitBooking() and setLayerBooking() replace setObject(), which now throws an exception.
     *
     * <p> The rules and restrictions on this method are: <br> - If their is no booking with the same ywd, profileId
     * and spanSd in the periodSd then an exception is thrown. You should use setCreateBooking() method in this
     * instance.<br> - The new booking retains the same bookingId as the first replaced booking.<br> - The
     * replaceable booking(s) must be OPEN or RESTRICTED to be able to be replaced.<br> - If the replaceable
     * booking(s) are OPEN the spanSd and bookingSd equals the periodSd<br> - If the replaceable booking(s) are
     * RESTRICTED the spanSd is the greatest start/end </p>
     *
     * @param forYwd
     * @param forPeriodSd
     * @param forProfileId
     * @param roomId
     * @param liableContactId
     * @param bookingDropOffId
     * @param bookingPickupId
     * @param bookingTypeId
     * @param actionBits
     * @param requestedYwd
     * @param notes
     * @param owner
     * @return the changed booking
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public BookingBean setReplaceBooking(int forYwd, int forPeriodSd, int forProfileId, int roomId, int liableContactId,
            int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, int requestedYwd, String notes,
            String owner)
                throws PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException,
                        IllegalArgumentException, NoSuchIdentifierException {

        if(!BTIdBits.isValid(bookingTypeId, BTIdBits.TYPE_BOOKING)) {
            throw new IllegalArgumentException("The BookingTypeId is not a valid booking type");
        }
        List<BookingBean> chkList = getAllFilteredObjectsForYwd(forYwd, forPeriodSd, -1, forProfileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true);
        // if there are no bookings then it can't be replaced and data transfered
        if(chkList.isEmpty()) {
            throw new PersistenceException("There are no bookings for this profile to be replaced");
        }
        // there might be more than one booking to replace so check all are still ok to edit
        for(BookingBean cBooking : chkList) {
            if(cBooking.isEqualOrAfterState(BookingState.RECONCILED)) {
                throw new IllegalActionException("BookingStateIsReconciledOrLater");
            }
            // if the booking is Authorised then change back to completed
            if(cBooking.isState(BookingState.AUTHORISED)) {
                this.resetObjectStateToCompleted(cBooking.getYwd(), cBooking.getBookingId(), owner);
            }
        }
        // run through the bookings to create the spanSd and remove bookings
        int spanSd = forPeriodSd;
        // get the first booking
        BookingBean booking = chkList.get(0);
        boolean wasAttending = BTIdBits.contain(booking.getBookingTypeId(), BTBits.ATTENDING_BIT);
        boolean isNowAttending = BTIdBits.contain(bookingTypeId, BTBits.ATTENDING_BIT);

        for(BookingBean cBooking : chkList) {
            if(!BTIdBits.contain(cBooking.getBookingTypeId(), BTBits.NO_CHARGE_BIT) && cBooking.isStateType(BookingState.TYPE_NOTICE)) {
                // get the greats span accross new booking beyond the periodSd
                spanSd = SDHolder.spanSD(cBooking.getSpanSd(), spanSd);
            }
            // remove all the objects
            this.removeObject(forYwd, cBooking.getBookingId());
        }

        // we need to be careful if this has already been billed as a pre booking
        if(booking.isActionOn(ActionBits.BOOKING_PRE_BILLED_BIT)) {
            actionBits = ActionBits.turnOn(actionBits, ActionBits.BOOKING_PRE_BILLED_BIT);
        }
        // set the new attributes
        booking.setBookingSd(forPeriodSd, owner);
        booking.setSpanSd(spanSd, owner);
        booking.setRoomId(roomId, owner);
        booking.setBookingTypeId(bookingTypeId, owner);
        booking.setLiableContactId(liableContactId, owner);
        booking.setBookingDropOffId(bookingDropOffId, owner);
        booking.setBookingPickupId(bookingPickupId, owner);
        booking.setActionBits(actionBits, owner);
        booking.setRequestedYwd(requestedYwd, owner);
        booking.setNotes(notes, owner);
        cleanStateAndActuals(wasAttending, isNowAttending, booking, owner);
        return (super.setObject(forYwd, booking));
    }

    /*
     * utility method
     */
    protected void cleanStateAndActuals(boolean wasAttending, boolean isNowAttending, BookingBean booking, String owner)
            throws IllegalActionException, PersistenceException, NullObjectException, NoSuchIdentifierException {

        // reset actuals which were automatically entered so that the real values have to be entered, and return to restricted
        if(!wasAttending && isNowAttending) {
            booking.setActualStart(ObjectEnum.INITIALISATION.value(), owner);
            booking.setActualEnd(ObjectEnum.INITIALISATION.value(), owner);
            booking.setActualDropOffId(ObjectEnum.INITIALISATION.value(), owner);
            booking.setActualPickupId(ObjectEnum.INITIALISATION.value(), owner);
            booking.setState(BookingState.RESTRICTED, owner);
        }
        else if(!wasAttending && !isNowAttending) {
            // nothing to do
        }
        else if(wasAttending && isNowAttending) {
            // retain actuals already set
        }
        // wasAttending && !isNowAttending
        else {
            // clean up custodians
            booking.setActualDropOffId(ObjectEnum.INITIALISATION.value(), owner);
            booking.setActualPickupId(ObjectEnum.INITIALISATION.value(), owner);
            // move to complete, setting actuals
            booking.setActualStart(booking.getBookingStart(), owner);
            booking.setActualEnd(booking.getBookingEnd(), owner);
            booking.setState(BookingState.COMPLETED, owner);
        }
    }

    /**
     * setCutBooking() allows a new booking to replace an existing booking but unlike setReplaceBooking, the existing
     * booking is cut to make way for the new booking with it/their bookingSd and spanSd cut to make way for the new
     * booking and it/their bookingTypeId set to cutBookingTypeId The methods setCreateBooking(), setReplaceBooking(),
     * setCutBooking(), setFitBooking() and setLayerBooking() replace setObject(), which now throws an exception.
     *
     * <p> setCutBooking() considers the spanSd and not the bookingSd. Therfore if a periodSd is outside a bookingSd but
     * within a spanSd the original bookingSd will be preserved and the spanSd cut to make way for the new booking. Also
     * if the cutBookingTypeId is set to SDBits.UNDEFINED then the original bookingTypeId is taken. The method returns a
     * list of all booking affected by the cut with the first index of the list being the new cut booking. </p>
     *
     * <p> The rules and restrictions on this method are: <br> - If their is no booking with the same ywd and
     * profileId and spanSd in the periodSd then an exception is thrown. You should use setCreateBooking() method in
     * this instance.<br> - If the periodSd is greater then the spanSd and thus will replace the old booking, an
     * exception will be thrown. You should use setReplaceBooking() method in this instance. <br> - if split once the
     * underlying booking will reduce bookingSd and spanSd to make way for the new booking. This will create two
     * bookings<br> - if split twice (the cut is in the middle of an existing booking), three bookings will be
     * created. The first as the underlying with a reduced bookingSd and spanSd, a newly created booking and a mirror of
     * the first cut booking with reduced bookingSd and spanSd with a new bookingId<br> - the new booking act like
     * setCreateBooking() taking liabilityContactId and bookingPickupId values from the cut booking, setting actuals to
     * default and bookingState to OPEN<br> - the new cut bookings will take the cutBookingTypeId.<br> - The booking
     * must be OPEN or RESTRICTED to be able to be cut.<br> </p> <p> The split bookings will have their spanSD values
     * set to: <br> 1) spanSd start before - spanSD and bookingSd cut to end at start of periodSd<br> 2) spanSd end
     * after - spanSD and bookingSd cut to start at end of periodSd<br> 3) spanSd start before and end after - split
     * booking and set as above </p>
     *
     * @param forYwd
     * @param forPeriodSd
     * @param forProfileId
     * @param roomId
     * @param liableContactId
     * @param bookingDropOffId
     * @param bookingPickupId
     * @param newBookingTypeId
     * @param cutBookingTypeId
     * @param actionBits
     * @param requestedYwd
     * @param notes
     * @param owner
     * @return all the affected bookings
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     * @throws com.oathouse.oss.storage.exceptions.NoSuchIdentifierException
     */
    public List<BookingBean> setCutBooking(int forYwd, int forPeriodSd, int forProfileId, int roomId,
            int liableContactId, int bookingDropOffId, int bookingPickupId, int newBookingTypeId, int cutBookingTypeId,
            int actionBits, int requestedYwd, String notes, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException, NoSuchIdentifierException {
        // Check the new BookingTypeId
        if(!BTIdBits.isValid(newBookingTypeId, BTIdBits.TYPE_BOOKING)) {
            throw new IllegalArgumentException("The newBookingTypeId parameter is not a valid booking type");
        }
        // check the cut bookingTypeId
        if(!BTIdBits.equals(cutBookingTypeId, BTBits.UNDEFINED)) {
            if(!BTIdBits.isValid(cutBookingTypeId, BTIdBits.TYPE_BOOKING)) {
                throw new IllegalArgumentException("The cutBookingTypeId parameter is not a valid booking type");
            }
        }
        List<BookingBean> rtnList = new LinkedList<>();
        List<BookingBean> chkList = getAllFilteredObjectsForYwd(forYwd, forPeriodSd, -1, forProfileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true);
        // if there are no bookings then it can't be replaced and data transfered
        if(chkList.isEmpty()) {
            throw new PersistenceException("There are no bookings for this profile to be cut");
        }
        // there might be more than one booking to cut so check all are in a booked state and not immutable
        for(BookingBean cBooking : chkList) {
            if(cBooking.isEqualOrAfterState(BookingState.RECONCILED)) {
                throw new IllegalActionException("BookingStateIsReconciledOrLater");
            }
            // if the booking is Authorised then change back to completed
            if(cBooking.isState(BookingState.AUTHORISED)) {
                this.resetObjectStateToCompleted(cBooking.getYwd(), cBooking.getBookingId(), owner);
            }
            // We need to make sure there is a booking to cut
            int[] cutSpanSd = SDHolder.fitInSD(forPeriodSd, cBooking.getSpanSd());
            if(cutSpanSd[0] == -1 && cutSpanSd[1] == -1) {
                throw new PersistenceException("The spanSd is smaller than the periodSd so there is nothing to cut");
            }
        }
        // work thorugh each of the bookings
        for(BookingBean booking : chkList) {
            // get the new spanSd(s) form the origional booking (remeber periodSd is the dominant)
            int[] cutSpanSd = SDHolder.fitInSD(forPeriodSd, booking.getSpanSd());
            // we need to use the origional bookingId to replace it
            int bookingId = booking.getBookingId();
            for(int i = 0; i < cutSpanSd.length; i++) {
                if(cutSpanSd[i] > 0) {
                    // set the bookingSd to be the same as in the origional booking
                    int adjBookingSd = booking.getBookingSd();
                    // we have to consider the original bookingSd is different from the spanSd
                    if(SDBits.contain(SDHolder.compare(cutSpanSd[i], booking.getBookingSd()), SDBits.START_BEFORE)) {
                        adjBookingSd = SDHolder.addSD(cutSpanSd[i], booking.getBookingSd());
                    }
                    if(SDBits.contain(SDHolder.compare(cutSpanSd[i], booking.getBookingSd()), SDBits.END_AFTER)) {
                        adjBookingSd = SDHolder.addSD(booking.getBookingSd(), cutSpanSd[i]);
                    }

                    int cutTypeId = BTBits.equals(cutBookingTypeId, BTBits.UNDEFINED) ? booking.getBookingTypeId() : cutBookingTypeId;

                    BookingBean cutBooking = BookingBean.create(bookingId, forYwd, booking.getRoomId(), forProfileId,
                            adjBookingSd, booking.getLiableContactId(), booking.getBookingDropOffId(), booking.getBookingPickupId(),
                            cutTypeId, booking.getActionBits(), booking.getRequestedYwd(), booking.getNotes(), owner);
                    cutBooking.setState(booking.getState(), owner);
                    cutBooking.setSpanSd(cutSpanSd[i], owner);
                    rtnList.add(super.setObject(forYwd, cutBooking));
                    // reset the booking id
                    bookingId = this.generateIdentifier();
                }
            }
        }
        BookingBean newBooking = BookingBean.create(this.generateIdentifier(), forYwd, roomId, forProfileId, forPeriodSd,
                liableContactId, bookingDropOffId, bookingPickupId, newBookingTypeId, actionBits, requestedYwd, notes, owner);
        // add the new booking as the first
        rtnList.add(0, super.setObject(forYwd, newBooking));
        return rtnList;
    }

    /**
     * setFitBooking() allows a new booking to fit around an existing booking where the profileId of the new booking is
     * the same as that of the proposed booking. The new booking will 'fit' where the periodSd does not conflict with an
     * existing spanSd. This method allows a more dominant underlying booking to maintain its spanSd and bookingTypeId.
     * One of the key features of this method is that the underlying booking(s) can be in ANY booking state and is
     * defined by the bookingTypeMask. Additionally the 'fit' booking(s) can be set to any BookingState. The methods
     * setCreateBooking(), setReplaceBooking(), setCutBooking(), setFitBooking() and setLayerBooking() replace
     * setObject(), which now throws an exception.
     *
     * <p> The rules and restrictions on this method are: <br> - If their is no booking with the same ywd and
     * profileId and spanSd in the periodSd then an exception is thrown. You should use setCreateBooking() method in
     * this instance.<br> - if the periodSd cover two of more bookings it will fit all gaps<br> - The booking state
     * of the new booking can be set<br> - There are NO restrictions on booking state and is defined by the
     * bookingTypeMask. </p> <p> The fit bookings will have their periodSd values set to: <br> 1) periodSd starts
     * before spanSd start - periodSd end cut to spanSd start<br> 2) periodSd ends after spanSd ends - periodSd start
     * cut to soanSd end<br> 3) periodSd starts in one spanSd and ends in a second spanSd - periodSd start cut to end
     * of first spanSd and periodSd end cut to second spanSd start.<br> 4) periodSd covers the spanSd - periodSd split
     * to make room for the spanSd. </p>
     *
     * @param forYwd
     * @param forPeriodSd
     * @param forProfileId
     * @param roomId
     * @param liableContactId
     * @param bookingDropOffId
     * @param bookingPickupId
     * @param bookingTypeId
     * @param actionBits
     * @param bookingState
     * @param requestedYwd
     * @param notes
     * @param owner
     * @return the newly created BookingBean
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public List<BookingBean> setFitBooking(int forYwd, int forPeriodSd, int forProfileId, int roomId,
            int liableContactId, int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits,
            BookingState bookingState, int requestedYwd, String notes, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException {
        if(!BTIdBits.isValid(bookingTypeId, BTIdBits.TYPE_BOOKING)) {
            throw new IllegalArgumentException("The BookingTypeId is not a valid booking type");
        }
        List<BookingBean> rtnList = new LinkedList<>();
        // get ALL bookings touched by the periodSd
        List<BookingBean> chkList = getAllFilteredObjectsForYwd(forYwd, forPeriodSd, -1, forProfileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true);
        // if there are no bookings then it can't be replaced and data transfered
        if(chkList.isEmpty()) {
            throw new PersistenceException("There are no bookings for this profile and bookingType to be fitted around");
        }
        //work through each of the bookings
        int rightSd = forPeriodSd;
        int leftSd = 0;
        // assume the chkList is in SD order
        for(BookingBean booking : chkList) {
            // get the new spanSd(s) form the origional booking (remeber spanSD is the dominant)
            int[] splitSpanSd = SDHolder.fitInSD(booking.getSpanSd(), rightSd);
            // to make the fit booking we only deal with the left of the split
            leftSd = splitSpanSd[0];
            // the next booking will have the current right remaining SD to fit
            rightSd = splitSpanSd[1];
            // lets make a booking out of the left side
            if(leftSd > 0) {
                int bookingId = this.generateIdentifier();
                BookingBean fitBooking = BookingBean.create(bookingId, forYwd, roomId, forProfileId, leftSd, liableContactId,
                        bookingDropOffId, bookingPickupId, bookingTypeId, actionBits, requestedYwd, notes, owner);
                fitBooking.setState(bookingState, owner);
                // save the new booking and add it to the return list
                rtnList.add(super.setObject(forYwd, fitBooking));
            }
        }
        // finally check if thereis anything left on the rightSd
        if(rightSd > 0) {
            int bookingId = this.generateIdentifier();
            BookingBean fitBooking = BookingBean.create(bookingId, forYwd, roomId, forProfileId, rightSd, liableContactId,
                    bookingDropOffId, bookingPickupId, bookingTypeId, actionBits, requestedYwd, notes, owner);
            fitBooking.setState(bookingState, owner);
            // save the new booking and add it to the return list
            rtnList.add(super.setObject(forYwd, fitBooking));
        }
        // better to warn there was no fits than just return zero size rtnList
        if(rtnList.isEmpty()) {
            throw new PersistenceException("There are no booking gaps in this periodSd to fit a booking");
        }
        return rtnList;
    }

    /**
     * setLayerBooking() allows a new booking to be layered on top of any existing booking where the profileId of the
     * new booking is the same as that of the proposed booking. The new booking will exist along with the original
     * booking, be it partially or fully covering the spanSd. One of the key features of this method is that the
     * underlying booking(s) can be in ANY booking state and is defined by the bookingTypeMask. Additionally the layered
     * booking(s) can be set to any BookingState. The methods setCreateBooking(), setReplaceBooking(), setCutBooking(),
     * setFitBooking() and setLayerBooking() replace setObject(), which now throws an exception.
     *
     * <p> The rules and restrictions on this method are: <br> - If their is no booking with the same ywd and
     * profileId and spanSd in the periodSd then an exception is thrown. You should use setCreateBooking() method in
     * this instance.<br> - The booking state of the new booking can be set<br> - There are NO restrictions on
     * booking state and is defined by the bookingTypeMask. </p>
     *
     * @param forYwd
     * @param forPeriodSd
     * @param forProfileId
     * @param roomId
     * @param liableContactId
     * @param bookingDropOffId
     * @param bookingPickupId
     * @param bookingTypeId
     * @param actionBits
     * @param bookingState
     * @param requestedYwd
     * @param notes
     * @param owner
     * @return the newly created BookingBean
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public BookingBean setLayerBooking(int forYwd, int forPeriodSd, int forProfileId, int roomId, int liableContactId,
            int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, BookingState bookingState,
            int requestedYwd, String notes, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException {
        if(!BTIdBits.isValid(bookingTypeId, BTIdBits.TYPE_LAYER)) {
            throw new IllegalArgumentException("The BookingTypeId is not a valid layer type");
        }
        // get ALL bookings touched by the periodSd
        List<BookingBean> chkList = getAllFilteredObjectsForYwd(forYwd, forPeriodSd, -1, forProfileId, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF, true);
        // if there are no bookings then it can't be replaced and data transfered
        if(chkList.isEmpty()) {
            throw new PersistenceException("There are no bookings for this profile and bookingType to be layered");
        }
        // build the new booking
        final int bookingId = this.generateIdentifier();
        BookingBean booking = BookingBean.create(bookingId, forYwd, roomId, forProfileId, forPeriodSd, liableContactId,
                bookingDropOffId, bookingPickupId, bookingTypeId, actionBits, requestedYwd, notes, owner);
        // add the booking.
        return super.setObject(forYwd, booking);
    }

    /**
     * Sets a booking to Cancelled, retaining its BookingTypeId charge type
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @return the newly changed booking
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public BookingBean setCancelledBooking(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException {
        BookingBean booking = this.getObject(ywd, bookingId);
        if(BTBits.isNotBits(booking.getBookingTypeId(), BTIdBits.TYPE_BOOKING)) {
            throw new IllegalActionException("You can only cancel bookings that are of BookingTypeId TYPE_BOOKING");
        }
        int bookingTypeId = BTBits.turnOff(booking.getBookingTypeId(), BTIdBits.TYPE_BOOKING) | BTBits.CANCELLED_BIT;
        return setReplaceBookingType(ywd, bookingId, bookingTypeId, owner);
    }

    /**
     * Replaces all active bookings for a specified profileId with a suspended bookings. The charge bit of the
     * bookingTypeId will be maintained, and only ATTENDING and WAITING types will be replaced. Additionally only
     * bookings in an OPEN or RESTRICTED state will be suspended.
     *
     * @param profileId the profile Id of the person to be suspended
     * @param owner
     * @return a list of bookings that have been suspended
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public List<BookingBean> setSuspendedBookingsForProfile(int profileId, String owner) throws PersistenceException, IllegalActionException, MaxCountReachedException, NullObjectException, NoSuchIdentifierException {
        List<BookingBean> rtnList = new LinkedList<>();
        int bookingTypeIdMask = BTBits.ATTENDING_BIT;
        for(int ywd : this.getAllKeys()) {
            for(BookingBean booking : this.getYwdBookingsForProfile(ywd, ALL_PERIODS, profileId, bookingTypeIdMask, ActionBits.TYPE_FILTER_OFF)) {
                // make sure the booking is in a booked state
                if(booking.isEqualOrAfterState(BookingState.STARTED)) {
                    continue;
                }
                int bookingTypeId = BTBits.turnOff(booking.getBookingTypeId(), BTIdBits.TYPE_BOOKING) | BTBits.SUSPENDED_BIT;
                rtnList.add(this.setReplaceBookingType(ywd, booking.getBookingId(), bookingTypeId, owner));
            }
        }
        return rtnList;
    }

    /**
     * Reverses a suspended child by re-booking all the currently suspended bookings. It should be noted that the
     * original booking charge type of the bookingTypeId will be retained with only the identifier part of the
     * bookingTypeId replaced with the value passed.
     *
     * @param profileId
     * @param btIdBit
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public List<BookingBean> setReinstateBookingsForProfile(int profileId, int btIdBit, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, MaxCountReachedException, IllegalActionException {
        List<BookingBean> rtnList = new LinkedList<>();
        // clean up the btIdBit to ensure it is only the IdentifierBit
        final int cleanBtId = btIdBit & BTIdBits.TYPE_IDENTIFIER;
        for(int ywd : this.getAllKeys()) {
            for(BookingBean booking : this.getYwdBookingsForProfile(ywd, ALL_PERIODS, profileId, BTBits.SUSPENDED_BIT, ActionBits.TYPE_FILTER_OFF)) {
                int bookingTypeId = BTBits.turnOff(booking.getBookingTypeId(), BTBits.SUSPENDED_BIT) | cleanBtId;
                rtnList.add(this.setReplaceBookingType(ywd, booking.getBookingId(), bookingTypeId, owner));
            }
        }
        return rtnList;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="set() attribute methods for BookingBean">
    /*
     * *****************************************************************
     * S E T B O O K I N G A T T R I B U T E M E T H O D S
     * ****************************************************************
     */
    /**
     * Used to set the BookingTypeId.The booking type is validated as part of this call.
     *
     * @param ywd
     * @param bookingId
     * @param bookingTypeId
     * @param owner
     * @return the updated booking
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public BookingBean setObjectBookingTypeId(int ywd, int bookingId, int bookingTypeId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        if(!BTIdBits.isValid(bookingTypeId, BTIdBits.TYPE_BOOKING)) {
            throw new IllegalActionException("InvalidBookingTypeId");
        }
        booking.setBookingTypeId(bookingTypeId, owner);
        // save the changes
        return super.setObject(ywd, booking);
    }

    /**
     * Used to set the spanSd of a booking to be the same as a bookingSd. This has limited restrictions imposed and can
     * be done in any state other than AUTHORISED, RECONCILED and ARCHIVED
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @return the updated BookingBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public BookingBean setSpanSdToBookingSd(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        if(booking.isStateType(BookingState.TYPE_BILLING)) {
            throw new IllegalActionException("IllegalChangeOfSpanSd");
        }
        booking.setSpanSd(booking.getBookingSd(), owner);
        // save the changes
        return super.setObject(ywd, booking);
    }

    /**
     * Sets the state of a booking. Rules: * A RECONCILED booking cannot be changed Once not OPEN, a booking cannot be
     * returned to OPEN AUTHORISED can be set back to RESTRICTED, STARTED, COMPLETED or forward to RECONCILED RESTRICTED
     * cannot be set directly to AUTHORISED or RECONCILED: must be COMPLETED first From STARTED a booking can only go to
     * COMPLETED: must be reset to return to RESTRICTED <p> Basic flow through
     * OPEN-RESTRICTED-STARTED-COMPLETED-AUTHORISED-RECONCILED </p>
     *
     * @param ywd the year week day key with day set to 0
     * @param bookingId
     * @param state
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setObjectState(int ywd, int bookingId, BookingState state, String owner)
            throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        BookingBean booking = getObject(ywd, bookingId);
        if(state.equals(booking.getState())) {
            return;
        }
        // Business rule: cannot set state UNDEFINED
        if(state == BookingState.UNDEFINED || state == BookingState.NO_VALUE) {
            throw new IllegalActionException("CannotSetUndefinedOrNoValue");
        }
        // Business Rule: cannot change state back to OPEN
        if(state == BookingState.OPEN) {
            throw new IllegalActionException("CannotSetBackToOpen");
        }
        // Business Rule: cannot change OPEN, RESTRICTED or STARTED directly to AUTHORISED or RECONCILED:
        // must be at least COMPLETED
        if(booking.isBeforeState(BookingState.COMPLETED) && BookingState.isStateType(state, BookingState.TYPE_BILLING)) {
            throw new IllegalActionException("CanOnlyAuthoriseFromCompleted");
        }
        // Business Rule: once STARTED can only go to COMPLETED
        if(booking.isState(BookingState.STARTED) && !state.equals(BookingState.COMPLETED)) {
            throw new IllegalActionException("CanOnlyCompleteFromStarted");
        }
        // Business Rule: Must be COMPLETE before you can AUTHORISE
        if(!booking.isState(BookingState.COMPLETED) && state.equals(BookingState.AUTHORISED)) {
            throw new IllegalActionException("CanOnlyAuthoriseFromComplete");
        }
        // Business Rule: Must be AUTHORISED before you can RECONCILE
        if(!booking.isState(BookingState.AUTHORISED) && state.equals(BookingState.RECONCILED)) {
            throw new IllegalActionException("CanOnlyReconcileFromAuthorise");
        }
        // Business Rule: Must be RECONCILED before you can ARCHIVE
        if(!booking.isState(BookingState.RECONCILED) && state.equals(BookingState.ARCHIVED)) {
            throw new IllegalActionException("CanOnlyArchiveFromReconcile");
        }
        // Business Rule: cannot change state once ARCHIVED (final invoicing has been paid)
        if(booking.isState(BookingState.ARCHIVED)) {
            throw new IllegalActionException("BookingARCHIVED");
        }
        // Business Rule: cannot set AUTHORISED if booking is COMPLETED
        // until actualStart, actualEnd are set
        if(state.equals(BookingState.COMPLETED)
                && (booking.getActualStart() == -1 || booking.getActualEnd() == -1)) {
            throw new IllegalActionException("CannotCompleteTillAllActualsSet");
        }
        // you can't jump from COMPLETED back to STARTED or RESTRICTED (use reset)
        if(booking.isState(BookingState.COMPLETED)
                && (state.equals(BookingState.STARTED) || state.equals(BookingState.RESTRICTED))) {
            throw new IllegalActionException("CanOnlyResetBack");
        }
        booking.setState(state, owner);
        // save the changes
        super.setObject(ywd, booking);
    }

    /**
     * Used to complete a booking that is OPEN, RESTRICTED or STARTED. This sets the actual start and end to be the same
     * value as the booked. This is a quick set for Bookings where the actual has not changed from the original booking,
     * or non ATTENDING bookings are transfered to COMPLETE.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setBookingComplete(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = this.getObject(ywd, bookingId);
        // fail safe to deal with non relevant bookings tidily
        if(booking.isEqualOrAfterState(BookingState.COMPLETED)) {
            return;
        }
        // use manager level to validate
        this.setObjectActualStart(ywd, bookingId, booking.getBookingStart(), owner);
        this.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        // we need to check if we get an exception so we can roll back
        try {
            this.setObjectActualEnd(ywd, bookingId, booking.getBookingEnd(), owner);
            this.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        } catch(IllegalActionException iae) {
            // rollback
            this.resetObjectActualStartEnd(ywd, bookingId, owner);
            throw new IllegalActionException("Failed to Complete booking, booking has been rolled back: " + iae.getMessage());
        }

    }

    /**
     * Sets the actual start time of a booking. <p> The rules and restrictions on this method are: <br> - The booking
     * must be 'live' (state is OPEN or RESTRICTED) <br> - The startTime must be valid (0 - MAX_PERIOD_LENGTH) <br>
     * - The startTime must be be before the bookingSd end time - the actual end time must not be set </p> <p>
     * Behaviour: <br> - The booking state is set to STARTED <br> </p>
     *
     * @param ywd
     * @param bookingId
     * @param startTime
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setObjectActualStart(int ywd, int bookingId, int startTime, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // The start time must be valid
        if(startTime < 0 || startTime > MAX_PERIOD_LENGTH) {
            throw new IllegalActionException("BookingActualStartInvalid");
        }
        // as they can be set in any order, check if startTime > endTime
        if(booking.getActualEnd() >= 0 && startTime > booking.getActualEnd()) {
            throw new IllegalActionException("BookingActualEndEarlierThanActualStart");
        }
        booking.setActualStart(startTime, owner);
        // save the changes
        super.setObject(ywd, booking);
    }

    /**
     * Sets the actual end time for a booking. For the end time to be set, the actual start time must have been set.
     * (state is STARTED)
     *
     * <p> The rules and restrictions on this method are: <br> - The booking must be STARTED <br> - The endTime must
     * be valid (0 - MAX_PERIOD_LENGTH) <br> - The endTime must be be after the actual start time </p> <p> Behaviour:
     * <br> - The booking state is set to COMPLETED <br> </p>
     *
     * @param ywd the year week day key with day set to 0
     * @param bookingId
     * @param endTime
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setObjectActualEnd(int ywd, int bookingId, int endTime, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        BookingBean booking = getObject(ywd, bookingId);
        // The end time must be valid
        if(endTime < 0 || endTime > MAX_PERIOD_LENGTH) {
            throw new IllegalActionException("BookingActualEndInvalid");
        }
        // actual end must be after the actual start
        if(booking.getActualStart() >= 0 && endTime <= booking.getActualStart()) {
            throw new IllegalActionException("BookingActualEndEarlierThanActualStart");
        }
        // set actual end and set the booking to COMPLETED
        booking.setActualEnd(endTime, owner);
        // save the changes
        super.setObject(ywd, booking);
    }

    /**
     * Sets the actualPickupId but only if the BookingState is not AUTHORISED or RECONCILED
     *
     * @param ywd
     * @param bookingId
     * @param actualPickupId
     * @param owner
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectActualPickupId(int ywd, int bookingId, int actualPickupId, String owner) throws IllegalActionException, NoSuchIdentifierException, PersistenceException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        booking.setActualPickupId(actualPickupId, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Sets the actualDropOffId but only if the BookingState is not AUTHORISED or RECONCILED
     *
     * @param ywd
     * @param bookingId
     * @param actualDropOffId
     * @param owner
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectActualDropOffId(int ywd, int bookingId, int actualDropOffId, String owner) throws IllegalActionException, NoSuchIdentifierException, PersistenceException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        booking.setActualDropOffId(actualDropOffId, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Resets the booking actuals <p> The rules and restrictions on this method are: <br> - The booking must be
     * STARTED or COMPLETED <br> </p> <p> Behaviour: <br> - The booking state is set to RESTRICTED <br> - All
     * actuals are set back to INITALISATION <br> </p>
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void resetObjectActualStartEnd(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        BookingBean booking = getObject(ywd, bookingId);
        // A booking must be active to be reset
        if(!booking.isStateType(BookingState.TYPE_ACTIVE)) {
            throw new IllegalActionException("BookingIsNotActive");
        }
        booking.setActualStart(ObjectEnum.INITIALISATION.value(), owner);
        booking.setActualEnd(ObjectEnum.INITIALISATION.value(), owner);
        booking.setActualPickupId(ObjectEnum.INITIALISATION.value(), owner);
        booking.setActualDropOffId(ObjectEnum.INITIALISATION.value(), owner);
        booking.setState(BookingState.RESTRICTED, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Resets the booking spanSd back to the bookingSd.
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void resetObjectSpanSd(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        booking.setSpanSd(booking.getBookingSd(), owner);
        super.setObject(ywd, booking);
    }

    /**
     * Resets the booking from COMPLETED to STARTED. This is generally used when the rules of when a booking should be
     * marked as completed have changed and result in the booking not being completed. <p> The rules and restrictions on
     * this method are: <br> - The booking must be COMPLETED only <br> </p> <p> Behaviour: <br> - The booking
     * state is set to STARTED <br> - No changes are made to any of the actuals<br> </p>
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void resetObjectStateToStarted(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // A booking must be COMPLETED to be reset back to STARTED
        if(!booking.isState(BookingState.COMPLETED)) {
            throw new IllegalActionException("BookingIsNotCompleted");
        }
        booking.setState(BookingState.STARTED, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Resets the booking from AUTHORISED or RECONCILED to COMPLETE. This is generally used when the rules of when a booking
     * has been authorised but needs to be rechecked<p> The rules and restrictions on
     * this method are: <br> - The booking must be AUTHORISED or after <br> </p> <p> Behaviour: <br> - The booking
     * state is set to COMPLETED <br> - No changes are made to any of the actuals<br> </p>
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void resetObjectStateToCompleted(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // A booking must be COMPLETED to be reset back to STARTED
        if(!booking.isEqualOrAfterState(BookingState.AUTHORISED)) {
            throw new IllegalActionException("BookingIsBeforeAuthoised");
        }
        booking.setState(BookingState.COMPLETED, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Resets the booking from RECONCILED or ARCHIVED back to AUTHORISED . This is generally used when a booking
     * has been reconciled but then voided
     * <p> The rules and restrictions on this method are: <br>
     * - The booking must be RECONCILED or after <br> </p>
     * <p> Behaviour: <br>
     * - The booking state is set to AUTHORISED <br>
     * - if the booking is in any other state it is ignored<br> </p>
     *
     * @param ywd
     * @param bookingId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void resetObjectStateToAuthorised(int ywd, int bookingId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // A booking must be RECONCILED or ARCHIVED to be reset back to AUTHORISED
        if(booking.isEqualOrAfterState(BookingState.RECONCILED)) {
            booking.setState(BookingState.AUTHORISED, owner);
            super.setObject(ywd, booking);
        }
    }

    /**
     * Sets the bookingPickupId but only if the BookingState is before COMPLETED
     *
     * @param ywd
     * @param bookingId
     * @param bookingPickupId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setObjectBookingPickupId(int ywd, int bookingId, int bookingPickupId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // Bookings must be before COMPLETED to be changed
        if(booking.isEqualOrAfterState(BookingState.COMPLETED)) {
            throw new IllegalActionException("BookingNotBeforeCompleted");
        }
        booking.setBookingPickupId(bookingPickupId, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Sets the bookingDropOffId but only if the BookingState is not AUTHORISED or RECONCILED
     *
     * @param ywd
     * @param bookingId
     * @param bookingDropOffId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setObjectBookingDropOffId(int ywd, int bookingId, int bookingDropOffId, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        // Bookings must be 'booked' to be changed
        if(booking.isEqualOrAfterState(BookingState.COMPLETED)) {
            throw new IllegalActionException("BookingNotBeforeCompleted");
        }
        booking.setBookingDropOffId(bookingDropOffId, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Allows the action bits of a booking to be set
     *
     * @param ywd
     * @param bookingId
     * @param actionBits
     * @param isOn the value of isOn
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void setObjectActionBits(int ywd, int bookingId, int actionBits, boolean isOn, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        BookingBean booking = getObject(ywd, bookingId);
        int newAction = isOn ? ActionBits.turnOn(booking.getActionBits(), actionBits)
                : ActionBits.turnOff(booking.getActionBits(), actionBits);
        booking.setActionBits(newAction, owner);
        super.setObject(ywd, booking);
    }

    /**
     * Sets the notes for a booking. This replaces any existing notes
     *
     * @param ywd the year week day key with day set to 0
     * @param bookingId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setObjectNotes(int ywd, int bookingId, String notes, String owner)
            throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        BookingBean booking = getObject(ywd, bookingId);
        // Bookings must be 'booked' to be changed
        if(booking.isStateType(BookingState.TYPE_BILLING)) {
            throw new IllegalActionException("BookingNotBeforeAuthorised");
        }
        booking.setNotes(notes, owner);
        super.setObject(ywd, booking);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private consolidation and helper methods">
    /*
     * *******************************************************************************
     * P R I V A T E C O N S O L I D A T I O N A N D H E L P E R M E T H O D S
     * ******************************************************************************
     */
    /*
     * Covers a number of different getAllObject methods calls with filter over a week
     */
    private List<BookingBean> getAllFilteredObjectsForYw(int yw0, int roomId, int profileId, int bookingTypeIdMask,
            int actionInclude) throws PersistenceException {
        LinkedList<BookingBean> rtnList = new LinkedList<>();
        // just makes sure the yw0 starts at the beginning of the week
        int cleanYw0 = YWDHolder.getYW(yw0);
        // run through the 7 days of the week
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            // get all filtered objects for each day
            rtnList.addAll(getAllFilteredObjectsForYwd(cleanYw0 + day, ALL_PERIODS, roomId, profileId, bookingTypeIdMask, actionInclude, false));
        }
        return (rtnList);
    }

    /*
     * Covers a number of different getAllObject methods calls with filter
     */
    protected List<BookingBean> getAllFilteredObjectsForYwd(int ywd, int periodSd, int roomId, int profileId,
            int bookingTypeIdMask, int actionMask, boolean useSpan) throws PersistenceException {
        LinkedList<BookingBean> rtnList = new LinkedList<>();
        int period = periodSd < 0 ? ALL_PERIODS : periodSd;
        for(BookingBean booking : getAllObjects(ywd)) {
            /*
             * laid out this way for clarity of the logic
             */
            // check profileId filter (-1 is all profileIds)
            if(profileId != ALL_PROFILE && profileId != booking.getProfileId()) {
                continue;
            }
            // check roomId filter (-1 is all roomIds)
            if(roomId != ALL_ROOM && roomId != booking.getRoomId()) {
                continue;
            }
            // filter out mutually exclusive identifer bits from the bookingTypeId that are not wanted
            if(!BTIdBits.isFilter(booking.getBookingTypeId(), bookingTypeIdMask)) {
                continue;
            }
            // if the actionMask is greater than zero then include the action filter
            if(actionMask > 0 && !booking.isActionOn(actionMask)) {
                continue;
            }
            // check if the bookingSd is in range
            if(!SDHolder.inRange(useSpan ? booking.getSpanSd() : booking.getBookingSd(), period)) {
                continue;
            }
            // all the filters have been applied so add
            rtnList.add(booking);
        }
        Collections.sort(rtnList);
        return (rtnList);
    }

    /*
     * Replaces only the booking type for a given bookingId. Other than the bookingTypeId, all other attributes of the
     * booking are maintained.
     */
    private BookingBean setReplaceBookingType(int ywd, int bookingId, int bookingTypeId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException {
        BookingBean b = this.getObject(ywd, bookingId);
        return (setReplaceBooking(ywd, b.getBookingSd(), b.getProfileId(), b.getRoomId(), b.getLiableContactId(),
                b.getBookingDropOffId(), b.getBookingPickupId(), bookingTypeId, b.getActionBits(), b.getRequestedYwd(),
                b.getNotes(), owner));
    }
    //</editor-fold>
}
