/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.config;

import java.util.Collections;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class RoomConfigManagerTest {

    private String owner;
    private String rootStorePath;
    private RoomConfigManager manager;

    public RoomConfigManagerTest() {
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
        manager = new RoomConfigManager("RoomConfigManager");
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
    public void system01_RoomConfig() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        RoomConfigBean bean1 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        RoomConfigBean bean2 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new RoomConfigManager("RoomConfigManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_RoomConfig() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.RoomConfigBean", false);
    }

    /**
     * Unit test: To test the comparator
     */
    @Test
    public void unit02_RoomConfig() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("name", "AAA");
        RoomConfigBean bean01 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), ++id, fieldSet);
        manager.setObject(bean01);
        fieldSet.put("name", "ABC");
        RoomConfigBean bean02 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), ++id, fieldSet);
        manager.setObject(bean02);
        fieldSet.put("name", "AAA");
        RoomConfigBean bean03 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), ++id, fieldSet);
        manager.setObject(bean03);
        fieldSet.put("name", "aBb");
        RoomConfigBean bean04 = (RoomConfigBean) BeanBuilder.addBeanValues(new RoomConfigBean(), ++id, fieldSet);
        manager.setObject(bean04);
        LinkedList<RoomConfigBean> order = (LinkedList<RoomConfigBean>) manager.getAllObjects();
        testOrder(new int[] {1,2,3,4}, order);
        Collections.sort(order, RoomConfigBean.CASE_INSENSITIVE_NAME_ORDER);
        testOrder(new int[] {1,3,4,2}, order);
    }

    /**
     * Test of getTotalRoomCapacity method, of class RoomConfigManager.
     */
    @Test
    public void testGetTotalRoomCapacity() throws Exception {
        manager.setObject(new RoomConfigBean(1, 10, "AA", 9, 7, 2500, "Tester"));
        manager.setObject(new RoomConfigBean(2, 12, "BB", 8, 7, 1200, "Tester"));
        manager.setObject(new RoomConfigBean(3, 12, "CC", 7, 6, 1700, "Tester"));
        manager.setObject(new RoomConfigBean(4, 13, "DD", 6, 5, 1900, "Tester"));
        assertEquals(30,manager.getTotalRoomCapacity());
    }

    /**
     * Test of getRoomForAgeGroup method, of class RoomConfigManager.
     */
    @Test
    public void testGetRoomForAgeGroup() throws Exception {
        manager.setObject(new RoomConfigBean(1, 10, "AA", 9, 7, 2500, "Tester"));
        manager.setObject(new RoomConfigBean(2, 10, "BB", 8, 7, 1200, "Tester"));
        manager.setObject(new RoomConfigBean(3, 12, "CC", 11, 6, 1700, "Tester"));
        manager.setObject(new RoomConfigBean(5, 12, "CC", 12, 6, 1700, "Tester"));
        manager.setObject(new RoomConfigBean(4, 12, "DD", 10, 5, 1900, "Tester"));
        manager.setObject(new RoomConfigBean(6, 12, "DD", 11, 5, 1900, "Tester"));
        assertEquals(1, manager.getRoomForAgeGroup(10).getRoomId());
        assertEquals(5, manager.getRoomForAgeGroup(12).getRoomId());


    }
    /*
     *
     */

    @Test
    public void testGetDefaultObject() throws Exception {
        RoomConfigBean room = manager.getObject(-1);
        assertEquals(room.getRoomId(), RoomConfigManager.NO_ROOM_ID);
        assertEquals(room.getName(), RoomConfigManager.NO_ROOM_NAME);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(RoomConfigBean bean : manager.getAllObjects()) {
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
    private void testOrder(int[] order, List<RoomConfigBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }

}