/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

// common imports
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ChildServiceTest_checkChildRemovable {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildService service;

    @Before
    public void setUp() throws Exception {
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
        service = ChildService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void checkChildRemovable_ChildNotDeparted() throws Exception {
        int accountId = 11;
        int childId = 13;
        ChildServiceTest.setChild(accountId, childId, 3, false);
        service.reInitialise();
        try {
            service.checkChildRemovable(childId);
            fail("Should throw IllegalActionException with message 'ChildNotDeparted'");
        } catch(IllegalActionException iae) {
            assertThat(iae.getMessage(), is("ChildNotDeparted"));
        }
    }

    /*
     *
     */
    @Test
    public void checkChildRemovable_bookingsNotAchived() throws Exception {
        final int accountId = 11;
        final int childId = 13;

        ChildServiceTest.setChild(accountId, childId, 3, true);
        service.reInitialise();

        final List<BookingBean> bookingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(1));
        fieldSet.put("state", BookingState.RECONCILED.toString());
        bookingList.add((BookingBean) BeanBuilder.addBeanValues(new BookingBean(), seed++, fieldSet));

        new Expectations() {
            @Cascading private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.getBookingManager().getBookingsFromYwdForProfile(CalendarStatic.getRelativeYW(-8), childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
                result = bookingList;
            }
        };
        try {
            service.checkChildRemovable(childId);
            fail("Should throw IllegalActionException with message 'BookingsNotAchived'");
        } catch(IllegalActionException iae) {
            assertThat(iae.getMessage(), is("BookingsNotAchived"));
        }
    }

    /*
     *
     */
    @Test
    public void checkChildRemovable_archiveBooking() throws Exception {
        final int accountId = 11;
        final int childId = 13;

        ChildServiceTest.setChild(accountId, childId, 3, true);
        service.reInitialise();

        final List<BookingBean> bookingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(1));
        fieldSet.put("state", BookingState.ARCHIVED.toString());
        bookingList.add((BookingBean) BeanBuilder.addBeanValues(new BookingBean(), seed++, fieldSet));

        new Expectations() {
            @Cascading private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.getBookingManager().getBookingsFromYwdForProfile(CalendarStatic.getRelativeYW(-8), childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
                result = bookingList;
            }
        };
        try {
            service.checkChildRemovable(childId);
            // success
        } catch( NoSuchIdentifierException | PersistenceException | IllegalActionException noSuchIdentifierException) {
            fail("Should throw no exceptions");
        }
    }
    /*
     *
     */
    @Test
    public void checkChildRemovable_noBooking() throws Exception {
        final int accountId = 11;
        final int childId = 13;

        ChildServiceTest.setChild(accountId, childId, 3, true);
        service.reInitialise();

        final List<BookingBean> bookingList = new LinkedList<>();

        new Expectations() {
            @Cascading private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.getBookingManager().getBookingsFromYwdForProfile(CalendarStatic.getRelativeYW(-8), childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF);
                result = bookingList;
            }
        };
        try {
            service.checkChildRemovable(childId);
            // success
        } catch( NoSuchIdentifierException | PersistenceException | IllegalActionException noSuchIdentifierException) {
            fail("Should throw no exceptions");
        }
    }
}