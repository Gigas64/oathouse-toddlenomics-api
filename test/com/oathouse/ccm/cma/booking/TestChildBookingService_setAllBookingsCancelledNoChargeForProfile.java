/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
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
public class TestChildBookingService_setAllBookingsCancelledNoChargeForProfile {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingService service;

    int childId = Builders.childId;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Before
    public void setUp() throws Exception {
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
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
    public void setAllBookingsCancelledNoChargeForProfile_NoBookings() throws Exception {
        List<BookingBean> results = service.setAllBookingsCancelledNoChargeForProfile(childId, owner);
        assertThat(results.isEmpty(), is(true));
    }

    /*
     *
     */
    @Test
    public void setAllBookingsCancelledNoChargeForProfile_SingleBooking() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(1);
        int bookingSd = SDHolder.buildSD("9:00", "13:00");
        attributes.put("bookingSd", bookingSd);
        attributes.put("ywd", ywd);
        Builders.setBooking(1,attributes);
        service.reInitialise();
        List<BookingBean> results = service.getBookingManager().getAllObjects();
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));
        results = service.setAllBookingsCancelledNoChargeForProfile(childId, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.CANCELLED_NOCHARGE));
    }

    /*
     *
     */
    @Test
    public void setAllBookingsCancelledNoChargeForProfile_MultiBooking() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(1);
        int bookingSd = SDHolder.buildSD("9:00", "13:00");
        attributes.put("bookingSd", bookingSd);
        attributes.put("ywd", ywd);
        Builders.setBooking(1,attributes);
        attributes.put("ywd", ywd+1);
        Builders.setBooking(2,attributes);
        service.reInitialise();
        List<BookingBean> results = service.getBookingManager().getAllObjects();
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));
        assertThat(results.get(1).getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));
        results = service.setAllBookingsCancelledNoChargeForProfile(childId, owner);
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.CANCELLED_NOCHARGE));
        assertThat(results.get(1).getBookingTypeId(), is(BTIdBits.CANCELLED_NOCHARGE));
    }

    /*
     *
     */
    @Test
    public void setAllBookingsCancelledNoChargeForProfile_BookingAuthorised() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(1);
        int bookingSd = SDHolder.buildSD("9:00", "13:00");
        attributes.put("bookingSd", bookingSd);
        attributes.put("ywd", ywd);
        BookingBean booking = Builders.setBooking(1,attributes);
        attributes.put("ywd", ywd+1);
        Builders.setBooking(2,attributes);
        service.reInitialise();
        service.getBookingManager().setBookingComplete(ywd, booking.getBookingId(), owner);
        service.getBookingManager().setObjectState(ywd, booking.getBookingId(), BookingState.AUTHORISED, owner);
        List<BookingBean> results = service.setAllBookingsCancelledNoChargeForProfile(childId, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.CANCELLED_NOCHARGE));
        results = service.getBookingManager().getAllObjects();
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));
        assertThat(results.get(1).getBookingTypeId(), is(BTIdBits.CANCELLED_NOCHARGE));

    }



}