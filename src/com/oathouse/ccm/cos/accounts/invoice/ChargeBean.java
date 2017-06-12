/*
 * @(#)ChargeBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code ChargeBean} Class is the base class for charge information.
 *
 * @author Darryl Oatridge
 * @version 1.02 23-Feb-2011
 */
public abstract class ChargeBean extends ObjectBean {

    private static final long serialVersionUID = 20110223102L;
    private volatile int ywd;
    private volatile int accountId;
    private volatile int profileId;
    private volatile int invoiceId;
    private volatile long preAdjustmentValue;
    private volatile int taxRate; // the tax rate, to 2dp, expressed as a long: example 17.5% would be 1750
    private volatile int discountRate;
    private volatile String notes;

    public ChargeBean(int chargeId, int ywd, int accountId, int profileId, int invoiceId, long preAdjustmentValue,
            int taxRate, int discountRate, String notes, String owner) {
        super(chargeId, owner);
        this.ywd = ywd;
        this.accountId = accountId;
        this.profileId = profileId;
        this.invoiceId = invoiceId;
        this.preAdjustmentValue = preAdjustmentValue;
        this.taxRate = taxRate;
        this.discountRate = discountRate;
        this.notes = notes != null ? notes : "";;
    }

    public ChargeBean() {
        super();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.profileId = ObjectEnum.INITIALISATION.value();
        this.invoiceId = ObjectEnum.INITIALISATION.value();
        this.preAdjustmentValue = ObjectEnum.INITIALISATION.value();
        this.taxRate = ObjectEnum.INITIALISATION.value();
        this.discountRate = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    public int getChargeId() {
        return super.getIdentifier();
    }

    public int getYwd() {
        return ywd;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public long getPreAdjustmentValue() {
        return preAdjustmentValue;
    }

    public long[] getInvoiceValues() {
        long invoiceValue = getPreAdjustmentValue() - getDiscountValue();
        long[] rtnValues = new long[3];
        rtnValues[0] = TDCalc.getValueIncTax(invoiceValue, getTaxRate());
        rtnValues[1] = invoiceValue;
        rtnValues[2] = TDCalc.getTax(invoiceValue, getTaxRate());
        return rtnValues;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public long getDiscountValue() {
        return TDCalc.getDiscountValue(preAdjustmentValue, getDiscountRate());
    }

    public String getNotes() {
        return notes;
    }

    protected void setInvoiceId(int invoiceId, String owner) {
        this.invoiceId = invoiceId;
        this.setOwner(owner);
    }

    protected void setValue(long value, String owner) {
        this.preAdjustmentValue = value;
        super.setOwner(owner);
    }

    protected void setTaxRate(int taxRate, String owner) {
        this.taxRate = taxRate;
        super.setOwner(owner);
    }

    protected void setDiscountRate(int discountRate, String owner) {
        this.discountRate = discountRate;
        super.setOwner(owner);
    }

    /**
     *Sort the bean in created order.
     */
    @Override
    public int compareTo(ObjectBean other) {
        return (this.getCreated() < other.getCreated() ? -1 : (this.getCreated() == other.getCreated() ? 0 : 1));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ChargeBean other = (ChargeBean) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.profileId != other.profileId) {
            return false;
        }
        if(this.invoiceId != other.invoiceId) {
            return false;
        }
        if(this.preAdjustmentValue != other.preAdjustmentValue) {
            return false;
        }
        if(this.taxRate != other.taxRate) {
            return false;
        }
        if(this.discountRate != other.discountRate) {
            return false;
        }
        if((this.notes == null) ? (other.notes != null) : !this.notes.equals(other.notes)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.ywd;
        hash = 47 * hash + this.accountId;
        hash = 47 * hash + this.profileId;
        hash = 47 * hash + this.invoiceId;
        hash = 47 * hash + (int) (this.preAdjustmentValue ^ (this.preAdjustmentValue >>> 32));
        hash = 47 * hash + this.taxRate;
        hash = 47 * hash + this.discountRate;
        hash = 47 * hash + (this.notes != null ? this.notes.hashCode() : 0);
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
        Element bean = new Element("ChargeBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("profileId", Integer.toString(profileId));
        bean.setAttribute("invoiceId", Integer.toString(invoiceId));
        bean.setAttribute("preAdjustmentValue", Long.toString(preAdjustmentValue));
        bean.setAttribute("taxRate", Integer.toString(taxRate));
        bean.setAttribute("discountRate", Integer.toString(discountRate));
        bean.setAttribute("notes", notes);

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
        Element bean = root.getChild("ChargeBean");
        // set up the data
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        profileId = Integer.parseInt(bean.getAttributeValue("profileId", "-1"));
        invoiceId = Integer.parseInt(bean.getAttributeValue("invoiceId", "-1"));
        preAdjustmentValue = Long.parseLong(bean.getAttributeValue("preAdjustmentValue", "-1"));
        taxRate = Integer.parseInt(bean.getAttributeValue("taxRate", "-1"));
        discountRate = Integer.parseInt(bean.getAttributeValue("discountRate", "-1"));
        notes = bean.getAttributeValue("notes", "");

    }
}
