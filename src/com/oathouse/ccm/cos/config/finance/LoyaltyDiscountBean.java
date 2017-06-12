/*
 * @(#)LoyaltyDiscountBean.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.*;
import org.jdom2.Element;

/**
 * The {@code LoyaltyDiscountBean} Class defines different discount schemes based duration. This is generally used for
 * daily and weekly discount options where a duration threshold is rewarded with a discount.
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 20, 2012
 */
public class LoyaltyDiscountBean extends ObjectBean {

    private static final long serialVersionUID = 20120220100L;
    private volatile String name; // the name of the Bill Item config
    private volatile int billingBits; // Billing bits of the Billing item (see BillingBits)
    private volatile long discount; // the value of the billing item
    private volatile int start; // an optional start value when the loyalty starts from
    private volatile int duration; // the duration in minutes before loyalty applies
    private final boolean[] priorityDays; // the days the loyalty applies too

    /**
     * Constructor for a read only loyalty configuration bean providing the parameters for a loyalty discount. NOTE the
     * billingBits will have a fixed Identifier bit (BillingBits.LOYALTY_DISCOUNT) and Action bit (BillingBits.DISCOUNT) with the
     * calculation bit being the variable.
     *
     * @param loyaltyDiscountId
     * @param name a name to identify the the loyalty configuration
     * @param billingBits the billing bits to this loyalty
     * @param discount the discount percentage to apply
     * @param start a specific start time for the duration or less than 1 if any start
     * @param duration the duration threshold when a discount is applied
     * @param priorityDays the days in a week to include.
     * @param owner
     */
    public LoyaltyDiscountBean(int loyaltyDiscountId, String name, int billingBits, long discount, int start, int duration,
            boolean[] priorityDays, String owner) {
        super(loyaltyDiscountId, owner);
        this.name = name;
        this.billingBits = billingBits;
        this.discount = discount;
        this.start = start;
        this.duration = duration;
        this.priorityDays = new boolean[7];
        int length = priorityDays.length < 7 ? priorityDays.length : 7;
        System.arraycopy(priorityDays, 0, this.priorityDays, 0, length);
    }

    public LoyaltyDiscountBean() {
        super();
        this.name = "";
        this.billingBits = ObjectEnum.INITIALISATION.value();
        this.discount = ObjectEnum.INITIALISATION.value();
        this.start = ObjectEnum.INITIALISATION.value();
        this.duration = ObjectEnum.INITIALISATION.value();
        this.priorityDays = new boolean[7];
    }

    public int getLoyaltyDiscountId() {
        return super.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public int getBillingBits() {
        return billingBits;
    }

    public long getDiscount() {
        return discount;
    }

    public int getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public boolean[] getPriorityDays() {
        return priorityDays;
    }

    public int getDurationOut() {
        return duration + 1;
    }

    /**
     * Tests to see if the billingBits have the requested
     *
     * @param bitEnum a comma separated list of BillingEnum
     * @return true if all the BillingEnum are in the billingBits
     */
    public boolean hasBillingBit(BillingEnum... bitEnum) {
        if(bitEnum.length == 0) {
            return false;
        }
        if(BillingEnum.getAllBillingEnums(billingBits).containsAll(Arrays.asList(bitEnum))) {
            return true;
        }
        return false;
    }

    public static final Comparator<LoyaltyDiscountBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<LoyaltyDiscountBean>() {
        @Override
        public int compare(LoyaltyDiscountBean p1, LoyaltyDiscountBean p2) {
            if (p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (p1 != null && p2 == null) {
                return -1;
            }
            if (p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)p1.getIdentifier()).compareTo((Integer)p2.getIdentifier()));
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
        final LoyaltyDiscountBean other = (LoyaltyDiscountBean) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(this.billingBits != other.billingBits) {
            return false;
        }
        if(this.discount != other.discount) {
            return false;
        }
        if(this.start != other.start) {
            return false;
        }
        if(this.duration != other.duration) {
            return false;
        }
        if(!Arrays.equals(this.priorityDays, other.priorityDays)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + this.billingBits;
        hash = 89 * hash + (int) (this.discount ^ (this.discount >>> 32));
        hash = 89 * hash + this.start;
        hash = 89 * hash + this.duration;
        hash = 89 * hash + Arrays.hashCode(this.priorityDays);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     *
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<Element>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element(VT.LOYALTY_DISCOUNT.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("billingBits", Integer.toString(billingBits));
        bean.setAttribute("discount", Long.toString(discount));
        bean.setAttribute("start", Integer.toString(start));
        bean.setAttribute("duration", Integer.toString(duration));
        Element allElements = new Element("priorityDays_array");
        bean.addContent(allElements);
        for(int i = 0; i < priorityDays.length; i++) {
            Element eachElement = new Element("priorityDays");
            eachElement.setAttribute("index", Integer.toString(i));
            eachElement.setAttribute("value", Boolean.toString(priorityDays[i]));
            allElements.addContent(eachElement);
        }

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to put default values in getAttribute() and check the
     * content of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild(VT.LOYALTY_DISCOUNT.bean());
        // set up the data
        name = bean.getAttributeValue("name", "");
        billingBits = Integer.parseInt(bean.getAttributeValue("billingBits", "-1"));
        discount = Long.parseLong(bean.getAttributeValue("discount", "-1"));
        start = Integer.parseInt(bean.getAttributeValue("start", "-1"));
        duration = Integer.parseInt(bean.getAttributeValue("duration", "-1"));
        for(Object o : bean.getChild("priorityDays_array").getChildren("priorityDays")) {
            Element eachDay = (Element) o;
            int dow = Integer.valueOf(eachDay.getAttributeValue("index", "0"));
            if(dow < priorityDays.length) {
                priorityDays[dow] = Boolean.valueOf(eachDay.getAttributeValue("value", "false"));
            }
        }
    }
}
