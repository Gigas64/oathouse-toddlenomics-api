/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
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
public class FixedChargeManagerTest {

    private String owner;
    private FixedChargeManager manager;

    public FixedChargeManagerTest() {
        owner = "tester";
    }

    @Before
    public void setUp() throws Exception {
        manager = new FixedChargeManager("FixedChargeManager");
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
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", "1");
        fieldSet.put("owner", owner);
        FixedChargeBean bean1 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), seed, fieldSet);
        fieldSet.put("id", "2");
        FixedChargeBean bean2 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), seed, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new FixedChargeManager("FixedChargeManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.invoice.FixedChargeBean", false);
    }

    /**
     * Test of getOutstandingFixedCharges method, of class FixedChargeManager.
     */
    @Test
    public void testGetOutstandingFixedCharges() throws Exception {
        int accountId = 99;
        int invoiceId = ObjectEnum.INITIALISATION.value();
        HashMap<String, String> fieldSet = new HashMap<String, String>();
        fieldSet.put("id", "1");
        fieldSet.put("invoiceId", Integer.toString(invoiceId));
        fieldSet.put("accountId", Integer.toString(accountId));
        fieldSet.put("preAdjustmentValue", Integer.toString(100));
        FixedChargeBean bean1 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), fieldSet);
        fieldSet.put("id", "2");
        fieldSet.put("preAdjustmentValue", Integer.toString(20));
        FixedChargeBean bean2 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), fieldSet);
        fieldSet.put("id", "3");
        fieldSet.put("preAdjustmentValue", Integer.toString(35));
        FixedChargeBean bean3 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), fieldSet);
        manager.setObject(accountId, bean1);
        manager.setObject(accountId, bean2);
        manager.setObject(accountId, bean3);

        assertEquals(3, manager.getOutstandingFixedCharges(accountId).size());
        assertEquals(bean1, manager.getOutstandingFixedCharges(accountId).get(0));
        assertEquals(bean2, manager.getOutstandingFixedCharges(accountId).get(1));
        assertEquals(bean3, manager.getOutstandingFixedCharges(accountId).get(2));

        invoiceId = 101;

        fieldSet.put("id", "4");
        fieldSet.put("invoiceId", Integer.toString(invoiceId));
        FixedChargeBean bean4 = (FixedChargeBean) BeanBuilder.addBeanValues(new FixedChargeBean(), fieldSet);
        manager.setObject(accountId, bean4);
        assertEquals(3, manager.getOutstandingFixedCharges(accountId).size());
        assertEquals(bean1, manager.getOutstandingFixedCharges(accountId).get(0));
        assertEquals(bean2, manager.getOutstandingFixedCharges(accountId).get(1));
        assertEquals(bean3, manager.getOutstandingFixedCharges(accountId).get(2));
        assertEquals(35, manager.getInvoiceCharges(accountId, invoiceId)[1]);


        manager.setInvoiceId(accountId, bean1.getFixedChargeId(), invoiceId, 10, 2, owner);
        assertEquals(2, manager.getOutstandingFixedCharges(accountId).size());
        assertEquals(bean2, manager.getOutstandingFixedCharges(accountId).get(0));
        assertEquals(bean3, manager.getOutstandingFixedCharges(accountId).get(1));
        assertEquals(135, manager.getInvoiceCharges(accountId, invoiceId)[1]);

        manager.setInvoiceId(accountId, bean3.getFixedChargeId(), invoiceId, 10, 2, owner);
        assertEquals(1, manager.getOutstandingFixedCharges(accountId).size());
        assertEquals(bean2, manager.getOutstandingFixedCharges(accountId).get(0));
        assertEquals(170, manager.getInvoiceCharges(accountId, invoiceId)[1]);


        assertEquals(3, manager.getAllObjectsForInvoice(accountId, invoiceId).size());
        assertEquals(bean1, manager.getAllObjectsForInvoice(accountId, invoiceId).get(0));
        assertEquals(bean3, manager.getAllObjectsForInvoice(accountId, invoiceId).get(1));
        assertEquals(bean4, manager.getAllObjectsForInvoice(accountId, invoiceId).get(2));

    }
}