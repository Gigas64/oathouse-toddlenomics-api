/*
 * @(#)VABoolean.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma;

import java.util.*;
import static java.util.Arrays.asList;
import java.util.concurrent.*;

/**
 * The {@code VABoolean} Enumeration are a list of Variable Arguments representing boolean values.
 * The purpose for this method is to avoid lots of boolean parameters in a method call. The added
 * bonus is not having to include extra parameters that are normally false. The presence of a
 * VABoolean represents true, its absence means false.
 *
 * @author Darryl Oatridge
 * @version 1.00 22-Apr-2012
 */
public enum VABoolean {

    EXCEED_CAPACITY,
    INCLUDE_SESSION,
    INCLUDE_EDUCATION,
    INCLUDE_LOYALTY,
    INCLUDE_ADJUSTMENTS,
    INCLUDE_FIXED,
    INCLUDE_LIVE_BOOKINGS,
    INCLUDE_BOOKING_REQUESTS,
    ADHOC_BOOKING,
    RESET_SPANSD,
    IMMEDIATE_RECALC,
    REPEATED,
    SPECIAL,
    STANDARD;

    /**
     * compares a VABoolean against a list, returning true if the list contains the enumeration.
     *
     * @param array the array or arguments
     * @return true if the list contains the enumeration
     */
    public boolean isIn(VABoolean[] array) {
        return (asList(array).contains(this));
    }

    /**
     * compares a VABoolean against a list, returning true if the list is empty.
     *
     * @param array the array or arguments
     * @return true if the list is empty
     */
    public boolean isEmpty(VABoolean[] array) {
        return (asList(array).isEmpty());
    }

    /**
     * A static helper method to convert a List to a VABoolean array.
     *
     * @param list a list of VABoolean
     * @return the list converted to an array of VABoolean
     */
    public static VABoolean[] toArray(List<VABoolean> list) {
        return list.toArray(new VABoolean[list.size()]);
    }

    /**
     * A static helper to add VABoolean enumeration to an already existing VABoolean array
     *
     * @param array the original array of VABoolean enumeration
     * @param args the VABoolean enumerations to be added
     * @return the new array of VABoolean enumerations
     */
    public static VABoolean[] addToArray(VABoolean[] array, VABoolean... args) {
        ArrayList<VABoolean> arrayList = new ArrayList<>(asList(array));
        arrayList.addAll(asList(args));
        return arrayList.toArray(new VABoolean[arrayList.size()]);
    }

    /**
     * A static helper method that does the equivalent of Arrays.asList() for
     * Set. This was meant to be included with Java 7 but missed the release.
     *
     * @param args a list of Integer arguments
     * @return a new set with the values added
     */
    public static Set<Integer> asSet(Integer... args) {
        return new ConcurrentSkipListSet<>(asList(args));
    }
}
