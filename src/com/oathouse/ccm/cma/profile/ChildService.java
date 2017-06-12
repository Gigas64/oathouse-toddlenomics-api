/*
 * @(#)ChildService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.profile;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cma.accounts.*;
import com.oathouse.ccm.cma.booking.*;
import com.oathouse.ccm.cma.config.*;
import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.config.education.*;
import com.oathouse.ccm.cos.profile.*;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code ChildService} Class is a encapsulated API, service level singleton that provides
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
 * TODO : parameter name change for for CustodialRelationship and LegalAccountRelationship
 * TODO : properly validate all passed values
 *
 * @author Darryl Oatridge
 * @version 1.00 30-Nov-2011
 */
public class ChildService {
    // Singleton Instance

    private volatile static ChildService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final AccountManager accountManager = new AccountManager(VT.ACCOUNT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final ChildManager childManager = new ChildManager(VT.CHILD.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final MedicalManager medicalManager = new MedicalManager(VT.MEDICAL.manager());
    private final ContactManager contactManager = new ContactManager(VT.CONTACT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // relation managers
    private final RelationManager custodialRelationship = new RelationManager(VT.CUSTODIAL_RELATION.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final RelationManager legalAccountRelationship = new RelationManager(VT.LEGAL_RELATION.manager(), ObjectDataOptionsEnum.ARCHIVE);
    // DayRange managers
    private final DayRangeManager childEducationDayRange = new DayRangeManager(VT.CHILD_EDUCATION_RANGE.manager());

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private ChildService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code ChildService} class
     * @return instance of the {@code ChildService}
     * @throws PersistenceException
     */
    public static ChildService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (ChildService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new ChildService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code ChildService} class has been initialised. This is used
     * mostly for testing to avoid initialisation of managers when the underlying
     * elements of the initialisation are not available.
     * @return
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code ChildService}. The
     * method returns an instance of the {@code ChildService} so it can be chained.
     * This must be called before a the {@code ChildService} is used. e.g  {@code ChildService myChildService = }
     * @return instance of the {@code ChildService}
     * @throws PersistenceException
     */
    public synchronized ChildService init() throws PersistenceException {
        if(initialise) {
            accountManager.init();
            childManager.init();
            contactManager.init();
            medicalManager.init();
            legalAccountRelationship.init();
            custodialRelationship.init();
            childEducationDayRange.init(childManager.getAllIdentifier());
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code ChildService}. The
     * method returns an instance of the {@code ChildService} so it can be chained.
     * @return instance of the {@code ChildService}
     * @throws PersistenceException
     */
    public ChildService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code ChildService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = accountManager.clear() ? success : false;
        success = childManager.clear() ? success : false;
        success = contactManager.clear() ? success : false;
        success = medicalManager.clear() ? success : false;
        success = legalAccountRelationship.clear() ? success : false;
        success = custodialRelationship.clear() ? success : false;
        success = childEducationDayRange.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code ChildService} class instance by setting the INSTANCE reference
     * to null. This is mostly used for testing to clear and reset internal memory stores
     * when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval Methods">
    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     *
     * This section allows the retrieval of the instance
     * of the manager to use as part of this service. For
     * ease of reference each manager has both a long and
     * a short name method call. ALL manager instances should
     * be referenced through the Service and not instanciated
     * directly.
     * ***************************************************/
    public AccountManager getAccountManager() {
        return accountManager;
    }

    public ChildManager getChildManager() {
        return childManager;
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public RelationManager getCustodialRelationship() {
        return custodialRelationship;
    }

    public DayRangeManager getChildEducationRangeManager() {
        return childEducationDayRange;
    }

    public RelationManager getLegalAccountRelationship() {
        return legalAccountRelationship;
    }

    public MedicalManager getMedicalManager() {
        return medicalManager;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">
    /* ***************************************************
     * S E R V I C E   V A L I D A T I O N   M E T H O D S
     *
     * This section is for all validation methods so that
     * other services can check blind id values are legitimate
     * and any associated business rules are adheared to.
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
            case ACCOUNT:
                return accountManager.isIdentifier(id);
            case CHILD:
                return childManager.isIdentifier(id);
            case MEDICAL:
                return medicalManager.isIdentifier(id);
            case CONTACT:
                return contactManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    /**
     * Tests whether an account is eligible for removal
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     */
    public boolean isAccountRemovable(int accountId) throws PersistenceException, NoSuchIdentifierException, IllegalActionException {
        try {
            checkAccountRemovable(accountId);
        } catch(IllegalActionException iae) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether a legal account relationship is eligible for removal
     * @param accountId
     * @param contactId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     */
    public boolean isLegalAccountRelationshipRemovable(int accountId, int contactId) throws PersistenceException, NoSuchIdentifierException, IllegalActionException {
        RelationType oldType = getAccountContactType(accountId, contactId);
        try {
            checkLegalAccountRelationshipRemovable(accountId, contactId, oldType);
        } catch(IllegalActionException iae) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether a child is eligible for removal
     * @param childId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isChildRemovable(int childId) throws PersistenceException, NoSuchIdentifierException {
        try {
            this.checkChildRemovable(childId);
        } catch(IllegalActionException e) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether a custodial relationship is eligible for removal
     * @param contactId
     * @return true if removable
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isProfessionalRemovable(int contactId) throws PersistenceException, NoSuchIdentifierException {
        try {
            this.checkProfessionalRemovable(contactId);
        } catch(IllegalActionException e) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether a custodial relationship is eligible for removal
     * @param contactId
     * @return true if removable
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isNonProfessionalRemovable(int contactId) throws PersistenceException, NoSuchIdentifierException {
        try {
            this.checkNonProfessionalRemovable(contactId);
        } catch(IllegalActionException e) {
            return false;
        }
        return true;
    }

    /**
     * Validate whether the account for a childId is suspended
     * @param childId
     * @return true if the account is suspended
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public boolean isAccountSuspended(int childId) throws NoSuchIdentifierException, PersistenceException {
        if(!isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        int accountId = childManager.getObject(childId).getAccountId();
        if(!isId(accountId, VT.ACCOUNT)) {
            throw new NoSuchIdentifierException("The accountId " + accountId + " for child " + childId + "does not exist", true);
        }
        return (accountManager.getObject(accountId).getStatus().equals(AccountStatus.SUSPENDED) ? true : false);
    }

    /**
     * Validate if the ChildEducationTimetable exists
     *
     * @param childId
     * @param ywd
     * @return true if exists
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public boolean isDayChildEducationTimetable(int childId, int ywd) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        DayRangeBean dayRange = childEducationDayRange.getDayRange(childId, ywd);
        if(dayRange == null || dayRange.getDayRangeId() == ObjectEnum.DEFAULT_ID.value()) {
            return false;
        }
        return(true);
    }

    /**
     * Validation method to check various aspects of a profile identifier set of values. The validation
     * cover direct id reference and relational reference. To use the method you must have at least an
     * accountId or a childId. To check a custodian you must have a childId
     *
     * @param accountId
     * @param childId
     * @param liableId
     * @param custodianId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void checkProfile(int accountId, int childId, int liableId, int custodianId) throws PersistenceException, NoSuchIdentifierException {
        // validate the values passed must have an account or child
        if(accountId < 1 && childId < 1) {
            throw new IllegalArgumentException("You must have a value for either the account or the child to validate");
        }
        // to check liable we need either a childId or an account Id
        if(liableId > 0 && accountId < 1 && childId < 1) {
            throw new IllegalArgumentException("You must have an accountId or childId to validate liableId");
        }
        // to check the custodianId you must have a childId
        if(custodianId > 0 && childId < 1) {
            throw new IllegalArgumentException("You must have a childId to validate custodianId");
        }
        // we might need to get the accountId from the child
        int chkAccId = accountId;
        // check child first as we might need to use child to get accountId
        if(childId > 0) {
            // check the child exists
            if(!childManager.isIdentifier(childId)) {
                throw new NoSuchIdentifierException("Child " + childId + " is not a recognised child id");
            }
            // check custodian
            if(custodianId > 0 && !custodialRelationship.isIdentifier(childId, custodianId)) {
                throw new NoSuchIdentifierException("custodianId " + custodianId + " does not belong to child " + childId);
            }
            // set the accountId for further checking
            if(accountId < 1) {
                chkAccId = childManager.getObject(childId).getAccountId();
            } else {
                if(childManager.getObject(childId).getAccountId() != accountId) {
                    throw new NoSuchIdentifierException("Account passed is different from the child accountId");
                }
            }
        }
        // chkAccId must have a value as either it was passed or child set it
        if(!accountManager.isIdentifier(chkAccId)) {
            throw new NoSuchIdentifierException("Account " + chkAccId + " is not a recognised account id");
        }
        // check liability
        if(liableId > 0 && !legalAccountRelationship.isIdentifier(chkAccId, liableId)) {
            throw new NoSuchIdentifierException("liableId " + liableId + " does not belong to account " + chkAccId);
        }
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
     * Finds a child's pro rata holiday allowance up to the given ywd
     * @param childId
     * @param onYwd
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public double getProRataHolAllocation(int childId, int onYwd) throws NoSuchIdentifierException, PersistenceException, IllegalValueException {
        AgeRoomService rs = AgeRoomService.getInstance();
        if(!isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        int daysInAllocationYear = CalendarStatic.getDayInterval(rs.getHolYearStartYWD(childId), onYwd);
        double dailyRate = ((double)daysInAllocationYear)/365d;
        double childAllocation = (double)(childManager.getObject(childId).getHolidayAllocation());
        double proRataAllocation = dailyRate * childAllocation;
        if(proRataAllocation < 0) {
            proRataAllocation = 0;
        }
        return proRataAllocation;
    }

   /**
     * Finds a child's pro rata holiday allowance today
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public double getProRataHolAllocation(int childId) throws NoSuchIdentifierException, PersistenceException, IllegalValueException {
        return getProRataHolAllocation(childId, getTodayYwd());
    }

    /**
     * Finds a child's rounded up pro rata holiday allowance today
     * @param childId
     * @param onYwd
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public int getRoundedProRataHolAllocation(int childId, int onYwd) throws PersistenceException, NoSuchIdentifierException, IllegalValueException {
        double roundedAllocation = Math.round(getProRataHolAllocation(childId, onYwd));
        return (int)roundedAllocation;
    }

    /**
     * Finds out which children would be affected by a change in MaxAgeMonths in the System Properties.
     * Returns a list of ChildBean objects where the new MaxAgeMonths would mean the child will be older
     * than the maximum age.
     *
     * @param maxAgeMonths
     * @param owner
     * @return list of ChildBean objects older than MaxAgeMonths
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public List<ChildBean> getAffectedFromMaxAgeMonthsChange(int maxAgeMonths, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException {
        // get the managers needed
        PropertiesService ps = PropertiesService.getInstance();

        List<ChildBean> rtnList = new LinkedList<>();
        int maxAgeYW0 = CalendarStatic.getApproxYW0(maxAgeMonths);

        int maxAgeDepartYwd, departYwd;
        if(maxAgeMonths < ps.getSystemProperties().getMaxAgeMonths()) {
            for(ChildBean child : childManager.getAllObjects()) {
                maxAgeDepartYwd = YWDHolder.add(child.getDateOfBirth(), maxAgeYW0);
                departYwd = maxAgeDepartYwd < this.getTodayYwd() ? this.getTodayYwd() : maxAgeDepartYwd;
                if(child.getDepartYwd() > departYwd) {
                    rtnList.add(child);
                }
            }
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week for a leftRef. If no DayRangeBean is found the
     * ChildEducationRangeManager.NO_RANGE is added.
     *
     * @param childId
     * @param yw0
     * @return a List of rightRef integers for the week
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<ChildEducationTimetableBean> getChildEducationTimetablesForWeekChild(int childId, int yw0) throws PersistenceException, NoSuchIdentifierException {
        TimetableService ts = TimetableService.getInstance();
        LinkedList<ChildEducationTimetableBean> rtnList = new LinkedList<>();
        if(!isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        for(int id : childEducationDayRange.getRightRefForWeekLeftRef(childId, yw0)) {
            rtnList.add(ts.getChildEducationTimetableManager().getObject(id));
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week. If no DayRangeBean is found the
     * ChildEducationRangeManager.NO_RANGE is added.
     *
     * @param yw0
     * @return a Map of the week
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public Map<Integer, List<ChildEducationTimetableBean>> getAllChildEducationTimetablesForWeek(int yw0) throws PersistenceException, NoSuchIdentifierException {
        Map<Integer, List<ChildEducationTimetableBean>> rtnMap = new ConcurrentSkipListMap<>();
        for(int childId : childEducationDayRange.getAllKeys()) {
            rtnMap.put(childId, getChildEducationTimetablesForWeekChild(childId, yw0));
        }
        return (rtnMap);
    }


    /**
     * Finds the ywds of the holidays that a child has used in the current holiday year from their allowance
     *
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public Set<Integer> getHolUsedYWDs(int childId) throws NoSuchIdentifierException, PersistenceException, IllegalValueException {

        if(!isId(childId, VT.CHILD)) {
            throw new NoSuchIdentifierException("The childId " + childId + " does not exist");
        }
        Set<Integer> hols = new ConcurrentSkipListSet<>();
        int confirmedPeriodWeeks = PropertiesService.getInstance().getSystemProperties().getConfirmedPeriodWeeks();
        int ywd = AgeRoomService.getInstance().getHolYearStartYWD(childId);
        if(ywd == -1) {
            return hols;
        }
        int lastDayInCurrentHolYear = YWDHolder.add(YWDHolder.add(ywd, 1000), -1);
        int lastConfirmedPeriodDay = YWDHolder.add(this.getTodayYwd(), confirmedPeriodWeeks * 10) + 6;
        int lastCountableDayInCurrentHolYear = lastDayInCurrentHolYear < lastConfirmedPeriodDay ? lastDayInCurrentHolYear : lastConfirmedPeriodDay;
        while(ywd <= lastCountableDayInCurrentHolYear) {
            if(!ChildBookingService.getInstance().getYwdBookingsForProfile(ywd, BookingManager.ALL_PERIODS, childId, BTBits.LEAVE_BIT, BTFlagBits.ALLOWANCE_FLAG).isEmpty()) {
                    hols.add(ywd);
                }
            ywd = YWDHolder.add(ywd, 1);
        }
        return (hols);
    }

    /**
     * Returns the AccountBean that a child belongs to
     *
     * @param childId
     * @return AccountBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public AccountBean getAccountForChild(int childId) throws NoSuchIdentifierException, PersistenceException {

        if(childManager.isIdentifier(childId)) {
            int accountId = childManager.getObject(childId).getAccountId();
            if(accountManager.isIdentifier(accountId)) {
                return accountManager.getObject(accountId);
            }
            throw new NoSuchIdentifierException("There is no account with id " + accountId);
        }
        throw new NoSuchIdentifierException("There is no child with id " + childId);
    }

    /**
     * Returns AccountBeans for a contactId, given a context type such as liable, invoice
     *
     * @param liableId
     * @param type
     * @return AccountBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<AccountBean> getAccountsForContact(int liableId, RelationType type) throws NoSuchIdentifierException, PersistenceException {
        if(contactManager.isIdentifier(liableId)) {
            List<AccountBean> rtnList = new LinkedList<>();
            if(!type.isAccounts()) {
                return (rtnList);
            }
            for(int accountId : legalAccountRelationship.getAllKeys()) {
                if(legalAccountRelationship.isIdentifier(accountId, liableId)) {
                    RelationBean relation = legalAccountRelationship.getObject(accountId, liableId);
                    AccountBean a = accountManager.getObject(accountId);
                    if(!rtnList.contains(a) && (type.isLiable() && relation.getType().isLiable()) || (type.isInvoice() && relation.getType().isInvoice())) {
                        rtnList.add(a);
                    }
                }
            }
            return (rtnList);
        }
        throw new NoSuchIdentifierException("There is no contact with id " + liableId);
    }

    /**
     * Determines the exact type of the relationship between the account and contact
     * @param accountId
     * @param contactId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public RelationType getAccountContactType(int accountId, int contactId) throws NoSuchIdentifierException, PersistenceException {
        for(RelationBean relation : legalAccountRelationship.getAllObjects(accountId)) {
            if(relation.getProfileId() == contactId) {
                return (relation.getType());
            }
        }
        throw new NoSuchIdentifierException("The contactId " + contactId + " has no relation with accountId " + accountId);
    }

    /**
     * Returns a list of ContactBean objects that belong to an account and are liable, sorted in the order
     * they were last updated, with the oldest first (not the order of adding the contact to the system)
     *
     * @param accountId
     * @return List of ContactBean Objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getLiableContactsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ContactBean> rtnList = new LinkedList<>();
        List<RelationBean> allCustomerRelations = legalAccountRelationship.getAllObjects(accountId);
        Collections.sort(allCustomerRelations);
        for(RelationBean relation : allCustomerRelations) {
            if(relation.getType().isLiable()) {
                rtnList.add(contactManager.getObject(relation.getProfileId()));
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of ContactBean objects that belong to an account and are liable, sorted in the order
     * they were added to the account (not the order of adding the contact to the system)
     *
     * @param accountId
     * @return List of ContactBean Objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getInvoiceContactsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ContactBean> rtnList = new LinkedList<>();
        List<RelationBean> allCustomerRelations = legalAccountRelationship.getAllObjects(accountId);
        Collections.sort(allCustomerRelations);
        for(RelationBean relation : allCustomerRelations) {
            if(relation.getType().isInvoice()) {
                rtnList.add(contactManager.getObject(relation.getProfileId()));
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of all ContactBeans that belong to an account.
     *
     * @param accountId
     * @return List of ContactBean Objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getAllContactsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ContactBean> rtnList = new LinkedList<>();
        ContactBean c;
        for(RelationBean relation : legalAccountRelationship.getAllObjects(accountId)) {
            c = contactManager.getObject(relation.getProfileId());
            if(!rtnList.contains(c)) {
                rtnList.add(c);
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of all ContactBeans belonging to the specified account that require an invoice by email
     * @param accountId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getEmailInvoiceableContactsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean c : getInvoiceContactsForAccount(accountId)) {
            if(getAccountContactType(accountId, c.getContactId()).isInvoiceEmail()) {
                rtnList.add(c);
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of all ContactBeans belonging to the specified account that require an invoice on paper
     * @param accountId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getPaperInvoiceableContactsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean c : getInvoiceContactsForAccount(accountId)) {
            if(getAccountContactType(accountId, c.getContactId()).isInvoiceFull()
                    || getAccountContactType(accountId, c.getContactId()).isInvoiceSummary()) {
                rtnList.add(c);
            }
        }
        return (rtnList);
    }

    /**
     * Returns a list of ChildBean that belong to an account
     *
     * @param accountId
     * @return List of ChildBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ChildBean> getChildrenForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        List<ChildBean> rtnList = new LinkedList<>();
        for(ChildBean child : childManager.getAllObjects()) {
            if(child.getAccountId() == accountId) {
                rtnList.add(child);
            }
        }
        return (rtnList);
    }

    /**
     * Returns a Set of the Child Identifiers for this account
     *
     * @param accountId
     * @return List of ChildBean objects
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public Set<Integer> getAllChildIdsForAccount(int accountId) throws NoSuchIdentifierException, PersistenceException {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(ChildBean child : childManager.getAllObjects()) {
            if(child.getAccountId() == accountId) {
                rtnSet.add(child.getChildId());
            }
        }
        return (rtnSet);
    }

    /**
     * Gets all the MedicalBean Objects that belong to a child and are of a particular MedType
     *
     * @param childId the id of the child
     * @param medTypeOptions the medical type
     * @return a list of MedicalBean objects
     * @throws PersistenceException
     */
    public List<MedicalBean> getMedicalBeansForChild(int childId, MedicalType... medTypeOptions) throws PersistenceException {
        LinkedList<MedicalBean> rtnList = new LinkedList<>();
        for(MedicalBean med : medicalManager.getAllObjects()) {
            if(med.getChildId() == childId
                    && (medTypeOptions.length == 0 || Arrays.asList(medTypeOptions).contains(med.getType()))) {
                rtnList.add(med);
            }
        }
        return (rtnList);
    }

    /**
     * Gets all the custodians for a childId
     *
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getAllCustodiansForChild(int childId) throws NoSuchIdentifierException, PersistenceException {
        LinkedList<ContactBean> rtnList = new LinkedList<>();
        // these relations have a defined type determining the relationship between child and contact
        for(RelationBean relation : custodialRelationship.getAllObjects(childId)) {
            if(relation.getType().isCustodian()) {
                rtnList.add(contactManager.getObject(relation.getProfileId()));
            }
        }
        return (rtnList);
    }

    /**
     * Gets all Children for a contactId.  Contact can be custodian or professional
     *
     * @param contactId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ChildBean> getAllChildrenForContact(int contactId) throws NoSuchIdentifierException, PersistenceException {
        LinkedList<ChildBean> rtnList = new LinkedList<>();
        for(int childId : custodialRelationship.getAllKeysForIdentifier(contactId)){
            rtnList.add(childManager.getObject(childId));
        }
        return (rtnList);
    }

    /**
     * Return a list of custodians that have no relationship and have a status type
     * found in the profileStatus list
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getCustodiansWithoutRelationship() throws NoSuchIdentifierException, PersistenceException {
        LinkedList<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean contact : contactManager.getAllObjects()) {
            // custodians are held without a defined relationship - NO_RELATION.
            // The relationship is held in the custodianManager, but the contacts themselves are examined
            if(!custodialRelationship.isIdentifier(contact.getContactId())
                    && contact.getType() == RelationType.NON_PROFESSIONAL) {
                rtnList.add(contact);
            }
        }
        return (rtnList);
    }

    /**
     * A list of Professionals without relationships that also have a status type found
     * within the profileStatus list.
     *
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<ContactBean> getProfessionalsWithoutRelationship() throws NoSuchIdentifierException, PersistenceException {
        LinkedList<ContactBean> rtnList = new LinkedList<>();
        for(ContactBean contact : contactManager.getAllObjects()) {
            // contacts with relationships are professionals
            if(!custodialRelationship.isIdentifier(contact.getContactId())
                    && contact.getType().isProfessional()) {
                rtnList.add(contact);
            }
        }
        return (rtnList);
    }

    /**
     * gets a Contact for a child Id with a specific type. E.g. get me a child's Doctor.
     *
     * @param childId
     * @param type
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public ContactBean getContactForChild(int childId, RelationType type) throws NoSuchIdentifierException, PersistenceException {
        for(RelationBean relation : custodialRelationship.getAllObjects(childId)) {
            if(relation.getType().equals(type)) {
                return (contactManager.getObject(relation.getProfileId()));
            }
        }
        throw new NoSuchIdentifierException("There is no contact of type " + type.toString() + " for childId '" + childId + "'");
    }

    /**
     * Returns the type of relation a child and a contact have
     *
     * @param childId
     * @param contactId
     * @return
     * @throws PersistenceException
     */
    public RelationType getRelationship(int childId, int contactId) throws PersistenceException {
        try {
            return (custodialRelationship.getObject(childId, contactId).getType());
        } catch(NoSuchIdentifierException e) {
            return (RelationType.NON_PROFESSIONAL);
        }

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
     * Adds a relation, to do this the child and the contact must exist or it will throw a
     * NoSuchIdentifierException. If the RelationType is UNDERINED it will be ignored. If
     * the RealtionType is a professional and that professional exists, the original professional
     * will be replaced.
     *
     * @param childId
     * @param contactId
     * @param type
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void addRelationship(int childId, int contactId, RelationType type, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        if(!type.isProfessional() && !type.isCustodian()) {
            throw new NoSuchIdentifierException("The Relation type must be a Professional or Custodian");
        }
        if(!childManager.isIdentifier(childId)) {
            throw new NoSuchIdentifierException("You must create the child profile before you set the relationships");
        }
        if(!contactManager.isIdentifier(contactId)) {
            throw new NoSuchIdentifierException("You must create the contact profile before you set the relationship");
        }
        for(RelationBean bean : custodialRelationship.getAllObjects(childId)) {
            if((type.isProfessional() && bean.getType().equals(type))
                    || (type.isCustodian() && bean.getProfileId() == contactId)) {
                custodialRelationship.removeObject(childId, bean.getProfileId());
            }
        }
        custodialRelationship.setObject(childId, new RelationBean(contactId, type, owner));
    }

    /**
     * removes a relationship between a child and a contact
     *
     * @param childId
     * @param contactId
     * @throws PersistenceException
     */
    public void removeRelationship(int childId, int contactId) throws PersistenceException {
        custodialRelationship.removeObject(childId, contactId);
    }

    /**
     * removes a professional RelationType from a child
     *
     * @param childId
     * @param profession
     * @throws PersistenceException
     */
    public void removeRelationship(int childId, RelationType profession) throws PersistenceException {
        if(profession.isProfessional()) {
            for(RelationBean relation : custodialRelationship.getAllObjects(childId)) {
                if(relation.getType().equals(profession)) {
                    custodialRelationship.removeObject(childId, relation.getProfileId());
                }
            }
        }
    }

    /**
     * Creates a new account. To create an account you must have a contact
     * that exists who will be made liable and invoiceable. This can be changed at a later date
     *
     * @param accountRef
     * @param contactId
     * @param notes
     * @param owner
     * @return the newly created account bean
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     */
    public AccountBean createAccount(String accountRef, int contactId, String notes, String owner) throws PersistenceException, IllegalActionException, NoSuchIdentifierException, MaxCountReachedException, NullObjectException {
        if(accountManager.isAccountRef(accountRef)) {
            throw new IllegalActionException("AccountRefAlreadyUsed");
        }
        if(contactManager.isIdentifier(contactId) && contactManager.getObject(contactId).getType().isNonProfessional()) {
            AccountBean account = new AccountBean(accountManager.generateIdentifier(), accountRef, notes, owner);
            accountManager.setObject(account);
            legalAccountRelationship.setObject(account.getAccountId(),
                    new RelationBean(contactId, RelationType.LIABLE_INVOICE_FULL, owner));
            return (account);
        }
        throw new NoSuchIdentifierException("There is no contact with id " + contactId + " or the contact is not a custodian");
    }

    /**
     * edits an accountRef for an existing bean
     * @param accountId
     * @param accountRef
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setAccountRef(int accountId, String accountRef, String owner) throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        accountManager.setObjectAccountRef(accountId, accountRef, owner);
    }

    /**
     * edits the notes for an existing bean
     * @param accountId
     * @param notes
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setAccountNotes(int accountId, String notes, String owner) throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        accountManager.setObjectNotes(accountId, notes, owner);
    }

    /**
     * sets the account contract boolean
     * @param accountId
     * @param contract
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setAccountContract(int accountId, boolean contract, String owner)
            throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        accountManager.setObjectContract(accountId, contract, owner);
    }

    /**
     * edits the fixedItemDiscountRate for an existing bean
     * @param accountId
     * @param fixedItemDiscountRate
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setAccountFixedItemDiscountRate(int accountId, int fixedItemDiscountRate, String owner) throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        accountManager.setObjectFixedItemDiscountRate(accountId, fixedItemDiscountRate, owner);
    }

    /**
     * Allows an account to be suspended or reinstated cleanly.
     *
     * If suspended, finds all future bookings for each child and sets confirmed bookings as suspended.  All other
     * bookings remain with their current status.  New bookings cannot be set and using this.setBookingStatus() does not
     * allow changing of booking status when child's account is suspended.  This effectively locks down all bookings.
     *
     * If being reinstated, all suspended bookings are brought back up with the status chosen by the user: this will either be
     * CONFIRMED (but then bookings might exceed capacity) or WAITING, in which case the user will have to go through and manually
     * change these to confirmed where possible.
     *
     * @param accountId
     * @param status
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     */
    public void setAccountStatus(int accountId, AccountStatus status, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException {

        BookingManager bookingManager = ChildBookingService.getInstance().getBookingManager();
        PropertiesService propertiesService = PropertiesService.getInstance();
        int btIdBit = propertiesService.getSystemProperties().getReinstateBTIdBit();
        // clean it to be just the identifier
        if(!BTIdBits.isValid(btIdBit, BTIdBits.TYPE_BOOKING)) {
            throw new NoSuchIdentifierException("The reinstateBookingType '" + BTIdBits.getAllStrings(btIdBit) + "' is not valid");
        }
        // loop each child on the account
        for(ChildBean child : getChildrenForAccount(accountId)) {
            if(status.equals(AccountStatus.SUSPENDED)) {
                bookingManager.setSuspendedBookingsForProfile(child.getChildId(), owner);
            } else {
                bookingManager.setReinstateBookingsForProfile(child.getChildId(), btIdBit, owner);
            }
        }
        // set account
        accountManager.setObjectStatus(accountId, status, owner);
    }

    /**
     * sets the account payment period. The payment period an enumerated type to indicate
     * if an account should be paid as per invoice or as a fixed amount over a predefined
     * time. The payment period value must zero or greater.
     * @param accountId
     * @param paymentPeriod
     * @param regularPaymentInstruction
     * @param paymentPeriodValue
     * @param owner
     * @throws IllegalValueException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @see PaymentPeriodEnum
     */
    public void setAccountPaymentPeriod(int accountId, PaymentPeriodEnum paymentPeriod, String regularPaymentInstruction, long paymentPeriodValue, String owner)throws IllegalValueException, NullObjectException, NoSuchIdentifierException, PersistenceException{
        // if the payment period is AS_INVOICE, tidy up the paymentValue
        if(paymentPeriod.equals(PaymentPeriodEnum.AS_INVOICE)) {
            regularPaymentInstruction = "";
            paymentPeriodValue = -1L;
        } else if(paymentPeriodValue < 0) {
            throw new IllegalValueException("The payment period value must be zero or greater");
        }
        accountManager.setObjectPaymentPeriod(accountId, paymentPeriod, regularPaymentInstruction, paymentPeriodValue, owner);
    }

    /**
     * Allows a child to be moved to a different account
     *
     * @param accountId
     * @param childId
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws MaxCountReachedException
     */
    public void setAccountForChild(int accountId, int childId, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, IllegalActionException, MaxCountReachedException {
        this.isId(childId, VT.CHILD);
        this.isId(accountId, VT.ACCOUNT);

        // change the associated accountId
        childManager.setObjectAccountId(childId, accountId, owner);
        //so it makes sense change the owner of the account so it shows who made changes
        accountManager.setObjectOwner(accountId, owner);

        int liableId = getLiableContactsForAccount(accountId).get(0).getContactId();
        int yesterday = YWDHolder.add(this.getTodayYwd(), -1);

        // copy any present & future booking requests (right refs) that this child uses, creating a map of old right ref to new right ref
        List<DayRangeBean> oldSchedule = ChildBookingRequestService.getInstance().getBookingRequestRangeManager().getAllObjects(childId);
        // need to start at the bottom
        Collections.reverse(oldSchedule);
        for(int i = oldSchedule.size() - 1; i >= 0; i--) {
            if(oldSchedule.get(i).getEndYwd() < this.getTodayYwd()) {
                oldSchedule.remove(i);
            }
        }

        Map<Integer, Integer> requestIds = new ConcurrentSkipListMap<>();

        BookingRequestBean oldBr;
        int newRangeId;
        int startYwd;
        for(DayRangeBean r : oldSchedule) {
            if(!requestIds.keySet().contains(r.getRightRef())) {
                oldBr = ChildBookingRequestService.getInstance().getBookingRequestManager().getObject(r.getRightRef());
                newRangeId = ChildBookingRequestService.getInstance().createBookingRequest(oldBr.getName(), oldBr.getLabel(), accountId, liableId, oldBr.getDropOffContactId(), oldBr.getPickupContactId(), oldBr.getAllSd(), oldBr.getBookingTypeId(), owner).getBookingRequestId();
                requestIds.put(r.getRightRef(), newRangeId);
            } else {
                newRangeId = requestIds.get(r.getRightRef());
            }
            // create a new range from today
            startYwd = r.getStartYwd() < this.getTodayYwd() ? this.getTodayYwd() : r.getStartYwd();
            ChildBookingRequestService.getInstance().createBookingRequestDayRange(childId, newRangeId, startYwd, r.getEndYwd(), "none", r.getDays(), owner);
            // remove the old day range for this child
            ChildBookingRequestService.getInstance().getBookingRequestRangeManager().removeObject(childId, r.getRangeId());

        }
    }

    /**
     * sets an account contact. if the account contact already exists it will be
     * overwritten with the new contact type.
     *
     * @param accountId
     * @param contactId
     * @param type
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     */
    public void setLegalAccountRelationship(int accountId, int contactId, RelationType type, String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException, IllegalActionException {
        if(contactManager.isIdentifier(contactId)
                && contactManager.getObject(contactId).getType().isNonProfessional()
                && accountManager.isIdentifier(accountId)
                && type.isAccounts()) {

            // Business Rule: if removing liability:
            if(!type.isLiable()) {
                // check there are no bookings with this contact as liable
                if(ChildBookingService.getInstance().getBookingManager().hasLiability(contactId)) {
                    throw new IllegalActionException("ContactIsLiableForBookings");
                }
                // check there are no booking requests with this contact as liable
                if(ChildBookingRequestService.getInstance().getBookingRequestManager().hasLiability(contactId)) {
                    throw new IllegalActionException("ContactIsLiableForBookingRequests");
                }
            }

            // ensure there will be at least one liable and at least one invoiceable contact after this set
            boolean liable = false;
            boolean invoiceable = false;

            // test new relationship
            RelationBean newRelation = new RelationBean(contactId, type, owner);
            if(newRelation.getType().isLiable()) {
                liable = true;
            }
            if(newRelation.getType().isInvoice()) {
                invoiceable = true;
            }
            // if the new relationship is both, no need to test the others
            if(!liable || !invoiceable) {
                for(RelationBean relation : legalAccountRelationship.getAllObjects(accountId)) {
                    if(relation.getProfileId() == contactId) {
                        relation = newRelation;
                    }
                    if(relation.getType().isLiable()) {
                        liable = true;
                    }
                    if(relation.getType().isInvoice()) {
                        invoiceable = true;
                    }
                }
            }
            if(liable && invoiceable) {
                legalAccountRelationship.setObject(accountId, newRelation);
                // reflect the change to the account by changing the account owner
                accountManager.setObjectOwner(accountId, owner);
                //reflect the changes to the contact by changing the contact owner
                contactManager.setObjectOwner(contactId, owner);
                return;
            }
            throw new IllegalActionException("MustRetainLiableAndInvoiceable");
        }
        throw new NoSuchIdentifierException("The contact with contactId " + contactId + " does not exist or is not a custodian");
    }

    /**
     * Sets an account contact (a "customer") with a contact type that is determined
     * by liability and invoice type choice made on a web page:
     * criteria are: liable, invoicePaperFull, invoicePaperSummary, invoiceEmail
     *
     * TODO sort out the shit code in getAccountType!!
     * @param accountId
     * @param contactId
     * @param criteria
     * @param owner
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setLegalAccountRelationship(int accountId, int contactId, List<Boolean> criteria, String owner) throws IllegalActionException, NoSuchIdentifierException, NullObjectException, PersistenceException {
        boolean liable = criteria.get(0);
        boolean invoicePaperFull = criteria.get(1);
        boolean invoicePaperSummary = criteria.get(2);
        boolean invoiceEmail = criteria.get(3);
        RelationType accountContactType = getAccountType(liable, invoicePaperFull, invoicePaperSummary, invoiceEmail);
        setLegalAccountRelationship(accountId, contactId, accountContactType, owner);
    }

    /**
     * Removes a a legal account relationship from an account if that is allowed
     *
     * @param accountId
     * @param contactId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void removeLegalAccountRelationship(int accountId, int contactId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {

        RelationType oldType = getAccountContactType(accountId, contactId);

        checkLegalAccountRelationshipRemovable(accountId, contactId, oldType);

        legalAccountRelationship.removeObject(accountId, contactId);
        //reflect the changes to the contact by changing the contact owner
        contactManager.setObjectOwner(contactId, owner);
        // reset owner of account as changes have happened
        accountManager.setObjectOwner(accountId, owner);
    }

    /**
     * If the account is eligible for removal (see checkAccountRemovable()):
     * Takes a snapshot of the account
     * Removes the account and all the contact relations with that account.
     *
     * @param accountId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void removeAccount(int accountId, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalActionException, NullObjectException {
        checkAccountRemovable(accountId);
        // remove the children
        for(ChildBean child : ChildService.getInstance().getChildrenForAccount(accountId)) {
            removeChild(child.getChildId(), owner);
        }
        // get all the guardians
        for(int contactId : legalAccountRelationship.getAllIdentifier(accountId)) {
            if(this.isLegalAccountRelationshipRemovable(accountId, contactId)) {
                this.removeLegalAccountRelationship(accountId, contactId, owner);
            }
            // if they are not used anywhere else then remove them.
            if(isNonProfessionalRemovable(contactId)) {
                this.removeNonProfessional(contactId);
            }
        }
        // account relations
        legalAccountRelationship.removeKey(accountId);
        // booking requests (the removal of each child removes the ranges for each child)
        ChildBookingRequestService.getInstance().removeAllBookingRequests(accountId);
        // remove all the transactions including billings
        TransactionService.getInstance().removeAccountTransactions(accountId);
        // remove the account
        accountManager.removeObject(accountId);
    }

    /**
     * Creates a new child profile with default settings. Note that the account legal guardians are automatically set
     * as the child's custodians
     * @param accountId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param phone
     * @param gender
     * @param dateOfBirth
     * @param departYwd set this to -1 if not known
     * @param photoId
     * @param notes
     * @param owner
     * @return
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     */
    public ChildBean createChild(int accountId,
            String title, List<String> forenames, String surname, String commonName,
            String line1, String line2, String line3, String city, String province, String postcode, String country,
            String phone,
            String gender,
            int dateOfBirth, int departYwd,
            int photoId,
            String notes, String owner)
            throws NoSuchIdentifierException,
            PersistenceException, NullObjectException, MaxCountReachedException, IllegalActionException {

        if(!accountManager.isIdentifier(accountId)) {
            throw new NoSuchIdentifierException("There is no account with id " + accountId);
        }

        // get the configService instance
        PropertiesService ps = PropertiesService.getInstance();
        AgeRoomService rs = AgeRoomService.getInstance();

        int childId = childManager.generateIdentifier();
        int holidayAllocation = ps.getHolidayConcession().getDefaultAllocation();
        if(departYwd == -1) {
            departYwd = YWDHolder.add(dateOfBirth, CalendarStatic.getApproxYW0(ps.getSystemProperties().getMaxAgeMonths()));
        }
        int noticeYwd = -1;
        // business rule: child cannot be older than max age months
        if(CalendarStatic.getAgeMonths(dateOfBirth) > ps.getSystemProperties().getMaxAgeMonths()) {
            throw new IllegalActionException("ChildTooOldForNursery");
        }
        // set new object
        ChildBean rtnChild = childManager.setObject(new ChildBean(childId, accountId, title, forenames,
                surname, commonName, line1, line2, line3, city, province, postcode,
                country, phone, gender, dateOfBirth, photoId, holidayAllocation,
                departYwd, noticeYwd, false, false, notes, owner));
        // set default start dates
        rs.setChildRoomStartDefault(rtnChild.getChildId(), owner);
        // set the account legal custodians to be the childs custodians
        for(ContactBean legal : getLiableContactsForAccount(accountId)) {
            setCustodialRelationship(childId, legal.getContactId(), RelationType.LEGAL_PARENT_GUARDIAN, owner);
        }
        // reset the owner of the account to reflect a change in the account
        accountManager.setObjectOwner(accountId, owner);
        return (rtnChild);
    }

    /**
     * Enables a child's personal details to be modified
     * @param childId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param gender
     * @param dateOfBirth
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setChildPersonalDetails(int childId,
            String title, List<String> forenames, String surname, String commonName,
            String gender, int dateOfBirth, String owner)
            throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        childManager.setObjectPersonalDetails(childId, title, forenames, surname, commonName, gender, dateOfBirth, owner);
    }

    /**
     * Enables a child's discount rates to be modified
     * @param childId
     * @param bookingDiscountRate
     * @param fixedItemDiscountRate
     * @param tariffDiscountOff
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setChildDiscounts(int childId, int bookingDiscountRate, int fixedItemDiscountRate, boolean tariffDiscountOff, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        childManager.setObjectDiscounts(childId, bookingDiscountRate, fixedItemDiscountRate, tariffDiscountOff, owner);
    }

    /**
     * Enables a child's holiday allocation to be modified
     * @param childId
     * @param holidayAllocation
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setChildHolidayAllocation(int childId, int holidayAllocation, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        childManager.setObjectHolidayAllocation(childId, holidayAllocation, owner);
    }

    /**
     * Allows a child's departure to be set cleanly.
     * Cancels any confirmed bookings after the departYwd but still within the notice period.
     * Removes all bookings after the departYwd and after the notice period.
     * Sets the child's departYwd and noticeYwd.
     *
     * @param childId
     * @param departYwd
     * @param noticeYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public void setChildDepart(int childId, int departYwd, int noticeYwd, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, NoSuchKeyException, MaxCountReachedException, IllegalActionException {
        // fetch all bookings for profile from departYwd + 1 ("after the departYwd")
        BookingManager bookingManager = ChildBookingService.getInstance().getBookingManager();
        BookingTypeManager bookingTypeManager = ChildBookingService.getInstance().getBookingTypeManager();

        if(!YWDHolder.isValid(noticeYwd)) {
            noticeYwd = departYwd;
        }
        int btIdMask = BTIdBits.TYPE_PHYSICAL_BOOKING | BTBits.WAITING_BIT;
        for(BookingBean booking : bookingManager.getBookingsFromYwdForProfile(YWDHolder.add(departYwd, 1), childId, btIdMask, ActionBits.TYPE_FILTER_OFF)) {
            // remove any waiting bookings
            if(BTIdBits.isFilter(booking.getBookingTypeId(), BTBits.WAITING_BIT)) {
                bookingManager.removeObject(booking.getYwd(), booking.getBookingId());
                continue;
            }
            // if there is no noticeYwd and it is state OPEN then remove
            if(noticeYwd == departYwd && booking.isState(BookingState.OPEN)) {
                bookingManager.removeObject(booking.getYwd(), booking.getBookingId());
                continue;
            }
            // cancel any PHYSICAL bookings within notice period and set to RESTRICTED
            if(bookingTypeManager.getObject(booking.getBookingTypeId()).isFlagOn(BTFlagBits.REGULAR_BOOKING_FLAG)
                    && AgeRoomService.getInstance().isWithinSpecialNoticePeriod(booking.getYwd(), noticeYwd, booking.getRoomId())) {
                // special booking notice
                BookingBean cancelBooking = bookingManager.setCancelledBooking(booking.getYwd(), booking.getBookingId(), owner);
                // if OPEN set to RESTRICTED
                if(cancelBooking.isState(BookingState.OPEN)){
                    bookingManager.setObjectState(cancelBooking.getYwd(), cancelBooking.getBookingId(), BookingState.RESTRICTED, owner);
                }
            } else if(AgeRoomService.getInstance().isWithinStandardNoticePeriod(booking.getYwd(), noticeYwd, booking.getRoomId())) {
                // standard booking
                BookingBean cancelBooking = bookingManager.setCancelledBooking(booking.getYwd(), booking.getBookingId(), owner);
                // if OPEN set to RESTRICTED
                if(cancelBooking.isState(BookingState.OPEN)){
                    bookingManager.setObjectState(cancelBooking.getYwd(), cancelBooking.getBookingId(), BookingState.RESTRICTED, owner);
                }
            } else {
                if(booking.isState(BookingState.OPEN)){
                    bookingManager.removeObject(booking.getYwd(), booking.getBookingId());
                } else {
                    try {
                        bookingManager.setCancelledBooking(booking.getYwd(), booking.getBookingId(), owner);
                    } catch(IllegalActionException iae) {
                        // ignore because the booking is Authorised or later.
                    }
                }
            }

        }
        // set the child departYwd: this prevents any new bookings or booking request insertions after this date
        childManager.setObjectDepartYwd(childId, departYwd, owner);
        // set the noticeYWd
        childManager.setObjectNoticeYwd(childId, noticeYwd, owner);
    }

    /**
     * Enables a child's photoId to be modified when a new photo is uploaded
     * @param childId
     * @param photoId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setChildPhotoId(int childId, int photoId, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        childManager.setObjectPhotoId(childId, photoId, owner);
    }

    /**
     * Enables a child profile to indicate if a medical form has been received
     * @param childId
     * @param medFormReceived
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     */
    public void setChildMedFormReceived(int childId, boolean medFormReceived, String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        childManager.setObjectMedFormReceived(childId, medFormReceived, owner);
    }

    /**
     * Enables a child's notes to be modified
     * @param childId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setChildNotes(int childId, String notes, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        childManager.setObjectNotes(childId, notes, owner);
    }

    /**
     * Enables a child's address to be modified
     * @param childId
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param phone
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setChildAddress(int childId, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String phone, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        childManager.setObjectAddress(childId, line1, line2, line3, city, province, postcode, country, phone, owner);
    }

    /**
     * If the child is eligible for removal (see checkChildRemovable()):
     * Takes a snapshot of the account
     * Removes the account and all the contact relations with that account.
     * Removes contacts unless they are custodians of other children.
     *
     * @param childId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public void removeChild(int childId, String owner) throws IllegalActionException, NoSuchIdentifierException, PersistenceException, NullObjectException {
        checkChildRemovable(childId);
        // reflect this change in the account by setting the owner.
        accountManager.setObjectOwner(getAccountForChild(childId).getAccountId(), owner);
        // get the custodians before the relationships are removed
        List<ContactBean> custodians = getAllCustodiansForChild(childId);
        // remove the relationship with any contacts
        custodialRelationship.removeKey(childId);
        // if associated guardians are not used elsewhere then remove
        for(ContactBean c : custodians) {
            if(isNonProfessionalRemovable(c.getContactId())) {
                removeNonProfessional(c.getContactId());
            }
        }
        // (schools and other professional contacts are retained)

        // remove all medical beans
        removeAllMedicalBeansForChild(childId);
        // remove all room allocations
        AgeRoomService.getInstance().getChildRoomStartManager().removeKey(childId);
        // remove booking request ranges
        ChildBookingRequestService.getInstance().removeAllBookingRequestDayRangeForChild(childId);
        // remove all booking for the child
        ChildBookingService.getInstance().removeAllBookingsForChild(childId);
        // child education ranges
        childEducationDayRange.removeKey(childId);
        // remove the child bean
        childManager.removeObject(childId);
    }

    /**
     * Sets a child's requirements for education
     * @param childId  leftRef
     * @param cetId  rightRef
     * @param startYwd
     * @param endYwd
     * @param days
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public DayRangeBean createChildEducationDayRange(int childId, int cetId, int startYwd, int endYwd, boolean[] days,
            String owner) throws NullObjectException, PersistenceException, MaxCountReachedException {

        return (childEducationDayRange.setObject(childId,
                new DayRangeBean(childEducationDayRange.generateIdentifier(), childId, cetId, startYwd, endYwd, days, owner)));
    }

    /**
     * Terminates a child's education day range early, if the endYwd required is before the existing one.
     * If the range starts after the new endYwd, it is removed entirely.
     * Allows old ranges to be retained for ref integrity although the user cannot see them.
     *
     * @param leftRef
     * @param rangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void terminateChildEducationDayRange(int leftRef, int rangeId, int endYwd, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        if(endYwd < childEducationDayRange.getObject(leftRef, rangeId).getStartYwd()) {
            childEducationDayRange.removeObject(leftRef, rangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < childEducationDayRange.getObject(leftRef, rangeId).getEndYwd()) {
            childEducationDayRange.setObjectEndYwd(leftRef, rangeId, endYwd, owner);
        }
    }

    /**
     * removes a Child Education Day Range
     *
     * @param leftRef
     * @param rangeId
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void removeChildEducationDayRange(int leftRef, int rangeId) throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        childEducationDayRange.removeObject(leftRef, rangeId);
    }

    /**
     * Moves one of a child's education day ranges up or down in the layers
     * @param childId
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
    public List<ChildEducationTimetableBean> moveChildEducationDayRange(int childId, int rangeId, int viewYw0,
            int startYwd, boolean moveUp, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, MaxCountReachedException, NoSuchKeyException {
        TimetableService ts = TimetableService.getInstance();
        LinkedList<ChildEducationTimetableBean> rtnList = new LinkedList<>();
        for(int id : childEducationDayRange.moveObject(childId, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(ts.getChildEducationTimetableManager().getObject(id));
        }
        return (rtnList);
    }

    /**
     * Creates a new medical bean
     * @param childId this can be -1 if the med is a template
     * @param name
     * @param type
     * @param description
     * @param instruction
     * @param trigger
     * @param reviewYwd
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public MedicalBean createMedicalBean(int childId, String name, MedicalType type, String description,
            String instruction, int trigger, int reviewYwd, String owner) throws NullObjectException, PersistenceException, MaxCountReachedException {
        return (medicalManager.setObject(
                new MedicalBean(medicalManager.generateIdentifier(), childId, name, type, description, instruction, trigger, reviewYwd, owner)));
    }

    /**
     * Enables an existing medical bean to be modified
     * @param medId
     * @param childId this can be -1 if the med is a template
     * @param name
     * @param type
     * @param description
     * @param instruction
     * @param trigger
     * @param reviewYwd
     * @param owner
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setMedicalBean(int medId, int childId, String name, MedicalType type, String description,
            String instruction, int trigger, int reviewYwd, String owner) throws NullObjectException, PersistenceException, NoSuchIdentifierException {
        if(!medicalManager.isIdentifier(medId)) {
            throw new NoSuchIdentifierException("MedicalBean " + medId + " does not exist");
        }
        if(childId != -1 && !childManager.isIdentifier(childId)) {
            throw new NoSuchIdentifierException("Child " + childId + " does not exist");
        }
        medicalManager.setObject(new MedicalBean(medId, childId, name, type, description, instruction, trigger, reviewYwd, owner));
    }

    /**
     * Removes a medical bean
     * @param medId
     * @throws PersistenceException
     */
    public void removeMedicalBean(int medId) throws PersistenceException {
        medicalManager.removeObject(medId);
    }

    /**
     * Removes all medical beans for a child, regardless of type
     * @param childId
     * @throws PersistenceException
     */
    public void removeAllMedicalBeansForChild(int childId) throws PersistenceException {
        LinkedList<MedicalBean> medList = new LinkedList<>();
        for(MedicalBean med : medicalManager.getAllObjects()) {
            if(med.getChildId() == childId) {
                medList.add(med);
            }
        }
        for(MedicalBean m : medList) {
            medicalManager.removeObject(m.getMedId());
        }
    }

    /**
     * Creates a new ContactBean. WARNING the availableEndIn is the
     * true availableEnd + 1
     *
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param business
     * @param phone
     * @param phone2
     * @param mobile
     * @param email
     * @param photoId
     * @param availableStart
     * @param availableEndIn available end + 1
     * @param type
     * @param notes
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     * @throws IllegalActionException
     */
    public ContactBean createContact(String title, List<String> forenames, String surname, String commonName,
            String line1, String line2, String line3, String city, String province, String postcode, String country,
            String business,
            String phone, String phone2, String mobile, String email,
            int photoId,
            int availableStart, int availableEndIn,
            RelationType type,
            String notes, String owner)
            throws NullObjectException, PersistenceException, MaxCountReachedException, IllegalActionException {

        int availableEnd = availableEndIn - 1;
        validateAvailability(availableStart, availableEnd);
        return (contactManager.setObject(new ContactBean(contactManager.generateIdentifier(),
                title, forenames, surname, commonName,
                line1, line2, line3, city, province, postcode, country,
                business,
                phone, phone2, mobile, email,
                photoId,
                availableStart, availableEnd,
                type,
                notes, owner)));
    }

    /**
     * Enables a contact's personal details to be modified. WARNING the available end time
     * it the true end time + 1
     * @param contactId
     * @param title
     * @param forenames
     * @param surname
     * @param commonName
     * @param business
     * @param availableStart
     * @param availableEndIn the available end time + 1
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void setContactPersonalDetails(int contactId,
            String title, List<String> forenames, String surname, String commonName,
            String business, int availableStart, int availableEndIn, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        int availableEnd = availableEndIn - 1;
        validateAvailability(availableStart, availableEnd);
        contactManager.setObjectPersonalDetails(contactId,
                title, forenames, surname, commonName, business, availableStart, availableEnd, owner);
    }

    protected void validateAvailability(int availableStart, int availableEnd) {
        if(availableStart < 0 || availableStart > BookingManager.MAX_PERIOD_LENGTH) {
            throw new IllegalArgumentException("Invalid start time, was " + availableStart);
        }
        if(availableEnd < 0 || availableEnd > BookingManager.MAX_PERIOD_LENGTH) {
            throw new IllegalArgumentException("Invalid end time, was " + availableEnd);
        }
        if(availableEnd < availableStart) {
            throw new IllegalArgumentException("End time is before start, start=" + availableStart + ", end=" + availableEnd);
        }
    }


    /**
     * Enables a contact's address to be modified
     * @param contactId
     * @param line1
     * @param line2
     * @param line3
     * @param city
     * @param province
     * @param postcode
     * @param country
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setContactAddress(int contactId,
            String line1, String line2, String line3, String city, String province, String postcode, String country,
            String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        contactManager.setObjectAddress(contactId, line1, line2, line3, city, province, postcode, country, owner);
    }

    /**
     * Enables a contact's photoId to be modified when a new photo is uploaded
     * @param contactId
     * @param photoId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setContactPhotoId(int contactId, int photoId, String owner)
            throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        contactManager.setObjectPhotoId(contactId, photoId, owner);
    }

    /**
     * Enables a contact's communications to be modified
     * @param contactId
     * @param phone
     * @param phone2
     * @param mobile
     * @param email
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setContactCommunications(int contactId, String phone, String phone2, String mobile, String email,
            String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        contactManager.setObjectCommunications(contactId, phone, phone2, mobile, email, owner);
    }

    /**
     * Enables a contact's notes to be modified
     * @param contactId
     * @param notes
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public void setContactNotes(int contactId, String notes, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException {
        contactManager.setObjectNotes(contactId, notes, owner);
    }

    /**
     * remove a professional contact from the contact manager
     *
     * @param contactId
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void removeProfessional(int contactId)
            throws IllegalActionException, NoSuchIdentifierException, PersistenceException {
        checkProfessionalRemovable(contactId);
        contactManager.removeObject(contactId);
    }

    /**
     * remove a non-professional contact from the contact manager
     *
     * @param contactId
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void removeNonProfessional(int contactId) throws IllegalActionException, NoSuchIdentifierException, PersistenceException {
        checkNonProfessionalRemovable(contactId);
        contactManager.removeObject(contactId);
    }

    /**
     * Sets up a custodial relationship between a nonprofessional contact and a
     *
     * @param childId
     * @param contactId
     * @param type
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     */
    public void setCustodialRelationship(int childId, int contactId, RelationType type, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException {
        checkProfile(-1, childId, -1, -1);
        custodialRelationship.setObject(childId, new RelationBean(contactId, type, owner));
    }

    /**
     * Removes the custodial relationship of a nonprofessional with a child. If the nonprofessional
     * has no other custodial or legal account relationships then the the ContactBean Object is also
     * removed
     *
     * @param childId
     * @param contactId
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void removeCustodialRelationship(int childId, int contactId) throws IllegalActionException, NoSuchIdentifierException, PersistenceException {
        // no checks need to be done on removing a custodial relationship
        if(!custodialRelationship.getObject(childId, contactId).getType().isProfessional() ) {
            custodialRelationship.removeObject(childId, contactId);
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
     * Holds the business rules that need to be met before an account is eligible for removal
     */
    protected void checkAccountRemovable(int accountId) throws IllegalActionException, PersistenceException, NoSuchIdentifierException {
        TransactionService transactionService = TransactionService.getInstance();

        // check transactions are removable
        if(!transactionService.isAccountTransactionsRemovable(accountId)) {
            throw new IllegalActionException("TransactionsOutstanding");
        }
        // check each child is removable
        for(ChildBean child : this.getChildrenForAccount(accountId)) {
            checkChildRemovable(child.getChildId());
        }
    }

    /*
     * Holds the business rules that need to be met before a child is eligible for removal
     */
    protected void checkChildRemovable(int childId) throws NoSuchIdentifierException, PersistenceException, IllegalActionException {
        ChildBookingService bookingService = ChildBookingService.getInstance();

        // child's depart ywd must be in the past (Look to the start of the week)
        if(CalendarStatic.getRelativeYW(0) < childManager.getObject(childId).getDepartYwd()) {
            throw new IllegalActionException("ChildNotDeparted");
        }
        // from the previous 8 weeks there should only be archived bookings.
        for(BookingBean booking : bookingService.getBookingManager().getBookingsFromYwdForProfile(CalendarStatic.getRelativeYW(-8), childId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF)) {
            if(!booking.isState(BookingState.ARCHIVED)) {
                throw new IllegalActionException("BookingsNotAchived");
            }
        }
    }

    /*
     * Holds the business rules that make an account contact eligible for removal from the account
     */
    private void checkLegalAccountRelationshipRemovable(int accountId, int contactId, RelationType oldType) throws NoSuchIdentifierException, PersistenceException, IllegalActionException {

        if(contactManager.isIdentifier(contactId)
                && contactManager.getObject(contactId).getType().isNonProfessional()
                && accountManager.isIdentifier(accountId)) {

            // Business Rule: if removing liability:
            if(oldType.isLiable()) {
                // check there are no bookings with this contact as liable
                if(ChildBookingService.getInstance().getBookingManager().hasLiability(contactId)) {
                    throw new IllegalActionException("ContactIsLiableForBookings");
                }
                // check there are no booking requests with this contact as liable
                if(ChildBookingRequestService.getInstance().getBookingRequestManager().hasLiability(contactId)) {
                    throw new IllegalActionException("ContactIsLiableForBookingRequests");
                }
            }
            boolean liable = false;
            boolean invoiceable = false;
            boolean found = false;
            for(RelationBean relation : legalAccountRelationship.getAllObjects(accountId)) {
                if(relation.getProfileId() == contactId) {
                    found = true;
                    continue;
                }
                if(relation.getType().isLiable()) {
                    liable = true;
                }
                if(relation.getType().isInvoice()) {
                    invoiceable = true;
                }
            }
            if(!found) {
                throw new NoSuchIdentifierException("AccountDoesNotHoldContact");
            }
            if(!(liable && invoiceable)) {
                throw new IllegalActionException("MustRetainLiableAndInvoiceable");
            }
        } else {
            throw new NoSuchIdentifierException("InvalidAccountContact");
        }
    }

    /*
     * Validation that need to be met before a professional contact is eligible for removal
     */
    private void checkProfessionalRemovable(int contactId) throws IllegalActionException, NoSuchIdentifierException, PersistenceException {
        if(!contactManager.isIdentifier(contactId)) {
            throw new NoSuchIdentifierException("InvalidContactId");
        }
        if(legalAccountRelationship.isIdentifier(contactId)) {
            throw new IllegalActionException("ContactHasLegalAccountRelationship");
        }
        if(custodialRelationship.isIdentifier(contactId)) {
            throw new IllegalActionException("ContactHasCustodialRelationship");
        }
        if(contactManager.getObject(contactId).getType().isNonProfessional()) {
            throw new IllegalActionException("ContactNotProfessional");
        }
    }

    /*
     * validation that need to be met before a non-professional contact is eligible for removal
     */
    private void checkNonProfessionalRemovable(int contactId) throws IllegalActionException, NoSuchIdentifierException, PersistenceException {
        if(!contactManager.isIdentifier(contactId)) {
            throw new NoSuchIdentifierException("InvalidContactId");
        }
        if(legalAccountRelationship.isIdentifier(contactId)) {
            throw new IllegalActionException("ContactHasLegalAccountRelationship");
        }
        if(custodialRelationship.isIdentifier(contactId)) {
            throw new IllegalActionException("ContactHasCustodialRelationship");
        }
        if(contactManager.getObject(contactId).getType().isProfessional()) {
            throw new IllegalActionException("ContactIsProfessional");
        }
    }

    /*
     * Determines the account type given the criteria to work it out from
     * TODO this code is shit!
     */
    private RelationType getAccountType(boolean liable, boolean invoicePaperFull, boolean invoicePaperSummary,
            boolean invoiceEmail) {
        if(invoicePaperFull && invoicePaperSummary) {
            invoicePaperSummary = false;
        }
        if(liable) {
            if(invoicePaperFull) {
                if(invoiceEmail) {
                    return RelationType.LIABLE_INVOICE_EMAIL_FULL;
                } else {
                    return RelationType.LIABLE_INVOICE_FULL;
                }
            }
            if(invoicePaperSummary) {
                if(invoiceEmail) {
                    return RelationType.LIABLE_INVOICE_EMAIL_SUM;
                } else {
                    return RelationType.LIABLE_INVOICE_SUM;
                }
            }
            if(invoiceEmail) {
                return RelationType.LIABLE_INVOICE_EMAIL;
            }
            return RelationType.LIABLE;
        } else {
            if(invoicePaperFull) {
                if(invoiceEmail) {
                    return RelationType.INVOICE_EMAIL_FULL;
                } else {
                    return RelationType.INVOICE_FULL;
                }
            }
            if(invoicePaperSummary) {
                if(invoiceEmail) {
                    return RelationType.INVOICE_EMAIL_SUM;
                } else {
                    return RelationType.INVOICE_SUM;
                }
            }
            return RelationType.INVOICE_EMAIL;
        }
    }

    /*
     * reduced name for commonly used get todays date as a ywd
     */
    private int getTodayYwd() throws PersistenceException {
        return CalendarStatic.getRelativeYWD(0);
    }
    //</editor-fold>

}
