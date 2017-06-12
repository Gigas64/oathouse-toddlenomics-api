/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.profile.ChildService;
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
public class TestChildBookingRequestService_setYwBookingRequestsForChildren {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingRequestService service;

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
        service = ChildBookingRequestService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void setYwBookingRequestsForChildren_suspendedAccount() throws Exception {
        int yw = CalendarStatic.getRelativeYW(1);
//        new Expectations() {
//            @Mocked
//            private ChildService childService;
//            @Mocked
//            private AgeRoomService ageRoomService;
//            {
//                ChildService.getInstance(); returns(childService);
//                AgeRoomService.getInstance();returns(ageRoomService);
//            }
//        };

    }
}