/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.invoice;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl
 */
public class AdjustmentTypeTest {


    @Before
    public void setUp() {
    }

    /**
     * Test of isPositive method, of class AdjustmentType.
     */
    @Test
    public void testIsPositive() {
        AdjustmentType education = AdjustmentType.EDUCATION_SESSION;
        AdjustmentType penalty = AdjustmentType.LATE_PICKUP;
        AdjustmentType undefined = AdjustmentType.UNDEFINED;
        AdjustmentType no_value = AdjustmentType.NO_VALUE;
        assertEquals(false,education.isPositive());
        assertEquals(true,penalty.isPositive());
        try {
            undefined.isPositive();
            fail();
        } catch(UnsupportedOperationException e) {
        }
        try {
            no_value.isPositive();
            fail();
        } catch(UnsupportedOperationException e) {
        }
    }

    /**
     * Test of getPositive method, of class AdjustmentType.
     */
    @Test
    public void testGets() {
        assertEquals(1, AdjustmentType.getNegative().size());
        assertEquals(AdjustmentType.EDUCATION_SESSION, AdjustmentType.getNegative().get(0));
        assertEquals(2, AdjustmentType.getPositive().size());
        assertEquals(AdjustmentType.EARLY_DROP, AdjustmentType.getPositive().get(0));
    }
}
