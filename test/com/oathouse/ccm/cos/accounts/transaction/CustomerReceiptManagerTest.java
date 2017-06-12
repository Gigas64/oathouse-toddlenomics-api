/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import static org.junit.Assert.*;
/**
 *
 * @author Darryl Oatridge
 */
public class CustomerReceiptManagerTest {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private final int accountId = 13;
    private CustomerReceiptManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new CustomerReceiptManager("CustomerReceiptManager");
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
        CustomerReceiptBean bean1 = (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed, fieldSet);
        fieldSet.put("id", "2");
        CustomerReceiptBean bean2 = (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(accountId, bean1);
        manager.setObject(accountId, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        manager = null;
        manager = new CustomerReceiptManager("CustomerReceiptManager");
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(accountId, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    @Test
    public void testBean() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean", false);
    }

    /*
     *
     */
    @Test
    public void testGetAllVoid() throws Exception {
        setCustomerReceipt();
        List<CustomerReceiptBean> result = manager.getAllVoidCustomerReceipts(accountId);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getPaymentType(), is(PaymentType.ADMIN_VOID));
    }

    /*
     *
     */
    @Test
    public void testGetAllOverrides() throws Exception {
        setCustomerReceipt();
        List<CustomerReceiptBean> result = manager.getAllObjects(accountId);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getPaymentType(), is(PaymentType.BANK_TRANSFER));
        assertThat(result.get(1).getPaymentType(), is(PaymentType.CREDITCARD));

        result = manager.getAllObjects();
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getPaymentType(), is(PaymentType.BANK_TRANSFER));
        assertThat(result.get(1).getPaymentType(), is(PaymentType.CREDITCARD));
        assertThat(result.get(2).getPaymentType(), is(PaymentType.CREDITCARD));


    }


    //<editor-fold defaultstate="collapsed" desc="private">
    private void setCustomerReceipt() throws Exception {
        int altAccountId = 17;
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(1));
        fieldSet.put("paymentType", PaymentType.BANK_TRANSFER.toString());
        manager.setObject(accountId, (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(2));
        fieldSet.put("paymentType", PaymentType.ADMIN_VOID.toString());
        manager.setObject(accountId, (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("paymentType", PaymentType.CREDITCARD.toString());
        manager.setObject(accountId, (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed++, fieldSet));
        // add a control alternative account id as key
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("paymentType", PaymentType.CREDITCARD.toString());
        manager.setObject(altAccountId, (CustomerReceiptBean) BeanBuilder.addBeanValues(new CustomerReceiptBean(), seed++, fieldSet));

    }
    //</editor-fold>
}