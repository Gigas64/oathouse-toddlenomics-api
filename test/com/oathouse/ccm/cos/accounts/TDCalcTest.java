package com.oathouse.ccm.cos.accounts;

import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TDCalcTest {


    @Before
    public void setUp() {
    }

    /*
     *
     */
    @Test
    public void test_getDiscountValue() throws Exception {
        long value = 20000L;
        assertThat(TDCalc.getDiscountValue(value, 1000), is(2000L));
        assertThat(TDCalc.getDiscountValue(-value, 1000), is(-2000L));
    }


    /*
     * test that this method returns the correct precision
     */
    @Test
    public void test01_getPrecisionValue() throws Exception {
        long value = 540954;
        assertThat(TDCalc.getPrecisionValue(value, -1), is(540954L));
        assertThat(TDCalc.getPrecisionValue(value, 0), is(540954L));
        assertThat(TDCalc.getPrecisionValue(value, 1), is(540950L));
        assertThat(TDCalc.getPrecisionValue(value, 2), is(541000L));
        assertThat(TDCalc.getPrecisionValue(value, 3), is(541000L));
        assertThat(TDCalc.getPrecisionValue(value, 4), is(540000L));
        assertThat(TDCalc.getPrecisionValue(value, 5), is(500000L));
        assertThat(TDCalc.getPrecisionValue(value, 6), is(1000000L));
        assertThat(TDCalc.getPrecisionValue(value, 7), is(0L));
    }

}