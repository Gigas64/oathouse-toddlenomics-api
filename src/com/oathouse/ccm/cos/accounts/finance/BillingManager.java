/*
 * @(#)BillingManager.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.finance.*;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code BillingManager} Class extends the methods of the parent class.
 * This map stores the billing items against an account.
 * <p>
 *      Key - AccountId <br>
 *      Id  - BillingId
 * </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Mar-2012
 */
public class BillingManager extends ObjectMapStore<BillingBean> {

    /**
     * Constructs a {@code BillingManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public BillingManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Helper method to retrieve all the billing objects for an account that are in a specific week.
     * The selection can be further filtered on chargeBit and optionally by billingEnum values
     *
     * @param yw the week of interest
     * @param accountId the id of the account to check
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean objects that exist for an account in a certain ywd
     * @throws PersistenceException
     */
    public List<BillingBean> getYwBilling(int accountId, int yw, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        int yw0 = YWDHolder.getYW(yw);
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            int ywd = yw0 + day;
            rtnList.addAll(this.getYwdBilling(accountId, ywd, chargeBits, onlyBillingArgs));
        }
        return rtnList;
    }

    /**
     * Helper method to retrieve all the billing objects for an account that are on a specific ywd.
     * The selection can be further filtered on chargeBit and optionally by billingEnum values
     *
     * @param ywd the ywd
     * @param accountId the id of the account to check
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean objects that exist for an account in a certain ywd
     * @throws PersistenceException
     */
    public List<BillingBean> getYwdBilling(int accountId, int ywd, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : getAllObjects(accountId)) {
            if(BTBits.isNotBits(billing.getBtChargeBit(), chargeBits)){
                continue;
            }
            if(onlyBillingArgs.length > 0 && !billing.hasBillingBits(onlyBillingArgs)) {
                continue;
            }
            if(billing.getYwd() != ywd) {
                continue;
            }
            // filter complete so now add the billing to the list
            rtnList.add(billing);
        }
        return rtnList;
    }

    /**
     * Helper method to retrieve all the billing objects for an account for a specific profileId
     * that are in a specific week
     *
     * @param accountId the id of the account to check
     * @param yw the week or interest
     * @param profileId the profile Id
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean objects that exist for an account and profileId in a certain ywd
     * @throws PersistenceException
     */
    public List<BillingBean> getYwBillingForProfile(int accountId, int yw, int profileId, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        int yw0 = YWDHolder.getYW(yw);
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            int ywd = yw0 + day;
            rtnList.addAll(this.getYwdBillingForProfile(accountId, ywd, profileId, chargeBits, onlyBillingArgs));
        }
        return rtnList;
    }

    /**
     * Helper method to retrieve all the billing objects for an account for a specific profileId
     * that are on a specific day
     *
     * @param accountId the id of the account to check
     * @param ywd the ywd or interest
     * @param profileId the profile Id
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean objects that exist for an account and profileId in a certain ywd
     * @throws PersistenceException
     */
    public List<BillingBean> getYwdBillingForProfile(int accountId, int ywd, int profileId, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getYwdBilling(accountId, ywd, chargeBits, onlyBillingArgs)) {
            if(billing.getProfileId() == profileId) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    /**
     * Helper method to retrieve a signed total value of all billings associated with a set
     * of parameters, each of which are optional.
     * If adjustmentId is set to -1 then it is ignored.
     * The BillingEnum ellipsis is 'any-of' and is optional.
     * Returns the total signed value of all billings based on the filter criteria. Credit is negative, debit is positive
     *
     * @param accountId
     * @param bookingId an optional bookingId filter or -1 if not used
     * @param adjustmentId an optional adjustmentId filter or -1 if not used
     * @param anyBillingArgs an optional comma separated list of 0..n BillingEnum to filter
     * @return the total signed value, negative credit, positive debit.
     * @throws PersistenceException
     */
    public long getSignedTotalValue(int accountId, int bookingId, int adjustmentId, BillingEnum... anyBillingArgs) throws PersistenceException {
        long rtnValue = 0;
        for(BillingBean billing : this.getAllObjects(accountId)) {
            // filter billingEnum
            if(anyBillingArgs.length > 0 && !billing.hasAnyBillingBit(anyBillingArgs)) {
                continue;
            }
            // filter bookingId
            if(bookingId > 0 && billing.getBookingId() != bookingId) {
                continue;
            }
            // filter adjustmentId
            if(adjustmentId > 0 && billing.getAdjustmentId() != adjustmentId) {
                continue;
            }
            rtnValue += billing.getSignedValueArray()[BillingBean.VALUE_ONLY];
        }
        return rtnValue;
    }

    /**
     * Helper method to retrieve a discount associated with a billing.
     *
     * @param accountId the account of the billing
     * @param billingId the id of the billing with a discount.
     * @return the discount billing associated with the billingId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public BillingBean getDiscountBilling(int accountId, int billingId) throws PersistenceException, NoSuchIdentifierException{
        BillingBean billing = getObject(accountId, billingId);
        if(billing.getDiscountId() > 0) {
            return getObject(accountId, billing.getDiscountId());
        }
        throw new NoSuchIdentifierException("The billing id '" + billingId + "' does not have a discount associated");
    }

    /**
     * returns a list of all outstanding BillingBean objects from an accountId that have not yet
     * been allocated to an invoice on the specified account. The optional BillingEnum arguments
     * are a comma separated list of ANY billingEnum filter.
     *
     * @param accountId the id of the account to check
     * @param lastYwd the last ywd to get outstanding to.
     * @param anyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingOutstanding(int accountId, int lastYwd, BillingEnum... anyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            // filter out if already invoiced or after date
            if(billing.getInvoiceId() > 0 || billing.getYwd() > lastYwd) {
                continue;
            }
            // filter on billing arguments
            if(anyBillingArgs.length > 0 && !billing.hasAnyBillingBit(anyBillingArgs)) {
                continue;
            }
            rtnList.add(billing);

        }
        return rtnList;
    }

    /**
     * returns a list of all BillingBean objects for an account Id that have been allocated to an invoice.
     *
     * @param accountId the id of the account to check
     * @param invoiceId the invoice id to filter on
     * @param anyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingForInvoice(int accountId, int invoiceId, BillingEnum... anyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            if(anyBillingArgs.length > 0 && !billing.hasAnyBillingBit(anyBillingArgs)) {
                continue;
            }
            if(billing.getInvoiceId() == invoiceId) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    /**
     * Determines the invoiceIds associated with the billings for a single booking.
     * May return an empty set if billings have yet to be applied to an invoice.
     *
     * @param accountId
     * @param bookingId
     * @return
     * @throws PersistenceException
     */
    public Set<Integer> getAllInvoiceIdsForBooking(int accountId, int bookingId) throws PersistenceException {
        Set<Integer> invoiceIds = new ConcurrentSkipListSet<>();
        for(BillingBean billing : getAllBillingForBooking(accountId, bookingId, BTIdBits.TYPE_CHARGE)) {
            if(billing.getInvoiceId() != ObjectEnum.INITIALISATION.value()) {
                invoiceIds.add(billing.getInvoiceId());
            }
        }
        return invoiceIds;
    }

    /**
     * Helper method to find all the associated BillingBean objects with a bookingId from an account. The list can be
     * further filtered on chargeBits and optionally BillingEnum. If no billing enumerations are provided then
     * all are included. The billing must belong to a booking not a loyalty or fixed item
     *
     * @param accountId the id of the account to check
     * @param bookingId the id of the booking
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingForBooking(int accountId, int bookingId, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            if(!billing.hasBillingBits(BillingEnum.GROUP_BOOKING)) {
                continue;
            }
            if(billing.getBookingId() != bookingId) {
                continue;
            }
            if(BTBits.isNotBits(billing.getBtChargeBit(), chargeBits)){
                continue;
            }
            if(onlyBillingArgs.length > 0 && !billing.hasBillingBits(onlyBillingArgs)) {
                continue;
            }
            // filter complete so now add the billing to the list
            rtnList.add(billing);
        }
        return rtnList;
    }

    /**
     * Helper method to find all the associated BillingBean objects with a bookingId from an account where the bookingId
     * is the Loyalty trigger. The list can be further filtered on chargeBits and optionally BillingEnum. If no billing
     * enumerations are provided then all are included.
     *
     * @param accountId the id of the account to check
     * @param bookingId the id of the booking that is the loyalty trigger
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingForBookingLoyalty(int accountId, int bookingId, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            if(!billing.hasBillingBits(BillingEnum.GROUP_LOYALTY)) {
                continue;
            }
            if(billing.getBookingId() != bookingId) {
                continue;
            }
            if(BTBits.isNotBits(billing.getBtChargeBit(), chargeBits)){
                continue;
            }
            if(onlyBillingArgs.length > 0 && !billing.hasBillingBits(onlyBillingArgs)) {
                continue;
            }
            // filter complete so now add the billing to the list
            rtnList.add(billing);
        }
        return rtnList;
    }

    /**
     * Helper Method to retrieve all BillingBean objects, filtered by bookingId, where the
     * loyaltyId of the billing is not found in the loyalty set.
     *
     * @param accountId
     * @param bookingId
     * @param loyaltyIdSet
     * @return list of BillingBean objects
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingsWithLoyaltyIdNotFound(int accountId, int bookingId, Set<Integer> loyaltyIdSet) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            if(!billing.hasBillingBits(BillingEnum.GROUP_LOYALTY)) {
                continue;
            }
            if(billing.getBookingId() != bookingId) {
                continue;
            }
            if(billing.getAdjustmentId() > 0 && !loyaltyIdSet.contains(billing.getAdjustmentId())) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    /**
     * Helper Method to retrieve all BillingBean objects, filtered by bookingId, where the
     * adjustmentId of the billing is not found in the adjustmentId set.
     *
     * @param accountId
     * @param bookingId
     * @param adjustmentIdSet
     * @return list of BillingBean objects
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingsWithAdjustmentIdNotFound(int accountId, int bookingId, Set<Integer> adjustmentIdSet) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : this.getAllObjects(accountId)) {
            if(!billing.hasBillingBits(BillingEnum.GROUP_BOOKING)) {
                continue;
            }
            if(billing.getBookingId() != bookingId) {
                continue;
            }
            if(billing.getAdjustmentId() > 0 && !adjustmentIdSet.contains(billing.getAdjustmentId())) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    /**
     * Helper method to find all the associated BillingBean objects with a bookingId. The list can be
     * further filtered on chargeBits and optionally BillingEnum. If no billing enumerations are provided then
     * all are included. NOTE: if you know the accountId then use the overloaded method that includes the
     * accountId as a parameter as this is a much quicker search.
     *
     * @param bookingId the id of the booking
     * @param chargeBits the BTBits representing the charge element of the bookingTypeId
     * @param onlyBillingArgs (optional) a comma separated list of BillingEnum to filter on
     * @return list of BillingBean objects
     * @throws PersistenceException
     */
    public List<BillingBean> getAllBillingForBooking(int bookingId, int chargeBits, BillingEnum... onlyBillingArgs) throws PersistenceException {
        final List<BillingBean> rtnList = new LinkedList<>();
        for(int accountId : this.getAllKeys()) {
            rtnList.addAll(this.getAllBillingForBooking(accountId, bookingId, chargeBits, onlyBillingArgs));
        }
        return rtnList;
    }

    /**
     * Object set method allowing the invoiceId to be set outwith the BillingBean constructor
     *
     * @param accountId the id of the account to check
     * @param billingId
     * @param invoiceId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectInvoiceId(int accountId, int billingId, int invoiceId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        final BillingBean billing = this.getObject(accountId, billingId);
        billing.setInvoiceId(invoiceId, owner);
        super.setObject(accountId, billing);
    }

    /**
     * Object set method allowing the discountId to be set outwith the BillingBean constructor
     *
     * @param accountId
     * @param billingId
     * @param discountId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectDiscountId(int accountId, int billingId, int discountId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        final BillingBean billing = this.getObject(accountId, billingId);
        billing.setDiscountId(discountId, owner);
        super.setObject(accountId, billing);
    }

    /**
     * A static helper method to allow the discountId to be set for a specific billing passed.
     * This method does not store the changes to disk but ONLY modifies the billing passed
     *
     * @param billing
     * @param discountId
     * @param owner
     */
    public static void setObjectDiscountId(BillingBean billing, int discountId, String owner) {
        billing.setDiscountId(discountId, owner);
    }

    /**
     * Object set method allowing the notes to be set outwith the BillingBean constructor
     *
     * @param accountId the id of the account to check
     * @param billingId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setObjectNotes(int accountId, int billingId, String notes, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        final BillingBean billing = this.getObject(accountId, billingId);
        billing.setNotes(notes, owner);
        super.setObject(accountId, billing);
    }

    /**
     * Override of the underlying removeObject() to ensure a billing with an invoiceId associated does not
     * get removed and any associated discount billing is also removed
     *
     * @param accountId
     * @param billingId
     * @return
     * @throws PersistenceException
     */
    @Override
    public BillingBean removeObject(int accountId, int billingId) throws PersistenceException {
        int discountId = -1;
        try {
            BillingBean billing = getObject(accountId, billingId);
            if(billing.getInvoiceId() > 0) {
                throw new PersistenceException("The billing id '" + billing.getBillingId() + "' has been allocated to invoice '" + billing.getInvoiceId() + " and can't be changed");
            }
            discountId = billing.getDiscountId();
        } catch(NoSuchIdentifierException nsie) {
            // carry on and let super removedObject deal with the exception
        }
        if(discountId > 0) {
            super.removeObject(accountId, discountId);
        }
        return super.removeObject(accountId, billingId);
    }

    /**
     * Override of the underlying setObject() to ensure a billing with an invoiceId associated does not
     * get removed.
     *
     * @param accountId
     * @param ob
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     */
    @Override
    public BillingBean setObject(int accountId, BillingBean ob) throws NullObjectException, PersistenceException {
        if(ob != null && this.isIdentifier(accountId, ob.getBillingId())) {
            try {
                BillingBean billing = getObject(accountId, ob.getBillingId());
                if(billing.getInvoiceId() > 0) {
                    throw new PersistenceException("The billing id '" + ob.getBillingId() + "' has been allocated to invoice '" + ob.getInvoiceId() + " and can't be changed");
                }
            } catch(NoSuchIdentifierException nsi) {
                // carry on as the billing obvioulsy doesn't exist
            }
        }
        return super.setObject(accountId, ob);
    }

    /**
     * Rollback method that allows multiple billings to be rolled back. The method returns a set of integers
     * representing billing identifiers that were not rolled back. Every attempt is made to roll back but
     * billings can't be allocated to an invoice.
     *
     * @param accountId
     * @param BillingList
     * @return a set of 0..n bookingIds that were not rolled back.
     * @throws PersistenceException
     */
    public Set<Integer> rollback(int accountId, List<BillingBean> BillingList) throws PersistenceException {
        Set<Integer> billingIdSet = new ConcurrentSkipListSet<>();
        for(BillingBean billing : BillingList) {
            billingIdSet.add(billing.getBillingId());
        }
        return rollback(accountId, billingIdSet);
    }

    /**
     * Rollback method that allows multiple billings to be rolled back. The method returns a set of integers
     * representing billing identifiers that were not rolled back. Every attempt is made to roll back but
     * billings can't be allocated to an invoice.
     *
     * @param accountId
     * @param billingIdSet
     * @return list of Identifiers of non rolled back billings
     * @return a set of 0..n bookingIds that were not rolled back.
     * @throws PersistenceException
     */
    public Set<Integer> rollback(int accountId, Set<Integer> billingIdSet) throws PersistenceException {
        Set<Integer> returnSet = new ConcurrentSkipListSet<>();
        for(int billingId : billingIdSet) {
            try {
                // use this instance remove as we don't want to rollback on invoiced billing
                this.removeObject(accountId, billingId);
            } catch(PersistenceException pe) {
                // add this to the rejected pile
                returnSet.add(billingId);
            }
        }
        return returnSet;
    }

}
