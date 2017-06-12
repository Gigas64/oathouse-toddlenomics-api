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
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
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
public class BillingManagerTest_setObjectOverride {

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

    public BillingManagerTest_setObjectOverride() {
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
    public void setObject_runThrough() throws Exception {
        BillingBean billing = setBilling();
        assertThat(manager.getAllObjects(accountId).size(), is(1));
        assertThat(manager.getAllObjects(accountId).get(0), is(billing));
    }

    /*
     *
     */
    @Test
    public void setObjectWithInvoiceId() throws Exception {
        int invoiceId = 13;

        setBilling();
        manager.setObjectInvoiceId(accountId, 1, invoiceId, owner);
        try {
            manager.setObject(accountId, manager.getObject(accountId, 1));
            fail();
        } catch(PersistenceException persistenceException) {
            assertThat(persistenceException.getMessage().contains("' has been allocated to invoice '"), is (true));
        }
    }

    /*
     *
     */
    @Test
    public void setObjectWithChangeOfBillingButFixedItem() throws Exception {
        int billingId = 1;
        int periodSd = SDHolder.getSD(10, 19);
        int btChargeBit = BTBits.STANDARD_CHARGE_BIT;
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        long value = 400;
        int invoiceId = -1;
        String description = "description";
        String notes = "notes";
        BillingBean billing = manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
        assertThat(billing.hasBillingBits(BILL_CHARGE), is(true));

        //change the billingBits
        billingBits = BillingEnum.resetBillingBits(billingBits, BILL_CREDIT);
        billing = manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
        assertThat(billing.hasBillingBits(BILL_CREDIT), is(true));
    }

    //<editor-fold defaultstate="collapsed" desc="Set Billing Private Methods">
    private BillingBean setBilling() throws Exception {
        int billingId = 1;
        int periodSd = SDHolder.getSD(10, 19);
        int btChargeBit = BTBits.STANDARD_CHARGE_BIT;
        int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        long value = 400;
        int invoiceId = -1;
        String description = "description";
        String notes = "notes";

        // child 1
        return manager.setObject(accountId, new BillingBean(billingId, accountId, ywd, periodSd, value, taxRate, bookingId01, profileId01, btChargeBit, billingBits, invoiceId, description, notes, -1, -1, owner));
    }

    //</editor-fold>

}
