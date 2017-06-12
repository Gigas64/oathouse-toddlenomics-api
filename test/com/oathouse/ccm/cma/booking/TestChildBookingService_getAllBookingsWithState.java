/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.io.File;
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
public class TestChildBookingService_getAllBookingsWithState {
    final private int ywd = CalendarStatic.getRelativeYWD(0);
    private ChildBookingService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        String rootStorePath = "." + sep + "oss" + sep + "data";
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = ChildBookingService.getInstance();

    }

    /*
     *
     */
    @Test
    public void test01_private_setBooking() throws Exception {
        setBookings();
        assertThat(service.getBookingManager().getAllObjects(ywd).size(), is(3));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(0).getState(), is(BookingState.OPEN));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(1).getState(), is(BookingState.RESTRICTED));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(2).getState(), is(BookingState.RECONCILED));

    }

    /*
     *
     */
    @Test
    public void test01_withNoState() throws Exception {
        setBookings();
        assertThat(service.getAllBookingsWithState(BTIdBits.ATTENDING_STANDARD, BTFlagIdBits.TYPE_FLAG).size(), is(3));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(0).getState(), is(BookingState.OPEN));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(1).getState(), is(BookingState.RESTRICTED));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(2).getState(), is(BookingState.RECONCILED));
    }
    /*
     *
     */

    @Test
    public void test01_noStateFound() throws Exception {
        setBookings();
        BookingState[] states = {BookingState.STARTED};
        assertThat(service.getAllBookingsWithState(BTIdBits.ATTENDING_STANDARD, BTFlagIdBits.TYPE_FLAG, states).size(), is(0));
    }

    /*
     *
     */
    @Test
    public void test01_withALLState() throws Exception {
        setBookings();
        BookingState[] states = {BookingState.OPEN, BookingState.RESTRICTED, BookingState.STARTED, BookingState.RECONCILED};
        assertThat(service.getAllBookingsWithState(BTIdBits.ATTENDING_STANDARD, BTFlagIdBits.TYPE_FLAG, states).size(), is(3));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(0).getState(), is(BookingState.OPEN));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(1).getState(), is(BookingState.RESTRICTED));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(2).getState(), is(BookingState.RECONCILED));
    }

    /*
     *
     */
    @Test
    public void test01_oneState() throws Exception {
        setBookings();
        BookingState[] states = {BookingState.STARTED, BookingState.RECONCILED};
        assertThat(service.getAllBookingsWithState(BTIdBits.ATTENDING_STANDARD, BTFlagIdBits.TYPE_FLAG, states).size(), is(1));
        assertThat(service.getBookingManager().getAllObjects(ywd).get(2).getState(), is(BookingState.RECONCILED));
    }

    private void setBookings() throws Exception {
        ObjectMapStore<BookingBean> manager = new ObjectMapStore<>(VT.CHILD_BOOKING.manager(), ObjectDataOptionsEnum.PERSIST, ObjectDataOptionsEnum.ARCHIVE);

        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("bookingTypeId", Integer.toString(BTIdBits.ATTENDING_STANDARD));
        fieldSet.put("state", BookingState.OPEN.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);
        manager.setObject(booking.getYwd(), booking);

        id++;
        fieldSet.put("state", BookingState.RESTRICTED.toString());
        booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);
        manager.setObject(booking.getYwd(), booking);

        id++;
        fieldSet.put("state", BookingState.RECONCILED.toString());
        booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, fieldSet);
        manager.setObject(booking.getYwd(), booking);

        // reinitialise the service so as to include the new additions
        service.reInitialise();
    }


}