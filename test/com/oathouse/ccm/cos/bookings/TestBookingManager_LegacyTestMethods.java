/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.bookings;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.Map;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.valueholder.CalendarHelper;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TestBookingManager_LegacyTestMethods {

    private String owner = "tester";
    private String sep = File.separator;
    private String rootStorePath = "." + sep + "oss" + sep + "data";
    private BookingManager manager;
    // test globals
    private int ywd;
    private int requestedYwd;
    private int room1 = 31;
    private int room2 = 33;
    private int child1 = 1;
    private int child2 = 2;
    private int child3 = 3;
    private int child4 = 4;
    private int child5 = 5;
    private int child6 = 6;
    private int bookingId1;
    private int bookingId2a;
    private int bookingId2b;
    private int bookingId3;
    private int bookingId4;
    private int bookingId5;
    private int bookingId6;

    // NOTE: Constants are defined at the bottom

    @Before
    public void setUp() throws Exception {
        ywd = CalendarStatic.getRelativeYW(4);
        requestedYwd = CalendarStatic.getRelativeYW(0);
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority("man00test");
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        manager = new BookingManager("BookingManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    /**
     * System test:
     */
    @Test
    public void system01_Booking() throws Exception {
        setChildren();
        assertEquals(7, manager.getAllObjects().size());
        assertEquals(7, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        int[] order1 = {1, 6, 5, 2, 3, 4, 2};
        testOrder(order1, manager.getAllObjects(ywd));
        testOrder(order1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));
        // try a reset
        manager = null;
        manager = new BookingManager("BookingManager");
        manager.init();
        assertEquals(7, manager.getAllObjects().size());
        assertEquals(7, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        testOrder(order1, manager.getAllObjects(ywd));
        testOrder(order1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));

    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_Booking() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.bookings.BookingBean", false);
    }

    /**
     * Unit test: To test all the attribute sets
     */
    @Test
    public void unit02_Booking() throws Exception {
        int bookingId = 1;
        setChildren();

        // set BookingPickupId &  ActualPickupId
        assertEquals(92, manager.getObject(ywd, bookingId).getBookingPickupId());
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualPickupId());
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectBookingPickupId(ywd, bookingId, 14, owner);
        manager.setObjectActualPickupId(ywd, bookingId, 22, owner);
        assertEquals(14, manager.getObject(ywd, bookingId).getBookingPickupId());
        assertEquals(22, manager.getObject(ywd, bookingId).getActualPickupId());

        // set ActualStart & ActualEnd
        manager.resetObjectActualStartEnd(ywd, bookingId, owner);
        manager.resetObjectSpanSd(ywd, bookingId, owner);
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualStart());
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualEnd());
        manager.setObjectActualStart(ywd, bookingId, 20, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        assertEquals(20, manager.getObject(ywd, bookingId).getActualStart());
        assertEquals(40, manager.getObject(ywd, bookingId).getActualEnd());

        // set Notes
        assertEquals("notes", manager.getObject(ywd, bookingId).getNotes());
        manager.setObjectNotes(ywd, bookingId, "new Notes", owner);
        assertEquals("new Notes", manager.getObject(ywd, bookingId).getNotes());

        // reset Actuals
        manager.resetObjectActualStartEnd(ywd, bookingId, owner);
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualPickupId());
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualStart());
        assertEquals(-1, manager.getObject(ywd, bookingId).getActualEnd());
    }

    /**
     * Unit test: Test the overridden setObject methods
     */
    @Test
    public void unit03_Booking() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("ywd", Integer.toString(ywd));
        BookingBean b1 = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);
        try {
            manager.setObject(ywd, b1);
            fail("setObject() should now throw an exception");
        } catch(PersistenceException persistenceException) {
            // success
        }
    }

    /**
     * Unit test: Test the getYwdBookings() for a ywd and periodSd restricted by bookingType
     */
    @Test
    public void unit04_getYwdBookings() throws Exception {
        //set all the children
        setChildren();

        // Test getYwdBookings clean
        assertEquals(7, manager.getAllObjects().size());
        assertEquals(7, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        int[] order1 = {1, 6, 5, 2, 3, 4, 2};
        testOrder(order1, manager.getAllObjects(ywd));
        testOrder(order1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));

        // check filter of bookingTypeId
        assertEquals(6, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(3, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.NO_CHARGE_BIT, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF).size());
        int[] order2 = {1, 6, 2, 3, 4, 2};
        testOrder(order2, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.ATTENDING_BIT, ActionBits.TYPE_FILTER_OFF));
        int[] order3 = {5, 2, 2};
        testOrder(order3, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.NO_CHARGE_BIT, ActionBits.TYPE_FILTER_OFF));
        int[] order4 = {5};
        testOrder(order4, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF));

        // check the bookingSD filter
        assertEquals(3, manager.getYwdBookings(ywd, SDHolder.getSD(10, 4), BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        int[] order5 = {1, 6, 5};
        testOrder(order5, manager.getYwdBookings(ywd, SDHolder.getSD(10, 4), BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));
        assertEquals(5, manager.getYwdBookingsForRoom(ywd, SDHolder.getSD(10, 30), room1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        int[] order6 = {1, 6, 5, 2, 4};
        testOrder(order6, manager.getYwdBookingsForRoom(ywd, SDHolder.getSD(10, 30), room1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));
        assertEquals(2, manager.getYwdBookingsForProfile(ywd, SDHolder.getSD(10, 50), child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        int[] order7 = {2, 2};
        testOrder(order7, manager.getYwdBookingsForProfile(ywd, SDHolder.getSD(10, 50), child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF));

        // test the action include
        assertEquals(1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF).size());
        manager.setObjectActionBits(ywd, bookingId1, ActionBits.IMMEDIATE_RECALC_BIT, true, owner);
        assertEquals(1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(1, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTIdBits.TYPE_BOOKING, ActionBits.IMMEDIATE_RECALC_BIT).size());
        assertEquals(0, manager.getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.IMMEDIATE_RECALC_BIT).size());

    }

    /**
     * Unit test: test the set state as a standard flow.
     */
    @Test
    public void unit05_setState() throws Exception {
        setChildren();
        int bookingId = 1;
        // Basic flow through OPEN->RESTRICTED->STARTED->COMPLETED->AUTHORISED->RECONCILED
        manager.setObjectState(ywd, bookingId, BookingState.OPEN, owner);
        assertEquals(BookingState.OPEN, manager.getObject(ywd, bookingId).getState());
        manager.setObjectState(ywd, bookingId, BookingState.RESTRICTED, owner);
        assertEquals(BookingState.RESTRICTED, manager.getObject(ywd, bookingId).getState());
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        assertEquals(BookingState.STARTED, manager.getObject(ywd, bookingId).getState());
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        assertEquals(BookingState.COMPLETED, manager.getObject(ywd, bookingId).getState());
        manager.setObjectState(ywd, bookingId, BookingState.AUTHORISED, owner);
        assertEquals(BookingState.AUTHORISED, manager.getObject(ywd, bookingId).getState());
        manager.setObjectState(ywd, bookingId, BookingState.RECONCILED, owner);
        assertEquals(BookingState.RECONCILED, manager.getObject(ywd, bookingId).getState());

        // Basic flow through OPEN->STARTED
        bookingId = 2;
        assertEquals(BookingState.OPEN, manager.getObject(ywd, bookingId).getState());
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        assertEquals(BookingState.STARTED, manager.getObject(ywd, bookingId).getState());

        // Basic flow through OPEN->COMPLETED
        bookingId = 3;
        assertEquals(BookingState.OPEN, manager.getObject(ywd, bookingId).getState());
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        assertEquals(BookingState.COMPLETED, manager.getObject(ywd, bookingId).getState());

        // Basic Flow from COMPLETED back to RESTRICED (reset)
        manager.resetObjectActualStartEnd(ywd, bookingId, owner);
        assertEquals(BookingState.RESTRICTED, manager.getObject(ywd, bookingId).getState());

        // Basic Flow from COMPLETED back to STARTED (reset)
        bookingId = 4;
        assertEquals(BookingState.OPEN, manager.getObject(ywd, bookingId).getState());
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        assertEquals(BookingState.COMPLETED, manager.getObject(ywd, bookingId).getState());
        manager.resetObjectStateToStarted(ywd, bookingId, owner);
        assertEquals(BookingState.STARTED, manager.getObject(ywd, bookingId).getState());

    }

    /**
     * Unit test: Test the state change exceptions
     */
    @Test
    public void unit06_setStateException() throws Exception {
        setChildren();
        int bookingId = 1;
        // OPEN test
        assertEquals(BookingState.OPEN, manager.getObject(ywd, bookingId).getState());
        try {
            manager.setObjectState(ywd, bookingId, BookingState.AUTHORISED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.RECONCILED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }

        // RESTRICTED test
        manager.setObjectState(ywd, bookingId, BookingState.RESTRICTED, owner);
        assertEquals(BookingState.RESTRICTED, manager.getObject(ywd, bookingId).getState());
        try {
            manager.setObjectState(ywd, bookingId, BookingState.OPEN, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.AUTHORISED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.RECONCILED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }

        // should be able to jump to completed
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        assertEquals(BookingState.COMPLETED, manager.getObject(ywd, bookingId).getState());
        manager.resetObjectActualStartEnd(ywd, bookingId, owner);
        assertEquals(BookingState.RESTRICTED, manager.getObject(ywd, bookingId).getState());
        try {
            manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        manager.setObjectActualStart(ywd, bookingId, 15, owner);
        manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
        manager.setObjectActualEnd(ywd, bookingId, 40, owner);
        manager.setObjectState(ywd, bookingId, BookingState.COMPLETED, owner);
        assertEquals(BookingState.COMPLETED, manager.getObject(ywd, bookingId).getState());
        // can't jump to Locked or back to started or restricted
        try {
            manager.setObjectState(ywd, bookingId, BookingState.RECONCILED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.STARTED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
        try {
            manager.setObjectState(ywd, bookingId, BookingState.RESTRICTED, owner);
            fail("Should have thrown an exception");
        } catch(IllegalActionException iae) { /* success */ }
    }

    /**
     * Unit test: Test setReplaceBooking when there are more than one that override the setObject
     */

    @Test
    public void unit07_setReplaceBooking() throws Exception {
        // constants for help
        int liabilityId = 23;
        int pickupId = 21;
        String notes = "Some Notes";
        setChildren();
        // check starting point
        assertEquals(SDHolder.getSD(20, 60), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        // now check replace
        manager.setReplaceBooking(ywd, SDHolder.getSD(10, 40), child4, room1, liabilityId, -1, pickupId, BTIdBits.WAITING_NOCHARGE, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(10, 40), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 40), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(BTIdBits.WAITING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        //change the state to check from NO_CHARGE to ATTENDING the spanSD is reset
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 40), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(20, 40), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(20, 40), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        //check the spanSd is preserved
        manager.setReplaceBooking(ywd, SDHolder.getSD(10, 10), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(10, 50), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 10), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

        // test the state AUTHORISED changes back to COMPLETED
        manager.setObjectActualStart(ywd, bookingId4, 20, owner);
        manager.setObjectActualEnd(ywd, bookingId4, 80, owner);
        manager.setObjectState(ywd, bookingId4, BookingState.COMPLETED, owner);
        manager.setObjectState(ywd, bookingId4, BookingState.AUTHORISED, owner);
        assertEquals(BookingState.AUTHORISED, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getState());
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 50), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(BookingState.COMPLETED, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getState());

        manager.setObjectState(ywd, bookingId4, BookingState.AUTHORISED, owner);
        manager.setObjectState(ywd, bookingId4, BookingState.RECONCILED, owner);
        try {
            manager.setReplaceBooking(ywd, SDHolder.getSD(5, 20), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalActionException illegalActionException) {
            // success
        }
        // replace two bookings into one
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 40), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(20, 40), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(20, 40), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        // reset so we can test one RESTRICTED
        manager.clear();
        setChildren();
        // check they are reset
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());
        // set the first one restricted
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectBookingTypeId(ywd, bookingId2a, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectBookingTypeId(ywd, bookingId2b, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 35), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 40), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

        // reset so we can test both RESTRICTED
        manager.clear();
        setChildren();
        // set both to restricted
        manager.setObjectBookingTypeId(ywd, bookingId2a, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectBookingTypeId(ywd, bookingId2b, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 35), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 60), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());


        // reset so we can test them linked
        manager.clear();
        setChildren();
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 29), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(20, 29), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());

        // reset so we can test them linked (Restricted)
        manager.clear();
        setChildren();
        // set both to restricted
        manager.setObjectBookingTypeId(ywd, bookingId2a, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectBookingTypeId(ywd, bookingId2b, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 29), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 34), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());

        try {
            manager.setReplaceBooking(ywd, SDHolder.getSD(1, 10), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
            fail("Should have thrown an exception");
        } catch(PersistenceException persistenceException) {
            // success
        }
    }

    /**
     * Unit test: setFitBooking() that override the setObject()
     */
    @Test
    public void unit08_setFitBooking() throws Exception {
        // constants for help
        int liabilityId = 23;
        int pickupId = 21;
        String notes = "Some Notes";
        // set up the children
        setChildren();
        manager.setFitBooking(ywd, SDHolder.getSD(9, 31), child1, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 0), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 30), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());

        // span across two bookings
        manager.setFitBooking(ywd, SDHolder.getSD(14, 62), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(5, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(14, 0), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(15, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(41, 8), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(3).getSpanSd());
        assertEquals(SDHolder.getSD(76, 0), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(4).getSpanSd());

        //check after a booking
        manager.setFitBooking(ywd, SDHolder.getSD(10, 36), child6, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(10, 35), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(46, 0), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());

        // check what happens if we try it inside already existing single
        try {
            manager.setFitBooking(ywd, SDHolder.getSD(20, 60), child4, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(PersistenceException persistenceException) {
            // SUCCESS
        }
        // check integrity
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(20, 60), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());

        // check what happens if we try it inside already existing linked
        try {
            manager.setFitBooking(ywd, SDHolder.getSD(14, 62), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(PersistenceException persistenceException) {
            // SUCCESS
        }
        // check integrity
        assertEquals(5, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(14, 0), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(15, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(41, 8), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(3).getSpanSd());
        assertEquals(SDHolder.getSD(76, 0), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(4).getSpanSd());
    }

    /**
     * Unit test: setLayerBooking() that override the setObject()
     */
    @Test
    public void unit09_setLayerBooking() throws Exception {
        // constants for help
        int liabilityId = 23;
        int pickupId = 21;
        String notes = "Some Notes";
        // set up the children
        setChildren();
        manager.setLayerBooking(ywd, SDHolder.getSD(9, 31), child1, room1, liabilityId, -1, pickupId, BTIdBits.DISCOUNT_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 31), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 30), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());

        // span across two bookings
        manager.setLayerBooking(ywd, SDHolder.getSD(14, 62), child2, room1, liabilityId, -1, pickupId, BTIdBits.PENALTY_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(3, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(14, 62), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(15, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(50, 25), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());

        //check after a booking
        manager.setLayerBooking(ywd, SDHolder.getSD(10, 36), child6, room1, liabilityId, -1, pickupId, BTIdBits.REFUND_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(10, 35), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 36), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());

    }

    /**
     * Unit test: setReplaceBooking() that override the setObject(). this is a separate test to test No_Charge logic
     *
     * 10  15  20  25  30  35  40  45  50  55  60  65  70  75  80
     * ROOMS1 (Test Room)
     *     |-------Child2-------|       |-------Child2------|
     */
    @Test
    public void unit10_setReplaceBooking_NoChargeLogic() throws Exception {
        // constants for help
        int liabilityId = 23;
        int pickupId = 21;
        String notes = "Some Notes";

        // set children (both NO_CHARGE)
        setChildren();
        // set both to restricted
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.buildSD(20, 55), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.buildSD(20, 55), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

        // reset to first attending
        manager.clear();
        setChildren();
        // set both to restricted
        manager.setObjectBookingTypeId(ywd, bookingId2a, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.buildSD(20, 55), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.buildSD(15, 55), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

        // reset to second attending
        manager.clear();
        setChildren();
        // set both to restricted
        manager.setObjectBookingTypeId(ywd, bookingId2b, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.buildSD(20, 55), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.buildSD(20, 75), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

        // reset to both ATTENDING
        manager.clear();
        setChildren();
        // set both to restricted
        manager.setObjectBookingTypeId(ywd, bookingId2a, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectBookingTypeId(ywd, bookingId2b, BTIdBits.ATTENDING_STANDARD, owner);
        manager.setObjectState(ywd, bookingId2a, BookingState.RESTRICTED, owner);
        manager.setObjectState(ywd, bookingId2b, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.buildSD(20, 55), child2, room1, liabilityId, -1, pickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.buildSD(15, 75), manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child2, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());

    }

    /**
     * Unit test: setReplaceBooking() that override the setObject()
     */
    @Test
    public void unit10_setReplaceBooking() throws Exception {
        // constants for help
        int liabilityId = 23;
        int bookingPickupId = 21;
        String notes = "Some Notes";
        // set up the children
        setChildren();
        // check before booking
        manager.setReplaceBooking(ywd, SDHolder.getSD(9, 31), child1, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 31), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(bookingId1, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingId());

        //check after a booking
        manager.setReplaceBooking(ywd, SDHolder.getSD(10, 36), child6, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(10, 36), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(bookingId6, manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingId());

        //span a booking
        manager.setReplaceBooking(ywd, SDHolder.getSD(9, 67), child5, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(1, manager.getYwBookingsForProfile(ywd, child5, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 67), manager.getYwBookingsForProfile(ywd, child5, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(bookingId5, manager.getYwBookingsForProfile(ywd, child5, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingId());

        // now we need to check the spanSd and bookingSd work
        manager.setObjectState(ywd, bookingId4, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.getSD(30, 30), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(20, 60), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(30, 30), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        manager.setReplaceBooking(ywd, SDHolder.getSD(20, 20), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(20, 60), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(20, 20), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        manager.setReplaceBooking(ywd, SDHolder.getSD(10, 40), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(SDHolder.getSD(10, 70), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 40), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
    }

    /**
     * Unit test: setCutBooking() that override the setObject()
     */
    @Test
    public void unit11_setCutBooking() throws Exception {
        // constants for help
        int liabilityId = 23;
        int bookingPickupId = 21;
        boolean immutable = false;
        String notes = "Some Notes";
        // set up the children
        setChildren();
        // check with OPEN state left side cut
        manager.setCutBooking(ywd, SDHolder.getSD(9, 1), child1, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_NOCHARGE, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(9, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(SDHolder.getSD(11, 29), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(11, 29), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_NOCHARGE, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(BTIdBits.CANCELLED_STANDARD, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());
        // change to RESTRICTED right side cut
        manager.setObjectState(ywd, bookingId1, BookingState.RESTRICTED, owner);
        manager.setCutBooking(ywd, SDHolder.getSD(40, 1), child1, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(3, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(11, 28), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(11, 28), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingSd());
        assertEquals(SDHolder.getSD(40, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());
        assertEquals(SDHolder.getSD(40, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getBookingSd());
        //test the exception
        manager.setBookingComplete(ywd, bookingId1, owner);

        try {
            manager.setCutBooking(ywd, SDHolder.getSD(4, 20), child1, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        } catch(PersistenceException persistenceException) {
            // SUCCESS
        }
        // ensure integrity
        assertEquals(3, manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(9, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(11, 28), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(40, 1), manager.getYwBookingsForProfile(ywd, child1, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());

        // check cut inside a span
        try {
            manager.setCutBooking(ywd, SDHolder.getSD(10, 35), child6, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
            fail("Should have thrown an exception");
        } catch(PersistenceException persistenceException) {
            // SUCCESS
        }
        // check cuts covering the span
        try {
            manager.setCutBooking(ywd, SDHolder.getSD(10, 36), child6, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
            fail("Should have thrown an exception");
        } catch(PersistenceException persistenceException) {
            // SUCCESS
        }
        //make a cut in the middle
        manager.setCutBooking(ywd, SDHolder.getSD(11, 33), child6, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(3, manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(10, 0), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(10, 0), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(SDHolder.getSD(11, 33), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(11, 33), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingSd());
        assertEquals(SDHolder.getSD(45, 0), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());
        assertEquals(SDHolder.getSD(45, 0), manager.getYwBookingsForProfile(ywd, child6, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getBookingSd());

        // now test the bookingSd is dealt with properly
        manager.setObjectState(ywd, bookingId4, BookingState.RESTRICTED, owner);
        manager.setReplaceBooking(ywd, SDHolder.getSD(30, 30), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, notes, owner);

        manager.setCutBooking(ywd, SDHolder.getSD(15, 19), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(2, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 19), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(15, 19), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(SDHolder.getSD(35, 45), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(35, 25), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingSd());

        // cut where there is no bookingSd only spanSd
        manager.setCutBooking(ywd, SDHolder.getSD(62, 20), child4, room1, liabilityId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.CANCELLED_STANDARD, 0, requestedYwd, notes, owner);
        assertEquals(3, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).size());
        assertEquals(SDHolder.getSD(15, 19), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getSpanSd());
        assertEquals(SDHolder.getSD(15, 19), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        assertEquals(SDHolder.getSD(35, 26), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getSpanSd());
        assertEquals(SDHolder.getSD(35, 25), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingSd());
        assertEquals(BTIdBits.CANCELLED_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(1).getBookingTypeId());
        assertEquals(SDHolder.getSD(62, 20), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getSpanSd());
        assertEquals(SDHolder.getSD(62, 20), manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getBookingSd());
        assertEquals(BTIdBits.ATTENDING_STANDARD, manager.getYwBookingsForProfile(ywd, child4, BTIdBits.TYPE_BOOKING, ActionBits.TYPE_FILTER_OFF).get(2).getBookingTypeId());
    }

    /**
     * Unit test: getBookingCount()
     */
    @Test
    public void unit12_Count() throws Exception {
        setChildren();
        assertEquals(0, manager.getBookingCount(ywd, room1, SDHolder.getSD(9, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(3, manager.getBookingCount(ywd, room1, SDHolder.getSD(10, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(3, manager.getBookingCount(ywd, room1, SDHolder.getSD(10, 100), BTIdBits.TYPE_BOOKING));
        assertEquals(5, manager.getBookingCount(ywd, room1, SDHolder.getSD(40, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(3, manager.getBookingCount(ywd, room1, SDHolder.getSD(41, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(2, manager.getBookingCount(ywd, room1, SDHolder.getSD(49, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(3, manager.getBookingCount(ywd, room1, SDHolder.getSD(50, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(1, manager.getBookingCount(ywd, room1, SDHolder.getSD(80, 0), BTIdBits.TYPE_BOOKING));
        assertEquals(0, manager.getBookingCount(ywd, room1, SDHolder.getSD(81, 0), BTIdBits.TYPE_BOOKING));

        assertEquals(1, manager.getBookingCount(ywd, room2, SDHolder.getSD(15, 0), BTIdBits.TYPE_BOOKING));
    }

    /**
     * Unit test: tests getYwdRoomOccupancyTrend
     */
    @Test
    public void unit13_getYwdRoomOccupancyTrend() throws Exception {

        // the periodSdSet
        Set<Integer> periodSdSet = new ConcurrentSkipListSet<Integer>();
        periodSdSet.add(SDHolder.getSD(0, 0));
        periodSdSet.add(SDHolder.getSD(5, 0));
        periodSdSet.add(SDHolder.getSD(10, 0));
        periodSdSet.add(SDHolder.getSD(20, 0));
        periodSdSet.add(SDHolder.getSD(30, 0));
        periodSdSet.add(SDHolder.getSD(40, 0));
        periodSdSet.add(SDHolder.getSD(45, 0));
        periodSdSet.add(SDHolder.getSD(50, 0));
        periodSdSet.add(SDHolder.getSD(60, 0));
        periodSdSet.add(SDHolder.getSD(70, 0));
        periodSdSet.add(SDHolder.getSD(80, 0));
        periodSdSet.add(SDHolder.getSD(90, 0));
        periodSdSet.add(SDHolder.getSD(95, 0));

        Map<Integer, Integer> variant = new ConcurrentSkipListMap<Integer, Integer>();

        // try first with no bookings
        assertEquals(variant, manager.getYwdRoomOccupancyVariant(ywd, room1, periodSdSet, BTIdBits.TYPE_BOOKING));

        // set the bookings
        setChildren();
        // set the control variant
        variant.put(SDHolder.getSD(10, 0), 3);
        variant.put(SDHolder.getSD(20, 0), 5);
        variant.put(SDHolder.getSD(45, 0), 3);
        variant.put(SDHolder.getSD(80, 0), 1);
        variant.put(SDHolder.getSD(90, 0), 0);

        assertEquals(variant, manager.getYwdRoomOccupancyVariant(ywd, room1, periodSdSet, BTIdBits.TYPE_BOOKING));
    }

    /**
     * Unit test: getYwdRoomOccupancyTrend
     */
    @Test
    public void unit14_getYwdRoomOccupancyTrend() throws Exception {
        setChildren();
        // the periodSdSet
        Set<Integer> periodSdSet = new ConcurrentSkipListSet<Integer>();
        // zero checks
        int roomCapacity = 0;
        assertEquals(0, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));

        roomCapacity = 10;
        assertEquals(0, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));

        // now populate
        periodSdSet.add(SDHolder.getSD(0, 0));
        assertEquals(0, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));

        periodSdSet.add(SDHolder.getSD(5, 0));
        assertEquals(0, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));

        periodSdSet.add(SDHolder.getSD(10, 0));
        assertEquals(10, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));

        periodSdSet.clear();
        periodSdSet.add(SDHolder.getSD(10, 0)); //3
        periodSdSet.add(SDHolder.getSD(20, 0)); //5
        periodSdSet.add(SDHolder.getSD(30, 0)); //5
        periodSdSet.add(SDHolder.getSD(40, 0)); //5
        periodSdSet.add(SDHolder.getSD(50, 0)); //3
        periodSdSet.add(SDHolder.getSD(60, 0)); //3
        periodSdSet.add(SDHolder.getSD(70, 0)); //3
        periodSdSet.add(SDHolder.getSD(80, 0)); //1
        assertEquals(35, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));
        // change roomCapacity = 9 will result in 38.88 so we can test the rounding
        roomCapacity = 9;
        assertEquals(39, manager.getYwdRoomOccupancyTrend(ywd, room1, roomCapacity, periodSdSet, BTIdBits.TYPE_BOOKING));
    }
    /**
     * Unit test:
     */
    @Test
    public void unit15_setExceptionTest() throws Exception {
        int periodSd = SDHolder.getSD(10, 9);
        int childId = 22;
        int liableContactId = 10;
        int bookingPickupId = 13;
        boolean immutable = false;
        String notes = "notes";

        try {
            manager.setCreateBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.DISCOUNT_STANDARD, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setReplaceBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.PENALTY_STANDARD, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setFitBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.PENALTY_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setReplaceBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.REFUND_STANDARD, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setCutBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, BTIdBits.REFUND_STANDARD, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setCutBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.REFUND_STANDARD, BTBits.UNDEFINED, 0, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
        try {
            manager.setLayerBooking(ywd, periodSd, childId, room1, liableContactId, -1, bookingPickupId, BTIdBits.ATTENDING_STANDARD, 0, BookingState.OPEN, requestedYwd, notes, owner);
            fail("should have thrown an exception");
        } catch(IllegalArgumentException iae) {
            // SUCCESS
        }
    }

    /**
     * Unit test:
     */
    @Test
    public void unit16_suspendedTest() throws Exception {
        int periodSd = SDHolder.getSD(10, 9);
        int childId = 22;
        int liableContactId = 10;
        int bookingPickupId = 13;
        String notes = "notes";
        // create bean
        int[] bookingTypeIds = {
            BTIdBits.ATTENDING_STANDARD,
            BTIdBits.ATTENDING_SPECIAL,
            BTIdBits.SICK_NOCHARGE,
            BTIdBits.WAITING_NOCHARGE,
            BTIdBits.HOLIDAY_SPECIAL,
            BTIdBits.WAITING_SPECIAL,
            BTIdBits.ATTENDING_STANDARD
        };
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            manager.setCreateBooking(ywd + day, periodSd, childId, room1, liableContactId, -1, bookingPickupId, bookingTypeIds[day], 0, requestedYwd, notes, owner);
        }
        // suspend
        manager.setSuspendedBookingsForProfile(childId, owner);

        int[] suspendedTypeIds = {
            BTIdBits.SUSPENDED_STANDARD,
            BTIdBits.SUSPENDED_SPECIAL,
            BTIdBits.SICK_NOCHARGE,
            BTIdBits.WAITING_NOCHARGE,
            BTIdBits.HOLIDAY_SPECIAL,
            BTIdBits.WAITING_SPECIAL,
            BTIdBits.SUSPENDED_STANDARD
        };

        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            assertEquals(suspendedTypeIds[day], manager.getYwdBookingsForProfile(ywd + day, periodSd, childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        }
        // suspend
        manager.setReinstateBookingsForProfile(childId, BTIdBits.ATTENDING_STANDARD, owner);

        int[] reinstateTypeIds = {
            BTIdBits.ATTENDING_STANDARD,
            BTIdBits.ATTENDING_SPECIAL,
            BTIdBits.SICK_NOCHARGE,
            BTIdBits.WAITING_NOCHARGE,
            BTIdBits.HOLIDAY_SPECIAL,
            BTIdBits.WAITING_SPECIAL,
            BTIdBits.ATTENDING_STANDARD
        };

        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            assertEquals(reinstateTypeIds[day], manager.getYwdBookingsForProfile(ywd + day, periodSd, childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).get(0).getBookingTypeId());
        }
    }

    /**
     * Unit test: sets the action flags
     */
    @Test
    public void unit17_setActionFlags() throws Exception {
        setChildren();
        BookingBean booking = manager.getObject(ywd, child1);
        assertEquals(ActionBits.ALL_OFF, booking.getActionBits());
        manager.setObjectActionBits(ywd, booking.getBookingId(), ActionBits.DAY_DISCOUNT_BIT, true, owner);
        booking = manager.getObject(ywd, child1);
        assertEquals(ActionBits.DAY_DISCOUNT_BIT, booking.getActionBits());
        assertTrue(booking.isActionOn(ActionBits.DAY_DISCOUNT_BIT));
        assertFalse(booking.isActionOn(ActionBits.ADHOC_BOOKING_BIT));
        StringBuilder sb = new StringBuilder();
        //System.out.println("TYPE_ALL");
        for(String s : ActionBits.getAllStrings(ActionBits.TYPE_FILTER_OFF)) {
            sb.append(s).append(" ");
            //System.out.println("\t" + s);
        }
        assertEquals(ActionBits.TYPE_FILTER_OFF,ActionBits.getBitFromString(sb.toString()));
        sb = new StringBuilder();
        //System.out.println("TYPE_PREBOOKING");
        for(String s : ActionBits.getAllStrings(ActionBits.TYPE_PREBOOKING)) {
            sb.append(s).append(" ");
            //System.out.println("\t" + s);
        }
        assertEquals(ActionBits.TYPE_PREBOOKING,ActionBits.getBitFromString(sb.toString()));
        sb = new StringBuilder();
        //System.out.println("TYPE_BOOKING");
        for(String s : ActionBits.getAllStrings(ActionBits.TYPE_BOOKING)) {
            sb.append(s).append(" ");
            //System.out.println("\t" + s);
        }
        assertEquals(ActionBits.TYPE_BOOKING,ActionBits.getBitFromString(sb.toString()));
        sb = new StringBuilder();
        //System.out.println("TYPE_POSTBOOKING");
        for(String s : ActionBits.getAllStrings(ActionBits.TYPE_POSTBOOKING)) {
            sb.append(s).append(" ");
            //System.out.println("\t" + s);
        }
        assertEquals(ActionBits.TYPE_POSTBOOKING,ActionBits.getBitFromString(sb.toString()));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit18_StateCompareTests() throws Exception {
        BookingBean booking = new BookingBean(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, BookingState.STARTED, 0, "", owner);

        assertEquals(false, BookingState.isBefore(booking.getState(), BookingState.OPEN));
        assertEquals(false, BookingState.isBefore(booking.getState(), BookingState.RESTRICTED));
        assertEquals(false, BookingState.isBefore(booking.getState(), BookingState.STARTED));
        assertEquals(true, BookingState.isBefore(booking.getState(), BookingState.COMPLETED));
        assertEquals(true, BookingState.isBefore(booking.getState(), BookingState.ARCHIVED));

        assertEquals(false, BookingState.isImmediatelyBefore(booking.getState(), BookingState.OPEN));
        assertEquals(false, BookingState.isImmediatelyBefore(booking.getState(), BookingState.RESTRICTED));
        assertEquals(false, BookingState.isImmediatelyBefore(booking.getState(), BookingState.STARTED));
        assertEquals(true, BookingState.isImmediatelyBefore(booking.getState(), BookingState.COMPLETED));
        assertEquals(false, BookingState.isImmediatelyBefore(booking.getState(), BookingState.ARCHIVED));

        assertEquals(true, BookingState.isEqualOrAfter(booking.getState(), BookingState.OPEN));
        assertEquals(true, BookingState.isEqualOrAfter(booking.getState(), BookingState.RESTRICTED));
        assertEquals(true, BookingState.isEqualOrAfter(booking.getState(), BookingState.STARTED));
        assertEquals(false, BookingState.isEqualOrAfter(booking.getState(), BookingState.COMPLETED));
        assertEquals(false, BookingState.isEqualOrAfter(booking.getState(), BookingState.ARCHIVED));

    }

    /*
     * create 6 bookings in a day for 5 children in 2 rooms
     *
     * 10  15  20  25  30  35  40  45  50  55  60  65  70  75  80
     * ROOMS1 (Test Room)
     *     |-------Child2-------|       |-------Child2------|
     * |-----------Child1-------|
     *         |-------------------Child4-----------------------|
     * |------------Child6----------|
     * |-------------------------Child5---------------------|
     *
     * ROOMS2 (Control Room)
     *     |-------Child3-------|
     */
    private void setChildren() throws Exception {
        // child2
        bookingId2a = manager.setCreateBooking(ywd, SDHolder.getSD(15, 25), child2, room1, 82, -1, 92, BTIdBits.ATTENDING_NOCHARGE, 0, requestedYwd, "notes", owner).getBookingId();
        bookingId2b = manager.setCreateBooking(ywd, SDHolder.getSD(50, 25), child2, room1, 82, -1, 92, BTIdBits.ATTENDING_NOCHARGE, 0, requestedYwd, "notes", owner).getBookingId();
        // child1
        bookingId1 = manager.setCreateBooking(ywd, SDHolder.getSD(10, 30), child1, room1, 81, -1, 91, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, "notes", owner).getBookingId();
        // child4
        bookingId4 = manager.setCreateBooking(ywd, SDHolder.getSD(20, 60), child4, room1, 84, -1, 94, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, "notes", owner).getBookingId();
        // child6
        bookingId6 = manager.setCreateBooking(ywd, SDHolder.getSD(10, 35), child6, room1, 86, -1, 96, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd,  "notes", owner).getBookingId();
        // child5
        bookingId5 = manager.setCreateBooking(ywd, SDHolder.getSD(10, 65), child5, room1, 85, -1, 95, BTIdBits.WAITING_NOCHARGE, 0, requestedYwd,  "notes", owner).getBookingId();
        // CONTROL
        manager.setCreateBooking(ywd, SDHolder.getSD(15, 25), child3, room2, 83, -1, 93, BTIdBits.ATTENDING_STANDARD, 0, requestedYwd, "notes", owner);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        printAll(manager.getAllObjects());
    }

    /*
     * utility private method when debugging to print all objects in a list
     */
    private void printAll(List<BookingBean> list) throws Exception {
        for(BookingBean bean : list) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of the beans in the manager
     */
    private void testOrder(int[] order) throws Exception {
        testOrder(order, manager.getAllObjects());
    }


    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<BookingBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getProfileId());
        }
    }
}