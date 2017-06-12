/*
 * @(#)RoomMoveRebookingDTO.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.*;

/**
 * The {@code RoomMoveRebookingDTO} Class represents a potential rebooking based on requested room moves
 *
 * The beans are created on the fly so no persistence is required
 *
 * @author Darryl Oatridge
 * @since 23 June 2012
 */
public class RoomMoveRebookingDTO {

    private volatile BookingBean preBooking;
    private volatile RoomConfigBean preRoom;
    private volatile BookingTypeBean preBookingType;
    private volatile BookingBean postBooking;
    private volatile RoomConfigBean postRoom;
    private volatile BookingTypeBean postBookingType;
    private volatile Type type;

    public static enum Type{
        ROOM_MOVE_OK,             // the booking can be rebooked to the new room without any changes
        ROOM_MOVE_SD_CHANGE,      // the booking or request had a periodSd that was not valid
        ROOM_MOVE_IMPOSSIBLE_CLOSED_ROOM;    // the booking change was to a closed room
    }

    public RoomMoveRebookingDTO(BookingBean preBooking, RoomConfigBean preRoom, BookingTypeBean preBookingType,
                                BookingBean postBooking, RoomConfigBean postRoom, BookingTypeBean postBookingType, Type type) {
        this.preBooking = preBooking;
        this.preRoom = preRoom;
        this.preBookingType = preBookingType;
        this.postBooking = postBooking;
        this.postRoom = postRoom;
        this.postBookingType = postBookingType;
        this.type = type;
    }

    public BookingBean getPostBooking() {
        return postBooking;
    }

    public RoomConfigBean getPostRoom() {
        return postRoom;
    }

    public RoomConfigBean getPreRoom() {
        return preRoom;
    }

    public BookingBean getPreBooking() {
        return preBooking;
    }

    public BookingTypeBean getPostBookingType() {
        return postBookingType;
    }

    public BookingTypeBean getPreBookingType() {
        return preBookingType;
    }

    public Type getType() {
        return type;
    }

}
