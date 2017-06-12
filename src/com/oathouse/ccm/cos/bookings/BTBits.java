/*
 * @(#)BTBits.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;
import java.util.List;

/**
 * The {@code BTBits} Class is a bitwise helper to provides a numerical
 * logic values for BookingTypeBean identifiers.
 * <p>
 * Logic follows the following bitwise logic <br>
 *  &amp; - true if both operands are true, otherwise false <br>
 *  ^ - true if both operands are different, otherwise false <br>
 *  | - false if both operands are false, otherwise, true <br>
 *</p>
 *
 * <p>
 * An example of how to build a bit mask for ATTENDING_BIT and NO_CHARGE_BIT might be:
 * </p>
 *
 * <blockquote>
 * <pre>
 *     int bitflagMask = BTBits.ATTENDING_BIT | BTBits.NO_CHARGE_BIT;
 * </pre>
 * </blockquote>
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Oct-2011
 */
public class BTBits extends AbstractBits {

    /*
     * The following bit values are 'identifier' bits and MUST be present in a BookingTypeId.
     * They are also MUTUALLY EXCLUSIVE and so ONLY one can be using in any single
     * BookingTypeId. TYPE_LAYER bits are special case identifier bits.
     */
    // TYPE_BOOKING
    public static final int ATTENDING_BIT = (int) Math.pow(0x2, 0x1);   // 2
    public static final int WAITING_BIT = (int) Math.pow(0x2, 0x2);     // 4
    public static final int HOLIDAY_BIT = (int) Math.pow(0x2, 0x3);     // 8
    public static final int LEAVE_BIT = (int) Math.pow(0x2, 0x4);       // 16
    public static final int SICK_BIT = (int) Math.pow(0x2, 0x5);        // 32
    public static final int ABSENT_BIT = (int) Math.pow(0x2, 0x6);      // 64
    public static final int SUSPENDED_BIT = (int) Math.pow(0x2, 0x7);   // 128
    public static final int CANCELLED_BIT = (int) Math.pow(0x2, 0x8);   // 256
    // TYPE_LAYER
    public static final int DISCOUNT_BIT = (int) Math.pow(0x2, 0x9);    // 512
    public static final int REFUND_BIT = (int) Math.pow(0x2, 0xa);      // 1024
    public static final int PENALTY_BIT = (int) Math.pow(0x2, 0xb);     // 2048
    /*
     * The following bit values are 'charge' bits and MUST be present in a BookingTypeId
     * They are also MUTUALLY EXCLUSIVE and so ONLY one can be using in any single
     * BookingTypeId.
     */
    public static final int NO_CHARGE_BIT = (int) Math.pow(0x2, 0xc);       // 4096
    public static final int STANDARD_CHARGE_BIT = (int) Math.pow(0x2, 0xd); // 8192
    public static final int SPECIAL_CHARGE_BIT = (int) Math.pow(0x2, 0xe);  // 16384

    /**
     * Helper method that converts BTBits to a string list. Each
     * bit that is set will add a string representation to the list.
     *
     * @param bits the BTBits to be converted
     * @return a list of strings
     */
    public static List<String> getAllStrings(int bits) {
        List<String> rtnList = AbstractBits.getBitsAsString(bits);

        if(match(bits, ATTENDING_BIT)) { rtnList.add("ATTENDING_BIT"); }
        if(match(bits, WAITING_BIT)) { rtnList.add("WAITING_BIT"); }
        if(match(bits, HOLIDAY_BIT)) { rtnList.add("HOLIDAY_BIT"); }
        if(match(bits, LEAVE_BIT)) { rtnList.add("LEAVE_BIT"); }
        if(match(bits, SICK_BIT)) { rtnList.add("SICK_BIT"); }
        if(match(bits, ABSENT_BIT)) { rtnList.add("ABSENT_BIT"); }
        if(match(bits, SUSPENDED_BIT)) { rtnList.add("SUSPENDED_BIT"); }
        if(match(bits, CANCELLED_BIT)) { rtnList.add("CANCELLED_BIT"); }

        if(match(bits, DISCOUNT_BIT)) { rtnList.add("DISCOUNT_BIT"); }
        if(match(bits, REFUND_BIT)) { rtnList.add("REFUND_BIT"); }
        if(match(bits, PENALTY_BIT)) { rtnList.add("PENALTY_BIT"); }

        if(match(bits, NO_CHARGE_BIT)) { rtnList.add("NO_CHARGE_BIT"); }
        if(match(bits, STANDARD_CHARGE_BIT)) { rtnList.add("STANDARD_CHARGE_BIT"); }
        if(match(bits, SPECIAL_CHARGE_BIT)) { rtnList.add("SPECIAL_CHARGE_BIT"); }

        return rtnList;
    }

    /**
     * Helper method that converts a String to a BTBit or UNDEFINED if not found.
     * The String must be identical the the BTBit constant name.
     *
     * @param sBit the String representation of the BTBit
     * @return the BTBit bit value
     */
    public static int getBit(String sBit) {
        int rtnBits = 0x0;
        if(sBit.contains("ATTENDING_BIT")) { rtnBits |= ATTENDING_BIT; }
        if(sBit.contains("WAITING_BIT")) { rtnBits |= WAITING_BIT; }
        if(sBit.contains("HOLIDAY_BIT")) { rtnBits |= HOLIDAY_BIT; }
        if(sBit.contains("LEAVE_BIT")) { rtnBits |= LEAVE_BIT; }
        if(sBit.contains("SICK_BIT")) { rtnBits |= SICK_BIT; }
        if(sBit.contains("ABSENT_BIT")) { rtnBits |= ABSENT_BIT; }
        if(sBit.contains("SUSPENDED_BIT")) { rtnBits |= SUSPENDED_BIT; }
        if(sBit.contains("CANCELLED_BIT")) { rtnBits |= CANCELLED_BIT; }

        if(sBit.contains("DISCOUNT_BIT")) { rtnBits |= DISCOUNT_BIT; }
        if(sBit.contains("REFUND_BIT")) { rtnBits |= REFUND_BIT; }
        if(sBit.contains("PENALTY_BIT")) { rtnBits |= PENALTY_BIT; }

        if(sBit.contains("NO_CHARGE_BIT")) { rtnBits |= NO_CHARGE_BIT; }
        if(sBit.contains("STANDARD_CHARGE_BIT")) { rtnBits |= STANDARD_CHARGE_BIT; }
        if(sBit.contains("SPECIAL_CHARGE_BIT")) { rtnBits |= SPECIAL_CHARGE_BIT; }

        return(rtnBits | AbstractBits.getBitsFromString(sBit));
    }

}
