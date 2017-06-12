/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingManagerTest_beanMethods {
    private int accountId = 3;
    private long chargeValue = 15000; // £15
    private long creditValue = 3250; // £3.25
    private int taxRate = 2000; // 20%
    private String owner = ObjectBean.SYSTEM_OWNED;
    private int chargeBits = BillingEnum.getBillingBits(BillingEnum.BILL_CHARGE);
    private int creditBits = BillingEnum.getBillingBits(BillingEnum.BILL_CREDIT);
    private int billingBits;
    private BillingBean charge;
    private BillingBean credit;

    @Before
    public void setUp() {
        billingBits = BillingEnum.getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING);
        charge = new BillingBean(0, accountId, 0, 0, chargeValue, taxRate, 0, 0, chargeBits, billingBits, -1,"", "", -1, -1, owner);
        billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_EQUAL, GROUP_FIXED_ITEM);
        credit = new BillingBean(0, accountId, 0, 0, creditValue, taxRate, 0, 0, creditBits, billingBits, -1, "", "",-1, -1,  owner);
    }

    /*
     * charge
     */
    @Test
    public void test01_getValueArray() throws Exception {
        long[] chargeArray = charge.getValueArray();
        assertThat(chargeArray[0], is(TDCalc.getValueIncTax(chargeValue, taxRate)));
        assertThat(chargeArray[1], is(chargeValue));
        assertThat(chargeArray[2], is(TDCalc.getTax(chargeValue, taxRate)));
        long[] creditArray = credit.getValueArray();
        assertThat(creditArray[0], is(TDCalc.getValueIncTax(creditValue, taxRate)));
        assertThat(creditArray[1], is(creditValue));
        assertThat(creditArray[2], is(TDCalc.getTax(creditValue, taxRate)));
    }

    /*
     * credit
     */
    @Test
    public void test02_getSignedValue() throws Exception {
        long[] chargeArray = charge.getSignedValueArray();
        assertThat(chargeArray[0], is(TDCalc.getValueIncTax(chargeValue, taxRate)));
        assertThat(chargeArray[1], is(chargeValue));
        assertThat(chargeArray[2], is(TDCalc.getTax(chargeValue, taxRate)));
        long[] creditInvoice = credit.getSignedValueArray();
        assertThat(creditInvoice[0], is(-1 * TDCalc.getValueIncTax(creditValue, taxRate)));
        assertThat(creditInvoice[1], is(-1 * creditValue));
        assertThat(creditInvoice[2], is(-1 * TDCalc.getTax(creditValue, taxRate)));
    }

    /*
     *
     */
    @Test
    public void test01_hasBillingBits() throws Exception {
        assertThat(charge.hasBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT), is(true));
        assertThat(charge.hasAnyBillingBit(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT), is(true));
        assertThat(credit.hasBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT), is(false));
        assertThat(credit.hasAnyBillingBit(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT), is(true));

        assertThat(charge.hasBillingBits(CALC_AS_VALUE, APPLY_NO_DISCOUNT), is(false));
        assertThat(charge.hasAnyBillingBit(CALC_AS_VALUE, APPLY_NO_DISCOUNT), is(true));
        assertThat(credit.hasBillingBits(CALC_AS_VALUE, APPLY_NO_DISCOUNT), is(true));
        assertThat(credit.hasAnyBillingBit(CALC_AS_VALUE, APPLY_NO_DISCOUNT), is(true));

        assertThat(charge.hasBillingBits(), is(false));
        assertThat(charge.hasAnyBillingBit(), is(false));
        assertThat(credit.hasBillingBits(), is(false));
        assertThat(credit.hasAnyBillingBit(), is(false));

        assertThat(charge.hasBillingBits(CALC_AS_VALUE), is(true));
        assertThat(charge.hasAnyBillingBit(CALC_AS_VALUE), is(true));
        assertThat(credit.hasBillingBits(CALC_AS_VALUE), is(true));
        assertThat(credit.hasAnyBillingBit(CALC_AS_VALUE), is(true));

        assertThat(charge.hasBillingBits(CALC_AS_PERCENT), is(false));
        assertThat(charge.hasAnyBillingBit(CALC_AS_PERCENT), is(false));
        assertThat(credit.hasBillingBits(CALC_AS_PERCENT), is(false));
        assertThat(credit.hasAnyBillingBit(CALC_AS_PERCENT), is(false));
}


}