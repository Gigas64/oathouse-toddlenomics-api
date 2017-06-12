/*
 * @(#)BillingService.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cma.VT;
import static com.oathouse.ccm.cma.VT.BILLING;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PriceConfigService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.config.TimetableService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.YwdComparator;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingManager;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableManager;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.MultiRefEnum;
import static com.oathouse.ccm.cos.config.finance.MultiRefEnum.*;
import com.oathouse.ccm.cos.config.finance.PriceAdjustmentBean;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.config.finance.TariffBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDBits;
import com.oathouse.oss.storage.valueholder.SDHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The {@literal BillingService} Class is a encapsulated API, service level singleton that provides
 * access to related underlying manager instances. The class provides three distinct sections:
 * <h2>Manager Retrieval</h2>
 * <p>
 * This section allows the retrieval of the instance of the manager to use as part of this service. For
 * ease of reference each manager has both a long and a short name method call. ALL manager instances should
 * be referenced through the Service and not instantiated directly.
 * </p>
 *
 * <h2>Service Level get Methods</h2>
 * <p>
 * This section is for all get methods that require information outside their own data store. All other
 * get methods can be found up the tree in the manager or bean of the bean type.
 * </p>
 *
 * <h2>Service Level set Methods</h2>
 * <p>
 * All set methods must be implemented up to the Service level as this allows consistency across the API and
 * security for the underlying sets. If we have sets at the Service level, the Manager level and ObjectStore,
 * the inconsistency might cause a set to be made to the manager when a set in the service adds additional logic
 * and thus is bypassed.
 * </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 30-Jul-2013
 */
public class BillingService {
    // Singleton Instance
    private volatile static BillingService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final BillingManager billingManager = new BillingManager(VT.BILLING.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private BillingService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@literal BillingService} class
     * @return instance of the {@literal BillingService}
     * @throws PersistenceException
     */
    public static BillingService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (BillingService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new BillingService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@literal BillingService} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying
     * elements of the initialisation are not available.
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return(true);
        }
        return(false);
    }

    /**
     * initialise all the managed classes in the {@literal BillingService}. The
     * method returns an instance of the {@literal BillingService} so it can be chained.
     * This must be called before a the {@literal BillingService} is used. e.g  {@literal BillingService myBillingService = }
     * @return instance of the {@literal BillingService}
     * @throws PersistenceException
     */
    public synchronized BillingService init() throws PersistenceException {
        if(initialise) {
            billingManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@literal BillingService}. The
     * method returns an instance of the {@literal BillingService} so it can be chained.
     * @return instance of the {@literal BillingService}
     * @throws PersistenceException
     */
    public BillingService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@literal BillingService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = billingManager.clear()?success:false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@literal BillingService} class instance by setting the INSTANCE reference
     * to null. This is mostly used for testing to clear and reset internal memory stores
     * when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval">
    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     *
     * This section allows the retrieval of the instance
     * of the manager to use as part of this service. For
     * ease of reference each manager has both a long and
     * a short name method call. ALL manager instances should
     * be referenced through the Service and not instantiated
     * directly.
     * ***************************************************/

    public BillingManager getBillingManager() {
        return billingManager;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">
    /* ***************************************************
     * S E R V I C E   V A L I D A T I O N   M E T H O D S
     *
     * This section is for all validation methods so that
     * other services can check blind id values are legitimate
     * and any associated business rules are adhere to.
     * ***************************************************/

    /**
     * Validation Method to check pure id values exist. This method ONLY checks an ID exists in a manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch(validationType) {
            case BILLING:
                return billingManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    /**
     * Allows the verification of priceList values by returning the calculated value a specific chargeSd on
     * a ywd. If the childEducationTimetableId is less than 0 then the child education price reduction is taken off
     * the value.
     *
     * @param ywd
     * @param priceGroupMr
     * @param chargeSd
     * @param childEducationTimetableId
     * @param priceListType
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public long verifyTotalValue(int ywd, int priceGroupMr, int chargeSd, int childEducationTimetableId, MultiRefEnum priceListType) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        PriceConfigService priceConfigService = PriceConfigService.getInstance();
        TimetableService timetableService = TimetableService.getInstance();

        long rtnValue;
        // get the session charge
        TariffBean priceTariff = priceConfigService.getPriceTariff(ywd, priceGroupMr);
        rtnValue = priceConfigService.getPriceList(priceTariff.getTariffId(), priceListType).getPeriodSdSum(chargeSd);
        // adjust for education reductions
        long educationReduction = 0L;
        if(childEducationTimetableId > 0) {
            for(int educationSd : timetableService.getChildEducationTimetableManager().getObject(childEducationTimetableId).getAllEducationSd()) {
                int billedEducationSd = SDHolder.intersectSD(educationSd, chargeSd);
                if(billedEducationSd < 1) {
                    continue;
                }
                educationReduction +=  priceConfigService.getChildEducationPriceReduction(ywd, childEducationTimetableId).getPeriodSdSum(billedEducationSd);
            }
        }
        // just for safty of negative values reverse the educationReduction and add it
        rtnValue += (educationReduction * -1);
        //
        return rtnValue ;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Level Get Methods">
    /* ***************************************************
     * S E R V I C E  L E V E L   G E T   M E T H O D S
     *
     * This section is for all get methods that require
     * information outside their own data store. All other
     * get methods can be found up the tree in the manager
     * or bean of the bean type.
     * ***************************************************/

    /**
     * Core Method for obtaining the predicted billing before invoice. All billings are created on the fly till
     * they are invoiced so as to refect mistakes and changes to charging before invoice.
     *
     * @param accountId
     * @param booking
     * @param includeArgs
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     */
    public List<BillingBean> getPredictedBillingsForBooking(int accountId, BookingBean booking, VABoolean... includeArgs) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException, MaxCountReachedException, NullObjectException {
        List<BillingBean> rtnList = new LinkedList<>();
        // always add the session charge
        BillingBean session = createBookingSessionCharge(accountId, booking, ObjectBean.SYSTEM_OWNED);
        if(session != null) {
            rtnList.add(session);
            //include any other options
            if(VABoolean.INCLUDE_EDUCATION.isIn(includeArgs)) {
                CollectionUtils.addIgnoreNull(rtnList, createBookingEducationReduction(accountId, booking, ObjectBean.SYSTEM_OWNED));
            }
            if(VABoolean.INCLUDE_ADJUSTMENTS.isIn(includeArgs)) {
                rtnList.addAll(createBookingPriceAdjustments(accountId, booking, ObjectBean.SYSTEM_OWNED));
            }
            // the booking discount
            CollectionUtils.addIgnoreNull(rtnList, createBookingDiscount(accountId, booking, rtnList, ObjectBean.SYSTEM_OWNED));
        }
        if(VABoolean.INCLUDE_LOYALTY.isIn(includeArgs)) {
            rtnList.addAll(createBookingLoyaltyDiscounts(accountId, booking, ObjectBean.SYSTEM_OWNED));
        }
        return rtnList;
    }

    /**
     * Returns a List of BillingBean objects that relate to InvoiceLineDTO's for an invoice. This method call is
     * different from getPredictedBillingsForAllAccounts() in that it extends to the inclusion of related BillingBeans of previous
     * invoices if they are from the same booking. For example if a booking has already been
     * Pre-charged invoiced on a different invoice id then reconciled on the requested preview, the billings returned
     * for this preview would also include the original pre-charge billings from the previous invoice.
     *
     * Note that if an associated billing for a booking has a ywd after the lastYwd it will be ignored.
     *
     * @param accountId
     * @param lastYwd
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public List<BillingBean> getPredictedLineBillingsForAccount(int accountId, int lastYwd) throws PersistenceException, IllegalValueException, MaxCountReachedException, NoSuchKeyException, IllegalActionException, NullObjectException, NoSuchIdentifierException  {
        List<BillingBean> rtnList = new LinkedList<>();
        Set<Integer> bookingIdSet = new ConcurrentSkipListSet<>();
        // keep this separate so as to add them on to the end of the list
        List<BillingBean> predictedList = this.getPredictedBillingsForAccount(accountId, lastYwd);
        // get all the unique BookingIds that are associated with these predicted billings
        for(BillingBean billing : predictedList) {
            bookingIdSet.add(billing.getBookingId());
        }
        // run through each of the bookings and add all the previous billings
        for(Integer bookingId : bookingIdSet) {
            for(BillingBean invoicedBilling : billingManager.getAllBillingForBooking(accountId, bookingId, BTIdBits.TYPE_CHARGE)) {
                if(invoicedBilling.getInvoiceId() > 0) {
                    rtnList.add(invoicedBilling);
                }
            }
            for(BillingBean invoicedBilling : billingManager.getAllBillingForBookingLoyalty(accountId, bookingId, BTIdBits.TYPE_CHARGE)) {
                if(invoicedBilling.getInvoiceId() > 0) {
                    rtnList.add(invoicedBilling);
                }
            }
        }
        // now add on the predicted
        rtnList.addAll(predictedList);
        return rtnList;
    }

    /**
     * Helper Method to retrieve all BillingBean objects that are associated with a single invoiceId. The results
     * can be filtered on an optional BillingEnum list whereby ANY BillingEnum included in the list that match
     * the BillingBean billingBits will be included. If no BillingEnum values are given then all BillingBean objects will be returned.
     *
     * @param accountId
     * @param invoiceId
     * @param anyBillingArgs
     * @return list of BillingBean objects
     * @throws PersistenceException
     */
    public List<BillingBean> getBillingsForInvoice(int accountId, int invoiceId, BillingEnum... anyBillingArgs) throws PersistenceException {
        LinkedList<BillingBean> rtnList = new LinkedList<>();
        rtnList.addAll(billingManager.getAllBillingForInvoice(accountId, invoiceId, anyBillingArgs));
        Collections.sort(rtnList, new YwdComparator());
        return (rtnList);
    }

    /**
     * Returns a List of BillingBean objects that relate to InvoiceLineDTO's for an invoice. This method call is
     * different from getBillingsForInvoice() in that it extends to the inclusion of related BillingBeans of different
     *  invoices if they are from the same booking or loyalty reference. For example if a booking has already been
     *  Pre-charged invoiced on a different invoice id then reconciled on the requested invoice Id, the billings returned
     *  for this invoiceId would also include the original pre-charge billings from the previous invoice.
     *
     *  The invoice is taken as the latest invoice and any billings created after the invoice date are ignored.
     *
     * @param accountId the account id of the invoice
     * @param invoiceId the invoiceId of interest
     * @param anyBillingArgs a comma separated or array of filtered BillingEnum
     * @return a list of BillingBean objects
     * @throws PersistenceException
     */
    public List<BillingBean> getLineBillingsForInvoice(int accountId, int invoiceId, BillingEnum... anyBillingArgs) throws PersistenceException {
        TransactionService transactionService = TransactionService.getInstance();

        List<BillingBean> rtnList = new LinkedList<>();
        // run through all the billings for this invoice and look for related billings
        for(BillingBean billing : billingManager.getAllBillingForInvoice(accountId, invoiceId)) {
            // if there is a bookingId then we need to get all the billings for that id
            if(billing.hasAnyBillingBit(GROUP_BOOKING, GROUP_LOYALTY)) {
                // get all the billing for the booking
                List<BillingBean> billingList = billingManager.getAllBillingForBooking(accountId, billing.getBookingId(), BTIdBits.TYPE_CHARGE);
                // get all the loyalties where the booking is the trigger
                billingList.addAll(billingManager.getAllBillingForBookingLoyalty(accountId, billing.getBookingId(), BTIdBits.TYPE_CHARGE));
                for(BillingBean bookingBilling : billingList) {
                    // continue if filtering on BillingEnum and does not exists.
                    if(anyBillingArgs.length > 0 && !bookingBilling.hasAnyBillingBit(anyBillingArgs)) {
                        continue;
                    }
                    // if the billing invoice is before the bookingBilling invoice then reject (also rejects -1 invoices)
                    if(!transactionService.isInvoiceBefore(accountId, bookingBilling.getInvoiceId(), invoiceId)) {
                        continue;
                    }
                    //only add if we don't already have it
                    if(!rtnList.contains(bookingBilling)) {
                        rtnList.add(bookingBilling);
                    }
                }
            } else {
                // continue if filtering on BillingEnum and does not exists.
                if(anyBillingArgs.length > 0 && !billing.hasAnyBillingBit(anyBillingArgs)) {
                    continue;
                }
                //if there is no bookingId then just add it as must be fixed Item or loyalty
                rtnList.add(billing);
            }
        }
        return (rtnList);
    }

    /**
     * Gets a list of billing beans (booking charges and fixed charges) for the period specified. Will use "real"
     * confirmed bookings if the period required is within the confirmed period, then uses predicted bookings based on
     * booking requests. Only uses fixed charges for manual items on the account. Takes into account any previous
     * invoicing where applicable.
     *
     * @param accountId
     * @param lastYwd
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public List<BillingBean> getPredictedBillingsForAccount(int accountId, int lastYwd) throws PersistenceException, IllegalValueException, MaxCountReachedException, NoSuchKeyException, IllegalActionException, NullObjectException, NoSuchIdentifierException {
        ChildBookingService bookingService = ChildBookingService.getInstance();
        PropertiesService propertiesService = PropertiesService.getInstance();

        List<BillingBean> rtnList = new LinkedList<>();
        int firstInvoiceYwd = propertiesService.getSystemProperties().getFirstInvoiceYwd();
        // get all the pre charge bookings and make them into billing
        for(BookingBean booking : bookingService.setInvoiceRestrictions(lastYwd, accountId)) {
            // don't include if before the first invoice ywd
            if(booking.getYwd() < firstInvoiceYwd){
                continue;
            }
            rtnList.addAll(this.getPredictedBillingsForBooking(accountId, booking, VABoolean.INCLUDE_EDUCATION, VABoolean.INCLUDE_ADJUSTMENTS, VABoolean.INCLUDE_LOYALTY));
        }
        // add the fixed items that need to be specially modelled
        for(BillingBean billing : this.getPredictedFixedChargeBilling(accountId, lastYwd)) {
            // don't include if before the first invoice ywd
            if(billing.getYwd() < firstInvoiceYwd){
                continue;
            }
            rtnList.add(billing);
        }
        return rtnList;
    }

    /**
     * Gets a list of billing beans (booking charges and fixed charges) for the period specified for the entire nursery.
     *
     * Will use "real" confirmed bookings if the period required is within the confirmed period, then uses predicted
     * bookings based on booking requests. Only uses fixed charges for manual items on accounts. Takes into account any
     * previous invoicing where applicable.
     *
     * @param accountIds
     * @param lastYwd
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public List<BillingBean> getPredictedBillingsForAllAccounts(Set<Integer> accountIds, int lastYwd) throws PersistenceException, IllegalValueException, MaxCountReachedException, NoSuchKeyException, IllegalActionException, NullObjectException, NoSuchIdentifierException {
        List<BillingBean> rtnList = new LinkedList<>();
        for(int accountId : accountIds) {
            rtnList.addAll(this.getPredictedBillingsForAccount(accountId, lastYwd));
        }
        return rtnList;
    }


    /**
     * Gets a list of billing beans for fixed charges ONLY up to the end of the period specified.
     * The billings returned are cloned BillingBean objects.
     *
     * @param accountId
     * @param lastYwd
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     */
    public List<BillingBean> getPredictedFixedChargeBilling(int accountId, int lastYwd) throws PersistenceException, NoSuchIdentifierException, MaxCountReachedException, NullObjectException {
        List<BillingBean> rtnList = new LinkedList<>();
        // clone all the fixed item billings
        for(BillingBean billing : billingManager.getAllBillingOutstanding(accountId, lastYwd, BillingEnum.TYPE_FIXED_ITEM)) {
            // Keep things tidy and make sure there is no discount for a predicted fixed item
            if(billing.getDiscountId() != -1) {
                int discountId = billing.getDiscountId();
                billingManager.setObjectDiscountId(accountId, billing.getBillingId(), -1, ObjectBean.SYSTEM_OWNED);
                billingManager.removeObject(accountId, discountId);
            }
            BillingBean cloneBilling = billingManager.cloneObjectBean(billing.getBillingId(), billing);
            // if this comes back with null then no discount to apply
            BillingBean discount = this.createFixedChargeDiscount(accountId, cloneBilling, ObjectBean.SYSTEM_OWNED);
            if(discount != null) {
                BillingManager.setObjectDiscountId(cloneBilling, discount.getBillingId(), ObjectBean.SYSTEM_OWNED);
                rtnList.add(discount);
            } else {
                BillingManager.setObjectDiscountId(cloneBilling, -1, ObjectBean.SYSTEM_OWNED);
            }
            rtnList.add(0,cloneBilling);
        }
        return rtnList;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Level Set Methods">
    /* **************************************************
     * S E R V I C E   L E V E L   S E T   M E T H O D S
     *
     * All set methods should be interfaced at the Service
     * level as this allows consistency across the API and
     * security for the underlying sets. If we have sets at
     * the Service level, the Manager level and ObjectStore,
     * the inconsistency might cause a set to be made to the
     * manager when a set in the service adds additional logic
     * and thus is bypassed.
     * **************************************************/


    /**
     * Sets a fixed item for either a profile or an account.
     * If the profileId is -1 then an account fixed item is taken else a profile fixed item is created.
     * If a discount is applicable, this will not be created until the fixed item is invoiced and the
     * discount rate at that time, taken.
     * NOTE: this method can not be used for predicted billing as a real billing is added to the manager.
     *
     * The value passed MUST be positive.
     * The billType determines how this item is treated
     *
     *
     * @param billingId
     * @param ywd
     * @param accountId
     * @param profileId
     * @param value
     * @param chargeOrCredit BILL_CHARGE (when fixed item discount will be applied) OR BILL_CREDIT (when NO discount will be applied)
     * @param description
     * @param notes
     * @param owner
     * @param nonStandardBillingArgs other billingBits that are different from a standard fixed item.
     * @return The newly created fixed item
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws MaxCountReachedException
     * @throws IllegalValueException
     */
    public BillingBean setFixedItem(int billingId, int ywd, int accountId, int profileId, long value, BillingEnum chargeOrCredit, String description, String notes, String owner, BillingEnum... nonStandardBillingArgs) throws NullObjectException, PersistenceException, NoSuchIdentifierException, MaxCountReachedException, IllegalValueException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildService  childService = ChildService.getInstance();
        // even though only one billing is created, a List is returned to fit in with other method calls so isEmpty() can be used
        // ensure the BillingEnum is correct
        if(!chargeOrCredit.equals(BILL_CHARGE) && !chargeOrCredit.equals(BILL_CREDIT)) {
            throw new IllegalValueException("The billType passed must be either BILL_CREDIT or BILL_CHARGE");
        }
        if(ywd < propertiesService.getSystemProperties().getFirstInvoiceYwd()) {
            throw new IllegalValueException("ChargeYwdBeforeFirstInvoiceYwd");
        }
        if(!childService.isId(accountId, VT.ACCOUNT)) {
            throw new NoSuchIdentifierException("The account id '" + accountId + "' does not exist");
        }
        if(profileId > 0 && !childService.isId(profileId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The profileId '" + profileId + "' does not exist");
        }
        if(value < 0) {
            throw new IllegalValueException("The value passed must be zero or greater");
        }
        if(value == 0) {
            // return nothing
            return null;
        }
        //set as charge and reset if credit
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);
        if(chargeOrCredit.equals(BILL_CREDIT)) {
            billingBits = BillingEnum.resetBillingBits(billingBits, BILL_CREDIT, APPLY_NO_DISCOUNT);
        }
        // reset any billing bits that are different from the standard
        if(nonStandardBillingArgs.length > 0) {
            billingBits = resetBillingBits(billingBits, nonStandardBillingArgs);
            // for safty hard code the type, charge and group
            billingBits = resetBillingBits(billingBits, TYPE_FIXED_ITEM, chargeOrCredit, GROUP_FIXED_ITEM);
        }
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        int id = billingId == -1 ? billingManager.generateIdentifier(1, 5000): billingId;
        return billingManager.setObject(accountId, new BillingBean(id, accountId, ywd, -1, value, taxRate, -1, profileId, -1, billingBits, -1, description, notes, -1, -1, owner));
    }

    /**
     * Sets all the billings for a booking. As a billing is only set when you create an invoice,
     * the invoiceId must be passed to be set in the billing.
     *
     * @param accountId
     * @param booking
     * @param invoiceId
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws DuplicateIdentifierException
     * @throws IllegalActionException
     */
    public List<BillingBean> setBillingForBooking(int accountId, BookingBean booking, int invoiceId, String owner) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException, MaxCountReachedException, NullObjectException, DuplicateIdentifierException, IllegalActionException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        List<BillingBean> rtnList = new LinkedList<>();
        if(booking.getYwd() < propertiesService.getSystemProperties().getFirstInvoiceYwd()) {
            return rtnList;
        }
        // always add the session charge
        BillingBean session = createBookingSessionCharge(accountId, booking, owner);
        if(session != null) {
            rtnList.add(session);
            // funded
            CollectionUtils.addIgnoreNull(rtnList, createBookingEducationReduction(accountId, booking, ObjectBean.SYSTEM_OWNED));
            // adjustment
            rtnList.addAll(createBookingPriceAdjustments(accountId, booking, ObjectBean.SYSTEM_OWNED));
            // discount
            CollectionUtils.addIgnoreNull(rtnList, createBookingDiscount(accountId, booking, rtnList, ObjectBean.SYSTEM_OWNED));
        }
        //loyalty
        rtnList.addAll(createBookingLoyaltyDiscounts(accountId, booking, ObjectBean.SYSTEM_OWNED));
        // save the billing list
        for(BillingBean billing : rtnList) {
            // this is just to ensure we don't overwrite an existing billing.
            if(billingManager.isIdentifier(accountId, billing.getBillingId())) {
                throw new DuplicateIdentifierException("The newly created billing id " + billing.getBillingId() + " has been recycled and reallocated");
            }
            // writes the billing to persistence
            billingManager.setObject(accountId, billing);
            // set the invoiceId
            billingManager.setObjectInvoiceId(accountId, billing.getBillingId(), invoiceId, owner);
        }
        // set the booking reconciled (only set if is authorised)
        bookingService.setBookingReconciled(booking.getYwd(), booking.getBookingId(), owner);
        return rtnList;
    }

    /**
     * BillingBean set method to set the invoiceId of the specified billing. by setting this
     * you are linking a billing to an invoice. Both the accountId and invoiceId must exist
     *
     * @param accountId
     * @param billingId
     * @param invoiceId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setBillingInvoiceId(int accountId, int billingId, int invoiceId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        if(!ChildService.getInstance().isId(accountId, VT.ACCOUNT)) {
            throw new NoSuchIdentifierException("The account id '" + accountId + "' does not exist");
        }
        billingManager.setObjectInvoiceId(accountId, billingId, invoiceId, owner);
    }

    /**
     * Remove billing allows a billing to be removed from an account. This has a number of restrictions:
     * - At this time ONLY fixed item types can be removed
     * - Removing a fixed item will also remove any associated discounts
     * - you can't remove a billing that has been allocated to an invoice.
     *
     * @param accountId
     * @param billingId
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     */
    public void removeBilling(int accountId, int billingId) throws PersistenceException, IllegalActionException, NoSuchIdentifierException {
        // at this time you are ONLY allowed to remove fixed items
        if(billingManager.isIdentifier(accountId, billingId)) {
            if(!billingManager.getObject(accountId, billingId).hasBillingBits(TYPE_FIXED_ITEM)) {
                throw new IllegalActionException("The billingId '" + billingId + "' is not a fixed item or has been reconciled. Only non reconciled fixed items can be removed");
            }
            billingManager.removeObject(accountId, billingId);
        }
    }

    /**
     * Removes all billings relation to bookings that are not yet invoiced. This has a number of restrictions:
     * - The billing must be part of a booking billing, e.g. not a fixed item
     * - you can't remove a billing that has been allocated to an invoice.
     *
     * @param accountId
     * @throws PersistenceException
     */
    public void removeAllOustandingBillings(int accountId) throws PersistenceException {
        for(BillingBean billing : billingManager.getAllBillingOutstanding(accountId, YWDHolder.MAX_YWD)) {
            // don't include fixed items
            if(billing.hasBillingBits(TYPE_FIXED_ITEM)) {
                continue;
            }
            billingManager.removeObject(accountId, billing.getBillingId());
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private Class Methods">
    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     *
     * This section is for all private methods for the
     * class. Private methods should be carefully used
     * so as to avoid multi jump calls and as a general
     * rule of thumb, only when multiple methods use a
     * common algorithm.
     * ***************************************************/

    /*
     * Deals with creating a session charge
     */
    protected BillingBean createBookingSessionCharge(int accountId, BookingBean booking, String owner) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, IllegalValueException, MaxCountReachedException, NullObjectException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        PriceConfigService priceConfigService = PriceConfigService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        // standard billing bits for session billing
        int billingBits = BillingEnum.getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING);
        int chargeSd = this.getCheckedSd(booking.getSpanSd(), booking.getActualSd());
        // create the multiRef and check the chargeType is chargeable
        MultiRefEnum chargeMr = getMultiRefFromBookingType(booking.getBookingTypeId(), MultiRefEnum.PRICE);
        // get the charge Value from the pricelist
        long signedBillingValue = 0L;
        if(!chargeMr.equals(MultiRefEnum.NO_VALUE)) {
            signedBillingValue = priceConfigService.getPriceListForProfile(booking.getYwd(), booking.getRoomId(), booking.getProfileId(), chargeMr).getPeriodSdSum(chargeSd);
            // adjust it according to the booking type charge value
            double chargePercent = bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId()).getChargePercent();
            signedBillingValue *= chargePercent/100;
        }
        //get the value of any previous session charges for this booking
        long signedPreviousBillingValue = billingManager.getSignedTotalValue(accountId, booking.getBookingId(), -1, TYPE_SESSION);
        // take any previous values away from the original values.
        signedBillingValue += (signedPreviousBillingValue * -1);
        // check the BILL enum and set accordingly
        BillingEnum billEnum = signedBillingValue < 0 ? BILL_CREDIT : BILL_CHARGE;
        billingBits = BillingEnum.resetBillingBits(billingBits, billEnum);
        // set the value to a positive
        long billingValue = Math.abs(signedBillingValue);
        /*
         * If the SESSION has a previously billing (has been precharged) and has not yet been
         * AUTHORISED (ready for reconciliation) and the billing type IMMEDIATE_RECALC_FLAG
         * and the action IMMEDIATE_RECALC_BIT is not set the SESSION billing should be ignored
         * until reconcilation.
         */
        if( signedPreviousBillingValue != 0
            && booking.isBeforeState(BookingState.AUTHORISED)
            && !bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.IMMEDIATE_RECALC_FLAG)
            && !booking.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT)) {
                return null;
        }
        // create the billing
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        // change the description if not reconciled booking
        String description = ApplicationConstants.SESSION_RECONCILED;
        if(booking.isBeforeState(BookingState.AUTHORISED)) {
            description = ApplicationConstants.SESSION_ESTIMATE;
        }
        // we need to check we are not just generating lots of zero billings after reconciliation
        if(booking.isEqualOrAfterState(BookingState.RECONCILED) && billingValue == 0) {
            return null;
        }
        int billingId = billingManager.generateIdentifier(1, 5000);
        return(new BillingBean(billingId, accountId, booking.getYwd(), chargeSd, billingValue, taxRate, booking.getBookingId(), booking.getProfileId(), booking.getBookingTypeChargeBit(), billingBits, -1, description, "", -1, -1, owner));
    }

    /*
     * Deals with creating a child education reduction
     */
    protected BillingBean createBookingEducationReduction(int accountId, BookingBean booking, String owner) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, IllegalValueException, MaxCountReachedException, NullObjectException {
        TimetableService timetableService = TimetableService.getInstance();
        PriceConfigService priceConfigService = PriceConfigService.getInstance();
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();
        // NOTE: this is APPLY_DISCOUNT so that child education reverses out any SESSION discounts.
        int billingBits = BillingEnum.getBillingBits(TYPE_FUNDED, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        int chargeSd = this.getCheckedSd(booking.getSpanSd(), booking.getActualSd());
        long signedPriceReduction = 0L;
        // Calculate to see if there is any child education reduction
        ChildEducationTimetableBean childEducationTimetable = timetableService.getChildEducationTimetable(booking.getProfileId(), booking.getYwd());
        if(childEducationTimetable.getCetId() != ChildEducationTimetableManager.NO_EDUCATION && BTIdBits.isNotBits(booking.getBookingTypeId(), BTBits.NO_CHARGE_BIT)) {
            // get the price reduction list
            PriceListBean priceReductionList = priceConfigService.getChildEducationPriceReduction(booking.getYwd(), childEducationTimetable.getCetId());
            int billedEducationSd;
            long unsignedPriceReduction = 0L;
            // for each periodSd in the education session periodSds, work out the part that the booking uses (if any)
            for(int educationSd : childEducationTimetable.getAllEducationSd()) {
                billedEducationSd = SDHolder.intersectSD(educationSd, chargeSd);
                if(billedEducationSd < 1) {
                    continue;
                }
                unsignedPriceReduction += priceReductionList.getPeriodSdSum(billedEducationSd);
            }
            // change the sign to a negative value as this is a reduction
            signedPriceReduction = unsignedPriceReduction * -1;
        }
        // get any previous funded values for this booking
        long signedPreviousPriceReduction = billingManager.getSignedTotalValue(accountId, booking.getBookingId(), -1, TYPE_FUNDED);
        // take add the reverse of the previous value to the value.
        signedPriceReduction += (signedPreviousPriceReduction * -1);
        // check the BILL enum and set accordingly
        BillingEnum billEnum = signedPriceReduction < 0 ? BILL_CREDIT : BILL_CHARGE;
        billingBits = BillingEnum.resetBillingBits(billingBits, billEnum);
        // set the value to positive
        long billingValue = Math.abs(signedPriceReduction);
        /*
         * If the billing value is zero there is no reduction charges or the outstanding
         * balance is zero so we don't return a reduction billing
         */
        if(billingValue == 0) {
            return null;
        }
        /*
         * If the billing is an estimate and we have already generated a precharge billing,
         * indicated by the previous value not being zero, would would not create another precharge
         * unless the BookingType flag IMMEDIATE_RECALC_FLAG is set and the action IMMEDIATE_RECALC_BIT
         * is not setand and the new billing has a value that is not zero. In this case we generate another precharge.
         */
        if(booking.isBeforeState(BookingState.AUTHORISED) && signedPreviousPriceReduction != 0) {
            if(!bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.IMMEDIATE_RECALC_FLAG)
                 && !booking.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT)) {
                return null;
            }
        }
        // create the billing
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        // change the description if not Authorised
        String description = ApplicationConstants.EDUCATION_REDUCTION_RECONCILED;
        if(booking.isBeforeState(BookingState.AUTHORISED)) {
            description = ApplicationConstants.EDUCATION_REDUCTION_ESTIMATE;
        }
        int billingId = billingManager.generateIdentifier(1, 5000);
        return(new BillingBean(billingId, accountId, booking.getYwd(), chargeSd, billingValue, taxRate, booking.getBookingId(), booking.getProfileId(), booking.getBookingTypeChargeBit(), billingBits, -1, description, "", -1, -1, owner));

    }

    /*
     * Deals with creating a discount associated with a booking.
     */
    protected BillingBean createBookingDiscount(int accountId, BookingBean booking, List<BillingBean> predictedList, String owner) throws PersistenceException, NoSuchIdentifierException, MaxCountReachedException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildService childService = ChildService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        // Get the total value of all the predicted billings where discount is applied
        long totalPredictedValue = 0L;
        if(!BTIdBits.isFilter(booking.getBookingTypeId(), BTBits.NO_CHARGE_BIT)) {
            if(predictedList != null) {
                for(BillingBean billing : predictedList) {
                    if(billing == null || billing.hasBillingBits(APPLY_NO_DISCOUNT)) {
                        continue;
                    }
                    totalPredictedValue += billing.getSignedValue();
                }
            }
        }
        // even though the discount rate could be 0 we still need to get it incase of change
        int discountRate = childService.getChildManager().getObject(booking.getProfileId()).getBookingDiscountRate();
        // now get any previous discounts
        long signedPreviousDiscountValue = billingManager.getSignedTotalValue(accountId, booking.getBookingId(), -1, TYPE_BOOKING_CHILD_DISCOUNT, TYPE_ADJUSTMENT_CHILD_DISCOUNT);
        /*
         * a zero value discount based on the predicted value is a special case. This is because the discount is a calculation
         * on the total billings that have already been adjusted. e.g a second session billing value of 0 means that there is no
         * change from the first billing not that the booking has a session value of 0. Therefore when calculating the discount
         * based on the total predicted value the discount value could be zero because:
         * 1. the reconciled booking type changed to NO_CHARGE (signedPreviousDiscountValue reverse sign)
         * 2. the reconciled discount rate changed to zero (signedPreviousDiscountValue reverse sign, discount value calculates to zero)
         * 3. the reconciled billings reflect no change to the booking (return null)
         * 2. the credits and debits balance to zero (return null)
         * 3. the discount rate is zero (signedPreviousDiscountValue reverse sign)
         */
        if(signedPreviousDiscountValue != 0) {
            if(BTIdBits.isFilter(booking.getBookingTypeId(), BTBits.NO_CHARGE_BIT) || discountRate == 0) {
                signedPreviousDiscountValue *= -1;
            } else if(totalPredictedValue == 0) {
                return null;
            }
        }
        // the discount is reversed to give it the proper sign (Note the * -1)
        long signedDiscountValue = TDCalc.getDiscountValue(totalPredictedValue, discountRate) * -1;
        // add the discounts because both resutls are discounts on adjustment valuses not the true values of the booking
        signedDiscountValue += signedPreviousDiscountValue;
        // set up the billingbits as a BILL_CHARGE to make resetting logic easier
        int discountBits = BillingEnum.getBillingBits(TYPE_BOOKING_CHILD_DISCOUNT, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_BOOKING);
        // check the BILL enum and set accordingly
        BillingEnum billEnum = signedDiscountValue < 0 ? BILL_CREDIT : BILL_CHARGE;
        discountBits = BillingEnum.resetBillingBits(discountBits, billEnum);
        // set the value to positive
        long billingValue = Math.abs(signedDiscountValue);
        /*
         * If the billing value is zero there is no reduction charges or the outstanding
         * balance is zero so we don't return a reduction billing
         */
        if(billingValue == 0) {
            return null;
        }
        /*
         * If the billing is an estimate and we have already generated a precharge billing,
         * indicated by the previous value not being zero, would would not create another precharge
         * unless the BookingType flag IMMEDIATE_RECALC_FLAG is set and the action IMMEDIATE_RECALC_BIT
         * is not set and the new billing has a value that is not zero. In this case we generate another precharge.
         */
        if(booking.isBeforeState(BookingState.AUTHORISED) && signedPreviousDiscountValue != 0) {
            if(!bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.IMMEDIATE_RECALC_FLAG)
                 && !booking.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT)) {
                return null;
            }
        }
        // create the billing
        int chargeSd = this.getCheckedSd(booking.getSpanSd(), booking.getActualSd());
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        int billingId = billingManager.generateIdentifier(1, 5000);
        String description = ApplicationConstants.BOOKING_CHILD_DISCOUNT;
        if(billingValue > 0) {
            return(new BillingBean(billingId, accountId, booking.getYwd(), chargeSd, billingValue, taxRate, booking.getBookingId(), booking.getProfileId(), booking.getBookingTypeChargeBit(), discountBits, -1, description, "", -1, -1, owner));
        }
        return null;
    }

    /*
     * Deals with any additional charges generated from the booking
     */
    protected List<BillingBean> createBookingPriceAdjustments(int accountId, BookingBean booking, String owner) throws IllegalValueException, PersistenceException, NoSuchIdentifierException, NoSuchKeyException, MaxCountReachedException, NullObjectException {
        PriceConfigService configService = PriceConfigService.getInstance();

        List<BillingBean> rtnList = new LinkedList<>();
        int bookingActualSd = this.getBookingActuals(booking)[0];
        // create the multiRef and check the chargeType is chargeable
        MultiRefEnum chargeMr = getMultiRefFromBookingType(booking.getBookingTypeId(), MultiRefEnum.ADJUSTMENT);
        // create an empty adjustment list and only populate if there is a charge type
        List<PriceAdjustmentBean> adjList = new LinkedList<>();
        if(!chargeMr.equals(MultiRefEnum.NO_VALUE)) {
            adjList = configService.getAllPriceAdjustmentForProfile(booking.getYwd(), booking.getRoomId(), booking.getProfileId(), chargeMr);
        }
        // get all the adjustments against this booking
        for(PriceAdjustmentBean adjustment : adjList) {
            // check the bookingType and ensure it is ATTENDING
            if(BTBits.isBits(booking.getBookingTypeId(), BTBits.ATTENDING_BIT) || adjustment.hasBillingBit(TYPE_ADJUSTMENT_ON_ALL)) {
                int adjChargeSd = 0;
                if(adjustment.hasBillingBit(TYPE_EARLY_DROPOFF)) {
                    // get the drop off range
                    adjChargeSd = SDHolder.splitSD(bookingActualSd, booking.getBookingStart())[0];
                    /*
                     * if the actual is one minute before then the split will be duration zero with the
                     * start one minute before.we therefor need to add one to the duration to charge
                     * for that minute. if the return is -1 then by adding one will still work as zero is
                     * not charged
                     */
                    adjChargeSd++;
                } else if(adjustment.hasBillingBit(TYPE_LATE_PICKUP)) {
                    // get the pick up range
                    adjChargeSd = SDHolder.splitSD(bookingActualSd, booking.getBookingEnd())[1];
                } else if(adjustment.hasBillingBit(TYPE_ADJUSTMENT_ON_ATTENDING) || adjustment.hasBillingBit(TYPE_ADJUSTMENT_ON_ALL)) {
                    // is the chargeSd
                    adjChargeSd = this.getBookingActuals(booking)[1];
                }
                /*
                 * If the late pick up is the same end as the bookingSd it will return a valid SD with
                 * a duration of zero instead of -1, therfore test the duration has a value before charging.
                 */
                if(SDHolder.getDuration(adjChargeSd) > 0) {
                    BillingBean billing = this.createPriceAdjustmentBilling(accountId, adjustment, adjChargeSd, booking, owner);
                    if(billing != null) {
                        rtnList.add(billing);
                    }
                }
            }
        }
        // create a set of all the current booking adjustments
        Set<Integer> adjustmentIdSet = new ConcurrentSkipListSet<>();
        for(PriceAdjustmentBean adjustment : adjList) {
            adjustmentIdSet.add(adjustment.getPriceAdjustmentId());
        }
        // if we have billings where there is no adjustmentId then we need to reverse it out
        for(BillingBean billing : billingManager.getAllBillingsWithAdjustmentIdNotFound(accountId, booking.getBookingId(), adjustmentIdSet)) {
            int billingBits = BillingEnum.resetBillingBits(billing.getBillingBits(), APPLY_NO_DISCOUNT, (billing.hasBillingBits(BILL_CREDIT) ? BILL_CHARGE : BILL_CREDIT));
            int billingId = billingManager.generateIdentifier(1, 5000);
            rtnList.add(new BillingBean(billingId, accountId, billing.getYwd(), billing.getBillingSd(), billing.getValue(), billing.getTaxRate(), billing.getBookingId(), billing.getProfileId(), billing.getBtChargeBit(), billingBits, -1, billing.getDescription(), "", -1, billing.getAdjustmentId(), owner));
        }
        return rtnList;
    }

    /*
     * Internal common algorithm method to convert a price adjustment into a billing
     */
    protected BillingBean createPriceAdjustmentBilling(int accountId, PriceAdjustmentBean adjustment, int bookingRangeSd, BookingBean booking, String owner) throws IllegalValueException, PersistenceException, NoSuchIdentifierException, NoSuchKeyException, MaxCountReachedException, NullObjectException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        PriceConfigService configService = PriceConfigService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        // We don'tcheck the booking is chargable as if it is null it might be a change to NO_CHARGE
        MultiRefEnum chargeMr = getMultiRefFromBookingType(booking.getBookingTypeId(), MultiRefEnum.ADJUSTMENT);
        // RANGE can't be RANGE_SUM_TOTAL for PriceAdjustment as based on single day values
        if(adjustment.hasBillingBit(RANGE_SUM_TOTAL)) {
            throw new IllegalValueException("The BillingBit RANGE_SUM_TOTAL can not be used in a PriceAdjustment");
        }
        // initialise the adjustment value
        long signedAdjustmentValue = 0L;
        // find the intersect of the adjustment rangeSd and the chargeSd
        int adjChargeSd = adjustment.hasBillingBit(RANGE_IGNORED)? bookingRangeSd : SDHolder.intersectSD(adjustment.getRangeSd(), bookingRangeSd);
        // if the charge type is NO_VALUE ignore but carry on because it might be a cancel
        if(!chargeMr.equals(MultiRefEnum.NO_VALUE)) {
            // sort out if the adjustment applies
            if((adjustment.hasBillingBit(RANGE_AT_LEAST) && SDBits.match(SDBits.AT_LEAST, SDHolder.compare(adjustment.getRangeSd(), bookingRangeSd)))
                    || (adjustment.hasBillingBit(RANGE_AT_MOST) && SDBits.match(SDBits.AT_MOST, SDHolder.compare(adjustment.getRangeSd(), bookingRangeSd)))
                    || (adjustment.hasBillingBit(RANGE_SOME_PART) && SDBits.match(SDBits.SOME_PART, SDHolder.compare(adjustment.getRangeSd(), bookingRangeSd)))
                    || (adjustment.hasBillingBit(RANGE_EQUAL) && adjustment.getRangeSd() == bookingRangeSd)
                    || (adjustment.hasBillingBit(RANGE_IGNORED))) {

                // -1 indicates there is no intersect
                if(SDHolder.getDuration(adjChargeSd) < 1) {
                    return null;
                }
                long unsignedAdjustmentValue = 0L;
                if(adjustment.hasBillingBit(CALC_AS_PERCENT)) {
                     // to calculate the bill value get the value of the period...
                    long periodValue = configService.getPriceListForProfile(booking.getYwd(), booking.getRoomId(), booking.getProfileId(), chargeMr).getPeriodSdSum(adjChargeSd);
                    // ... get the raw bill value using the adjustment value as the percentage discount of the period value ...
                    long billValueRaw = TDCalc.getDiscountValue(periodValue, (int) adjustment.getValue());
                    // ... then apply the precision to get the true bill value.
                    unsignedAdjustmentValue = TDCalc.getPrecisionValue(billValueRaw, adjustment.getPrecision());
                } else if(adjustment.hasBillingBit(CALC_AS_VALUE)) {
                    // the adjustment value is the bill value but could be repeated
                    int remainingDuration = SDHolder.getDuration(adjChargeSd);
                    while(remainingDuration > 0) {
                        // add the value to the bill value
                        unsignedAdjustmentValue += adjustment.getValue();
                        // either reduce the remainder to 0 or, if repeated, reduce by duration
                        remainingDuration = adjustment.isRepeated() ? (remainingDuration - adjustment.getRepeatDuration()) : 0;
                    }
                }
                // correct the sign according to the BILL enum type
                signedAdjustmentValue = BillingEnum.hasBillingBit(adjustment.getBillingBits(), BILL_CHARGE) ?  unsignedAdjustmentValue : unsignedAdjustmentValue * -1;
            }
        }
        // get any previous billing adjustments
        long signedPreviousAdjustmentValue = billingManager.getSignedTotalValue(accountId, booking.getBookingId(), adjustment.getPriceAdjustmentId(), TYPE_ADJUSTMENT_ON_ALL, TYPE_ADJUSTMENT_ON_ATTENDING);
        // reverse the the previous sign and add to avoid negative sign issues
        signedAdjustmentValue += (signedPreviousAdjustmentValue * -1);
        int billingBits = adjustment.getBillingBits();
        // make sure we have the correct BILL enum type.
        BillingEnum billEnum = signedAdjustmentValue < 0 ? BILL_CREDIT : BILL_CHARGE;
        billingBits = BillingEnum.resetBillingBits(billingBits, billEnum);
        // revert it back to a positive bill value
        long billingValue = Math.abs(signedAdjustmentValue);
        /*
         * If the billing value is zero there is no adjustment charges or the outstanding
         * balance is zero so we don't return an adjustment billing
         */
        if(billingValue == 0) {
            return null;
        }
        /*
         * If the billing is an estimate and we have already generated a precharge billing,
         * indicated by the signedPreviousAdjustmentValue not being zero, would would not create another precharge
         * unless the BookingType flag IMMEDIATE_RECALC_FLAG is set and the action IMMEDIATE_RECALC_BIT is not set
         * and the new billing has a value that is not zero. In this case we generate another precharge.
         */
        if(booking.isBeforeState(BookingState.AUTHORISED) && signedPreviousAdjustmentValue != 0) {
            if(!bookingService.getBookingTypeManager().getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.IMMEDIATE_RECALC_FLAG)
                 && !booking.isActionOn(ActionBits.IMMEDIATE_RECALC_BIT)) {
                return null;
            }
        }
        // create the billingId
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        String description = adjustment.getName();
        int billingId = billingManager.generateIdentifier(1, 5000);
        return(new BillingBean(billingId, accountId, booking.getYwd(), adjChargeSd, billingValue, taxRate, booking.getBookingId(), booking.getProfileId(), booking.getBookingTypeChargeBit(), billingBits, -1, description, "", -1, adjustment.getPriceAdjustmentId(), owner));
    }

    /*
     * deals with creating a discount associated with a fixed item billing.
     */
    protected BillingBean createFixedChargeDiscount(int accountId, BillingBean fixedBilling, String owner) throws PersistenceException, PersistenceException, NoSuchIdentifierException, MaxCountReachedException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildService childService = ChildService.getInstance();

        //if there is no discount to be applied then return null;
        if(fixedBilling.hasBillingBits(APPLY_NO_DISCOUNT)) {
            return null;
        }
        int discountBits = BillingEnum.getBillingBits(BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);
        String discountDesc;
        int discountRate;
        // find out if it is an account or child billing
        if(fixedBilling.getProfileId() > 0) {
            discountBits += BillingEnum.getBillingBits(TYPE_FIXED_CHILD_DISCOUNT, GROUP_FIXED_ITEM);
            discountDesc = ApplicationConstants.FIXED_CHILD_DISCOUNT;
            discountRate = childService.getChildManager().getObject(fixedBilling.getProfileId()).getFixedItemDiscountRate();
        } else {
            discountBits += BillingEnum.getBillingBits(TYPE_FIXED_ACCOUNT_DISCOUNT, GROUP_FIXED_ITEM);
            discountDesc = ApplicationConstants.FIXED_ACCOUNT_DISCOUNT;
            discountRate = childService.getAccountManager().getObject(fixedBilling.getAccountId()).getFixedItemDiscountRate();
        }
        // check the original charge and reverse the discount
        if(fixedBilling.hasBillingBits(BILL_CREDIT)) {
            // because we are using signed values we know this must be a credit
            discountBits = BillingEnum.resetBillingBits(discountBits, BILL_CHARGE);
        }
        // get the discount value
        long billingValue = TDCalc.getDiscountValue(fixedBilling.getValue(), discountRate);
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        int billingId = billingManager.generateIdentifier(1, 5000);
        if(billingValue > 0) {
            return(new BillingBean(billingId, accountId, fixedBilling.getYwd(), -1, billingValue, taxRate, -1, fixedBilling.getProfileId(), fixedBilling.getBtChargeBit(), discountBits, -1, discountDesc, "", -1, -1, owner));
        }
        return null;
    }

    /*
     * Internal method: Generates a list of discount billingBean objects. You pass through the current session billing
     * to ensure it is included in the calculations.
     */
    protected List<BillingBean> createBookingLoyaltyDiscounts(int accountId, BookingBean booking, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalValueException, NoSuchKeyException, NullObjectException, MaxCountReachedException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        PriceConfigService priceConfigService = PriceConfigService.getInstance();

        List<BillingBean> rtnList = new LinkedList<>();
        List<LoyaltyDiscountBean> loyaltyList = new LinkedList<>();
        // sort out the chargeMr for finding the loyalty discounts for this profile from the Tariff
        MultiRefEnum chargeMr = getMultiRefFromBookingType(booking.getBookingTypeId(), MultiRefEnum.LOYALTY);
        // if the booking is NO_VALUE we have to check this wasn't origionally a trigger booking that has now changed
        if(chargeMr.equals(MultiRefEnum.NO_VALUE)) {
            // run through all the loyalties for this booking ywd and check this was not a trigger
            for(BillingBean loyaltyBilling : billingManager.getYwdBillingForProfile(accountId, booking.getYwd(), booking.getProfileId(), BTIdBits.TYPE_CHARGE, TYPE_LOYALTY)) {
                //check the trigger bookingId
                if(loyaltyBilling.getBookingId() == booking.getBookingId()) {
                    // get the original loyalty
                    LoyaltyDiscountBean originalLoylaty = priceConfigService.getLoyaltyDiscountManager().getObject(loyaltyBilling.getAdjustmentId());
                    // we need to create a fake loylaty to reverse out the old
                    long discount = 0;
                    loyaltyList.add(new LoyaltyDiscountBean(originalLoylaty.getLoyaltyDiscountId(), originalLoylaty.getName(), originalLoylaty.getBillingBits(), discount, originalLoylaty.getStart(), originalLoylaty.getDuration(), originalLoylaty.getPriorityDays(), owner));
                }
            }
        } else {
            // get all the loyalty discounts on this day, for this child, of the right charge type
            loyaltyList = priceConfigService.getAllLoyaltyDiscountForProfile(booking.getYwd(), booking.getRoomId(), booking.getProfileId(), chargeMr);
        }
        // see if any of the loyalties are on their
        for(LoyaltyDiscountBean loyalty : loyaltyList) {
            // Only when the loyalty is on the last priority day is it a candidate for loyalty discount
            if(!this.isPriorityDayLast(loyalty.getPriorityDays(), YWDHolder.getDay(booking.getYwd()))) {
                continue;
            }
            int billingBits = loyalty.getBillingBits();
            // calculates the 'reconciled' values adding or subtracting any other related loyalty
            long[] signedValues = calculateLoyaltyDiscountValue(accountId, loyalty, booking);
            long signedBillingValue = signedValues[0];
            long signedDiscountValue = signedValues[1];
            int taxRate = propertiesService.getSystemProperties().getTaxRate();
            // create the billingId early so it is higher than the discountId (no significance other than store order)
            int billingId = billingManager.generateIdentifier(1, 5000);
            int discountId = -1;
            /*
             * Create the discount (note the discount is a charge.)
             */
            if(signedDiscountValue != 0) {
                int discountBits = BillingEnum.getBillingBits(TYPE_LOYALTY_CHILD_DISCOUNT, BILL_CHARGE, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_LOYALTY);
                String discountDesc = ApplicationConstants.LOYALTY_CHILD_DISCOUNT;
                if(signedDiscountValue < 0) {
                    discountBits = BillingEnum.resetBillingBits(discountBits, BILL_CREDIT);
                }
                // remove any signs
                long discountValue = Math.abs(signedDiscountValue);
                discountId = billingManager.generateIdentifier(1, 5000);
                rtnList.add(new BillingBean(discountId, accountId, booking.getYwd(), -1, discountValue, taxRate, booking.getBookingId(), booking.getProfileId(), booking.getBookingTypeChargeBit(), discountBits, -1, discountDesc, "", -1, loyalty.getLoyaltyDiscountId(), ObjectBean.SYSTEM_OWNED));
            }
            /*
             * Create the Billing
             */
            if(signedBillingValue > 0) {
                billingBits = BillingEnum.resetBillingBits(billingBits, BILL_CHARGE);
            }
            // remove any signs
            long billingValue = Math.abs(signedBillingValue);

            // only add a billing if there are values to be added
            if(signedDiscountValue != 0 || billingValue != 0) {
                int triggerBookingId = booking.getBookingId();
                String description = loyalty.getName();
                //for clarity of the defaults
                int billingSd = -1;
                int invoiceId = -1;
                // set the loyalty
                rtnList.add(0,
                    new BillingBean(billingId, accountId, booking.getYwd(), billingSd, billingValue, taxRate, triggerBookingId,
                        booking.getProfileId(),  booking.getBookingTypeChargeBit(), billingBits, invoiceId, description,
                        "", discountId, loyalty.getLoyaltyDiscountId(), owner));
            }

        }
        // create a set of all the current booking loyaltyIds
        Set<Integer> loyaltyIdSet = new ConcurrentSkipListSet<>();
        for(LoyaltyDiscountBean loyalty : loyaltyList) {
            loyaltyIdSet.add(loyalty.getLoyaltyDiscountId());
        }
        // if we have billings where there is no loyalty then we need to reverse it out
        for(BillingBean billing : billingManager.getAllBillingsWithLoyaltyIdNotFound(accountId, booking.getBookingId(), loyaltyIdSet)) {
            int billingBits = BillingEnum.resetBillingBits(billing.getBillingBits(), APPLY_NO_DISCOUNT, (billing.hasBillingBits(BILL_CREDIT) ? BILL_CHARGE : BILL_CREDIT));
            int billingId = billingManager.generateIdentifier(1, 5000);
            rtnList.add(new BillingBean(billingId, accountId, billing.getYwd(), billing.getBillingSd(), billing.getValue(), billing.getTaxRate(), billing.getBookingId(), billing.getProfileId(), billing.getBtChargeBit(), billingBits, -1, billing.getDescription(), "", -1, billing.getAdjustmentId(), owner));
        }
        return rtnList;
    }

    /*
     * Internal common algorithm method to convert a loyalty discount into a billing.
     */
    protected long[] calculateLoyaltyDiscountValue(int accountId, LoyaltyDiscountBean loyalty, BookingBean booking) throws NullObjectException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        if(booking.isState(BookingState.ARCHIVED)) {
            throw new IllegalValueException("The booking " + booking.getBookingId() + " is an ARCHIVED booking and can't be calculated");
        }
        // return array of { billValue, discountValue }
        final int SIGNED_VALUE_INDEX = 0;
        final int SIGNED_DISCOUNT_INDEX = 1;
        final int SIGNED_CALC_VALUE_INDEX = 2;
        final int SIGNED_CALC_DISCOUNT_INDEX = 3;
        long[] rtnArray =  { 0, 0, 0, 0 };

        /*
         * To check if the loyalty discount can be applied, the success criteria needs to be assessed.
         * To do this we need to run through the whole week the booking falls within and associate this
         * booking and all related bookings or billings to the loyalty discount attributes.
         */
        long totalChargeValue = this.calculateLoyaltyDiscountSuccessCriteria(accountId, loyalty, booking);
        // if a value of 0 or more is returned then the success critaria was met and the value can be calculated
        if(totalChargeValue > 0) {
            // apply the loylaty discount
            long billingValue = loyalty.getDiscount();
            // if the discount is a percentage then take the percentage value
            if(loyalty.hasBillingBit(CALC_AS_PERCENT)) {
                billingValue = TDCalc.getDiscountValue(totalChargeValue, (int) loyalty.getDiscount());
            }
            // as this is a CREDIT change the value to a negative
            rtnArray[SIGNED_VALUE_INDEX] = billingValue * -1;
            rtnArray[SIGNED_CALC_VALUE_INDEX] = rtnArray[SIGNED_VALUE_INDEX];
            // get the discount for the billing if applicable
            if(loyalty.hasBillingBit(APPLY_DISCOUNT)) {
                // get the billing discount using the billingValue so we don't have to reverse the sign
                int discountRate = ChildService.getInstance().getChildManager().getObject(booking.getProfileId()).getBookingDiscountRate();
                rtnArray[SIGNED_DISCOUNT_INDEX] = TDCalc.getDiscountValue(billingValue, discountRate);
                rtnArray[SIGNED_CALC_DISCOUNT_INDEX] = rtnArray[SIGNED_DISCOUNT_INDEX];
            }
        }
        /*
         * now we have the value we need to ensure we have not charged this previously and are about to charge again
         */
        // get all the existing billing for this loyalty discount
        List<BillingBean> chkList = billingManager.getAllBillingForBookingLoyalty(accountId, booking.getBookingId(), BTIdBits.TYPE_CHARGE, TYPE_LOYALTY);
        if(!chkList.isEmpty()) {
            long signedPreviousBillingValue = 0;
            long signedPreviousDiscountValue = 0;
            // go through all the previous billings and then make the adjustments
            for(BillingBean billing : chkList) {
                // throw out any that are not for this loyalty
                if(billing.getAdjustmentId() != loyalty.getLoyaltyDiscountId()) {
                    continue;
                }
                // add/subtract the bill value of previous billing
                signedPreviousBillingValue += billing.getSignedValueArray()[BillingBean.VALUE_ONLY];
                //if this has a discount then get the discount value
                if(billing.getDiscountId() > 0) {
                    signedPreviousDiscountValue += billingManager.getObject(accountId, billing.getDiscountId()).getSignedValueArray()[BillingBean.VALUE_ONLY];
                }
            }
            /*
             * any of the previous billings need to be reflected in the previous loyalty but
             * we have to be careful with signing. To make sure we correctly deal with previous values
             * we must reverse the sign then add
             */
            rtnArray[SIGNED_VALUE_INDEX] += (signedPreviousBillingValue * -1);
            rtnArray[SIGNED_DISCOUNT_INDEX] += (signedPreviousDiscountValue * -1);
        }
        return rtnArray;
    }

    /*
     * helper method see if a loyalty should be applied and if so the sum value of duration and billing or estimted value.
     */
    protected long calculateLoyaltyDiscountSuccessCriteria(int accountId, LoyaltyDiscountBean loyalty, BookingBean booking) throws PersistenceException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildService childService = ChildService.getInstance();

        // get the week this booking is within
        int yw0 = YWDHolder.getYW(booking.getYwd());

        long durationSum = 0;
        long totalValue = 0;
        boolean atLeastDuration = false;
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            int ywd = YWDHolder.add(yw0, day);
            // check the booking is on the priority day
            if(!loyalty.getPriorityDays()[YWDHolder.getDay(ywd)]) {
                continue;
            }
            /*
             * If this is an Education (Funded) day then by default we don't want to apply any loyalty
             * discount as the customer already is paying a reduced ammount. But in certain circumstances
             * the nursery might want to still give the discount. So we need to check if this is an Education
             * (Funded) day for the child and if so is the system property LoyaltyApplyedToEducationDays set to
             * true.
             */
            if(!propertiesService.getSystemProperties().isLoyaltyApplyedToEducationDays() && childService.isDayChildEducationTimetable(booking.getProfileId(), ywd)) {
                continue;
            }
            /*
             * In a day there might be more than one booking (if not authorised) or billing (if reconciled)
             * so we need to find the earliest start, the sum of the durations and finally the value
             * of the billings or the estimated value of the bookings taken from the pricelist.
             */
            long[] dayTotals = this.calculateLoyaltyDiscountDayBookingBillingTotals(accountId, ywd, booking);
            int dayStart = (int) dayTotals[0];
            int spanDuration = (int) dayTotals[1];
            totalValue += dayTotals[2];
            // there should only be RANGE_SUM_TOTAL and RANGE_AT_LEAST
            if(loyalty.hasBillingBit(RANGE_AT_LEAST)) {
                // if there were no billing then the criteria can't be fulfilled
                if(spanDuration < 1) {
                    atLeastDuration = false;
                    break;
                }
                // check the optional start
                if(loyalty.getStart() < 0 || loyalty.getStart() <= dayStart) {
                    // as long as it stays true we carry on
                    if(spanDuration >= loyalty.getDuration()) {
                        atLeastDuration = true;
                    } else {
                        atLeastDuration = false;
                        break;
                    }
                }
            } else {
                durationSum += spanDuration;
            }
        }
        // if we are looking at a sum total this can be checked here
        if(loyalty.hasBillingBit(RANGE_SUM_TOTAL)) {
            atLeastDuration = durationSum >= loyalty.getDuration();
        }
        // if the criteria was met then return the value.
        if(atLeastDuration) {
            return totalValue;
        }
        return -1;
    }

    /*
     * helper method to retrieve the start, duration and totals of all bookings or billings in a day
     * - The session BillingBean is a newly generated billing not yet saved so won't be in the manager
     * - The session represents a reconciled billing so is the trigger for the check
     *
     * return startTime, spanDuration, value
     */
    protected long[] calculateLoyaltyDiscountDayBookingBillingTotals(int accountId, int ywd, BookingBean booking) throws PersistenceException, IllegalValueException, NoSuchIdentifierException, NoSuchKeyException {
        // constants for ease of reading
        final short DAY_START_INDEX = 0;
        final short DURATION_SUM_INDEX = 1;
        final short TOTAL_VALUE_INDEX = 2;
        // types must match so return all as long
        long[] rtnArray = new long[3];
        // initialise the return values of start to -1 as 0 is a valid start
        rtnArray[DAY_START_INDEX] = -1;
        // checker for booking is trigger
        int lastDayStart = -1;
        // just incase the booking is null
        if(booking == null) {
            return rtnArray;
        }

        // booking is live so is an ESTIMATE from the live bookings
        PropertiesService propertiesService = PropertiesService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();
        BookingForecastService forecastService = BookingForecastService.getInstance();
        PriceConfigService priceConfigService = PriceConfigService.getInstance();

        // get the confirmed weeks to determ getting live or request bookings
        int confirmedWeeks = propertiesService.getSystemProperties().getConfirmedPeriodWeeks();

        List<BookingBean> ywdEstimateBookingList;
        if(ywd > CalendarStatic.getRelativeYW(confirmedWeeks)) {
            ywdEstimateBookingList = forecastService.getYwdBookingFromRequest(ywd, accountId, booking.getProfileId());
        } else {
            int bookingTypeMask = BTIdBits.setIdentifier(booking.getBookingTypeChargeBit(), BTBits.ATTENDING_BIT);
            ywdEstimateBookingList = bookingService.getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, booking.getProfileId(), bookingTypeMask, BTFlagIdBits.TYPE_FLAG);
        }
        for(BookingBean chkBooking : ywdEstimateBookingList) {
            rtnArray[DURATION_SUM_INDEX] += SDHolder.getDuration(chkBooking.getSpanSd());
            int start = SDHolder.getStart(chkBooking.getSpanSd());
            if(rtnArray[DAY_START_INDEX] == -1 || rtnArray[DAY_START_INDEX] > start) {
                rtnArray[DAY_START_INDEX] = start;
            }
            if(lastDayStart < start) {
                lastDayStart = start;
            }
            /*
             * retrieve the price list for the ywd, room and profile so, for the purposes of percentage values
             * we have the value of the booking to calculate the loyalty discount percentage value
             */
            MultiRefEnum chargeMr = getMultiRefFromBookingType(chkBooking.getBookingTypeChargeBit(), MultiRefEnum.PRICE);
            if(!chargeMr.equals(MultiRefEnum.NO_VALUE)) {
                rtnArray[TOTAL_VALUE_INDEX] += priceConfigService.getPriceListForProfile(chkBooking.getYwd(), chkBooking.getRoomId(), chkBooking.getProfileId(), chargeMr).getPeriodSdSum(chkBooking.getSpanSd());
            }
        }

        // the trigger booking is the last in the day so check this is true
        if(lastDayStart > SDHolder.getStart(booking.getSpanSd())) {
            // rest the defaults
            rtnArray[DAY_START_INDEX] = -1;
            rtnArray[DURATION_SUM_INDEX] = 0;
            rtnArray[TOTAL_VALUE_INDEX] = 0;
        }
        // we have to match the type so return all as long
        return rtnArray;
    }

    /************************
     * Private Utility Methods
     ***********************/

    /*
     * finds how many days there are that are true in a priorityDays array
     */
    protected int getPriorityDayCount(boolean[] priorityDays) {
        return Collections.frequency(Arrays.asList(ArrayUtils.toObject(priorityDays)), true);
    }

    /*
     * finds how many days there are that are true in a priorityDays array
     */
    protected boolean isPriorityDayLast(boolean[] priorityDays, int day) {
        if(ArrayUtils.lastIndexOf(priorityDays, true) == day) {
            return true;
        }
        return false;
    }

    /*
     * Internal common algorithm method to provide the bookingActuals. The return is an array [bookingActualSd]
     * [spanActualSd]
     */
    protected int[] getBookingActuals(BookingBean booking) throws NoSuchIdentifierException, PersistenceException {
        int[] rtnArray = new int[2];
        rtnArray[0] = getCheckedSd(booking.getBookingSd(), booking.getActualSd());
        rtnArray[1] = getCheckedSd(booking.getSpanSd(), booking.getActualSd());

        return rtnArray;
    }

    /*
     * Takes a periodSd and compares it to an actualSd returning a corrected periodSd
     */
    protected int getCheckedSd(int periodSd, int actualSd) throws NoSuchIdentifierException, PersistenceException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        SystemPropertiesBean properties = propertiesService.getSystemProperties();

        if(actualSd == -1) {
            return periodSd;
        }
        int periodStart = SDHolder.getStart(periodSd);
        int periodEnd = SDHolder.getEnd(periodSd);
        int actualStart = SDHolder.getStart(actualSd);
        int actualEnd = SDHolder.getEnd(actualSd);

        // tidy the actuals to the period
        int trueStart = actualSd == ObjectEnum.INITIALISATION.value()
                || actualStart > periodStart ? periodStart : actualStart;
        int trueEnd = actualEnd < periodEnd ? periodEnd : actualEnd;


        // set the chargeSd to be the maximum charge span
        int rtnStart = trueStart;
        int rtnEnd = trueEnd;

        // if there is a charge margin then check for adjustments
        if(properties.isChargeMargin()) {
            // work out the correct chargeSd taking into account the charge margin
            rtnStart = (periodStart - properties.getDropOffChargeMargin()) > trueStart ? trueStart : periodStart;
            rtnEnd = (periodEnd + properties.getPickupChargeMargin()) < trueEnd ? trueEnd : periodEnd;
        }
        // return the chargeSd by building the charge start and end
        return SDHolder.buildSD(rtnStart, rtnEnd);
    }

    /*
     * Internal common algorithm method to map bookingType to MultiRef
     */
    public static MultiRefEnum getMultiRefFromBookingType(int bookingTypeId, MultiRefEnum level) throws IllegalValueException {
        if(BTBits.isBits(bookingTypeId, BTBits.NO_CHARGE_BIT)) {
            return MultiRefEnum.NO_VALUE;
        }
        if(BTBits.isBits(bookingTypeId, BTBits.SPECIAL_CHARGE_BIT | BTBits.STANDARD_CHARGE_BIT)) {
            switch(level) {
                case PRICE:
                    return (BTBits.isBits(bookingTypeId, BTBits.STANDARD_CHARGE_BIT)
                            ? MultiRefEnum.PRICE_STANDARD : MultiRefEnum.PRICE_SPECIAL);
                case ADJUSTMENT:
                    return (BTBits.isBits(bookingTypeId, BTBits.STANDARD_CHARGE_BIT)
                            ? MultiRefEnum.ADJUSTMENT_STANDARD : MultiRefEnum.ADJUSTMENT_SPECIAL);
                case LOYALTY:
                    return (BTBits.isBits(bookingTypeId, BTBits.STANDARD_CHARGE_BIT)
                            ? MultiRefEnum.LOYALTY_STANDARD : MultiRefEnum.LOYALTY_SPECIAL);
                case FIXED:
                    return (MultiRefEnum.NO_VALUE);
            }
        }
        // don't recognise the charge type
        throw new IllegalValueException("The BookingTypeId does not contain a recognised MultiRefEnum: "
                + BTBits.getAllStrings(bookingTypeId).toString());
    }

    //</editor-fold>
}
