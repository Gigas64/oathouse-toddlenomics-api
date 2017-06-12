/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BookingForecastManagerTest_defaultTests {

    private String owner = ObjectBean.SYSTEM_OWNED;
    private String rootStorePath;
    private BookingForecastManager manager;

    private int accountId = 11;
    private int profileId = 13;
    private long bookingModified = -1;
    private int today = CalendarStatic.getRelativeYWD(0);

    public BookingForecastManagerTest_defaultTests() {
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
        manager = new BookingForecastManager();
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
    public void system01_ForecastBilling() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        BookingForecastBean bean1 = (BookingForecastBean) BeanBuilder.addBeanValues(new BookingForecastBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        BookingForecastBean bean2 = (BookingForecastBean) BeanBuilder.addBeanValues(new BookingForecastBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
    }

    /*
     *
     */
    @Test
    public void getRangeBookingForecast_forRange() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(1);
        int altAccountId = 31;
        long value = 100;
        long valueIncTax = 150;


        manager.setObject(ywd, new BookingForecastBean(1, accountId, ywd, value, valueIncTax, profileId, bookingModified, owner));
        manager.setObject(ywd, new BookingForecastBean(2, accountId, ywd, value, valueIncTax, profileId, bookingModified, owner));
        manager.setObject(ywd, new BookingForecastBean(3, altAccountId, ywd, value, valueIncTax, profileId, bookingModified, owner));
        assertThat(manager.getAllObjects(ywd).size(), is(3));
        assertThat(manager.getAllForecastsForAccount(ywd, accountId).size(), is(2));

    }

    /*
     *
     */
    @Test
    public void getRangeBookingForecast_removeOld() throws Exception {
        int oldYw0 = CalendarStatic.getRelativeYW(-1);
        int yw0 = CalendarStatic.getRelativeYW(1);
        long value = 100;
        long valueIncTax = 150;
        manager.setObject(oldYw0 + 0, new BookingForecastBean(1, accountId, oldYw0 + 0, value, valueIncTax, profileId, bookingModified, owner));
        manager.setObject(oldYw0 + 2, new BookingForecastBean(2, accountId, oldYw0 + 2, value, valueIncTax, profileId, bookingModified, owner));
        manager.setObject(yw0 + 3, new BookingForecastBean(3, accountId, yw0 + 3, value, valueIncTax, profileId, bookingModified, owner));
        manager.setObject(yw0 + 5, new BookingForecastBean(4, accountId, yw0 + 5, value, valueIncTax, profileId, bookingModified, owner));
        assertThat(manager.getAllObjects().size(), is(4));

        manager.doHousekeeping(today);
        assertThat(manager.getAllObjects().size(), is(2));

    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_ForecastBilling() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.finance.BookingForecastBean", false);
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(BookingForecastBean bean : manager.getAllObjects()) {
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
    private void testOrder(int[] order, List<BookingForecastBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}