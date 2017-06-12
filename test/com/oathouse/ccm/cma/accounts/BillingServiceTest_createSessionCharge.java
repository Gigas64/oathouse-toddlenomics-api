/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
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
public class BillingServiceTest_createSessionCharge {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();


    @Before
    public void setUp() throws Exception {
        //clear out the attributes
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
        service = BillingService.getInstance();
        service.clear();

        final PriceListBean priceList = Builders.getPriceList();
        final SystemPropertiesBean properties = Builders.getProperties();

        new NonStrictExpectations() {
            @Cascading private PriceConfigService priceConfigService;
            @Cascading private PropertiesService propertiesService;
            @Cascading private ChildBookingService bookingService;
            {
                PriceConfigService.getInstance(); returns(priceConfigService);
                PropertiesService.getInstance(); returns(propertiesService);
                ChildBookingService.getInstance(); returns(bookingService);

                priceConfigService.getPriceListForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = priceList;
                propertiesService.getSystemProperties(); returns(properties);
            }
        };
    }

    /*
     * standard session booking no pre billing
     */
    @Test
    public void createSessionCharge_noPreviousBilling() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        int billingBits = BillingEnum.getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING);
        // expected attribute results for full billing
        assertThat(result.getAccountId(), is(accountId));
        assertThat(result.getYwd(), is(booking.getYwd()));
        assertThat(result.getBillingSd(), is(booking.getSpanSd()));
        assertThat(result.getValue(), is(22000L));
        assertThat(result.getTaxRate(), is(0));
        assertThat(result.getBookingId(), is(booking.getBookingId()));
        assertThat(result.getProfileId(), is(booking.getProfileId()));
        assertThat(result.getBtChargeBit(), is(BTBits.STANDARD_CHARGE_BIT));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(billingBits)));
        assertThat(result.getInvoiceId(), is(-1));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(result.getDiscountId(), is(-1));
        assertThat(result.getAdjustmentId(), is(-1));
        assertThat(result.getNotes().isEmpty(), is(true));
        // no billings should be saved
        assertThat(service.getBillingManager().getAllObjects().size(), is(0));

    }

    /*
     * standard session booking and no pre billing booking no charge
     */
    @Test
    public void createSessionCharge_noPreviousBilling_NoCharge() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        attributes.put("bookingTypeId", BTIdBits.CANCELLED_NOCHARGE);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result.getValue(), is(0L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

    /*
     * standard session booking precharge
     */
    @Test
    public void createSessionCharge_precharge() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result.getValue(), is(22000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

    /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_sameSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        BookingBean booking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, booking, owner));

        // precharge again
        booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result, nullValue());
    }

    /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_biggerSd_notImmediate() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);

        new NonStrictExpectations() {
            @Mocked ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = false;
            }
        };
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result, nullValue());
    }

    /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_biggerSd_immediate() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);

        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result.getValue(), is(12000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

    /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_smallerSd_immediate() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);

        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);
        assertThat(result.getValue(), is(12000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CREDIT), is(true));
    }

   /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_thenCharge_sameSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean secondCharge = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(secondCharge.getYwd(), secondCharge.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, secondCharge, owner));

        // now get charge
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        assertThat(result.getValue(), is(0L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

   /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_thenCharge_allBiggerSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean secondCharge = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(secondCharge.getYwd(), secondCharge.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, secondCharge, owner));

        // now get charge
        spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        assertThat(result.getValue(), is(5000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

    /*
     * standard session booking already precharged
     */
    @Test
    public void createSessionCharge_preChargeTwice_thenCharge_smallerSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preCharge = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean secondCharge = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(secondCharge.getYwd(), secondCharge.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, secondCharge, owner));

        // now get charge
        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        assertThat(result.getValue(), is(12000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CREDIT), is(true));
    }

    /*
     * standard session booking precharge
     */
    @Test
    public void createSessionCharge_existingPreCharge_sameSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        BookingBean booking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, booking, owner));

        // charge
        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        assertThat(result.getValue(), is(0L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CHARGE), is(true));
    }

    /*
     * standard session booking precharge
     */
    @Test
    public void createSessionCharge_existingPreCharge_smallerSd() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("spanSd", spanSd);
        BookingBean booking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingSessionCharge(accountId, booking, owner));

        // charge
        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        BillingBean result = service.createBookingSessionCharge(accountId, booking, owner);

        assertThat(result.getValue(), is(12000L));
        assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_SESSION, BILL_CREDIT), is(true));
    }

    //</editor-fold>
}