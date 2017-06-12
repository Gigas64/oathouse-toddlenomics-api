/*
 * @(#)BillingBean.java
 *
 * Copyright:	Copyright 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cos.accounts.*;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.config.finance.*;
import com.oathouse.oss.storage.objectstore.*;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.*;

/**
 * The {@code BillingBean} Class holding information relating to billing
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 22, 2012
 */
public class BillingBean extends ObjectBean {

    private static final long serialVersionUID = 20120212100L;

    public static final int VALUE_INC_TAX = 0;
    public static final int VALUE_ONLY = 1;
    public static final int TAX_ONLY = 2;

    private volatile int accountId; // the account this billing is for
    private volatile int ywd; // the ywd of the booking
    private volatile int billingSd; // the period for which this billing has been applied
    private volatile long value; // the value to be billed to 10th penny (£1.43.2 = 1432L)
    private volatile int taxRate; // the tax rate at the time of creation in percent to 2 dp (17.5% = 1750)
    private volatile int bookingId; // a booking id associated with this billing, or -1
    private volatile int profileId; // the profileId of thechild this billing applies to
    private volatile int btChargeBit; // the charge bit of the bookingType
    private volatile int billingBits; // the billing bits that define this billing
    private volatile int invoiceId; // if applied to an invoice, the invoice id or -1
    private volatile String description; // a description of the bill
    private volatile int discountId; // a reference id to the billing this billing is a discount for, or -1
    private volatile int adjustmentId; // a reference to an adjustment or loyalty that generated this billing, or -1
    private volatile String notes; // notes that can be added to a billing

    /**
     * BillingBean Object Constructor
     *
     * @param billingId the ObjectBean reference id
     * @param accountId the account this billing is part of
     * @param ywd the ywd of the booking, loyalty or fixed item
     * @param billingSd the period for which this billing has been applied(-1 if not used)
     * @param value the value to be billed to 10th penny (£1.43.2 = 1432L)
     * @param taxRate the tax rate at the time of creation in percent to 2 dp (17.5% = 1750)
     * @param bookingId a booking associated with this billing (-1 if not used)
     * @param profileId the profileId of the child associated with this billing (-1 if not used)
     * @param btChargeBit The BTBits charge bit taken from the booking bookingType (-1 if not used)
     * @param billingBits the bit representation of the BillingEnum billing values that define this billing
     * @param invoiceId the invoice associated with this billing (-1 if not yet allocated)
     * @param description a description of the bill
     * @param notes notes that can be added to a billing
     * @param discountId if this billing has a discount then this is the billing id of the discount
     * @param adjustmentId a reference to an adjustment or loyalty that generated this billing, or -1
     * @param owner
     */


    public BillingBean(int billingId, int accountId, int ywd, int billingSd, long value, int taxRate, int bookingId, int profileId, int btChargeBit, int billingBits, int invoiceId, String description, String notes, int discountId, int adjustmentId, String owner) {
        super(billingId, owner);
        this.accountId = accountId;
        this.ywd = ywd;
        this.billingSd = billingSd;
        this.value = Math.abs(value); // has to be a positive
        this.taxRate = taxRate;
        this.bookingId = bookingId;
        this.profileId = profileId;
        this.btChargeBit = btChargeBit;
        this.billingBits = billingBits;
        this.invoiceId = invoiceId;
        this.description = description;
        this.notes = notes;
        this.discountId = discountId;
        this.adjustmentId = adjustmentId;
    }

    public BillingBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.billingSd = ObjectEnum.INITIALISATION.value();
        this.value = ObjectEnum.INITIALISATION.value();
        this.taxRate = ObjectEnum.INITIALISATION.value();
        this.bookingId = ObjectEnum.INITIALISATION.value();
        this.profileId = ObjectEnum.INITIALISATION.value();
        this.btChargeBit = ObjectEnum.INITIALISATION.value();
        this.billingBits = ObjectEnum.INITIALISATION.value();
        this.invoiceId = ObjectEnum.INITIALISATION.value();
        this.description = "";
        this.notes = "";
        this.discountId = ObjectEnum.INITIALISATION.value();
        this.adjustmentId = ObjectEnum.INITIALISATION.value();
    }

    public int getBillingId() {
        return super.getIdentifier();
    }

    public int getAccountId() {
        return accountId;
    }

    public int getYwd() {
        return ywd;
    }

    public int getBillingSd() {
        return billingSd;
    }

    public long getValue() {
        return value;
    }

    public long getSignedValue() {
        int signAdjuster = hasBillingBits(BillingEnum.BILL_CREDIT) ? -1 : 1;
        return value * signAdjuster;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public long getTax() {
        return TDCalc.getTax(value, taxRate);
    }

    public long getValueIncTax() {
        return TDCalc.getValueIncTax(value, taxRate);
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getBtChargeBit() {
        return btChargeBit;
    }

    public int getBillingBits() {
        return billingBits;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public int getDiscountId() {
        return discountId;
    }

    public int getAdjustmentId() {
        return adjustmentId;
    }

    protected void setInvoiceId(int invoiceId, String owner) {
        this.invoiceId = invoiceId;
        super.setOwner(owner);
    }

    protected void setDiscountId(int discountId, String owner) {
        this.discountId = discountId;
        super.setOwner(owner);
    }

    protected void setNotes(String notes,String owner) {
        this.notes = notes;
        super.setOwner(owner);
    }

    /**
     * Helper method to see if the billingBits has ONLY the
     * BillingEnum values in the billingBits
     *
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if only the BillingEnum are in the billingBits
     * @see BillingEnum#hasBillingBit(int,BillingEnum[])
     */
    public boolean hasBillingBits(BillingEnum... billingArgs) {
        return BillingEnum.hasBillingBit(billingBits, billingArgs);
    }

    /**
     * Helper method to see if the billingBits has ANY of the
     * BillingEnum values in the billingBits
     *
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if any of the BillingEnum are in the billingBits
     * @see BillingEnum#hasAnyBillingBit(int, BillingEnum[])
     */
    public boolean hasAnyBillingBit(BillingEnum... billingArgs) {
        return BillingEnum.hasAnyBillingBit(billingBits, billingArgs);
    }

    /**
     * Calculated Field: An array of values with consideration to the taxRate.
     * All values are always positive be they CREDIT or DEBIT.
     * For signed values use getSignedValueArray()
     * The array is of size 3 and made up of the following:<br>
     * index[0] - value including tax <br>
     * index[1] - value only<br>
     * index[2] - tax only<br>
     *
     * @return an array of calculated values with consideration to the taxRate
     * @see #getSignedValueArray()
     */
    public long[] getValueArray() {
        long[] rtnValues = new long[3];
        rtnValues[VALUE_INC_TAX] = getValueIncTax();
        rtnValues[VALUE_ONLY] = value;
        rtnValues[TAX_ONLY] = getTax();
        return rtnValues;
    }

    /**
     * Calculated Field: An array of signed values with consideration to the taxRate.
     * The signed values are negative(-) for CREDIT and positive(+) for CHARGE.
     * For values that are not signed use getValueArray()
     * The array is of size 3 and made up of the following:<br>
     * index[0] - value including tax <br>
     * index[1] - value only<br>
     * index[2] - tax only<br>
     *
     * @return an array of signed calculated values with consideration to the taxRate
     * @see #getValueArray()
     */
    public long[] getSignedValueArray() {
        int signAdjuster = hasBillingBits(BillingEnum.BILL_CREDIT) ? -1 : 1;
        long[] rtnValues = new long[3];
        rtnValues[VALUE_INC_TAX] = signAdjuster * getValueIncTax();
        rtnValues[VALUE_ONLY] = signAdjuster * value;
        rtnValues[TAX_ONLY] = signAdjuster * getTax();
        return rtnValues;
    }

    /**
     * Helper Method: adds an Value array to a base value array
     *
     * @param base
     * @param addition
     * @return the base + addition
     */
    public static long[] addValueArray(long[] base, long[] addition) {
        base[VALUE_INC_TAX] += addition[VALUE_INC_TAX];
        base[VALUE_ONLY] += addition[VALUE_ONLY];
        base[TAX_ONLY] += addition[TAX_ONLY];
        return base;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BillingBean id = ").append(getIdentifier()).append("\n");
        sb.append("\taccountId    = ").append(accountId).append("\n");
        sb.append("\tywd          = ").append(ywd).append("\n");
        sb.append("\tbillingSd    = ").append(billingSd).append("\n");
        sb.append("\tvalue        = ").append(value).append("\n");
        sb.append("\ttaxRate      = ").append(taxRate).append("\n");
        sb.append("\tbookingId    = ").append(bookingId).append("\n");
        sb.append("\tprofileId    = ").append(profileId).append("\n");
        sb.append("\tbtChargeBit  = ").append(BTBits.getAllStrings(btChargeBit)).append("\n");
        sb.append("\tbillingBits  = ").append(BillingEnum.getAllStrings(billingBits)).append("\n");
        sb.append("\tinvoiceId    = ").append(invoiceId).append("\n");
        sb.append("\tdescription  = ").append(description).append("\n");
        sb.append("\tnotes        = ").append(notes).append("\n");
        sb.append("\tdiscountId   = ").append(discountId).append("\n");
        sb.append("\tadjustmentId = ").append(adjustmentId).append("\n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.accountId;
        hash = 17 * hash + this.ywd;
        hash = 17 * hash + this.billingSd;
        hash = 17 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 17 * hash + this.taxRate;
        hash = 17 * hash + this.bookingId;
        hash = 17 * hash + this.profileId;
        hash = 17 * hash + this.btChargeBit;
        hash = 17 * hash + this.billingBits;
        hash = 17 * hash + this.invoiceId;
        hash = 17 * hash + Objects.hashCode(this.description);
        hash = 17 * hash + Objects.hashCode(this.notes);
        hash = 17 * hash + this.discountId;
        hash = 17 * hash + this.adjustmentId;
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
        final BillingBean other = (BillingBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.billingSd != other.billingSd) {
            return false;
        }
        if(this.value != other.value) {
            return false;
        }
        if(this.taxRate != other.taxRate) {
            return false;
        }
        if(this.bookingId != other.bookingId) {
            return false;
        }
        if(this.profileId != other.profileId) {
            return false;
        }
        if(this.btChargeBit != other.btChargeBit) {
            return false;
        }
        if(this.billingBits != other.billingBits) {
            return false;
        }
        if(this.invoiceId != other.invoiceId) {
            return false;
        }
        if(!Objects.equals(this.description, other.description)) {
            return false;
        }
        if(!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if(this.discountId != other.discountId) {
            return false;
        }
        if(this.adjustmentId != other.adjustmentId) {
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
        Element bean = new Element(VT.BILLING.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("billingSd", Integer.toString(billingSd));
        bean.setAttribute("value", Long.toString(value));
        bean.setAttribute("taxRate", Integer.toString(taxRate));
        bean.setAttribute("bookingId", Integer.toString(bookingId));
        bean.setAttribute("profileId", Integer.toString(profileId));
        bean.setAttribute("btChargeBit", Integer.toString(btChargeBit));
        bean.setAttribute("billingBits", Integer.toString(billingBits));
        bean.setAttribute("invoiceId", Integer.toString(invoiceId));
        bean.setAttribute("description", description);
        bean.setAttribute("notes", notes);
        bean.setAttribute("discountId", Integer.toString(discountId));
        bean.setAttribute("adjustmentId", Integer.toString(adjustmentId));

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
        Element bean = root.getChild(VT.BILLING.bean());
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        billingSd = Integer.parseInt(bean.getAttributeValue("billingSd", "-1"));
        value = Long.parseLong(bean.getAttributeValue("value", "-1"));
        taxRate = Integer.parseInt(bean.getAttributeValue("taxRate", "-1"));
        bookingId = Integer.parseInt(bean.getAttributeValue("bookingId", "-1"));
        profileId = Integer.parseInt(bean.getAttributeValue("profileId", "-1"));
        btChargeBit = Integer.parseInt(bean.getAttributeValue("btChargeBit", "-1"));
        billingBits = Integer.parseInt(bean.getAttributeValue("billingBits", "-1"));
        invoiceId = Integer.parseInt(bean.getAttributeValue("invoiceId", "-1"));
        description = bean.getAttributeValue("description", "");
        notes = bean.getAttributeValue("notes", "");
        discountId = Integer.parseInt(bean.getAttributeValue("discountId", "-1"));
        adjustmentId = Integer.parseInt(bean.getAttributeValue("adjustmentId", "-1"));
    }

}
