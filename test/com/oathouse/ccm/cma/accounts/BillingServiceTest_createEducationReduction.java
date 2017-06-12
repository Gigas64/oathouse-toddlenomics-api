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
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
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
public class BillingServiceTest_createEducationReduction {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

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
        service = BillingService.getInstance();
        service.clear();

        final ChildEducationTimetableBean childEducationTimetable = Builders.getChildEdTimetable();
        final PriceListBean priceList = Builders.getPriceReduction();
        final SystemPropertiesBean properties = Builders.getProperties();

        new NonStrictExpectations() {
            @Cascading private TimetableService timetableService;
            @Cascading private PriceConfigService priceConfigService;
            @Cascading private PropertiesService propertiesService;
            {
                PriceConfigService.getInstance(); returns(priceConfigService);
                TimetableService.getInstance(); returns(timetableService);
                PropertiesService.getInstance(); returns(propertiesService);

                priceConfigService.getChildEducationPriceReduction(anyInt, anyInt); result = priceList;
                timetableService.getChildEducationTimetable(anyInt, anyInt); result = childEducationTimetable;
                propertiesService.getSystemProperties(); returns(properties);
            }
        };
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_OneSession() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("9:00", "11:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);
        // NOTE: this is APPLY_DISCOUNT so that child education reverses out any SESSION discounts.
        int billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        // expected attribute results for full billing
        assertThat(result.getAccountId(), is(accountId));
        assertThat(result.getYwd(), is(booking.getYwd()));
        assertThat(result.getBillingSd(), is(booking.getSpanSd()));
        assertThat(result.getValue(), is(10000L));
        assertThat(result.getTaxRate(), is(0));
        assertThat(result.getBookingId(), is(booking.getBookingId()));
        assertThat(result.getProfileId(), is(booking.getProfileId()));
        assertThat(result.getBtChargeBit(), is(BTBits.STANDARD_CHARGE_BIT));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(getAllStrings(result.getBillingBits()), is(getAllStrings(billingBits)));
        assertThat(result.getInvoiceId(), is(-1));
        assertThat(result.getDescription(), is(ApplicationConstants.EDUCATION_REDUCTION_RECONCILED));
        assertThat(result.getDiscountId(), is(-1));
        assertThat(result.getAdjustmentId(), is(-1));
        assertThat(result.getNotes().isEmpty(), is(true));
        // no billings should be saved
        assertThat(service.getBillingManager().getAllObjects().size(), is(0));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_OneSession_Lunch() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("9:00", "12:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        // expected attribute results for full billing
        assertThat(result.getValue(), is(10000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CREDIT), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_TwoSession() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("9:00", "15:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        // expected attribute results for full billing
        assertThat(result.getValue(), is(20000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CREDIT), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_AllDay() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("8:00", "16:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        // expected attribute results for full billing
        assertThat(result.getValue(), is(20000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CREDIT), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_existingPrecharge() throws Exception {
        final int accountId = 11;
        final int spanSd = SDHolder.buildSD("8:00", "16:59");
        final BookingBean precharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, precharge, owner));

        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);
        assertThat(result, nullValue());
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_existingPrecharge_largerSd() throws Exception {
        final int accountId = 11;
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        final BookingBean precharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, precharge, owner));

        spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        assertThat(result.getValue(), is(10000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CREDIT), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_existingPrecharge_smallSd() throws Exception {
        final int accountId = 11;
        int spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean precharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, precharge, owner));

        spanSd = SDHolder.buildSD("8:00", "12:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        assertThat(result.getValue(), is(10000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CHARGE), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_existingPrecharge_cancelled() throws Exception {
        final int accountId = 11;
        int spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean precharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, precharge, owner));

        spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.CANCELLED_NOCHARGE, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        assertThat(result.getValue(), is(20000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CHARGE), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_prechargeTwice_chargeDiff() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        BookingBean preCharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean secondCharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(secondCharge.getYwd(), secondCharge.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, secondCharge, owner));

        spanSd = SDHolder.buildSD("8:00", "12:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        assertThat(result.getValue(), is(10000L));
        assertThat(BillingEnum.isValid(result.getBillingBits()), is(true));
        assertThat(hasBillingBit(result.getBillingBits(), TYPE_FUNDED, BILL_CHARGE), is(true));
    }

    /*
     *
     */
    @Test
    public void createEducationReduction_prechargeTwice_chargeSame() throws Exception {
        final int accountId = 11;
        // precharge
        int spanSd = SDHolder.buildSD("8:00", "12:59");
        BookingBean preCharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RESTRICTED);
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, preCharge, owner));

        // precharge again
        spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean secondCharge = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.COMPLETED);
        new NonStrictExpectations() {
            @Mocked private ChildBookingService bookingService;
            {
                ChildBookingService.getInstance(); returns(bookingService);
                bookingService.isBookingTypeFlagOn(secondCharge.getYwd(), secondCharge.getBookingId(), BTFlagBits.IMMEDIATE_RECALC_FLAG); result = true;
            }
        };
        service.getBillingManager().setObject(accountId, service.createBookingEducationReduction(accountId, secondCharge, owner));

        spanSd = SDHolder.buildSD("8:00", "15:59");
        final BookingBean booking = setBookingMock(1, BTIdBits.ATTENDING_STANDARD, spanSd, BookingState.RECONCILED);
        BillingBean result = service.createBookingEducationReduction(accountId, booking, owner);

        assertThat(result, nullValue());
    }

    //<editor-fold defaultstate="collapsed" desc="private helper methods">

    private BookingBean setBookingMock(int bookingId, int bookingTypeId, int spanSd, BookingState state) throws Exception {
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("id", Integer.toString(bookingId));
        fieldMap.put("ywd", Integer.toString(CalendarStatic.getRelativeYWD(0)));
        fieldMap.put("spanSd", Integer.toString(spanSd));
        fieldMap.put("actualStart", Integer.toString(SDHolder.getStart(spanSd)));
        fieldMap.put("actualEnd", Integer.toString(SDHolder.getEnd(spanSd)));
        fieldMap.put("bookingTypeId", Integer.toString(bookingTypeId));
        fieldMap.put("state", state.toString());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), fieldMap);
        return booking;
    }

    //</editor-fold>

}