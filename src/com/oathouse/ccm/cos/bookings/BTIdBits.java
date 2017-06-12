/*
 * @(#)BTIdBits.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;

/**
 * The {@code BTIdBits} Class is a helper class used to identify
 * BookingTypeId bit content
 *
 * @author Darryl Oatridge
 * @version 1.00 19-Dec-2011
 */
public class BTIdBits extends AbstractBits {
    /**
     * BookingType Bit Filter constant used to filter all identifiers. All identifiers is
     * every booking has to have a bookingTypeId that contains one of these and therefore
     * filtering on TYPE_IDENTIFIER will result in every booking being returned.
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.WAITING_BIT
            | BTBits.SICK_BIT
            | BTBits.HOLIDAY_BIT
            | BTBits.LEAVE_BIT
            | BTBits.ABSENT_BIT
            | BTBits.SUSPENDED_BIT
            | BTBits.CANCELLED_BIT
            | BTBits.PENALTY_BIT
            | BTBits.DISCOUNT_BIT
            | BTBits.REFUND_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            TYPE_IDENTIFIER
            = BTBits.ATTENDING_BIT
            | BTBits.WAITING_BIT
            | BTBits.SICK_BIT
            | BTBits.HOLIDAY_BIT
            | BTBits.LEAVE_BIT
            | BTBits.ABSENT_BIT
            | BTBits.SUSPENDED_BIT
            | BTBits.CANCELLED_BIT
            | BTBits.PENALTY_BIT
            | BTBits.DISCOUNT_BIT
            | BTBits.REFUND_BIT;

    /**
     * BookingType Bit Filter constant used to filter physical type identifiers.
     * These are identifiers that have a physical presence on a booking system
     * and this constant allows a quick way of selecting all bookings of this
     * type.
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.SICK_BIT
            | BTBits.LEAVE_BIT
            | BTBits.HOLIDAY_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            TYPE_PHYSICAL_BOOKING
            = BTBits.ATTENDING_BIT
            | BTBits.SICK_BIT
            | BTBits.LEAVE_BIT
            | BTBits.HOLIDAY_BIT;

    /**
     * BookingType Bit Filter constant used to filter booking type identifiers
     * These are identifiers that relate to the booking process of the system
     * and this constant allows a quick way of selecting all bookings only
     * type.
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.WAITING_BIT
            | BTBits.SICK_BIT
            | BTBits.HOLIDAY_BIT
            | BTBits.LEAVE_BIT
            | BTBits.ABSENT_BIT
            | BTBits.SUSPENDED_BIT
            | BTBits.CANCELLED_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            TYPE_BOOKING
            = BTBits.ATTENDING_BIT
            | BTBits.WAITING_BIT
            | BTBits.SICK_BIT
            | BTBits.HOLIDAY_BIT
            | BTBits.LEAVE_BIT
            | BTBits.ABSENT_BIT
            | BTBits.SUSPENDED_BIT
            | BTBits.CANCELLED_BIT;

    /**
     * BookingType Bit Filter constant used to filter layer type identifiers
     * These are identifiers that might overlay a booking and add additional
     * information during the charging process.
     * <blockquote>
     * <pre>
            = BTBits.PENALTY_BIT
            | BTBits.DISCOUNT_BIT
            | BTBits.REFUND_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            TYPE_LAYER
            = BTBits.PENALTY_BIT
            | BTBits.DISCOUNT_BIT
            | BTBits.REFUND_BIT;

    /**
     * BookingType Bit Filter constant used to identify charge type bits. These
     * tend to be used in the filter extract only the charge bits of a mask
     * <blockquote>
     * <pre>
            = BTBits.NO_CHARGE_BIT
            | BTBits.STANDARD_CHARGE_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            TYPE_CHARGE
            = BTBits.NO_CHARGE_BIT
            | BTBits.STANDARD_CHARGE_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * BookingType Bit Filter constant used to filter all identifiers &amp; charged.This is
     * identical to TYPE_IDENTIFIER and can be used as a shorted form.
     */
    public static final int TYPE_ALL = TYPE_IDENTIFIER | TYPE_CHARGE;

    /**
     * Confirmed booking
     *
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.STANDARD_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            ATTENDING_STANDARD
            = BTBits.ATTENDING_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Confirmed booking with no payment
     *
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            ATTENDING_NOCHARGE
            = BTBits.ATTENDING_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * Booking with a percent of the periodSD charge
     * Note: 100% chargeValue charges standard for the period, 0% is no charge
     *
     * <blockquote>
     * <pre>
            = BTBits.ATTENDING_BIT
            | BTBits.SPECIAL_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            ATTENDING_SPECIAL
            = BTBits.ATTENDING_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

   /**
     * Booking that is on a waiting list
     * <blockquote>
     * <pre>
            = BTBits.WAITING_BIT
            | BTBits.STANDARD_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            WAITING_STANDARD
            = BTBits.WAITING_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Booking that is on a waiting list with no charge
     * <blockquote>
     * <pre>
            = BTBits.WAITING_BIT
            | BTBits.NO_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            WAITING_NOCHARGE
            = BTBits.WAITING_BIT
            | BTBits.NO_CHARGE_BIT;

   /**
     * Booking that is on a waiting list with special charge
     * <blockquote>
     * <pre>
            = BTBits.WAITING_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            WAITING_SPECIAL
            = BTBits.WAITING_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * booking with no attendance due to holiday
     * <blockquote>
     * <pre>
            = BTBits.HOLIDAY_BIT
            | BTBits.STANDARD_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            HOLIDAY_STANDARD
            = BTBits.HOLIDAY_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * booking with no attendance due to holiday with no payment required
     * <blockquote>
     * <pre>
            = BTBits.HOLIDAY_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            HOLIDAY_NOCHARGE
            = BTBits.HOLIDAY_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * booking with no attendance due to holiday with special charge
     * <blockquote>
     * <pre>
            = BTBits.HOLIDAY_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            HOLIDAY_SPECIAL
            = BTBits.HOLIDAY_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

   /**
     * booking with no attendance due to leave
     * <blockquote>
     * <pre>
            = BTBits.LEAVE_BIT
            | BTBits.STANDARD_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            LEAVE_STANDARD
            = BTBits.LEAVE_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * booking with no attendance due to leave with no payment required
     * <blockquote>
     * <pre>
            = BTBits.HOLIDAY_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            LEAVE_NOCHARGE
            = BTBits.LEAVE_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * booking with no attendance due to leave with special payment
     * <blockquote>
     * <pre>
            = BTBits.LEAVE_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            LEAVE_SPECIAL
            = BTBits.LEAVE_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * booking with no attendance due to sick
     * <blockquote>
     * <pre>
            = BTBits.SICK_BIT
            | BTBits.STANDARD_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            SICK_STANDARD
            = BTBits.SICK_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * booking with no attendance due to sick with no payment required
     * <blockquote>
     * <pre>
            = BTBits.SICK_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            SICK_NOCHARGE
            = BTBits.SICK_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * booking with no attendance due to sick with special charge
     * <blockquote>
     * <pre>
            = BTBits.SICK_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            SICK_SPECIAL
            = BTBits.SICK_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Absent at time of booking
     * <blockquote>
     * <pre>
            = BTBits.ABSENT_BIT
            | BTBits.STANDARD_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            ABSENT_STANDARD
            = BTBits.ABSENT_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Absent at time of booking with no payment required
     * <blockquote>
     * <pre>
            = BTBits.ABSENT_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            ABSENT_NOCHARGE
            = BTBits.ABSENT_BIT
            | BTBits.NO_CHARGE_BIT;

   /**
     * Absent at time of booking with special charge
     * <blockquote>
     * <pre>
            = BTBits.ABSENT_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            ABSENT_SPECIAL
            = BTBits.ABSENT_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Booking Cancelled
     * <blockquote>
     * <pre>
            = BTBits.CANCELLED_BIT
            | BTBits.STANDARD_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            CANCELLED_STANDARD
            = BTBits.CANCELLED_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Booking cancelled with no payment required
     * <blockquote>
     * <pre>
            = BTBits.CANCELLED_BIT
            | BTBits.NO_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            CANCELLED_NOCHARGE
            = BTBits.CANCELLED_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * Booking cancelled with special charge
     * <blockquote>
     * <pre>
            = BTBits.CANCELLED_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            CANCELLED_SPECIAL
            = BTBits.CANCELLED_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Booking suspended
     * <blockquote>
     * <pre>
            = BTBits.SUSPENDED_BIT
            | BTBits.STANDARD_CHARGE_BIT
     * </pre>
     * </blockquote>
     */
    public static final int
            SUSPENDED_STANDARD
            = BTBits.SUSPENDED_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Booking suspended with no payment required
     * <blockquote>
     * <pre>
            = BTBits.SUSPENDED_BIT
            | BTBits.NO_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            SUSPENDED_NOCHARGE
            = BTBits.SUSPENDED_BIT
            | BTBits.NO_CHARGE_BIT;

    /**
     * Booking suspended with special charge
     * <blockquote>
     * <pre>
            = BTBits.SUSPENDED_BIT
            | BTBits.SPECIAL_CHARGE_BIT;
     * </pre>
     * </blockquote>
     */
    public static final int
            SUSPENDED_SPECIAL
            = BTBits.SUSPENDED_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Layer identifier indicating a refund
     */
    public static final int
            REFUND_STANDARD
            = BTBits.REFUND_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Layer identifier indicating a refund based on special charge
     */
    public static final int
            REFUND_SPECIAL
            = BTBits.REFUND_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Layer identifier indicating a discount
     */
    public static final int
            DISCOUNT_STANDARD
            = BTBits.DISCOUNT_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Layer identifier indicating a discount based on special charge
     */
    public static final int
            DISCOUNT_SPECIAL
            = BTBits.DISCOUNT_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * Layer identifier indicating a Penalty based
     */
    public static final int
            PENALTY_STANDARD
            = BTBits.PENALTY_BIT
            | BTBits.STANDARD_CHARGE_BIT;

    /**
     * Layer identifier indicating a Penalty based on a special charge
     */
    public static final int
            PENALTY_SPECIAL
            = BTBits.PENALTY_BIT
            | BTBits.SPECIAL_CHARGE_BIT;

    /**
     * used to filter test a bookingTypeId against a mask. The filter treats each of the two
     * bookingTypeId elements separately to allow full flexibility of the masking in both breadth
     * and depth. The two types are TYPE_IDENTIFIER and TYPE_CHARGE.
     *
     * @param bookingTypeBits
     * @param maskBits
     * @return true if the BookingTypeBits fulfil the filter of the maskBits
     */
    public static boolean isFilter(int bookingTypeBits, int maskBits) {
        if(maskBits == ALL_OFF || maskBits == UNDEFINED ) {
            return false;
        }
        if(!contain(bookingTypeBits, TYPE_ALL)
                || !contain(maskBits, TYPE_IDENTIFIER | TYPE_CHARGE)) {
            return false;
        }
        //check the identifiers
        boolean isEqual = contain(maskBits, BTBits.FILTER_EQUALS);
        boolean isMatch = contain(maskBits, BTBits.FILTER_MATCH);

        int identifierMask = maskBits & TYPE_IDENTIFIER;
        int identifierBits = bookingTypeBits & TYPE_IDENTIFIER;
        boolean isIdentifier = true;
        if(identifierMask > 0x0) {
            if(isEqual) {
                isIdentifier = equals(identifierBits, identifierMask);
            } else if(isMatch) {
                isIdentifier = match(identifierBits, identifierMask);
            } else { // conatins
                isIdentifier = contain(identifierBits, identifierMask);
            }
        }
        int chargeMask = maskBits & TYPE_CHARGE;
        int chargeBits = bookingTypeBits & TYPE_CHARGE;
        boolean isCharge = true;
        if(chargeMask > 0x0){
            if(isEqual){
                isCharge = equals(chargeBits, chargeMask);
            } else if(isMatch) {
                isCharge = match(chargeBits, chargeMask);
            } else { // conatins
                isCharge = contain(chargeBits, chargeMask);
            }
        }
        // all must be true
        return(isIdentifier && isCharge);
    }

    /**
     * sets a bookingTypeId with a new identifier leaving the charge unchanged
     *
     * @param bookingTypeId the value to set
     * @param btBitToSet the value to set it with
     * @return bookingTypeId with the changed Identifier
     */
    public static int setIdentifier(int bookingTypeId, int btBitToSet) {
        if(isNotBits(btBitToSet, TYPE_IDENTIFIER)) {
            return bookingTypeId;
        }
        return turnOn(turnOff(bookingTypeId, TYPE_IDENTIFIER), btBitToSet);
    }

    /**
     * sets a bookingTypeId with a new charge, leaving the identifier unchanged
     *
     * @param bookingTypeId the value to set
     * @param btBitToSet the value to set it with
     * @return bookingTypeId with the changed charge
     */
    public static int setCharge(int bookingTypeId, int btBitToSet) {
        if(isNotBits(btBitToSet, TYPE_CHARGE)) {
            return bookingTypeId;
        }
        return turnOn(turnOff(bookingTypeId, TYPE_CHARGE), btBitToSet);
    }

    /**
     * Validation method to check a bookingTypeId is a valid bookingTypeId
     *
     * @param bookingTypeId
     * @param typeMask
     * @return true if it is a valid bookingTypeId
     */
    public static boolean isValid(int bookingTypeId, int typeMask) {
        boolean isId = false;
        if(match(typeMask, TYPE_PHYSICAL_BOOKING)) {
            isId = equals(bookingTypeId, ATTENDING_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, ATTENDING_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, ATTENDING_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, HOLIDAY_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, HOLIDAY_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, HOLIDAY_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, LEAVE_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, LEAVE_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, LEAVE_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, SICK_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, SICK_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, SICK_SPECIAL) ? true : isId;
        }
        if(match(typeMask, TYPE_BOOKING)) {
            isId = equals(bookingTypeId, WAITING_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, WAITING_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, WAITING_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, ABSENT_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, ABSENT_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, ABSENT_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, CANCELLED_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, CANCELLED_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, CANCELLED_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, SUSPENDED_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, SUSPENDED_NOCHARGE) ? true : isId;
            isId = equals(bookingTypeId, SUSPENDED_SPECIAL) ? true : isId;
        }
        if(match(typeMask, TYPE_LAYER)) {
            isId = equals(bookingTypeId, REFUND_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, REFUND_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, DISCOUNT_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, DISCOUNT_SPECIAL) ? true : isId;
            isId = equals(bookingTypeId, PENALTY_STANDARD) ? true : isId;
            isId = equals(bookingTypeId, PENALTY_SPECIAL) ? true : isId;
        }
        return(isId);
    }

    /**
     * Helper method that converts BTBits to a string list. Each
     * bit that is set will add a string representation to the list.
     *
     * @param bookingTypeId the BTBits to be converted
     * @return a list of strings
     */
    public static String getAllStrings(int bookingTypeId) {

        if(match(bookingTypeId, ATTENDING_NOCHARGE)) { return("ATTENDING_NOCHARGE"); }
        if(match(bookingTypeId, ATTENDING_SPECIAL)) { return("ATTENDING_SPECIAL"); }
        if(match(bookingTypeId, ATTENDING_STANDARD)) { return("ATTENDING_STANDARD"); }

        if(match(bookingTypeId, WAITING_NOCHARGE)) { return("WAITING_NOCHARGE"); }
        if(match(bookingTypeId, WAITING_SPECIAL)) { return("WAITING_SPECIAL"); }
        if(match(bookingTypeId, WAITING_STANDARD)) { return("WAITING_STANDARD"); }

        if(match(bookingTypeId, HOLIDAY_NOCHARGE)) { return("HOLIDAY_NOCHARGE"); }
        if(match(bookingTypeId, HOLIDAY_SPECIAL)) { return("HOLIDAY_SPECIAL"); }
        if(match(bookingTypeId, HOLIDAY_STANDARD)) { return("HOLIDAY_STANDARD"); }

        if(match(bookingTypeId, LEAVE_NOCHARGE)) { return("LEAVE_NOCHARGE"); }
        if(match(bookingTypeId, LEAVE_SPECIAL)) { return("LEAVE_SPECIAL"); }
        if(match(bookingTypeId, LEAVE_STANDARD)) { return("LEAVE_STANDARD"); }

        if(match(bookingTypeId, SICK_NOCHARGE)) { return("SICK_NOCHARGE"); }
        if(match(bookingTypeId, SICK_SPECIAL)) { return("SICK_SPECIAL"); }
        if(match(bookingTypeId, SICK_STANDARD)) { return("SICK_STANDARD"); }

        if(match(bookingTypeId, ABSENT_NOCHARGE)) { return("ABSENT_NOCHARGE"); }
        if(match(bookingTypeId, ABSENT_SPECIAL)) { return("ABSENT_SPECIAL"); }
        if(match(bookingTypeId, ABSENT_STANDARD)) { return("ABSENT_STANDARD"); }

        if(match(bookingTypeId, CANCELLED_NOCHARGE)) { return("CANCELLED_NOCHARGE"); }
        if(match(bookingTypeId, CANCELLED_SPECIAL)) { return("CANCELLED_SPECIAL"); }
        if(match(bookingTypeId, CANCELLED_STANDARD)) { return("CANCELLED_STANDARD"); }

        if(match(bookingTypeId, SUSPENDED_NOCHARGE)) { return("SUSPENDED_NOCHARGE"); }
        if(match(bookingTypeId, SUSPENDED_SPECIAL)) { return("SUSPENDED_SPECIAL"); }
        if(match(bookingTypeId, SUSPENDED_STANDARD)) { return("SUSPENDED_STANDARD"); }

        if(match(bookingTypeId, DISCOUNT_SPECIAL)) { return("DISCOUNT_SPECIAL"); }
        if(match(bookingTypeId, DISCOUNT_STANDARD)) { return("DISCOUNT_STANDARD"); }

        if(match(bookingTypeId, PENALTY_SPECIAL)) { return("PENALTY_SPECIAL"); }
        if(match(bookingTypeId, PENALTY_STANDARD)) { return("PENALTY_STANDARD"); }

        if(match(bookingTypeId, REFUND_SPECIAL)) { return("REFUND_SPECIAL"); }
        if(match(bookingTypeId, REFUND_STANDARD)) { return("REFUND_STANDARD"); }

        return("UNDEFINED");

    }

}
