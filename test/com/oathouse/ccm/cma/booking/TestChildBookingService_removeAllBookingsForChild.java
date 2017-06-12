/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
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
public class TestChildBookingService_removeAllBookingsForChild {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingService service;

    private final int childId = 13;
    private final int yw = CalendarStatic.getRelativeYW(0);

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
        service = ChildBookingService.getInstance();
        service.clear();
    }

    /*
     *
     */
    @Test
    public void removeAllBookingsForChild_noBookings() throws Exception {
        List<BookingBean> bookingList = service.removeAllBookingsForChild(childId);
        assertThat(bookingList.isEmpty(), is(true));
    }

    /*
     *
     */
    @Test
    public void removeAllBookingsForChild_bookings() throws Exception {
        setBookings();
        // show the bookings are there
        assertThat(service.getYwdBookings(yw+0, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).size(), is(1));
        assertThat(service.getYwdBookings(yw+1, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).size(), is(2));
        assertThat(service.getYwdBookingsForProfile(yw+1, BookingManager.ALL_PERIODS, childId, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).size(), is(1));
        assertThat(service.getYwdBookings(yw+2, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).size(), is(1));

        List<BookingBean> bookingList = service.removeAllBookingsForChild(childId);
        assertThat(bookingList.size(), is(3));

        assertThat(service.getYwdBookings(yw+0, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).isEmpty(), is(true));
        assertThat(service.getYwdBookings(yw+1, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).size(), is(1));
        assertThat(service.getYwdBookingsForProfile(yw+1, BookingManager.ALL_PERIODS, childId, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).isEmpty(), is(true));
        assertThat(service.getYwdBookings(yw+2, BookingManager.ALL_PERIODS, BTIdBits.TYPE_ALL, BTFlagIdBits.TYPE_ALL).isEmpty(), is(true));
    }


    private void setBookings() throws Exception {
        ObjectMapStore<BookingBean> manager = new ObjectMapStore<>(VT.CHILD_BOOKING.manager(), ObjectDataOptionsEnum.PERSIST, ObjectDataOptionsEnum.ARCHIVE);
        int id = 1;

        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("ywd", Integer.toString(yw+0));
        fieldSet.put("profileId", Integer.toString(childId));
        fieldSet.put("bookingTypeId", Integer.toString(BTIdBits.ATTENDING_STANDARD));
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), fieldSet);
        manager.setObject(booking.getYwd(), booking);

        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("ywd", Integer.toString(yw+1));
        booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), fieldSet);
        manager.setObject(booking.getYwd(), booking);

        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("ywd", Integer.toString(yw+2));
        booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), fieldSet);
        manager.setObject(booking.getYwd(), booking);

        // another child booking
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("ywd", Integer.toString(yw+1));
        fieldSet.put("profileId", Integer.toString(99));
        booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), fieldSet);
        manager.setObject(booking.getYwd(), booking);

        // reinitialise the service so as to include the new additions
        service.reInitialise();
    }

}