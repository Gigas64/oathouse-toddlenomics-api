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
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.valueholder.CalendarHelper;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
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
public class BillingServiceTest_calculateLoyaltyDiscountSuccessCriteria {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;

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
     * Priority days does not match the booking ywd
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_OneLoyaltyBookingWrongDay() throws Exception {
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.put("priorityDays", BeanBuilder.SAT_ONLY);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(-1L));
    }

    /*
     * a single day loyalty where the booking matches and criteria met
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_singleLoyaltyBookingCriteriaMet() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(39000L));
    }

    /*
     * a single day loyalty where the booking matches and criteria met
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_SingleLoyaltyBookingCritiriaNotMet() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "15:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation();
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(-1L));
    }

    /*
     * Met criteria child funded and include funded true
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_metCriteriaChildEdTrueInclude() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation(true, true);
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(39000L));
    }

    /*
     * Met criteria child funded and include funded false
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_metCriteriaChildEdFalseInclude() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation(true, false);
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(-1L));
    }

    /*
     * Met criteria no child funded and include funded true
     */
    @Test
    public void calculateLoyaltyDiscountSuccessCriteria_metCriteriaNoChildEdTrueInclude() throws Exception {
        int bookingSd = SDHolder.buildSD("08:00", "16:59");
        int duration = Builders.getSdDuration("08:00", "16:59");
        attributes.put("ywd", CalendarStatic.getRelativeYW(0));
        attributes.put("bookingSd", bookingSd);
        final BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        attributes.clear();
        attributes.put("priorityDays", BeanBuilder.MON_ONLY);
        attributes.put("duration", duration);
        attributes.put("value", 10000);
        final LoyaltyDiscountBean loyalty = Builders.getLoyalty(1, attributes);
        mockExpectation(false, true);
        long result = service.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        assertThat(result, is(39000L));
    }

    private void mockExpectation() throws Exception {
        mockExpectation(false, false);
    }

    private void mockExpectation(final boolean isEdTimetable, final boolean isLoyaltyAppliedToEducationDays) throws Exception {
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
        if(isLoyaltyAppliedToEducationDays) { attributes.put("loyaltyApplyedToEducationDays", 0); }
        final SystemPropertiesBean properties = Builders.getProperties(attributes);
        final PriceListBean priceList = Builders.getPriceList();
        new NonStrictExpectations() {{
            PropertiesService.getInstance(); returns(psm);
            psm.getSystemProperties(); result = properties;
            pcsm.getPriceListForProfile(anyInt, anyInt, anyInt, MultiRefEnum.PRICE_STANDARD); result = priceList;
            csm.isDayChildEducationTimetable(childId, anyInt); result = isEdTimetable;
         }};
    }
}
