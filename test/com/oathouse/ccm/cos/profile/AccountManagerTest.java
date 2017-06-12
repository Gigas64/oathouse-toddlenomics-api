/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.profile;

import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import java.util.HashMap;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class AccountManagerTest {

    private String owner;
    private String rootStorePath;
    private AccountManager manager;

    public AccountManagerTest() {
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
        manager = new AccountManager("AccountManager");
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
    public void system01_Account() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        AccountBean bean1 = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        AccountBean bean2 = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(bean1);
        manager.setObject(bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new AccountManager("AccountManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_Account() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.profile.AccountBean", false);
    }

    /**
     * Unit test: Tests the sets
     */
    @Test
    public void testAllSetValues() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        AccountBean bean01 = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), id, fieldSet);
        manager.setObject(bean01);
        manager.setObjectAccountRef(id, "ref", owner);
        assertEquals("ref", manager.getObject(id).getAccountRef());
        manager.setObjectContract(id, true, owner);
        assertTrue(manager.getObject(id).isContract());
        manager.setObjectContract(id, false, owner);
        assertFalse(manager.getObject(id).isContract());
        manager.setObjectFixedItemDiscountRate(id, 25, owner);
        assertEquals(25, manager.getObject(id).getFixedItemDiscountRate());
        manager.setObjectStatus(id, AccountStatus.ACTIVE, owner);
        assertEquals(AccountStatus.ACTIVE, manager.getObject(id).getStatus());
        manager.setObjectPaymentPeriod(id, PaymentPeriodEnum.MONTHLY, "ABC", 100L, owner);
        assertEquals(PaymentPeriodEnum.MONTHLY, manager.getObject(id).getPaymentPeriod());
        assertEquals("ABC", manager.getObject(id).getRegularPaymentInstruction());
        assertEquals(100, manager.getObject(id).getPaymentPeriodValue());
    }


    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(AccountBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }
}