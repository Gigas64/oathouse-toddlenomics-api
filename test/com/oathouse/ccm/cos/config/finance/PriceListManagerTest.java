package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class PriceListManagerTest {

    private String owner;
    private String rootStorePath;
    private PriceListManager manager;

    public PriceListManagerTest() {
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
        manager = new PriceListManager("PriceConfigManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_PriceConfig() throws Exception {
        Map<Integer, Long> periodSdValue = new ConcurrentSkipListMap<>();
        periodSdValue.put(SDHolder.getSD(10, 9), 20L);
        periodSdValue.put(SDHolder.getSD(20, 9), 20L);
        periodSdValue.put(SDHolder.getSD(30, 9), 20L);
        periodSdValue.put(SDHolder.getSD(40, 9), 20L);

        PriceListBean bean1 = new PriceListBean(1, "TestName1", "TestLable1", periodSdValue, owner);
        PriceListBean bean2 = new PriceListBean(2, "TestName2", "TestLable2", periodSdValue, owner);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new PriceListManager("PriceConfigManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_PriceConfig() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.finance." + VT.PRICE_LIST.bean(), false);
    }

    /**
     * Test of getObject method, of class PeriodSdValueManager.
     */
    @Test
    public void unit02_getObject() throws Exception {
        // create bean
        Map<Integer, Long> periodSdValue = new ConcurrentSkipListMap<Integer, Long>();
        periodSdValue.put(SDHolder.getSD(10, 9), 20L);
        periodSdValue.put(SDHolder.getSD(20, 9), 20L);
        periodSdValue.put(SDHolder.getSD(30, 9), 20L);
        periodSdValue.put(SDHolder.getSD(40, 9), 20L);
        PriceListBean bean = new PriceListBean(1, "TestName", "TestLable", periodSdValue, owner);
        manager.setObject(bean);
        assertEquals(bean.getName(), manager.getObject(bean.getName()).getName());
        try {
            manager.getObject("wrongName");
            fail("should have thrown an exception");
        } catch(NoSuchIdentifierException noSuchIdentifierException) {
            // success
        }
        // now check that we have not changed the origional
        assertEquals(periodSdValue, manager.getObject(bean.getPriceListId()).getPeriodSdValues());
    }
    /**
     * Unit test: test the set method does not accept overlapping values
     */
    @Test
    public void unit02_setObject() throws Exception {
        // create beans with empty periodSdValues
        PriceListBean bean = new PriceListBean(1, "TestName", "TestLable", null, owner);
        try {
            manager.setObject(bean);
            fail("Should have thrown an exception");
        } catch(NullObjectException e) {
            // success
        }

        Map<Integer, Long> periodSdValue = new ConcurrentSkipListMap<Integer, Long>();
        bean = new PriceListBean(1, "TestName", "TestLable", periodSdValue, owner);
        try {
            manager.setObject(bean);
            fail("Should have thrown an exception");
        } catch(NullObjectException e) {
            // success
        }

        periodSdValue.put(SDHolder.getSD(10, 9), 20L);
        periodSdValue.put(SDHolder.getSD(20, 9), 20L);
        periodSdValue.put(SDHolder.getSD(30, 10), 20L);
        periodSdValue.put(SDHolder.getSD(40, 9), 20L);
        bean = new PriceListBean(1, "TestName", "TestLable", periodSdValue, owner);
        try {
            manager.setObject(bean);
            fail("Should have thrown an exception");
        } catch(PersistenceException pe) {
            // success
        }
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(PriceListBean bean : manager.getAllObjects()) {
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
    private void testOrder(int[] order, List<PriceListBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}
