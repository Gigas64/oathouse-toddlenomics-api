package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cma.ApplicationConstants;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author Darryl Oatridge
 */
public class BillingManagerTest_managerMethods {

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
    private int discountId = 23;
    private int adjustmentId = 29;


    public BillingManagerTest_managerMethods() {
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

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * System test: ' is working ' system level test.
     */
    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void system01_BillingItem() throws Exception {
        // create bean
        int id = 0;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(++id));
        fieldSet.put("owner", owner);
        fieldSet.put("accountId", Integer.toString(accountId));
        BillingBean bean1 = (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), id, fieldSet);
        fieldSet.put("id", Integer.toString(++id));
        BillingBean bean2 = (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), id, fieldSet);
        // test manager
        assertEquals(0, manager.getAllIdentifier().size());
        manager.setObject(accountId, bean1);
        manager.setObject(accountId, bean2);
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(accountId, 1));
        assertEquals(bean2, manager.getObject(accountId, 2));
        manager = null;
        manager = new BillingManager(VT.BILLING.manager());
        manager.init();
        assertEquals(2, manager.getAllIdentifier().size());
        assertEquals(bean1, manager.getObject(accountId, 1));
        assertEquals(bean2, manager.getObject(2));
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_BillingItem() throws Exception {
        BuildBeanTester.testObjectBean(VT.BILLING.qualifiedBean(), false);
    }

    /*
     *
     */
    @Test
    public void test01_getAllObjectsForYwd() throws Exception {
        setBilling();
        // check all is as we assume
        assertThat(manager.getAllObjects().size(), is(6));

        int chargeBits = BTBits.STANDARD_CHARGE_BIT;
        BillingEnum [] billingBits = {BillingEnum.TYPE_SESSION, BillingEnum.CALC_AS_VALUE};

        assertThat(manager.getYwdBilling(accountId, ywd, chargeBits, billingBits).size(), is(4));
        assertThat(manager.getYwdBilling(accountId, ywd, chargeBits).size(), is(5));
        assertThat(manager.getYwdBilling(accountId, CalendarStatic.getRelativeYWD(-1), chargeBits, billingBits).size(), is(1));
        assertThat(manager.getYwdBilling(accountId, CalendarStatic.getRelativeYWD(0), chargeBits, billingBits).size(), is(0));

        BillingEnum [] altBillingBits = {BillingEnum.TYPE_FIXED_ITEM, BillingEnum.CALC_AS_VALUE};
        assertThat(manager.getYwdBilling(accountId, ywd, chargeBits, altBillingBits).size(), is(0));
    }

    /*
     *
     */
    @Test
    public void test01_getAllObjectsForYwdAndProfile() throws Exception {
        setBilling();
        // check all is as we assume
        assertThat(manager.getAllObjects().size(), is(6));
        int chargeBits = BTBits.STANDARD_CHARGE_BIT;
        BillingEnum [] billingBits = {BillingEnum.TYPE_SESSION, BillingEnum.CALC_AS_VALUE};


        assertThat(manager.getYwdBillingForProfile(accountId, ywd, profileId01, chargeBits, billingBits).size(), is(2));
        assertThat(manager.getYwdBillingForProfile(accountId, ywd, profileId01, chargeBits).size(), is(2));
        assertThat(manager.getYwdBillingForProfile(accountId, ywd, profileId02, chargeBits, billingBits).size(), is(1));
        assertThat(manager.getYwdBillingForProfile(accountId, ywd, profileId03, chargeBits, billingBits).size(), is(1));
        int anotherYwd = CalendarStatic.getRelativeYWD(-1);
        assertThat(manager.getYwdBillingForProfile(accountId, anotherYwd, profileId01, chargeBits, billingBits).size(), is(1));
        int anotherProfileId = 54;
        assertThat(manager.getYwdBillingForProfile(accountId, ywd, anotherProfileId, chargeBits, billingBits).size(), is(0));
    }

    /*
     *
     */
    @Test
    public void test01_getAllBillingForBooking() throws Exception {
        setBilling();

        int chargeBits = BTBits.STANDARD_CHARGE_BIT;
        BillingEnum [] billingBits = {BillingEnum.TYPE_SESSION, BillingEnum.CALC_AS_VALUE};

        assertThat(manager.getAllBillingForBooking(bookingId01, chargeBits, billingBits).size(), is(2));
        assertThat(manager.getAllBillingForBooking(bookingId02, chargeBits, billingBits).size(), is(1));
    }

    /*
     *
     */
    @Test
    public void test01_setObjectNotes() throws Exception {
        String notes = "test";
        setBilling();

        manager.setObjectNotes(accountId, 1, notes, owner);
        assertThat(manager.getObject(accountId, 1).getNotes(), is(notes));
        assertThat(manager.getObject(accountId, 1).getAccountId(), is(accountId));
        assertThat(manager.getObject(accountId, 1).getNotes(), is(notes));

    }

    /*
     *
     */
    @Test
    public void test01_setObjectInvoiceId() throws Exception {
        int invoiceId = 11;
        setBilling();

        manager.setObjectInvoiceId(accountId, 1, invoiceId, owner);
        assertThat(manager.getObject(accountId, 1).getInvoiceId(), is(invoiceId));
        assertThat(manager.getObject(accountId, 1).getAccountId(), is(accountId));
        assertThat(manager.getObject(accountId, 1).getInvoiceId(), is(invoiceId));
    }

    /*
     *
     */
    @Test
    public void test01_getAllBillingOutstanding() throws Exception {
        setBilling();
        int invoiceId = 13;
        assertThat(manager.getAllBillingOutstanding(accountId, CalendarStatic.getRelativeYWD(-3)).size(), is(0));
        assertThat(manager.getAllBillingOutstanding(accountId, ywd).size(), is(5));
        assertThat(manager.getAllBillingOutstanding(accountId, CalendarStatic.getRelativeYWD(-1)).size(), is(6));
        manager.setObjectInvoiceId(accountId, 1, invoiceId, owner);
        manager.setObjectInvoiceId(accountId, 3, invoiceId, owner);
        manager.setObjectInvoiceId(accountId, 4, invoiceId, owner);
        assertThat(manager.getAllBillingOutstanding(accountId, CalendarStatic.getRelativeYWD(-3)).size(), is(0));
        assertThat(manager.getAllBillingOutstanding(accountId, ywd).size(), is(2));
        assertThat(manager.getAllBillingOutstanding(accountId, ywd).get(0).getBillingId(), is(2));
        assertThat(manager.getAllBillingOutstanding(accountId, ywd).get(1).getBillingId(), is(discountId));
        int today = CalendarStatic.getRelativeYWD(0);
        assertThat(manager.getAllBillingOutstanding(accountId, today).size(), is(3));
        assertThat(manager.getAllBillingOutstanding(accountId, today).get(0).getBillingId(), is(2));
        assertThat(manager.getAllBillingOutstanding(accountId, today).get(1).getBillingId(), is(5));
        assertThat(manager.getAllBillingOutstanding(accountId, today).get(2).getBillingId(), is(discountId));
    }

    /*
     * test the YwdOrderedTypeComparator for BillingBean objects
     */
    @Test
    public void test01_YwdCompartor() throws Exception {
        setBilling();
        List<BillingBean> billingList = manager.getAllObjects();
        int[] billOrder = {1,2,3,4,5,23};
        testOrder(billOrder, billingList);
        Collections.sort(billingList, new YwdOrderedTypeComparator());
        int[] testOrder = {1,3,4,23,2,5};
        testOrder(testOrder, billingList);
    }

    /*
     *
     */
    @Test
    public void getDiscountBillingForFixedItem() throws Exception {
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

        assertThat(manager.getDiscountBilling(accountId, fixedItemId).getBillingId(), is(childDiscountId));
    }

    /*
     *
     */
    @Test
    public void getSignedTotalValue() throws Exception {
        setBilling();
        long result = manager.getSignedTotalValue(accountId, -1, -1);
        assertThat(result, is(2940L));
        result = manager.getSignedTotalValue(accountId, -1, 13);
        assertThat(result, is(0L));
        result = manager.getSignedTotalValue(accountId, 31, 13);
        assertThat(result, is(0L));
        result = manager.getSignedTotalValue(accountId, bookingId01, -1);
        assertThat(result, is(900L));
        result = manager.getSignedTotalValue(accountId, bookingId01, adjustmentId);
        assertThat(result, is(500L));
        result = manager.getSignedTotalValue(accountId, -1, adjustmentId);
        assertThat(result, is(1300L));
        result = manager.getSignedTotalValue(accountId, -1, -1, TYPE_BOOKING_CHILD_DISCOUNT);
        assertThat(result, is(-60L));
        result = manager.getSignedTotalValue(accountId, -1, -1, TYPE_BOOKING_CHILD_DISCOUNT, TYPE_SESSION);
        assertThat(result, is(2940L));
        result = manager.getSignedTotalValue(accountId, -1, -1, TYPE_SESSION);
        assertThat(result, is(3000L));
        result = manager.getSignedTotalValue(accountId, -1, adjustmentId, TYPE_BOOKING_CHILD_DISCOUNT);
        assertThat(result, is(0L));
    }

    /*
     *
     */
    @Test
    public void getAllBillingsWithAdjustmentIdNotFound() throws Exception {
        setBilling();
        Set<Integer> adjustmentIdSet = new ConcurrentSkipListSet<>();
        List<BillingBean> result = manager.getAllBillingsWithAdjustmentIdNotFound(accountId, bookingId01, adjustmentIdSet);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getAdjustmentId(), is(adjustmentId));
        adjustmentIdSet.add(adjustmentId);
        result = manager.getAllBillingsWithAdjustmentIdNotFound(accountId, bookingId01, adjustmentIdSet);
        assertThat(result.size(), is(0));
    }


    //<editor-fold defaultstate="collapsed" desc="Set Billing Private Methods">
    private void setBilling() throws Exception {
        int billingId = 1;
        int discountId = 23;
        int periodSd = SDHolder.getSD(10, 19);
        int btChargeBit = BTBits.STANDARD_CHARGE_BIT;
        int billingBits = BillingEnum.getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        long value = 400;
        int invoiceId = -1;
        String description = "Child Adjustment";
        String notes = "notes";

        // child 1
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
        // child 1
        billingId++;
        periodSd = SDHolder.getSD(30, 20);
        value = 500;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, adjustmentId, owner));

        //child 2
        billingId++;
        periodSd = SDHolder.getSD(10, 19);
        value = 600;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId02, profileId02, btChargeBit, billingBits, invoiceId, description, notes, discountId, 17, owner));
        // child 3
        billingId++;
        value = 700;
        manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId03, profileId03, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));

        //child 1 previous day
        int anotherYwd = CalendarStatic.getRelativeYWD(-1);
        billingId++;
        value = 800;
        manager.setObject(accountId, new BillingBean(billingId, accountId, anotherYwd, periodSd, value, taxRate,  bookingId04, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, adjustmentId, owner));

        // child 2 discount
        int discountBits = BillingEnum.getBillingBits(TYPE_BOOKING_CHILD_DISCOUNT, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_BOOKING);
        value = 60;
        description = ApplicationConstants.ADJUSTMENT_CHILD_DISCOUNT;
        manager.setObject(accountId, new BillingBean(discountId, accountId, ywd, periodSd, value, taxRate,  bookingId02, profileId02, btChargeBit, discountBits, invoiceId, description, notes, -1, -1, owner));

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
