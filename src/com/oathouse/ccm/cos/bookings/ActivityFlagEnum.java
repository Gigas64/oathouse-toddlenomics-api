/*
 * @(#)ActivityFlagEnum.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

/**
 * The {@code ActivityFlagEnum} Enumeration
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Mar-2013
 */
public enum ActivityFlagEnum {
    // internal
    UNDEFINED,
    // not Set
    NO_VALUE,

    PRE_BOOKING,
    BOOKING,
    ACTIVE_BOOKING,
    POST_BOOKING;
}
