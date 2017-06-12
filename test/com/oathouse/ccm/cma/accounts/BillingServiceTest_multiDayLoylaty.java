/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.profile.ChildBean;
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
public class BillingServiceTest_multiDayLoylaty {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    // method parameters
    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;
    private final int yw0 = CalendarStatic.getRelativeYW(0);

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Mocked() private PriceConfigService pcsm;
    @Mocked() private PropertiesService psm;

    @Before
    public void setUp() throws Exception {
        //clear out the attributes
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
        // set up the properties
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
        // Global Mock
        attributes.put("bookingDiscountRate", 1000);
        attributes.put("childDiscountRate", 1000);
        attributes.put("accountDiscountRate", 1000);
        Builders.setChild(Builders.childId,attributes);

        final PriceListBean priceList = Builders.getPriceList();
        final List<LoyaltyDiscountBean> loyaltyList = Builders.getLoyaltyWeek();
        final SystemPropertiesBean properties = Builders.getProperties();
        new NonStrictExpectations() {{
            PropertiesService.getInstance(); returns(psm);
            psm.getSystemProperties(); result = properties;
            PriceConfigService.getInstance(); returns(pcsm);
            pcsm.getPriceListForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = priceList;
            pcsm.getAllLoyaltyDiscountForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = loyaltyList;
        }};

    }

    /*
     * Loylaty for a singleday to test the process before multiple
     */
    @Test
    public void  sessionChangeDiscountAfterPreCharge_SingleLoyaltyApplyDiscount() throws Exception {
        // set the booking
        attributes.put("ywd", yw0);
        BookingBean booking = Builders.setBooking(attributes);

        /*
         * Create the invoice check the results
         */
        int invoiceYwd = booking.getYwd();
        InvoiceBean invoice = TransactionService.getInstance().createInvoice(accountId, false, invoiceYwd, invoiceYwd, invoiceYwd, "", owner);
        List<BillingBean> billingList = BillingService.getInstance().getBillingsForInvoice(accountId, invoice.getInvoiceId());

        assertThat(billingList.size(), is(4));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getSignedValue(), is(-400L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getYwd(), is(booking.getYwd()));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getSignedValue(), is(40L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getYwd(), is(booking.getYwd()));

        /*
         * Change the child discount
         */
        attributes.put("bookingDiscountRate", 0);
        attributes.put("childDiscountRate", 0);
        attributes.put("accountDiscountRate", 0);
        Builders.setChild(Builders.childId,attributes);

        /*
         * Make the booking authorised and ready for reconciliation
         */
        ChildBookingService.getInstance().getBookingManager().setBookingComplete(booking.getYwd(), booking.getBookingId(), owner);
        ChildBookingService.getInstance().setBookingAuthorised(booking.getYwd(), booking.getBookingId(), owner);

        /*
         * preview invoice should reimburse the no charge
         */
        invoice = TransactionService.getInstance().createInvoice(accountId, false, invoiceYwd, invoiceYwd, invoiceYwd, "", owner);
        billingList = BillingService.getInstance().getBillingsForInvoice(accountId, invoice.getInvoiceId());

        assertThat(billingList.size(), is(4));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getSignedValue(), is(0L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getYwd(), is(booking.getYwd()));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getSignedValue(), is(-40L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getYwd(), is(booking.getYwd()));
    }

    /*
     * Loylaty each day with discount
     */
    @Test
    public void  sessionChangeDiscountAfterPreCharge_MultipleLoyaltyApplyDiscount() throws Exception {
        /*
         * create the bookings for the week
         */
        // set the booking
        int[] bookingSds = {
            SDHolder.buildSD("08:30", "16:14"),
            SDHolder.buildSD("07:30", "12:44"),
            SDHolder.buildSD("13:15", "17:59"),
            SDHolder.buildSD("07:30", "17:59")
        };
        List<BookingBean> bookingList = new LinkedList<>();
        for(int day = 0; day < 4; day++) {
            attributes.put("ywd", yw0 + day);
            attributes.put("bookingSd", bookingSds[day]);
            bookingList.add(Builders.setBooking(attributes));
        }

        /*
         * Create the invoice check the results
         */
        int invoiceYwd = yw0 + 4;
        InvoiceBean invoice = TransactionService.getInstance().createInvoice(accountId, false, invoiceYwd, invoiceYwd, invoiceYwd, "", owner);
        List<BillingBean> billingList = BillingService.getInstance().getBillingsForInvoice(accountId, invoice.getInvoiceId());
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getSignedValue(), is(-400L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getYwd(), is(yw0 + 3));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getSignedValue(), is(40L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getYwd(), is(yw0 + 3));

        /*
         * Change the child discount
         */
        attributes.put("bookingDiscountRate", 0);
        attributes.put("childDiscountRate", 0);
        attributes.put("accountDiscountRate", 0);
        Builders.setChild(Builders.childId,attributes);

        /*
         * Make the booking authorised and ready for reconciliation
         */
        int ywd = yw0;
        for(BookingBean booking : bookingList) {
            ChildBookingService.getInstance().getBookingManager().setBookingComplete(ywd, booking.getBookingId(), owner);
            ChildBookingService.getInstance().setBookingAuthorised(ywd, booking.getBookingId(), owner);
            ywd++;
        }

        /*
         * preview invoice should reimburse the no charge
         */
        invoice = TransactionService.getInstance().createInvoice(accountId, false, invoiceYwd, invoiceYwd, invoiceYwd, "", owner);
        billingList = BillingService.getInstance().getBillingsForInvoice(accountId, invoice.getInvoiceId());
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getSignedValue(), is(-40L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY_CHILD_DISCOUNT, billingList).get(0).getYwd(), is(yw0 + 3));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).size(), is(1));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getSignedValue(), is(0L));
        assertThat(Builders.getBillingType(TYPE_LOYALTY, billingList).get(0).getYwd(), is(yw0 + 3));
    }

}
