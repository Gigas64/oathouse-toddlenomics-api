/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingManager;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
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
public class BillingServiceTest_getPredictedLineBillingsForAccount {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    private final int lastYwd = CalendarStatic.getRelativeYW(1);

    // mock objects
    @Mocked({"getPredictedBillingsForAccount"})
    private BillingService bsm;
    @Mocked({"getAllBillingForBooking", "getAllBillingForBookingLoyalty"})
    private BillingManager bmm;

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
    }

    /*
     * no predicted billings are found so nothing returned
     */
    @Test
    public void getPredictedLineBillingsForAccount_noBillings() throws Exception {
        final int accountId = 13;
        final List<BillingBean> emptyList = new LinkedList<>();
        new Expectations() {
            {
                bsm.getPredictedBillingsForAccount(accountId, lastYwd); result = emptyList;
            }
        };
        List<BillingBean> resultsList = service.getPredictedLineBillingsForAccount(accountId, lastYwd);
        assertThat(resultsList.isEmpty(), is(true));
    }

    /*
     * a predicted billing but no previous invoiced billings for this booking.
     */
    @Test
    public void getPredictedLineBillingsForAccount_billingNoPreviousInvoice() throws Exception {
        final BillingBean billing = Builders.getBilling(1);
        final List<BillingBean> billingList = new LinkedList<>(asList(billing));
        new Expectations() {
            {
                bsm.getPredictedBillingsForAccount(billing.getAccountId(), lastYwd); result = billingList; times = 1;
                bmm.getAllBillingForBooking(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); times = 1;
                bmm.getAllBillingForBookingLoyalty(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); times = 1;
            }
        };
        List<BillingBean> resultsList = service.getPredictedLineBillingsForAccount(billing.getAccountId(), lastYwd);
        assertThat(resultsList.size(), is(1));
    }

    /*
     * a predicted billing and a previous invoice for the booking
     */
    @Test
    public void getPredictedLineBillingsForAccount_billingPreviousInvoiceBooking() throws Exception {
        final BillingBean billing = Builders.getBilling(2);
        final List<BillingBean> billingList = new LinkedList<>(asList(billing));
        attributes.put("invoiceId", 23);
        BillingBean invoiceBilling = Builders.getBilling(1, attributes);
        final List<BillingBean> invoiceList = new LinkedList<>(asList(invoiceBilling));
        new Expectations() {
            {
                bsm.getPredictedBillingsForAccount(billing.getAccountId(), lastYwd); result = billingList; times = 1;
                bmm.getAllBillingForBooking(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); result = invoiceList; times = 1;
                bmm.getAllBillingForBookingLoyalty(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); times = 1;
            }
        };
        List<BillingBean> resultsList = service.getPredictedLineBillingsForAccount(billing.getAccountId(), lastYwd);
        assertThat(resultsList.size(), is(2));
    }

    /*
     * a predicted billing and a previous invoice on the loyalty and the booking
     */
    @Test
    public void getPredictedLineBillingsForAccount_billingPreviousInvoiceLoyalty() throws Exception {
        final BillingBean billing = Builders.getBilling(2);
        final List<BillingBean> billingList = new LinkedList<>(asList(billing));
        attributes.put("invoiceId", 23);
        BillingBean invoiceBilling = Builders.getBilling(1, attributes);
        final List<BillingBean> invoiceList = new LinkedList<>(asList(invoiceBilling));
        new Expectations() {
            {
                bsm.getPredictedBillingsForAccount(billing.getAccountId(), lastYwd); result = billingList; times = 1;
                bmm.getAllBillingForBooking(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); result = invoiceList; times = 1;
                bmm.getAllBillingForBookingLoyalty(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); result = invoiceList; times = 1;
            }
        };
        List<BillingBean> resultsList = service.getPredictedLineBillingsForAccount(billing.getAccountId(), lastYwd);
        assertThat(resultsList.size(), is(3));
    }


}
