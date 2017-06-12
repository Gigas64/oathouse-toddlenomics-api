/*
 * @(#)InvoiceLineTransformer.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.accounts.TransactionService;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceType;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_ADJUSTMENT_CHILD_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_ADJUSTMENT_ON_ALL;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_ADJUSTMENT_ON_ATTENDING;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_BOOKING_CHILD_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_EARLY_DROPOFF;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FIXED_ACCOUNT_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FIXED_CHILD_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FIXED_ITEM;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FUNDED;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_LATE_PICKUP;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_LOYALTY;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_LOYALTY_CHILD_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_SESSION;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The {@literal InvoiceLineTransformer} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 24-May-2012
 */
public class InvoiceLineTransformer {

    /**
     * Returns a ConcurrentHashMap of ProfileId key map pointing to a sorted
     * list of InvoiceLineDTO
     *
     * @param invoiceId
     * @param billingList
     * @return a ConcurrentHashMap so that the order of the keys is maintained
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalValueException
     */
    public static ConcurrentHashMap<Integer,List<InvoiceLineDTO>> toDTO(int invoiceId, List<BillingBean> billingList) throws PersistenceException, NoSuchIdentifierException, IllegalValueException {
        // use a hashMap so it retains its order
        ConcurrentHashMap<Integer,List<InvoiceLineDTO>> rtnMap = new ConcurrentHashMap<>();

        ChildService childService = ChildService.getInstance();

        //sort the billings into their child id (-1 if no profile)
        ConcurrentSkipListMap<Integer, List<BillingBean>> billingMap = new ConcurrentSkipListMap<>();
        int accountId = -1;
        for(BillingBean billing : billingList) {
            //we need to check that all the billings are for the same account
            if(accountId == -1) {
                accountId = billing.getAccountId();
            } else {
                if(accountId != billing.getAccountId()) {
                    throw new IllegalValueException("All billings must be from the same account: accountId '" + accountId + "' and accountId '" + billing.getAccountId() + " found' found");
                }
            }
            // if the profile key doesn't exist
            if(!billingMap.containsKey(billing.getProfileId())) {
                billingMap.put(billing.getProfileId(), new LinkedList<BillingBean>());
            }
            billingMap.get(billing.getProfileId()).add(billing);
        }

        //run through the billing listand convert to DTO
        for(int childId : billingMap.descendingKeySet()) {
            // within the child break them into booking, loyalty and fixed items
            Map<Integer, List<BillingBean>> bookingMap = new ConcurrentSkipListMap<>(); // bookingId -> billingList
            Map<Integer, List<BillingBean>> loyaltyMap = new ConcurrentSkipListMap<>(); // loyaltyId -> billingList
            Map<Integer, List<BillingBean>> fixedChildMap = new ConcurrentSkipListMap<>(); // loyaltyId -> billingList
            Map<Integer, List<BillingBean>> fixedAccountMap = new ConcurrentSkipListMap<>(); // loyaltyId -> billingList
            for(BillingBean billing : billingMap.get(childId)) {
                BillingEnum billingType = BillingEnum.getBillingEnumForLevel(billing.getBillingBits(), TYPE);
                switch(billingType) {
                    case TYPE_ADJUSTMENT_ON_ATTENDING:
                    case TYPE_ADJUSTMENT_ON_ALL:
                    case TYPE_LATE_PICKUP:
                    case TYPE_EARLY_DROPOFF:
                    case TYPE_BOOKING_CHILD_DISCOUNT:
                    case TYPE_ADJUSTMENT_CHILD_DISCOUNT:
                    case TYPE_SESSION:
                    case TYPE_FUNDED:
                        if(!bookingMap.containsKey(billing.getBookingId())) {
                            bookingMap.put(billing.getBookingId(), new LinkedList<BillingBean>());
                        }
                        bookingMap.get(billing.getBookingId()).add(billing);
                        break;
                    case TYPE_LOYALTY:
                    case TYPE_LOYALTY_CHILD_DISCOUNT:
                        if(!loyaltyMap.containsKey(billing.getBookingId())) {
                            loyaltyMap.put(billing.getBookingId(), new LinkedList<BillingBean>());
                        }
                        loyaltyMap.get(billing.getBookingId()).add(billing);
                        break;
                    case TYPE_FIXED_ITEM:
                    case TYPE_FIXED_CHILD_DISCOUNT:
                    case TYPE_FIXED_ACCOUNT_DISCOUNT:
                        int key = billing.getBillingId();
                        /*
                         * Because discount association is from billing to discount (the billing has the discountId)
                         * there is no way of knowing, if the billing is a dicsount, which billing it belongs to
                         * and thus which key toput it into so if we find a billing with a discount then key on
                         * the discountId and not the billingId so when the dicount is processed it is automatically
                         * assigned to the same key as the billing and thus grouped together.
                         */
                        if(billing.getDiscountId() > 0) {
                            key = billing.getDiscountId();
                        }
                        if(childId > 0) {
                            if(!fixedChildMap.containsKey(key)) {
                                fixedChildMap.put(key, new LinkedList<BillingBean>());
                            }
                            fixedChildMap.get(key).add(billing);
                        } else {
                            if(!fixedAccountMap.containsKey(key)) {
                                fixedAccountMap.put(key, new LinkedList<BillingBean>());
                            }
                            fixedAccountMap.get(key).add(billing);
                        }
                        break;
                    default:
                        throw new IllegalValueException("The billingEnum type '" + billingType.toString() + "' has not been mapped");
                }
            }
            List<InvoiceLineDTO> InvoiceLineDTOList = new LinkedList<>();
            AccountBean account = childService.getAccountManager().getObject(accountId);
            if(childId > 0) {
                ChildBean child = childService.getChildManager().getObject(childId, ObjectDataOptionsEnum.ARCHIVE);
                InvoiceLineDTOList.addAll(getBookingDto(invoiceId, account, child, bookingMap));
                InvoiceLineDTOList.addAll(getLoyaltyDto(invoiceId, account, child, loyaltyMap));
                InvoiceLineDTOList.addAll(getFixedItemDto(invoiceId, account, child, fixedChildMap));
            } else {
                InvoiceLineDTOList.addAll(getFixedItemDto(invoiceId, account, null, fixedAccountMap));
            }
            // now sort
            Collections.sort(InvoiceLineDTOList, InvoiceLineDTO.YWD_TYPE_SD_ORDER);
            rtnMap.put(childId, InvoiceLineDTOList);
        }
        return rtnMap;
    }


    /**
     * returns the invoiceLineDTO for a receipt fee invoice. If the invoice number is not a receipt fee then
     * an IllegalActionException is thrown.
     *
     * @param invoiceId
     * @return
     * @throws IllegalActionException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public static InvoiceLineDTO feeInvoiceToDTO(int invoiceId) throws IllegalActionException, PersistenceException, NoSuchIdentifierException {
        // assuming I give you a fee invoice Id, just get the single billing, transform it and return
        TransactionService transactionService = TransactionService.getInstance();
        ChildService childService = ChildService.getInstance();
        BillingService billingService = BillingService.getInstance();

        InvoiceBean invoice = transactionService.getInvoiceManager().getObject(invoiceId);
        if(!invoice.isType(InvoiceType.RECEIPT_FEE)) {
            throw new IllegalActionException("The invoiceId '" + invoiceId + "' is not a Reciept Fee Invoice");
        }
        AccountBean account = childService.getAccountManager().getObject(invoice.getAccountId());
        final List<BillingBean> billingList = billingService.getBillingsForInvoice(invoice.getAccountId(), invoiceId);
        if(billingList.size() != 1) {
            throw new IllegalActionException("The invoiceId '" + invoiceId + "' does not have a single billing in it");
        }
        BillingBean billing = billingList.get(0);
        final List<LineItemDTO> lineItems = new LinkedList<>();
        lineItems.add(new LineItemDTO(billing.getDescription(), billing.getBillingSd(), billing.getValue(),
                billing.getTaxRate(), billing.getBillingBits(), billing.getInvoiceId(), billing.getDiscountId(), billing.getNotes()));
        // create the InvoiceLineDTO
        return(new InvoiceLineDTO(invoiceId, billing.getYwd(), InvoiceLineDTO.FIXED_ITEM_ACCOUNT, -1,
                billing.getDescription(), account, null, null, null, lineItems));
    }


    /*
     * create an invoiceLineDto from a booking
     * Map: bookingId - billingBean
     */
    protected static List<InvoiceLineDTO> getBookingDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap)
            throws NoSuchIdentifierException, PersistenceException {
        List<InvoiceLineDTO> rtnList = new LinkedList<>();

        ChildBookingService bookingService = ChildBookingService.getInstance();

        if(billingMap == null) {
            return rtnList;
        }
        for(int bookingId : billingMap.keySet()) {
            if(billingMap.get(bookingId) == null || billingMap.get(bookingId).isEmpty()) {
                continue;
            }
            // get all the Objects
            final BookingBean booking = bookingService.getBookingManager().getObject(bookingId, ObjectDataOptionsEnum.ARCHIVE);
            final BookingTypeBean bookingType = bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId());
            final int ywd = booking.getYwd();
            final String description = ApplicationConstants.BOOKING;
            final List<BillingBean> billItems = billingMap.get(bookingId);
            // create the line items
            final List<LineItemDTO> lineItems = new LinkedList<>();
            for (BillingBean billItem : billItems) {
                lineItems.add(new LineItemDTO(billItem.getDescription(), billItem.getBillingSd(), billItem.getValue(),
                        billItem.getTaxRate(), billItem.getBillingBits(), billItem.getInvoiceId(), billItem.getDiscountId(), billItem.getNotes()));
            }
            Collections.sort(lineItems, LineItemDTO.BILLING_SD_ORDER);
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.BOOKING_ADJUSTMENT, booking.getBookingStart(), description,
                    account, child, booking, bookingType, lineItems));
        }
        return rtnList;
    }

    /*
     * creates a List of invoiceLineDto from all loyalty discounts.
     * Map: loyaltyId (bookingId) - billingBean
     */
    protected static List<InvoiceLineDTO> getLoyaltyDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap) {
        List<InvoiceLineDTO> rtnList = new LinkedList<>();

        if(billingMap == null) {
            return rtnList;
        }
        for(int loyaltyId : billingMap.keySet()) {
            if(billingMap.get(loyaltyId) == null || billingMap.get(loyaltyId).isEmpty()) {
                continue;
            }
            BookingBean booking = null;
            BookingTypeBean bookingType = null;
            final String description = billingMap.get(loyaltyId).get(0).getDescription();
            final int ywd = billingMap.get(loyaltyId).get(0).getYwd();
            int orderStart = -1;
            // create the line items
            final List<LineItemDTO> lineItems = new LinkedList<>();
            for (BillingBean billItem : billingMap.get(loyaltyId)) {
                lineItems.add(new LineItemDTO(billItem.getDescription(), billItem.getBillingSd(), billItem.getValue(),
                        billItem.getTaxRate(), billItem.getBillingBits(), billItem.getInvoiceId(), billItem.getDiscountId(), billItem.getNotes()));
                // take the lastest orderStart
                orderStart = SDHolder.getStart(billItem.getBillingSd()) > orderStart ? SDHolder.getStart(billItem.getBillingSd()) : orderStart;
            }
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.LOYALTY_DISCOUNT, orderStart, description,
                    account, child, booking, bookingType, lineItems));
        }
        return rtnList;
    }

    /*
     * creates all the DTO for fixed items relating to a child
     */
    protected static List<InvoiceLineDTO> getFixedItemDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap) {
        List<InvoiceLineDTO> rtnList = new LinkedList<>();

        if(billingMap == null) {
            return rtnList;
        }
        // remeber the FixedItems can be keyed on the discountId or the billingId if no discount
        for(int billingId : billingMap.keySet()) {
            if(billingMap.get(billingId) == null || billingMap.get(billingId).isEmpty()) {
                continue;
            }
            BookingBean booking = null;
            BookingTypeBean bookingType = null;
            String description = child == null? "Fixed Item on the Account" : "Fixed Item for a Child";
            int group = child == null? InvoiceLineDTO.FIXED_ITEM_ACCOUNT : InvoiceLineDTO.FIXED_ITEM_CHILD;
            final int ywd = billingMap.get(billingId).get(0).getYwd();
            // get all the billing items under this key
            final List<BillingBean> billItemList = billingMap.get(billingId);
            // run through each andconvert to a line item
            final List<LineItemDTO> lineItemList = new LinkedList<>();
            for (BillingBean fixedItem : billItemList) {
                LineItemDTO lineItem = new LineItemDTO(fixedItem.getDescription(), fixedItem.getBillingSd(), fixedItem.getValue(),
                        fixedItem.getTaxRate(), fixedItem.getBillingBits(), fixedItem.getInvoiceId(), fixedItem.getDiscountId(), fixedItem.getNotes());
                // make sure the discount is last in the list and the descrition is correct
                if(fixedItem.hasBillingBits(BillingEnum.TYPE_FIXED_ITEM)) {
                    lineItemList.add(0,lineItem);
                    description = fixedItem.getDescription();
                } else {
                    // if not a fixed item type it must be a disccount
                    lineItemList.add(lineItem);
                }
            }
            // create the InvoiceLineDTO
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, group, -1, description, account, child, booking, bookingType, lineItemList));
        }
        return rtnList;
    }

    /*
     * private constructor as all methods are static
     */
    protected InvoiceLineTransformer() {
    }
}
