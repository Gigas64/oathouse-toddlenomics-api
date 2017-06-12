/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
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
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingServiceTest_createBookingLoyaltyDiscounts {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    final private int accountId = Builders.accountId;
    final private int childId = Builders.childId;

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
        final List<LoyaltyDiscountBean> loyaltyList = Builders.getLoyaltyDay();
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
     * standard loyalty no loyalty
     */
    @Test
    public void createBookingLoyaltyDiscounts_noLoyalty() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "15:58");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(attributes);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.isEmpty(), is(true));
    }

    /*
     * standard loyalty one short day loyalty extream test lower limit
     */
    @Test
    public void createBookingLoyaltyDiscounts_oneExtreamLowerLoyalty() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "15:59");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getSignedValue(), is(-400L));
    }

    /*
     * Extream test on one loyaltyextream test upper limit
     */
    @Test
    public void createBookingLoyaltyDiscounts_oneExtreamUpperLoyalty() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "16:58");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getSignedValue(), is(-400L));
    }

    /*
     * standard loyalty two short day loyalty extream test lower limit
     */
    @Test
    public void createBookingLoyaltyDiscounts_twoExtreamLowerLoyalty() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getSignedValue(), is(-200L));
        assertThat(results.get(1).getSignedValue(), is(-400L));
    }

    /*
     * standard loyalty two short day loyalty extream test lower limit
     */
    @Test
    public void createBookingLoyaltyDiscounts_oneExtendedStart() throws Exception {
        int bookingSd = SDHolder.buildSD("9:00", "16:59");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getSignedValue(), is(-400L));
    }

    /*
     * standard loyalty one short day loyalty extream test lower limit
     */
    @Test
    public void createBookingLoyaltyDiscounts_authNoChange() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "16:59");
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes, BookingState.AUTHORISED);
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(2));
    }

    /*
     * Loyalty has already been awarded previously
     */
    @Test
    public void createBookingLoyaltyDiscounts_alreadyCharged() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "16:59");
        int invoiceId = 97;
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        for(BillingBean billing : service.createBookingLoyaltyDiscounts(accountId, booking, owner)) {
            service.getBillingManager().setObject(accountId, billing);
            service.setBillingInvoiceId(accountId, billing.getBillingId(), invoiceId, owner);
        }
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat("Should be empty as already billed",results.isEmpty(), is(true));

    }

    /*
     * Loyalty has already been awarded loyalty then removed
     */
    @Test
    public void createBookingLoyaltyDiscounts_chargedThenRemoved() throws Exception {
        int bookingSd = SDHolder.buildSD("8:00", "16:59");
        int invoiceId = 97;
        attributes.put("bookingSd", bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        for(BillingBean billing : service.createBookingLoyaltyDiscounts(accountId, booking, owner)) {
            service.getBillingManager().setObject(accountId, billing);
            service.setBillingInvoiceId(accountId, billing.getBillingId(), invoiceId, owner);
        }
        // remove a loyaty
        final List<LoyaltyDiscountBean> loyaltyList = Builders.getLoyaltyDay();
        loyaltyList.remove(0);
        new NonStrictExpectations() {{
            pcsm.getAllLoyaltyDiscountForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = loyaltyList;
        }};
        List<BillingBean> results = service.createBookingLoyaltyDiscounts(accountId, booking, owner);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getSignedValue(), is(400L));
    }

}
