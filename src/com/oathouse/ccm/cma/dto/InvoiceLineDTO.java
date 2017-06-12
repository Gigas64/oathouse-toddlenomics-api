/*
 * @(#)InvoiceLineDTO.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.finance.*;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.profile.*;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code InvoiceLineDTO} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 24-May-2012
 */
public class InvoiceLineDTO {
    /* Static constants to identify order of InvoiceLineDTO and help identify groups */
    protected static final int BOOKING_ADJUSTMENT = 1;
    protected static final int LOYALTY_DISCOUNT = 2;
    protected static final int FIXED_ITEM_CHILD = 3;
    protected static final int FIXED_ITEM_ACCOUNT = 4;

    private int invoiceId; // the current invoiceId - used internally
    private int ywd; // the ywd of the booking
    private int group; // the group this adjustment belongs too used for ordering
    private int orderStart; // the start time for ordering groups
    private String description; //

    private AccountBean account;
    private ChildBean child;
    private BookingBean booking;
    private BookingTypeBean bookingType;
    private List<LineItemDTO> lineItemList;

    protected InvoiceLineDTO(int invoiceId, int ywd, int group, int orderStart, String description,
                            AccountBean account, ChildBean child, BookingBean booking,
                            BookingTypeBean bookingType, List<LineItemDTO> lineItemList) {
        this.invoiceId = invoiceId;
        this.ywd = ywd;
        this.group = group;
        this.orderStart = orderStart;
        this.description = description;
        this.account = account;
        this.child = child;
        this.booking = booking;
        this.bookingType = bookingType;
        this.lineItemList = new LinkedList<>();
        if(lineItemList != null) {
            this.lineItemList.addAll(lineItemList);
        }
    }

    public int getYwd() {
        return ywd;
    }

    public String getDescription() {
        return description;
    }

    protected int getGroup() {
        return group;
    }

    protected int getOrderStart() {
        return orderStart;
    }

    public BookingBean getBooking() {
        return booking;
    }

    public AccountBean getAccount() {
        return account;
    }

    public ChildBean getChild() {
        return child;
    }

    public BookingTypeBean getBookingType() {
        return bookingType;
    }

    public List<LineItemDTO> getLineItems() {
        return new LinkedList<>(lineItemList);
    }

    public long getLineItemListValue() {
        return getAllLineItemsValue(lineItemList);
    }

    /**
     * Determines whether this line is for a booking adjustment
     * @return
     */
    public boolean isBooking() {
        return group == BOOKING_ADJUSTMENT ? true : false;
    }

    /**
     * Determines whether this line is for a loyalty discount
     * @return
     */
    public boolean isLoyalty() {
        return group == LOYALTY_DISCOUNT ? true : false;
    }

    /**
     * Determines whether this line is for a child fixed item
     * @return
     */
    public boolean isChildFixedItem() {
        return group == FIXED_ITEM_CHILD ? true : false;
    }

    /**
     * Determines whether this line is for an account fixed item
     * @return
     */
    public boolean isAccountFixedItem() {
        return group == FIXED_ITEM_ACCOUNT ? true : false;
    }

    /**
     * Determines whether this line is for any fixed item
     * @return
     */
    public boolean isFixedItem() {
        return group >= FIXED_ITEM_CHILD ? true: false;
    }

    /**
     * Helper Method: returns an ordered set of all the invoiceIds in the
     * lineItems
     *
     * @param excludeIds optional invoiceId's to exclude from the returned selection
     * @return all the previous invoiceIds as an array
     */
    public Integer[] getLineItemInvoiceIds(Integer... excludeIds) {
        Set<Integer> rtnSet = getFilteredInvoiceIds(excludeIds);
        // make sure we don't return null
        return rtnSet.isEmpty() ? new Integer[0] : rtnSet.toArray(new Integer[rtnSet.size()]);
    }

    /**
     * Returns an ordered list of previous invoiceIds, highest first
     * @return
     */
    public List<Integer> getPreviousInvoiceIds() {
        List<Integer> previousIds = new LinkedList<>(getFilteredInvoiceIds(invoiceId));
        Collections.reverse(previousIds);
        return previousIds;
    }

    /**
     * Finds the billingSd used for the session charge from the latest invoice;
     * also works if there is no invoice yet
     * @return
     */
    public int getCurrentBillingSd() {
        if(isBooking()) {
            for(LineItemDTO lineItem : lineItemList) {
                if(lineItem.hasOnlyBillingBits(BillingEnum.TYPE_SESSION)
                        && lineItem.getInvoiceId() == invoiceId) {
                    return lineItem.getBillingSd();
                }
            }
        } else if(isLoyalty()) {
            for(LineItemDTO lineItem : lineItemList) {
                if(lineItem.hasOnlyBillingBits(BillingEnum.TYPE_LOYALTY)
                        && lineItem.getInvoiceId() == invoiceId) {
                    return lineItem.getBillingSd();
                }
            }
        }
        // not found so return -1
        return -1;
    }

    /**
     * If this line is for a fixed item, returns the single line item dto for the charge
     * @return
     */
    public LineItemDTO getFixedItemLineItemDTO() {
        if(isFixedItem()) {
            return lineItemList.get(0);
        }
        throw new IllegalArgumentException("This DTO is not a Fixed Item");
    }

    /**
     * Returns all the items that make up the "list price" for this line - sessions, fixed items,
     * adjustments that are not penalties (eg snack etc) full price before discount
     * @param invoiceIds
     * @return
     */
    public List<LineItemDTO> getListPriceItems(Integer... invoiceIds) {
        BillingEnum[] billingArgs = {TYPE_SESSION, TYPE_FIXED_ITEM, TYPE_ADJUSTMENT_ON_ALL, TYPE_ADJUSTMENT_ON_ATTENDING};
        return intersect(this.getLineItems(invoiceIds), this.getLineItems(billingArgs));
    }

    /**
     * Sum Calculation: Calculates the "list price" value of this line
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceIds
     * @return
     */
    public long getListPriceValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getListPriceItems(invoiceIds));
    }

    /**
     * LineItemDTO Filter: If the InvoiceLine instance is from a booking then only the FUNDED lineItems
     * are returned.
     * Non Booking InvoiceLine instances will return an empty list
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the List.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public List<LineItemDTO> getEducationReductionItems(Integer... invoiceIds) {
        return intersect(this.getLineItems(invoiceIds), this.getLineItems(TYPE_FUNDED));
    }

    /**
     * Sum Calculation: If the InvoiceLine instance is from a booking then only the FUNDED lineItems are
     * included in the calculation.
     * Non Booking InvoiceLine instances will return a zero value
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public long getEducationReductionValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getEducationReductionItems(invoiceIds));
    }

    /**
     * LineItemDTO Filter: If the InvoiceLine instance is from a booking then TYPE_EARLY_DROPOFF and TYPE_LATE_PICKUP lineItems are returned.
     * Non Booking InvoiceLine instances will return an empty list
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the List.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public List<LineItemDTO> getPenaltyItems(Integer... invoiceIds) {
        BillingEnum[] billingArgs = {TYPE_EARLY_DROPOFF, TYPE_LATE_PICKUP};
        return intersect(this.getLineItems(invoiceIds), this.getLineItems(billingArgs));
    }

    /**
     * Sum Calculation: If the InvoiceLine instance is from a booking then both the TYPE_EARLY_DROPOFF and TYPE_LATE_PICKUP lineItems are included in the
     * calculation.
     * Non Booking InvoiceLine instances will return a zero value
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public long getPenaltyValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getPenaltyItems(invoiceIds));
    }

    /**
     * LineItemDTO Filter: Education Reductions and Penalties combined
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the List.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public List<LineItemDTO> getAdjustmentItems(Integer... invoiceIds) {
        BillingEnum[] billingArgs = {TYPE_FUNDED, TYPE_EARLY_DROPOFF, TYPE_LATE_PICKUP};
        return intersect(this.getLineItems(invoiceIds), this.getLineItems(billingArgs));
    }

    /**
     * Sum Calculation: Education Reductions and Penalties combined
     * in the calculation.
     * Non Booking InvoiceLine instances will return a zero value
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public long getAdjustmentValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getAdjustmentItems(invoiceIds));
    }

    /**
     * LineItemDTO Filter: Items of types TYPE_FIXED_ACCOUNT_DISCOUNT, TYPE_FIXED_CHILD_DISCOUNT, TYPE_LOYALTY, TYPE_LOYALTY, TYPE_LOYALTY
     * are returned
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the List.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public List<LineItemDTO> getDiscountItems(Integer... invoiceIds) {
        BillingEnum[] billingArgs = {TYPE_BOOKING_CHILD_DISCOUNT,
                                     TYPE_ADJUSTMENT_CHILD_DISCOUNT,
                                     TYPE_FIXED_ACCOUNT_DISCOUNT,
                                     TYPE_FIXED_CHILD_DISCOUNT,
                                     TYPE_LOYALTY,
                                     TYPE_LOYALTY_CHILD_DISCOUNT};
        return intersect(this.getLineItems(invoiceIds), this.getLineItems(billingArgs));
    }

    /**
     * Sum Calculation: If the InvoiceLine instance is from a booking then both the TYPE_EARLY_DROPOFF and TYPE_LATE_PICKUP lineItems are included in the
     * calculation.
     * Non Booking InvoiceLine instances will return a zero value
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public long getDiscountValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getDiscountItems(invoiceIds));
    }

    /**
     * Returns the discount rate appropriate to this line
     * @return
     */
    public int getDiscountRate() {
        if(isBooking()) {
            return child.getBookingDiscountRate();
        }
        else if(isChildFixedItem()) {
            return child.getFixedItemDiscountRate();
        }
        return account.getFixedItemDiscountRate();
    }

    /**
     * LineItemDTO Filter: Returns all the lineItem instances.
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the List.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public List<LineItemDTO> getLineItems(Integer... invoiceIds) {
        List<LineItemDTO> rtnList = new LinkedList<>();
        Set<Integer> invoiceIdSet = VABoolean.asSet(invoiceIds);
        for(LineItemDTO lineItem : lineItemList) {
            if(invoiceIdSet.isEmpty() || invoiceIdSet.contains(lineItem.getInvoiceId())) {
                rtnList.add(lineItem);
            }
        }
        return rtnList;
    }

    /*
     * Generalised Method: gets a list of line items filtered on the billingEnum Arguments
     */
    public List<LineItemDTO> getLineItems(BillingEnum... billingEnumArgs) {
        List<LineItemDTO> rtnList = new LinkedList<>();
        for(LineItemDTO lineItem : lineItemList) {
            if(billingEnumArgs.length == 0 || lineItem.hasAnyBillingBit(billingEnumArgs)) {
                rtnList.add(lineItem);
            }
        }
        return rtnList;
    }

    /**
     * Sum Calculation: returns all the lineItem values.
     * Optionally you can filter the selection by the lineItem invoiceId. By passing zero or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return
     */
    public long getLineItemsValue(Integer... invoiceIds) {
        return getAllLineItemsValue(getLineItems(invoiceIds));
    }

    /**
     * Calculated Field: An array of sum values taken from all the lineItemDTO's.
     * The returned values are signed with CREDIT negative(-) and CHARGE positive(+).
     * Optionally you can filter the selection by the lineItem invoiceId. By passing one or more
     * invoiceId's, only the lineItems with those invoiceId values will be included in the calculation.
     * The return array is:<br>
     * index[0] - value inc tax <br>
     * index[1] - value ex tax <br>
     * index[2] - tax <br>
     *
     * @param invoiceIds an optional filter to only included the listed invoiceId's
     * @return an array of invoice values with tax, without tax and just tax
     */
    public long[] getSumValueArray(Integer... invoiceIds) {
        Set<Integer> invoiceIdSet = VABoolean.asSet(invoiceIds);
        long[] rtnValues = new long[3];
        for(LineItemDTO lineItem : lineItemList) {
            if(invoiceIdSet.isEmpty() || invoiceIdSet.contains(lineItem.getInvoiceId())) {
                long[] lineInvoiceValues = lineItem.getSignedValueArray();
                rtnValues[BillingBean.VALUE_INC_TAX] += lineInvoiceValues[0];
                rtnValues[BillingBean.VALUE_ONLY] += lineInvoiceValues[1];
                rtnValues[BillingBean.TAX_ONLY] += lineInvoiceValues[2];
            }
        }
        return rtnValues;
    }

    public long[] getAllPreviousInvoiceValues() {
        if(isInvoicedPreviously()) {
            return getSumValueArray(getLineItemInvoiceIds(invoiceId));
        }
        return new long[3];
    }

    public boolean isInvoicedPreviously() {
        return getLineItemInvoiceIds(invoiceId).length > 0;
    }

    public long[] getThisInvoiceValues() {
        return getSumValueArray(invoiceId);
    }

    public boolean isCreditLine() {
        return getThisInvoiceValues()[0] < 0;
    }

    public static final Comparator<InvoiceLineDTO> YWD_TYPE_SD_ORDER = new Comparator<InvoiceLineDTO>() {
        @Override
        public int compare(InvoiceLineDTO dto1, InvoiceLineDTO dto2) {
            if (dto1 == null && dto2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (dto1 != null && dto2 == null) {
                return -1;
            }
            if (dto1 == null && dto2 != null) {
                return 1;
            }
            // compare ywd first (new to old)
            int ywdResult = ((Integer)dto1.getYwd()).compareTo((Integer)dto2.getYwd());
            if(ywdResult != 0) {
                return ywdResult;
            }
            // compare ywd first (new to old)
            int typeResult = ((Integer)dto1.getGroup()).compareTo((Integer)dto2.getGroup());
            if(typeResult != 0) {
                return typeResult;
            }
            int startResult = ((Integer)dto2.getOrderStart()).compareTo((Integer)dto1.getOrderStart());
            if(startResult != 0) {
                return startResult;
            }
            // compare sd if the same ywd (earliest to latest)
            // not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)dto1.hashCode()).compareTo((Integer)dto2.hashCode()));
        }
    };



    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.ywd;
        hash = 53 * hash + this.group;
        hash = 53 * hash + this.orderStart;
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.child);
        hash = 53 * hash + Objects.hashCode(this.booking);
        hash = 53 * hash + Objects.hashCode(this.bookingType);
        hash = 53 * hash + Objects.hashCode(this.lineItemList);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final InvoiceLineDTO other = (InvoiceLineDTO) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.group != other.group) {
            return false;
        }
        if(this.orderStart != other.orderStart) {
            return false;
        }
        if(!Objects.equals(this.description, other.description)) {
            return false;
        }
        if(!Objects.equals(this.child, other.child)) {
            return false;
        }
        if(!Objects.equals(this.booking, other.booking)) {
            return false;
        }
        if(!Objects.equals(this.bookingType, other.bookingType)) {
            return false;
        }
        if(!Objects.equals(this.lineItemList, other.lineItemList)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InvoiceLineDTO:\n");
        sb.append(" ywd          = ").append(ywd).append("\n");
        sb.append(" orderStart   = ").append(orderStart).append("\n");
        sb.append(" group        = ").append(group).append("\n");
        sb.append(" description  = ").append(description).append("\n");
        sb.append(" child       -> ").append(child == null? "null\n" : child.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.PRINTED));
        sb.append(" booking     -> ").append(booking == null? "null\n" : booking.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.PRINTED));
        sb.append(" bookingType -> ").append(bookingType == null? "null\n" : bookingType.toXML(ObjectDataOptionsEnum.COMPACTED, ObjectDataOptionsEnum.PRINTED)).append("\n");
        for(LineItemDTO lineItemDTO : lineItemList) {
            sb.append(lineItemDTO.toString()).append("\n");
        }
        return sb.toString();
    }

    /*
     * Internal Method: to get the total value of a list of lineItemDTO passed.
     */
    protected static long getAllLineItemsValue(List<LineItemDTO> valueList) {
        long rtnValue = 0;
        for(LineItemDTO lineItem : valueList) {
            rtnValue += lineItem.getSignedValue();
        }
        return rtnValue;
    }

    /*
     * Internal Method: to get the intersect of two LineItemDTO lists.
     */
    private static List<LineItemDTO> intersect(List<LineItemDTO> A, List<LineItemDTO> B) {
        List<LineItemDTO> rtnList = new LinkedList<>();
        for(LineItemDTO dto : A) {
            if(B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }

    /*
     * Internal Method: to getall line item invoiceIds, excluding those passed.
     */
    private Set<Integer> getFilteredInvoiceIds(Integer... excludeIds) {
        ConcurrentSkipListSet<Integer> rtnSet = new ConcurrentSkipListSet<>();
        Set<Integer> excludeIdSet = VABoolean.asSet(excludeIds);
        for(LineItemDTO lineItem : lineItemList) {
            rtnSet.add(lineItem.getInvoiceId());
        }
        //remove excluded
        rtnSet.removeAll(excludeIdSet);
        return rtnSet;
    }

}
