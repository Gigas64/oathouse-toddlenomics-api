/*
 * @(#)LineItemDTO.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cma.accounts.*;
import com.oathouse.ccm.cos.accounts.*;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.config.finance.*;
import java.util.*;

/**
 * The {@code LineItemDTO} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Jun-2012
 */
public class LineItemDTO {
    private String description;
    private int billingSd; // the period for which this billing has been applied
    private long value; // the value to be billed to 10th penny (£1.43.2 = 1432L)
    private int taxRate; // the tax rate at the time of creation in percent to 2 dp (17.5% = 1750)
    private int billingBits; // the billing bits that define this billing
    private int invoiceId; // if applied to an invoice, the invoice id or -1
    private int discountId; // the discountId associated with this invoiceline, -1 if not applicable
    private String notes; // notes that can be added to a billing

    public LineItemDTO(String description, int billingSd, long value, int taxRate, int billingBits, int invoiceId, int discountId, String notes) {
        this.description = description;
        this.billingSd = billingSd;
        this.value = value;
        this.taxRate = taxRate;
        this.billingBits = billingBits;
        this.invoiceId = invoiceId;
        this.discountId = discountId;
        this.notes = notes;
    }

    public LineItemDTO() {
    }

    public String getDescription() {
        return description;
    }

    public int getBillingBits() {
        return billingBits;
    }

    public int getBillingSd() {
        return billingSd;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public String getNotes() {
        return notes;
    }

    public long getValue() {
        return value;
    }

    public int getDiscountId() {
        return discountId;
    }

    /**
     * Helper method to return a signed value
     * CHARGE is positive, CREDIT is negative
     * @return a +/- value
     */
    public long getSignedValue() {
        int signAdjuster = hasOnlyBillingBits(BillingEnum.BILL_CREDIT) ? -1 : 1;
        return signAdjuster * value;
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
    /**
     * Determines whether this item is a credit
     * @return true if a credit
     */
    public boolean isCredit() {
        return BillingEnum.hasBillingBit(getBillingBits(), BillingEnum.BILL_CREDIT);
    }

    /**
     * Determines whether this item is a debit
     * @return true if a debit
     */
    public boolean isDebit() {
        return BillingEnum.hasBillingBit(getBillingBits(), BillingEnum.BILL_CHARGE);
    }

    /**
     * Determines whether this item is discountable
     * @return true is is discountable
     */
    public boolean isDiscountable() {
        return BillingEnum.hasBillingBit(getBillingBits(), BillingEnum.APPLY_DISCOUNT);
    }

    /**
     * Helper method to see if the billingBits has ONLY the
     * BillingEnum values in the billingBits
     *
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if only the BillingEnum are in the billingBits
     */
    public boolean hasOnlyBillingBits(BillingEnum... billingArgs) {
        return BillingEnum.hasBillingBit(billingBits, billingArgs);
    }

    /**
     * Helper method to see if the billingBits has ANY of the
     * BillingEnum values in the billingBits
     *
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if any of the BillingEnum are in the billingBits
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
        rtnValues[BillingBean.VALUE_INC_TAX] = getValueIncTax();
        rtnValues[BillingBean.VALUE_ONLY] = value;
        rtnValues[BillingBean.TAX_ONLY] = getTax();
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
        int signAdjuster = hasOnlyBillingBits(BillingEnum.BILL_CREDIT) ? -1 : 1;
        long[] rtnValues = new long[3];
        rtnValues[BillingBean.VALUE_INC_TAX] = signAdjuster * getValueIncTax();
        rtnValues[BillingBean.VALUE_ONLY] = signAdjuster * value;
        rtnValues[BillingBean.TAX_ONLY] = signAdjuster * getTax();
        return rtnValues;
    }

    public static final Comparator<LineItemDTO> BILLING_SD_ORDER = new Comparator<LineItemDTO>() {
        @Override
        public int compare(LineItemDTO b1, LineItemDTO b2) {
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
            int result = ((Integer)b1.getBillingSd()).compareTo((Integer)b2.getBillingSd());
            if(result != 0) {
                return result;
            }
            // booking start not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)b1.hashCode()).compareTo((Integer)b2.hashCode()));
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
        final LineItemDTO other = (LineItemDTO) obj;
        if(!Objects.equals(this.description, other.description)) {
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
        if(this.billingBits != other.billingBits) {
            return false;
        }
        if(this.invoiceId != other.invoiceId) {
            return false;
        }
        if(!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + this.billingSd;
        hash = 53 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 53 * hash + this.taxRate;
        hash = 53 * hash + this.billingBits;
        hash = 53 * hash + this.invoiceId;
        hash = 53 * hash + Objects.hashCode(this.notes);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(" LineItemDTO:\n");
        sb.append("   description = ").append(description).append("\n");
        sb.append("   billingSd   = ").append(billingSd).append("\n");
        sb.append("   value       = ").append(value).append("\n");
        sb.append("   taxRate     = ").append(taxRate).append("\n");
        sb.append("   billingBits = ").append(BillingEnum.getAllStrings(billingBits)).append("\n");
        sb.append("   invoiceId   = ").append(invoiceId).append("\n");
        sb.append("   notes       = ").append(notes).append("\n");

        return sb.toString();
    }

}
