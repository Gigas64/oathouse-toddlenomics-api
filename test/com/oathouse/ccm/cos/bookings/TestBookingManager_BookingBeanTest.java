/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.bookings;

import static com.oathouse.ccm.cos.bookings.BTBits.*;
import static com.oathouse.ccm.cos.bookings.BTIdBits.*;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


/**
 *
 * @author Darryl Oatridge
 */
public class TestBookingManager_BookingBeanTest {

    @Before
    public void setUp() {
    }
    /*
     *
     */

    @Test
    public void test01_getBookingTypeChargeBit() throws Exception {
        int bookingTypeId = ATTENDING_SPECIAL;
        assertThat(BTBits.isBits(bookingTypeId, ATTENDING_BIT), is(true));
        assertThat(BTBits.isBits(bookingTypeId, SPECIAL_CHARGE_BIT), is(true));
        assertThat(BTBits.getAllStrings(bookingTypeId).size(), is(2));

        int testBits = BTIdBits.turnOff(bookingTypeId, BTIdBits.TYPE_BOOKING | BTIdBits.TYPE_LAYER);
        assertThat(BTBits.isBits(testBits, SPECIAL_CHARGE_BIT), is(true));
        assertThat(BTBits.getAllStrings(testBits).size(), is(1));
    }


}