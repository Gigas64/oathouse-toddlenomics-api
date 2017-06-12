/**
 * @(#)BookingBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code BookingBean} Class used to store each booking for the week
 *
 * @author 	Darryl Oatridge
 * @version 	2.52 20-Oct-2010
 */
public class BookingBean extends ObjectBean {

    private static final long serialVersionUID = 20101020252L;
    private volatile int ywd;
    private volatile int roomId;
    private volatile int bookingSd; //the booking SDHolder key value for the booking period
    private volatile int spanSd; // to span of a booking since first booked (represents extremes of booking history)
    private volatile int actualStart; // when the booking actually started
    private volatile int actualEnd; // when the booking actually ended
    private volatile int profileId; // the profileId this booking is applied to
    private volatile int liableContactId; // the person who requested the booking and is liable
    private volatile int bookingDropOffId; // the person who will be dropping off
    private volatile int actualDropOffId; // the actual person who dropping off
    private volatile int bookingPickupId; // the person who will be picking up
    private volatile int actualPickupId; // the actual person who picked up
    private volatile int bookingTypeId; // the booking type object reference
    private volatile int actionBits; // a bit flag indicating actions for the booking
    private volatile int requestedYwd; // the ywd the booking was requested
    private volatile BookingState state;
    private volatile String notes;

    /**
     * Constructor to create a new booking bean
     *
     * @param bookingId the booking Id
     * @param ywd the year, week, day of the booking
     * @param roomId the room to place the booking in
     * @param profileId the id of the person the booking is for
     * @param bookingSd the booking start-duration time
     * @param spanSd the booking potential charge start-duration span
     * @param actualStart the actual time the booking started
     * @param actualEnd the actual time the booking ended
     * @param liableContactId the person responsible for the booking (childBooking)
     * @param bookingDropOffId the value of bookingDropOffId
     * @param bookingPickupId the person identified to pickup (childBooking)
     * @param actualDropOffId the value of actualDropOffIdId
     * @param actualPickupId the person identified to pickup (childBooking)
     * @param bookingTypeId the bookingTypeBean id of the booking
     * @param actionBits the action bit for this booking
     * @param state the state of the booking
     * @param requestedYwd the ywd the booking was requested
     * @param notes additional information
     * @param owner
     */
    public BookingBean(int bookingId, int ywd, int roomId, int profileId, int bookingSd, int spanSd, int actualStart, int actualEnd, int liableContactId, int bookingDropOffId, int bookingPickupId, int actualDropOffId, int actualPickupId, int bookingTypeId, int actionBits, BookingState state, int requestedYwd, String notes, String owner) {
        super(bookingId, owner);
        this.ywd = ywd;
        this.roomId = roomId;
        this.bookingSd = bookingSd;
        this.spanSd = spanSd;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;
        this.profileId = profileId;
        this.liableContactId = liableContactId;
        this.bookingDropOffId = bookingDropOffId;
        this.bookingPickupId = bookingPickupId;
        this.actualDropOffId = actualDropOffId;
        this.actualPickupId = actualPickupId;
        this.bookingTypeId = bookingTypeId;
        this.requestedYwd = requestedYwd;
        this.notes = notes;
        this.actionBits = actionBits;
        this.state = state;
    }

     public BookingBean() {
        super();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.roomId = ObjectEnum.INITIALISATION.value();
        this.bookingSd = ObjectEnum.INITIALISATION.value();
        this.spanSd = ObjectEnum.INITIALISATION.value();
        this.actualStart = ObjectEnum.INITIALISATION.value();
        this.actualEnd = ObjectEnum.INITIALISATION.value();
        this.profileId = ObjectEnum.INITIALISATION.value();
        this.liableContactId = ObjectEnum.INITIALISATION.value();
        this.bookingDropOffId = ObjectEnum.INITIALISATION.value();
        this.bookingPickupId = ObjectEnum.INITIALISATION.value();
        this.actualDropOffId = ObjectEnum.INITIALISATION.value();
        this.actualPickupId = ObjectEnum.INITIALISATION.value();
        this.bookingTypeId = ObjectEnum.INITIALISATION.value();
        this.actionBits = ObjectEnum.INITIALISATION.value();
        this.requestedYwd = ObjectEnum.INITIALISATION.value();
        this.notes = "";
        this.state = BookingState.UNDEFINED;
    }

    public int getBookingId() {
        return super.getIdentifier();
    }

    public int getYear() {
        return YWDHolder.getYear(getYwd());
    }

    public int getWeek() {
        return YWDHolder.getWeek(getYwd());
    }

    public int getDay() {
        return YWDHolder.getDay(getYwd());
    }

    public int getYwd() {
        return ywd;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getBookingSd() {
        return bookingSd;
    }

    public int getBookingStart() {
        return (SDHolder.getStart(bookingSd));
    }

    public int getBookingEnd() {
        return (SDHolder.getEnd(bookingSd));
    }

    public int getBookingEndOut() {
        return (SDHolder.getEndOut(bookingSd));
    }

    public int getBookingDuration() {
        return (SDHolder.getDuration(bookingSd));
    }

    public int getActualSd() {
        if(actualStart != ObjectEnum.INITIALISATION.value() && actualEnd != ObjectEnum.INITIALISATION.value()) {
            return(SDHolder.getSD(actualStart, actualEnd-actualStart));
        }
        return(ObjectEnum.INITIALISATION.value());
    }

    public int getActualStart() {
        return actualStart;
    }

    public int getActualEnd() {
        return actualEnd;
    }

    public int getActualEndOut() {
        // TIME ADJUSTMENT: add one to the duration
        if(SDHolder.getDuration(actualEnd) > 0) {
            return actualEnd + 1;
        }
        return actualEnd;
    }

    public int getSpanSd() {
        return spanSd;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getLiableContactId() {
        return liableContactId;
    }

    public int getBookingDropOffId() {
        return bookingDropOffId;
    }

    public int getBookingPickupId() {
        return bookingPickupId;
    }

    public int getActualDropOffId() {
        return actualDropOffId;
    }

    public int getActualPickupId() {
        return actualPickupId;
    }

    public int getBookingTypeId() {
        return bookingTypeId;
    }

    /**
     * @return chargeType from the BookingTypeId
     */
    public int getBookingTypeChargeBit() {
        return BTIdBits.turnOff(bookingTypeId, BTIdBits.TYPE_IDENTIFIER | BTIdBits.TYPE_LAYER);
    }

    public int getRequestedYwd() {
        return requestedYwd;
    }

    public int getActionBits() {
        return actionBits;
    }

    public String getNotes() {
        return notes;
    }

    public BookingState getState() {
        return state;
    }

    public boolean isRoomDay(int roomId, int day) {
        if(this.roomId == roomId && YWDHolder.getDay(ywd) == day) {
            return (true);
        }
        return (false);
    }

    public boolean isState(BookingState state) {
        if(this.state.equals(state)) {
            return (true);
        }
        return (false);
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
     * the booking state is immediately before
     *
     * @param state
     * @return true if this.state is immediately before the passed state
     */
    public boolean isImmediatelyBeforeState(BookingState state) {
        return BookingState.isImmediatelyBefore(this.state, state);
    }

    /**
     * the booking state is this state or after
     *
     * @param state
     * @return true if this.state is immediately before the passed state
     */
    public boolean isEqualOrAfterState(BookingState state) {
        return BookingState.isEqualOrAfter(this.state, state);
    }

    /**
     * the booking state is of Type stateType. The types are also BookingState
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
        return ActionBits.isBits(actionBits, bits);
    }

    protected void setRoomId(int roomId, String owner) {
        this.roomId = roomId;
        super.setOwner(owner);    }

    protected void setLiableContactId(int liableContactId, String owner) {
        this.liableContactId = liableContactId;
        super.setOwner(owner);
    }

    protected void setBookingTypeId(int bookingTypeId, String owner) {
        this.bookingTypeId = bookingTypeId;
        super.setOwner(owner);
    }

    protected void setState(BookingState state, String owner) {
        this.state = state;
        super.setOwner(owner);
    }

    protected void setBookingSd(int periodSd, String owner) {
        if(isBeforeState(BookingState.AUTHORISED)) {
            this.bookingSd = periodSd;
            super.setOwner(owner);
        }
    }

    protected void setSpanSd(int periodSd, String owner) {
        if(isBeforeState(BookingState.AUTHORISED)) {
            this.spanSd = periodSd;
            super.setOwner(owner);
        }
    }

    protected void setActualStart(int actualStart, String owner) {
        this.actualStart = actualStart;
        super.setOwner(owner);
    }

    protected void setActualEnd(int actualEnd, String owner) {
        this.actualEnd = actualEnd;
        super.setOwner(owner);
    }

    protected void setBookingDropOffId(int bookingDropOffId, String owner) {
        this.bookingDropOffId = bookingDropOffId;
        super.setOwner(owner);
    }

    protected void setBookingPickupId(int bookingPickupId, String owner) {
        this.bookingPickupId = bookingPickupId;
        super.setOwner(owner);
    }

    protected void setActualDropOffId(int actualDropOffId, String owner) {
        this.actualDropOffId = actualDropOffId;
        super.setOwner(owner);
    }

    protected void setActualPickupId(int actualPickupId, String owner) {
        this.actualPickupId = actualPickupId;
        super.setOwner(owner);
    }

    protected void setRequestedYwd(int requestedYwd, String owner) {
        this.requestedYwd = requestedYwd;
        super.setOwner(owner);
    }

    protected void setActionBits(int actionBits, String owner) {
        this.actionBits = actionBits;
        super.setOwner(owner);
    }

    protected void setNotes(String notes, String owner) {
        this.notes = notes != null ? notes : "";
        super.setOwner(owner);
    }

    public static final Comparator<BookingBean> BOOKING_SD_ORDER = new Comparator<BookingBean>() {
        @Override
        public int compare(BookingBean b1, BookingBean b2) {
            if (b1 == null && b2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (b1 != null && b2 == null) {
                return -1;
            }
            if (b1 == null && b2 != null) {
                return 1;
            }
            // compare
            int result = ((Integer)b1.getBookingSd()).compareTo((Integer)b2.getBookingSd());
            if(result != 0) {
                return result;
            }
            // booking start not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)b1.getIdentifier()).compareTo((Integer)b2.getIdentifier()));
        }
    };

    /**
     * helper static used to create a new booking with default values with spanSd set to bookingSd,
     * all actuals set to ObjectEnum.INITIALISATION.value() and state set to BookingState.OPEN
     *
     * @param bookingId the booking Id
     * @param ywd the year, week, day of the booking
     * @param roomId the room to place the booking in
     * @param profileId the id of the person the booking is for
     * @param bookingSd the booking start-duration time
     * @param liableContactId the person responsible for the booking (childBooking)
     * @param bookingDropOffId the value of bookingDropOffId
     * @param bookingPickupId the person identified to pickup (childBooking)
     * @param bookingTypeId the bookingTypeBean id of the booking
     * @param actionBits the value of actionBits
     * @param requestedYwd the ywd the booking was requested
     * @param notes additional information
     * @param owner
     * @return the newly created BookingBean
     */
    protected static BookingBean create(int bookingId, int ywd, int roomId, int profileId, int bookingSd, int liableContactId, int bookingDropOffId, int bookingPickupId, int bookingTypeId, int actionBits, int requestedYwd, String notes, String owner) {
        return(
                new BookingBean(bookingId, ywd, roomId, profileId, bookingSd, bookingSd, // spanSd set to bookingSd
                        ObjectEnum.INITIALISATION.value(), // actualStart
                        ObjectEnum.INITIALISATION.value(), // actualEnd
                        liableContactId, bookingDropOffId, bookingPickupId,
                        ObjectEnum.INITIALISATION.value(), // actualDropOff
                        ObjectEnum.INITIALISATION.value(), // actualPickup
                        bookingTypeId, actionBits,
                        BookingState.OPEN, // default to OPEN
                        requestedYwd, notes, owner));
    }

    // sorted by ywd then bookingSd
    @Override
    public int compareTo(ObjectBean other) {
        BookingBean booking = (BookingBean) other;
        if(getYwd() != booking.getYwd()) {
            return ((getYwd() - booking.getYwd()) * 100000000);
        }
        return (bookingSd - booking.getBookingSd());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final BookingBean other = (BookingBean) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.roomId != other.roomId) {
            return false;
        }
        if(this.bookingSd != other.bookingSd) {
            return false;
        }
        if(this.spanSd != other.spanSd) {
            return false;
        }
        if(this.actualStart != other.actualStart) {
            return false;
        }
        if(this.actualEnd != other.actualEnd) {
            return false;
        }
        if(this.profileId != other.profileId) {
            return false;
        }
        if(this.liableContactId != other.liableContactId) {
            return false;
        }
        if(this.bookingDropOffId != other.bookingDropOffId) {
            return false;
        }
        if(this.actualDropOffId != other.actualDropOffId) {
            return false;
        }
        if(this.bookingPickupId != other.bookingPickupId) {
            return false;
        }
        if(this.actualPickupId != other.actualPickupId) {
            return false;
        }
        if(this.bookingTypeId != other.bookingTypeId) {
            return false;
        }
        if(this.actionBits != other.actionBits) {
            return false;
        }
        if(this.requestedYwd != other.requestedYwd) {
            return false;
        }
        if(this.state != other.state) {
            return false;
        }
        if((this.notes == null) ? (other.notes != null) : !this.notes.equals(other.notes)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.ywd;
        hash = 17 * hash + this.roomId;
        hash = 17 * hash + this.bookingSd;
        hash = 17 * hash + this.spanSd;
        hash = 17 * hash + this.actualStart;
        hash = 17 * hash + this.actualEnd;
        hash = 17 * hash + this.profileId;
        hash = 17 * hash + this.liableContactId;
        hash = 17 * hash + this.bookingDropOffId;
        hash = 17 * hash + this.actualDropOffId;
        hash = 17 * hash + this.bookingPickupId;
        hash = 17 * hash + this.actualPickupId;
        hash = 17 * hash + this.bookingTypeId;
        hash = 17 * hash + this.actionBits;
        hash = 17 * hash + this.requestedYwd;
        hash = 17 * hash + (this.state != null ? this.state.hashCode() : 0);
        hash = 17 * hash + (this.notes != null ? this.notes.hashCode() : 0);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element(VT.Booking.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("roomId", Integer.toString(roomId));
        bean.setAttribute("bookingSd", Integer.toString(bookingSd));
        bean.setAttribute("spanSd", Integer.toString(spanSd));
        bean.setAttribute("actualStart", Integer.toString(actualStart));
        bean.setAttribute("actualEnd", Integer.toString(actualEnd));
        bean.setAttribute("profileId", Integer.toString(profileId));
        bean.setAttribute("liableContactId", Integer.toString(liableContactId));
        bean.setAttribute("bookingDropOffId", Integer.toString(bookingDropOffId));
        bean.setAttribute("bookingPickupId", Integer.toString(bookingPickupId));
        bean.setAttribute("actualDropOffId", Integer.toString(actualDropOffId));
        bean.setAttribute("actualPickupId", Integer.toString(actualPickupId));
        bean.setAttribute("bookingTypeId", Integer.toString(bookingTypeId));
        bean.setAttribute("requestedYwd", Integer.toString(requestedYwd));
        bean.setAttribute("actionBits", Integer.toString(actionBits));
        bean.setAttribute("notes", notes);
        bean.setAttribute("state", state.toString());
        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to
     * put default values in getAttribute() and check the content
     * of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild(VT.Booking.bean());
        // set up the data
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        roomId = Integer.parseInt(bean.getAttributeValue("roomId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        bookingSd = Integer.parseInt(bean.getAttributeValue("bookingSd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        spanSd = Integer.parseInt(bean.getAttributeValue("spanSd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        actualStart = Integer.parseInt(bean.getAttributeValue("actualStart", Integer.toString(ObjectEnum.INITIALISATION.value())));
        actualEnd = Integer.parseInt(bean.getAttributeValue("actualEnd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        profileId = Integer.parseInt(bean.getAttributeValue("profileId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        liableContactId = Integer.parseInt(bean.getAttributeValue("liableContactId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        bookingDropOffId = Integer.parseInt(bean.getAttributeValue("bookingDropOffId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        bookingPickupId = Integer.parseInt(bean.getAttributeValue("bookingPickupId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        actualDropOffId = Integer.parseInt(bean.getAttributeValue("actualDropOffId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        actualPickupId = Integer.parseInt(bean.getAttributeValue("actualPickupId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        bookingTypeId = Integer.parseInt(bean.getAttributeValue("bookingTypeId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        requestedYwd = Integer.parseInt(bean.getAttributeValue("requestedYwd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        actionBits = Integer.parseInt(bean.getAttributeValue("actionBits", Integer.toString(ObjectEnum.INITIALISATION.value())));
        notes = bean.getAttributeValue("notes", "");
        state = BookingState.valueOf(bean.getAttributeValue("state", "UNDEFINED"));
    }
}
