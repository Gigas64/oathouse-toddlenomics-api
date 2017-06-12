/*
 * @(#)AgeRoomService.java
 *
 * Copyright:	Copyright (c) 2011
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cma.*;
import com.oathouse.ccm.cma.booking.*;
import com.oathouse.ccm.cma.profile.*;
import com.oathouse.ccm.cos.bookings.*;
import com.oathouse.ccm.cos.config.*;
import com.oathouse.ccm.cos.config.finance.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;

/**
 * The {@code AgeRoomService} Class is a encapsulated API, service level singleton that provides
 * access to related underlying manager instances. The class provides three distinct sections:
 * <h2>Manager Retrieval</h2> <p> This section allows the retrieval of the instance of the manager
 * to use as part of this service. For ease of reference each manager has both a long and a short
 * name method call. ALL manager instances should be referenced through the Service and not
 * instantiated directly. </p>
 *
 * <h2>Service Level get Methods</h2> <p> This section is for all get methods that require
 * information outside their own data store. All other get methods can be found up the tree in the
 * manager or bean of the bean type. </p>
 *
 * <h2>Service Level set Methods</h2> <p> All set methods must be implemented up to the Service
 * level as this allows consistency across the API and security for the underlying sets. If we have
 * sets at the Service level, the Manager level and ObjectStore, the inconsistency might cause a set
 * to be made to the manager when a set in the service adds additional logic and thus is bypassed.
 * </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 08-Dec-2011
 */
public class AgeRoomService {
    // Singleton Instance

    private volatile static AgeRoomService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final RoomConfigManager roomConfigManager = new RoomConfigManager(VT.ROOM_CONFIG.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final ChildRoomStartManager childRoomStartManager = new ChildRoomStartManager(VT.ROOM_START.manager());
    private final AgeRangeManager ageRangeManager = new AgeRangeManager(VT.AGE_RANGE.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private AgeRoomService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code AgeRoomService} class
     *
     * @return instance of the {@code AgeRoomService}
     * @throws PersistenceException
     */
    public static AgeRoomService getInstance() throws PersistenceException {
        if (INSTANCE == null) {
            synchronized (AgeRoomService.class) {
                // Check again just incase before we synchronised an instance was created
                if (INSTANCE == null) {
                    INSTANCE = new AgeRoomService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code AgeRoomService} class has been initialised. This is used mostly
     * for testing to avoid initialisation of managers when the underlying elements of the
     * initialisation are not available.
     *
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if (INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code AgeRoomService}. The method returns an
     * instance of the {@code AgeRoomService} so it can be chained. This must be called before a the {@code AgeRoomService}
     * is used. e.g  {@code AgeRoomService myRoomService = }
     *
     * @return instance of the {@code AgeRoomService}
     * @throws PersistenceException
     */
    public synchronized AgeRoomService init() throws PersistenceException {
        if (initialise) {
            ageRangeManager.init();
            roomConfigManager.init();
            childRoomStartManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code AgeRoomService}. The method returns an
     * instance of the {@code AgeRoomService} so it can be chained.
     *
     * @return instance of the {@code AgeRoomService}
     * @throws PersistenceException
     */
    public AgeRoomService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code AgeRoomService}. This is generally used for
     * testing. If you wish to refresh the object store reInitialise() should be used.
     *
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = ageRangeManager.clear() ? success : false;
        success = roomConfigManager.clear() ? success : false;
        success = childRoomStartManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory. <p> Used to reset the {@code AgeRoomService}
     * class instance by setting the INSTANCE reference to null. This is mostly used for testing to
     * clear and reset internal memory stores when the underlying persistence data has been removed.
     * </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval">
    /*
     * ***************************************************
     * M A N A G E R R E T R I E V A L
     *
     * This section allows the retrieval of the instance of the manager to use as part of this
     * service. For ease of reference each manager has both a long and a short name method call. ALL
     * manager instances should be referenced through the Service and not instantiated directly.
     * **************************************************
     */
    public AgeRangeManager getAgeRangeManager() {
        return ageRangeManager;
    }

    public ChildRoomStartManager getChildRoomStartManager() {
        return childRoomStartManager;
    }

    public RoomConfigManager getRoomConfigManager() {
        return roomConfigManager;
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">
    /*
     * ***************************************************
     * S E R V I C E V A L I D A T I O N M E T H O D S
     *
     * This section is for all validation methods so that other services can check blind id values
     * are legitimate and any associated business rules are adhere to.
     * **************************************************
     */
    /**
     * Validation Method to check pure id values exist. This method ONLY checks an ID exists in a
     * manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch (validationType) {
            case ROOM_CONFIG:
                return roomConfigManager.isIdentifier(id);
            case AGE_RANGE:
                return ageRangeManager.isIdentifier(id);
            default:
                return false;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Service Level Get Methods">
    /*
     * ***************************************************
     * S E R V I C E L E V E L G E T M E T H O D S
     *
     * This section is for all get methods that require information outside their own data store.
     * All other get methods can be found up the tree in the manager or bean of the bean type.
     * **************************************************
     */
    /**
     * Finds the start of the child's holiday year as a ywd
     *
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public int getHolYearStartYWD(int childId) throws NoSuchIdentifierException, PersistenceException, IllegalValueException {
        PropertiesService ps = PropertiesService.getInstance();
        if (ps.getHolidayConcession().getCountFrom() == -1) {
            List<ChildRoomStartBean> starts = getAllChildRoomStartYwd(childId);
            if (!starts.isEmpty()) {
                return CalendarStatic.getRecentYWD(getAllChildRoomStartYwd(childId).get(0).getStartYwd());
            }
            return -1;
        }
        return CalendarStatic.getRecentYWD((ps.getHolidayConcession().getCountFrom() * 100) + 1, true);
    }

    /**
     * Determines whether a particular ywd is within the required special notice period for a roomId
     * from a baseYwd value. An example might be a baseYwd being set as 'today' with the ywd being
     * some day in the future to determine if that future day falls in a set special notice period
     * from today, as defined in the
     * {@code ageRangeManager} that is associated with a room.
     *
     * @param ywd the future ywd to test.
     * @param baseYwd the base point from where the special notice period is being added.
     * @param roomId the room associated with an ageRange where the special notice period is held
     * @return true if the ywd provided is less than the baseYwd plus the special notice period,
     * else false
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     */
    public boolean isWithinStandardNoticePeriod(int ywd, int baseYwd, int roomId) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException {
        if (getStandardNoticeYwd(roomId) == 0) {
            return false;
        }
        return this.isWithinPeriod(ywd, baseYwd, getStandardNoticeDays(roomId));
    }

    /**
     * Determines whether a particular ywd is within the required standard notice period for a
     * roomId from a baseYwd value. An example might be a baseYwd being set as 'today' with the ywd
     * being some day in the future to determine if that future day falls in a set standard notice
     * period from today, as defined in the
     * {@code ageRangeManager} that is associated with a room.
     *
     * @param ywd the future ywd to test.
     * @param baseYwd the base point from where the standard notice period is being added.
     * @param roomId the room associated with an ageRange where the standard notice period is held
     * @return true if the ywd provided is less than the baseYwd plus the standard notice period,
     * else false
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws NoSuchKeyException
     */
    public boolean isWithinSpecialNoticePeriod(int ywd, int baseYwd, int roomId) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException {
        if (getSpecialNoticeYwd(roomId) == 0) {
            return false;
        }
        return this.isWithinPeriod(ywd, baseYwd, getSpecialNoticeDays(roomId));
    }

    /**
     * Determines whether a particular ywd is within the required period from a baseYwd value. An
     * example might be a baseYwd being set as 'today' with the ywd being some day in the future to
     * determine if that future day falls in the period from today. NOTE: the period might not be 7
     * days but a working 5 day
     *
     * @param ywd the future ywd to test.
     * @param baseYwd the base point from where the notice period is being added.
     * @param noticeDays a period of time in day
     * @return true if the ywd is within the duration from the baseYwd
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     */
    protected boolean isWithinPeriod(int ywd, int baseYwd, int noticeDays) throws PersistenceException, NoSuchIdentifierException, NoSuchKeyException {
        PropertiesService ps = PropertiesService.getInstance();
        TimetableService ts = TimetableService.getInstance();
        // all days counted
        if (ps.getSystemProperties().isClosedDayIncludedInNoticePeriod()) {
            int earliestYwdOutsideNoticePeriod = YWDHolder.add(baseYwd, YWDHolder.getYwdFromDays(noticeDays));
            return (ywd < earliestYwdOutsideNoticePeriod);
        }
        // working days only
        int currentYwd = baseYwd;
        for (int day = 1; day <= noticeDays; day++) {
            while (ts.isClosed(currentYwd)) {
                currentYwd = YWDHolder.add(currentYwd, 1);
            }
            currentYwd = YWDHolder.add(currentYwd, 1);
        }
        return (ywd < currentYwd);
    }

    /**
     * Method used to determine in which rooms a timetable is assigned to be used (in DRM)
     *
     * @param timetableId
     * @return a set of room reference that use this timetable
     * @throws PersistenceException
     */
    public List<RoomConfigBean> getRoomsAssigned(int timetableId) throws PersistenceException {
        TimetableService ts = TimetableService.getInstance();

        LinkedList<RoomConfigBean> rtnList = new LinkedList<>();
        for (RoomConfigBean room : roomConfigManager.getAllObjects()) {
            if (!ts.getDayRangeManager().getAllObjects(room.getRoomId(), timetableId).isEmpty()) {
                rtnList.add(room);
            }
        }
        return (rtnList);
    }

    /**
     * Get all rooms that are currently missing from the child's room Allocation List where the room
     * has a capacity greater than zero
     *
     * @param childId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public List<RoomConfigBean> getUnallocatedRooms(int childId) throws NoSuchIdentifierException, PersistenceException {
        LinkedList<RoomConfigBean> rtnList = new LinkedList<>();
        for (RoomConfigBean room : roomConfigManager.getAllObjects()) {
            if (!childRoomStartManager.isIdentifier(childId, room.getRoomId()) && room.getCapacity() > 0) {
                rtnList.add(room);
            }
        }
        return (rtnList);
    }

    /*
     * ***************************************************
     * R O O M S
     * **************************************************
     */
    /**
     * Returns all rooms that are do not return a closed timetable
     *
     * @param ywd
     * @return a List of RoomConfigBean objects that have open timetables
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public List<RoomConfigBean> getOpenRooms(int ywd) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        TimetableService ts = TimetableService.getInstance();
        LinkedList<RoomConfigBean> rtnList = new LinkedList<>();
        for (int room : ts.getDayRangeManager().getAllKeys()) {
            if (ts.getDayRangeManager().getDayRange(room, ywd).getRightRef() != TimetableManager.CLOSED_ID) {
                rtnList.add(roomConfigManager.getObject(room));
            }
        }
        return (rtnList);
    }

    /**
     * Tests whether a room is eligible for removal
     *
     * @param roomId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws NoSuchIdentifierException
     */
    public boolean isRoomRemovable(int roomId) throws PersistenceException, IllegalActionException, NoSuchIdentifierException {
        try {
            checkRoomRemovable(roomId);
        }
        catch (IllegalActionException e) {
            return false;
        }
        return true;
    }

    /**
     * Holds the business rules that need to be met before a room is eligible for removal
     *
     * @param roomId
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    protected void checkRoomRemovable(int roomId)
            throws PersistenceException, IllegalActionException {

        ChildBookingService childBookingService = ChildBookingService.getInstance();
        TimetableService ts = TimetableService.getInstance();

        int today = CalendarStatic.getRelativeYWD(0);
        // all day ranges for the room must be in the past
        if (!ts.getDayRangeManager().getAllObjects(roomId, today, YWDHolder.MAX_YWD).isEmpty()) {
            throw new IllegalActionException("RoomHasTimetableAssigned");
        }
        // cannot be any current bookings
        for (int ywd : childBookingService.getBookingManager().getAllKeys()) {
            if (!childBookingService.getBookingManager().getYwdBookingsForRoom(ywd, BookingManager.ALL_PERIODS, roomId, BTIdBits.TYPE_ALL, ActionBits.TYPE_FILTER_OFF).isEmpty()) {
                throw new IllegalActionException("RoomHasBookings");
            }
        }
        // cannot be any child room starts for the room
        for (ChildRoomStartBean start : childRoomStartManager.getAllObjects()) {
            if (start.getRoomId() == roomId) {
                throw new IllegalActionException("RoomHasChildAssigned");
            }
        }
        // cannot have priceTariff assigned
        if (!PriceConfigService.getInstance().getPriceTariffRangeManager().getAllIdentifier(MRHolder.getMR(MultiRefEnum.ROOM.type(), roomId)).isEmpty()) {
            throw new IllegalActionException("RoomHasPriceTariffAssigned");
        }
    }

    /**
     * Returns the RoomConfigBean Object that relates to the childId's room allocation on the ywd
     * provided.
     *
     * @param childId
     * @param ywd
     * @return roomConfigBean Object
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public RoomConfigBean getRoomForChild(int childId, int ywd) throws NoSuchIdentifierException, PersistenceException {
        int roomId = childRoomStartManager.getRoomIdForChildYwd(childId, ywd);
        return (roomConfigManager.getObject(roomId));
    }

    /**
     * Returns the start YWDHolder key value for a child in a room
     *
     * @param childId
     * @param roomId
     * @return a YWDHolder key value for start date
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getChildRoomStartYwd(int childId, int roomId) throws NoSuchIdentifierException, PersistenceException {
        return (childRoomStartManager.getObject(childId, roomId).getStartYwd());
    }

    /**
     * Returns a list of ChildRoomStartBean objects for a child. The Bean is identified by the
     * roomId
     *
     * @param childId
     * @return a list of ChildRoomStartBean objects
     * @throws PersistenceException
     */
    public List<ChildRoomStartBean> getAllChildRoomStartYwd(int childId) throws PersistenceException {
        LinkedList<ChildRoomStartBean> rtnList = new LinkedList<>(childRoomStartManager.getAllObjects(childId));
        return (rtnList);
    }

    /**
     * Finds the child's start date in the nursery, being the earliest start ywd in the
     * childRoomStartManager. If no rooms are assigned (because the user has deleted them all, -1 is
     * returned)
     *
     * @param childId
     * @return
     * @throws PersistenceException
     */
    public int getChildStartYwd(int childId) throws PersistenceException {
        List<ChildRoomStartBean> list = getAllChildRoomStartYwd(childId);
        if (list.isEmpty()) {
            return -1;
        }
        return (list.get(0).getStartYwd());
    }

    /*
     * ***************************************************
     * A G E R A N G E S
     * **************************************************
     */
    /**
     * Tests whether an age range is eligible for removal
     *
     * @param ageRangeId
     * @return
     * @throws PersistenceException
     */
    public boolean isAgeRangeRemovable(int ageRangeId) throws PersistenceException {
        try {
            checkAgeRangeRemovable(ageRangeId);
        }
        catch (IllegalActionException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes an age range if it is eligible for removal
     *
     * @param ageRangeId
     * @throws PersistenceException
     */
    public void removeAgeRange(int ageRangeId)
            throws PersistenceException, IllegalActionException {
        checkAgeRangeRemovable(ageRangeId);
        ageRangeManager.removeObject(ageRangeId);
    }

    /**
     * Holds the business rules that make an age range eligible for removal
     *
     * @param ageRangeId
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    protected void checkAgeRangeRemovable(int ageRangeId) throws PersistenceException, IllegalActionException {
        for (RoomConfigBean room : roomConfigManager.getAllObjects()) {
            if (room.getAgeRangeId() == ageRangeId) {
                throw new IllegalActionException("AgeRangeAssignedToRoom");
            }
        }
    }

    /**
     * Provides the required special notice days for a roomId
     *
     * @param roomId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getStandardNoticeDays(int roomId) throws NoSuchIdentifierException, PersistenceException {
        return (ageRangeManager.getObject(roomConfigManager.getObject(roomId).getAgeRangeId()).getStandardNoticeDays());
    }

    /**
     * Provides the required special notice Ywd for a roomId
     *
     * @param roomId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getStandardNoticeYwd(int roomId) throws NoSuchIdentifierException, PersistenceException {
        return (ageRangeManager.getObject(roomConfigManager.getObject(roomId).getAgeRangeId()).getStandardNoticeYwd());
    }

    /**
     * Provides the required standard notice days for a roomId
     *
     * @param roomId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getSpecialNoticeDays(int roomId) throws NoSuchIdentifierException, PersistenceException {
        return (ageRangeManager.getObject(roomConfigManager.getObject(roomId).getAgeRangeId()).getSpecialNoticeDays());
    }

    /**
     * Provides the required standard notice Ywd for a roomId
     *
     * @param roomId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public int getSpecialNoticeYwd(int roomId) throws NoSuchIdentifierException, PersistenceException {
        return (ageRangeManager.getObject(roomConfigManager.getObject(roomId).getAgeRangeId()).getSpecialNoticeYwd());
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Level Set Methods">
    /*
     * **************************************************
     * S E R V I C E L E V E L S E T M E T H O D S
     *
     * All set methods should be interfaced at the Service level as this allows consistency across
     * the API and security for the underlying sets. If we have sets at the Service level, the
     * Manager level and ObjectStore, the inconsistency might cause a set to be made to the manager
     * when a set in the service adds additional logic and thus is bypassed.
     * *************************************************
     */
    /**
     * Sets a room
     *
     * @param roomId
     * @param ageRangeId
     * @param name
     * @param capacity
     * @param fullBuffer
     * @param floorArea
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public RoomConfigBean setRoom(int roomId, int ageRangeId, String name, int capacity, int fullBuffer, int floorArea,
                                  String owner)
            throws MaxCountReachedException, NullObjectException, PersistenceException, NoSuchIdentifierException {
        if (roomId == -1) {
            roomId = roomConfigManager.generateIdentifier();
        }
        if (!ageRangeManager.isIdentifier(ageRangeId)) {
            throw new NoSuchIdentifierException("Age range " + ageRangeId + " does not exist");
        }
        return roomConfigManager.setObject(new RoomConfigBean(roomId, ageRangeId, name, capacity, fullBuffer, floorArea, owner));
    }

    /**
     * Sets the child room start date for each of the rooms as a default. Room starts are always on
     * a Monday (day 0 in the week), except if the child is starting in the middle of an age range,
     * where the start date is set to today
     *
     * @param childId
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setChildRoomStartDefault(int childId, String owner) throws NoSuchIdentifierException,
            IllegalActionException, PersistenceException, NullObjectException {
        PropertiesService ps = PropertiesService.getInstance();
        int dob = ChildService.getInstance().getChildManager().getObject(childId).getDateOfBirth();
        // business rule: ensure child is not too old for nursery
        if (CalendarStatic.getAgeMonths(dob) > ps.getSystemProperties().getMaxAgeMonths()) {
            throw new IllegalActionException("ChildTooOldForNursery");
        }
        // remove any existing room starts
        childRoomStartManager.removeKey(childId);
        // set a room for all available age groups
        for (AgeRangeBean ageRange : ageRangeManager.getAllObjects()) {
            int roomCapacity = 0;
            int roomId = -1;
            for (RoomConfigBean roomConfig : roomConfigManager.getAllObjects()) {
                // find room with the largest capacity which holds that age range
                if (roomConfig.getAgeRangeId() == ageRange.getAgeRangeId() && roomConfig.getCapacity() > roomCapacity) {
                    roomId = roomConfig.getRoomId();
                    roomCapacity = roomConfig.getCapacity();
                }
            }
            if (roomId != -1) {
                int startDate = ageRange.getAgeRangeYW(dob);
                ChildRoomStartBean childRoomStart = new ChildRoomStartBean(roomId, startDate, owner);
                childRoomStartManager.setObject(childId, childRoomStart);
            }
        }
        // clean out the past dates but keep ones where the date falls within the age range
        int today = CalendarStatic.getRelativeYWD(0);
        ChildRoomStartBean currentChildRoomStart = null;
        for (ChildRoomStartBean childRoomStart : childRoomStartManager.getAllObjects(childId)) {
            if (childRoomStart.getStartYwd() < today) {
                if (currentChildRoomStart == null) {
                    currentChildRoomStart = childRoomStart;
                }
                else {
                    if (currentChildRoomStart.getStartYwd() < childRoomStart.getStartYwd()) {
                        childRoomStartManager.removeObject(childId, currentChildRoomStart.getRoomId());
                        currentChildRoomStart = childRoomStart;
                    }
                    else {
                        childRoomStartManager.removeObject(childId, childRoomStart.getRoomId());
                    }
                }
            }
        }
        // if we are in the middle of a room start bracket, set the start ywd of the bracket to today
        if (currentChildRoomStart != null) {
            childRoomStartManager.setObject(childId, new ChildRoomStartBean(currentChildRoomStart.getRoomId(), today, owner));
        }
    }

    /**
     * Sets the child room start YWDHolder key value for a childId and roomId
     *
     * @param childId
     * @param roomId
     * @param startYwd
     * @param owner
     * @return the ChildRoomStartBean saved
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public ChildRoomStartBean setChildRoomStartYwd(int childId, int roomId, int startYwd, String owner) throws PersistenceException, NullObjectException {
        return (childRoomStartManager.setObject(childId, new ChildRoomStartBean(roomId, startYwd, owner)));
    }

    /**
     * Sets the child room start for a list of ChildRoomStartBean objects. It should be noted that
     * the bean is taken as is and saved, this means the owner must be correctly set.
     *
     * @param childId
     * @param childRoomStartList
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void setAllChildRoomStartYwd(int childId, List<ChildRoomStartBean> childRoomStartList) throws PersistenceException, NullObjectException {
        childRoomStartManager.removeKey(childId);
        for (ChildRoomStartBean childRoomStart : childRoomStartList) {
            childRoomStartManager.setObject(childId, childRoomStart);
        }
    }

    /**
     * This method should be used instead of roomConfigManager.removeObject() as this higher level
     * method checks for the use of a room in the dayRangeManager before removing.
     *
     * @param roomId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalActionException
     */
    public void removeRoom(int roomId)
            throws PersistenceException, NoSuchIdentifierException, IllegalActionException {
        checkRoomRemovable(roomId);
        // TODO remove any price lists ranges associated with this room
        roomConfigManager.removeObject(roomId);
    }

    /**
     * Removes a child's planned use of a room. This may affect the child's start date, but this is
     * obvious to a user.
     *
     * @param childId
     * @param roomId
     * @throws PersistenceException
     */
    public void removeChildRoomStart(int childId, int roomId) throws PersistenceException {
        childRoomStartManager.removeObject(childId, roomId);
    }

    /**
     * Removes all the child planned use of rooms
     *
     * @param childId
     * @throws PersistenceException
     */
    public void removeAllChildRoomStart(int childId) throws PersistenceException {
        childRoomStartManager.removeKey(childId);
    }

    /**
     * Sets an AgeRange. As part of this change we need to ensure we have not affected any bookings.
     *
     * @param ageRangeId
     * @param name
     * @param label
     * @param dobAdd
     * @param dobTo
     * @param regularNoticeDays
     * @param extraNoticeDays
     * @param requiredStaffRatio
     * @param requiredFloorArea
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws IllegalActionException
     */
    public AgeRangeBean setAgeRange(int ageRangeId, String name, String label, int dobAdd, Set<Integer> dobTo,
                                    int regularNoticeDays, int extraNoticeDays, int requiredStaffRatio, int requiredFloorArea, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException, NoSuchIdentifierException, NoSuchKeyException, IllegalActionException {
        // required notice period must be no more than 52 weeks x 7 days = 364 days
        if (regularNoticeDays > 364 || regularNoticeDays < 0) {
            throw new IllegalArgumentException("Regular notice days must be between 0 and 364 days, was " + regularNoticeDays);
        }
        if (extraNoticeDays > 364 || extraNoticeDays < 0) {
            throw new IllegalArgumentException("Extra notice days must be between 0 and 364 days, was " + extraNoticeDays);
        }
        // dobAdd must be less than SystemProperties.maxAgeMonths (+1 for safety)
        if (getClosestMonthsWeeks(dobAdd)[0] > PropertiesService.getInstance().getSystemProperties().getMaxAgeMonths()) {
            throw new IllegalArgumentException("dobAdd months exceeds system max age months, dobAdd=" + dobAdd
                    + ", equivalent to ~" + getClosestMonthsWeeks(dobAdd)[0] + " months");
        }

        if (ageRangeId == -1) {
            ageRangeId = ageRangeManager.generateIdentifier();
        }
        AgeRangeBean arb = ageRangeManager.setObject(new AgeRangeBean(ageRangeId, name, label, dobAdd, dobTo, regularNoticeDays, extraNoticeDays, requiredStaffRatio, requiredFloorArea, owner));

        // sort out notice period changes on bookings
        int today = CalendarStatic.getRelativeYWD(0);
        BookingManager bookingManager = ChildBookingService.getInstance().getBookingManager();
        BookingTypeManager bookingTypeManager = ChildBookingService.getInstance().getBookingTypeManager();
        for (int ywd : bookingManager.getAllKeys()) {
            for (BookingBean booking : bookingManager.getAllObjects(ywd)) {
                int noticePeriod;
                if (BTFlagIdBits.isFlag(bookingTypeManager.getObject(booking.getBookingTypeId()).getFlagBits(), BTFlagBits.REGULAR_BOOKING_FLAG)) {
                    noticePeriod = regularNoticeDays;
                }
                else {
                    noticePeriod = extraNoticeDays;
                }
                // if the date is extending then we need to set RESTRICTED bookings within the notice period
                if (this.isWithinPeriod(ywd, today, noticePeriod)) {
                    if (booking.isState(BookingState.OPEN)) {
                        bookingManager.setObjectState(ywd, booking.getBookingId(), BookingState.RESTRICTED, ObjectBean.SYSTEM_OWNED);
                    }

                }
                // if the date is shortened then we need to reset the spanSD to the bookingSD but can't change the state
                else {
                    bookingManager.resetObjectSpanSd(ywd, booking.getBookingId(), ObjectBean.SYSTEM_OWNED);
                }
            }
        }
        return (arb);
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Private Class Methods">
    /*
     * ***************************************************
     * P R I V A T E C L A S S M E T H O D S
     *
     * This section is for all private methods for the class. Private methods should be carefully
     * used so as to avoid multi jump calls and as a general rule of thumb, only when multiple
     * methods use a common algorithm.
     * **************************************************
     */
    //</editor-fold>

    private int[] getClosestMonthsWeeks(int ywd) {
        int[] mw = new int[2];
        // week numbering starts at week 1
        if (ywd < 10) {
            mw[0] = 0;
            mw[1] = 0;
        }
        YWDHolder ywdb = new YWDHolder(ywd);
        int m = ywdb.getYear() * 12;
        int week = ywdb.getWeek();
        week = week == 53 ? 52 : week;
        double months = ((week * 7d + ywdb.getDay()) / 365d) * 12d;
        mw[0] = m += months;
        if ((months - (int) months) == 0) {
            mw[1] = 0;
        }
        else {
            mw[1] = (int) Math.ceil((months - (int) months) * 4) - 1;
        }
        return mw;
    }
}
