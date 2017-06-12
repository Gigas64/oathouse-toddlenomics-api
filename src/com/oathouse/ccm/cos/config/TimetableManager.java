/*
 * @(#)TimetableManager.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.valueholder.SDBits;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Description:	A class to manage the storage and retrieval of TimetableBean objects.
 *				This class extends ObjectPlusManager that allows the storage of a default
 *              TimetableBean that is set within init(). This provides a manager that
 *              does not throw an exception when a TimetableBean is not found but returns
 *              the default TimetableBean.
 *  Note: Some of the methods return NoSuchIdentifierException and NoSuchKeyException. These are
 *        there for safety if the default has not been initialised or set up correctly.
 *
 * @author      Darryl Oatridge
 * @version 	2.00 11/01/2010
 */
public class TimetableManager extends ObjectSetStore<TimetableBean> {

    public static final int CLOSED_ID = 0;
    protected static final String CLOSED_LABEL = "#cccccc";
    protected static final String CLOSED_NAME = "timetable.closed";

    /**
     * Constructs a TimetableManager passing the root path of where all persistence data
     * is to be held, and the name of the manager used for persistence
     *
     * @param managerName name of the manager
     * @param dataOptions 
     */
    public TimetableManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        TimetableBean ttb = new TimetableBean(CLOSED_ID, CLOSED_NAME, CLOSED_LABEL, new ConcurrentSkipListMap<Integer, Set<Integer>>(), "system.default");
        super.resetDefaultObject(ttb);
    }

    /**
     * extends the methods provided by ObjectPlusManager to allow the retrieval
     * of a TimetableBean by its String name.
     *
     * @param timetableName reference name for a TimetableBean
     * @return TimetableBean Object
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public TimetableBean getObject(String timetableName) throws NoSuchIdentifierException, PersistenceException {
        for(TimetableBean d : getAllObjects()) {
            if(d.getName().equalsIgnoreCase(timetableName)) {
                return (d);
            }
        }
        throw new NoSuchIdentifierException("The Timetable Name '" + timetableName + "' does not exist");
    }

    @Override
    public TimetableBean setObject(TimetableBean ob) throws PersistenceException, NullObjectException {
        if(ob.getTimetableId() == CLOSED_ID) {
            throw new PersistenceException("The timetable to be saved is using the same id as the default");
        }
        if(ob.getAllPeriodSd() == null || ob.getAllPeriodSd().isEmpty()) {
            throw new NullObjectException("There are no periodSd values in the TimetableBean");
        }
        if(SDHolder.hasOverlap(ob.getAllPeriodSd())) {
            throw new PersistenceException("A periodSd in the timetable overlaps with another");
        }
        return super.setObject(ob);
    }

    @Override
    public TimetableBean removeObject(int timetableId) throws PersistenceException {
        if(timetableId != CLOSED_ID) {
            return super.removeObject(timetableId);
        }
        return null;
    }

    /**
     * Used to create a temporary timetableBean where a new periodSd set is to be fitted in
     * The new periodSd set must fit inside the current
     * There are no clashes between the end of a new option and the start of an old
     *
     * @param basetable
     * @param mergePeriodSd
     * @param owner
     * @return
     */
    public TimetableBean setTemporaryTimetableMerge(TimetableBean basetable, Set<Integer> mergePeriodSd, String owner) {
        TimetableBean rtnBean = new TimetableBean(basetable.getTimetableId(), basetable.getName(),
                basetable.getLabel(), basetable.getAllPeriodSdAndEndOptions(), owner);
        for(int periodSd : mergePeriodSd) {
            rtnBean = mergePeriodSd(rtnBean, periodSd, owner);
        }
        return (rtnBean);
    }

    /*
     * Private method to merge a period Sd into a TimetableBean
     */
    private TimetableBean mergePeriodSd(TimetableBean timetable, int mergeSd, String owner) {
        // get the end of the merge that we use later
        int endOfMergeSd = SDHolder.getSD(SDHolder.getEnd(mergeSd), 0);
        if(timetable.getEndOfDayTime() < SDHolder.getEnd(mergeSd)) {
            endOfMergeSd = SDHolder.getSD(timetable.getEndOfDayTime(), 0);
        }

        Set<Integer> periodSdList = new ConcurrentSkipListSet<Integer>();
        //find all the periodSd that are affected
        for(int periodSd : timetable.getAllPeriodSd()) {
            // if the periodSd is the same as a mergeSd then return as nothing to do
            if(periodSd == mergeSd) {
                // include the end option even if it is already there
                Set<Integer> endOptions = timetable.getEndOptionsForTime(SDHolder.getStart(periodSd));
                endOptions.add(endOfMergeSd);
                timetable.setPeriodSdAndOptions(periodSd, endOptions, owner);
                return (timetable);
            }
            if(SDHolder.inRange(periodSd, mergeSd)) {
                periodSdList.add(periodSd);
            }
        }
        if(!periodSdList.isEmpty()) {
            for(int periodSd : periodSdList) {
                int result = SDHolder.compare(periodSd, mergeSd);
                if(SDBits.match(result, SDBits.START_INSIDE)) {
                    int[] splitPeriodSd = SDHolder.splitSD(periodSd, SDHolder.getStart(mergeSd));
                    Set<Integer> options = timetable.getEndOptionsForTime(SDHolder.getStart(periodSd));
                    timetable.removePeriodSd(periodSd);
                    timetable.setPeriodSdAndOptions(splitPeriodSd[0], options, owner);
                    timetable.setPeriodSdAndOptions(splitPeriodSd[1], options, owner);
                }
            }
        }
        // now insert new end option into all non-empty endOptions for starts before and equal to the start of the periodSd
        for(int periodSd : timetable.getAllPeriodSd()) {
            if(SDHolder.getStart(periodSd) <= SDHolder.getEnd(mergeSd)) {
                Set<Integer> endOptions = timetable.getEndOptionsForTime(SDHolder.getStart(periodSd));
                if(!endOptions.isEmpty()) {
                    endOptions.add(endOfMergeSd);
                }
                timetable.setPeriodSdAndOptions(periodSd, endOptions, owner);
            }
            int result = SDHolder.compare(periodSd, endOfMergeSd);
            if(SDBits.match(result, SDBits.END_INSIDE)) {
                // NOTE: splitSD time is + 1 as we split after the end
                int[] splitPeriodSd = SDHolder.splitSD(periodSd, SDHolder.getEnd(mergeSd) + 1);
                Set<Integer> options = timetable.getEndOptionsForTime(SDHolder.getStart(periodSd));
                timetable.removePeriodSd(periodSd);
                timetable.setPeriodSdAndOptions(splitPeriodSd[0], options, owner);
                timetable.setPeriodSdAndOptions(splitPeriodSd[1], null, owner);
            }
        }

        return (timetable);
    }
}
