/*
 * @(#)BookingExceptionBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

/**
 * The {@code BookingExceptionBean} Class represents a bookings that produced an exception
 * due to a change in Start, Duration or status of the original booking. So the pre and post
 * are states the pre booking that was or is and the post booking that is or should be.
 *
 * The beans are created on the fly so no persistence is required
 *
 * @author Darryl Oatridge
 * @version 1.00 14-Aug-2010
 */
public class BookingExceptionBean {
    private volatile int ywd;                           // this is the key in the BM to find the relevant booking
    private volatile int childId;                       // the child related to this attempted booking (required in case a bookingId could not be created)
    private volatile int bookingId;                     // the newly created bookingId (if there is one)
    private volatile int preBookingTypeId;              // the original bookingTyepId (if there was one)
    private volatile int preBookingSd;                  // the original planned sd
    private volatile int preSpanSd;                     // the original spanSd
    private volatile int postBookingTypeId;             // the new bookingTyepId
    private volatile int postBookingSd;                 // the new planned sd
    private volatile Type type;                         // an indicator to why the exception

    public static enum Type{
        BOOKING_EXISTING,       // When creating from a booking request, a booking already exists
        BOOKING_SD_CHANGE,      // the booking or request had a periodSd that was not valid
        BOOKING_CLOSED_ROOM;    // the booking change was to a closed room
    }

    public BookingExceptionBean(int ywd, int bookingId, int childId, int preBookingTypeId, int preBookingSd,
            int preSpanSd, int postBookingTypeId, int postBookingSd, Type type) {
        this.ywd = ywd;
        this.childId = childId;
        this.bookingId = bookingId;
        this.preBookingTypeId = preBookingTypeId;
        this.preBookingSd = preBookingSd;
        this.preSpanSd = preSpanSd;
        this.postBookingTypeId = postBookingTypeId;
        this.postBookingSd = postBookingSd;
        this.type = type;
    }

    public int getYwd() {
        return ywd;
    }

    public int getChildId() {
        return childId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getPostBookingSd() {
        return postBookingSd;
    }

    public int getPreSpanSd() {
        return preSpanSd;
    }

    public int getPreBookingTypeId() {
        return preBookingTypeId;
    }

    public int getPreBookingSd() {
        return preBookingSd;
    }

    public int getPostBookingTypeId() {
        return postBookingTypeId;
    }

    public Type getType() {
        return type;
    }

    public boolean isCleanBooking() {
        return (bookingId != -1 && preBookingTypeId==postBookingTypeId && preBookingSd==postBookingSd);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean->\n");
        sb.append("BookingExceptionBean\n");
        sb.append("\tywd = ").append(ywd).append("\n");
        sb.append("\tchildId = ").append(childId).append("\n");
        sb.append("\tbookingId = ").append(bookingId).append("\n");
        sb.append("\tpreBookingSd = ").append(preBookingSd).append("\n");
        sb.append("\tpreSpanSd = ").append(preSpanSd).append("\n");
        sb.append("\tpreBookingTypeId = ").append(preBookingTypeId).append("\n");
        sb.append("\tpostBookingSd = ").append(postBookingSd).append("\n");
        // does not need post booking spanSd
        sb.append("\tpostBookingTypeId = ").append(postBookingTypeId).append("\n");
        sb.append("\ttype = ").append(type.toString()).append("\n");
        sb.append("[Not an ObjectBean]\n\n");
        return sb.toString();
    }

}
