/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
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
public class TestChildBookingService_newBookingSameDayAsReconciledBooking {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingService service;

    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Before
    public void setUp() throws Exception {
        //clear out the attributes
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
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
        attributes.put("bookingDiscountRate", 750);
        attributes.put("childDiscountRate", 750);
        attributes.put("accountDiscountRate", 750);
        Builders.setChild(childId, attributes);
    }


    /*
     *
     */
    @Test
    public void newBookingSameDayAsReconciledBooking() throws Exception {
        int bookingSd = SDHolder.buildSD(10, 20);
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        /*
         * Make the booking authorised and ready for reconciliation
         */
        ChildBookingService.getInstance().getBookingManager().setBookingComplete(booking.getYwd(), booking.getBookingId(), owner);
        new NonStrictExpectations() {
            @Mocked private BillingService billingService;
            {
                BillingService.getInstance(); returns(billingService);
            }
        };
        ChildBookingService.getInstance().setBookingAuthorised(booking.getYwd(), booking.getBookingId(), owner);
        List<BookingBean> bookingList = service.getBookingManager().getAllObjects(booking.getYwd());
        assertThat(bookingList.size(), is(1));
        assertThat(bookingList.get(0).getState(), is(BookingState.AUTHORISED));

        /*
         * Make a booking on the same day
         */
        bookingSd = SDHolder.buildSD(30, 50);
        attributes.put("bookingSd", bookingSd);
        BookingBean newBooking = Builders.setBooking(2, attributes, BookingState.OPEN);
        bookingList = service.getBookingManager().getAllObjects(newBooking.getYwd());
        assertThat(bookingList.get(0).getState(), is(BookingState.AUTHORISED));
        assertThat(bookingList.get(1).getState(), is(BookingState.OPEN));
        assertThat(bookingList.size(), is(2));
    }

}
