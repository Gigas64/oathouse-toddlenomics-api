package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TestBookingManager_CleanStateAndActuals {

    private String owner = ObjectBean.SYSTEM_OWNED;
    private String rootStorePath;
    private BookingManager manager;

    // global attributes
    private int ywd = CalendarStatic.getRelativeYW(0);
    private int periodSd = SDHolder.buildSD("08:00", "17:00");
    private int contactId = 17;
    private int roomId = 19;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    public TestBookingManager_CleanStateAndActuals() {
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        //clear out the attributes
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
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

    @Test
    public void resetsActualsIfWasntAttendingAndNowIs() throws Exception {


    }

    @Test
    public void cleansCustodiansAndMovesBookingToCompleteWhenWasAttendingAndNowIsnt() throws Exception {
    }


}