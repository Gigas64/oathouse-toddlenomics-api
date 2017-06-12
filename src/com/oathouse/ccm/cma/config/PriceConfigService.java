/*
 * @(#)PriceConfigService.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.finance.*;
import static com.oathouse.ccm.cos.config.finance.MultiRefEnum.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.MRHolder;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code PriceConfigService} Class is a encapsulated API, service level singleton that provides
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
 * @version 1.00 Feb 20, 2012
 */
public class PriceConfigService {
    // Singleton Instance
    private volatile static PriceConfigService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final AgeStartManager ageStartManager = new AgeStartManager(VT.AGE_START.manager(), ObjectDataOptionsEnum.ARCHIVE);

    // a priceConfig that holds price lists
    private final PriceListManager priceListManager = new PriceListManager(VT.PRICE_LIST.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // a priceConfig that holds price adjustments
    private final PriceAdjustmentManager priceAdjustmentManager = new PriceAdjustmentManager(VT.PRICE_ADJUSTMENT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // relates a tariffId to one or more priceConfigMr values (tariffId -> priceConfigMr)
    private final TariffRelationManager priceTariffRelationship = new TariffRelationManager(VT.PRICE_TARIFF_RELATION.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // a reference class for grouping multiple priceConfig objects
    private final TariffManager priceTariffManager = new TariffManager(VT.PRICE_TARIFF.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // Scheduling for tariff retalting to PriceList and PriceAdjustments (priceGroupMr -> tariffId)
    private final DayRangeManager priceTariffRange = new DayRangeManager(VT.PRICE_TARIFF_RANGE.manager(), ObjectDataOptionsEnum.ARCHIVE);

    // a priceConfig that holds the loyaltyDiscount
    private final LoyaltyDiscountManager loyaltyDiscountManager = new LoyaltyDiscountManager(VT.LOYALTY_DISCOUNT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // a reference class for grouping multiple priceConfig objects
    private final TariffManager loyaltyTariffManager = new TariffManager(VT.LOYALTY_TARIFF.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // relates a tariffId to one or more priceConfigMr values (tariffId -> priceConfigMr)
    private final TariffRelationManager loyaltyTariffRelationship = new TariffRelationManager(VT.LOYALTY_TARIFF_RELATION.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // Scheduling for tariff retalting to loyaltyDiscount (priceGroupMr -> tariffId)
    private final DayRangeManager loyaltyTariffRange = new DayRangeManager(VT.LOYALTY_TARIFF_RANGE.manager(), ObjectDataOptionsEnum.ARCHIVE);

    // Child education definition and scheduling
    private final PriceListManager childEducationPriceReductionManager = new PriceListManager(VT.CHILD_EDUCATION_PRICE_REDUCTION.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final DayRangeManager childEducationPriceReductionRange = new DayRangeManager(VT.CHILD_EDUCATION_PRICE_REDUCTION_RANGE.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private PriceConfigService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code PriceConfigService} class
     * @return instance of the {@code PriceConfigService}
     * @throws PersistenceException
     */
    public static PriceConfigService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (PriceConfigService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new PriceConfigService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code PriceConfigService} class has been initialised. This is used
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
     * initialise all the managed classes in the {@code PriceConfigService}. The
     * method returns an instance of the {@code PriceConfigService} so it can be chained.
     * This must be called before a the {@code PriceConfigService} is used. e.g  {@code PriceConfigService myChargingService = }
     * @return instance of the {@code PriceConfigService}
     * @throws PersistenceException
     */
    public synchronized PriceConfigService init() throws PersistenceException {
        if(initialise) {
            ageStartManager.init();

            // setup dayRange with all the leftRef elements in place
            AgeRoomService rs = AgeRoomService.getInstance();
            TimetableService ts = TimetableService.getInstance();
            // make up priceListRange's mixed set of leftRef keys
            Set<Integer> allLeftRef = new ConcurrentSkipListSet<>();
            for(int roomId : rs.getRoomConfigManager().getAllIdentifier()) {
                allLeftRef.add(MRHolder.getMR(MultiRefEnum.ROOM.type(), roomId));
            }
            for(int ageStartId : ageStartManager.getAllIdentifier()) {
                allLeftRef.add(MRHolder.getMR(MultiRefEnum.AGE.type(), ageStartId));
            }
            priceTariffRange.init(allLeftRef);
            loyaltyTariffRange.init(allLeftRef);

            priceTariffManager.init();
            priceTariffRelationship.init();
            priceAdjustmentManager.init();
            priceListManager.init();

            loyaltyTariffManager.init();
            loyaltyTariffRelationship.init();
            loyaltyDiscountManager.init();

            childEducationPriceReductionRange.init(ts.getChildEducationTimetableManager().getAllIdentifier());
            childEducationPriceReductionManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code PriceConfigService}. The
     * method returns an instance of the {@code PriceConfigService} so it can be chained.
     * @return instance of the {@code PriceConfigService}
     * @throws PersistenceException
     */
    public PriceConfigService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code PriceConfigService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = ageStartManager.clear() ? success : false;

        success = priceListManager.clear() ? success : false;
        success = priceAdjustmentManager.clear()?success:false;
        success = priceTariffManager.clear() ? success : false;
        success = priceTariffRelationship.clear() ? success : false;
        success = priceTariffRange.clear() ? success : false;

        success = loyaltyDiscountManager.clear()?success:false;
        success = loyaltyTariffManager.clear() ? success : false;
        success = loyaltyTariffRelationship.clear() ? success : false;
        success = loyaltyTariffRange.clear() ? success : false;


        success = childEducationPriceReductionRange.clear() ? success : false;
        success = childEducationPriceReductionManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code PriceConfigService} class instance by setting the INSTANCE reference
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

    public AgeStartManager getAgeStartManager() {
        return ageStartManager;
    }

    public PriceListManager getChildEducationPriceReductionManager() {
        return childEducationPriceReductionManager;
    }

    public DayRangeManager getChildEducationPriceReductionRangeManager() {
        return childEducationPriceReductionRange;
    }

    public LoyaltyDiscountManager getLoyaltyDiscountManager() {
        return loyaltyDiscountManager;
    }

    public TariffManager getLoyaltyTariffManager() {
        return loyaltyTariffManager;
    }

    public DayRangeManager getLoyaltyTariffRangeManager() {
        return loyaltyTariffRange;
    }

    public PriceAdjustmentManager getPriceAdjustmentManager() {
        return priceAdjustmentManager;
    }

    public PriceListManager getPriceListManager() {
        return priceListManager;
    }

    public TariffManager getPriceTariffManager() {
        return priceTariffManager;
    }

    public DayRangeManager getPriceTariffRangeManager() {
        return priceTariffRange;
    }

    protected TariffRelationManager getPriceTariffRelationship() {
        return priceTariffRelationship;
    }

    protected TariffRelationManager getLoyaltyTariffRelationship() {
        return loyaltyTariffRelationship;
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
     * Validation Method to check pure id values exist. This method ONLY checks an ID
     * exists in a manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch(validationType) {
            case AGE_START:
                return ageStartManager.isIdentifier(id);
            case PRICE_TARIFF:
                return priceTariffManager.isIdentifier(id);
            case LOYALTY_TARIFF:
                return loyaltyTariffManager.isIdentifier(id);
            case PRICE_LIST:
                return priceListManager.isIdentifier(id);
            case PRICE_ADJUSTMENT:
                return priceAdjustmentManager.isIdentifier(id);
            case LOYALTY_DISCOUNT:
                return loyaltyDiscountManager.isIdentifier(id);
            case CHILD_EDUCATION_PRICE_REDUCTION:
                return childEducationPriceReductionManager.isIdentifier(id);
            default:
                throw new UnsupportedOperationException(validationType.toString() + " is not supported in this method");
        }
    }

    /**
     * Validation Method to check a generic price configuration id with a MultiRefEnum and determine
     * if it exists in the appropriate manager and if so if the billingBits of the underlying bean,
     * if they exist, are of the correct type for the MultiRefEnum.
     *
     * @param priceConfigId a price configuration id (priceList, priceAdjustment, loyaltyDiscount)
     * @param priceConfigEnum an associated MultiRefEnum
     * @return true if found in the manager, false if not found
     * @throws PersistenceException
     */
    public boolean isPriceConfigValid(int priceConfigId, MultiRefEnum priceConfigEnum) throws PersistenceException {
        // priceList doesn't have billingBits so don't need to check
        if(priceConfigEnum.isPriceList() && priceListManager.isIdentifier(priceConfigId)) {
            return true;
        }
        try {
            if(priceConfigEnum.isAdjustment()) {
                PriceAdjustmentBean adjustment = priceAdjustmentManager.getObject(priceConfigId);
                // TYPE_FIXED_ITEM can only be MultuRefEnum of PRICE_ADJ CHILD or ACCOUNT
                if(adjustment.hasBillingBit(BillingEnum.TYPE_FIXED_ITEM)) {
                    if(priceConfigEnum.isPriceAdjustment()) {
                        return false;
                    }
                }
                return true;
            } else if(priceConfigEnum.isLoyaltyDiscount()) {
                // loyalty discount should only ever be of TYPE_LOYALTY and BILL_CREDIT
                LoyaltyDiscountBean loyalty = loyaltyDiscountManager.getObject(priceConfigId);
                return loyalty.hasBillingBit(BillingEnum.TYPE_LOYALTY, BillingEnum.BILL_CREDIT);
            } else {
                // not a recognised priceConfigEnum so return false
                return false;
            }
        } catch(NoSuchIdentifierException nsie) {
            // if no identifer is found then it is incompatable
            return false;
        }
    }

    /**
     * Validation Method to check a generic price configuration id with a MultiRefEnum and determine if it exists in the
     * appropriate manager.
     *
     * @param priceConfigId a price configuration id (priceList, priceAdjustment, loyaltyDiscount)
     * @param priceConfigEnum an associated MultiRefEnum
     * @return true if found in the manager, false if not found
     * @throws PersistenceException
     */
    public boolean isPriceConfig(int priceConfigId, MultiRefEnum priceConfigEnum) throws PersistenceException {
        if(priceConfigEnum.isPriceList()) {
            return priceListManager.isIdentifier(priceConfigId);
        }
        if(priceConfigEnum.isAdjustment()) {
            return priceAdjustmentManager.isIdentifier(priceConfigId);
        }
        if(priceConfigEnum.isLoyaltyDiscount()) {
            return loyaltyDiscountManager.isIdentifier(priceConfigId);
        }
        return false;
    }

    /**
     * Determines whether a Price Tariff is currently scheduled in a DayRange and thus in use and not removable.
     *
     * @param tariffId the tariff identifier
     * @return true if the tariff is removable (e.g. not in any schedule)
     * @throws PersistenceException
     */
    public boolean isPriceTariffRemovable(int tariffId) throws PersistenceException {
        if(DayRangeManager.inDayRange(tariffId, priceTariffRange)) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether a Loyalty Tariff is currently scheduled in a DayRange and thus in use and not removable.
     *
     * @param tariffId the tariff identifier
     * @return true if the tariff is removable (e.g. not in any schedule)
     * @throws PersistenceException
     */
    public boolean isLoyaltyTariffRemovable(int tariffId) throws PersistenceException {
        if(DayRangeManager.inDayRange(tariffId, loyaltyTariffRange)) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether the price tariff has a price list or price adjustment assigned (otherwise it would be daft to
     * schedule it)
     *
     * @param tariffId
     * @return
     * @throws PersistenceException
     */
    public boolean isPriceTariffReadyForScheduling(int tariffId) throws PersistenceException {
        return !priceTariffRelationship.getAllObjects(tariffId).isEmpty();
    }

    /**
     * Determines whether any of the price tariffs are ready for scheduling
     *
     * @return
     * @throws PersistenceException
     */
    public boolean isPriceTariffReadyForScheduling() throws PersistenceException {
        for(TariffBean tariff : priceTariffManager.getAllObjects()) {
            if(this.isPriceTariffReadyForScheduling(tariff.getTariffId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the loyalty tariff has a price list or price adjustment assigned
     * (otherwise it would be daft to schedule it)
     * @param tariffId
     * @return
     * @throws PersistenceException
     */
    public boolean isLoyaltyTariffReadyForScheduling(int tariffId) throws PersistenceException {
        return !loyaltyTariffRelationship.getAllObjects(tariffId).isEmpty();
    }

    /**
     * Determines whether any of the loyalty tariffs are ready for scheduling
     * @return
     * @throws PersistenceException
     */
    public boolean isLoyaltyTariffReadyForScheduling() throws PersistenceException {
        for(TariffBean tariff : loyaltyTariffManager.getAllObjects()) {
            if(this.isLoyaltyTariffReadyForScheduling(tariff.getTariffId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether any price list identifier is not associated with a price tariff
     * and thus able to be removed.
     *
     * @param priceListId the reference value of the price list to be checked
     * @return true if the object is removable
     * @throws PersistenceException
     * @see MultiRefEnum
     */
    public boolean isPriceListRemovable(int priceListId) throws PersistenceException {
        if(priceTariffRelationship.isIdentifier(MRHolder.getMR(PRICE_SPECIAL.type(), priceListId))
                || priceTariffRelationship.isIdentifier(MRHolder.getMR(PRICE_STANDARD.type(), priceListId))){
            return false;
        }
        return true;
    }

    /**
     * Determines whether a price configuration identifier is not associated with a price tariff
     * and thus able to be removed. A price configuration is PriceList or PriceAdjustment
     *
     * @param priceAdjustmentId the reference value of the price adjustment to be checked
     * @return true if the object is removable
     * @throws PersistenceException
     * @see MultiRefEnum
     */
    public boolean isPriceAdjustmentRemovable(int priceAdjustmentId) throws PersistenceException {
        if(priceTariffRelationship.isIdentifier(MRHolder.getMR(ADJUSTMENT_SPECIAL.type(), priceAdjustmentId))
                || priceTariffRelationship.isIdentifier(MRHolder.getMR(ADJUSTMENT_STANDARD.type(), priceAdjustmentId))
                || priceTariffRelationship.isIdentifier(MRHolder.getMR(FIXED_ACCOUNT.type(), priceAdjustmentId))
                || priceTariffRelationship.isIdentifier(MRHolder.getMR(FIXED_CHILD.type(), priceAdjustmentId))) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether a loyalty discount is not being used as part of a LoyaltyTariff
     *
     * @param loyaltyDiscountId
     * @return
     * @throws PersistenceException
     */
    public boolean isLoyaltyDiscountRemovable(int loyaltyDiscountId) throws PersistenceException {
        if(loyaltyTariffRelationship.isIdentifier(MRHolder.getMR(LOYALTY_SPECIAL.type(), loyaltyDiscountId))
                || loyaltyTariffRelationship.isIdentifier(MRHolder.getMR(LOYALTY_STANDARD.type(), loyaltyDiscountId))) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether an age start is not being used and thus able to be removed
     * The if a dayRange key value is the ageStart id.
     *
     * @param ageStartId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     */
    public boolean isAgeStartRemovable(int ageStartId) throws PersistenceException, IllegalActionException, NoSuchIdentifierException {
        if(priceTariffRange.getAllIdentifier(MRHolder.getMR(MultiRefEnum.AGE.type(), ageStartId)).isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * This method is designed to ensure that a priceGroupMr has a tariff allocated on each OPEN day of the week,
     * starting from the startYw0, for the number of weeks given. If there is no priceGroupMr key or the key is empty
     * the true is returned.
     *
     * @param priceGroupMr the MRHolder reference value of the price group to be checked (AgeStart or Room)
     * @param startYw0
     * @param noOfWeeks
     * @return true if there is a tariff on every day unless CLOSED, or there the priceGroupMr
     *         is not found or has no DayRange objects associated with it.
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public boolean hasPriceGroupGotPriceTariff(int priceGroupMr, int startYw0, int noOfWeeks) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        return hasPriceGroupGotTariff(priceGroupMr, startYw0, noOfWeeks, priceTariffRange, true);
    }

    /**
     * This method is designed to ensure that a priceGroupMr has a tariff allocated on each OPEN day of the week,
     * starting from the startYw0, for the number of weeks given. If there is no priceGroupMr key or the key is empty
     * the true is returned.
     *
     * @param priceGroupMr the MRHolder reference value of the price group to be checked (AgeStart or Room)
     * @param startYw0
     * @param noOfWeeks
     * @return true if there is a tariff on every day unless CLOSED, or there the priceGroupMr
     *         is not found or has no DayRange objects associated with it.
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public boolean hasPriceGroupGotLoyaltyTariff(int priceGroupMr, int startYw0, int noOfWeeks) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        return hasPriceGroupGotTariff(priceGroupMr, startYw0, noOfWeeks, loyaltyTariffRange, false);
    }

    /**
     * Determines whether a child education price reduction bean is able to be removed
     *
     * @param priceReductionId
     * @return
     * @throws PersistenceException
     */
    public boolean isChildEducationPriceReductionRemovable(int priceReductionId) throws PersistenceException {
        if(DayRangeManager.inDayRange(priceReductionId, childEducationPriceReductionRange)){
            return false;
        }
        return true;
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
     * Retrieves a list of Loyalty Discount objects for a specified day belonging to a specified Price Group.
     *
     * @param ywd the YWDHolder value representing the day of interest
     * @param priceGroupMr the MRHolder price group identifier representing the price group of interest
     * @param loyaltyDiscountType a MultiRefEnum enumerator representing the loyalty discount type of interest
     * @return a list of LoyaltyDiscountBean objects
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public List<LoyaltyDiscountBean> getAllLoyaltyDiscounts(int ywd, int priceGroupMr, MultiRefEnum loyaltyDiscountType) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException, IllegalValueException {
        // get the PriceTariffId (rightRef) from the DayRange using the mrGroupId (leftRef) and ywd
        int priceTariffId = loyaltyTariffRange.getDayRange(priceGroupMr, ywd).getRightRef();
        return(this.getAllLoyaltyDiscounts(priceTariffId, loyaltyDiscountType));
    }

    /**
     * Retrieves a list of Loyalty Discount objects that exist in a specified  Tariff
     *
     * @param tariffId a tariff identifier for the Tariff containing the objects
     * @param loyaltyDiscountType a MultiRefEnum enumerator representing the loyalty discount type of interest
     * @return a list of LoyaltyDiscountBean objects
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public List<LoyaltyDiscountBean> getAllLoyaltyDiscounts(int tariffId, MultiRefEnum loyaltyDiscountType) throws PersistenceException, IllegalValueException, NoSuchIdentifierException {
        if(!loyaltyDiscountType.isLoyaltyDiscount()) {
            throw new IllegalValueException("the loyaltyDiscount is not of the correct enumerator type");
        }
        List<LoyaltyDiscountBean> rtnList = new LinkedList<>();
        for(int loyaltyMr : loyaltyTariffRelationship.getAllPriceConfigMr(tariffId, loyaltyDiscountType)) {
            rtnList.add(loyaltyDiscountManager.getObject(MRHolder.getRef(loyaltyMr)));
        }
        return rtnList;
    }

    /**
     * Retrieves a list of Price Adjustments objects for a specified day belonging to a specified Price Group
     * and of a specified Multi Reference Price Adjustment type.
     *
     * @param ywd a YWDHolder value representing the day of interest
     * @param priceGroupMr a MRHolder price group identifier representing the price group of interest
     * @param priceAdjustmentType a MultiRefEnum enumerator representing the Price Adjustment type of interest
     * @return a list of PriceAdjustmentBean objects
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public List<PriceAdjustmentBean> getAllPriceAdjustments(int ywd, int priceGroupMr, MultiRefEnum priceAdjustmentType) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException, IllegalValueException {
        // get the PriceTariffId (rightRef) from the DayRange using the mrGroupId (leftRef) and ywd
        int priceTariffId = priceTariffRange.getDayRange(priceGroupMr, ywd).getRightRef();
        return(this.getAllPriceAdjustments(priceTariffId, priceAdjustmentType));
    }

    /**
     * Retrieves a list of Price Adjustment objects that exist in a specified Tariff
     * and of a specified Multi Reference Price Adjustment type.
     *
     * @param tariffId a tariff identifier for the Tariff containing the objects
     * @param priceAdjustmentType a MultiRefEnum enumerator representing the Price Adjustment type of interest
     * @return a list of PriceAdjustmentBean objects
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public List<PriceAdjustmentBean> getAllPriceAdjustments(int tariffId, MultiRefEnum priceAdjustmentType) throws PersistenceException, IllegalValueException, NoSuchIdentifierException {
        if(!priceAdjustmentType.isAdjustment()) {
            throw new IllegalValueException("the priceAdjustment is not of the correct enumerator type");
        }
        List<PriceAdjustmentBean> rtnList = new LinkedList<>();
        for(int priceAdjustmentMr : priceTariffRelationship.getAllPriceConfigMr(tariffId, priceAdjustmentType)) {
            rtnList.add(priceAdjustmentManager.getObject(MRHolder.getRef(priceAdjustmentMr)));
        }
        return rtnList;
    }

    /**
     * Retrieves a Price List object for a specified day belonging to a specified Price Group
     * and of a specified Multi Reference Price Adjustment type.
     * If No price list is found then the default price list is returned
     *
     * @param ywd a YWDHolder value representing the day of interest
     * @param priceGroupMr a MRHolder price group identifier representing the price group of interest
     * @param priceListType a MultiRefEnum enumerator representing the Price List type of interest
     * @return a list of PriceListBean objects
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public PriceListBean getPriceList(int ywd, int priceGroupMr, MultiRefEnum priceListType) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException, IllegalValueException {
        // get the PriceTariffId (rightRef) from the DayRange using the mrGroupId (leftRef) and ywd
        int priceTariffId = priceTariffRange.getDayRange(priceGroupMr, ywd).getRightRef();
        return(this.getPriceList(priceTariffId, priceListType));
    }

    /**
     * Retrieves a Price List object that exist in a specified Tariff and of a specified Multi Reference type.
     * If No price list is found then the default price list is returned
     *
     * @param tariffId a tariff identifier for the Tariff containing the objects
     * @param priceListType a MultiRefEnum enumerator representing the Price List type of interest
     * @return a a PriceListBean objects
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public PriceListBean getPriceList(int tariffId, MultiRefEnum priceListType) throws PersistenceException, IllegalValueException, NoSuchIdentifierException {
        if(!priceListType.isPriceList()) {
            throw new IllegalValueException("the priceList is not of the correct enumerator type");
        }
        for(int priceListMr : priceTariffRelationship.getAllPriceConfigMr(tariffId, priceListType)) {
            return(priceListManager.getObject(MRHolder.getRef(priceListMr)));
        }
        return priceListManager.getDefaultObject();
    }

    /**
     * Retrieves a TariffBean object for a specified day belonging to a specified Price Group.
     *
     * @param ywd the YWDHolder value representing the day of interest
     * @param priceGroupMr the MRHolder price group identifier representing the price group of interest
     * @return a TariffBean
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public TariffBean getPriceTariff(int ywd, int priceGroupMr) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        // get the PriceTariffId (rightRef) from the DayRange using the mrGroupId (leftRef) and ywd
        int priceTariffId = priceTariffRange.getDayRange(priceGroupMr, ywd).getRightRef();
        return priceTariffManager.getObject(priceTariffId);
    }

    /**
     * Retrieves a TariffBean object for a specified day belonging to a specified Price Group.
     *
     * @param ywd the YWDHolder value representing the day of interest
     * @param priceGroupMr the MRHolder price group identifier representing the price group of interest
     * @return a TariffBean
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public TariffBean getLoyaltyTariff(int ywd, int priceGroupMr) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        // get the loyaltyTariffId (rightRef) from the DayRange using the mrGroupId (leftRef) and ywd
        int loyaltyTariffId = loyaltyTariffRange.getDayRange(priceGroupMr, ywd).getRightRef();
        return loyaltyTariffManager.getObject(loyaltyTariffId);
    }

    /**
     * returns the price tariff for each of the days of the week for every priceGroup in the PriceTariffRange.
     * If no DayRangeBean is found  for the priceGroup, the default PriceTariff is added.
     *
     * @param yw0 the week to be
     * @return a Map of the week's Tariff
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public Map<Integer, List<TariffBean>> getAllPriceTariffsForWeek(int yw0) throws NoSuchIdentifierException, PersistenceException {
        Map<Integer, List<TariffBean>> rtnMap = new ConcurrentSkipListMap<>();
        for(int priceGroupMr : priceTariffRange.getAllKeys()) {
            rtnMap.put(priceGroupMr, this.getAllPriceTariffsForWeekPriceGroup(yw0, priceGroupMr));
        }
        return (rtnMap);
    }

    /**
     * Utility method to drive the methods above
     *
     * @param priceGroupMr
     * @param yw0
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<TariffBean> getAllPriceTariffsForWeekPriceGroup(int yw0, int priceGroupMr) throws NoSuchIdentifierException, PersistenceException {
        List<TariffBean> rtnList = new LinkedList<>();
        for(int priceTariffId : priceTariffRange.getRightRefForWeekLeftRef(priceGroupMr, yw0)) {
            rtnList.add(priceTariffManager.getObject(priceTariffId));
        }
        return (rtnList);
    }

    /**
     * returns the loyalty tariff for each of the days of the week for every priceGroup in the LoyaltyTariffRange.
     * If no DayRangeBean is found  for the priceGroup, the default LoyaltyTariff is added.
     *
     * @param yw0 the week to be
     * @return a Map of the week's Tariff
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public Map<Integer, List<TariffBean>> getAllLoyaltyTariffsForWeek(int yw0) throws NoSuchIdentifierException, PersistenceException {
        Map<Integer, List<TariffBean>> rtnMap = new ConcurrentSkipListMap<>();
        for(int priceGroupMr : loyaltyTariffRange.getAllKeys()) {
            rtnMap.put(priceGroupMr, this.getAllLoyaltyTariffsForWeekPriceGroup(yw0, priceGroupMr));
        }
        return (rtnMap);
    }

    /**
     * Utility method to drive the methods getAllLoyaltyTariffsForWeek
     *
     * @param priceGroupMr
     * @param yw0
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<TariffBean> getAllLoyaltyTariffsForWeekPriceGroup(int yw0, int priceGroupMr) throws NoSuchIdentifierException, PersistenceException {
        List<TariffBean> rtnList = new LinkedList<>();
        for(int priceTariffId : loyaltyTariffRange.getRightRefForWeekLeftRef(priceGroupMr, yw0)) {
            rtnList.add(loyaltyTariffManager.getObject(priceTariffId));
        }
        return (rtnList);
    }

    /**
     * Helper method that extends getPriceTariffForProfile to return the price list allocated to a ywd.
     * If no price list can be found then the default price list is returned.
     *
     * @param ywd the YWDHolder value
     * @param roomId the roomId the child is in
     * @param profileId the profileId (used for ageStart)
     * @param priceListType a MultiRefEnum enumerator representing the Price List type of interest
     * @return a PriceListBean object
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public PriceListBean getPriceListForProfile(int ywd, int roomId, int profileId, MultiRefEnum priceListType) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        TariffBean tariff = this.getPriceTariffForProfile(ywd, roomId, profileId);
        return this.getPriceList(tariff.getTariffId(), priceListType);
    }

    /**
     * Helper method that extends getPriceTariffForProfile to return all the price adjustments
     * allocated to a ywd.
     *
     * @param ywd the YWDHolder value
     * @param roomId the roomId the child is in
     * @param profileId the profileId (used for ageStart)
     * @param priceAdjustmentType a MultiRefEnum enumerator representing the Price Adjustment type of interest
     * @return a PriceAdjustmentBean object
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public List<PriceAdjustmentBean> getAllPriceAdjustmentForProfile(int ywd, int roomId, int profileId, MultiRefEnum priceAdjustmentType) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        TariffBean tariff = this.getPriceTariffForProfile(ywd, roomId, profileId);
        return this.getAllPriceAdjustments(tariff.getTariffId(), priceAdjustmentType);
    }

    /**
     * Helper method that extends getLoyaltyTariffForProfile to return all the loyalty discounts
     * allocated to a ywd.
     *
     * @param ywd the YWDHolder value
     * @param roomId the roomId the child is in
     * @param profileId the profileId (used for ageStart)
     * @param loyaltyDiscountType a MultiRefEnum enumerator representing the Loyalty Discount type of interest
     * @return a list of LoyaltyDiscountBean objects
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public List<LoyaltyDiscountBean> getAllLoyaltyDiscountForProfile(int ywd, int roomId, int profileId, MultiRefEnum loyaltyDiscountType) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        TariffBean tariff = this.getLoyaltyTariffForProfile(ywd, roomId, profileId);
        return this.getAllLoyaltyDiscounts(tariff.getTariffId(), loyaltyDiscountType);
    }

    /**
     * if a room has a Tariff allocated for the ywd, it takes precedence over an ageStart. If neither room nor
     * ageStart can be determined, returns default object
     *
     * @param ywd the YWDHolder value
     * @param roomId the roomId the child is in
     * @param profileId the profileId (used for ageStart)
     * @return a TariffBean object
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    public TariffBean getPriceTariffForProfile(int ywd, int roomId, int profileId) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, IllegalValueException {
        int priceTariffId = this.getTariffIdForProfile(priceTariffRange, ywd, roomId, profileId);
        return(priceTariffManager.getObject(priceTariffId));
    }

    /**
     * if a room has a Tariff allocated for the ywd, it takes precedence over an ageStart. If neither room nor
     * ageStart can be determined, returns default object
     *
     * @param ywd the YWDHolder value
     * @param roomId the roomId the child is in
     * @param profileId the profileId (used for ageStart)
     * @return a TariffBean object
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     * @throws IllegalValueException
     */
    public TariffBean getLoyaltyTariffForProfile(int ywd, int roomId, int profileId) throws PersistenceException, NoSuchKeyException, NoSuchIdentifierException, IllegalValueException {
        int priceTariffId = this.getTariffIdForProfile(loyaltyTariffRange, ywd, roomId, profileId);
        return(loyaltyTariffManager.getObject(priceTariffId));
    }

    /**
     * Returns a list of AgeStartBean objects where there is a priceTariff allocated
     *
     * @return a list of AgeStartBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<AgeStartBean> getAgeStartsWithActivePriceTariff() throws NoSuchIdentifierException, PersistenceException {
        return getAgeStartsWithActiveTariff(priceTariffRange);
    }

    /**
     * Returns a list of RoomConfigBean objects where there is a priceTariff allocated
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<RoomConfigBean> getRoomsWithActivePriceTariff() throws NoSuchIdentifierException, PersistenceException {
        return getRoomsWithActiveTariff(priceTariffRange);
    }

    /**
     * Returns a list of AgeStartBean objects where there is a loyaltyTariff allocated
     *
     * @return a list of AgeStartBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<AgeStartBean> getAgeStartsWithActiveLoyaltyTariff() throws NoSuchIdentifierException, PersistenceException {
        return getAgeStartsWithActiveTariff(loyaltyTariffRange);
    }

    /**
     * Returns a list of RoomConfigBean objects where there is a loyaltyTariff allocated
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<RoomConfigBean> getRoomsWithActiveLoyaltyTariff() throws NoSuchIdentifierException, PersistenceException {
        return getRoomsWithActiveTariff(loyaltyTariffRange);
    }

    /**
     * Determines which ageStarts and room(s) a tariff is assigned to in the priceTariffRangeManager
     *
     * @param priceTariffId
     * @return a list of ObjectBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ObjectBean> getPriceGroupAssignedToPriceTariff(int priceTariffId) throws NoSuchIdentifierException, PersistenceException {
        return getPriceGroupAssigned(priceTariffId, priceTariffRange);
    }

    /**
     * Determines which ageStarts and room(s) a tariff is assigned to in the loyaltyTariffRangeManager
     *
     * @param loyaltyTariffId
     * @return a list of ObjectBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ObjectBean> getPriceGroupAssignedToLoyaltyTariff(int loyaltyTariffId) throws NoSuchIdentifierException, PersistenceException {
        return getPriceGroupAssigned(loyaltyTariffId, loyaltyTariffRange);
    }


    /**
     * Determines which child education timetable(s) a PriceList is assigned to in the
     * childEducationPriceReductionRangeManager
     *
     * @param priceReductionId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ChildEducationTimetableBean> getChildEducationTimetablesAssigned(int priceReductionId) throws NoSuchIdentifierException, PersistenceException {
        TimetableService ts = TimetableService.getInstance();
        LinkedList<ChildEducationTimetableBean> rtnList = new LinkedList<>();
        for(ChildEducationTimetableBean cet : ts.getChildEducationTimetableManager().getAllObjects()) {
            if(!childEducationPriceReductionRange.getAllObjects(cet.getCetId(), priceReductionId).isEmpty()) {
                rtnList.add(cet);
            }
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week for a leftRef. If no PeriodSdValueBean is found the
     * PeriodSdValueManager.NO_PRICE_LIST is added.
     *
     * @param cetId
     * @param yw0
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<PriceListBean> getChildEducationPriceReductionsForWeekTimetable(int cetId, int yw0) throws NoSuchIdentifierException, PersistenceException {
        List<PriceListBean> rtnList = new LinkedList<>();
        for(int id : childEducationPriceReductionRange.getRightRefForWeekLeftRef(cetId, yw0)) {
            rtnList.add(childEducationPriceReductionManager.getObject(id));
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week. If no DayRangeBean is found the
     * PeriodSdValueManager.NO_PRICE_LIST is added.
     *
     * @param yw0
     * @return a Map of the week
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public Map<Integer, List<PriceListBean>> getAllChildEducationPriceReductionsForWeek(int yw0) throws NoSuchIdentifierException, PersistenceException {
        Map<Integer, List<PriceListBean>> rtnMap = new ConcurrentSkipListMap<>();
        for(int cetId : childEducationPriceReductionRange.getAllKeys()) {
            rtnMap.put(cetId, getChildEducationPriceReductionsForWeekTimetable(cetId, yw0));
        }
        return (rtnMap);
    }

    /**
     * returns the PeriodSdValueBean Object for a ywd and education timetable
     *
     * @param ywd
     * @param cetId
     * @return
     * @throws NoSuchKeyException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public PriceListBean getChildEducationPriceReduction(int ywd, int cetId) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        return (childEducationPriceReductionManager.getObject(childEducationPriceReductionRange.getDayRange(cetId, ywd).getRightRef()));
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
     * MAINTENANCE helper method to ensure the integrity of the tariff
     * relationship managers. THis will remove any relationships that have
     * become disassociated with a tariff.
     * 
     * @throws PersistenceException
     */
    public void tidyTariffRelationships() throws PersistenceException {
        // run through the price Tariff relationships and ensure there is a price Tariff
        for(int tariffId : priceTariffRelationship.getAllKeys()) {
            if(!priceTariffManager.isIdentifier(tariffId)) {
                priceTariffRelationship.removeKey(tariffId);
            }
        }
        // run through the loyalty tariff relationships and ensure there is a loyalty tariff
        for(int tariffId : loyaltyTariffRelationship.getAllKeys()) {
            if(!loyaltyTariffManager.isIdentifier(tariffId)) {
                loyaltyTariffRelationship.removeKey(tariffId);
            }
        }
    }

    /**
     * Sets a price list. If a pricelIst id is passed then the price list must not be in use. if
     * -1 is passed then a new price list Id is generated.
     *
     * @param priceListId
     * @param name
     * @param label
     * @param periodSdValues
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public PriceListBean setPriceList(int priceListId, String name, String label,
            Map<Integer, Long> periodSdValues, String owner) throws PersistenceException, MaxCountReachedException, NullObjectException, IllegalActionException {
        int id = priceListId > 0 ? priceListId : priceListManager.generateIdentifier();
        // check the priceList is not in use
        if(!isPriceListRemovable(id)) {
            throw new IllegalActionException("The PriceList id is already in use and can't be changed");
        }
        PriceListBean bean = new PriceListBean(id, name, label, periodSdValues, owner);
        return priceListManager.setObject(bean);
    }

    /**
     * Removes a booking price list if it is not being used in the priceListRange
     *
     * @param priceListId
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void removePriceList(int priceListId) throws PersistenceException, IllegalActionException {
        if(!isPriceListRemovable(priceListId)) {
            throw new IllegalActionException("The PriceList id is in use and can't be removed");
        }
        priceListManager.removeObject(priceListId);
    }

    /**
     * Sets the a BillItem. If the bill item is set to -1 then a new billItemId is generated.
     * If the billItemId exists it will be replaced except if the billItem is in use in a
     * price list.
     *
     * @param priceAdjustmentId
     * @param name the name of the BillItem
     * @param billingBits a billing bit (see BillingBits)
     * @param value a value relating to the BillingBits
     * @param precision
     * @param rangeSd
     * @param repeated
     * @param durationSd
     * @param owner
     * @return the new PriceAdjustmentBean
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public PriceAdjustmentBean setPriceAdjustment(int priceAdjustmentId, String name, int billingBits, long value,
            int precision, int rangeSd, boolean repeated, int durationSd, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException, IllegalActionException {
        int id = priceAdjustmentId > 0 ? priceAdjustmentId : priceAdjustmentManager.generateIdentifier();

        // the id passed is in use and can't be replaced
        if(!this.isPriceAdjustmentRemovable(id)) {
            throw new IllegalActionException("The price adjustment is in use and can not be modified");
        }
        PriceAdjustmentBean bean = new PriceAdjustmentBean(id, name, billingBits, value, precision, rangeSd, repeated, durationSd, owner);
        return priceAdjustmentManager.setObject(bean);
    }

    /**
     * Removes a billItem. The billItem can not be in use in a priceList
     *
     * @param priceAdjustmentId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public PriceAdjustmentBean removePriceAdjustment(int priceAdjustmentId) throws PersistenceException, IllegalActionException {
        // check it is not in use
        if(!this.isPriceAdjustmentRemovable(priceAdjustmentId)) {
            throw new IllegalActionException("The price adjustment is in use and can not be removed");
        }
        return priceAdjustmentManager.removeObject(priceAdjustmentId);
    }

    /**
     * sets a new price tariff. If the priceTariffId argument is -1 then a new number is generated.
     *
     * @param tariffId
     * @param name
     * @param label
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public TariffBean setPriceTariff(int tariffId, String name, String label, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException {
        int id = tariffId > 0 ? tariffId : priceTariffManager.generateIdentifier();
        if(!isPriceTariffRemovable(id)) {
            throw new IllegalActionException("The price tariff is in use and can not be modified");
        }
        return priceTariffManager.setObject(new TariffBean(id, name, label, owner));
    }

    /**
     * removes a price tariff
     *
     * @param tariffId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public TariffBean removePriceTariff(int tariffId) throws PersistenceException, IllegalActionException {
        if(!isPriceTariffRemovable(tariffId)) {
            throw new IllegalActionException("The price tariff is in use and can not be removed");
        }
        priceTariffRelationship.removeKey(tariffId);
        return priceTariffManager.removeObject(tariffId);
    }

    /**
     * Sets the price tariff relationship between a priceTariff and a priceConfig. The type
     * of priceConfig is determined by the MultiRefEnum priceConfigType. Though you can
     * allocate one priceList across multiple Tariffs and allocate one price list different
     * priceConfigTypes, you can only have one priceList and priceConfigType per Tariff.
     * You cannot mix LoyaltyDiscounts with any other priceConfigTypein a single tariff.
     *
     * The priceConfigId can be -1 for a PriceList config type.  This will have the effect of
     * removing the price list of this type from the tariff.
     *
     * @param tariffId
     * @param priceConfigId
     * @param priceConfigType
     * @param owner
     * @throws DuplicateIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public void setPriceTariffRelationship(int tariffId, int priceConfigId, MultiRefEnum priceConfigType, String owner) throws DuplicateIdentifierException, PersistenceException, NullObjectException, IllegalActionException, IllegalValueException, NoSuchIdentifierException {
        if(!priceConfigType.isPriceList() && !priceConfigType.isAdjustment()) {
            throw new IllegalValueException("the priceConfigType is not of the correct enumerator type");
        }
        if(priceConfigId == -1 && !priceConfigType.isPriceList()) {
            throw new IllegalValueException("You can only pass a priceConfigId of -1 for PriceList priceConfigType");
        }
        if(priceConfigId != -1) {
            if(!isPriceConfig(priceConfigId, priceConfigType)){
                throw new NoSuchIdentifierException("the priceConfigId is not a valid identifier");
            }
            if(!isPriceConfigValid(priceConfigId, priceConfigType)){
                throw new IllegalValueException("the priceConfigType is not compatable with the priceConfigId");
            }
        }
        if(!this.isId(tariffId, VT.PRICE_TARIFF)) {
            throw new NoSuchIdentifierException("the tariffId is not a valid identifier");
        }
        // if the priceTariff is sheduled then we can not alter its relationships
        if(!isPriceTariffRemovable(tariffId)) {
            throw new IllegalActionException("The price tariff is in use and therfore it and its relationships can't be altered");
        }
        // if this is a priceList we need to check there is only one
        for(int priceConfigMr : priceTariffRelationship.getAllIdentifier(tariffId)) {
            if(priceConfigType.isPriceList() && MRHolder.getType(priceConfigMr) == priceConfigType.type()) {
                // remove the old pricelist
                priceTariffRelationship.removeObject(tariffId, priceConfigMr);
                // we wanted to remove the price list only
                if(priceConfigId == -1) {
                    return;
                }
                // there should only be one pricelist so jump out
                break;
            }
        }
        if(priceConfigId != -1) {
            int priceConfigMr = MRHolder.getMR(priceConfigType.type(), priceConfigId);
            priceTariffRelationship.setObject(tariffId, new TariffRelationBean(priceConfigMr, owner));
        }
    }

    /**
     * removes a tariff relationship to a priceConfig
     *
     * @param tariffId
     * @param priceConfigId
     * @param priceConfigType
     * @throws IllegalActionException
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public void removePriceTariffRelationship(int tariffId, int priceConfigId, MultiRefEnum priceConfigType) throws IllegalActionException, PersistenceException, IllegalValueException, NoSuchIdentifierException {
        if(!priceConfigType.isPriceList() && !priceConfigType.isAdjustment()) {
            throw new IllegalValueException("the priceConfigType is not of the correct enumerator type");
        }
        if(!isPriceConfig(priceConfigId, priceConfigType)){
            throw new NoSuchIdentifierException("the priceConfigId is not a valid identifier");
        }
        if(!this.isId(tariffId, VT.PRICE_TARIFF)) {
            throw new NoSuchIdentifierException("the tariffId is not a valid identifier");
        }
        // if the priceTariff is sheduled then we can remove the relationship
        if(!isPriceTariffRemovable(tariffId)) {
            throw new IllegalActionException("The price tariff is in use and therfore it and its relationships can't be removed");
        }
        priceTariffRelationship.removeObject(tariffId, MRHolder.getMR(priceConfigType.type(), priceConfigId));
    }

    /**
     * Sets a day range for a priceGroup (leftRef) referencing a tariff (rightRef)
     *
     * @param priceGroupMr
     * @param tariffId
     * @param startYwd
     * @param endYwd
     * @param days
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public DayRangeBean setPriceTariffDayRange(int priceGroupMr, int tariffId, int startYwd,
            int endYwd, boolean[] days, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException, NoSuchIdentifierException {
        // create the new dayRange
        if(!this.isId(tariffId, VT.PRICE_TARIFF)) {
            throw new NoSuchIdentifierException("the tariffId is not a valid identifer");
        }
        int dayRangeId = priceTariffRange.generateIdentifier();
        DayRangeBean dayRange = new DayRangeBean(dayRangeId, priceGroupMr, tariffId, startYwd, endYwd, days, owner);
        // add to the priceTariffRange
        return priceTariffRange.setObject(priceGroupMr, dayRange);
    }

    /**
     * Terminates a price list day range early, if the endYwd required is before the existing one. If a range starts
     * after the new endYwd, it is removed entirely. Allows old ranges to be retained for type integrity although the
     * user cannot see them.
     *
     * @param priceGroupMr
     * @param dayRangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void terminatePriceTariffDayRange(int priceGroupMr, int dayRangeId, int endYwd, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException {
        if(endYwd < priceTariffRange.getObject(priceGroupMr, dayRangeId).getStartYwd()) {
            priceTariffRange.removeObject(priceGroupMr, dayRangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < priceTariffRange.getObject(priceGroupMr, dayRangeId).getEndYwd()) {
            priceTariffRange.setObjectEndYwd(priceGroupMr, dayRangeId, endYwd, owner);
        }
    }

    /**
     * Moves a tariff range up or down in the layers
     *
     * @param priceGroupMr
     * @param rangeId
     * @param viewYw0
     * @param startYwd
     * @param moveUp
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     */
    public List<TariffBean> movePriceTariffDayRange(int priceGroupMr, int rangeId, int viewYw0, int startYwd, boolean moveUp, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, MaxCountReachedException, NoSuchKeyException {
        List<TariffBean> rtnList = new LinkedList<>();
        for(int id : priceTariffRange.moveObject(priceGroupMr, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(priceTariffManager.getObject(id));
        }
        return rtnList;
    }

    /**
     * completely removes a PriceTariffDayRange
     *
     * @param priceGroupMr
     * @param dayRangeId
     * @throws PersistenceException
     */
    public void removePriceTariffDayRange(int priceGroupMr, int dayRangeId) throws PersistenceException {
        priceTariffRange.removeObject(priceGroupMr, dayRangeId);
    }

    /**
     * Sets a Loyalty Discount
     *
     * @param loyaltyDiscountId
     * @param name the name of the Loyalty Discount
     * @param billingBits the chargeBits associated with this loyalty discount
     * @param discount the discount
     * @param start (optional) when to start, -1 if not used
     * @param durationIn the minimum number of minutes
     * @param priorityDays the days the discount applies to (over one week)
     * @param owner
     * @return the newly created LoyaltyDiscountBean
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws IllegalValueException
     */
    public LoyaltyDiscountBean setLoyaltyDiscount(int loyaltyDiscountId, String name, int billingBits, long discount, int start, int durationIn,
            boolean[] priorityDays, String owner) throws MaxCountReachedException, NullObjectException, PersistenceException, IllegalActionException, IllegalValueException {
        int id = loyaltyDiscountId > 0 ? loyaltyDiscountId : loyaltyDiscountManager.generateIdentifier();
        // duration need 1 taken off it it is not zero
        int duration = durationIn == 0 ? durationIn : durationIn - 1;
        if(!isLoyaltyDiscountRemovable(id)) {
            throw new IllegalActionException("The Loyalty Discount is in use and can not be modified");
        }
        if(start < -1 || start > BookingManager.MAX_PERIOD_LENGTH) {
            throw new IllegalValueException("The start value is out of range");
        }
        if(duration < 0) {
            throw new IllegalValueException("The duration value must be a positive value");
        }
        int completeBillingBits = LoyaltyDiscountManager.getCompleteBillingBits(billingBits);
        LoyaltyDiscountBean config =  new LoyaltyDiscountBean(id, name, completeBillingBits, discount, start, duration, priorityDays, owner);
        // last check to ensure the duration is not excessive
        if(!config.hasBillingBit(BillingEnum.RANGE_SUM_TOTAL) && config.getDuration() > BookingManager.MAX_PERIOD_LENGTH) {
             throw new IllegalValueException("The duration is greater than the maximum period length in a day");
        }
        if(config.hasBillingBit(BillingEnum.RANGE_SUM_TOTAL) && config.getDuration() > (BookingManager.MAX_PERIOD_LENGTH * 7)) {
             throw new IllegalValueException("The duration is greater than the maximum period length in a week");
        }
        return loyaltyDiscountManager.setObject(config);
    }


    /**
     * removes a Loyalty Discount
     *
     * @param loyaltyDiscountId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public LoyaltyDiscountBean removeLoyaltyDiscount(int loyaltyDiscountId) throws PersistenceException, IllegalActionException {
        if(!isLoyaltyDiscountRemovable(loyaltyDiscountId)) {
            throw new IllegalActionException("The Loyalty is in use and can not be removed");
        }
        return loyaltyDiscountManager.removeObject(loyaltyDiscountId);
    }

    /**
     * sets a new loyalty tariff. If the loyaltyTariffId argument is -1 then a new number is generated.
     *
     * @param tariffId
     * @param name
     * @param label
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public TariffBean setLoyaltyTariff(int tariffId, String name, String label, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException {
        int id = tariffId > 0 ? tariffId : loyaltyTariffManager.generateIdentifier();
        if(!isLoyaltyTariffRemovable(id)) {
            throw new IllegalActionException("The loyalty tariff is in use and can not be modified");
        }
        return loyaltyTariffManager.setObject(new TariffBean(id, name, label, owner));
    }

    /**
     * removes a loyalty tariff and any loyalty tariff relationships
     *
     * @param tariffId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public TariffBean removeLoyaltyTariff(int tariffId) throws PersistenceException, IllegalActionException {
        if(!isLoyaltyTariffRemovable(tariffId)) {
            throw new IllegalActionException("The loyalty tariff is in use and can not be removed");
        }
        loyaltyTariffRelationship.removeKey(tariffId);
        return loyaltyTariffManager.removeObject(tariffId);
    }

    /**
     * Sets the price tariff relationship between a priceTariff and a priceConfig. The type
     * of priceConfig is determined by the MultiRefEnum priceConfigType. Though you can
     * allocate one priceList across multiple Tariffs and allocate one price list different
     * priceConfigTypes, you can only have one priceList and priceConfigType per Tariff.
     * You cannot mix LoyaltyDiscounts with any other priceConfigTypein a single tariff.
     *
     * @param tariffId
     * @param loyaltyDiscountId
     * @param loyaltyConfigType
     * @param owner
     * @throws DuplicateIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws IllegalValueException
     * @throws NoSuchIdentifierException
     */
    public void setLoyaltyTariffRelationship(int tariffId, int loyaltyDiscountId, MultiRefEnum loyaltyConfigType, String owner) throws DuplicateIdentifierException, PersistenceException, NullObjectException, IllegalActionException, IllegalValueException, NoSuchIdentifierException {
        if(!loyaltyConfigType.isLoyaltyDiscount()) {
            throw new IllegalValueException("the loyaltyConfigType is not of the correct enumerator type");
        }
        if(!isId(loyaltyDiscountId, VT.LOYALTY_DISCOUNT)) {
            throw new NoSuchIdentifierException("the loyaltyDiscountId is not a valid ");
        }
        if(!this.isId(tariffId, VT.LOYALTY_TARIFF)) {
            throw new NoSuchIdentifierException("the tariffId is not a valid loyalty tariff identifier");
        }
        if(!isPriceConfigValid(loyaltyDiscountId, loyaltyConfigType)) {
            throw new IllegalValueException("The loyaltyConfigType is not compatable with the loyaltyDiscountId");
        }
        // if the priceTariff is sheduled then we can not alter its relationships
        if(!isLoyaltyTariffRemovable(tariffId)) {
            throw new IllegalActionException("The price tariff is use and therfore it and its relationships can't be altered");
        }
        int loyaltyDiscountMr = MRHolder.getMR(loyaltyConfigType.type(), loyaltyDiscountId);
        loyaltyTariffRelationship.setObject(tariffId, new TariffRelationBean(loyaltyDiscountMr, owner));
    }

    /**
     * removes a tariff relationship to a priceConfig
     *
     * @param tariffId
     * @param loyaltyDiscountId
     * @param loyaltyDiscountType
     * @throws IllegalActionException
     * @throws PersistenceException
     */
    public void removeLoyaltyTariffRelationship(int tariffId, int loyaltyDiscountId, MultiRefEnum loyaltyDiscountType) throws IllegalActionException, PersistenceException {
        // if the priceTariff is sheduled then we can remove the relationship
        if(!isLoyaltyTariffRemovable(tariffId)) {
            throw new IllegalActionException("The loyalty tariff is use and therfore it and its relationships can't be removed");
        }
        loyaltyTariffRelationship.removeObject(tariffId, MRHolder.getMR(loyaltyDiscountType.type(), loyaltyDiscountId));
    }

    /**
     * Sets a day range for a priceGroup (leftRef) referencing a tariff (rightRef)
     *
     * @param priceGroupMr
     * @param tariffId
     * @param startYwd
     * @param endYwd
     * @param days
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public DayRangeBean setLoyaltyTariffDayRange(int priceGroupMr, int tariffId, int startYwd,
            int endYwd, boolean[] days, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException {
        // create the new dayRange
        int dayRangeId = loyaltyTariffRange.generateIdentifier();
        DayRangeBean dayRange = new DayRangeBean(dayRangeId, priceGroupMr, tariffId, startYwd, endYwd, days, owner);
        // add to the priceTariffRange
        return loyaltyTariffRange.setObject(priceGroupMr, dayRange);
    }

    /**
     * Terminates a price list day range early, if the endYwd required is before the existing one. If a range starts
     * after the new endYwd, it is removed entirely. Allows old ranges to be retained for type integrity although the
     * user cannot see them.
     *
     * @param priceGroupMr
     * @param dayRangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void terminateLoyaltyTariffDayRange(int priceGroupMr, int dayRangeId, int endYwd, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException {
        if(endYwd < loyaltyTariffRange.getObject(priceGroupMr, dayRangeId).getStartYwd()) {
            loyaltyTariffRange.removeObject(priceGroupMr, dayRangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < loyaltyTariffRange.getObject(priceGroupMr, dayRangeId).getEndYwd()) {
            loyaltyTariffRange.setObjectEndYwd(priceGroupMr, dayRangeId, endYwd, owner);
        }
    }

    /**
     * Moves a tariff range up or down in the layers
     *
     * @param priceGroupMr
     * @param rangeId
     * @param viewYw0
     * @param startYwd
     * @param moveUp
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     */
    public List<TariffBean> moveLoyaltyTariffDayRange(int priceGroupMr, int rangeId, int viewYw0, int startYwd, boolean moveUp, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, MaxCountReachedException, NoSuchKeyException {
        List<TariffBean> rtnList = new LinkedList<>();
        for(int id : loyaltyTariffRange.moveObject(priceGroupMr, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(loyaltyTariffManager.getObject(id));
        }
        return rtnList;
    }

    /**
     * Completely removes a LoyaltyTariffDayRange
     *
     * @param priceGroupMr
     * @param dayRangeId
     * @throws PersistenceException
     */
    public void removeLoyaltyTariffDayRange(int priceGroupMr, int dayRangeId) throws PersistenceException {
        loyaltyTariffRange.removeObject(priceGroupMr, dayRangeId);
    }

    /**
     * Sets an ageStartBean
     *
     * @param ageStartId
     * @param name
     * @param startAgeMonths
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public AgeStartBean setAgeStart(int ageStartId, String name, int startAgeMonths, String owner)
            throws NullObjectException, PersistenceException, MaxCountReachedException {
        int id = ageStartId > 0 ? ageStartId : ageStartManager.generateIdentifier();
        // add as a key to the priceListRange
        if(!priceTariffRange.getAllKeys().contains(id)) {
            priceTariffRange.setKey(MRHolder.getMR(MultiRefEnum.AGE.type(), id));
        }
        // set theobject
        return ageStartManager.setObject(new AgeStartBean(id, name, startAgeMonths, owner));
    }

    /**
     * Removes an ageStartBean
     *
     * @param ageStartId
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void removeAgeStart(int ageStartId) throws PersistenceException, IllegalActionException {
        int ageStartMr = MRHolder.getMR(MultiRefEnum.AGE.type(), ageStartId);
        if(!priceTariffRange.getAllObjects(ageStartMr).isEmpty()) {
            throw new IllegalActionException("AgeStartHasPriceTariffScheduled");
        }
        priceTariffRange.removeKey(ageStartMr);
        ageStartManager.removeObject(ageStartId);
    }

    /**
     * Sets a child education price reduction bean. NB the id and owner must be correctly set in the bean
     *
     * @param priceReductionId
     * @param name
     * @param label
     * @param periodSdValues
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public PriceListBean setChildEducationPriceReduction(int priceReductionId, String name, String label, Map<Integer, Long> periodSdValues, String owner)throws NullObjectException, PersistenceException, MaxCountReachedException, IllegalActionException {
        int id = priceReductionId > 0 ? priceReductionId : childEducationPriceReductionManager.generateIdentifier();
        if(DayRangeManager.inDayRange(id, childEducationPriceReductionRange)){
            throw new IllegalActionException("The ChildEducation Price List is in use");
        }
        return (childEducationPriceReductionManager.setObject(new PriceListBean(id, name, label, periodSdValues, owner)));
    }

    /**
     * Removes a child education price reduction bean if it is not being used in the priceListRangeManager
     *
     * @param priceReductionId
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void removeChildEducationPriceReduction(int priceReductionId) throws PersistenceException, IllegalActionException {
        if(DayRangeManager.inDayRange(priceReductionId, childEducationPriceReductionRange)){
            throw new IllegalActionException("The ChildEducation Price List is in use");
        }
        childEducationPriceReductionManager.removeObject(priceReductionId);
    }

    /**
     * Sets a child education timetable's price reduction bean schedule
     *
     * @param cetId
     * @param priceReductionId
     * @param startYwd
     * @param endYwd
     * @param days
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public DayRangeBean setChildEducationPriceReductionDayRange(int cetId, int priceReductionId, int startYwd,
            int endYwd, boolean[] days, String owner) throws NullObjectException, PersistenceException, MaxCountReachedException {
        return (childEducationPriceReductionRange.setObject(cetId,
                new DayRangeBean(childEducationPriceReductionRange.generateIdentifier(), cetId, priceReductionId, startYwd, endYwd, days, owner)));
    }

    /**
     * Terminates a child education price reduction day range early, if the endYwd required is before the existing one.
     * If a range starts after the new endYwd, it is removed entirely. Allows old ranges to be retained for type
     * integrity although the user cannot see them.
     *
     * @param cetId
     * @param rangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void terminateChildEducationPriceReductionDayRange(int cetId, int rangeId, int endYwd, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        if(endYwd < childEducationPriceReductionRange.getObject(cetId, rangeId).getStartYwd()) {
            childEducationPriceReductionRange.removeObject(cetId, rangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < childEducationPriceReductionRange.getObject(cetId, rangeId).getEndYwd()) {
            childEducationPriceReductionRange.setObjectEndYwd(cetId, rangeId, endYwd, owner);
        }
    }

    /**
     * Removes a child Education Price Reduction
     *
     * @param cetId
     * @param rangeId
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void removeChildEducationPriceReductionDayRange(int cetId, int rangeId) throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        childEducationPriceReductionRange.removeObject(cetId, rangeId);
    }

    /**
     * Moves one of a education timetable's child education price ranges up or down in the layers
     *
     * @param cetId
     * @param rangeId
     * @param viewYw0
     * @param startYwd
     * @param moveUp
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public List<PriceListBean> moveChildEducationPriceReductionDayRange(int cetId, int rangeId, int viewYw0,
            int startYwd, boolean moveUp, String owner)
            throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, NullObjectException, MaxCountReachedException {
        LinkedList<PriceListBean> rtnList = new LinkedList<>();
        for(int id : childEducationPriceReductionRange.moveObject(cetId, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(childEducationPriceReductionManager.getObject(id));
        }
        return (rtnList);
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

    /**
     * Determines which ageStarts and room(s) a PriceTariff is assigned to in the TariffRangeManager
     *
     * @param priceListId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    private List<ObjectBean> getPriceGroupAssigned(int tariffId, DayRangeManager tariffRange) throws NoSuchIdentifierException, PersistenceException {
        AgeRoomService rs = AgeRoomService.getInstance();
        LinkedList<ObjectBean> rtnList = new LinkedList<>();
        for(RoomConfigBean room : rs.getRoomConfigManager().getAllObjects()) {
            if(!tariffRange.getAllObjects(MRHolder.getMR(MultiRefEnum.ROOM.type(), room.getRoomId()), tariffId).isEmpty()) {
                rtnList.add(room);
            }
        }
        for(AgeStartBean ageBand : ageStartManager.getAllObjects()) {
            if(!tariffRange.getAllObjects(MRHolder.getMR(MultiRefEnum.AGE.type(), ageBand.getAgeStartId()), tariffId).isEmpty()) {
                rtnList.add(ageBand);
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of AgeStartBean objects where there is a priceTariff allocated
     *
     * @return a list of AgeStartBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    private List<AgeStartBean> getAgeStartsWithActiveTariff(DayRangeManager tariffRange) throws NoSuchIdentifierException, PersistenceException {
        List<AgeStartBean> rtnList = new LinkedList<>();
        for(int priceGroupMr : tariffRange.getAllKeys()) {
            if(!tariffRange.getAllObjects(priceGroupMr).isEmpty()) {
                // ageStarts
                if(MRHolder.isType(priceGroupMr, MultiRefEnum.AGE.type())) {
                    rtnList.add(ageStartManager.getObject(MRHolder.getRef(priceGroupMr)));
                }
            }
        }
        return rtnList;
    }

    /**
     * Returns a list of RoomConfigBean objects where there is a priceTariff allocated
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    private List<RoomConfigBean> getRoomsWithActiveTariff(DayRangeManager tariffRange) throws NoSuchIdentifierException, PersistenceException {
        AgeRoomService rs = AgeRoomService.getInstance();
        List<RoomConfigBean> rtnList = new LinkedList<>();
        for(int priceGroupId : tariffRange.getAllKeys()) {
            if(!tariffRange.getAllObjects(priceGroupId).isEmpty()) {
                // rooms
                if(MRHolder.isType(priceGroupId, MultiRefEnum.ROOM.type())) {
                    rtnList.add(rs.getRoomConfigManager().getObject(MRHolder.getRef(priceGroupId)));
                }
            }
        }
        return rtnList;
    }

    private int getTariffIdForProfile(DayRangeManager tariffRange, int ywd, int roomId, int profileId) throws PersistenceException, NoSuchIdentifierException, IllegalValueException, NoSuchKeyException {
        if(!YWDHolder.isValid(ywd)) {
            throw new IllegalValueException("The ywd '" + ywd + "' is not valid");
        }
        // make up the priceGroupMr
        int groupMr = MRHolder.getMR(MultiRefEnum.ROOM.type(), roomId);
        // determine whether it has been allocated to the room on the ywd
        int tariffId = tariffRange.getDayRange(groupMr, ywd).getRightRef();
        // if the return is a default value then it must be an ageStart tariff
        if(tariffId == ObjectEnum.DEFAULT_ID.value()) {
            // we now need to find the age group for the profile DOB
            PropertiesService ps = PropertiesService.getInstance();
            ChildService cs = ChildService.getInstance();

            // get date of birth from the child
            int dob = cs.getChildManager().getObject(profileId).getDateOfBirth();
            // conver that to how many months to today
            int ageMonths = CalendarStatic.getAgeMonths(dob);
            // get the ageStart that is within the age in months
            int ageStartId;
            try {
                ageStartId = this.ageStartManager.getObjectByAge(ageMonths).getAgeStartId();
            } catch(IllegalValueException ive) {
                // if this isn't found there is no tarif so return the default tariff id
                return(TariffManager.NO_TARIFF);
            }
            // create the MRHolder id;
            int ageStartMr = MRHolder.getMR(MultiRefEnum.AGE.type(), ageStartId);
            // find the price in the dayRange
            tariffId = tariffRange.getDayRange(ageStartMr, ywd).getRightRef();
        }
        return tariffId;
    }

    /**
     * This method is designed to ensure that a priceGroupMr has at least one valid tariff assigned AND
     * a tariff is allocated on each OPEN day of the week, starting from the startYw0, for the number of weeks given.
     * If there is no priceGroupMr key or the key is empty then false is returned.
     *
     * @param priceGroupMr the MRHolder reference value of the price group to be checked (AgeStart or Room)
     * @param startYw0
     * @param noOfWeeks
     * @param tariffManager
     * @param isPrice
     * @return true if the group has at least one tariff allocated and there is a tariff on every OPEN day
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalValueException
     */
    protected boolean hasPriceGroupGotTariff(int priceGroupMr, int startYw0, int noOfWeeks, DayRangeManager tariffManager, boolean isPrice) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException, IllegalValueException {
        // if there is no range at all for this priceGroupMr return false
        if(tariffManager.getAllObjects(priceGroupMr).isEmpty()) {
            return false;
        }
        TimetableService ts = TimetableService.getInstance();
        // loop weeks
        int yw0 = startYw0;
        TariffBean tariff;
        for(int w = 0; w < noOfWeeks; w++) {
            // loop days in week
            for(int d = 0; d < YWDHolder.DAYS_IN_WEEK; d++) {
                // ignore closed days
                if(ts.isClosed(yw0 + d)) {
                    continue;
                }
                // always returns at least the default object
                if(isPrice) {
                    tariff = this.getPriceTariff(yw0 + d, priceGroupMr);
                } else {
                    tariff = this.getLoyaltyTariff(yw0 + d, priceGroupMr);
                }
                if(tariff.getTariffId() == TariffManager.NO_TARIFF) {
                    return false;
                }
            }
            // add on a week for the next loop
            yw0 = YWDHolder.add(yw0, 10);
        }
        // there is a valid tariff for every open day
        return true;
    }

    //</editor-fold>

}
