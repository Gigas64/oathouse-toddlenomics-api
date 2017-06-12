/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
import java.util.concurrent.ConcurrentSkipListMap;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingService_setInvoiceRestrictions {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingService service;

    final int accountId = Builders.accountId;
    final int childId = Builders.childId;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Before
    public void setUp() throws Exception {
        //clear out the attributes
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
        // set up the properties
        String authority = ObjectBean.SYSTEM_OWNED;
        String rootStorePath = Paths.get("./oss/data").toString();
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(Paths.get(rootStorePath + "/conf/oss_log4j.properties").toString());
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = ChildBookingService.getInstance();
        service.clear();

        Builders.setChild(childId);

    }

    /*
     * standard OPEN bookings are selected and state changed to RESTRICTED
     * Also check nothing happens when the lastYwd is before the booking
     */
    @Test
    public void setInvoiceRestrictions_LastYwd() throws Exception {
        final int yw0 = CalendarStatic.getRelativeYW(0);
        final int lastYwd = yw0 + 2;
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        for(int days = 0; days < 5; days++) {
            int ywd = yw0 + days;
            attributes.put("ywd", ywd);
            Builders.setBooking(attributes, BookingState.OPEN);

        }
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(3));
        for(int index = 0; index < results.size(); index++) {
            assertThat(results.get(index).getState(), is(BookingState.RESTRICTED));
        }
    }

    /*
     * Booking is ARCHIVED so not included
     */
    @Test
    public void setInvoiceRestrictions_Archive() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        Builders.setBooking(attributes, BookingState.ARCHIVED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(0));
    }

    /*
     * Booking is AUTHORISED and has been PRE_BILLED
     */
    @Test
    public void setInvoiceRestrictions_AuthorisedPrebilled() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        attributes.put("actionBits", ActionBits.BOOKING_PRE_BILLED_BIT);
        Builders.setBooking(attributes, BookingState.AUTHORISED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(1));
    }

    /*
     * Booking is COMPLETED and has been PRE_BILLED
     */
    @Test
    public void setInvoiceRestrictions_CompletedPrebilled() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        attributes.put("actionBits", ActionBits.BOOKING_PRE_BILLED_BIT);
        Builders.setBooking(attributes, BookingState.COMPLETED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(0));
    }

    /*
     * Booking is COMPLETED and has been PRE_BILLED, Action IMMEDIATE_RECALC
     */
    @Test
    public void setInvoiceRestrictions_CompletedPrebilledActionRecalc() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        attributes.put("actionBits", ActionBits.BOOKING_PRE_BILLED_BIT | ActionBits.IMMEDIATE_RECALC_BIT);
        Builders.setBooking(attributes, BookingState.COMPLETED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(1));
    }

    /*
     * Booking is COMPLETED and has been PRE_BILLED, bookingType IMMEDIATE_RECALC
     */
    @Test
    public void setInvoiceRestrictions_CompletedPrebilledBookingTypeRecalc() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        attributes.put("actionBits", ActionBits.BOOKING_PRE_BILLED_BIT);
        attributes.put("flagBits", BTFlagBits.turnOn(BTFlagIdBits.STANDARD_FLAGS, BTFlagBits.IMMEDIATE_RECALC_FLAG));
        Builders.setBooking(attributes, BookingState.COMPLETED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(1));
    }

    /*
     * Booking is COMPLETED and has been PRE_BILLED, Action IMMEDIATE_RECALC
     */
    @Test
    public void setInvoiceRestrictions_CompletedPrebilledActionRecalcNoPrecharge() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final int lastYwd = CalendarStatic.getRelativeYW(1);
        attributes.put("profileId", childId);
        attributes.put("actionBits", 0);
        attributes.put("ywd", ywd);
        attributes.put("actionBits", ActionBits.BOOKING_PRE_BILLED_BIT | ActionBits.IMMEDIATE_RECALC_BIT);
        attributes.put("flagBits", BTFlagBits.turnOff(BTFlagIdBits.STANDARD_FLAGS, BTFlagBits.PRECHARGE_FLAG));
        Builders.setBooking(attributes, BookingState.COMPLETED);
        // test
        List<BookingBean> results = service.setInvoiceRestrictions(lastYwd, accountId);
        assertThat(results.size(), is(0));
    }


}