/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.List;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class PriceAdjustmentManagerTest {

    private PriceAdjustmentManager manager;
    String owner = ObjectBean.SYSTEM_OWNED;

    @Before
    public void setup() throws Exception {
        manager = new PriceAdjustmentManager(VT.PRICE_ADJUSTMENT.manager());
        manager.clear();
        manager.init();
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_BillItemConfig() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.finance." + VT.PRICE_ADJUSTMENT.bean(), false);
    }

    /*
     *
     */
    @Test
    public void test01_isBillingBit() throws Exception {
        int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        PriceAdjustmentBean bean = new PriceAdjustmentBean(1, "One", billingBits, 0, 1, 0, false, 0, owner);
        assertThat(bean.hasBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING), is(true));
        assertThat(bean.hasBillingBit(TYPE_FIXED_ITEM), is(false));
        assertThat(bean.hasBillingBit(CALC_AS_VALUE), is(true));
        assertThat(bean.hasBillingBit(CALC_AS_PERCENT), is(false));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_RunThrough() throws Exception {
       int id = manager.regenerateIdentifier();
       int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
       manager.setObject(new PriceAdjustmentBean(id, "One", billingBits, 0, 1, 0, false, 0, owner));
       assertEquals(id, manager.getObject("One").getPriceAdjustmentId());
       assertEquals(1, manager.getAllObjects().size());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getBillingLevel() throws Exception {
       int billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_MOST, GROUP_BOOKING);
       PriceAdjustmentBean adjustmentBean = new PriceAdjustmentBean(1, "One", billingBits, 0, 1, 0, false, 0, owner);
       assertEquals(TYPE_FUNDED, adjustmentBean.getBillingLevel(TYPE));
       assertEquals(BILL_CHARGE, adjustmentBean.getBillingLevel(BILL));
       assertEquals(CALC_AS_PERCENT, adjustmentBean.getBillingLevel(CALC));
       assertEquals(APPLY_DISCOUNT, adjustmentBean.getBillingLevel(APPLY));
       assertEquals(RANGE_AT_MOST, adjustmentBean.getBillingLevel(RANGE));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getBillingLevelId() throws Exception {
       int billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_MOST, GROUP_BOOKING);
       PriceAdjustmentBean adjustmentBean = new PriceAdjustmentBean(1, "One", billingBits, 0, 1, 0, false, 0, owner);
       assertEquals(TYPE_FUNDED.getBitValue(), adjustmentBean.getBillingLevelId(TYPE));
       assertEquals(BILL_CHARGE.getBitValue(), adjustmentBean.getBillingLevelId(BILL));
       assertEquals(CALC_AS_PERCENT.getBitValue(), adjustmentBean.getBillingLevelId(CALC));
       assertEquals(APPLY_DISCOUNT.getBitValue(), adjustmentBean.getBillingLevelId(APPLY));
       assertEquals(RANGE_AT_MOST.getBitValue(), adjustmentBean.getBillingLevelId(RANGE));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_setObject_TestBillingBits() throws Exception {
       int id = manager.regenerateIdentifier();
       int billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, CALC_AS_PERCENT);
       manager.setObject(new PriceAdjustmentBean(id, "One", billingBits, 0, 1, 0, false, 0, owner));
       int rtnBits = manager.getObject(id).getBillingBits();
       assertEquals(TYPE_FIXED_ITEM, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(BILL_CHARGE, manager.getObject(id).getBillingLevel(BILL));
       assertEquals(CALC_AS_PERCENT, manager.getObject(id).getBillingLevel(CALC));
       assertEquals(APPLY_DISCOUNT, manager.getObject(id).getBillingLevel(APPLY));
       assertEquals(RANGE_SOME_PART, manager.getObject(id).getBillingLevel(RANGE));

       id = manager.regenerateIdentifier();
       billingBits = BillingEnum.getBillingBits(BILL_CREDIT, CALC_AS_PERCENT);
       manager.setObject(new PriceAdjustmentBean(id, "Two", billingBits, 30, 1, 45, false, 0, owner));
       rtnBits = manager.getObject(id).getBillingBits();
       assertEquals(TYPE_ADJUSTMENT_ON_ALL, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(BILL_CREDIT, manager.getObject(id).getBillingLevel(BILL));
       assertEquals(CALC_AS_PERCENT, manager.getObject(id).getBillingLevel(CALC));
       assertEquals(APPLY_DISCOUNT, manager.getObject(id).getBillingLevel(APPLY));
       assertEquals(RANGE_SOME_PART, manager.getObject(id).getBillingLevel(RANGE));

       id = manager.regenerateIdentifier();
       billingBits = BillingEnum.getBillingBits(TYPE_FUNDED);
       manager.setObject(new PriceAdjustmentBean(id, "Three", billingBits, 30, 1, 45, false, 0, owner));
       rtnBits = manager.getObject(id).getBillingBits();
       assertEquals(TYPE_FUNDED, manager.getObject(id).getBillingLevel(TYPE));

       id = manager.regenerateIdentifier();
       billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, TYPE_LATE_PICKUP);
       manager.setObject(new PriceAdjustmentBean(id, "Three", billingBits, 30, 1, 45, false, 0, owner));
       rtnBits = manager.getObject(id).getBillingBits();
       assertEquals(TYPE_FUNDED, manager.getObject(id).getBillingLevel(TYPE));

    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_SetObject_CheckVales() throws Exception {
       int id, value, precision, rangeSd, durationSd;
       int billingBits = 0;
       String name;
       boolean repeated;

       // control
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = 0;
       precision = 1;
       rangeSd = 0;
       repeated = false;
       durationSd = 0;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(0, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(0, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());

       // value and precision
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = -1;
       precision = -1;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(0, manager.getObject(id).getValue());
       assertEquals(0, manager.getObject(id).getPrecision());

       // TYPE_FIXED_ITEM
       billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM);
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = 1;
       precision = 1;
       rangeSd = 10;
       repeated = true;
       durationSd = 10;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(0, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());

       // range negative
       billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING);
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = 1;
       precision = 1;
       rangeSd = -5;
       repeated = true;
       durationSd = 10;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(TYPE_FIXED_ITEM, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(0, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_SetObject_CheckRange() throws Exception {
       int id, value, precision, rangeSd, durationSd;
       int billingBits = 0;
       String name;
       boolean repeated;

       // control
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = 1;
       precision = 1;
       rangeSd = 10;
       repeated = true;
       durationSd = 5;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(10, manager.getObject(id).getRangeSd());
       assertEquals(true, manager.getObject(id).isRepeated());
       assertEquals(5, manager.getObject(id).getRepeatDuration());

       // range negative
       billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING);
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       value = 1;
       precision = 1;
       rangeSd = -5;
       repeated = true;
       durationSd = 10;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(TYPE_FIXED_ITEM, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(0, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());

       // rangeSd negative
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       rangeSd = 10;
       repeated = false;
       durationSd = 10;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(TYPE_ADJUSTMENT_ON_ATTENDING, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(10, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());

       // durationSd negative negative
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       rangeSd = 10;
       repeated = true;
       durationSd = -5;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(TYPE_ADJUSTMENT_ON_ATTENDING, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(10, manager.getObject(id).getRangeSd());
       assertEquals(false, manager.getObject(id).isRepeated());
       assertEquals(0, manager.getObject(id).getRepeatDuration());

       // durationSd > rangeSd
       id = manager.generateIdentifier();
       name = "name " + Integer.toString(id);
       rangeSd = 10;
       repeated = true;
       durationSd = 15;
       manager.setObject(new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner));
       assertEquals(TYPE_ADJUSTMENT_ON_ATTENDING, manager.getObject(id).getBillingLevel(TYPE));
       assertEquals(1, manager.getObject(id).getValue());
       assertEquals(1, manager.getObject(id).getPrecision());
       assertEquals(10, manager.getObject(id).getRangeSd());
       assertEquals(true, manager.getObject(id).isRepeated());
       assertEquals(10, manager.getObject(id).getRepeatDuration());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getAllPriceAdjustments() throws Exception {
       List<PriceAdjustmentBean> testList;
       int billingBits = 0;

       testList = manager.getAllPriceAdjustments();
       assertEquals(0, testList.size());

       billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
       manager.setObject(new PriceAdjustmentBean(1, "One", billingBits, 0, 1, 20, false, 0, owner));
       billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_FIXED_ITEM);
       manager.setObject(new PriceAdjustmentBean(2, "TQO", billingBits, 0, 1, 0, false, 0, owner));
       billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART,GROUP_FIXED_ITEM);
       manager.setObject(new PriceAdjustmentBean(3, "TWO", billingBits, 0, 1, 0, false, 0, owner));


       testList = manager.getAllPriceAdjustments();
       assertEquals(3, testList.size());

       testList = manager.getAllPriceAdjustments(TYPE_ADJUSTMENT_ON_ATTENDING);
       assertEquals(1, testList.size());
       assertEquals(1, testList.get(0).getPriceAdjustmentId());

       testList = manager.getAllPriceAdjustments(TYPE_FIXED_ITEM);
       assertEquals(2, testList.size());
       assertEquals(2, testList.get(0).getPriceAdjustmentId());
       assertEquals(3, testList.get(1).getPriceAdjustmentId());

       testList = manager.getAllPriceAdjustments(TYPE_FIXED_ITEM, BILL_CHARGE);
       assertEquals(1, testList.size());
       assertEquals(2, testList.get(0).getPriceAdjustmentId());

       testList = manager.getAllPriceAdjustments(BILL_CHARGE);
       assertEquals(2, testList.size());
       assertEquals(1, testList.get(0).getPriceAdjustmentId());
       assertEquals(2, testList.get(1).getPriceAdjustmentId());

       testList = manager.getAllPriceAdjustments(BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART);
       assertEquals(2, testList.size());
       assertEquals(1, testList.get(0).getPriceAdjustmentId());
       assertEquals(2, testList.get(1).getPriceAdjustmentId());

       testList = manager.getAllPriceAdjustments(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART);
       assertEquals(1, testList.size());
       assertEquals(3, testList.get(0).getPriceAdjustmentId());
    }

}
