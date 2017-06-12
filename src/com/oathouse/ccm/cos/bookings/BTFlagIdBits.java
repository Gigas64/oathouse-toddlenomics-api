/*
 * @(#)BTFlagIdBits.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;

/**
 * The {@code BTFlagIdBits} Class identifies the flag bits associated with each
 * BookingTypeId
 *
 * @author Darryl Oatridge
 * @version 1.00 29-Dec-2011
 */
public class BTFlagIdBits extends AbstractBits {

    /**
     * BookingType Flag Bit Filter constant used to identify behaviour type bits. These
     * tend to be used in the filter extract only the behaviour bits of a mask
     */
    public static final int
            TYPE_FLAG
            = BTFlagBits.SD_CHANGEABLE_FLAG
            | BTFlagBits.RESET_SPANSD_FLAG
            | BTFlagBits.IMMEDIATE_RECALC_FLAG
            | BTFlagBits.ALLOWANCE_FLAG
            | BTFlagBits.REGULAR_BOOKING_FLAG
            | BTFlagBits.BOOKING_CLOSED_FLAG
            | BTFlagBits.PREBOOKING_FLAG
            | BTFlagBits.BOOKING_FLAG
            | BTFlagBits.POSTBOOKING_FLAG
            | BTFlagBits.ACTIVEBOOKING_FLAG
            | BTFlagBits.PRECHARGE_FLAG
            | BTFlagBits.CHARGE_FLAG
            | BTFlagBits.HISTORY_FLAG
            | BTFlagBits.ARCHIVE_FLAG;

    /**
     * BookingType Flag Bit Filter constant used to identify behaviour type bits.
     * This is identical to TYPE_FLAG and can be used as a shorted form.
     */
    public static final int TYPE_ALL = TYPE_FLAG;

    /**
     * Standard flag set
     *
     * <blockquote>
     * <pre>
            = BTFlagBits.PREBOOKING_FLAG | BTFlagBits.BOOKING_FLAG
            | BTFlagBits.PRECHARGE_FLAG | BTFlagBits.CHARGE_FLAG
            | BTFlagBits.HISTORY_FLAG | BTFlagBits.ARCHIVE_FLAG;
     * </pre>
     * </blockquote>
     */
    public static final int
            STANDARD_FLAGS
            = BTFlagBits.BOOKING_FLAG
            | BTFlagBits.PRECHARGE_FLAG | BTFlagBits.CHARGE_FLAG
            | BTFlagBits.HISTORY_FLAG | BTFlagBits.ARCHIVE_FLAG;

    /**
     * Standard flag set with no payment
     *
     * <blockquote>
     * <pre>
            = BTFlagBits.PREBOOKING_FLAG | BTFlagBits.BOOKING_FLAG
            | BTFlagBits.CHARGE_FLAG // need to check there is no refunds
            | BTFlagBits.HISTORY_FLAG | BTFlagBits.ARCHIVE_FLAG;
     * </pre>
     * </blockquote>
     */
    public static final int
            NOCHARGE_FLAGS
            = BTFlagBits.BOOKING_FLAG
            | BTFlagBits.CHARGE_FLAG // need to check there is no refunds
            | BTFlagBits.HISTORY_FLAG | BTFlagBits.ARCHIVE_FLAG;

    /**
     * Flag set used with layer booking type
     *
     * <blockquote>
     * <pre>
            = BTFlagBits.PRECHARGE_FLAG | BTFlagBits.CHARGE_FLAG
            | BTFlagBits.ARCHIVE_FLAG;
     * </pre>
     * </blockquote>
     */
    public static final int
            LAYER_FLAGS
            = BTFlagBits.PRECHARGE_FLAG | BTFlagBits.CHARGE_FLAG
            | BTFlagBits.ARCHIVE_FLAG;

    /**
     * used to filter test a bookingTypeBean flag against a mask.
     *
     * @param flagBits
     * @param maskBits
     * @return
     */
    public static boolean isFlag(int flagBits, int maskBits) {
        if(maskBits == ALL_OFF || maskBits == UNDEFINED ) {
            return false;
        }
        return(AbstractBits.isBits(flagBits, maskBits));
    }
}
