/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.exceptions.DuplicateIdentifierException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import java.util.concurrent.ConcurrentSkipListSet;
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
public class CustomerCreditManagerTest {

    private String owner;
    private CustomerCreditManager manager;

    public CustomerCreditManagerTest() {
        owner = "tester";
    }

    @Before
    public void setUp() throws Exception {
        manager = new CustomerCreditManager("CustomerCreditManager");
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
        CustomerCreditBean bean1 = (CustomerCreditBean) BeanBuilder.addBeanValues(new CustomerCreditBean(), seed, fieldSet);
        fieldSet.put("id", "2");
        CustomerCreditBean bean2 = (CustomerCreditBean) BeanBuilder.addBeanValues(new CustomerCreditBean(), seed, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(99, bean1);
        manager.setObject(99, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new CustomerCreditManager("CustomerCreditManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(99, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.transaction.CustomerCreditBean", false);
    }

    @Test
    public void testSetCredit() throws Exception {
        int accountId = 99;
        int[] invoiceId = {3, 5, 7, 11};
        int[] receiptId = {23, 25, 27, 31, 33};

        ConcurrentSkipListSet<Integer> invoiceSet = new ConcurrentSkipListSet<Integer>();
        ConcurrentSkipListSet<Integer> receiptSet = new ConcurrentSkipListSet<Integer>();

        // add receipts
        manager.setCustomerCredit(10, accountId, receiptId[0], 100, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(1, manager.getAllObjects(accountId).size());
        assertEquals(0, manager.getDebitBalance(accountId));
        assertEquals(100, manager.getCreditBalance(accountId));

        manager.setCustomerDebit(11, accountId, invoiceId[0], 120, CustomerCreditType.INVOICE_DEBIT, owner);
        assertEquals(2, manager.getAllObjects(accountId).size());
        assertEquals(20, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));

        manager.setCustomerCredit(12, accountId, receiptId[1], 100, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(3, manager.getAllObjects(accountId).size());
        assertEquals(0, manager.getDebitBalance(accountId));
        assertEquals(80, manager.getCreditBalance(accountId));

        manager.setCustomerDebit(13, accountId, invoiceId[1], 80, CustomerCreditType.INVOICE_DEBIT, owner);
        assertEquals(3, manager.getAllObjects(accountId).size());
        assertEquals(0, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));

        assertEquals(0, manager.getOutstandingCreditId(accountId).size());




    }

    @Test
    public void testSetDebit() throws Exception {
        int accountId = 99;
        int[] invoiceId = {3, 5, 7, 11};
        int[] receiptId = {23, 25, 27, 31, 33};

        ConcurrentSkipListSet<Integer> invoiceSet = new ConcurrentSkipListSet<Integer>();
        ConcurrentSkipListSet<Integer> receiptSet = new ConcurrentSkipListSet<Integer>();

        // single invoice added
        invoiceSet.add(invoiceId[0]);
        manager.setCustomerDebit(10, accountId, invoiceId[0], 60, CustomerCreditType.INVOICE_DEBIT, owner);
        assertEquals(1, manager.getAllObjects(accountId).size());
        assertEquals(60, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));
        assertEquals(60, manager.getInvoiceBalance(accountId, invoiceId[0]));
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));

        try {
            manager.setCustomerDebit(10, accountId, invoiceId[0], 20, CustomerCreditType.INVOICE_DEBIT, owner);
            fail("should have tyhrown an exception");
        } catch(DuplicateIdentifierException e) {
            // success
        }
        // two invoices added
        invoiceSet.add(invoiceId[1]);
        manager.setCustomerDebit(11, accountId, invoiceId[1], 80, CustomerCreditType.INVOICE_DEBIT, owner);
        assertEquals(2, manager.getAllObjects(accountId).size());
        assertEquals(140, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));
        assertEquals(60, manager.getInvoiceBalance(accountId, invoiceId[0]));
        assertEquals(80, manager.getInvoiceBalance(accountId, invoiceId[1]));
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));

        // add receipts
        receiptSet.add(receiptId[0]);
        manager.setCustomerCredit(12, accountId, receiptId[0], 20, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(3, manager.getAllObjects(accountId).size());
        assertEquals(120, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));
        assertEquals(40, manager.getInvoiceBalance(accountId, invoiceId[0]));
        assertEquals(80, manager.getInvoiceBalance(accountId, invoiceId[1]));
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));

        receiptSet.add(receiptId[1]);
        manager.setCustomerCredit(13, accountId, receiptId[1], 40, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(3, manager.getAllObjects(accountId).size());
        assertEquals(80, manager.getDebitBalance(accountId));
        assertEquals(0, manager.getCreditBalance(accountId));
        assertEquals(0, manager.getInvoiceBalance(accountId, invoiceId[0]));
        assertEquals(80, manager.getInvoiceBalance(accountId, invoiceId[1]));
        invoiceSet.remove(invoiceId[0]);
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));

        receiptSet.add(receiptId[2]);
        manager.setCustomerCredit(14, accountId, receiptId[2], 100, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(4, manager.getAllObjects(accountId).size());
        assertEquals(0, manager.getDebitBalance(accountId));
        assertEquals(20, manager.getCreditBalance(accountId));
        assertEquals(0, manager.getInvoiceBalance(accountId, invoiceId[0]));
        assertEquals(0, manager.getInvoiceBalance(accountId, invoiceId[1]));
        invoiceSet.remove(invoiceId[1]);
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));
    }

    @Test
    public void testGetsForDebit() throws Exception {
        int accountId = 99;
        int[] debitId = {3, 5, 7, 11};
        int[] creditId = {23, 25, 27, 31, 33};
        ConcurrentSkipListSet<Integer> invoiceSet = new ConcurrentSkipListSet<Integer>();
        ConcurrentSkipListSet<Integer> paymentSet = new ConcurrentSkipListSet<Integer>();
        ConcurrentSkipListSet<Integer> receiptSet = new ConcurrentSkipListSet<Integer>();
        ConcurrentSkipListSet<Integer> invCreditSet = new ConcurrentSkipListSet<Integer>();

        invoiceSet.add(debitId[0]);
        invoiceSet.add(debitId[1]);
        invoiceSet.add(debitId[2]);
        paymentSet.add(debitId[0]);
        manager.setCustomerDebit(10, accountId, debitId[0], 50, CustomerCreditType.INVOICE_DEBIT, owner);
        manager.setCustomerDebit(10, accountId, debitId[1], 60, CustomerCreditType.INVOICE_DEBIT, owner);
        manager.setCustomerDebit(10, accountId, debitId[2], 70, CustomerCreditType.INVOICE_DEBIT, owner);
        // payment debit
        manager.setCustomerDebit(10, accountId, debitId[0], 20, CustomerCreditType.PAYMENT_DEBIT, owner);

        assertEquals(50, manager.getInvoiceBalance(accountId, debitId[0]));
        assertEquals(200, manager.getDebitBalance(accountId));
        assertEquals(invoiceSet, manager.getOutstandingInvoiceId(accountId));
    }

    @Test
    public void testSetCustomerCreditWithPriority() throws Exception {
        int accountId = 99;
        int[] debitId = {3, 5, 7, 11};
        int[] creditId = {23, 25, 27, 31, 33};

        manager.setCustomerDebit(10, accountId, debitId[0], 50, CustomerCreditType.INVOICE_DEBIT, owner);
        manager.setCustomerDebit(10, accountId, debitId[1], 60, CustomerCreditType.INVOICE_DEBIT, owner);
        manager.setCustomerDebit(10, accountId, debitId[2], 70, CustomerCreditType.INVOICE_DEBIT, owner);
        assertEquals(70, manager.getInvoiceBalance(accountId, debitId[2]));
        assertEquals(180, manager.getDebitBalance(accountId));
        manager.setCustomerCreditWithPriority(12, accountId, creditId[0], debitId[2], 70, CustomerCreditType.RECEIPT_CREDIT, owner);
        assertEquals(0, manager.getInvoiceBalance(accountId, debitId[2]));
        assertEquals(110, manager.getDebitBalance(accountId));
        assertFalse(manager.getOutstandingInvoiceId(accountId).contains(debitId[2]));

    }

    private void printAll() throws Exception {
        for(CustomerCreditBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }
/**/
}
