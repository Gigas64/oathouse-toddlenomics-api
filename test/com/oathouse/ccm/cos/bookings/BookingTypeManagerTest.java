/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import java.util.List;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BookingTypeManagerTest {

    private String owner;
    private String rootStorePath;
    private BookingTypeManager manager;

    public BookingTypeManagerTest() {
        owner = "tester";
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority("man00test");
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        manager = new BookingTypeManager("BookingTypeManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_BookingType() throws Exception {
        // test manager
        assertEquals(30, manager.getAllIdentifier().size());
        manager = null;
        manager = new BookingTypeManager("BookingTypeManager");
        manager.init();
        assertEquals(30, manager.getAllIdentifier().size());
        System.out.println(manager.getAllObjectsAsString(BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL));
    }

    /**
     * Unit test: test the setObject throws an exception. can only hand set BookingTypeBeans
     */
    @Test
    public void unit01_SetObject() throws Exception {
        try {
            manager.setObject(new BookingTypeBean(1, "name", 1, 0, owner));
            fail("should have thrown an exception");
        } catch(PersistenceException pe) {
            // success
        }
    }

    /**
     * Unit test: Test remove object throws an exception
     */
    @Test
    public void unit02_removeObject() throws Exception {
        try {
            manager.removeObject(1);
            fail("should have thrown an exception");
        } catch(PersistenceException pe) {
            // success
        }
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_BookingType() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.bookings.BookingTypeBean", false);
    }

    /**
     * Unit test: test different scenarios with BTBits
     */
    @Test
    public void unit02_BTBitsTest() throws Exception {
        int control = BTBits.ATTENDING_BIT;
        // now make it WAITING_BIT
        control = BTBits.turnOn(control, BTBits.WAITING_BIT | BTBits.ATTENDING_BIT);
        assertEquals(BTBits.WAITING_BIT|BTBits.ATTENDING_BIT, control);
    }

    /**
     * Unit test: Test the filters work for all scenarios
     */
    @Test
    public void unit03_filterTest() throws Exception {
        assertFalse(BTIdBits.isFilter(BTIdBits.TYPE_ALL, 0));
        assertFalse(BTIdBits.isFilter(BTIdBits.TYPE_ALL, BTBits.UNDEFINED));
        assertFalse(BTIdBits.isFilter(BTIdBits.TYPE_ALL, BTFlagBits.BOOKING_FLAG));
        assertFalse(BTIdBits.isFilter(BTFlagBits.BOOKING_FLAG, BTIdBits.TYPE_ALL));

        int bookingTypeBits = 0;
        int maskBits = 0;
        // test 0
        assertFalse(BTIdBits.isFilter(bookingTypeBits, maskBits));
        maskBits = BTBits.ATTENDING_BIT;
        assertFalse(BTIdBits.isFilter(bookingTypeBits, maskBits));
        bookingTypeBits = BTBits.ATTENDING_BIT;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, maskBits));

        bookingTypeBits += BTBits.WAITING_BIT;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, maskBits));

        maskBits += BTBits.FILTER_MATCH;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, maskBits));

        maskBits += BTBits.WAITING_BIT;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, maskBits));

    }

    /**
     * Unit test: Test specific filter type Holiday no charge
     */
    @Test
    public void unit04_FilterTest() throws Exception {
        // test getting a Holiday  nocharge
        int bookingTypeBits = BTIdBits.HOLIDAY_NOCHARGE;
        int maskBits = BTBits.ATTENDING_BIT | BTBits.NO_CHARGE_BIT;
        assertFalse(BTIdBits.isFilter(bookingTypeBits, maskBits));
        maskBits = BTBits.HOLIDAY_BIT | BTBits.SPECIAL_CHARGE_BIT;
        assertFalse(BTIdBits.isFilter(bookingTypeBits, maskBits));
        maskBits = BTBits.HOLIDAY_BIT | BTBits.NO_CHARGE_BIT;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, maskBits));

        bookingTypeBits = BTIdBits.TYPE_BOOKING;
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.ATTENDING_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.WAITING_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.SICK_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.HOLIDAY_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.ABSENT_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.SUSPENDED_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.CANCELLED_BIT));

        bookingTypeBits = BTBits.turnOff(bookingTypeBits, BTBits.ATTENDING_BIT);

        assertFalse(BTIdBits.isFilter(bookingTypeBits, BTBits.ATTENDING_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.WAITING_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.SICK_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.HOLIDAY_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.ABSENT_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.SUSPENDED_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, BTBits.CANCELLED_BIT));

    }

    /**
     * Unit test: Filter test for Match flag
     */
    @Test
    public void unit05_FilterTest() throws Exception {
        int bookingTypeBits = BTIdBits.ATTENDING_STANDARD;
        assertTrue(BTIdBits.match(bookingTypeBits,BTBits.STANDARD_CHARGE_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits,BTBits.STANDARD_CHARGE_BIT));
        assertTrue(BTIdBits.isFilter(bookingTypeBits,BTBits.STANDARD_CHARGE_BIT | BTBits.FILTER_EQUALS));
        assertFalse(BTIdBits.isFilter(bookingTypeBits,BTBits.STANDARD_CHARGE_BIT | BTBits.SPECIAL_CHARGE_BIT | BTBits.FILTER_EQUALS));
        assertTrue(BTIdBits.isFilter(bookingTypeBits,BTIdBits.ATTENDING_STANDARD | BTBits.FILTER_EQUALS));
        // NOTE the difference between EQUALS and MATCH
        assertFalse(BTIdBits.isFilter(bookingTypeBits, bookingTypeBits | BTBits.WAITING_BIT | BTBits.FILTER_EQUALS));
        assertFalse(BTIdBits.isFilter(bookingTypeBits, bookingTypeBits | BTBits.WAITING_BIT | BTBits.FILTER_MATCH));
        assertTrue(BTIdBits.isFilter(bookingTypeBits, bookingTypeBits | BTBits.WAITING_BIT));
        // now swap them around
        assertFalse(BTIdBits.isFilter(bookingTypeBits | BTBits.WAITING_BIT, bookingTypeBits | BTBits.FILTER_EQUALS));
        assertTrue(BTIdBits.isFilter(bookingTypeBits | BTBits.WAITING_BIT, bookingTypeBits | BTBits.FILTER_MATCH));
        assertTrue(BTIdBits.isFilter(bookingTypeBits | BTBits.WAITING_BIT, bookingTypeBits));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit06_FlagTest() throws Exception {
        int flagBits = BTFlagIdBits.STANDARD_FLAGS;
        assertTrue(BTFlagIdBits.isFlag(flagBits,BTFlagBits.ARCHIVE_FLAG | BTFlagBits.CHARGE_FLAG));
        assertFalse(BTFlagIdBits.isFlag(flagBits,BTFlagBits.ARCHIVE_FLAG | BTFlagBits.REGULAR_BOOKING_FLAG | BTFlagBits.FILTER_MATCH));
        assertTrue(BTFlagIdBits.isFlag(flagBits,BTFlagBits.ARCHIVE_FLAG | BTFlagBits.BOOKING_FLAG));
        assertTrue(BTFlagIdBits.isFlag(flagBits,BTFlagBits.ARCHIVE_FLAG | BTFlagBits.BOOKING_FLAG | BTFlagBits.FILTER_MATCH));
        assertFalse(BTFlagIdBits.isFlag(flagBits,BTFlagBits.ARCHIVE_FLAG | BTFlagBits.BOOKING_FLAG | BTFlagBits.FILTER_EQUALS));
        assertTrue(BTFlagIdBits.isFlag(flagBits,BTFlagIdBits.STANDARD_FLAGS | BTFlagBits.FILTER_MATCH));
    }

    /**
     * Unit test: test the reset of the flags
     */
    @Test
    public void unit07_resetTest() throws Exception {
        BookingTypeBean bean = manager.getObject(BTIdBits.ATTENDING_STANDARD);
        assertTrue(BTFlagIdBits.isFlag(BTFlagIdBits.STANDARD_FLAGS, bean.getFlagBits()));
        manager.setObjectFlagBits(BTIdBits.ATTENDING_STANDARD, BTFlagBits.turnOn(BTFlagIdBits.STANDARD_FLAGS, BTFlagBits.BOOKING_CLOSED_FLAG), true, owner);
        bean = manager.getObject(BTIdBits.ATTENDING_STANDARD);
        assertTrue(BTFlagIdBits.isFlag(BTFlagIdBits.STANDARD_FLAGS | BTFlagBits.BOOKING_CLOSED_FLAG, bean.getFlagBits()));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit08_isVaildTest() throws Exception {
        assertTrue(BTIdBits.isValid(BTIdBits.ATTENDING_STANDARD, BTIdBits.TYPE_IDENTIFIER));
        assertTrue(BTIdBits.isValid(BTIdBits.ATTENDING_STANDARD, BTIdBits.TYPE_PHYSICAL_BOOKING));
        assertTrue(BTIdBits.isValid(BTIdBits.ATTENDING_STANDARD, BTIdBits.TYPE_BOOKING));
        assertFalse(BTIdBits.isValid(BTIdBits.ATTENDING_STANDARD, BTIdBits.TYPE_LAYER));

        assertTrue(BTIdBits.isValid(BTIdBits.WAITING_NOCHARGE, BTIdBits.TYPE_IDENTIFIER));
        assertFalse(BTIdBits.isValid(BTIdBits.WAITING_NOCHARGE, BTIdBits.TYPE_PHYSICAL_BOOKING));
        assertTrue(BTIdBits.isValid(BTIdBits.WAITING_NOCHARGE, BTIdBits.TYPE_BOOKING));
        assertFalse(BTIdBits.isValid(BTIdBits.WAITING_NOCHARGE, BTIdBits.TYPE_LAYER));

        assertTrue(BTIdBits.isValid(BTIdBits.REFUND_STANDARD, BTIdBits.TYPE_IDENTIFIER));
        assertFalse(BTIdBits.isValid(BTIdBits.REFUND_STANDARD, BTIdBits.TYPE_PHYSICAL_BOOKING));
        assertFalse(BTIdBits.isValid(BTIdBits.REFUND_STANDARD, BTIdBits.TYPE_BOOKING));
        assertTrue(BTIdBits.isValid(BTIdBits.REFUND_STANDARD, BTIdBits.TYPE_LAYER));

        assertFalse(BTIdBits.isValid(BTBits.ATTENDING_BIT, BTIdBits.TYPE_IDENTIFIER));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit09_getAllObjectsTest() throws Exception {
        assertEquals(3,manager.getAllObjects(BTBits.HOLIDAY_BIT, BTFlagIdBits.TYPE_FLAG).size());
        assertEquals(12,manager.getAllObjects(BTIdBits.TYPE_IDENTIFIER, BTFlagBits.PREBOOKING_FLAG).size());

        manager.setObjectFlagBits(BTIdBits.ATTENDING_SPECIAL, BTFlagBits. BOOKING_CLOSED_FLAG, true, owner);
        int requiredFlags = BTFlagBits.FILTER_MATCH | BTFlagBits.PREBOOKING_FLAG;
        // add anotherflag
        requiredFlags |= BTFlagBits. BOOKING_CLOSED_FLAG;

        List<BookingTypeBean> types = manager.getAllObjects(BTIdBits.TYPE_BOOKING, requiredFlags);
        assertEquals(1, types.size());
        assertEquals(BTIdBits.ATTENDING_SPECIAL, types.get(0).getBookingTypeId());
    }

    /**
     * Unit test: test the isOn and isOff methods in BookingTypeBean
     */
    @Test
    public void unit10_isFlagOnOff() throws Exception {
        assertTrue(manager.getObject(BTIdBits.ATTENDING_STANDARD).isFlagOn(BTFlagBits.BOOKING_FLAG));
        assertFalse(manager.getObject(BTIdBits.ATTENDING_STANDARD).isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG));
    }


    /**
     * Unit test:
     */
    @Test
    public void unit11_getBitsTest() throws Exception {
        assertEquals("ATTENDING_BIT",BTBits.getAllStrings(BTBits.ATTENDING_BIT).get(0));
        assertEquals(BTBits.ATTENDING_BIT, BTBits.getBit("ATTENDING_BIT"));
        assertEquals(BTBits.ALL_OFF, BTBits.getBit("SOME_BIT"));
    }
    /**
     * Unit test:
     */
    @Test
    public void unit12_setIdentifier_setCharge() throws Exception {
        assertEquals(BTIdBits.ATTENDING_NOCHARGE,BTIdBits.setIdentifier(BTIdBits.CANCELLED_NOCHARGE, BTBits.ATTENDING_BIT));
        assertEquals(BTIdBits.ATTENDING_NOCHARGE,BTIdBits.setCharge(BTIdBits.ATTENDING_SPECIAL, BTBits.NO_CHARGE_BIT));
        assertEquals(BTIdBits.CANCELLED_NOCHARGE,BTIdBits.setIdentifier(BTIdBits.CANCELLED_NOCHARGE, BTBits.NO_CHARGE_BIT));
        assertEquals(BTIdBits.ATTENDING_SPECIAL,BTIdBits.setCharge(BTIdBits.ATTENDING_SPECIAL, BTBits.ATTENDING_BIT));
    }
    /**
     * Unit test:
     */
    @Test
    public void unit13_sandboxBitTests() throws Exception {
        int bit = BTIdBits.turnOff(BTIdBits.ATTENDING_NOCHARGE, BTIdBits.TYPE_CHARGE | BTIdBits.TYPE_LAYER);
        System.out.println(BTIdBits.ATTENDING_NOCHARGE);
        System.out.println("Bits = ["+ bit + "] " + BTBits.getAllStrings(bit));
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(BookingTypeBean bean : manager.getAllObjects()) {
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
    private void testOrder(int[] order, List<BookingTypeBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}
