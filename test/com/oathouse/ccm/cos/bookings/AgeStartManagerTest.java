/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.ccm.cos.config.AgeStartBean;
import com.oathouse.ccm.cos.config.AgeStartManager;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class AgeStartManagerTest {

    private String owner;
    private String rootStorePath;
    private AgeStartManager manager;

    public AgeStartManagerTest() {
        owner = "tester";
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority("man00test");
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        manager = new AgeStartManager("AgeStartManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_AgeStart() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        AgeStartBean bean1 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        AgeStartBean bean2 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new AgeStartManager("AgeStartManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_AgeStart() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.AgeStartBean", false);
    }

    /**
     * Unit test: To test the comparator
     */
    @Test
    public void unit02_Contact() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("name", "AAA");
        AgeStartBean bean01 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), ++id, fieldSet);
        manager.setObject(bean01);
        fieldSet.put("name", "ABC");
        AgeStartBean bean02 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), ++id, fieldSet);
        manager.setObject(bean02);
        fieldSet.put("name", "AAA");
        AgeStartBean bean03 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), ++id, fieldSet);
        manager.setObject(bean03);
        fieldSet.put("name", "aBb");
        AgeStartBean bean04 = (AgeStartBean) BeanBuilder.addBeanValues(new AgeStartBean(), ++id, fieldSet);
        manager.setObject(bean04);
        LinkedList<AgeStartBean> order = (LinkedList<AgeStartBean>) manager.getAllObjects();
        testOrder(new int[] {1,2,3,4}, order);
        Collections.sort(order, AgeStartBean.CASE_INSENSITIVE_NAME_ORDER);
        testOrder(new int[] {1,3,4,2}, order);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(AgeStartBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<AgeStartBean> list) throws Exception {
        assertEquals("Testing manager size",order.length,list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]",order[i], list.get(i).getIdentifier());
        }
    }
}
