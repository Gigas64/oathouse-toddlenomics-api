/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingManagerTest;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarHelper;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
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
public class BillingServiceTest_calculateLoyaltyDiscountDayBookingBillingTotals {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;
    private final int ywd = CalendarStatic.getRelativeYW(0);

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Mocked private PriceConfigService pcsm;
    @Mocked private PropertiesService psm;

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
        service = BillingService.getInstance();
        service.clear();
        // clear out the child
        ChildService.getInstance().clear();
        // clear any bookings
        ChildBookingService.getInstance().clear();
        // set the child
        attributes.put("bookingDiscountRate", 750);
        attributes.put("childDiscountRate", 750);
        attributes.put("accountDiscountRate", 750);
        Builders.setChild(childId, attributes);

        final PriceListBean priceList = Builders.getPriceList();
        final SystemPropertiesBean properties = Builders.getProperties();
        new NonStrictExpectations() {{
            PropertiesService.getInstance(); returns(psm);
            PriceConfigService.getInstance(); returns(pcsm);

            pcsm.getPriceListForProfile(anyInt, anyInt, anyInt, MultiRefEnum.PRICE_STANDARD); result = priceList;
            psm.getSystemProperties(); result = properties;
         }};
    }

    /*
     * no bookings in the booking mamager, the values passed are not representative
     */
    @Test
    public void calculateLoyaltyDiscountDayBookingBillingTotals_NoBooking() throws Exception {
        int bookingSd = SDHolder.buildSD("09:00", "15:59");
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.getBooking(1, attributes);
        long[] results = service.calculateLoyaltyDiscountDayBookingBillingTotals(accountId, ywd, booking);
        assertThat(results[0], is(-1L)); // start
        assertThat(results[1], is(0L)); // duration sum
        assertThat(results[2], is(0L));
    }

    /*
     * Single booking
     */
    @Test
    public void calculateLoyaltyDiscountDayBookingBillingTotals_oneBooking() throws Exception {
        // two bookings on the day
        int bookingSd = SDHolder.buildSD("09:00", "15:59");
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes);
        long[] results = service.calculateLoyaltyDiscountDayBookingBillingTotals(accountId, ywd, booking);

        long start = Builders.getSdStart("09:00");
        long duration = Builders.getSdDuration("09:00", "15:59");
        assertThat(results[0], is(start)); // start
        assertThat(results[1], is(duration)); // duration sum
        assertThat(results[2], is(29000L));
    }


    /*
     * Two bookings to check the second is the trigger.
     */
    @Test
    public void calculateLoyaltyDiscountDayBookingBillingTotals_twoBooking() throws Exception {
        // two bookings on the day
        int bookingSd = SDHolder.buildSD("08:00", "11:59");
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes);

        bookingSd = SDHolder.buildSD("13:00", "16:59");
        attributes.put("bookingSd", bookingSd);
        final BookingBean trigger = Builders.setBooking(2, attributes);
        // only use the first booking as the reference
        long[] results = service.calculateLoyaltyDiscountDayBookingBillingTotals(accountId, ywd, booking);
        // this should fail as is not the trigger
        assertThat(results[0], is(-1L)); // start
        assertThat(results[1], is(0L)); // duration sum
        assertThat(results[2], is(0L));

        // now use the propper trigger billing
        results = service.calculateLoyaltyDiscountDayBookingBillingTotals(accountId, ywd, trigger);
        long start = Builders.getSdStart("08:00");
        long duration = Builders.getSdDuration("08:00", "11:59") + Builders.getSdDuration("13:00", "16:59");
        assertThat(results[0], is(start)); // start
        assertThat(results[1], is(duration)); // duration sum
        assertThat(results[2], is(34000L));

    }

}
