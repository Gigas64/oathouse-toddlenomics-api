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
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceAdjustmentBean;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
import java.util.concurrent.ConcurrentSkipListMap;
// Test Imports
import mockit.*;
import org.apache.commons.collections.CollectionUtils;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingServiceTest_createMethods {
    protected static final int SESSION = getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING);
    protected static final int FUNDED = getBillingBits(TYPE_FUNDED, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
    protected static final int ADJUSTMENT = getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
    protected static final int DISCOUNT = getBillingBits(TYPE_BOOKING_CHILD_DISCOUNT, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_BOOKING);

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;
    private final int accountId = Builders.accountId;

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
        // set the child
        attributes.put("bookingDiscountRate", 1000);
        attributes.put("childDiscountRate", 1000);
        attributes.put("accountDiscountRate", 1000);
        final ChildBean child = Builders.setChild(Builders.childId,attributes);
        attributes.clear();

        final ChildEducationTimetableBean childEducationTimetable = Builders.getChildEdTimetable();
        final PriceListBean priceList = Builders.getPriceList();
        final PriceListBean priceReduction = Builders.getPriceReduction();
        final SystemPropertiesBean properties = Builders.getProperties();

        new NonStrictExpectations() {
            @Mocked private PropertiesService propertiesService;
            @Mocked private TimetableService timetableService;
            @Mocked private PriceConfigService priceConfigService;
            {
                PropertiesService.getInstance(); returns(propertiesService);
                PriceConfigService.getInstance(); returns(priceConfigService);
                TimetableService.getInstance(); returns(timetableService);

                propertiesService.getSystemProperties(); result = properties;
                priceConfigService.getPriceListForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = priceList;
                priceConfigService.getChildEducationPriceReduction(anyInt, anyInt); result = priceReduction;
                timetableService.getChildEducationTimetable(anyInt, anyInt); result = childEducationTimetable;
            }
        };
    }

    /*
     *
     */
    @Test
    public void factoryAllDayTest() throws Exception {
        final int spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        final PriceAdjustmentBean adjustment = Builders.getAdjustment();
        List<BillingBean> resultList = new LinkedList<>();
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        resultList.add(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner));
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            // expected attribute results for full billing
            assertThat(result.getAccountId(), is(accountId));
            assertThat(result.getYwd(), is(booking.getYwd()));
            assertThat(result.getTaxRate(), is(0));
            assertThat(result.getBookingId(), is(booking.getBookingId()));
            assertThat(result.getProfileId(), is(booking.getProfileId()));
            assertThat(result.getBtChargeBit(), is(BTBits.STANDARD_CHARGE_BIT));
            assertThat(result.getInvoiceId(), is(-1));
            assertThat(result.getDiscountId(), is(-1));
            assertThat(result.getNotes().isEmpty(), is(true));
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(39000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(SESSION)));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_RECONCILED));
                assertThat(result.getAdjustmentId(), is(-1));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(20000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(FUNDED)));
                assertThat(result.getDescription(), is(ApplicationConstants.EDUCATION_REDUCTION_RECONCILED));
                assertThat(result.getAdjustmentId(), is(-1));
            } else if(result.hasAnyBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING)) {
                assertThat(result.getBillingSd(), is(adjustment.getRangeSd()));
                assertThat(result.getValue(), is(550L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(ADJUSTMENT)));
                assertThat(result.getDescription(), is(adjustment.getName()));
                assertThat(result.getAdjustmentId(), is(adjustment.getPriceAdjustmentId()));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(TDCalc.getDiscountValue(19550, Builders.discountRate)));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(DISCOUNT)));
                assertThat(result.getDescription(), is(ApplicationConstants.BOOKING_CHILD_DISCOUNT));
                assertThat(result.getAdjustmentId(), is(-1));
            } else {
                fail("Unexpected billing");
            }
        }
        // no billings should be saved
        assertThat(service.getBillingManager().getAllObjects().size(), is(0));
    }

    /*
     *
     */
    @Test
    public void FactoryOneSessionTest() throws Exception {
        final int spanSd = SDHolder.buildSD("9:00", "11:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        final PriceAdjustmentBean adjustment = Builders.getAdjustment();
        List<BillingBean> resultList = new LinkedList<>();
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner), nullValue());
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            // expected attribute results for full billing
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(12000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(SESSION)));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(10000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(FUNDED)));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(TDCalc.getDiscountValue(2000, Builders.discountRate)));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(DISCOUNT)));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryTwoSessionsTest() throws Exception {
        final int spanSd = SDHolder.buildSD("9:00", "15:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.RECONCILED);
        final PriceAdjustmentBean adjustment = Builders.getAdjustment();
        List<BillingBean> resultList = new LinkedList<>();
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner), nullValue());
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            // expected attribute results for full billing
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(29000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(SESSION)));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(20000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(FUNDED)));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(TDCalc.getDiscountValue(9000, Builders.discountRate)));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(DISCOUNT)));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreCharge_Only() throws Exception {
        final int spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        final PriceAdjustmentBean adjustment = Builders.getAdjustment();
        List<BillingBean> resultList = new LinkedList<>();
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        resultList.add(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner));
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(39000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(SESSION)));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(20000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(FUNDED)));
                assertThat(result.getDescription(), is(ApplicationConstants.EDUCATION_REDUCTION_ESTIMATE));
            } else if(result.hasAnyBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING)) {
                assertThat(result.getBillingSd(), is(adjustment.getRangeSd()));
                assertThat(result.getValue(), is(550L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(ADJUSTMENT)));
                assertThat(result.getDescription(), is(adjustment.getName()));
                assertThat(result.getAdjustmentId(), is(adjustment.getPriceAdjustmentId()));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(TDCalc.getDiscountValue(19550, Builders.discountRate)));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(DISCOUNT)));
                assertThat(result.getDescription(), is(ApplicationConstants.BOOKING_CHILD_DISCOUNT));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreChargeTwice() throws Exception {
        int spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preBooking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        PriceAdjustmentBean adjustment = Builders.getAdjustment();
        service.getBillingManager().setObject(accountId, (service.createBookingSessionCharge(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingEducationReduction(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingDiscount(accountId, preBooking, service.getBillingManager().getAllBillingForBooking(accountId, preBooking.getBookingId(), BTIdBits.TYPE_CHARGE), owner)));

        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        List<BillingBean> resultList = new LinkedList<>();
        new NonStrictExpectations() {
            @Mocked ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = false;
            }
        };

        // everything should return null because the estimate has already been generated
        assertThat(service.createBookingSessionCharge(accountId, booking, owner), nullValue());
        assertThat(service.createBookingEducationReduction(accountId, booking, owner), nullValue());
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner), nullValue());
        assertThat(service.createBookingDiscount(accountId, booking, resultList, owner), nullValue());
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreChargeTwice_ImmeditateRefundFlagTrue_spanSdLess() throws Exception {
        int spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preBooking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        PriceAdjustmentBean adjustment = Builders.getAdjustment();
        service.getBillingManager().setObject(accountId, (service.createBookingSessionCharge(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingEducationReduction(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingDiscount(accountId, preBooking, service.getBillingManager().getAllBillingForBooking(accountId, preBooking.getBookingId(), BTIdBits.TYPE_CHARGE), owner)));

        spanSd = SDHolder.buildSD("9:00", "11:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        List<BillingBean> resultList = new LinkedList<>();
        new NonStrictExpectations() {
            @Mocked ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        resultList.add(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner));
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(27000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(10000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.EDUCATION_REDUCTION_ESTIMATE));
            } else if(result.hasAnyBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING)) {
                assertThat(result.getBillingSd(), is(-1));
                assertThat(result.getValue(), is(550L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(adjustment.getName()));
                assertThat(result.getAdjustmentId(), is(adjustment.getPriceAdjustmentId()));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                // previous total was 19550 @ 10% = -1955 discount
                // this total is -17550 @ 10% = +1755 discount
                assertThat(result.getValue(), is(200L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.BOOKING_CHILD_DISCOUNT));
                assertThat(result.getAdjustmentId(), is(-1));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreChargeTwice_ImmeditateRefundFlagTrue_spanSdMore() throws Exception {
        int spanSd = SDHolder.buildSD("9:00", "11:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preBooking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        PriceAdjustmentBean adjustment = Builders.getAdjustment();
        service.getBillingManager().setObject(accountId, (service.createBookingSessionCharge(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingEducationReduction(accountId, preBooking, owner)));
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, preBooking, owner), is(nullValue()));
        service.getBillingManager().setObject(accountId, (service.createBookingDiscount(accountId, preBooking, service.getBillingManager().getAllBillingForBooking(accountId, preBooking.getBookingId(), BTIdBits.TYPE_CHARGE), owner)));

        spanSd = SDHolder.buildSD("8:00", "12:59");
        attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        List<BillingBean> resultList = new LinkedList<>();
        new NonStrictExpectations() {
            @Mocked ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        assertThat(service.createBookingEducationReduction(accountId, booking, owner), nullValue());
        resultList.add(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner));
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(10000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
            } else if(result.hasAnyBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING)) {
                assertThat(result.getBillingSd(), is(adjustment.getRangeSd()));
                assertThat(result.getValue(), is(550L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(adjustment.getName()));
                assertThat(result.getAdjustmentId(), is(adjustment.getPriceAdjustmentId()));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                // previous total was 2000 @ 10% = -200
                // this total is 10550 @ 10% = -1055
                assertThat(result.getValue(), is(1250L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.BOOKING_CHILD_DISCOUNT));
                assertThat(result.getAdjustmentId(), is(-1));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreChargeTwice_ImmeditateRefundFlagTrue_spanSdSame() throws Exception {
        int spanSd = SDHolder.buildSD("9:00", "11:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preBooking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        PriceAdjustmentBean adjustment = Builders.getAdjustment();
        service.getBillingManager().setObject(accountId, (service.createBookingSessionCharge(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingEducationReduction(accountId, preBooking, owner)));
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, preBooking, owner), is(nullValue()));
        service.getBillingManager().setObject(accountId, (service.createBookingDiscount(accountId, preBooking, service.getBillingManager().getAllBillingForBooking(accountId, preBooking.getBookingId(), BTIdBits.TYPE_CHARGE), owner)));

        spanSd = SDHolder.buildSD("9:00", "11:59");
         attributes.put("spanSd", spanSd);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
       List<BillingBean> resultList = new LinkedList<>();
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        assertThat(service.createBookingEducationReduction(accountId, booking, owner), nullValue());
        assertThat(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner), nullValue());
        assertThat(service.createBookingDiscount(accountId, booking, resultList, owner), nullValue());

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(0L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    /*
     *
     */
    @Test
    public void FactoryAllDay_PreChargeTwice_ImmeditateRefundFlagTrue_CancelledNoCharge() throws Exception {
        int spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        final BookingBean preBooking = Builders.getBooking(1, attributes, BookingState.RESTRICTED);
        PriceAdjustmentBean adjustment = Builders.getAdjustment();
        service.getBillingManager().setObject(accountId, (service.createBookingSessionCharge(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingEducationReduction(accountId, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, preBooking, owner)));
        service.getBillingManager().setObject(accountId, (service.createBookingDiscount(accountId, preBooking, service.getBillingManager().getAllBillingForBooking(accountId, preBooking.getBookingId(), BTIdBits.TYPE_CHARGE), owner)));

        spanSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("spanSd", spanSd);
        attributes.put("bookingTypeId", BTIdBits.CANCELLED_NOCHARGE);
        final BookingBean booking = Builders.getBooking(1, attributes, BookingState.COMPLETED);
        List<BillingBean> resultList = new LinkedList<>();
        new NonStrictExpectations() {
            @Mocked ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(booking.getYwd(), booking.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        resultList.add(service.createBookingSessionCharge(accountId, booking, owner));
        resultList.add(service.createBookingEducationReduction(accountId, booking, owner));
        resultList.add(service.createPriceAdjustmentBilling(accountId, adjustment, spanSd, booking, owner));
        resultList.add(service.createBookingDiscount(accountId, booking, resultList, owner));

        for(BillingBean result : resultList) {
            //check there are no nulls
            assertThat(result,notNullValue());
            if(result.hasBillingBits(TYPE_SESSION)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(39000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.SESSION_ESTIMATE));
            } else if(result.hasBillingBits(TYPE_FUNDED)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                assertThat(result.getValue(), is(20000L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.EDUCATION_REDUCTION_ESTIMATE));
            } else if(result.hasAnyBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING)) {
                assertThat(result.getBillingSd(), is(adjustment.getRangeSd()));
                assertThat(result.getValue(), is(550L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CREDIT), is(true));
                assertThat(result.getDescription(), is(adjustment.getName()));
                assertThat(result.getAdjustmentId(), is(adjustment.getPriceAdjustmentId()));
            } else if(result.hasBillingBits(TYPE_BOOKING_CHILD_DISCOUNT)) {
                assertThat(result.getBillingSd(), is(booking.getSpanSd()));
                // previous total was 19550 @ 10% = -1950 discount
                // this total is 0 @ 10% = 0 discount
                assertThat(result.getValue(), is(1950L));
                assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
                assertThat(hasBillingBit(result.getBillingBits(), BILL_CHARGE), is(true));
                assertThat(result.getDescription(), is(ApplicationConstants.BOOKING_CHILD_DISCOUNT));
                assertThat(result.getAdjustmentId(), is(-1));
            } else {
                fail("Unexpected billing");
            }
        }
    }

    protected BillingBean getBillingFromList(List<BillingBean> list, BillingEnum billingEnum) throws Exception {
        for(BillingBean billing : list) {
            if(BillingEnum.hasBillingBit(billing.getBillingId(), billingEnum)) {
                return billing;
            }
        }
        return null;
    }
}