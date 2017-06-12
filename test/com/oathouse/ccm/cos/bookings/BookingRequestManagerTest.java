/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.bookings;

// common imports
import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.asSet;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import java.io.File;
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
 * @author Darryl Oatridge
 */
public class BookingRequestManagerTest {

    private String owner;
    private String rootStorePath;
    private BookingRequestManager manager;
    private int accountId = 11;

    public BookingRequestManagerTest() {
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
        manager = new BookingRequestManager("BookingRequestManager");
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
    public void system01_BookingRequest() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        BookingRequestBean bean1 = (BookingRequestBean) BeanBuilder.addBeanValues(new BookingRequestBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        BookingRequestBean bean2 = (BookingRequestBean) BeanBuilder.addBeanValues(new BookingRequestBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(accountId,bean1);
        manager.setObject(accountId,bean2);
        assertEquals(2, manager.getAllIdentifier(accountId).size());
        manager = null;
        manager = new BookingRequestManager("BookingRequestManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier(accountId).size());
        assertEquals(bean1, manager.getObject(accountId, 1));
        assertEquals(bean2, manager.getObject(accountId, 2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_BookingRequest() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.bookings.BookingRequestBean", false);
    }

    /*
     *
     */
    @Test
    public void testHasBookingRequestValues() throws Exception {
        BookingRequestBean test = new BookingRequestBean(1, "name", "label", accountId, -1, -1, -1, asSet(3,5,7), BTIdBits.ATTENDING_STANDARD, owner);
        BookingRequestBean controlpSd = new BookingRequestBean(2, "name", "label", accountId, -1, -1, -1, asSet(3,8,7), BTIdBits.ATTENDING_STANDARD, owner);
        BookingRequestBean controlType = new BookingRequestBean(3, "name", "label", accountId, -1, -1, -1, asSet(3,5,7), BTIdBits.ATTENDING_SPECIAL, owner);
        manager.setObject(accountId, test);
        manager.setObject(accountId, controlpSd);
        manager.setObject(accountId, controlType);

        assertThat(manager.hasBookingRequestValues(accountId, asSet(3,5,7), BTIdBits.ATTENDING_STANDARD), is(true));
        assertThat(manager.hasBookingRequestValues(accountId, asSet(3,8,7), BTIdBits.ATTENDING_SPECIAL), is(false));
        assertThat(manager.hasBookingRequestValues(accountId, asSet(3,5,7), BTIdBits.ATTENDING_SPECIAL), is(true));
        assertThat(manager.hasBookingRequestValues(accountId, asSet(3,7), BTIdBits.ATTENDING_STANDARD), is(false));

    }

    /**
     * Unit test: test sett
     */
    @Test
    public void unit02_BookingRequest() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        BookingRequestBean bean01 = (BookingRequestBean) BeanBuilder.addBeanValues(new BookingRequestBean(), id, fieldSet);
        manager.setObject(11, bean01);
        manager.setObjectBookingTypeId(accountId, id, 1, owner);
        assertEquals(1, manager.getObject(id).getBookingTypeId());
    }


    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(BookingRequestBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }
}
