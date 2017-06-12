/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.objectstore.BeanBuilder;
import java.util.HashMap;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceCreditManagerTest {

    private String owner;
    private InvoiceCreditManager manager;

    public InvoiceCreditManagerTest() {
        owner = "tester";
    }

    @Before
    public void setUp() throws Exception {
        manager = new InvoiceCreditManager("InvoiceCreditManager");
        manager.init();

    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void testManager() throws Exception {
        // create bean
        int id = 1;
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", Integer.toString(id++));
        fieldSet.put("owner", owner);
        InvoiceCreditBean bean1 = (InvoiceCreditBean) BeanBuilder.addBeanValues(new InvoiceCreditBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(id++));
        InvoiceCreditBean bean2 = (InvoiceCreditBean) BeanBuilder.addBeanValues(new InvoiceCreditBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new InvoiceCreditManager("InvoiceCreditManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.transaction.InvoiceCreditBean", false);
    }

}
