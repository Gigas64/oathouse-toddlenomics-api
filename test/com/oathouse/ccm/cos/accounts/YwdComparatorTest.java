/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts;

// common imports
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean;
import com.oathouse.ccm.cos.accounts.transaction.PaymentBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.io.File;
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
public class YwdComparatorTest {

    @Test
    public void singleBeans() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int billingSd = 1;
        int accountId = 1;
        int invoiceId = 1;

        List<ObjectBean> testList = new LinkedList<>();
        testList.add(getPayment(1, ywd, accountId));
        testList.add(getBilling(2, ywd, billingSd));
        testList.add(getReceipt(3, ywd, accountId));
        testList.add(getInvoice(4, ywd, invoiceId));

        int[] billOrder = {1,2,3,4};
        testOrder(billOrder, testList);
        Collections.sort(testList, new YwdOrderedTypeComparator());
        int[] testOrder = {2,3,4,1};
        testOrder(testOrder, testList);
    }

    @Test
    public void multipleVariable() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int billingSd = 1;
        int accountId = 1;
        int invoiceId = 1;

        List<ObjectBean> testList = new LinkedList<>();
        testList.add(getPayment(1, ywd, 5));
        testList.add(getBilling(2, ywd, 2));
        testList.add(getReceipt(3, ywd, 9));
        testList.add(getInvoice(4, ywd, 2));
        testList.add(getBilling(5, ywd, 5));
        testList.add(getReceipt(6, ywd, 4));
        testList.add(getBilling(7, ywd, 1));
        testList.add(getReceipt(8, ywd, 3));
        testList.add(getPayment(9, ywd, 3));

        int[] billOrder = {1,2,3,4,5,6,7,8,9};
        testOrder(billOrder, testList);
        Collections.sort(testList, new YwdOrderedTypeComparator());
        int[] testOrder = {7,2,5,8,6,3,4,9,1};
        testOrder(testOrder, testList);
    }

    /*
     *
     */
    @Test
    public void multipleYwd() throws Exception {
        int yw0 = CalendarStatic.getRelativeYW(0);
        int yw1 = yw0 + 1;
        int billingSd = 1;
        int accountId = 1;
        int invoiceId = 1;

        List<ObjectBean> testList = new LinkedList<>();
        testList.add(getPayment(1, yw1, accountId));
        testList.add(getBilling(2, yw0, billingSd));
        testList.add(getReceipt(3, yw0, accountId));
        testList.add(getInvoice(4, yw1, invoiceId));
        testList.add(getPayment(5, yw0, accountId));

        int[] billOrder = {1,2,3,4,5};
        testOrder(billOrder, testList);
        Collections.sort(testList, new YwdOrderedTypeComparator());
        int[] testOrder = {2,3,5,4,1};
        testOrder(testOrder, testList);
    }



    //<editor-fold defaultstate="collapsed" desc="private">
    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<ObjectBean> list) throws Exception {
        assertEquals("Testing manager size",order.length,list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]",order[i], list.get(i).getIdentifier());
        }
    }

    private PaymentBean getPayment(int id, int ywd, int accountId) throws Exception {
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("accountId", Integer.toString(accountId));
        PaymentBean payment = (PaymentBean) BeanBuilder.addBeanValues(new PaymentBean(), id++, fieldSet);
        return payment;
    }

    private InvoiceBean getInvoice(int id, int ywd, int invoiceId) throws Exception {
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("invoiceId", Integer.toString(invoiceId));
        InvoiceBean invoice = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), id++, fieldSet);
        return invoice;
    }

    private BillingBean getBilling(int id, int ywd, int billingSd) throws Exception {
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("billingSd", Integer.toString(billingSd));
        BillingBean billing = (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), id++, fieldSet);
        return billing;
    }

    private CustomerReceiptBean getReceipt(int id, int ywd, int accountId) throws Exception {
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(id));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("accountId", Integer.toString(accountId));
        CustomerReceiptBean customerReceipt = (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), id++, fieldSet);
        return customerReceipt;
    }
    //</editor-fold>
}
