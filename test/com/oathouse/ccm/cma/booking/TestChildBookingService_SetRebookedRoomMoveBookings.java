package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.VABoolean;
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
 * @author Nick
 */
public class TestChildBookingService_SetRebookedRoomMoveBookings {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingService service;

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
        service = ChildBookingService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void setRebookedRoomMoveBookings_runthrough() throws Exception {
        int childId = 13;
        //service.setRebookedRoomMoveBookings(childId, owner, VABoolean.INCLUDE_EDUCATION);
        fail("Not yet implemented");
    }


    @Test
    public void returnsList() throws Exception {
    }

}