package com.oathouse.ccm.cma;

/**
 * Static helper class to transform output from financial services into objects used by
 * the front end
 *
 * <p>&copy;     2012 oathouse.com ltd</p>
 * <p>author:   Darryl Oatridge</p>
 * <p>since:    15 July 2012</p>
 */
public class ApplicationConstants {

    @Deprecated
    public static final String SESSION_CHILD_DISCOUNT = "line.item.session.child.discount";
    @Deprecated
    public static final String ADJUSTMENT_CHILD_DISCOUNT = "line.item.adjustment.child.discount";
    @Deprecated
    public static final String EDUCATION_PRICE_REDUCTION_DISCOUNT = "line.item.education.price.reduction.discount";

    public static final String BOOKING = "invoice.line.booking";
    public static final String SESSION_ESTIMATE = "line.item.session.estimate";
    public static final String SESSION_RECONCILED = "line.item.session.reconciled";
    public static final String EDUCATION_REDUCTION_ESTIMATE = "line.item.education.reduction.estimate";
    public static final String EDUCATION_REDUCTION_RECONCILED = "line.item.education.reduction.reconciled";
    public static final String BOOKING_CHILD_DISCOUNT = "line.item.booking.child.discount";
    public static final String LOYALTY_CHILD_DISCOUNT = "line.item.loyalty.child.discount";
    public static final String FIXED_CHILD_DISCOUNT = "line.item.fixed.child.discount";
    public static final String FIXED_ACCOUNT_DISCOUNT = "line.item.fixed.account.discount";
    public static final String RECEIPT_CARD_FEE = "line.item.receipt.card.fee";
    public static final String ADMIN_VOID_CARD_FEE = "line.item.admin.void.card.fee";
}
