/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)AdjustmentType.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import java.util.LinkedList;
import java.util.List;

/**
 * The {@code AdjustmentType} Enumeration has a number of different
 * adjustment types that are sub categorised into positive and negative
 * through use of a boolean attribute.
 *
 * @author Darryl Oatridge
 * @version 1.00 24-Feb-2011
 */
public enum AdjustmentType {
    // Internal
    UNDEFINED(false),
    // not Set
    NO_VALUE(false),
    // reduction
    EDUCATION_SESSION(false),
    // addition
    EARLY_DROP(true),
    // addition
    LATE_PICKUP(true);


    private final boolean positive;

    AdjustmentType(boolean positive) {
        this.positive = positive;
    }

    /**
     * returns the attribute value of the AdjustmentType
     */
    public boolean isPositive() {
        if(this.equals(UNDEFINED) || this.equals(NO_VALUE)) {
            throw new UnsupportedOperationException("You are trying to check an unsupported AdjustmentType");
        }
        return (positive);
    }

    private boolean isSupported() {
        if(this.equals(UNDEFINED) || this.equals(NO_VALUE)) {
            return(false);
        }
        return(true);
    }

    /**
     * Provides a Set of AdjustmentType enum objects, that are categorised as positive
     */
    public static List<AdjustmentType> getPositive() {
        final List<AdjustmentType> rtnList = new LinkedList<AdjustmentType>();
        for(AdjustmentType t : AdjustmentType.values()) {
            if(t.isSupported() && t.isPositive()) {
                rtnList.add(t);
            }
        }
        return (rtnList);
    }

    /**
     * Provides a Set of AdjustmentType enum objects, that are categorised as negative
     */
    public static List<AdjustmentType> getNegative() {
        final List<AdjustmentType> rtnList = new LinkedList<AdjustmentType>();
        for(AdjustmentType t : AdjustmentType.values()) {
            if(t.isSupported() && !t.isPositive()) {
                rtnList.add(t);
            }
        }
        return (rtnList);
    }

}
