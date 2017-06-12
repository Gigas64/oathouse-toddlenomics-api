/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
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
public class BillingServiceTest_removeBilling {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        String rootStorePath = "." + sep + "oss" + sep + "data";
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = BillingService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test(expected=IllegalActionException.class)
    public void removeBilling_notFixedItem() throws Exception {
        int billingBits = BillingEnum.getBillingBits(TYPE_EARLY_DROPOFF, BILL_CHARGE, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_BOOKING);
        BillingBean billing = setBilling(billingBits);

        service.removeBilling(billing.getAccountId(), billing.getBillingId());
    }

    //<editor-fold defaultstate="collapsed" desc="setBilling">
    private BillingBean setBilling(int billingBits) throws Exception {
        int accountId = 11;
        int billingId = 2;
        int ywd = CalendarStatic.getRelativeYWD(0);
        int chargeSd = SDHolder.getSD(10, 20);
        long billValue = 20000;
        int taxRate = 0;
        int discountRate = 500;
        int bookingId = 13;
        int profileId = 19;
        int chargeBit = BTBits.STANDARD_CHARGE_BIT;
        String description = ApplicationConstants.SESSION_RECONCILED;
        String notes ="notes";
        // discount
        int discountId = 1;
        int discountBits = BillingEnum.getBillingBits(TYPE_FIXED_CHILD_DISCOUNT, BILL_CHARGE, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);
        long discountValue = TDCalc.getDiscountValue(billValue, discountRate);
        String discountDesc = ApplicationConstants.FIXED_CHILD_DISCOUNT;


        if(BillingEnum.hasBillingBit(billingBits, TYPE_FIXED_ITEM, APPLY_DISCOUNT)) {
             BillingBean discount = new BillingBean(billingId, accountId, ywd, -1, discountValue, taxRate, bookingId, profileId, chargeBit, discountBits, -1, discountDesc, "", -1, -1, owner);
            service.getBillingManager().setObject(accountId, discount);
        }
        BillingBean billing = new BillingBean(billingId, accountId, ywd, chargeSd, billValue, taxRate, bookingId, profileId, chargeBit, billingBits, -1, description, notes, discountId, -1, owner);
        service.getBillingManager().setObject(accountId, billing);
        return billing;
    }
    //</editor-fold>

}
