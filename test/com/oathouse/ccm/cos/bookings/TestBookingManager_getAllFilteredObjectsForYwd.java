/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.bookings;

// common imports
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
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
public class TestBookingManager_getAllFilteredObjectsForYwd {

    private String owner = ObjectBean.SYSTEM_OWNED;
    private String rootStorePath;
    private BookingManager manager;

    // global attributes
    private int ywd = CalendarStatic.getRelativeYW(0);
    private int periodSd = SDHolder.buildSD(10, 20);
    private int contactId = 17;
    private int roomId = 19;


    public TestBookingManager_getAllFilteredObjectsForYwd() {
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new BookingManager("BookingManager");
        manager.init();
        manager.clear();

    }

    @After
    public void tearDown() throws Exception {
    }

    /*
     * testthe action filter workdcorrectly
     */
    @Test
    public void getAllFilteredObjectsForYwd_actionFilterWorks() throws Exception {

        manager.setCreateBooking(ywd, periodSd, 13, roomId, contactId, contactId, contactId, BTIdBits.ATTENDING_STANDARD, ActionBits.IMMEDIATE_RECALC_BIT, ywd, "", owner);
        manager.setCreateBooking(ywd, periodSd, 14, roomId, contactId, contactId, contactId, BTIdBits.WAITING_STANDARD, ActionBits.ALL_OFF, ywd, "", owner);
        List<BookingBean> bookingList = manager.getAllFilteredObjectsForYwd(ywd, BookingManager.ALL_PERIODS, -1, -1, BTBits.WAITING_BIT, ActionBits.TYPE_FILTER_OFF, false);
        assertThat(bookingList.size(), is(1));
    }


}
