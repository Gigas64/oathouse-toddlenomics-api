/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
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
public class TransactionServiceTest_createInvoice {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;
    private BillingService billingService;

    private final int childId = Builders.childId;
    private final int accountId = Builders.accountId;

    private final int invoiceYwd = CalendarStatic.getRelativeYW(0);
    private final int lastYwd = CalendarStatic.getRelativeYW(1);
    private final int dueYwd = CalendarStatic.getRelativeYW(2);

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

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
        service = TransactionService.getInstance();
        service.clear();
        billingService = BillingService.getInstance();
        billingService.clear();
        ChildService.getInstance().clear();

        final ChildEducationTimetableBean childEducationTimetable = Builders.getChildEdTimetable();
        final PriceListBean priceList = Builders.getPriceList();
        final SystemPropertiesBean properties = Builders.getProperties();

        new NonStrictExpectations() {
            @Cascading private TimetableService timetableService;
            @Cascading private PriceConfigService priceConfigService;
            @Cascading private PropertiesService propertiesService;
            {
                PriceConfigService.getInstance(); returns(priceConfigService);
                TimetableService.getInstance(); returns(timetableService);
                PropertiesService.getInstance(); returns(propertiesService);

                priceConfigService.getPriceListForProfile(anyInt, anyInt, anyInt, (MultiRefEnum) any); result = priceList;
                timetableService.getChildEducationTimetable(anyInt, anyInt); result = childEducationTimetable;
                propertiesService.getSystemProperties(); returns(properties);
            }
        };
    }

    /*
     *
     */
    @Test
    public void createInvoice_preCharge() throws Exception {
        ChildBean child = Builders.setChild(childId);
        attributes.put("ywd", CalendarStatic.getRelativeYW(-4));
        BookingBean booking = Builders.setBooking(1, attributes, BookingState.OPEN);
        assertThat(billingService.getBillingManager().getAllBillingForBooking(booking.getBookingId(), BTIdBits.TYPE_CHARGE).size(),is(0));
        InvoiceBean invoice = service.createInvoice(accountId, false, invoiceYwd, lastYwd, dueYwd, "", owner);
        List<BillingBean> results = billingService.getBillingManager().getAllBillingForInvoice(accountId, invoice.getInvoiceId());
        assertThat(results.size(),is(1));

    }


}
