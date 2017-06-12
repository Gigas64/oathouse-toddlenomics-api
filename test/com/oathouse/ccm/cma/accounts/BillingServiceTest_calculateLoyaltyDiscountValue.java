/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
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
public class BillingServiceTest_calculateLoyaltyDiscountValue {

    private final int yw0 = CalendarStatic.getRelativeYW(0);
    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Mocked({"isDayChildEducationTimetable"}) private ChildService csm;
    @Mocked({"getPriceListForProfile"}) private PriceConfigService pcsm;
    @Mocked private PropertiesService psm;

    @Before
    public void setUp() throws Exception {
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
    }

    /*
     *
     */
    @Test(expected = IllegalValueException.class)
    public void calculateLoyaltyDiscountValue_rejectStateArchive() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.ARCHIVED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        service.calculateLoyaltyDiscountValue(accountId, loyalty, booking);
    }

    /*
     * test the calc works
     */
    @Test
    public void calculateLoyaltyDiscountValue_CalcAsValue() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        long[] result = service.calculateLoyaltyDiscountValue(accountId, loyalty, booking);
        assertThat(result[0], is(10000L * -1));
    }

    /*
     * test the calc works
     */
    @Test
    public void calculateLoyaltyDiscountValue_CalcAsPercentage() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        long result[] = service.calculateLoyaltyDiscountValue(accountId, loyalty, booking);
        assertThat(result[0], is(TDCalc.getDiscountValue(10000, (int) loyalty.getDiscount()) * -1));
    }

    //<editor-fold defaultstate="collapsed" desc="helper methods">
    private void mockExpectation() throws Exception {
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
        final SystemPropertiesBean properties = Builders.getProperties(attributes);
        final PriceListBean priceList = Builders.getPriceList();
        new NonStrictExpectations() {{
            PropertiesService.getInstance(); returns(psm);
            psm.getSystemProperties(); result = properties;
            pcsm.getPriceListForProfile(anyInt, anyInt, anyInt, MultiRefEnum.PRICE_STANDARD); result = priceList;
            csm.isDayChildEducationTimetable(childId, anyInt); result = false;
         }};
    }

    //</editor-fold>

}
