/*
 * @(#)ActionBits.java
 *
 * Copyright:	Copyright (c) Error: on line 10, column 33 in Templates/Classes/Class_1.java
Expecting a date here, found: Jan 26, 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;
import java.util.List;

/**
 * The {@code ActionBits} Class providing a set of action bits
 *
 * @author Darryl Oatridge
 * @version 1.00 Jan 26, 2012
 */
public class ActionBits extends AbstractBits {

    /**
     * immediate recalculate rules should be applied
     */
    public static final int IMMEDIATE_RECALC_BIT = (int) Math.pow(0x2, 0x1);
    /**
     * Indicates a booking is AdHoc and as such negates validation rules
     */
    public static final int ADHOC_BOOKING_BIT = (int) Math.pow(0x2, 0x2);
    /**
     * Day discount rules should be applied even if not reached
     */
    public static final int DAY_DISCOUNT_BIT = (int) Math.pow(0x2, 0x3);
    /**
     * The booking has been pre billed
     */
    public static final int BOOKING_PRE_BILLED_BIT = (int) Math.pow(0x2, 0x4);

    /**
     * Action Bit Filter constant used to identify all action type bits.
     */
    public static final int
            TYPE_ACTION
            = IMMEDIATE_RECALC_BIT
            | ADHOC_BOOKING_BIT
            | DAY_DISCOUNT_BIT
            | BOOKING_PRE_BILLED_BIT;

    public static final int
            TYPE_PREBOOKING
            = ADHOC_BOOKING_BIT;

    public static final int
            TYPE_BOOKING
            = IMMEDIATE_RECALC_BIT
            | ADHOC_BOOKING_BIT
            | DAY_DISCOUNT_BIT;

    public static final int
            TYPE_POSTBOOKING
            = ADHOC_BOOKING_BIT
            | DAY_DISCOUNT_BIT;

    /**
     * Action Bit Filter constant used to identify action type bits.
     * This is identical to TYPE_ACTION and can be used as a generic form.
     */
    public static final int TYPE_ALL = TYPE_ACTION;

    /**
     * Action Bit Filter constant used to identify action type bits.
     * This is identical to ALL_OFF and can be used when No filtering is required
     * when used action bit filtering is required ass a method parameter.
     */
    public static final int TYPE_FILTER_OFF = ALL_OFF;

    /**
     * Action Bit Filter constant used to identify action type bits.
     * This is identical to ALL_OFF and can be used when no actions are to be included.
     */
    public static final int TYPE_NONE = ALL_OFF;

    /**
     * Helper method that converts action bits to a string list. Each
     * bit that is set will add a string representation to the list.
     *
     * @param bits the action bits to be converted
     * @return a list of strings
     */
    public static List<String> getAllStrings(int bits) {
        List<String> rtnList = AbstractBits.getBitsAsString(bits);

        if(match(bits, IMMEDIATE_RECALC_BIT)) { rtnList.add("IMMEDIATE_RECALC_BIT"); }
        if(match(bits, ADHOC_BOOKING_BIT)) { rtnList.add("ADHOC_BOOKING_BIT"); }
        if(match(bits, DAY_DISCOUNT_BIT)) { rtnList.add("DAY_DISCOUNT_BIT"); }
        if(match(bits, BOOKING_PRE_BILLED_BIT)) { rtnList.add("BOOKING_PRE_BILLED_BIT"); }

        return rtnList;
    }

    /**
     * Helper method that converts a String to an action bit or UNDEFINED if not found.
     * The String must be identical the the action bit constant name
     *
     * @param bitString the String representation of the Bit
     * @return the Bit bit value
     */
    public static int getBitFromString(String bitString) {
        int rtnBits = 0;
        if(bitString.contains("IMMEDIATE_RECALC_BIT")) { rtnBits |= IMMEDIATE_RECALC_BIT; }
        if(bitString.contains("ADHOC_BOOKING_BIT")) { rtnBits |= ADHOC_BOOKING_BIT; }
        if(bitString.contains("DAY_DISCOUNT_BIT")) { rtnBits |= DAY_DISCOUNT_BIT; }
        if(bitString.contains("BOOKING_PRE_BILLED_BIT")) { rtnBits |= BOOKING_PRE_BILLED_BIT; }

        return(rtnBits | AbstractBits.getBitsFromString(bitString));
    }

}
