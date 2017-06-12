/*
 * @(#)BookingRequestBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.*;
import java.util.*;
import org.jdom2.*;

/**
 * The {@code BookingRequestBean} Class extends {@code ObjectSetBean} where the inherited
 * set contains a number of booking requests for a single day. BookingRequestBean is a
 * template bean for an accountId representing a possible pre booking scenario for a child or
 * children that are associated with an account.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Aug-2010
 */
public class BookingRequestBean extends ObjectSetBean {

    private static final long serialVersionUID = 20100807100L;
    private volatile int accountId;
    private volatile int liableContactId;
    private volatile int dropOffContactId;
    private volatile int pickupContactId;
    private volatile int bookingTypeId;


    public BookingRequestBean(int bookingRequestId, String name, String label, int accountId, int liableContactId, int dropOffContactId, int pickupContactId, Set<Integer> requestSdSet, int bookingTypeId, String owner) {
        super(bookingRequestId,name, label, requestSdSet, owner);
        this.accountId = accountId;
        this.liableContactId = liableContactId;
        this.dropOffContactId = dropOffContactId;
        this.pickupContactId = pickupContactId;
        this.bookingTypeId = bookingTypeId;
    }

    public BookingRequestBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.liableContactId = ObjectEnum.INITIALISATION.value();
        this.dropOffContactId = ObjectEnum.INITIALISATION.value();
        this.pickupContactId = ObjectEnum.INITIALISATION.value();
        this.bookingTypeId = ObjectEnum.INITIALISATION.value();
    }

    /**
     * Returns the identifier for this bean
     */
    public int getBookingRequestId() {
        return (super.getIdentifier());
    }

    /**
     * Shortened version of getBookingRequestId
     */
    public int getRequestId() {
        return (super.getIdentifier());
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getLabel() {
        return super.getLabel();
    }

    public int getAccountId() {
        return accountId;
    }

    public int getLiableContactId() {
        return liableContactId;
    }

    public int getDropOffContactId() {
        return dropOffContactId;
    }

    public int getPickupContactId() {
        return pickupContactId;
    }

    public Set<Integer> getRequestSdSet() {
        return (super.getValueStore());
    }

    /**
     * Shortened version of getRequestSdSet()
     */
    public Set<Integer> getAllSd() {
        return getRequestSdSet();
    }

    public int getBookingTypeId() {
        return bookingTypeId;
    }

    protected void setBookingTypeId(int bookingType, String owner) {
        this.bookingTypeId = bookingType;
        super.setOwner(owner);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final BookingRequestBean other = (BookingRequestBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.liableContactId != other.liableContactId) {
            return false;
        }
        if(this.dropOffContactId != other.dropOffContactId) {
            return false;
        }
        if(this.pickupContactId != other.pickupContactId) {
            return false;
        }
        if(this.bookingTypeId != other.bookingTypeId) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.accountId;
        hash = 47 * hash + this.liableContactId;
        hash = 47 * hash + this.dropOffContactId;
        hash = 47 * hash + this.pickupContactId;
        hash = 47 * hash + this.bookingTypeId;
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
        Element bean = new Element("BookingRequestBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("liableContactId", Integer.toString(liableContactId));
        bean.setAttribute("dropOffContactId", Integer.toString(dropOffContactId));
        bean.setAttribute("pickupContactId", Integer.toString(pickupContactId));
        bean.setAttribute("bookingTypeId", Integer.toString(bookingTypeId));

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
        Element bean = root.getChild("BookingRequestBean");
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        liableContactId = Integer.parseInt(bean.getAttributeValue("liableContactId", "-1"));
        dropOffContactId = Integer.parseInt(bean.getAttributeValue("dropOffContactId", "-1"));
        pickupContactId = Integer.parseInt(bean.getAttributeValue("pickupContactId", "-1"));
        bookingTypeId = Integer.parseInt(bean.getAttributeValue("bookingTypeId", "-1"));
    }
}
