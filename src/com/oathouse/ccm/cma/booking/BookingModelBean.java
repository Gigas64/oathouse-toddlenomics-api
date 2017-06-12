/*
 * @(#)BookingModelBean.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

/**
 * The {@code BookingModelBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Nov-2013
 */
public class BookingModelBean {

    private volatile int roomId; // the room this applies to
    private volatile int bookingSd; // the booking SDHolder key value for the booking period
    private volatile int accountId; // the account this booking is applied to
    private volatile int profileId; // the profileId this booking is applied to
    private volatile int bookingTypeId; // the booking type object reference
    private volatile long modified; // when the DayRange was modified

    public BookingModelBean(int roomId, int bookingSd, int accountId, int profileId, int bookingTypeId, long modified) {
        this.roomId = roomId;
        this.bookingSd = bookingSd;
        this.accountId = accountId;
        this.profileId = profileId;
        this.bookingTypeId = bookingTypeId;
        this.modified = modified;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getBookingSd() {
        return bookingSd;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getBookingTypeId() {
        return bookingTypeId;
    }

    public long getModified() {
        return modified;
    }

    public boolean isModified(long timestamp) {
        return !(modified == timestamp);
    }

}