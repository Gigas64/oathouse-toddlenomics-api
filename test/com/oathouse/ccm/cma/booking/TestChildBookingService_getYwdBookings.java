/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
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
public class TestChildBookingService_getYwdBookings {

    private final int ywd = CalendarStatic.getRelativeYW(0);
    private final String owner = ObjectBean.SYSTEM_OWNED;
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
        service.clear();

    }

    /*
     * requestwaiting listwhen only a single attending booking
     */
    @Test
    public void getYwdBookings_WatingListOnly_withActionFlag() throws Exception {
        sampleBookingA();
        sampleBookingB();
        List<BookingBean> bookingList = service.getBookingManager().getYwdBookings(ywd, BookingManager.ALL_PERIODS, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF);
        assertThat(bookingList.size(), is(1));
        assertThat(bookingList.get(0).getBookingTypeId(), is(BTIdBits.WAITING_STANDARD));
    }

    protected BookingBean sampleBookingA() throws Exception {
        int roomId = 1;
        int bookingSd = 7800179;
        int childId = 1;
        int contactId = 1;
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;
        int actionBits = 16;
        // set up the BookingType
        ObjectSetStore<BookingTypeBean> bookingTypeManager = new ObjectSetStore<>(VT.BOOKING_TYPE.manager(), ObjectDataOptionsEnum.PERSIST);
        bookingTypeManager.setObject(new BookingTypeBean(BTIdBits.ATTENDING_STANDARD, "STANDARD", BTFlagIdBits.STANDARD_FLAGS, 0, owner));
        bookingTypeManager.setObject(new BookingTypeBean(BTIdBits.WAITING_STANDARD, "WAITING", BTFlagIdBits.STANDARD_FLAGS, 0, owner));
        ChildBookingService.getInstance().reInitialise();
        BookingBean booking = service.getBookingManager().setCreateBooking(ywd, bookingSd, childId, roomId, contactId, -1, -1, bookingTypeId, actionBits, ywd, "", owner);
        service.getBookingManager().setObjectState(ywd, booking.getBookingId(), BookingState.RESTRICTED, owner);
        return booking;
    }

    protected BookingBean sampleBookingB() throws Exception {
        int roomId = 1;
        int bookingSd = 7800179;
        int childId = 2;
        int contactId = 2;
        int bookingTypeId = BTIdBits.WAITING_STANDARD;
        int actionBits = 0;
        // set up the BookingType
        ObjectSetStore<BookingTypeBean> bookingTypeManager = new ObjectSetStore<>(VT.BOOKING_TYPE.manager(), ObjectDataOptionsEnum.PERSIST);
        bookingTypeManager.setObject(new BookingTypeBean(BTIdBits.ATTENDING_STANDARD, "STANDARD", BTFlagIdBits.STANDARD_FLAGS, 0, owner));
        bookingTypeManager.setObject(new BookingTypeBean(BTIdBits.WAITING_STANDARD, "WAITING", BTFlagIdBits.STANDARD_FLAGS, 0, owner));
        ChildBookingService.getInstance().reInitialise();
        BookingBean booking = service.getBookingManager().setCreateBooking(ywd, bookingSd, childId, roomId, contactId, -1, -1, bookingTypeId, actionBits, ywd, "", owner);
        service.getBookingManager().setObjectState(ywd, booking.getBookingId(), BookingState.RESTRICTED, owner);
        return booking;
    }


}
