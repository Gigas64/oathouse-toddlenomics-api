/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TransactionServiceTest_isInvoiceRequired {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;

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
        service = TransactionService.getInstance();
        service.clear();

    }

    /*
     * return an empty List of BillingBean so should return false
     */
    @Test
    public void isInvoiceRequired_emptyList() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = false;
        final int requestYwd = CalendarStatic.getRelativeYWD(0);
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BillingBean> billingList = new LinkedList<>();
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                bookingServiceMock.setInvoiceRestrictions(lastYwd, accountId);
                billingServiceMock.getBillingManager().getAllBillingOutstanding(accountId, lastYwd); returns(billingList);
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(false));
    }

    /*
     * The Billing is NOT a fixed item and the fixedItemsOnly = true so should return false
     */
    @Test
    public void isInvoiceRequired_noFixedItems() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = true;
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BillingBean> billingList = Arrays.asList(new BillingBean());
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            @Mocked private BillingBean billing;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                billingServiceMock.getBillingManager().getAllBillingOutstanding(accountId, lastYwd); returns(billingList);
                billing.hasBillingBits(BillingEnum.TYPE_FIXED_ITEM); result = false;
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(false));
    }

    /*
     * The Billing is a fixed item and the fixedItemsOnly = true so should return true
     */
    @Test
    public void isInvoiceRequired_onlyFixedItem() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = true;
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BillingBean> billingList = Arrays.asList(new BillingBean());
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            @Mocked private BillingBean billing;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                billingServiceMock.getBillingManager().getAllBillingOutstanding(accountId, lastYwd); returns(billingList);
                billing.hasBillingBits(BillingEnum.TYPE_FIXED_ITEM); result = true;
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(true));
    }

    /*
     * The fixedItemsOnly = false with a BillingBean so should return true
     */
    @Test
    public void isInvoiceRequired_anyBilling() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = false;
        final int requestYwd = CalendarStatic.getRelativeYWD(0);
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BillingBean> billingList = Arrays.asList(new BillingBean());
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                bookingServiceMock.setInvoiceRestrictions(lastYwd, accountId);
                billingServiceMock.getBillingManager().getAllBillingOutstanding(accountId, lastYwd); returns(billingList);
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(true));
    }

    /*
     * The fixedItemsOnly = true with TWO BillingBeans one fixed, one not so should return true
     */
    @Test
    public void isInvoiceRequired_bothBillingTypes() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = true;
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BillingBean> billingList = Arrays.asList(new BillingBean(), new BillingBean());
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            @Mocked private BillingBean billing;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                billingServiceMock.getBillingManager().getAllBillingOutstanding(accountId, lastYwd); returns(billingList);
                billing.hasBillingBits(BillingEnum.TYPE_FIXED_ITEM); result = false; result = true; times = 2;
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(true));
    }

    /*
     * The fixedItemsOnly = false with a BillingBean so should return true
     */
    @Test
    public void isInvoiceRequired_onlyPreBilling() throws Exception {
        final int accountId = 19;
        final boolean fixedItemsOnly = false;
        final int requestYwd = CalendarStatic.getRelativeYWD(0);
        final int lastYwd = CalendarStatic.getRelativeYWD(0);
        final List<BookingBean> bookingList = Arrays.asList(new BookingBean());
        new Expectations() {
            @Cascading private ChildBookingService bookingServiceMock;
            @Cascading private BillingService billingServiceMock;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                bookingServiceMock.setInvoiceRestrictions(lastYwd, accountId); result = bookingList;
            }
        };
        boolean result = service.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd);
        assertThat(result, is(true));
    }

}