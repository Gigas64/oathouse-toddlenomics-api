/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.server.OssProperties;
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
public class RelationManagerTest {

    private String owner;
    private String rootStorePath;
    private RelationManager manager;

    public RelationManagerTest() {
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
        manager = new RelationManager("RelationManager");
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
    public void system01_Relation() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        RelationBean bean1 = (RelationBean) BeanBuilder.addBeanValues(new RelationBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        RelationBean bean2 = (RelationBean) BeanBuilder.addBeanValues(new RelationBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new RelationManager("RelationManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_Relation() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.profile.RelationBean", false);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(RelationBean bean : manager.getAllObjects()) {
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
    private void testOrder(int[] order, List<RelationBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}
