/*
 * @(#)PriceAdjustmentBean.java
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
 * The {@code PriceAdjustmentBean} Class is a configuration class to define price adjustments when billing.
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 24, 2012
 */
public class PriceAdjustmentBean extends ObjectBean {

    private static final long serialVersionUID = 20120224100L;
    private volatile String name; // the name of the Bill Item config
    private volatile int billingBits; // Billing bits of the adjustment item (see BillingBits)
    private volatile long value; // the percentage to 2 pd (3% = 300L) : value to 10th penny (£1.43.2 = 1432L)
    private volatile int precision; // percentage rounding, e.g. 1 - nearest penny, 3 - nearest pound)
    private volatile int rangeSd; // a periodSd, if within, this item gets billed
    private volatile boolean repeated; // if the adjustment should be repeated (repeatSd must be > 0)
    private volatile int repeatDuration; // if repeat, duration to repeat until the end of the chargeSd

    /**
     * Constructor
     *
     * @param priceAdjustmentId
     * @param name the name of the Bill Item config
     * @param billingBits Billing bits of the adjustment item (see BillingEnum)
     * @param value the value of the billing item
     * @param precision how any percentage calculations should be rounded
     * @param rangeSd a periodSd, if within, this item gets billed
     * @param repeated if the adjustment should be repeated (repeatSd must be greater than 0)
     * @param repeatDuration if repeat, duration to repeat until the end of the chargeSd
     * @param owner
     */
    public PriceAdjustmentBean(int priceAdjustmentId, String name, int billingBits, long value,
            int precision, int rangeSd, boolean repeated, int repeatDuration, String owner) {
        super(priceAdjustmentId, owner);
        this.name = name;
        this.billingBits = billingBits;
        this.value = value;
        this.precision = precision;
        this.rangeSd = rangeSd;
        this.repeated = repeated;
        this.repeatDuration = repeatDuration;
    }

    public PriceAdjustmentBean() {
        super();
        this.name = "";
        this.billingBits = ObjectEnum.INITIALISATION.value();
        this.value = ObjectEnum.INITIALISATION.value();
        this.precision = ObjectEnum.INITIALISATION.value();
        this.rangeSd = ObjectEnum.INITIALISATION.value();
        this.repeated = false;
        this.repeatDuration = ObjectEnum.INITIALISATION.value();
    }

    public int getPriceAdjustmentId() {
        return super.getIdentifier();
    }

    public int getBillingBits() {
        return billingBits;
    }

    public int getRepeatDuration() {
        return repeatDuration;
    }

    public String getName() {
        return name;
    }

    public int getPrecision() {
        return precision;
    }

    public int getRangeSd() {
        return rangeSd;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public long getValue() {
        return value;
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

    /**
     * Returns the part of the billingBits represented by the level
     *
     * @param level a BillingEnum of the level to be returned
     * @return BillingEnum
     */
    public BillingEnum getBillingLevel(BillingEnum level) {
        for(BillingEnum billing : BillingEnum.getAllBillingEnums(billingBits)) {
            if(billing.getLevel() == level.getLevel()) {
                return billing;
            }
        }
        return BillingEnum.UNDEFINED;
    }

    /**
     * Returns the part of the billingBits represented by the level
     *
     * @param level an integer of the level to be returned
     * @return billingBits id
     */
    public int getBillingLevelId(BillingEnum level) {
        return BillingEnum.getBillingBits(getBillingLevel(level));
    }

    public static final Comparator<PriceAdjustmentBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<PriceAdjustmentBean>() {
        @Override
        public int compare(PriceAdjustmentBean p1, PriceAdjustmentBean p2) {
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
        final PriceAdjustmentBean other = (PriceAdjustmentBean) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(this.billingBits != other.billingBits) {
            return false;
        }
        if(this.value != other.value) {
            return false;
        }
        if(this.precision != other.precision) {
            return false;
        }
        if(this.rangeSd != other.rangeSd) {
            return false;
        }
        if(this.repeated != other.repeated) {
            return false;
        }
        if(this.repeatDuration != other.repeatDuration) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + this.billingBits;
        hash = 37 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 37 * hash + this.precision;
        hash = 37 * hash + this.rangeSd;
        hash = 37 * hash + (this.repeated ? 1 : 0);
        hash = 37 * hash + this.repeatDuration;
        return hash + super.hashCode();
    }

     /**
     * crates all the elements that represent this bean at this level.
     *
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element(VT.PRICE_ADJUSTMENT.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("billingBits", Integer.toString(billingBits));
        bean.setAttribute("value", Long.toString(value));
        bean.setAttribute("precision", Integer.toString(precision));
        bean.setAttribute("rangeSd", Integer.toString(rangeSd));
        bean.setAttribute("repeated", Boolean.toString(repeated));
        bean.setAttribute("repeatDuration", Integer.toString(repeatDuration));

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
        Element bean = root.getChild(VT.PRICE_ADJUSTMENT.bean());
        // set up the data
        name = bean.getAttributeValue("name", "");
        billingBits = Integer.parseInt(bean.getAttributeValue("billingBits", "-1"));
        value = Long.parseLong(bean.getAttributeValue("value", "-1"));
        precision = Integer.parseInt(bean.getAttributeValue("precision", "-1"));
        rangeSd = Integer.parseInt(bean.getAttributeValue("rangeSd", "-1"));
        repeated = Boolean.parseBoolean(bean.getAttributeValue("repeated", "false"));
        repeatDuration = Integer.parseInt(bean.getAttributeValue("repeatDuration", "-1"));
    }
}
