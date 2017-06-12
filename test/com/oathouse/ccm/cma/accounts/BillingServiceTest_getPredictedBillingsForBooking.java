/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.*;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
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
public class BillingServiceTest_getPredictedBillingsForBooking {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final int accountId = 13;

    // common mock
    @Mocked({"createBookingSessionCharge", "createBookingEducationReduction", "createBookingPriceAdjustments", "createBookingDiscount", "createBookingLoyaltyDiscounts"})
    private BillingService bsm;


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

    }

    /*
     * null session billing, no options
     */
    @Test
    public void getPredictedBillingsForBooking_nullSessionNoOptions() throws Exception {
        final BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), 1);
        new Expectations() {
            {
                bsm.createBookingSessionCharge(accountId, booking, ObjectBean.SYSTEM_OWNED); result = null;
            }
        };
        VABoolean[] options = {};
        List<BillingBean> resultList = service.getPredictedBillingsForBooking(accountId, booking, options);
        assertThat(resultList.isEmpty(), is(true));
    }

    /*
     * null session billing, all options
     */
    @Test
    public void getPredictedBillingsForBooking_nullSessionAllOptions() throws Exception {
        final BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), 1);
        new Expectations() {
            {
                bsm.createBookingSessionCharge(accountId, booking, owner); result = null;
                bsm.createBookingLoyaltyDiscounts(accountId, booking, owner);
            }
        };
        VABoolean[] options = {INCLUDE_EDUCATION, INCLUDE_ADJUSTMENTS, INCLUDE_LOYALTY};
        List<BillingBean> resultList = service.getPredictedBillingsForBooking(accountId, booking, options);
        assertThat(resultList.isEmpty(), is(true));
    }

    /*
     * test that each of the methods are called and that returning nothing is OK
     */
    @Test
    public void getPredictedBillingsForBooking_SessionAllOptionsNoReturns() throws Exception {
        final BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), 1);
        final BillingBean session = (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), 1);
        final List<BillingBean> billingList = new LinkedList<>(asList(session));
        new Expectations() {
            {
                bsm.createBookingSessionCharge(accountId, booking, owner); result = session;
                bsm.createBookingEducationReduction(accountId, booking, owner); result = null;
                bsm.createBookingPriceAdjustments(accountId, booking, owner);
                bsm.createBookingDiscount(accountId, booking, billingList, owner); result = null;
                bsm.createBookingLoyaltyDiscounts(accountId, booking, owner);
            }
        };
        VABoolean[] options = {INCLUDE_EDUCATION, INCLUDE_ADJUSTMENTS, INCLUDE_LOYALTY};
        List<BillingBean> resultList = service.getPredictedBillingsForBooking(accountId, booking, options);
        assertThat(resultList.size(), is(1));
    }

    /*
     * test that each of the methods are called and when returning something they are added to the list
     */
    @Test
    public void getPredictedBillingsForBooking_SessionAllOptionsAllReturns() throws Exception {
        final BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), 1);
        final BillingBean session = (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), 1);
        final List<BillingBean> billingList = new LinkedList<>(asList(session));
        final List<BillingBean> discountList = new LinkedList<>(asList(session, session, session));
        new Expectations() {
            {
                bsm.createBookingSessionCharge(accountId, booking, owner); result = session;
                bsm.createBookingEducationReduction(accountId, booking, owner); result = session;
                bsm.createBookingPriceAdjustments(accountId, booking, owner); result = billingList;
                bsm.createBookingDiscount(accountId, booking, discountList, owner); result = session;
                bsm.createBookingLoyaltyDiscounts(accountId, booking, owner); result = billingList;
            }
        };
        VABoolean[] options = {INCLUDE_EDUCATION, INCLUDE_ADJUSTMENTS, INCLUDE_LOYALTY};
        List<BillingBean> resultList = service.getPredictedBillingsForBooking(accountId, booking, options);
        assertThat(resultList.size(), is(5));
    }



}
