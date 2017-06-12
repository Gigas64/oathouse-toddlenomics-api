/*
 * @(#)PropertiesService.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cma.booking.*;
import com.oathouse.ccm.cma.profile.*;
import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.concessions.*;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.profile.*;
import com.oathouse.ccm.cos.properties.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;

/**
 * The {@code PropertiesService} Class is a encapsulated API, service level singleton that provides
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
 * @version 1.00 08-Dec-2011
 */
public class PropertiesService {
    // Singleton Instance

    private volatile static PropertiesService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    // application.concession
    private final ObjectSingleStore<MilkConcessionBean> milkConcession = new ObjectSingleStore<>(VT.MILK_CONCESSION.manager());
    private final ObjectSingleStore<HolidayConcessionBean> holidayConcession = new ObjectSingleStore<>(VT.HOLIDAY_CONCESSION.manager());
    // application.properties
    private final SystemPropertiesManager systemPropertiesManager = new SystemPropertiesManager(VT.PROPERTIES.manager());
    private final AccountHolderManager accountHolderManager = new AccountHolderManager(VT.ACCOUNT_HOLDER.manager());

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private PropertiesService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code PropertiesService} class
     * @return instance of the {@code PropertiesService}
     * @throws PersistenceException
     */
    public static PropertiesService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (PropertiesService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new PropertiesService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code PropertiesService} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying
     * elements of the initialisation are not available.
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code PropertiesService}. The
     * method returns an instance of the {@code PropertiesService} so it can be chained.
     * This must be called before a the {@code PropertiesService} is used. e.g  {@code PropertiesService myPropertiesService = }
     * @return instance of the {@code PropertiesService}
     * @throws PersistenceException
     */
    public synchronized PropertiesService init() throws PersistenceException {
        if(initialise) {
            // application.concession
            milkConcession.init();
            holidayConcession.init();
            // application.properties
            systemPropertiesManager.init();
            accountHolderManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code PropertiesService}. The
     * method returns an instance of the {@code PropertiesService} so it can be chained.
     * @return instance of the {@code PropertiesService}
     * @throws PersistenceException
     */
    public PropertiesService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code PropertiesService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = milkConcession.clear()?success:false;
        success = holidayConcession.clear()?success:false;
        success = systemPropertiesManager.clear()?success:false;
        success = accountHolderManager.clear()?success:false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code PropertiesService} class instance by setting the INSTANCE reference
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

    /*
     * Returns the AccountHolderBean
     */
    public AccountHolderBean getAccountHolderProperties() throws NoSuchIdentifierException, PersistenceException {
        return accountHolderManager.getObject();
    }

    /*
     * Returns the HolidayConcessionBean
     */
    public HolidayConcessionBean getHolidayConcession() throws NoSuchIdentifierException, PersistenceException {
        return holidayConcession.getObject();
    }

    /*
     * returns the MilkConcessionBean
     */
    public MilkConcessionBean getMilkConcession() throws NoSuchIdentifierException, PersistenceException {
        return milkConcession.getObject();
    }

    /*
     * returns the SystemPropertiesBean
     */
    public SystemPropertiesBean getSystemProperties() throws NoSuchIdentifierException, PersistenceException {
        return systemPropertiesManager.getObject();
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
     * Sets all the account holder properties
     *
     * @param contact
     * @param businessName
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param phone
     * @param accountId
     * @param priceUrl
     * @param superUsername
     * @param superPassword
     * @param returnEmailAddress
     * @param returnEmailDisplayName
     * @param billingEmail
     * @param validated
     * @param suspended
     * @param timeZone
     * @param language
     * @param currency
     * @param chargeRate
     * @param registeredCapacity
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setAccountHolderProperties(String contact, String businessName,
            String line1, String line2, String line3, String city, String province, String postcode, String country,
            String phone,
            String accountId, String priceUrl,
            String superUsername, String superPassword,
            String returnEmailAddress, String returnEmailDisplayName,
            String billingEmail,
            boolean validated, boolean suspended,
            String timeZone, String language, String currency,
            int chargeRate, int registeredCapacity, String owner)
            throws PersistenceException, NullObjectException, NoSuchIdentifierException {

        this.accountHolderManager.setObject(new AccountHolderBean(contact, businessName,
                line1, line2, line3, city, province, postcode, country,
                phone,
                accountId, priceUrl,
                superUsername, superPassword,
                returnEmailAddress, returnEmailDisplayName,
                billingEmail,
                validated, suspended,
                timeZone, language, currency,
                chargeRate, registeredCapacity, owner));
    }

    /**
     * Sets the holiday concessions properties
     *
     * @param defaultAllocation
     * @param countFrom
     * @param limitProRata
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setHolidayConcession(int defaultAllocation, int countFrom, boolean limitProRata, String owner)
            throws PersistenceException, NullObjectException,
            NoSuchIdentifierException {

        holidayConcession.setObject(new HolidayConcessionBean(defaultAllocation, countFrom, limitProRata, owner));
    }

    /**
     * set the milk concession properties
     *
     * @param claimMinMonths
     * @param claimMaxMonths
     * @param sessionTimes
     * @param entitlement
     * @param units
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setMilkConcession(int claimMinMonths, int claimMaxMonths,
            Set<Integer> sessionTimes, double entitlement, String units, String owner)
            throws PersistenceException, NullObjectException,
            NoSuchIdentifierException {

        milkConcession.setObject(new MilkConcessionBean(claimMinMonths, claimMaxMonths,
                sessionTimes, entitlement, units, owner));
    }

    /**
     * Sets base module attributes in the system properties bean
     * @param confirmedPeriodWeeks
     * @param actualsPeriodWeeks
     * @param adminSuspended
     * @param reinstateBTIdBit
     * @param bookingActualLimit
     * @param closedDayIncludedInNoticePeriod
     * @param loyaltyApplyedToEducationDays
     * @param legacySystem
     * @param actualDropOffIdMandatory
     * @param actualPickupIdMandatory
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void setSystemPropertiesBaseModule(
            int confirmedPeriodWeeks,
            int actualsPeriodWeeks,
            boolean adminSuspended,
            int reinstateBTIdBit,
            int bookingActualLimit,
            boolean closedDayIncludedInNoticePeriod,
            boolean loyaltyApplyedToEducationDays,
            LegacySystem legacySystem,
            boolean actualDropOffIdMandatory,
            boolean actualPickupIdMandatory,
            String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException, MaxCountReachedException {

        // check if there was a change to the DropOff and Pickup Mandatory
        if(actualDropOffIdMandatory != systemPropertiesManager.getObject().isActualDropOffIdMandatory()
                || actualPickupIdMandatory != systemPropertiesManager.getObject().isActualPickupIdMandatory()) {
            ChildBookingService.getInstance().setBookingStateForMandatoryChange(actualDropOffIdMandatory, actualPickupIdMandatory);
        }

        systemPropertiesManager.setObjectBaseModule(confirmedPeriodWeeks, actualsPeriodWeeks, adminSuspended,
                reinstateBTIdBit, bookingActualLimit, closedDayIncludedInNoticePeriod, loyaltyApplyedToEducationDays, legacySystem,
                actualDropOffIdMandatory, actualPickupIdMandatory, owner);
    }

    /**
     * Sets minimum staff attributes in the system properties bean
     *
     * @param minStaff
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setSystemPropertiesMinStaff(int minStaff, String owner) throws PersistenceException, NoSuchIdentifierException {
        systemPropertiesManager.setObjectMinStaff(minStaff, owner);
    }

    /**
     * Sets planning module attributes in the system properties bean
     *
     * @param toExceedCapacityWhenInsertingRequests
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setSystemPropertiesPlanModule(boolean toExceedCapacityWhenInsertingRequests, String owner)
            throws NoSuchIdentifierException, PersistenceException {
        systemPropertiesManager.setObjectPlanModule(toExceedCapacityWhenInsertingRequests, owner);

    }

    /**
     * Sets finance module attributes in the system properties bean
     * @param chargeMargin
     * @param pickupChargeMargin
     * @param dropOffChargeMargin
     * @param bookingsTaxable
     * @param taxRate
     * @param firstInvoiceYwd
     * @param creditCardFeeRate
     * @param paymentInstructions
     * @param paymentPeriod
     * @param regularPaymentInstructions
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setSystemPropertiesFinanceModule(boolean chargeMargin,
            int dropOffChargeMargin,
            int pickupChargeMargin,
            boolean bookingsTaxable,
            int taxRate,
            int firstInvoiceYwd,
            int creditCardFeeRate,
            String paymentInstructions,
            PaymentPeriodEnum paymentPeriod,
            String regularPaymentInstructions,
            String owner) throws NoSuchIdentifierException, PersistenceException {

        systemPropertiesManager.setObjectFinanceModule(chargeMargin, dropOffChargeMargin, pickupChargeMargin,
                bookingsTaxable, taxRate, firstInvoiceYwd, creditCardFeeRate, paymentInstructions, paymentPeriod, regularPaymentInstructions, owner);
    }

    /**
     * Saves the default invoice settings.
     * @param defaultInvoiceLastYwd
     * @param defaultInvoiceDueYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setSystemPropertiesDefaultInvoiceSettings(RelativeDate defaultInvoiceLastYwd,
            RelativeDate defaultInvoiceDueYwd, String owner)
            throws NoSuchIdentifierException, PersistenceException {
        systemPropertiesManager.setObjectDefaultInvoiceSettings(defaultInvoiceLastYwd,
                defaultInvoiceDueYwd, owner);
    }

    /**
     * set the maxAgeMonths. WARNING: When changing the MaxAgeMonths you should use
     * setSystemMaxAgeMonthsWithCascadingUpdate() as this provides safe cascading updates
     * for dependencies.
     *
     * @param maxAgeMonths
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @see ChildService
     */
    public void setSystemMaxAgeMonths(int maxAgeMonths, String owner) throws NoSuchIdentifierException, PersistenceException  {
        systemPropertiesManager.setObjectMaxAgeMonths(maxAgeMonths, owner);
    }

    /**
     * set the textBTIdStandard and textBTIdSpecial text
     *
     * @param textBTIdStandard
     * @param textBTIdSpecial
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setSystemTextBTId(String textBTIdStandard, String textBTIdSpecial, String owner) throws PersistenceException, NoSuchIdentifierException {
        systemPropertiesManager.setObjectTextBTId(textBTIdStandard, textBTIdSpecial, owner);
    }

    /**
     * set the occupancySdSet
     *
     * @param occupancySdSet
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setSystemOccupancySdSet(Set<Integer> occupancySdSet, String owner) throws PersistenceException, NoSuchIdentifierException {
        systemPropertiesManager.setObjectOccupancySdSet(occupancySdSet, owner);
    }

    /**
     * Sets the maxAgeMonths with proper referential integrity. Changes to the maxAgeMonths is cascaded to
     * all referential objects with appropriate action taken. The return is a list of ChildBean objects
     * that are now beyond the age limit. Note also that any bookings or childRoomStartreferences are removed,
     *
     * @param maxAgeMonths
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public List<ChildBean> setSystemMaxAgeMonthsWithCascadingUpdate(int maxAgeMonths, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException {
        // get the managers needed
        AgeRoomService rs = AgeRoomService.getInstance();
        ChildBookingService bs = ChildBookingService.getInstance();
        ChildService cs = ChildService.getInstance();

        List<ChildBean> rtnList = new LinkedList<>();
        int maxAgeYW0 = CalendarStatic.getApproxYW0(maxAgeMonths);
        int today = CalendarStatic.getRelativeYWD(0);

        int maxAgeDepartYwd, departYwd;
        if(maxAgeMonths < getSystemProperties().getMaxAgeMonths()) {
            // committing: remove any future room starts
            ChildBean c;
            for(int childId : rs.getChildRoomStartManager().getAllKeys()) {
                c = cs.getChildManager().getObject(childId);
                if(c.getDepartYwd() > today) {
                    maxAgeDepartYwd = YWDHolder.add(c.getDateOfBirth(), maxAgeYW0);
                    departYwd = maxAgeDepartYwd < today ? today : maxAgeDepartYwd;
                    for(ChildRoomStartBean roomStart : rs.getChildRoomStartManager().getAllObjects(childId)) {
                        if(roomStart.getStartYwd() > departYwd) {
                            rs.getChildRoomStartManager().removeObject(childId, roomStart.getRoomId());
                        }
                    }
                }
            }
            // set departYwd to later of today or the calculated departYwd based on age
            for(ChildBean child : cs.getChildManager().getAllObjects()) {
                maxAgeDepartYwd = YWDHolder.add(child.getDateOfBirth(), maxAgeYW0);
                departYwd = maxAgeDepartYwd < today ? today : maxAgeDepartYwd;
                if(child.getDepartYwd() > departYwd) {
                    cs.getChildManager().setObjectDepartYwd(child.getChildId(), departYwd, owner);
                    // remove any booking for that child after departYwd
                    for(int ywd : bs.getBookingManager().getAllKeys()) {
                        if(ywd > departYwd) {
                            for(BookingBean booking : bs.getBookingManager().getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, child.getChildId(), BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF)) {
                                bs.getBookingManager().removeObject(ywd, booking.getBookingId());
                            }
                        }
                    }
                    rtnList.add(child);
                }
            }
        }
        systemPropertiesManager.setObjectMaxAgeMonths(maxAgeMonths, owner);
        return (rtnList);
    }

    /**
     * sets the Account Holder superUser attribute
     *
     * @param username
     * @param password
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderSuperUser(String username, String password, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectSuperUser(username, password, owner);
    }

    /**
     * sets the Account Holder returnEmail attribute
     *
     * @param emailAddress
     * @param displayName
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderReturnEmail(String emailAddress, String displayName, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectReturnEmail(emailAddress, displayName, owner);
    }

    /**
     * sets the Account Holder billing attribute
     *
     * @param priceUrl
     * @param validated
     * @param billingEmail
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderBilling(String priceUrl, boolean validated, String billingEmail, String owner)
            throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectBilling(priceUrl, validated, billingEmail, owner);
    }

    /**
     * sets the Account Holder suspended attribute
     *
     * @param isSuspended
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderSuspended(boolean isSuspended, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectSuspended(isSuspended, owner);
    }

    /**
     * Sets the AccountHolder locale
     * @param timeZone
     * @param language
     * @param currency
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderLocale(String timeZone, String language, String currency, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectLocale(timeZone, language, currency, owner);
    }

    /**
     * Sets the AccountHolder charge rate
     * @param chargeRate hundreths of a percent (1% = 100)
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderChargeRate(int chargeRate, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectChargeRate(chargeRate, owner);
    }

    /**
     * Sets the AccountHolder registered capacity
     * @param registeredCapacity
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAccountHolderRegisteredCapacity(int registeredCapacity, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        accountHolderManager.setObjectRegisteredCapacity(registeredCapacity, owner);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Private Class Methods">
    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     *
     * This section is for all private methods for the
     * class. Private methods should be carefully used
     * so as to avoid multi jump calls and as a general
     * rule of thumb, only when multiple methods use a
     * common algorithm.
     * ***************************************************/
    //</editor-fold>

}
