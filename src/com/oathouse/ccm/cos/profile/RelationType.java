/*
 * @(#)RelationType.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import java.util.LinkedList;
import java.util.List;

/**
 * The {@code RelationType} Enumeration is a list of enumerations
 * for relationships. using the method text() returns a text representation
 * of the enum for display.
 *
 * @author Darryl Oatridge
 * @version 1.01 23-Aug-2010
 */
public enum RelationType {
    // INTERNAL
    UNDEFINED(-1),
    //not Set
    NO_VALUE(0),
    NON_PROFESSIONAL(0),
    // Custodians
    LEGAL_PARENT_GUARDIAN(1),
    MOTHERS_PARTNER(1),
    FATHERS_PARTNER(1),
    GRANDPARENT(1),
    UNCLE(1),
    AUNT(1),
    FAMILY_MEMBER(1),
    FAMILY_EMPLOYEE(1),
    FRIEND(1),
    FOSTER_PARENT(1),
    // Professional
    DOCTOR(2),
    DENTIST(2),
    HEALTH_VISITOR(2),
    SCHOOL(2),
    // Customer
    LIABLE(3),
    INVOICE_FULL(3),
    INVOICE_SUM(3),
    INVOICE_EMAIL(3),
    INVOICE_EMAIL_FULL(3),
    INVOICE_EMAIL_SUM(3),
    LIABLE_INVOICE_FULL(3),
    LIABLE_INVOICE_SUM(3),
    LIABLE_INVOICE_EMAIL(3),
    LIABLE_INVOICE_EMAIL_FULL(3),
    LIABLE_INVOICE_EMAIL_SUM(3);

    private final int level;

    RelationType(int level) {
        this.level = level;
    }

    /**
     * returns true if the enum is type 0 (NON_PROFESSIONAL)
     */
    public boolean isNonProfessional() {
        return (level == 0 ? true : false);
    }

    /**
     * returns true if the enum is type 1 (custodian)
     */
    public boolean isCustodian() {
        return (level == 1 ? true : false);
    }

    /**
     * returns true if the enum is type 2 (professional)
     */
    public boolean isProfessional() {
        return (level == 2 ? true : false);
    }

    /**
     * returns true if the enum is type 3 (accounts)
     */
    public boolean isAccounts() {
        return (level == 3 ? true : false);
    }

    /**
     * Gets the level
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * Used to check if a RelationType is an invoice type
     */
    public boolean isInvoice() {
        if(this.isAccounts() && this.toString().contains("INVOICE")) {
            return (true);
        }
        return (false);
    }

    /**
     * Used to check if a RelationType is an email invoice type
     */
    public boolean isInvoiceEmail() {
        if(this.isAccounts() && this.toString().contains("EMAIL")) {
            return (true);
        }
        return (false);
    }
    /**
     * Used to check if a RelationType is an email invoice type
     */
    public boolean isInvoiceFull() {
        if(this.isAccounts() && this.toString().contains("FULL")) {
            return (true);
        }
        return (false);
    }
    /**
     * Used to check if a RelationType is an email invoice type
     */
    public boolean isInvoiceSummary() {
        if(this.isAccounts() && this.toString().contains("SUM")) {
            return (true);
        }
        return (false);
    }

    /**
     * Used to check if a RelationType is liable type
     */
    public boolean isLiable() {
        if(this.isAccounts() && this.toString().contains("LIABLE")) {
            return (true);
        }
        return (false);
    }

    /**
     * Used to check if a RelationType is of type that contains a name
     */
    public boolean isType(String name) {
        if(this.toString().contains(name)) {
            return(true);
        }
        return(false);
    }

    /**
     * Provides a Set of RelationType enum objects, that are specifically custodian
     *
     * @return all the custodian RelationTypes
     */
    public static List<RelationType> getCustodianTypes() {
        final List<RelationType> rtnList = new LinkedList<>();
        for(RelationType t : RelationType.values()) {
            if(t.isCustodian()) {
                rtnList.add(t);
            }
        }
        return (rtnList);
    }

    /**
     * Provides a Set of RelationType enum objects, that are specifically professional.
     *
     * @return all the professional RelationTypes
     */
    public static List<RelationType> getProfessionalTypes() {
        final List<RelationType> rtnList = new LinkedList<>();
        for(RelationType t : RelationType.values()) {
            if(t.isProfessional()) {
                rtnList.add(t);
            }
        }
        return (rtnList);
    }

    /**
     * Provides a Set of RelationType enum objects, that are specifically Accounts.
     *
     * @return all the professional RelationTypes
     */
    public static List<RelationType> getCustomerTypes() {
        final List<RelationType> rtnList = new LinkedList<>();
        for(RelationType t : RelationType.values()) {
            if(t.isAccounts()) {
                rtnList.add(t);
            }
        }
        return (rtnList);
    }
}
