/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.util;

// common imports
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.accounts.TransactionService;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
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
public class SandboxTest {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;

    @Before
    public void setUp() throws Exception {
        String authority = "ToddlenomicsWeb";
        String sep = File.separator;
        String rootStorePath = "." + sep + "oss" + sep + "data";
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // global instances
        service = TransactionService.getInstance();
    }

    /*
     *
     */
    @Test
    public void ResetBookings() throws Exception {

    }


}
