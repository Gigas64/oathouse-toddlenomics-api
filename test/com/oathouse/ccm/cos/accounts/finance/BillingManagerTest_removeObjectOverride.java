/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.accounts.YwdOrderedTypeComparator;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author Darryl Oatridge
 */
public class BillingManagerTest_removeObjectOverride {

    private String owner;
    private String rootStorePath;
    private BillingManager manager;

    private int accountId = 99;
    private int ywd = CalendarStatic.getRelativeYWD(-2);
    private int taxRate = 150;
    private int profileId01 = 1;
    private int profileId02 = 2;
    private int profileId03 = 4;
    private int bookingId01 = 101;
    private int bookingId02 = 102;
    private int bookingId03 = 103;
    private int bookingId04 = 104;

    public BillingManagerTest_removeObjectOverride() {
        owner = ObjectBean.SYSTEM_OWNED;
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
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new BillingManager(VT.BILLING.manager());
        manager.init();
    }

    /*
     *
     */
    @Test
    public void test01_removeObject() throws Exception {
        int invoiceId = 13;

        setBilling();
        manager.setObjectInvoiceId(accountId, 1, invoiceId, owner);
        assertThat(manager.getAllObjects(accountId).size(), is(5));
        manager.removeObject(accountId, 2);
        assertThat(manager.getAllObjects(accountId).size(), is(4));
        try {
            manager.removeObject(accountId, 1);
            fail("Should throw an exception");
        } catch(PersistenceException persistenceException) {
            // success
        }
        // does nothing as no id 22
        manager.removeObject(accountId, 22);
    }

   /*
     *
     */
    @Test
    public void removeObject_withDiscount() throws Exception {
        setBilling();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_CHILD_DISCOUNT, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_FIXED_ITEM);
        fieldSet.put("id", Integer.toString(manager.generateIdentifier()));
        fieldSet.put("billingBits", Integer.toString(billingBits));
        fieldSet.put("discountId", Integer.toString(-1));
        fieldSet.put("invoiceId", Integer.toString(-1));
        int childDiscountId = manager.setObject(accountId, (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet)).getBillingId();
        billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_FIXED_ITEM);
        fieldSet.put("id", Integer.toString(manager.generateIdentifier()));
        fieldSet.put("billingBits", Integer.toString(billingBits));
        fieldSet.put("discountId", Integer.toString(childDiscountId));
        int fixedItemId = manager.setObject(accountId, (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet)).getBillingId();
        assertThat(manager.isIdentifier(accountId, fixedItemId), is(true));
        assertThat(manager.isIdentifier(accountId, childDiscountId), is(true));
        manager.removeObject(accountId, fixedItemId);
        assertThat(manager.isIdentifier(accountId, fixedItemId), is(false));
        assertThat(manager.isIdentifier(accountId, childDiscountId), is(false));
    }

    //<editor-fold defaultstate="collapsed" desc="Set Billing Private Methods">
    private void setBilling() throws Exception {
        int billingId = 1;
        int periodSd = SDHolder.getSD(10, 19);
        int btChargeBit = BTBits.STANDARD_CHARGE_BIT;
        int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        long value = 400;
        int invoiceId = -1;
        String description = "description";
        String notes = "notes";

        // child 1
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
        // child 1
        billingId++;
        periodSd = SDHolder.getSD(30, 20);
        value = 500;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));

        //child 2
        billingId++;
        periodSd = SDHolder.getSD(10, 19);
        value = 600;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId02, profileId02, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
        // child 3
        billingId++;
        value = 700;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId03, profileId03, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));

        //child 1 previous day
        int anotherYwd = CalendarStatic.getRelativeYWD(-1);
        billingId++;
        value = 800;
        manager.setObject(accountId, new BillingBean(billingId, accountId, anotherYwd, periodSd, value, taxRate,  bookingId04, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));

    }

    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<BillingBean> list) throws Exception {
        assertEquals("Testing manager size",order.length,list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]",order[i], list.get(i).getIdentifier());
        }
    }

    //</editor-fold>

}
