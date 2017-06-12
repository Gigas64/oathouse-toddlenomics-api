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
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
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
public class BillingServiceTest_getLineBillingForInvoice {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    @Mocked
    private TransactionService tsm;
    @Mocked
    private BillingManager bmm;

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
        new Expectations() {
            {
                TransactionService.getInstance();returns(tsm);
            }
        };
    }

    /*
     * no invoice billings found
     */
    @Test
    public void getLineBillingsForInvoice_NoBillings() throws Exception {
        attributes.put("invoiceId", 23);
        final BillingBean billing = Builders.getBilling(1, attributes);
        final List<BillingBean> invoiceList = new LinkedList<>();
        new Expectations() {
            {
                bmm.getAllBillingForInvoice(billing.getAccountId(), billing.getInvoiceId()); result = invoiceList;
            }
        };
        BillingEnum[] options = {};
        List<BillingBean> resultsList = service.getLineBillingsForInvoice(billing.getAccountId(), billing.getInvoiceId(), options);
        assertThat(resultsList.isEmpty(), is(true));
    }

    /*
     * billing that is a session (GROUP_BOOKING), no filter, transaction not before
     */
    @Test
    public void getLineBillingsForInvoice_SessionBilling() throws Exception {
        attributes.put("invoiceId", 17);
        final BillingBean billing = Builders.getBilling(2, attributes);
        attributes.put("invoiceId", 23);
        final BillingBean prevBilling = Builders.getBilling(1, attributes);
        final List<BillingBean> invoiceList = new LinkedList<>(asList(billing));
        final List<BillingBean> bookingList = new LinkedList<>(asList(billing, prevBilling));
        final List<BillingBean> loyaltyList = new LinkedList<>();
        new Expectations() {
            {
                bmm.getAllBillingForInvoice(billing.getAccountId(), billing.getInvoiceId()); result = invoiceList;
                bmm.getAllBillingForBooking(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); result = bookingList; times = 1;
                bmm.getAllBillingForBookingLoyalty(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); result = loyaltyList; times = 1;
                tsm.isInvoiceBefore(billing.getAccountId(), anyInt, billing.getInvoiceId()); result = true; times = 2;
            }
        };
        BillingEnum[] options = {};
        List<BillingBean> resultsList = service.getLineBillingsForInvoice(billing.getAccountId(), billing.getInvoiceId(), options);
        assertThat(resultsList.size(), is(2));
    }

    /*
     * billing that is a not a GROUP_BOOKING or GROUP_LOYALTY
     */
    @Test
    public void getLineBillingsForInvoice_NotSessionBilling() throws Exception {
        attributes.put("invoiceId", 17);
        final BillingBean billing = Builders.getBilling(2, attributes, BillingEnum.GROUP_FIXED_ITEM);
        attributes.put("invoiceId", 23);
        final BillingBean prevBilling = Builders.getBilling(1, attributes, BillingEnum.GROUP_FIXED_ITEM);
        final List<BillingBean> invoiceList = new LinkedList<>(asList(billing));
        new Expectations() {
            {
                bmm.getAllBillingForInvoice(billing.getAccountId(), billing.getInvoiceId()); result = invoiceList;
                bmm.getAllBillingForBooking(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); times = 0;
                bmm.getAllBillingForBookingLoyalty(billing.getAccountId(), billing.getBookingId(), BTIdBits.TYPE_CHARGE); times = 0;
                tsm.isInvoiceBefore(billing.getAccountId(), prevBilling.getInvoiceId(), billing.getInvoiceId()); times = 0;
            }
        };
        BillingEnum[] options = {};
        List<BillingBean> resultsList = service.getLineBillingsForInvoice(billing.getAccountId(), billing.getInvoiceId(), options);
        assertThat(resultsList.size(), is(1));
    }

}
