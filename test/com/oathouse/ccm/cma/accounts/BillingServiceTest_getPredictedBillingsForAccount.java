/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
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
public class BillingServiceTest_getPredictedBillingsForAccount {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final int accountId = 13;
    private final int lastYwd = CalendarStatic.getRelativeYW(4);
    private final int requestYwd = CalendarStatic.getRelativeYWD(0);

    @Mocked() private ChildBookingService cbsm;
    @Mocked() private PropertiesService psm;
    @Mocked({"getPredictedBillingsForBooking", "getPredictedFixedChargeBilling"}) private BillingService bsm;

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

        // Global Mock
        final SystemPropertiesBean properties = Builders.getProperties();
        new Expectations() {{
                ChildBookingService.getInstance(); returns(cbsm);
                PropertiesService.getInstance(); returns(psm);

                psm.getSystemProperties(); result = properties;
        }};
    }

    /*
     *
     */
    @Test
    public void getPredictedBillingsForAccount_NoBookingsNoFixed() throws Exception {

        new Expectations() {
            {
                cbsm.setInvoiceRestrictions(lastYwd, accountId);
                bsm.getPredictedBillingsForBooking(accountId, (BookingBean) any, (VABoolean[]) any); times = 0;
                bsm.getPredictedFixedChargeBilling(accountId, lastYwd);
            }
        };

        List<BillingBean> resultList = service.getPredictedBillingsForAccount(accountId, lastYwd);
        assertThat(resultList.isEmpty(), is(true));
    }

    /*
     *
     */
    @Test
    public void getPredictedBillingsForAccount_BookingsFixed() throws Exception {

        new Expectations() {
            {
                cbsm.setInvoiceRestrictions(lastYwd, accountId);
                bsm.getPredictedBillingsForBooking(accountId, (BookingBean) any, (VABoolean[]) any); times = 0;
                bsm.getPredictedFixedChargeBilling(accountId, lastYwd);
            }
        };

        List<BillingBean> resultList = service.getPredictedBillingsForAccount(accountId, lastYwd);
        assertThat(resultList.isEmpty(), is(true));
    }

    /*
     *
     */
    @Test
    public void getPredictedBillingsForAccount_BookingsFixed_beforeFirstInvoiceYwd() throws Exception {

        new Expectations() {
            {
                cbsm.setInvoiceRestrictions(lastYwd, accountId);
                bsm.getPredictedBillingsForBooking(accountId, (BookingBean) any, (VABoolean[]) any); times = 0;
                bsm.getPredictedFixedChargeBilling(accountId, lastYwd);
            }
        };

        List<BillingBean> resultList = service.getPredictedBillingsForAccount(accountId, lastYwd);
        assertThat(resultList.isEmpty(), is(true));
    }

}
