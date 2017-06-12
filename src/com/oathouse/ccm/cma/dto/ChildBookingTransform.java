/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)ChildBookingTransform.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.config.RoomConfigBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.ContactBean;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code ChildBookingTransform} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 26-Mar-2012
 */
public class ChildBookingTransform {

    /**
     * Transforms a child booking into a childBookingDTO
     *
     * @param booking the booking to convert
     * @return a childBookingDTO
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public static ChildBookingDTO toDTO(BookingBean booking) throws PersistenceException, NoSuchIdentifierException {
        ChildBookingDTO rtnDTO;
        // fail safe
        if(booking == null) {
            return(null);
        }

        // get all the values needed
        RoomConfigBean room = AgeRoomService.getInstance().getRoomConfigManager().getObject(booking.getRoomId());
        BookingTypeBean bookingType = ChildBookingService.getInstance().getBookingTypeManager().getObject(booking.getBookingTypeId());
        ChildBean child = ChildService.getInstance().getChildManager().getObject(booking.getProfileId());

        ContactBean liability = ChildService.getInstance().getContactManager().getObject(booking.getLiableContactId());

        // drop off and pickup are null if not set
        ContactBean bookingDropOff = null;
        ContactBean bookingPickUp = null;
        ContactBean actualDropOff = null;
        ContactBean actualPickup = null;
        if(booking.getBookingDropOffId() > 0) {
            bookingDropOff = ChildService.getInstance().getContactManager().getObject(booking.getBookingDropOffId());
        }
        if(booking.getBookingPickupId() > 0) {
            bookingPickUp = ChildService.getInstance().getContactManager().getObject(booking.getBookingPickupId());
        }
        if(booking.getActualDropOffId() > 0) {
            actualDropOff = ChildService.getInstance().getContactManager().getObject(booking.getActualDropOffId());
        }
        if(booking.getActualPickupId() > 0) {
            actualPickup = ChildService.getInstance().getContactManager().getObject(booking.getActualPickupId());
        }
        // in the DTO the actualSD is only set if the bookingType is ATTENDING
        int actualSd = booking.getActualSd();
        if(!BTIdBits.isFilter(booking.getBookingTypeId(), BTBits.ATTENDING_BIT)) {
            actualSd = -1;
        }

        // create the DTO
        rtnDTO = new ChildBookingDTO(booking.getYwd(), booking.getBookingId(), room, child, booking.getBookingSd(),
                booking.getSpanSd(), actualSd, bookingType, booking.getActionBits(), booking.getState(),
                liability, bookingDropOff, actualDropOff, bookingPickUp, actualPickup, booking.getRequestedYwd(),
                booking.getNotes());

        return (rtnDTO);
    }

    /**
     * Transforms a list of child booking beans into a list of ChildBookingDTO
     *
     * @param bookingList the List of bookings to transform
     * @return the transformed bookings
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public static List<ChildBookingDTO> toDTO(List<BookingBean> bookingList) throws PersistenceException, NoSuchIdentifierException {
        List<ChildBookingDTO> rtnList = new LinkedList<ChildBookingDTO>();
        // convert each booking
        for(BookingBean booking : bookingList) {
            rtnList.add(ChildBookingTransform.toDTO(booking));
        }
        return rtnList;
    }

    private ChildBookingTransform() {
    }
}
