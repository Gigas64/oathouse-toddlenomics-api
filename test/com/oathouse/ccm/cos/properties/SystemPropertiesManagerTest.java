/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.properties;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.server.OssProperties;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class SystemPropertiesManagerTest {

    private String owner;
    private String rootStorePath;
    private SystemPropertiesManager manager;

    public SystemPropertiesManagerTest() {
        owner = "tester";
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String authority = "man00test";
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new SystemPropertiesManager("SystemPropertiesManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_SystemProperties() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.properties.SystemPropertiesBean", false);
    }

}