/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import static com.oathouse.ccm.cma.VABoolean.*;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.io.File;
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
public class TestChildBookingModel_getPredictedBilling {



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


    }

    /*
     * run through all the method
     */
    @Test
    public void test01_runThrough() throws Exception {
        final int accountId = 13;
        final int startYwd = CalendarStatic.getRelativeYW(0) + 2;
        final int endYwd = CalendarStatic.getRelativeYW(1) + 2;
        // TODO
        ChildBookingModel.getPredictedBilling(accountId, startYwd, endYwd, INCLUDE_EDUCATION, INCLUDE_ADJUSTMENTS, INCLUDE_LOYALTY,INCLUDE_LIVE_BOOKINGS);
        fail("Not yet implemented");
    }

}
