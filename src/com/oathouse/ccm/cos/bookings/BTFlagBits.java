/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)BTFlagBits.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;
import java.util.List;

/**
 * The {@code BTFlagBits} Class used to define the flag bits used with BookingTypeBean.
 * Collective constants and a filter method for these flags can be found in BTFlagIdBits
 *
 * @author Darryl Oatridge
 * @version 1.00 30-Dec-2011
 */
public class BTFlagBits extends AbstractBits{

    /*
     * On/Off boolean like, behaviour bit flags which indicates the potential for
     * the Booking to be acted upon if the flag is set. It should be noted
     * that because a flag is set does not mean it has to be acted upon, it is
     * more an indicator of behaviour than an action of behaviour.
     */
    // Special case flag to change the behaviour of the state
    public static final int SD_CHANGEABLE_FLAG = (int) Math.pow(0x2, 0x0f); //
    public static final int RESET_SPANSD_FLAG = (int) Math.pow(0x2, 0x10); //
    /** Flag indicates the booking should be immediately recalculated and not wait */
    public static final int IMMEDIATE_RECALC_FLAG = (int) Math.pow(0x2, 0x11); //
    /** Flag indicates the booking should ignore validation rules for closed days */
    public static final int BOOKING_CLOSED_FLAG = (int) Math.pow(0x2, 0x12); //
    // Special Case flags to extend behaviour and meaning of BTIdBits Identifier
    public static final int ALLOWANCE_FLAG = (int) Math.pow(0x2, 0x13); //
    public static final int REGULAR_BOOKING_FLAG = (int) Math.pow(0x2, 0x14); //
    // Booking Phase bit flags
    public static final int PREBOOKING_FLAG = (int) Math.pow(0x2, 0x15);
    public static final int BOOKING_FLAG = (int) Math.pow(0x2, 0x16);
    public static final int POSTBOOKING_FLAG = (int) Math.pow(0x2, 0x17);
    public static final int ACTIVEBOOKING_FLAG = (int) Math.pow(0x2, 0x18);
    // Charging phase bit flags (if not present indicates no charge)
    public static final int PRECHARGE_FLAG = (int) Math.pow(0x2, 0x19);
    public static final int CHARGE_FLAG = (int) Math.pow(0x2, 0x1a);
    // house keeping bit flags
    public static final int HISTORY_FLAG = (int) Math.pow(0x2, 0x1b);
    public static final int ARCHIVE_FLAG = (int) Math.pow(0x2, 0x1c); // SEE NOTE!!!!!!
    // NOTE: ADD TO THE START NUMBERS NOT THE END THIS IS BECAUSE WE DON'T WANT SAME VALUES AS BtBits

    /**
     * Helper method that converts BTFlagBits to a string list. Each
     * bit that is set will add a string representation to the list.
     *
     * @param bits the BTFlagBits to be converted
     * @return a list of strings
     */
    public static List<String> getAllStrings(int bits) {
        List<String> rtnList = AbstractBits.getBitsAsString(bits);

        if(match(bits, SD_CHANGEABLE_FLAG)) { rtnList.add("SD_CHANGEABLE_FLAG"); }
        if(match(bits, RESET_SPANSD_FLAG)) { rtnList.add("RESET_SPANSD_FLAG"); }
        if(match(bits, IMMEDIATE_RECALC_FLAG)) { rtnList.add("IMMEDIATE_RECALC_FLAG"); }
        if(match(bits, ALLOWANCE_FLAG)) { rtnList.add("ALLOWANCE_FLAG"); }
        if(match(bits, REGULAR_BOOKING_FLAG)) { rtnList.add("REGULAR_BOOKING_FLAG"); }
        if(match(bits, BOOKING_CLOSED_FLAG)) { rtnList.add("BOOKING_CLOSED_FLAG"); }
        if(match(bits, PREBOOKING_FLAG)) { rtnList.add("PREBOOKING_FLAG"); }
        if(match(bits, BOOKING_FLAG)) { rtnList.add("BOOKING_FLAG"); }
        if(match(bits, POSTBOOKING_FLAG)) { rtnList.add("POSTBOOKING_FLAG"); }
        if(match(bits, ACTIVEBOOKING_FLAG)) { rtnList.add("ACTIVEBOOKING_FLAG"); }
        if(match(bits, PRECHARGE_FLAG)) { rtnList.add("PRECHARGE_FLAG"); }
        if(match(bits, CHARGE_FLAG)) { rtnList.add("CHARGE_FLAG"); }
        if(match(bits, HISTORY_FLAG)) { rtnList.add("HISTORY_FLAG"); }
        if(match(bits, ARCHIVE_FLAG)) { rtnList.add("ARCHIVE_FLAG"); }

        if(match(bits, FILTER_EQUALS)) { rtnList.add("FILTER_EQUALS"); }
        if(match(bits, FILTER_MATCH)) { rtnList.add("FILTER_MATCH"); }

        return rtnList;
    }

    /**
     * Helper method that converts a String to a BTFlagBit or UNDEFINED if not found.
     * The String must be identical the the BTFlagBit constant name
     *
     * @param sFlag the String representation of the BTFlagBit
     * @return the BTFlagBit bit value
     */
    public static int getFlag(String sFlag) {
        if(sFlag.equals("SD_CHANGEABLE_FLAG")) { return SD_CHANGEABLE_FLAG; }
        if(sFlag.equals("RESET_SPANSD_FLAG")) { return RESET_SPANSD_FLAG; }
        if(sFlag.equals("IMMEDIATE_RECALC_FLAG")) { return IMMEDIATE_RECALC_FLAG; }
        if(sFlag.equals("ALLOWANCE_FLAG")) { return ALLOWANCE_FLAG; }
        if(sFlag.equals("REGULAR_BOOKING_FLAG")) { return REGULAR_BOOKING_FLAG; }
        if(sFlag.equals("BOOKING_CLOSED_FLAG")) { return BOOKING_CLOSED_FLAG; }
        if(sFlag.equals("PREBOOKING_FLAG")) { return PREBOOKING_FLAG; }
        if(sFlag.equals("BOOKING_FLAG")) { return BOOKING_FLAG; }
        if(sFlag.equals("POSTBOOKING_FLAG")) { return POSTBOOKING_FLAG; }
        if(sFlag.equals("ACTIVEBOOKING_FLAG")) { return ACTIVEBOOKING_FLAG; }
        if(sFlag.equals("PRECHARGE_FLAG")) { return PRECHARGE_FLAG; }
        if(sFlag.equals("CHARGE_FLAG")) { return CHARGE_FLAG; }
        if(sFlag.equals("HISTORY_FLAG")) { return HISTORY_FLAG; }
        if(sFlag.equals("ARCHIVE_FLAG")) { return ARCHIVE_FLAG; }

        return(UNDEFINED);
    }

}
