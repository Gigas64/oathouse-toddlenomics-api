/*
 * @(#)BillingEnum.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.oss.storage.valueholder.AbstractBits;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code BillingEnum} Enumeration representing Price Adjustment
 * information for processing Billing.
 *
 * @author Darryl Oatridge
 * @version 1.00 04-May-2012
 */
public enum BillingEnum {
    // internal
    UNDEFINED(-1, 0),
    // not Set
    NO_VALUE(0, 0),
    // levels
    TYPE(1, 0),
    BILL(2, 0),
    CALC(3, 0),
    APPLY(4, 0),
    RANGE(5, 0),
    GROUP(6, 0),

    /* types of PriceAdjustments that can be charged against a booking */
    /** Identifies that this billing relates to an adjustment on an booking which contains a bookingType which in turn contains BTBits.ATTENDING_BIT */
    TYPE_ADJUSTMENT_ON_ATTENDING(1, (int) Math.pow(0x1, 0x02)),
    /** Identifies that this billing relates to an adjustment on any bookingType booking at all (DEFAULT if booking)*/
    TYPE_ADJUSTMENT_ON_ALL(1, (int) Math.pow(0x2, 0x02)),
    /** Identifies the billing relates to late pickup */
    TYPE_LATE_PICKUP(1, (int) Math.pow(0x2, 0x03)),
    /** Identifies the billing relates to early drop off */
    TYPE_EARLY_DROPOFF(1, (int) Math.pow(0x2, 0x04)),

    /* Internally used booking types
    /** Identifies the billing relates to child discount on a booking */
    TYPE_BOOKING_CHILD_DISCOUNT(1, (int) Math.pow(0x2, 0x05)),
    /** Identifies the billing relates to Sessions */
    TYPE_SESSION(1, (int) Math.pow(0x2, 0x06)),
    /** Identifies the billing relates to funded sessions */
    TYPE_FUNDED(1, (int) Math.pow(0x2, 0x07)),
    /** Identifies the billing relates to child discount on a booking*/
    @Deprecated
    TYPE_ADJUSTMENT_CHILD_DISCOUNT(1, (int) Math.pow(0x2, 0x08)),

    /** Identifies a billing that is fixed item and outside of a booking billing (DEFAULT if not booking)  */
    TYPE_FIXED_ITEM(1, (int) Math.pow(0x2, 0x09)),
    /** Identifies a billing that is a fixed item discount for a child */
    TYPE_FIXED_CHILD_DISCOUNT(1, (int) Math.pow(0x2, 0x0a)),
    /** Identifies a billing that is fixed item discount for an account */
    TYPE_FIXED_ACCOUNT_DISCOUNT(1, (int) Math.pow(0x2, 0x0b)),

    /** Identifies the billing relates to a Loyalty */
    TYPE_LOYALTY(1, (int) Math.pow(0x2, 0x0c)),
    /** Identifies the billing relates to a Loyalty Child Discount */
    TYPE_LOYALTY_CHILD_DISCOUNT(1, (int) Math.pow(0x2, 0x0d)),

    /** Type indicates the billing is a charge (DEFAULT)*/
    BILL_CHARGE(2, (int) Math.pow(0x2, 0x0f)),
    /** Type indicates the billing is a credit */
    BILL_CREDIT(2, (int) Math.pow(0x2, 0x10)),

    /** Calculation should take the value as the billing value (DEFAULT)*/
    CALC_AS_VALUE(3, (int) Math.pow(0x2, 0x11)),
    /** Calculation should take the value as a percentage of a given billing value */
    CALC_AS_PERCENT(3, (int) Math.pow(0x2, 0x12)),

    /** Ensures billing is included after default calculation events  (DEFAULT) */
    APPLY_DISCOUNT(4, (int) Math.pow(0x2, 0x13)),
    /** Ensures billing is included before default calculation events  */
    APPLY_NO_DISCOUNT(4, (int) Math.pow(0x2, 0x14)),

    /** If chargeSd falls within some part of the rangeSd (DEFAULT) */
    RANGE_SOME_PART(5, (int) Math.pow(0x2, 0x15)),
    /** If the chargeSd or chargeSd duration is less than or equal to rangeSd or duration */
    RANGE_AT_MOST(5, (int) Math.pow(0x2, 0x16)),
    /** If the chargeSd or chargeSd duration is equal to or greater than the rangeSd or Loyalty duration */
    RANGE_AT_LEAST(5, (int) Math.pow(0x2, 0x17)),
    /** If the chargeSd or chargeSd duration is exactly equal to the rangeSd or Loyalty duration */
    RANGE_EQUAL(5, (int) Math.pow(0x2, 0x18)),
    /** the sum of of chargeSd or chargeSd duration over a number of days is equal to or greater than the rangeSd or duration */
    RANGE_SUM_TOTAL(5, (int) Math.pow(0x2, 0x19)),
    /** the rangeSd is ignored or is not relevant */
    RANGE_IGNORED(5, (int) Math.pow(0x2, 0x1a)),

    /** Belongs to a Booking group */
    GROUP_BOOKING(6, (int) Math.pow(0x2, 0x1b)),
    /** Belongs to a loyalty group */
    GROUP_LOYALTY(6, (int) Math.pow(0x2, 0x1c)),
    /** the rangeSd is ignored or is not relevant */
    GROUP_FIXED_ITEM(6, (int) Math.pow(0x2, 0x1d));

    private final int level;
    private final int bitValue;

    private BillingEnum(int level, int bitValue) {
        this.level = level;
        this.bitValue = bitValue;
    }

    public int getBitValue() {
        return bitValue;
    }

    protected int getLevel() {
        return level;
    }

    /**
     * @return the index value of the enumeration. WARNING non level types will return a negative number
     */
    public int getIndex() {
        return level - 1;
    }

    /**
     * Helper method to see if the billingBits contains ANY of the BillingEnum
     * arguments passed. The method will return true if one or more of the
     * enumeration arguments passed is contained within the billingBits argument value.
     *
     * @param billingBits the billing bits to check
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if any of the BillingEnum are in the billingBits
     */
    public static boolean hasAnyBillingBit(int billingBits, BillingEnum... billingArgs) {
        for(BillingEnum billingEnum : billingArgs) {
            if(BillingEnum.hasBillingBit(billingBits, billingEnum)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to see if the billingBits contains ONLY the BillingEnum
     * arguments passed. Each of the enumeration arguments passed MUST exist in the
     * billingBits argument. For the comparison to work there must be either zero
     * or one BillingEnum for each of the different levels that make up a billingBit.
     *
     * @param billingBits the billing bits to check
     * @param billingArgs a comma separated list of BillingEnum
     * @return true if all the BillingEnum are in the billingBits
     */
    public static boolean hasBillingBit(int billingBits, BillingEnum... billingArgs) {
        if(billingArgs.length == 0) {
            return false;
        }
        if(BillingEnum.getAllBillingEnums(billingBits).containsAll(Arrays.asList(billingArgs))) {
            return true;
        }
        return false;
    }

    /**
     * Helper method to validate a bit value. The bit value should have 5 value bits
     * relating to an enumeration at each of the 5 levels.
     *
     * @param bitValue the bit value to validate
     * @return true if the bit value is well formed
     */
    public static boolean isValid(int bitValue) {
        List<BillingEnum> chkList = getAllBillingEnums(bitValue);
        if(chkList.size() != 6) {
            return false;
        }
        BillingEnum[] chkArray = chkList.toArray(new BillingEnum[chkList.size()]);
        for(int index = 0; index < chkArray.length; index++) {
            if(chkArray[index].level != index + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to change or add BillingEnum to an existing bit value. Note
     * that if two enumerations are given of the same level only the last will be added.
     * NOTE: if the billing bit is not well formed it will be returned without change
     *
     * The billing bit will NOT be well formed unless all 5 levels have been added to it,
     * so calling this method after getBillingBits() will NOT work
     *
     * @param bitValue the original bit value
     * @param billingArgs a comma separated list of the Enumerations to add
     * @return the altered bit value
     */
    public static int resetBillingBits(int bitValue, BillingEnum... billingArgs) {
        if(!isValid(bitValue)) {
            return bitValue;
        }
        BillingEnum[] rtnBilling = toArray(bitValue);
        for(BillingEnum billingEnum : billingArgs) {
            // ignore any non legitimate values
            if(billingEnum.getBitValue() <= 0) {
                continue;
            }
            rtnBilling[billingEnum.getIndex()] = billingEnum;
        }
        return getBillingBits(rtnBilling);
    }

    /**
     * Helper method to retrieve the BillingEnum from a bitValue for a particular level.
     * If the level is not found then UNDEFINED is returned.
     *
     * @param bitValue
     * @param bitLevel
     * @return
     */
    public static BillingEnum getBillingEnumForLevel(int bitValue, BillingEnum bitLevel) {
        for(BillingEnum billingEnum : getAllBillingEnums(bitValue)) {
            if(billingEnum.getLevel() == bitLevel.getLevel()) {
                return(billingEnum);
            }
        }
        return(UNDEFINED);
    }

    /**
     * returns a List of BillingEnum found within the bitValue passes.
     *
     * @param bitValue the bit value
     * @return List of BillingEnum
     */
    public static List<BillingEnum> getAllBillingEnums(int bitValue){
        LinkedList<BillingEnum> rtnList = new LinkedList<>();
        for(BillingEnum billing : values()) {
            if(AbstractBits.contain(bitValue, billing.getBitValue())) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    /**
     * returns an array of BillingEnum found within the bitValue passes.
     *
     * @param bitValue the bit value
     * @return array of BillingEnum
     */
    public static BillingEnum[] toArray(int bitValue) {
        List<BillingEnum> billingList = getAllBillingEnums(bitValue);
        return billingList.toArray(new BillingEnum[billingList.size()]);
    }

    /**
     * Helper method that converts BillingEnum bits to a string list. Each
     * bit that is set will add a string representation to the list.
     *
     * @param bits the billing bits to be converted
     * @return a list of strings
     */
    public static List<String> getAllStrings(int bits) {
        LinkedList<String> rtnList = new LinkedList<>();
        for(BillingEnum billing : values()) {
            if(AbstractBits.contain(bits, billing.getBitValue())) {
                rtnList.add(billing.toString());
            }
        }
        return rtnList;
    }

    /**
     * Returns a billingBits number representing the passed BillingEnum vararg
     * Note that the billing bits will not necessarily be well formed (ie containing every level)
     *
     * NB the vararg is an array, and can be constructed from a List.toArray(new BillingEnum[0])
     *
     * @param values
     * @return
     */
    public static int getBillingBits(BillingEnum... values) {
        int rtnBits = 0;
        for(BillingEnum billing : values) {
            rtnBits += billing.getBitValue();
        }
        return rtnBits;
    }

    /**
     * Returns a billingBits number representing the passed BillingEnum list
     * Note that the billing bits will not necessarily be well formed (ie containing every level)
     *
     * @param values
     * @return
     */
    public static int getBillingBits(List<BillingEnum> values) {
        return getBillingBits(values.toArray(new BillingEnum[0]));
    }
}
