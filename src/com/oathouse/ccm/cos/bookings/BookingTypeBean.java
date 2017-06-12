/*
 * @(#)BookingTypeBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code BookingTypeBean} Class relates to a booking and the type
 * of booking it should be. The type bean allow a booking to be tailored
 * during its lifetime to act or be presented in different ways depending
 * upon this type.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Oct-2011
 */
public class BookingTypeBean extends ObjectBean {

    private static final long serialVersionUID = 21001007100L;

    // type string reference
    private volatile String name;
    private volatile int flagBits;
    private volatile int chargePercent;

    public BookingTypeBean(int bookingTypeId, String name, int flagBits, int chargePercent, String owner) {
        super(bookingTypeId, owner);
        this.name = name != null ? name : "UNDEFINED";
        this.flagBits = flagBits;
        this.chargePercent = chargePercent;
    }

    public BookingTypeBean() {
        super();
        this.name = "";
        this.flagBits = ObjectEnum.INITIALISATION.value();
        this.chargePercent = ObjectEnum.INITIALISATION.value();
    }

    public int getBookingTypeId() {
        return super.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public int getFlagBits() {
        return flagBits;
    }

    public int getChargePercent() {
        return chargePercent;
    }

    public boolean isFlagOn(int bits) {
        return BTFlagIdBits.isFlag(flagBits, bits);
    }

    public boolean isType(int bookingTypeId) {
        return BTIdBits.isFilter(super.getIdentifier(), bookingTypeId | BTFlagBits.FILTER_MATCH);
    }

    protected void setFlagBits(int flagBits, String owner) {
        this.flagBits = flagBits;
        super.setOwner(owner);
    }

    protected void setChargePercent(int chargePercent, String owner) {
        this.chargePercent = chargePercent;
        super.setOwner(owner);
    }

    public static final Comparator<BookingTypeBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<BookingTypeBean>() {
        @Override
        public int compare(BookingTypeBean p1, BookingTypeBean p2) {
            if(p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(p1 != null && p2 == null) {
                return -1;
            }
            if(p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) p1.getIdentifier()).compareTo((Integer) p2.getIdentifier()));
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final BookingTypeBean other = (BookingTypeBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if(this.flagBits != other.flagBits) {
            return false;
        }
        if(this.chargePercent != other.chargePercent) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 41 * hash + this.flagBits;
        hash = 41 * hash + this.chargePercent;
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<Element>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element("BookingTypeBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("flagBits", Integer.toString(flagBits));
        bean.setAttribute("chargePercent", Integer.toString(chargePercent));

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
        Element bean = root.getChild("BookingTypeBean");
        // set up the data
        name = bean.getAttributeValue("name", "");
        flagBits = Integer.parseInt(bean.getAttributeValue("flagBits", Integer.toString(ObjectEnum.INITIALISATION.value())));
        chargePercent = Integer.parseInt(bean.getAttributeValue("chargePercent", Integer.toString(ObjectEnum.INITIALISATION.value())));

    }
}
