/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.config.finance;

import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class BillingEnumTest {

    @Before
    public void setUp() {
    }

    /*
     *
     */
    @Test
    public void testAllNumbersAreUnique() throws Exception {
        Set<Integer> numSet = new ConcurrentSkipListSet<>();
        for(BillingEnum billingEnum : values()) {
            if(billingEnum.getBitValue() == 0) {
                continue;
            }
            if(numSet.contains(billingEnum.getBitValue())) {
                fail("There is a duplicate bit value " + billingEnum.getBitValue() + " " + getAllStrings(billingEnum.getBitValue()));
            }
            numSet.add(billingEnum.getBitValue());
        }
    }


    @Test
    public void testGetBitValue() {
        BillingEnum billingEnum = TYPE_ADJUSTMENT_ON_ATTENDING;
        assertEquals(1, billingEnum.getBitValue());
        assertEquals(1, TYPE_ADJUSTMENT_ON_ATTENDING.getBitValue());
    }

    /*
     *
     */
    @Test
    public void test01_isValid() throws Exception {
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)), is(true));

        assertThat(isValid(getBillingBits(BILL_CHARGE, TYPE_ADJUSTMENT_ON_ATTENDING, RANGE_AT_LEAST, CALC_AS_PERCENT, APPLY_DISCOUNT, GROUP_BOOKING)), is(true));

        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, TYPE_FIXED_ITEM, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)), is(false));
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)), is(false));
        // same value twice
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)), is(true));
        // same level twice
        assertThat(isValid(getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)), is(false));

        // test from something that didn't work
        int discountBits = BillingEnum.getBillingBits(TYPE_LOYALTY_CHILD_DISCOUNT, BILL_CHARGE, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_LOYALTY);
        assertThat(isValid(discountBits), is(true));
    }

    /*
     *
     */
    @Test
    public void test01_resetBillingBits() throws Exception {
        int bits = getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING);

        int test = resetBillingBits(bits, TYPE_FIXED_ITEM);
        assertThat(getAllBillingEnums(test), is(Arrays.asList(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)));
        test = resetBillingBits(bits, TYPE_FIXED_ITEM, BILL_CREDIT);
        assertThat(getAllBillingEnums(test), is(Arrays.asList(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)));
        test = resetBillingBits(bits, NO_VALUE, UNDEFINED);
        assertThat(getAllBillingEnums(test), is(getAllBillingEnums(bits)));
        test = resetBillingBits(bits, RANGE_EQUAL, TYPE, CALC);
        assertThat(getAllBillingEnums(test), is(Arrays.asList(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING)));
        test = resetBillingBits(bits, BILL_CREDIT);
        assertThat(getAllBillingEnums(test), is(Arrays.asList(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING)));
    }

    /*
     *
     */
    @Test
    public void test01_getAllBillingEnums() throws Exception {
        List<BillingEnum> controlList = Arrays.asList(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        int billingBits = getBillingBits(controlList.toArray(new BillingEnum[]{}));
        List<BillingEnum> testList = getAllBillingEnums(billingBits);

        assertTrue(controlList.containsAll(testList));
        assertTrue(testList.containsAll(controlList));
    }
    /*
     *
     */

    @Test
    public void test01_toArray() throws Exception {
        BillingEnum[] control = {TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING};
        int bits = getBillingBits(control);
        assertThat(toArray(bits), is(control));
    }

    /*
     *
     */
    @Test
    public void test01_getAllStrings() throws Exception {
        List<String> test = Arrays.asList("TYPE_ADJUSTMENT_ON_ATTENDING", "BILL_CHARGE", "CALC_AS_PERCENT", "APPLY_DISCOUNT", "RANGE_AT_LEAST", "GROUP_BOOKING");
        BillingEnum[] control = {TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_BOOKING};
        int bits = getBillingBits(control);
        assertThat(getAllStrings(bits), is(test));
    }

    /*
     *
     */
    @Test
    public void test01_getBillingBits() throws Exception {
        List<BillingEnum> controlList = Arrays.asList(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        int billingBits = getBillingBits(controlList.toArray(new BillingEnum[]{}));
        assertThat(getAllBillingEnums(billingBits), is(controlList));
    }

    /*
     *
     */
    @Test
    public void test01_hasBillingBits() throws Exception {
        List<BillingEnum> controlList = Arrays.asList(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        int billingBits = getBillingBits(controlList.toArray(new BillingEnum[]{}));
        assertThat(hasBillingBit(billingBits), is(false));
        assertThat(hasBillingBit(billingBits, TYPE_ADJUSTMENT_ON_ATTENDING), is(true));
        assertThat(hasBillingBit(billingBits, APPLY_DISCOUNT, RANGE_SOME_PART), is(true));
        assertThat(hasBillingBit(billingBits, BILL_CHARGE, CALC_AS_PERCENT), is(false));
        assertThat(hasBillingBit(billingBits, CALC_AS_PERCENT), is(false));
        assertThat(hasBillingBit(billingBits, TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING), is(true));
    }

}