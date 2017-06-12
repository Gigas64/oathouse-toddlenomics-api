/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)BookingForecastBean.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.finance;

import static com.oathouse.ccm.cos.accounts.finance.BillingBean.TAX_ONLY;
import static com.oathouse.ccm.cos.accounts.finance.BillingBean.VALUE_INC_TAX;
import static com.oathouse.ccm.cos.accounts.finance.BillingBean.VALUE_ONLY;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code BookingForecastBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Mar-2013
 */
public class BookingForecastBean extends ObjectBean {

    private static final long serialVersionUID = 100L;

    private volatile int accountId;
    private volatile int ywd;               // the ywd of the billing forecast
    private volatile long value;            // the total ywd value
    private volatile long valueIncTax;      // the total ywd value including tax
    private volatile int profileId;         // profileId of the child this forecast applies to
    private volatile long bookingModified;  // the modified data of the booking

    public BookingForecastBean(int bookingId, int accountId, int ywd, long value, long valueIncTax, int profileId, long bookingModified, String owner) {
        super(bookingId, owner);
        this.accountId = accountId;
        this.ywd = ywd;
        this.value = value;
        this.valueIncTax = valueIncTax;
        this.profileId = profileId;
        this.bookingModified = bookingModified;
    }

    public BookingForecastBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.value = ObjectEnum.INITIALISATION.value();
        this.valueIncTax = ObjectEnum.INITIALISATION.value();
        this.profileId = ObjectEnum.INITIALISATION.value();
        this.bookingModified = ObjectEnum.INITIALISATION.value();
    }

    public int getBookingForecastId() {
        return super.getIdentifier();
    }

    public int getAccountId() {
        return accountId;
    }

    public int getYwd() {
        return ywd;
    }

    public long getValue() {
        return value;
    }

    public long getTax() {
        return valueIncTax - value;
    }

    public long getValueIncTax() {
        return valueIncTax;
    }

    public int getProfileId() {
        return profileId;
    }

    public long getBookingModified() {
        return bookingModified;
    }

    /**
     * Calculated Field: An array of values with consideration to the taxRate.
     * The array is of size 3 and made up of the following:<br>
     * index[0] - value including tax <br>
     * index[1] - value only<br>
     * index[2] - tax only<br>
     *
     * @return an array of calculated values with consideration to the taxRate
     */
    public long[] getValueArray() {
        long[] rtnValues = new long[3];
        rtnValues[VALUE_INC_TAX] = valueIncTax;
        rtnValues[VALUE_ONLY] = value;
        rtnValues[TAX_ONLY] = getTax();
        return rtnValues;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.accountId;
        hash = 89 * hash + this.ywd;
        hash = 89 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 89 * hash + (int) (this.valueIncTax ^ (this.valueIncTax >>> 32));
        hash = 89 * hash + this.profileId;
        hash = 89 * hash + (int) (this.bookingModified ^ (this.bookingModified >>> 32));
        return hash + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final BookingForecastBean other = (BookingForecastBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.value != other.value) {
            return false;
        }
        if(this.valueIncTax != other.valueIncTax) {
            return false;
        }
        if(this.profileId != other.profileId) {
            return false;
        }
        if(this.bookingModified != other.bookingModified) {
            return false;
        }
        return super.equals(obj);
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
        Element bean = new Element("BookingForecastBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("value", Long.toString(value));
        bean.setAttribute("valueIncTax", Long.toString(valueIncTax));
        bean.setAttribute("profileId", Integer.toString(profileId));
        bean.setAttribute("bookingModified", Long.toString(bookingModified));

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return(rtnList);
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
        Element bean = root.getChild("BookingForecastBean");
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        value = Long.parseLong(bean.getAttributeValue("value", "-1"));
        valueIncTax = Long.parseLong(bean.getAttributeValue("valueIncTax", "-1"));
        profileId = Integer.parseInt(bean.getAttributeValue("profileId", "-1"));
        bookingModified = Long.parseLong(bean.getAttributeValue("bookingModified", "-1"));

    }

}
