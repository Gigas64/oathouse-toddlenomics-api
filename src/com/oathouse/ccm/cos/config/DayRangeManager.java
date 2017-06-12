/**
 * @(#)DayRangeManager.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NoSuchKeyException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.ObjectStoreException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectOrderMapStore;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code DayRangeManager} deals with the relationship between two sets of references. The right reference
 * and the left reference. This is like a many to many relationship between the leftRef and the rightRef where
 * the range is the joining relation to allow a one to one relation defined by the day. In other words a leftRef
 * has a one to one relationship with a righRef for a particular day of a week in a year.
 *
 * leftRef -- range(year,week,day) -- rightRef
 * ( Key )     (     List DayRangeBean      )
 *
 * All the information is retrieved from left to right so the leftRef is used as a key pointing to a number of
 * ranges. There can only be one rightRef for a leftRef on a single day in the week of a year.
 *
 * <p>
 * In the case of a timetable <br>
 * leftRef = roomId<br>
 * rightRef = timetableId<br>
 * </p>
 *
 *
 * @author 	Darryl Oatridge
 * @version 	2.01 09/01/2010
 */
public class DayRangeManager extends ObjectOrderMapStore<DayRangeBean> {

    /**
     * Constructs a {@code DayRangeManager} passing the root path of where all persistence data
     * is to be held. The specific path is added internally.
     *
     * @param managerName The name of the manager used to store persistence and distinguish different instances
     * @param dataOptions
     */
    public DayRangeManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        boolean[] days = {true, true, true, true, true, true, true};
        DayRangeBean d = new DayRangeBean(ObjectEnum.DEFAULT_ID.value(), ObjectEnum.DEFAULT_KEY.value(),
                    ObjectEnum.DEFAULT_ID.value(), 2000010, 9999010, days, "system.default");
        this.resetDefaultObject(d);
    }

    /**
     * This overrides the init() to throw an exception as with DayRangeManager you must
     * initialise with a set of LeftRef.
     *
     * @return instance of the DayRangeManager
     * @throws PersistenceException
     */
    @Override
    public DayRangeManager init() throws PersistenceException {
        throw new PersistenceException("With DayRangeManager you must initialise with a set of LeftRef. "
                    + "Please use the overload method init(Set<Integer> allLeftRef)");
    }



    /**
     * This overload of init() ensures there is a leftRef for all keys of object managers
     * that use the DayRangeManager. This ensures when a leftRef is requested, and a default
     * should be returned, the integrity is sound. Any managers using DayRangeRanger should
     * pass their key set at startup to ensure integrity.
     *
     * @param allLeftRef
     * @return instance of the DayRangeManager
     * @throws PersistenceException
     */
    public DayRangeManager init(Set<Integer> allLeftRef) throws PersistenceException, PersistenceException {
        super.init();
        for(int leftRef : allLeftRef) {
            if(!getAllKeys().contains(leftRef)) {
                setKey(leftRef);
            }
        }
        return(this);
    }

    /**
     * Gets the DayRangeBean for a leftRef on a single day in the week of a year. This gives the range that is at the
     * top of the range layers. This is position 0 in the List of ranges for this day.
     *
     * @param leftRef
     * @param ywd
     * @return DayRangeBean bean at the top of the list or null if not found
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws PersistenceException
     */
    public DayRangeBean getDayRange(int leftRef, int ywd) throws NoSuchIdentifierException, NoSuchKeyException, PersistenceException {
        for(DayRangeBean b : getAllObjects(leftRef, ywd, ywd)) {
            if(b.getDays()[YWDHolder.getDay(ywd)]) {
                return (b);
            }
        }
        return (getDefaultObject());
    }

    /**
     * returns the rightRef for each of the days of the week. If no DayRangeBean is found on
     * a day then the defaulr DayRangeBean is returned with id  DayRangeManager.NO_RANGE.
     *
     * @param leftRef
     * @param yw0
     * @return a List of rightRef integers for the week
     */
    public List<Integer> getRightRefForWeekLeftRef(int leftRef, int yw0) {
        int daysOfTheWeek = 7;  // just used for readability
        int cleanYw0 = YWDHolder.getYW(yw0);
        final List<Integer> rtnList = new LinkedList<Integer>();
        for(int i = 0; i < daysOfTheWeek; i++) {
            try {
                rtnList.add(getDayRange(leftRef, YWDHolder.add(cleanYw0, i)).getRightRef());
            } catch(ObjectStoreException ex) {
                rtnList.add(getDefaultObject().getRightRef());
            }
        }
        return (rtnList);
    }

    /**
     * returns a {@code List} of all {@code DayRange} where the range has
     * the passed {@code refId}. This is used to display all the ranges that
     * belong to a given {@code refId} to view or for selection to edit.
     *
     * @param leftRef The reqId the rangeId can be found in
     * @param rightRef
     * @return List of DayRange with the daySlotId passed.
     * @throws PersistenceException
     */
    public List<DayRangeBean> getAllObjects(int leftRef, int rightRef) throws PersistenceException {
        final List<DayRangeBean> rtnList = new LinkedList<DayRangeBean>();
        for(DayRangeBean p : getAllObjects(leftRef)) {
            if(p.getRightRef() == rightRef) {
                rtnList.add(p);
            }
        }
        return (rtnList);
    }

    /**
     * Returns all the {@code DayRange} Objects found within a given Query range for a
     * reqId. This can be used to identify conflicts, overlaps or to view and select for edit.
     * Note: this call assumes all the days of the week.
     *
     * @param leftRef the reqId this is applied to
     * @param startYwd
     * @param endYwd
     * @return a List of DayRange that have part or all of their range within the query range
     * @throws PersistenceException
     */
    public List<DayRangeBean> getAllObjects(int leftRef, int startYwd, int endYwd) throws PersistenceException {
        boolean[] days = {true, true, true, true, true, true, true};
        return (getAllObjects(leftRef, startYwd, endYwd, days));
    }

    /**
     * Returns all the {@code DayRange} Objects found within a given Query range for a
     * reqId. The returning {@code List} will only contain {@code DayRange} Objects
     * that fall on the days specified in the {@code days} array. The array passed should
     * be of size 7.
     *
     * This can be used to identify conflicts, overlaps or to view and select for edit.
     *
     * @param leftRef the reqId this is applied to
     * @param startYwd the start Ywd
     * @param endYwd the end ywd
     * @param days A boolean array of size 7, true for included false for excluded
     * @return a List of DayRange that have part or all of their range within the query range
     *         and fall on the days marked true
     * @throws PersistenceException
     */
    public List<DayRangeBean> getAllObjects(int leftRef, int startYwd, int endYwd, boolean[] days) throws PersistenceException {
        final List<DayRangeBean> rtnList = new LinkedList<>();
        // check if this profile clashes with others
        for(DayRangeBean p : getAllObjects(leftRef)) {
            // check there is a day that matches
            if(p.inRange(leftRef, startYwd, endYwd, days)) {
                rtnList.add(p);
            }
        }
        return (rtnList);
    }

    /**
     * returns a a set of LeftRef where the rightRef is used.
     *
     * @param rightRef
     * @return a set of rightRef
     * @throws PersistenceException
     */
    public Set<Integer> getLeftRefForRightRef(int rightRef) throws PersistenceException {
        final Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(int leftRef : getAllKeys()) {
            for(DayRangeBean d : getAllObjects(leftRef)) {
                if(d.getRightRef() == rightRef) {
                    rtnSet.add(leftRef);
                    break;
                }
            }
        }
        return(rtnSet);
    }

    /**
     * This method is used to reset all {@code DayRangeBean} objects referenced by a leftRef
     * with a single {@code DayRangeBean} that spans all time. the new single {@code DayRangeBean}
     * is referenced by the LeftRef and hold the rightRef.
     *
     * @param leftRef The leftRef to be reset
     * @param rightRef The new rightRef to be referenced in the new DayRangeBean
     * @param owner the owner of the change
     * @return the new DayRangeBean the leftRef has been set with
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public DayRangeBean resetLeftRef(int leftRef, int rightRef, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException {
        removeKey(leftRef);
        boolean[] days = {true, true, true, true, true, true, true};
        final DayRangeBean b = new DayRangeBean(generateIdentifier(), leftRef, rightRef, 2000010,
                    9999010, days, owner);
        setFirstObject(leftRef, b);
        return (b);
    }

    /**
     * Used to move {@code DayRangeBean} Objects up and down layers. {@code DayRange} Objects
     * are moved from their current layer to a lower or higher Layer and all affected
     * {@code DayRange} Objects are moved up or down one.
     *
     * @param leftRef
     * @param viewYw0
     * @param startYwd
     * @param rangeId
     * @param moveUp the direction to be moved true = up, false = down
     * @param owner the owner of the change
     * @return the new ordered List of DayRange
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public List<Integer> moveObject(int leftRef, int rangeId, int viewYw0, int startYwd, boolean moveUp, String owner) throws NoSuchIdentifierException, PersistenceException, NoSuchKeyException, NullObjectException, MaxCountReachedException {
        if(leftRef == getDefaultObject().getLeftRef() || ObjectEnum.isReserved(rangeId)) {
            throw new NoSuchIdentifierException("The leftRef or Identifier is a reserved value and can't be moved");
        }
        // so as not to cut a week
        if(startYwd > viewYw0) {
            startYwd = -1;
        }
        int cleanViewYw0 = YWDHolder.getYW(viewYw0); // clean the value
        int index = getSwapPosition(leftRef, rangeId, cleanViewYw0, moveUp);
        if(index > -1) {
            // split the range
            DayRangeBean b = splitObject(leftRef, rangeId, startYwd, owner);
            // set the new object in the correct position
            setObjectAt(leftRef, b, index);
        }
        return (getRightRefForWeekLeftRef(leftRef, cleanViewYw0));
    }

    /**
     * used to split a day range returning the new range with a new id. The new range duplicates
     * the original with a new id but has a start date of startCutYwd. The original range retains
     * its id and data except for the end date which will be changed to startCutYwd - 1. The
     * original
     *
     * @param leftRef
     * @param rangeId
     * @param startCutYwd the data to start the cut from
     * @param owner
     * @return
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     */
    private DayRangeBean splitObject(int leftRef, int rangeId, int startCutYwd, String owner) throws NoSuchIdentifierException, NoSuchKeyException, MaxCountReachedException, PersistenceException, NullObjectException {

        final DayRangeBean range = getObject(leftRef, rangeId);
        if(leftRef == getDefaultObject().getLeftRef() || ObjectEnum.isReserved(rangeId)) {
            throw new NoSuchIdentifierException("Unable to split as rangeId '" + rangeId
                        + "' does not exist in leftRef '" + leftRef + "'");
        }
        if(range.getStartYwd() >= startCutYwd || range.getEndYwd() <= startCutYwd) {
            return (range);
        }

        final DayRangeBean newRange = new DayRangeBean(generateIdentifier(), range.getLeftRef(),
                    range.getRightRef(), startCutYwd,
                    range.getEndYwd(),
                    range.getDays(), owner);
        // split the range
        range.setEndYwd(YWDHolder.add(startCutYwd, -1), owner);
        // get the current position of the range
        int index = getIndexOf(leftRef, range.getDayRangeId());
        // set the changes back into its position
        setObjectAt(leftRef, range, index);
        setFirstObject(leftRef, newRange);
        return (newRange);
    }

    /**
     * Removes all {@code DayRangeBean} objects that are associated with
     * {@code rightRef}. This is used when wanting to remove a whole set of
     * {@code DayRangeBean} objects for a particular {@code rightRef}
     *
     * @param rightRef
     * @throws PersistenceException
     */
    public void removeAllObjectsForRightRef(int rightRef) throws PersistenceException {
        final List<DayRangeBean> toRemove = new LinkedList<>();
        for(int leftRef : getAllKeys()) {
            for(DayRangeBean d : getAllObjects(leftRef)) {
                if(d.getRightRef() == rightRef) {
                    toRemove.add(d);
                }
            }
        }
        for(DayRangeBean d : toRemove) {
            removeObject(d.getLeftRef(), d.getDayRangeId());
        }
    }

    /**
     * MAINTENANCE helper method to removes {@code DayRange} objects that no longer apply because their end date
     * is older than the current date. NOTE: This is done on a week basis so the week
     * must have passed for this to remove. This would be used on some sort of timer task
     * as part of a maintenance schedule.
     * @param yw0
     * @throws PersistenceException
     */
    public void tidyDayRangeObjects(int yw0) throws PersistenceException {
        final List<DayRangeBean> toRemove = new LinkedList<>();
        for(int leftRef : getAllKeys()) {
            for(DayRangeBean d : getAllObjects(leftRef)) {
                if(d.getEndYwd() <= YWDHolder.getYW(yw0)) {
                    toRemove.add(d);
                }
            }
        }
        for(DayRangeBean d : toRemove) {
            removeObject(d.getLeftRef(), d.getDayRangeId());
        }
    }

    /**
     * This overrides the default setObject to add the object at the start and
     * not place it at the end. The DayRangeManager layers the ranges from 0 - n thus placing the
     * range as the first in the list puts it to the top.
     *
     * @param key
     * @param ob
     * @return the object that was set
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public DayRangeBean setObject(int key, DayRangeBean ob) throws PersistenceException, NullObjectException {
        // so as to avoid duplicates of the same day range look through and check there are not duplicates
        for(DayRangeBean dayRange : getAllObjects(key)) {
            if(dayRange.same(ob)) {
                removeObject(key, dayRange.getRangeId());
            }
        }
        return(setFirstObject(key, ob));
    }

    /**
     * Sets an object to the first position in the list held within a key
     *
     * @param key reference value to a list of objects
     * @param ob the object to be stored
     * @return The object that was set
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public DayRangeBean setFirstObject(int key, DayRangeBean ob) throws PersistenceException, NullObjectException {
        for(DayRangeBean dayRange : getAllObjects(key)) {
            if(dayRange.same(ob)) {
                removeObject(key, dayRange.getRangeId());
            }
        }
        return super.setFirstObject(key, ob);
    }

    /**
     * Sets an object to the last position in the list held within a key
     *
     * @param key reference value to a list of objects
     * @param ob the object to be stored
     * @return The object that was set
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public DayRangeBean setLastObject(int key, DayRangeBean ob) throws PersistenceException, NullObjectException {
        for(DayRangeBean dayRange : getAllObjects(key)) {
            if(dayRange.same(ob)) {
                removeObject(key, dayRange.getRangeId());
            }
        }
        return super.setLastObject(key, ob);
    }

    /**
     * Sets an object to the specified position index in the list held within a key
     *
     * @param key reference value to a list of objects
     * @param ob the object to be stored
     * @param index the index where the object should be placed
     * @return The object that was set
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public DayRangeBean setObjectAt(int key, DayRangeBean ob, int index) throws PersistenceException, NullObjectException {
        for(DayRangeBean dayRange : getAllObjects(key)) {
            if(dayRange.same(ob)) {
                removeObject(key, dayRange.getRangeId());
            }
        }
        return super.setObjectAt(key, ob, index);
    }

    /**
     * Allows a range to be "ended"
     * @param leftRef
     * @param dayRangeId
     * @param endYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public void setObjectEndYwd(int leftRef, int dayRangeId, int endYwd, String owner) throws NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalActionException {
        DayRangeBean r = getObject(leftRef, dayRangeId);
        if(endYwd < r.getStartYwd()) {
            throw new IllegalActionException("NewRangeEndBeforeRangeStart");
        }
        r.setEndYwd(endYwd, owner);
        setObject(leftRef, r);
    }

    /* ********************************************
     * U T I L I T Y   S T A T I C   M E T H O D S
     * *******************************************/

    /**
     * Static helper method to examine a DayRange manager and see if a rightRef exists
     * within a period range
     * @param rightRef the right reference to check
     * @param startSd the range start YWDHolder
     * @param endSd the range end YWDHolder
     * @param drm the DayRangeManager instance
     * @return true if found, false if not.
     * @throws PersistenceException
     */
    public static boolean inDayRange(int rightRef, int startSd, int endSd, DayRangeManager drm) throws PersistenceException {
        for(int leftRef : drm.getLeftRefForRightRef(rightRef)) {
            for(DayRangeBean dayRange : drm.getAllObjects(leftRef, startSd, endSd)) {
                if(dayRange.getRightRef() == rightRef) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Static helper method to examine a DayRange manager and see if a rightRef exists in all
     * day ranges.
     *
     * @param rightRef the right reference to check
     * @param drm the DayRangeManager instance
     * @return true if found, false if not.
     * @throws PersistenceException
     */

    public static boolean inDayRange(int rightRef, DayRangeManager drm) throws PersistenceException {
        // startSd set to a year previous
        int startSd = YWDHolder.getYW(2000, 1);
        // endSd set to forever
        int endSd = YWDHolder.getYW(9999, 1);
        return inDayRange(rightRef, startSd, endSd, drm);
    }

    /****************************************************************
     *  P R I V A T E   M E T H O D S
     ****************************************************************/
    private int getSwapPosition(int leftRef, int rangeId, int yw0, boolean moveUp) throws NoSuchIdentifierException, PersistenceException {

        int prevIndex = -1;
        boolean wasLast = false;
        int cleanYw0 = YWDHolder.getYW(yw0);
        // get the list just for the week being looked at
        List<DayRangeBean> dsrv = getAllObjects(leftRef, cleanYw0, cleanYw0 + 6);
        // if this is the only range in this week then do nothing
        if(dsrv.size() == 1) {
            return (-1);
        }
        // run through and find where to position the move
        for(DayRangeBean p : dsrv) {
            if(wasLast) {
                return (super.getIndexOf(leftRef, p.getDayRangeId()));
            }
            if(rangeId == p.getDayRangeId()) {
                if(moveUp) {
                    return (prevIndex);
                }
                wasLast = true;
            }
            prevIndex = super.getIndexOf(leftRef, p.getDayRangeId());
        }
        // if we have reached here then the rangeId was never found so move nothing
        return (-1);
    }

}
