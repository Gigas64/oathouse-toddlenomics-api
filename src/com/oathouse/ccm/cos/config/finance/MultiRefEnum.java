/*
 * @(#)MultiRefEnum.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.oss.storage.valueholder.MRHolder;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code MultiRefEnum} Enumeration represents the Multi Reference
 * type values used in the finance package each enum has an additional
 * level and type. MultiRefEnum work in conjunction with the MRHolder
 * class to provide an extended identification paradigm that includes
 * a single digit reference type to distinguish potential id clashes
 * when multiple reference sources are used and distinction of the origin
 * of the identifier needs to be held.
 *
 * <p>
 * Currently there are two distinctive 'levels' of MultiRef, PriceGroup and
 * PriceConfig. PriceGroup refers to AgeStart and Room. PriceConfig refers
 * to Standard charge, Special charge and No charge with PriceList and
 * PriceAdjustment combinations.
 * </p>
 *
 * <p>
 * The use MultiRefEnum in conjunction with a MRHolder you use the type() method
 * to retrieve a unique reference type for that enumeration. for example<br>
 * <blockquote>
 * <pre>
 *      int roomId = 2;
 *      int ref = MultiRefEnum.ROOM.type();
 *      int roomMr = MRHolder.getMR(ref, roomId);
 * </pre>
 * </blockquote>
 *
 * @author Darryl Oatridge
 * @version 1.00 09-May-2012
 * @see MRHolder
 */
public enum MultiRefEnum {
    // INTERNAL
    UNDEFINED(-1,-1),
    //not Set
    NO_VALUE(0,-1),

    /** PRICE level indicator. this has -1 value and is for level reference only */
    PRICE(2,-1),
    /** ADJUSTMENT level indicator. this has -1 value and is for level reference only */
    ADJUSTMENT(3,-1),
    /** FIXED level indicator. this has -1 value and is for level reference only */
    FIXED(4,-1),
    /** LOYALTY level indicator. this has -1 value and is for level reference only */
    LOYALTY(5,-1),

    // PriceGroup
    /** AGE enumeration type identifier */
    AGE(1,0),
    /** ROOM enumeration type identifier */
    ROOM(1,1),

    // PriceConfig
    /** List price standard enumeration type identifier */
    PRICE_STANDARD(2,2),
    /** List price special enumeration type identifier */
    PRICE_SPECIAL(2,3),
    /** price adjustment standard enumeration type identifier */
    ADJUSTMENT_STANDARD(3,4),
    /** price adjustment special enumeration type identifier */
    ADJUSTMENT_SPECIAL(3,5),
    /** fixed child adjustment enumeration type identifier */
    FIXED_CHILD(4,6),
    /** fixed account adjustment enumeration type identifier */
    FIXED_ACCOUNT(4,7),
    /** loyalty discount standard enumeration type identifier */
    LOYALTY_STANDARD(5,8),
    /** loyalty discount special enumeration type identifier */
    LOYALTY_SPECIAL(5,9);

    private final int level;
    private final int type;

    /*
     * private constructor to set the enumerations
     */
    private MultiRefEnum(int level, int type) {
        this.level = level;
        this.type = type;
    }

    /**
     * @return the level value of the enumeration
     */
    public int level() {
        return level;
    }

    /**
     * @return the reference number of the enumeration
     */
    public int type() {
        return type;
    }

    /**
     * returns a MultiRefEnum for the integer value. This compares the
     * MultiRefEnum type identifier with the
     *
     * @param type
     * @return
     */
    public static MultiRefEnum getMultiRefEnum(int type) {
        for(MultiRefEnum mr : MultiRefEnum.values()) {
            if(mr.type() == type) {
                return mr;
            }
        }
        return(UNDEFINED);
    }

    /**
     * @return return true if the MultiRefEnum is a price group enum (AGE or ROOM)
     */
    public boolean isPriceGroup() {
        if(this.level == 1) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a price configuration enum
     */
    public boolean isPriceConfig() {
        if(this.level > 1) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a price list enum
     */
    public boolean isPriceList() {
        if(this.level == 2) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a price adjustment enum
     */
    public boolean isAdjustment() {
        if(this.level == 3 || this.level == 4) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a price adjustment booking enum
     */
    public boolean isPriceAdjustment() {
        if(this.level == 3) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a price adjustment addition enum
     */
    public boolean isFixedAdjustment() {
        if(this.level == 4) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the MultiRefEnum is a loyalty discount enum
     */
    public boolean isLoyaltyDiscount() {
        if(this.level == 5) {
            return true;
        }
        return false;
    }

    /**
     * Used to check a MultiRefEnum has a
     *
     * @param name the String name to be checked
     * @return true if the String name is contained in the enumeration name
     */
    public boolean hasString(String name) {
        if(this.toString().toUpperCase().contains(name.toUpperCase())) {
            return(true);
        }
        return(false);
    }

    /**
     * Used to check the MultiRefEnum by reference
     *
     * @param type the type to be checked
     * @return true is the type is equal to the enumeration type
     */
    public boolean isType(int type) {
        if(this.level == level) {
            return(true);
        }
        return(false);
    }

    /**
     * Provides a Set of MultiRefEnum enum objects, that are specifically of a level
     *
     * @param level the level of enumerations to return
     * @return a list of MultiRefEnum objects
     */
    public static List<MultiRefEnum> getAllLevel(int level) {
        final List<MultiRefEnum> rtnList = new LinkedList<>();
        for(MultiRefEnum mr : MultiRefEnum.values()) {
            if(mr.level() == level) {
                rtnList.add(mr);
            }
        }
        return (rtnList);
    }

}
