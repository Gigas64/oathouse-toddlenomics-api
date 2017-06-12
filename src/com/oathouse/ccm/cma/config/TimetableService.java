/*
 * @(#)TimetableService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.config.DayRangeBean;
import com.oathouse.ccm.cos.config.DayRangeManager;
import com.oathouse.ccm.cos.config.TimetableBean;
import com.oathouse.ccm.cos.config.TimetableManager;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableManager;
import com.oathouse.ccm.cma.exceptions.BusinessRuleException;
import com.oathouse.oss.storage.exceptions.DuplicateIdentifierException;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NoSuchKeyException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code TimetableService} Class is a encapsulated API, service level singleton that provides
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
public class TimetableService {
    // Singleton Instance

    private volatile static TimetableService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    // config
    private final TimetableManager timetableManager = new TimetableManager(VT.TIMETABLE.manager());
    private final DayRangeManager timetableDayRange = new DayRangeManager(VT.TIMETABLE_RANGE.manager());
    // config.education
    private final ChildEducationTimetableManager childEducationTimetableManager = new ChildEducationTimetableManager(VT.TIMETABLE_EDUCATION.manager());
    private final DayRangeManager timetableEducationDayRange = new DayRangeManager(VT.TIMETABLE_EDUCATION_RANGE.manager());

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private TimetableService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code TimetableService} class
     * @return instance of the {@code TimetableService}
     * @throws PersistenceException
     */
    public static TimetableService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (TimetableService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new TimetableService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code TimetableService} class has been initialised. This is used
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
     * initialise all the managed classes in the {@code TimetableService}. The
     * method returns an instance of the {@code TimetableService} so it can be chained.
     * This must be called before a the {@code TimetableService} is used. e.g  {@code TimetableService myTimetableService = }
     * @return instance of the {@code TimetableService}
     * @throws PersistenceException
     */
    public synchronized TimetableService init() throws PersistenceException {
        if(initialise) {
            //config
            timetableDayRange.init(AgeRoomService.getInstance().getRoomConfigManager().getAllIdentifier());
            timetableManager.init();
            // education
            childEducationTimetableManager.init();
            Set<Integer> nurseryLeftRef = new ConcurrentSkipListSet<>();
            nurseryLeftRef.add(ChildEducationTimetableManager.ALL_ROOMS);
            timetableEducationDayRange.init(nurseryLeftRef);
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code TimetableService}. The
     * method returns an instance of the {@code TimetableService} so it can be chained.
     * @return instance of the {@code TimetableService}
     * @throws PersistenceException
     */
    public TimetableService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code TimetableService}. This is
     * generally used for testing. If you wish to refresh the object store
     * reInitialise() should be used.
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = timetableDayRange.clear() ? success : false;
        success = timetableManager.clear() ? success : false;
        success = childEducationTimetableManager.clear() ? success : false;
        success = timetableEducationDayRange.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory.
     * <p>
     * Used to reset the {@code TimetableService} class instance by setting the INSTANCE reference
     * to null. This is mostly used for testing to clear and reset internal memory stores
     * when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Manager Retrieval">
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

    public ChildEducationTimetableManager getChildEducationTimetableManager() {
        return childEducationTimetableManager;
    }

    public DayRangeManager getDayRangeManager() {
        return timetableDayRange;
    }

    public DayRangeManager getEducationRangeManager() {
        return timetableEducationDayRange;
    }

    public TimetableManager getTimetableManager() {
        return timetableManager;
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
            case TIMETABLE:
                return timetableManager.isIdentifier(id);
            case TIMETABLE_EDUCATION:
                return childEducationTimetableManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    public void validatePeriodSd(int ywd, int periodSd, int roomId, int childId, boolean includeChildEducation) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        //find if the child has an education timetable
        TimetableBean timetable;
        if(includeChildEducation && childId > 0 && ChildService.getInstance().isDayChildEducationTimetable(childId, ywd)) {
            timetable = this.getTimetableWithChildEducationSd(ywd, roomId, childId);
        } else {
            timetable = this.getTimetable(ywd, roomId);
        }
        //test the timetable
        if(timetable.getTimetableId() == TimetableManager.CLOSED_ID) {
            throw new IllegalArgumentException("YwdRoomClosed");
        }
        if(timetable.getValidSd(periodSd, 0) != periodSd) {
            throw new IllegalArgumentException("YwdPeriodInvalid");
        }
    }

    /**
     * Tests whether a timetable is eligible for removal
     * @param timetableId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isTimetableRemovable(int timetableId) throws PersistenceException, NoSuchIdentifierException {
        // check the day range
        if(timetableDayRange.getLeftRefForRightRef(timetableId).isEmpty()) {
            return true;
        }
        return false;
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

    /* ************************************
     *  T I M E T A B L E
     * ************************************/
    /**
     * returns a timetable for a given ywd and room.
     *
     * @param ywd
     * @param room
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public TimetableBean getTimetable(int ywd, int room) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        return (timetableManager.getObject(timetableDayRange.getDayRange(room, ywd).getRightRef()));
    }

    /**
     * a helper method that tests if a ywd and room falls on a closed day
     *
     * @param ywd
     * @param room
     * @return true if closed, false if not
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public boolean isClosed(int ywd, int room) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        return (getTimetable(ywd, room).getTimetableId() == TimetableManager.CLOSED_ID ? true : false);
    }

    /**
     * Finds if all the nursery is closed
     * @param ywd
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public boolean isClosed(int ywd) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        for(int room : AgeRoomService.getInstance().getAgeRangeManager().getAllIdentifier()) {
            if(!this.isClosed(ywd, room)) {
                return (false);
            }
        }
        return (true);
    }

    /**
     * Determines whether there are any education sessions programmed by the nursery
     * @return
     * @throws PersistenceException
     */
    public boolean isNurseryProvidingEducation() throws PersistenceException {
        return !timetableEducationDayRange.getAllObjects().isEmpty();
    }

    /**
     * Determines whether a ywd is an "education day" for the nursery as a whole
     *
     * @param ywd
     * @return
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public boolean isEducationDay(int ywd) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        return (timetableEducationDayRange.getDayRange(ChildEducationTimetableManager.ALL_ROOMS, ywd).getRightRef() == ChildEducationTimetableManager.ALL_ROOMS);
    }

    /**
     * Fetches all education day ranges for a particular week
     * @param ywd
     * @return
     * @throws PersistenceException
     */
    public List<DayRangeBean> getEducationRangesForWeek(int ywd) throws PersistenceException {
        // ensure a yw0 not a ywd
        int yw0 = YWDHolder.getYW(ywd);
        return (timetableEducationDayRange.getAllObjects(ChildEducationTimetableManager.ALL_ROOMS, yw0, yw0 + 6));
    }

    /**
     * Tests whether a timetable is eligible for removal
     * @param timetableId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public boolean isChildEducationTimetableRemovable(int timetableId) throws PersistenceException, NoSuchIdentifierException {
        int today = CalendarStatic.getRelativeYWD(0);
        // all child education day ranges must be in the past
        ChildService cs = ChildService.getInstance();
        for(int childId : cs.getChildEducationRangeManager().getLeftRefForRightRef(timetableId)) {
            // all day ranges must be in the past
            if(!cs.getChildEducationRangeManager().getAllObjects(childId, today, 9999010).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fetches the appropriate child education timetable chosen by a child on a ywd,
     * or the default object if (a) the nursery has not programmed education on that ywd or (b) the child has not
     * chosen any education on that day
     * @param childId
     * @param ywd
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public ChildEducationTimetableBean getChildEducationTimetable(int childId, int ywd) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        ChildService cs = ChildService.getInstance();
        if(isEducationDay(ywd)) {
            return (childEducationTimetableManager.getObject(cs.getChildEducationRangeManager().getDayRange(childId, ywd).getRightRef()));
        }
        return (childEducationTimetableManager.getDefaultObject());
    }

    /**
     * Calculates the number of education sessions that a child is programmed to receive on days when the nursery provides education.
     * This is used to indicate the level of uptake for a child. [In Scotland (2011), ante-pre-school
     * and pre-school children are entitled to 5 sessions a week]
     *
     * @param childId
     * @param ywd
     * @return
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public int getChildEducationSessionCountForWeek(int childId, int ywd) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        ChildService cs = ChildService.getInstance();

        int yw0 = YWDHolder.getYW(ywd);
        int count = 0;
        // count through week
        for(int day = 0; day < YWDHolder.DAYS_IN_WEEK; day++) {
            if(isEducationDay(yw0 + day)) {
                count += childEducationTimetableManager.getObject(cs.getChildEducationRangeManager().getDayRange(childId, yw0 + day).getRightRef()).getSessionCount();
            }
        }
        return (count);
    }

    /**
     * Merges a child's education requirements with the appropriate timetable for the room and ywd
     * @param ywd
     * @param roomId
     * @param childId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    public TimetableBean getTimetableWithChildEducationSd(int ywd, int roomId, int childId) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        TimetableBean timetable = this.getTimetable(ywd, roomId);
        ChildEducationTimetableBean childEducationTimetable = getChildEducationTimetable(childId, ywd);
        // merge the two timetables
        return (timetableManager.setTemporaryTimetableMerge(timetable, childEducationTimetable.getAllEducationSd(), ObjectBean.SYSTEM_OWNED));
    }

    /**
     * returns the rightRef for each of the days of the week for a leftRef. If no DayRangeBean is found the
     * DayRange.NO_RANGE is added.
     *
     * @param room
     * @param ywd
     * @return a List of rightRef integers for the week
     * @throws NoSuchIdentifierException
     * @throws PersistenceException

     */
    public List<TimetableBean> getTimetablesForWeekRoom(int room, int ywd) throws NoSuchIdentifierException, PersistenceException {
        LinkedList<TimetableBean> rtnList = new LinkedList<>();

        int yw0 = YWDHolder.getYW(ywd);
        for(int id : timetableDayRange.getRightRefForWeekLeftRef(room, yw0)) {
            rtnList.add(timetableManager.getObject(id));
        }
        return (rtnList);
    }

    /**
     * returns the rightRef for each of the days of the week. If no DayRangeBean is found the
     * DayRange.NO_RANGE is added.
     *
     * @param ywd
     * @return a Map of the week
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public Map<Integer, List<TimetableBean>> getAllTimetablesForWeek(int ywd) throws NoSuchIdentifierException, PersistenceException {
        Map<Integer, List<TimetableBean>> rtnMap = new ConcurrentSkipListMap<>();

        int yw0 = YWDHolder.getYW(ywd);
        for(int room : timetableDayRange.getAllKeys()) {
            rtnMap.put(room, this.getTimetablesForWeekRoom(room, yw0));
        }
        return (rtnMap);
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
     * Sets a timetable if it is not being used in the DRM
     * @param timetableId
     * @param name
     * @param label
     * @param periodSdAndOptions
     * @param owner
     * @return the newly created timetable
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws DuplicateIdentifierException
     * @throws NullObjectException
     */
    public TimetableBean setTimetable(int timetableId, String name, String label,
            Map<Integer, Set<Integer>> periodSdAndOptions, String owner) throws MaxCountReachedException, PersistenceException, NoSuchIdentifierException, DuplicateIdentifierException, NullObjectException {
        if(timetableId == -1) {
            timetableId = timetableManager.generateIdentifier();
        } else if(!isTimetableRemovable(timetableId)) {
            throw new DuplicateIdentifierException("The timetableId " + timetableId + " already exists and is in use");
        }
        return (timetableManager.setObject(new TimetableBean(timetableId, name, label, periodSdAndOptions, owner)));
    }

    /**
     * Sets a timetable day range
     * @param roomId
     * @param timetableId
     * @param startYwd
     * @param endYwd
     * @param days
     * @param username
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public DayRangeBean createTimetableDayRange(int roomId, int timetableId, int startYwd, int endYwd, boolean[] days,
            String username)
            throws NullObjectException, PersistenceException, MaxCountReachedException {
        return (timetableDayRange.setObject(roomId,
                new DayRangeBean(timetableDayRange.generateIdentifier(), roomId, timetableId, startYwd, endYwd, days, username)));
    }

    /**
     * removes a timetable day range entirely from the system.
     *
     * @param leftRef
     * @param rangeId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws PersistenceException
     */
    public void removeTimetableDayRange(int leftRef, int rangeId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        timetableDayRange.removeObject(leftRef, rangeId);
    }

    /**
     * Terminates a day range early, if the endYwd required is before the existing one.
     * If the range starts after the new endYwd, it is removed entirely.
     * Allows old ranges to be retained for ref integrity although the user cannot see them.
     *
     * @param leftRef
     * @param rangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalActionException
     * @throws PersistenceException
     */
    public void terminateTimetableDayRange(int leftRef, int rangeId, int endYwd, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException, IllegalActionException {
        if(endYwd < timetableDayRange.getObject(leftRef, rangeId).getStartYwd()) {
            timetableDayRange.removeObject(leftRef, rangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < timetableDayRange.getObject(leftRef, rangeId).getEndYwd()) {
            timetableDayRange.setObjectEndYwd(leftRef, rangeId, endYwd, owner);
        }
    }

    /**
     * Used to move {@code DayRangeBean} Objects down layers. {@code DayRange} Objects
     * are moved from their current layer to LowLayer and all affected {@code DayRange} Objects
     * are moved up one.
     *
     * @param room
     * @param owner the owner of the change
     * @param viewYw0
     * @param startYwd
     * @param rangeId
     * @param moveUp the direction to be moved true = up, false = down
     * @return the new ordered List of DayRange
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public List<TimetableBean> moveTimetableDayRange(int room, int rangeId, int viewYw0, int startYwd, boolean moveUp,
            String owner) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, NullObjectException, MaxCountReachedException {
        LinkedList<TimetableBean> rtnList = new LinkedList<>();
        for(int id : timetableDayRange.moveObject(room, rangeId, viewYw0, startYwd, moveUp, owner)) {
            rtnList.add(timetableManager.getObject(id));
        }
        return (rtnList);
    }

    /**
     * This method should be used instead of timetableManager.removeObject() as this higher level method
     * checks for the use of a timetable in the dayRangeManager before removing.
     *
     * @param timetableId the id of the timetable to be removed
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws BusinessRuleException
     */
    public void removeTimetable(int timetableId) throws PersistenceException, NoSuchIdentifierException, BusinessRuleException {
        if(!isTimetableRemovable(timetableId)) {
            throw new BusinessRuleException("TimetableIsAssigned");
        }
        if(this.isId(timetableId, VT.TIMETABLE)) {
            timetableManager.removeObject(timetableId);
        }
    }

    /**
     * Sets a ChildEducationTimetableBean
     * @param cetId
     * @param name
     * @param label
     * @param roomId
     * @param educationSd
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws NoSuchIdentifierException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     * @throws PersistenceException
     */
    public ChildEducationTimetableBean setChildEducationTimetable(int cetId, String name, String label, int roomId,
            Set<Integer> educationSd, String owner) throws PersistenceException, NoSuchIdentifierException, MaxCountReachedException, DuplicateIdentifierException, NullObjectException {
        ChildBookingService bs = ChildBookingService.getInstance();
        AgeRoomService rs = AgeRoomService.getInstance();
        // the roomId has to be either "all rooms" or a valid room id
        if(roomId != ChildEducationTimetableManager.ALL_ROOMS && !rs.isId(roomId, VT.ROOM_CONFIG)) {
            throw new NoSuchIdentifierException("Invalid roomId " + roomId);
        }
        if(cetId == -1) {
            cetId = childEducationTimetableManager.generateIdentifier();
        } else if(!isChildEducationTimetableRemovable(cetId)) {
            throw new DuplicateIdentifierException("The ChildEducationTimetableId " + cetId + " already exists and is in use");
        }
        return childEducationTimetableManager.setObject(new ChildEducationTimetableBean(cetId, name, label, roomId, educationSd, owner));
    }

    /**
     * This method should be used instead of timetableManager.removeObject() as this higher level method
     * checks for the use of a timetable in the dayRangeManager before removing.
     *
     * @param timetableId the id of the timetable to be removed
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws BusinessRuleException
     */
    public void removeChildEducationTimetable(int timetableId)
            throws PersistenceException, NoSuchIdentifierException, BusinessRuleException {
        if(!isChildEducationTimetableRemovable(timetableId)) {
            throw new BusinessRuleException("TimetableIsAssigned");
        }
        if(this.isId(timetableId, VT.TIMETABLE_EDUCATION)) {
            childEducationTimetableManager.removeObject(timetableId);
        }
    }

    /**
     * Sets a range of education days for the nursery as a whole.
     * The leftRef is the defaultKey (-2).
     * The rightRef is also the defaultKey (-2), indicating that this is an education day.
     * The default object has id (0) indicating that this day is not an education day
     * @param startYwd
     * @param endYwd
     * @param days
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public DayRangeBean createEducationDayRange(int startYwd, int endYwd, boolean[] days, String owner) throws MaxCountReachedException, NullObjectException, PersistenceException {
        return (timetableEducationDayRange.setObject(ChildEducationTimetableManager.ALL_ROOMS,
                new DayRangeBean(timetableEducationDayRange.generateIdentifier(),
                ChildEducationTimetableManager.ALL_ROOMS, ChildEducationTimetableManager.ALL_ROOMS,
                startYwd, endYwd, days, owner)));
    }

    /**
     * Terminates a day range early, if the endYwd required is before the existing one.
     * If the range starts after the new endYwd, it is removed entirely.
     * Allows old ranges to be retained for ref integrity although the user cannot see them.
     *
     * @param rangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NullObjectException
     */
    public void terminateEducationDayRange(int rangeId, int endYwd, String owner) throws NoSuchIdentifierException, PersistenceException, IllegalActionException, NullObjectException {
        if(endYwd < timetableEducationDayRange.getObject(ChildEducationTimetableManager.ALL_ROOMS, rangeId).getStartYwd()) {
            timetableEducationDayRange.removeObject(ChildEducationTimetableManager.ALL_ROOMS, rangeId);
            return;
        }
        // only allows end date to be brought earlier; cannot set later than it already is
        if(endYwd < timetableEducationDayRange.getObject(ChildEducationTimetableManager.ALL_ROOMS, rangeId).getEndYwd()) {
            timetableEducationDayRange.setObjectEndYwd(ChildEducationTimetableManager.ALL_ROOMS, rangeId, endYwd, owner);
        }
    }

    /**
     * Removes an education range from the educationRangeManager
     * @param rangeId
     * @throws PersistenceException
     */
    public void removeEducationDayRange(int rangeId) throws PersistenceException {
        if(timetableEducationDayRange.isIdentifier(rangeId)) {
            timetableEducationDayRange.removeObject(ChildEducationTimetableManager.ALL_ROOMS, rangeId);
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
    //</editor-fold>
}
