/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.profile;

// common imports
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

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
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
public class StaffQualificationManagerTest {

    private String owner = ObjectBean.SYSTEM_OWNED;
    private String rootStorePath;
    private StaffQualificationManager manager;

    public StaffQualificationManagerTest() {
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
        manager = new StaffQualificationManager("StaffQualificationManager");
        manager.init();
        manager.clear();

    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_StaffQualification() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        StaffQualificationBean bean1 = (StaffQualificationBean) BeanBuilder.addBeanValues(new StaffQualificationBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        StaffQualificationBean bean2 = (StaffQualificationBean) BeanBuilder.addBeanValues(new StaffQualificationBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new StaffQualificationManager("StaffQualificationManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_StaffQualification() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.profile.StaffQualificationBean", false, true);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(StaffQualificationBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of the beans in the manager
     */
    private void testOrder(int[] order) throws Exception {
        testOrder(order, manager.getAllObjects());
    }


    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<StaffQualificationBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}