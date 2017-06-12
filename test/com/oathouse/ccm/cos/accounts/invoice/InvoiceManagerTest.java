/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.accounts.invoice;

import java.util.HashMap;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceManagerTest {

    private String owner;
    private InvoiceManager manager;

    public InvoiceManagerTest() {
        owner = "tester";
    }

    @Before
    public void setUp() throws Exception {
        manager = new InvoiceManager("InvoiceManager");
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    @Test
    public void testManager() throws Exception {
        // create bean
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", "1");
        fieldSet.put("owner", owner);
        InvoiceBean bean1 = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), seed, fieldSet);
        fieldSet.put("id", "2");
        InvoiceBean bean2 = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), seed, fieldSet);

        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new InvoiceManager("InvoiceManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1,manager.getObject(99, 1));
        assertEquals(bean2,manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.invoice.InvoiceBean", false);
    }
}