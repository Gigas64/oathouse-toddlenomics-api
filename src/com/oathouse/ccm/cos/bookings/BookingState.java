/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)BookingState.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.valueholder.AbstractBits;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code BookingState} Enumeration representing the different states of a booking
 *
 * @author Darryl Oatridge
 * @version 1.01 12-Nov-2011
 */
public enum BookingState {

    UNDEFINED(AbstractBits.UNDEFINED), // Initialisation state
    NO_VALUE(AbstractBits.ALL_OFF), // Internal testing
    /**
     * can be deleted or changed without consequences, is in an open state
     */
    OPEN((int) Math.pow(0x2, 0x1)),
    /**
     * a booking is now in a state where changes need to have consequences
     */
    RESTRICTED((int) Math.pow(0x2, 0x2)),
    /**
     * the booking has started with the actual start set
     */
    STARTED((int) Math.pow(0x2, 0x3)),
    /**
     * the booking is completed with actual end set
     */
    COMPLETED((int) Math.pow(0x2, 0x4)),
    /**
     * a booking is checked and authorised for payment
     */
    AUTHORISED((int) Math.pow(0x2, 0x5)),
    /**
     * the booking has been reconciled against billing and billing items created.
     */
    RECONCILED((int) Math.pow(0x2, 0x6)),
    /**
     * The billing items have been paid and the booking archived
     */
    ARCHIVED((int) Math.pow(0x2, 0x7)),

    /**
     * A collective of OPEN and RESTRICTED
     */
    TYPE_BOOKING(OPEN.bit() | RESTRICTED.bit()),
    /**
     * a collective of RESTRICTED STARTED and COMPLETED
     */
    TYPE_NOTICE(RESTRICTED.bit() | STARTED.bit() | COMPLETED.bit()),
    /**
     * a collective of STARTED and COMPLETED
     */
    TYPE_ACTIVE(STARTED.bit() | COMPLETED.bit()),
    /**
     * a collective of AUTHORISED RECONCILED and ARCHIVED
     */
    TYPE_BILLING(AUTHORISED.bit() | RECONCILED.bit() | ARCHIVED.bit()),
    /**
     * a collective of RECONCILED and ARCHIVED
     */
    TYPE_BILLED(RECONCILED.bit() | ARCHIVED.bit()),
    /**
     * a collective of ALL the booking states (Excluding the TYPE_* UNDEFINED and ALL_OFF
     */
    TYPE_ALL(TYPE_BOOKING.bit() | TYPE_ACTIVE.bit() | TYPE_BILLING.bit());

    private int bit;

    private BookingState(int bit) {
        this.bit = bit;
    }

    /**
     * a bit value representation of the enumeration. This can be used to do bitwise
     * comparisons of the enumerations
     * @return
     */
    public int bit() {
        return bit;
    }

    /**
     * BookingState enumerators have associated bit values. This method
     * tests a state against a stateType. Returns true if the state is
     * of a state type.
     *
     * @param state a BookingState value
     * @param type a TYPE_
     * @return true if the state bit value compares favourably with the mask
     * @see AbstractBits
     */
    public static boolean isStateType(BookingState state, BookingState type) {
        if(AbstractBits.isBits(state.bit, type.bit)) {
            return(true);
        }
        return(false);
    }

    /**
     * BookingState enumerators are cascading. This method checks
     * to see if BookingState s1 is before BookingState s2.
     *
     * @param s1 booking state one
     * @param s2 booking state two
     * @return true if s1 is before s2
     */
    public static boolean isBefore(BookingState s1, BookingState s2) {
        if(s1.compareTo(s2) < 0) {
            return true;
        }
        return false;
    }

    /**
     * BookingState enumerators are cascading. This method checks
     * to see if BookingState s1 is equal to or after BookingState s2.
     *
     * @param s1 booking state one
     * @param s2 booking state two
     * @return true if s1 is equal or after s2
     */
    public static boolean isEqualOrAfter(BookingState s1, BookingState s2) {
        if(s1.compareTo(s2) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * BookingState enumerators are cascading. This method checks
     * to see if BookingState s1 is immediately before BookingState s2.
     *
     * @param s1 booking state one
     * @param s2 booking state two
     * @return true if s1 is immediately before s2
     */
    public static boolean isImmediatelyBefore(BookingState s1, BookingState s2) {
        if(s1.compareTo(s2) == -1) {
            return true;
        }
        return false;
    }
}
