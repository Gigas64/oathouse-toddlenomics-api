/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.ServicePool;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.builders.BSBuilder;
import com.oathouse.ccm.builders.RSBuilder;
import com.oathouse.ccm.builders.TSBuilder;
import com.oathouse.ccm.cma.dto.ChildBookingDTO;
import com.oathouse.ccm.cma.dto.ChildBookingTransform;
import com.oathouse.ccm.cma.exceptions.ExceedCapacityException;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.RelationType;
import com.oathouse.ccm.cos.properties.LegacySystem;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.ObjectStoreException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingService_LegacyTestMethods {

    private ServicePool engine;
    private String owner = BookingBean.SYSTEM_OWNED;
    private int roomId;
    private int timetableId;
    private int educationTimetableId;
    private int accountId1;
    private int accountId2;
    private int accountId3;
    private int childId11;
    private int childId21;
    private int childId22;
    private int childId23;
    private int childId31;
    private int childId32;
    private int contactId1;
    private int contactId2;
    private int contactId3;

    @Before
    public void setUp() throws Exception {
        engine = BSBuilder.getEngine();
        assertTrue(engine.clearAll());
        // set up nursery
        RSBuilder.setupSystemProperties();
        RSBuilder.setupAccountHolder();
        RSBuilder.setupHolidayConcession();
        roomId = RSBuilder.setupRoom();
        timetableId = TSBuilder.setupTimetables(roomId);
        //set up two accounts
        contactId1 = BSBuilder.setupContact().getContactId();
        accountId1 = BSBuilder.setupAccount("oat00tst", contactId1).getAccountId();
        contactId2 = BSBuilder.setupContact().getContactId();
        accountId2 = BSBuilder.setupAccount("oat01tst", contactId2).getAccountId();
        contactId3 = BSBuilder.setupContact().getContactId();
        accountId3 = BSBuilder.setupAccount("oat02tst", contactId3).getAccountId();

        // set children for account 1
        childId11 = setChild(accountId1, (2 * 52) + 13).getChildId();
        childId21 = setChild(accountId2, (2 * 52) + 26).getChildId();
        childId22 = setChild(accountId2, (4 * 52) + 31).getChildId();
        childId23 = setChild(accountId2, (4 * 52) + 31).getChildId();
        childId31 = setChild(accountId3, (3 * 52) + 1).getChildId();
        childId32 = setChild(accountId3, (4 * 52) + 18).getChildId();
        engine.getChildService().setCustodialRelationship(childId11, contactId1, RelationType.FATHERS_PARTNER, owner);
        engine.getChildService().setCustodialRelationship(childId21, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId22, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId23, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId31, contactId3, RelationType.AUNT, owner);
        engine.getChildService().setCustodialRelationship(childId32, contactId3, RelationType.AUNT, owner);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * System test: Simple test to check the empty booking, make sure everything starts OK and the basic calls work
     */
    @Test
    public void system01_StartupTest() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(6);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);
        Set<Integer> bookingIdSet = engine.getChildBookingService().getBookingManager().getAllIdentifier();
        assertTrue(bookingIdSet.isEmpty());
        // System.out.println(engine.getChildBookingService().getBookingTypeManager().getAllObjectAsString(BTIdBits.TYPE_IDENTIFIER));
        List<BookingTypeBean> bookingTypeList = engine.getChildBookingService().getBookingTypeManager().getAllObjects(BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG);
        assertEquals(30, bookingTypeList.size());
        // create one booking
        BookingBean booking1 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(1, engine.getChildBookingService().getBookingManager().getAllObjects(ywd).size());
        assertEquals(booking1, engine.getChildBookingService().getBookingManager().getAllObjects(ywd).get(0));
        assertEquals(booking1, engine.getChildBookingService().getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG).get(0));

        // create two bookings
        BookingBean booking2 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId21,
                contactId2, -1, contactId2, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(2, engine.getChildBookingService().getBookingManager().getAllObjects(ywd).size());
        assertEquals(booking1, engine.getChildBookingService().getBookingManager().getAllObjects(ywd).get(0));
        assertEquals(booking2, engine.getChildBookingService().getBookingManager().getAllObjects(ywd).get(1));
        assertEquals(booking1, engine.getChildBookingService().getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId11, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG).get(0));
        assertEquals(booking2, engine.getChildBookingService().getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId21, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG).get(0));
        assertEquals(booking1, engine.getChildBookingService().getYwdBookingsForRoom(ywd, BookingManager.ALL_PERIODS, roomId, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG).get(0));
        assertEquals(booking2, engine.getChildBookingService().getYwdBookingsForRoom(ywd, BookingManager.ALL_PERIODS, roomId, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG).get(1));
    }

    /**
     * System test: create a booking and change the booking to check the Span remains (booking RESTRICTED)
     */
    @Test
    public void system02_CreateBookingTest() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);
        BookingBean booking1 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(SDHolder.getSD(20, 54), booking1.getBookingSd());
        assertEquals(SDHolder.getSD(20, 54), booking1.getSpanSd());
        // now create another one which changes the bookingSd only
        bookingSd = SDHolder.getSD(20, 24);
        BookingBean booking2 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(SDHolder.getSD(20, 24), booking2.getBookingSd());
        assertEquals(SDHolder.getSD(20, 54), booking2.getSpanSd());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_CreateBookingWithAdminReset() throws Exception {
        LinkedList<BookingBean> history;
        final int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);

        // create a booking
        int bookingSd = SDHolder.getSD(20, 54);
        final BookingBean booking01 =  engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11, contactId1, -1, contactId1,
                BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);

        //check History
        history = (LinkedList<BookingBean>) engine.getChildBookingHistory().getBookingHistory(booking01.getBookingId());
        assertEquals(1, history.size());
        assertEquals(SDHolder.getSD(20, 54), history.getLast().getBookingSd());
        assertEquals(SDHolder.getSD(20, 54), history.getLast().getSpanSd());

        //make the booking RESTRICTED
        engine.getChildBookingService().setNoticePeriodRestrictions(ywd, requestedYwd, childId11);
        assertEquals(BookingState.RESTRICTED, engine.getChildBookingService().getBookingManager().getObject(ywd, booking01.getBookingId()).getState());

        // now create another one which changes the bookingSd
        bookingSd = SDHolder.getSD(20, 24);
        BookingBean booking02 =  engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11, contactId1, -1, contactId1,
                BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(SDHolder.getSD(20, 24), booking02.getBookingSd());
        assertEquals(SDHolder.getSD(20, 54), booking02.getSpanSd());

        // check the action bits are off
        assertEquals(ActionBits.ALL_OFF, booking02.getActionBits());

        //check History
        history = (LinkedList<BookingBean>) engine.getChildBookingHistory().getBookingHistory(booking01.getBookingId());
        assertEquals(2, history.size());
        assertEquals(SDHolder.getSD(20, 24), history.getLast().getBookingSd());
        assertEquals(SDHolder.getSD(20, 54), history.getLast().getSpanSd());

        // make the correction
        bookingSd = SDHolder.getSD(10, 29);
        BookingBean booking03 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner, VABoolean.RESET_SPANSD, VABoolean.IMMEDIATE_RECALC);
        assertEquals(SDHolder.getSD(10, 29), booking03.getBookingSd());
        assertEquals(SDHolder.getSD(10, 29), booking03.getSpanSd());

        // check the ActionBits.IMMEDIATE_RECALC_BIT is set
        assertTrue(booking02.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT));

        //check History
        history = (LinkedList<BookingBean>) engine.getChildBookingHistory().getBookingHistory(booking01.getBookingId());
        assertEquals(3, history.size());
        assertEquals(SDHolder.getSD(10, 29), history.getLast().getBookingSd());
        assertEquals(SDHolder.getSD(10, 29), history.getLast().getSpanSd());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit02_CreateBookingOnClosedDay() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);
        // make ywd a closed day and make sure it fails
        ywd += 6;
        try {
            engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11, contactId1, -1, contactId1,
                    BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            assertEquals("YwdRoomClosed", iae.getMessage());
        }

        // test BTFlagBits.BOOKING_CLOSED_FLAG
        engine.getChildBookingService().getBookingTypeManager().setObjectFlagBits(BTIdBits.ATTENDING_STANDARD, BTFlagBits.BOOKING_CLOSED_FLAG, true, owner);
        assertTrue(engine.getChildBookingService().getBookingTypeManager().getObject(BTIdBits.ATTENDING_STANDARD).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG));
        final BookingBean booking01 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11, contactId1, -1, contactId1,
                BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(booking01, engine.getChildBookingService().getBookingManager().getObject(ywd, booking01.getBookingId()));

        // test ActionBits.ADHOC_BOOKING_BIT
        BookingBean booking02 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner, VABoolean.ADHOC_BOOKING);
        assertEquals(booking02, engine.getChildBookingService().getBookingManager().getObject(ywd, booking02.getBookingId()));

    }

    /**
     * Unit test: isBookingTypeFlagOn and IsBookingType
     */
    @Test
    public void unit03_IsMethodsOnBookingType() throws Exception {
        ChildBookingService bookingService = engine.getChildBookingService();
        int bookingSd = SDHolder.getSD(20, 54);
        BookingBean booking = setBooking(bookingSd, childId11, BTIdBits.ATTENDING_STANDARD);
        assertTrue(bookingService.isBookingType(booking.getYwd(), booking.getBookingId(), BTIdBits.ATTENDING_STANDARD));
        assertFalse(bookingService.isBookingType(booking.getYwd(), booking.getBookingId(), BTIdBits.ATTENDING_SPECIAL));

        assertTrue(bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.CHARGE_FLAG));
        assertFalse(bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.ALLOWANCE_FLAG));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit04_getIncompleteBookings() throws Exception {
        ChildBookingService bookingService = engine.getChildBookingService();
        int bookingSd = SDHolder.getSD(20, 54);
        int start = SDHolder.getStart(bookingSd);
        int end = SDHolder.getEnd(bookingSd);

        int ywd = CalendarStatic.getRelativeYWD(-1);

        BookingBean booking01 = setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD);
        // TEST
        assertEquals(1, bookingService.getBookingManager().getIncompleteBookings().size());
        assertEquals(booking01, bookingService.getBookingManager().getIncompleteBookings().get(0));
        bookingService.setBookingActualStart(ywd, booking01.getBookingId(), start, owner);
        bookingService.setBookingActualEnd(ywd, booking01.getBookingId(), end, owner);
        assertEquals(0, bookingService.getBookingManager().getIncompleteBookings().size());

        BookingBean booking02 = setBooking(ywd, bookingSd, childId21, BTIdBits.ATTENDING_STANDARD);
        setBooking(ywd, bookingSd, childId22, BTIdBits.HOLIDAY_STANDARD);
        setBooking(ywd, bookingSd, childId23, BTIdBits.CANCELLED_STANDARD);
        assertEquals(1, bookingService.getBookingManager().getIncompleteBookings().size());
        assertEquals(booking02, bookingService.getBookingManager().getIncompleteBookings().get(0));

        ywd = CalendarStatic.getRelativeYWD(0);

        setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD);
        setBooking(ywd, bookingSd, childId21, BTIdBits.CANCELLED_STANDARD);
        setBooking(ywd, bookingSd, childId22, BTIdBits.SICK_NOCHARGE);
        setBooking(ywd, bookingSd, childId23, BTIdBits.ATTENDING_STANDARD);
        assertEquals(1, bookingService.getBookingManager().getIncompleteBookings().size());
        assertEquals(booking02, bookingService.getBookingManager().getIncompleteBookings().get(0));

    }

    /**
     * Unit test:
     */
    @Test
    public void unit05_getUncheckedBookings() throws Exception {
        ChildBookingService bookingService = engine.getChildBookingService();
        int bookingSd = SDHolder.getSD(20, 54);
        int start = SDHolder.getStart(bookingSd);
        int end = SDHolder.getEnd(bookingSd);

        int ywd = CalendarStatic.getRelativeYWD(-1);

        int bId01 = setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD).getBookingId();
        // TEST basic
        assertEquals(0, bookingService.getBookingManager().getUncheckedBookings().size());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(1, bookingService.getBookingManager().getUncheckedBookings().size());
        assertEquals(bId01, bookingService.getBookingManager().getUncheckedBookings().get(0).getBookingId());
        // Add Non ATTENDING
        int bId02 = setBooking(ywd, bookingSd, childId22, BTIdBits.HOLIDAY_STANDARD).getBookingId();
        int bId03 = setBooking(ywd, bookingSd, childId23, BTIdBits.CANCELLED_STANDARD).getBookingId();
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId02).getState());
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId03).getState());
        assertEquals(3, bookingService.getBookingManager().getUncheckedBookings().size());
        assertEquals(bId01, bookingService.getBookingManager().getUncheckedBookings().get(0).getBookingId());
        assertEquals(bId02, bookingService.getBookingManager().getUncheckedBookings().get(1).getBookingId());
        assertEquals(bId03, bookingService.getBookingManager().getUncheckedBookings().get(2).getBookingId());
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId02).getState());
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId03).getState());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit06_setActualCompleted() throws Exception {
        ChildBookingService bookingService = engine.getChildBookingService();
        SystemPropertiesBean sp = engine.getPropertiesService().getSystemProperties();

        new NonStrictExpectations() {
            BillingService billingServiceMock;
            {
                BillingService.getInstance(); returns(billingServiceMock);
            }
        };

        int bookingSd = SDHolder.getSD(20, 54);
        int start = SDHolder.getStart(bookingSd);
        int end = SDHolder.getEnd(bookingSd);

        int ywd = CalendarStatic.getRelativeYWD(0);

        // Test to completion with mandatory off
        boolean actualDropOffIdMandatory = false;
        boolean actualPickupIdMandatory = false;
        engine.getPropertiesService().setSystemPropertiesBaseModule(8, 8, false, 2, 3, false, false, LegacySystem.NONE,
                actualDropOffIdMandatory, actualPickupIdMandatory, owner);
        int bId01 = setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD).getBookingId();
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        assertEquals(start, bookingService.getBookingManager().getObject(ywd, bId01).getActualStart());
        //test if you can update a time
        bookingService.setBookingActualStart(ywd, bId01, start + 10, owner);
        assertEquals(start + 10, bookingService.getBookingManager().getObject(ywd, bId01).getActualStart());
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());

        // Test to completion with actualDropOffIdMandatory On
        actualDropOffIdMandatory = true;
        actualPickupIdMandatory = false;
        engine.getPropertiesService().setSystemPropertiesBaseModule(8, 8, false, 2, 3, false, false, LegacySystem.NONE,
                actualDropOffIdMandatory, actualPickupIdMandatory, owner);
        bId01 = setBooking(ywd, bookingSd, childId21, BTIdBits.ATTENDING_STANDARD).getBookingId();
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualDropOff(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());

        // Test to completion with actualPickupIdMandatory On
        actualDropOffIdMandatory = false;
        actualPickupIdMandatory = true;
        engine.getPropertiesService().setSystemPropertiesBaseModule(8, 8, false, 2, 3, false, false, LegacySystem.NONE,
                actualDropOffIdMandatory, actualPickupIdMandatory, owner);
        bId01 = setBooking(ywd, bookingSd, childId22, BTIdBits.ATTENDING_STANDARD).getBookingId();
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualPickup(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());

        // Test to completion with actualPickupIdMandatory On
        actualDropOffIdMandatory = true;
        actualPickupIdMandatory = true;
        engine.getPropertiesService().setSystemPropertiesBaseModule(8, 8, false, 2, 3, false, false, LegacySystem.NONE,
                actualDropOffIdMandatory, actualPickupIdMandatory, owner);
        bId01 = setBooking(ywd, bookingSd, childId23, BTIdBits.ATTENDING_STANDARD).getBookingId();
        assertEquals(BookingState.OPEN, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualPickup(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualDropOff(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());

        bookingService.resetBookingActuals(ywd, bId01, owner);
        assertEquals(BookingState.RESTRICTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualDropOff(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualStart(ywd, bId01, start, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualEnd(ywd, bId01, end, owner);
        assertEquals(BookingState.STARTED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        bookingService.setBookingActualPickup(ywd, bId01, contactId2, owner);
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());

        // let us try reversing out of completed by resetting just the start
        try {
            bookingService.setBookingActualStart(ywd, bId01, ObjectEnum.INITIALISATION.value(), owner);
            fail("should have thrown an exception");
        } catch(IllegalActionException iae) {
            assertEquals("BookingActualStartInvalid", iae.getMessage());
        }
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        // let us try changing a value when completed
        bookingService.setBookingActualStart(ywd, bId01, start + 10, owner);
        assertEquals(start + 10, bookingService.getBookingManager().getObject(ywd, bId01).getActualStart());
        assertEquals(BookingState.COMPLETED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        // try changing an actual when AUTHORISED
        bookingService.setBookingAuthorised(ywd, bId01, owner);
        assertEquals(BookingState.AUTHORISED, bookingService.getBookingManager().getObject(ywd, bId01).getState());
        try {
            bookingService.setBookingActualStart(ywd, bId01, start, owner);
            fail("should have thrown an exception");
        } catch(IllegalActionException iae) {
            assertEquals("BookingHasEnded", iae.getMessage());
        }


    }

    /**
     * Unit test:
     */
    @Test
    public void unit07_SetBooking_BookingTypeId() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);
        BookingBean booking1 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        assertEquals(BTIdBits.ATTENDING_STANDARD, booking1.getBookingTypeId());
        engine.getChildBookingService().setBooking(ywd, booking1.getBookingId(), BTIdBits.CANCELLED_NOCHARGE, owner);
        assertEquals(BTIdBits.CANCELLED_NOCHARGE, booking1.getBookingTypeId());
        // test capacity
        engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId21, contactId2, -1, contactId2, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId22, contactId2, -1, contactId2, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId23, contactId2, -1, contactId2, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId31, contactId3, -1, contactId3, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId32, contactId3, -1, contactId3, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);

        // now try changing booking 1 to ATTENDING
        try {
            engine.getChildBookingService().setBooking(ywd, booking1.getBookingId(), BTIdBits.ATTENDING_STANDARD, owner);
            fail("Should have exceeded capacity");
        } catch(ExceedCapacityException exceedCapacityException) {
            // success
        }
        //change to another non ATTENDING type
        engine.getChildBookingService().setBooking(ywd, booking1.getBookingId(), BTIdBits.ABSENT_NOCHARGE, owner);
        assertEquals(BTIdBits.ABSENT_NOCHARGE, booking1.getBookingTypeId());
        // allow exceed capacity
        engine.getChildBookingService().setBooking(ywd, booking1.getBookingId(), BTIdBits.ATTENDING_STANDARD, owner, VABoolean.EXCEED_CAPACITY);

    }
    /**
     * Unit test:
     */
    @Test
    public void unit08_BookingDTOTest() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);
        BookingBean booking = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", requestedYwd, owner);
        ChildBookingDTO dto = ChildBookingTransform.toDTO(booking);
    }

    /**
     * Unit test:
     */
    @Test
    public void unit09_setNoticePeriodRestrictions() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);
        int bookingSd = SDHolder.getSD(20, 54);

        BookingBean b1 = setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD);
        BookingBean b2 = setBooking(ywd, bookingSd, childId21, BTIdBits.ATTENDING_NOCHARGE);

        // control - should move torestricted as in the notice period
        engine.getChildBookingService().setNoticePeriodRestrictions(ywd, requestedYwd, childId11);
        assertEquals(BookingState.RESTRICTED, engine.getChildBookingService().getBookingManager().getObject(ywd, b1.getBookingId()).getState());
        // should stay open as NO_CHARGE_BIT in bookingTypeId
        engine.getChildBookingService().setNoticePeriodRestrictions(ywd, requestedYwd, childId21);
        assertEquals(BookingState.OPEN, engine.getChildBookingService().getBookingManager().getObject(ywd, b2.getBookingId()).getState());
    }
    /*
     *
     */

    @Test
    public void test10_setPreChargeRestrictions() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(2);
        int bookingSd = SDHolder.getSD(20, 54);

        BookingBean b1 = setBooking(ywd, bookingSd, childId11, BTIdBits.ATTENDING_STANDARD);
        BookingBean b2 = setBooking(ywd, bookingSd, childId21, BTIdBits.ATTENDING_NOCHARGE);

        // control - should move torestricted as in the notice period
        engine.getChildBookingService().setInvoiceRestrictions(ywd, accountId1);
        assertEquals(BookingState.RESTRICTED, engine.getChildBookingService().getBookingManager().getObject(ywd, b1.getBookingId()).getState());
    }

    /*
     * provate method to creat a quick BookingBean with automatic ywd
     */
    private BookingBean setBooking(int bookingSd, int childId, int bookingTypeId) throws ObjectStoreException {
        int ywd = CalendarStatic.getRelativeYW(2);
        return setBooking(ywd, bookingSd, childId, contactId1, bookingTypeId);
    }

    /*
     * provate method to creat a quick BookingBean with automatic liability
     */
    private BookingBean setBooking(int ywd, int bookingSd, int childId, int bookingTypeId) throws ObjectStoreException {
        return setBooking(ywd, bookingSd, childId, contactId1, bookingTypeId);
    }

    /*
     * provate method to creat a quick BookingBean
     */
    private BookingBean setBooking(int ywd, int bookingSd, int childId, int liability, int bookingTypeId) throws ObjectStoreException {
        int requestedYwd = CalendarStatic.getRelativeYWD(-1);

        BookingManager bookingManager = new BookingManager(VT.CHILD_BOOKING.manager(), ObjectDataOptionsEnum.PERSIST);
        BookingBean booking = bookingManager.setCreateBooking(ywd, bookingSd, childId, roomId, liability,
                liability, liability, bookingTypeId, ActionBits.ALL_OFF, requestedYwd, "", owner);
        engine.getChildBookingService().reInitialise();
        return booking;
    }

    /*
     * Private method to set up child
     */
    private ChildBean setChild(int accountId, int age) throws Exception {
        int departIn = (7 * 52) - age;

        int dob = CalendarStatic.getRelativeYW(-age);
        int departYwd = CalendarStatic.getRelativeYW(departIn);


        return BSBuilder.setupChild(accountId, dob, departYwd);
    }
}
