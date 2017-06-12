/*
 * @(#)ChildBookingDTO.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.profile.*;
import com.oathouse.oss.storage.valueholder.*;

/**
 * The {@code ChildBookingDTO} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 26-Mar-2012
 */
public class ChildBookingDTO {

    private int ywd;
    private int bookingId;
    private RoomConfigBean room;
    private ChildBean child;
    private int bookingSd;
    private int spanSd;
    private int actualSd;
    private BookingTypeBean bookingType;
    private int actionBits;
    private BookingState state;
    private ContactBean liableContact;
    private ContactBean bookingDropOff;
    private ContactBean actualDropOff;
    private ContactBean bookingPickup;
    private ContactBean actualPickup;
    private int requestedYwd;
    private String notes;

    public ChildBookingDTO(int ywd, int bookingId, RoomConfigBean room, ChildBean child, int bookingSd, int spanSd,
            int actualSd, BookingTypeBean bookingType, int actionBits, BookingState state, ContactBean liableContact,
            ContactBean bookingDropOff, ContactBean actualDropOff, ContactBean bookingPickup, ContactBean actualPickup,
            int requestedYwd, String notes) {
        this.ywd = ywd;
        this.bookingId = bookingId;
        this.room = room;
        this.child = child;
        this.bookingSd = bookingSd;
        this.spanSd = spanSd;
        this.actualSd = actualSd;
        this.bookingType = bookingType;
        this.actionBits = actionBits;
        this.state = state;
        this.liableContact = liableContact;
        this.bookingDropOff = bookingDropOff;
        this.actualDropOff = actualDropOff;
        this.bookingPickup = bookingPickup;
        this.actualPickup = actualPickup;
        this.requestedYwd = requestedYwd;
        this.notes = notes;
    }

    public ChildBookingDTO() {
    }

    public int getYwd() {
        return ywd;
    }

    public int getBookingId() {
        return bookingId;
    }

    public BookingTypeBean getBookingType() {
        return bookingType;
    }

    public int getActionBits() {
        return actionBits;
    }

    public BookingState getState() {
        return state;
    }

    public ChildBean getChild() {
        return child;
    }

    public RoomConfigBean getRoom() {
        return room;
    }

    public ContactBean getLiableContact() {
        return liableContact;
    }

    public int getRequestedYwd() {
        return requestedYwd;
    }

    public String getNotes() {
        return notes;
    }

    public ContactBean getBookingDropOffContact() {
        return bookingDropOff;
    }

    public ContactBean getBookingPickupContact() {
        return bookingPickup;
    }

    public ContactBean getActualDropOffContact() {
        return actualDropOff;
    }

    public ContactBean getActualPickupContact() {
        return actualPickup;
    }

    public int getBookingSd() {
        return bookingSd + 1;
    }

    public int getBookingStart() {
        return SDHolder.getStart(bookingSd);
    }

    public int getBookingEnd() {
        return SDHolder.getEndOut(bookingSd);
    }

    public int getBookingDuration() {
        return SDHolder.getDurationOut(bookingSd);
    }

    public int getSpanSd() {
        return spanSd + 1;
    }

    public int getSpanStart() {
        return SDHolder.getStart(spanSd);
    }

    public int getSpanEnd() {
        return SDHolder.getEndOut(spanSd);
    }

    public int getSpanDuration() {
        return SDHolder.getDurationOut(spanSd);
    }

    public int getActualSd() {
        if(actualSd > 0) {
            return actualSd + 1;
        }
        return -1;
    }

    public int getActualStart() {
        if(actualSd > 0) {
            return SDHolder.getStart(actualSd);
        }
        return -1;
    }

    public int getActualEnd() {
        if(actualSd > 0) {
            return SDHolder.getEndOut(actualSd);
        }
        return -1;
    }

    public int getActualDuration() {
        if(actualSd > 0) {
            return SDHolder.getDurationOut(actualSd);
        }
        return -1;
    }

    public boolean isState(BookingState state) {
        return this.state.equals(state);
    }

    /**
     * The DTO state is before the passed state
     *
     * @param state
     * @return true if this.state is before the passed state
     */
    public boolean isBeforeState(BookingState state) {
        return BookingState.isBefore(this.state, state);
    }

    /**
     * the DTO state is immediately before
     *
     * @param state
     * @return true if this.state is immediately before the passed state
     */
    public boolean isImmediatelyBeforeState(BookingState state) {
        return BookingState.isImmediatelyBefore(this.state, state);
    }

    /**
     * the DTO state is this state or after
     *
     * @param state
     * @return true if this.state is immediately before the passed state
     */
    public boolean isEqualOrAfterState(BookingState state) {
        return BookingState.isEqualOrAfter(this.state, state);
    }

    /**
     * the DTO state is of Type stateType. The types are also BookingState
     * enumerations but are
     *
     * @param stateType
     * @return true is the this.state is of a BookingState type
     */
    public boolean isStateType(BookingState stateType) {
        return BookingState.isStateType(this.state, stateType);
    }

    /**
     * Given an ActionBits bit, the method will test to see if the
     * action bit is on or set. An Action bit is said to be 'on' when
     * the bit value is 1.
     *
     * @return true is the action bit(s) is on false otherwise
     * @see ActionBits
     */
    public boolean isActionOn(int bits) {
        return ActionBits.isBits(this.actionBits, bits);
    }

    /**
     * Tests against the bookingType to see if the passed flag bits are on
     * @param bits
     * @return true if the bits are on
     */
    public boolean isFlagOn(int bits) {
        return bookingType.isFlagOn(bits);
    }

    /**
     * Tests against the bookingType to check they are of the same type
     * @param bookingTypeId
     * @return
     */
    public boolean isType(int bookingTypeId) {
        return bookingType.isType(bookingTypeId);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ChildBookingDTO other = (ChildBookingDTO) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.bookingId != other.bookingId) {
            return false;
        }
        if(this.room != other.room && (this.room == null || !this.room.equals(other.room))) {
            return false;
        }
        if(this.child != other.child && (this.child == null || !this.child.equals(other.child))) {
            return false;
        }
        if(this.bookingSd != other.bookingSd) {
            return false;
        }
        if(this.spanSd != other.spanSd) {
            return false;
        }
        if(this.actualSd != other.actualSd) {
            return false;
        }
        if(this.bookingType != other.bookingType && (this.bookingType == null || !this.bookingType.equals(other.bookingType))) {
            return false;
        }
        if(this.actionBits != other.actionBits) {
            return false;
        }
        if(this.state != other.state) {
            return false;
        }
        if(this.liableContact != other.liableContact && (this.liableContact == null || !this.liableContact.equals(other.liableContact))) {
            return false;
        }
        if(this.bookingDropOff != other.bookingDropOff && (this.bookingDropOff == null || !this.bookingDropOff.equals(other.bookingDropOff))) {
            return false;
        }
        if(this.actualDropOff != other.actualDropOff && (this.actualDropOff == null || !this.actualDropOff.equals(other.actualDropOff))) {
            return false;
        }
        if(this.bookingPickup != other.bookingPickup && (this.bookingPickup == null || !this.bookingPickup.equals(other.bookingPickup))) {
            return false;
        }
        if(this.actualPickup != other.actualPickup && (this.actualPickup == null || !this.actualPickup.equals(other.actualPickup))) {
            return false;
        }
        if(this.requestedYwd != other.requestedYwd) {
            return false;
        }
        if((this.notes == null) ? (other.notes != null) : !this.notes.equals(other.notes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.ywd;
        hash = 29 * hash + this.bookingId;
        hash = 29 * hash + (this.room != null ? this.room.hashCode() : 0);
        hash = 29 * hash + (this.child != null ? this.child.hashCode() : 0);
        hash = 29 * hash + this.bookingSd;
        hash = 29 * hash + this.spanSd;
        hash = 29 * hash + this.actualSd;
        hash = 29 * hash + (this.bookingType != null ? this.bookingType.hashCode() : 0);
        hash = 29 * hash + this.actionBits;
        hash = 29 * hash + (this.state != null ? this.state.hashCode() : 0);
        hash = 29 * hash + (this.liableContact != null ? this.liableContact.hashCode() : 0);
        hash = 29 * hash + (this.bookingDropOff != null ? this.bookingDropOff.hashCode() : 0);
        hash = 29 * hash + (this.actualDropOff != null ? this.actualDropOff.hashCode() : 0);
        hash = 29 * hash + (this.bookingPickup != null ? this.bookingPickup.hashCode() : 0);
        hash = 29 * hash + (this.actualPickup != null ? this.actualPickup.hashCode() : 0);
        hash = 29 * hash + this.requestedYwd;
        hash = 29 * hash + (this.notes != null ? this.notes.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "BookingDTO{" + "ywd=" + ywd + ", bookingId=" + bookingId + ", room=" + room + ", child=" + child + ", bookingSd=" + bookingSd + ", spanSd=" + spanSd + ", actualSd=" + actualSd + ", bookingType=" + bookingType + ", actionBits=" + actionBits + ", state=" + state + ", liableContact=" + liableContact + ", bookingDropOff=" + bookingDropOff + ", actualDropOff=" + actualDropOff + ", bookingPickup=" + bookingPickup + ", actualPickup=" + actualPickup + ", requestedYwd=" + requestedYwd + ", notes=" + notes + '}';
    }
}
