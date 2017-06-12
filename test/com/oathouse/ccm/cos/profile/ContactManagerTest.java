/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.profile;

import java.util.List;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import java.util.HashMap;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ContactManagerTest {

    private String owner;
    private String rootStorePath;
    private ContactManager manager;

    public ContactManagerTest() {
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
        manager = new ContactManager("ContactManager");
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
    public void system01_Contact() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        ContactBean bean1 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        ContactBean bean2 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new ContactManager("ContactManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_Contact() throws Exception {
        //BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.profile.ContactBean", false);
    }

    /**
     * Unit test: To test the comparator
     */
    @Test
    public void unit02_Contact() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("business", "AAA");
        ContactBean bean01 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), ++id, fieldSet);
        manager.setObject(bean01);
        fieldSet.put("business", "ABC");
        ContactBean bean02 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), ++id, fieldSet);
        manager.setObject(bean02);
        fieldSet.put("business", "AAA");
        ContactBean bean03 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), ++id, fieldSet);
        manager.setObject(bean03);
        fieldSet.put("business", "aBb");
        ContactBean bean04 = (ContactBean) BeanBuilder.addBeanValues(new ContactBean(), ++id, fieldSet);
        manager.setObject(bean04);
        LinkedList<ContactBean> order = (LinkedList<ContactBean>) manager.getAllObjects();
        testOrder(new int[] {1,2,3,4}, order);
        Collections.sort(order, ContactBean.CASE_INSENSITIVE_BUSINESS_ORDER);
        testOrder(new int[] {1,3,4,2}, order);
    }


    /**
     * Test of getObject method, of class ContactManager.
     */
    @Test
    public void testGetObject() throws Exception {
    }

    /**
     * Test of getAllObjects method, of class ContactManager.
     */
    @Test
    public void testGetAllObjects_0args() throws Exception {
    }

    /**
     * Test of getAllObjects method, of class ContactManager.
     */
    @Test
    public void testGetAllObjects_RelationType() throws Exception {
    }

    /**
     * Test of getAllCustodians method, of class ContactManager.
     */
    @Test
    public void testGetAllCustodians() throws Exception {
    }

    /**
     * Test of setObject method, of class ContactManager.
     */
    @Test
    public void testSetObject() throws Exception {
    }

    /**
     * Test of setObjectPersonalDetails method, of class ContactManager.
     */
    @Test
    public void testSetObjectPersonalDetails() throws Exception {
    }

    /**
     * Test of setObjectAddress method, of class ContactManager.
     */
    @Test
    public void testSetObjectAddress() throws Exception {
    }

    /**
     * Test of setObjectPhotoId method, of class ContactManager.
     */
    @Test
    public void testSetObjectPhotoId() throws Exception {
    }

    /**
     * Test of setObjectCommunications method, of class ContactManager.
     */
    @Test
    public void testSetObjectCommunications() throws Exception {
    }

    /**
     * Test of setObjectNotes method, of class ContactManager.
     */
    @Test
    public void testSetObjectNotes() throws Exception {
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(ContactBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<ContactBean> list) throws Exception {
        assertEquals("Testing manager size",order.length,list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]",order[i], list.get(i).getIdentifier());
        }
    }

}
