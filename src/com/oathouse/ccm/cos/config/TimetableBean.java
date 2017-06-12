/*
 * @(#)TimetableBean.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.SDBits;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jdom2.Element;

/**
 *
 * Provides setup information about the periods (called periodSd) associated with a day.
 * This can be considered a container to allow for the interrogation of how periods in a
 * particular day present themselves. Periods are SDHolder key values that are made up
 * of a start time in minutes from midnight and a duration again in minutes from the start
 * time. Where time is used as a parameter name rather than periodSd, this is a single time
 * in minutes not as SDHolder key value and represents a single time within a periodSd
 *
 * @author      Darryl Oatridge
 * @version 	2.03 15-Oct-2010
 * @see SDHolder
 */
public class TimetableBean extends ObjectBean {

    private static final long serialVersionUID = 20101015203L;
    // a map of periodSds in SDHolder format referencing set of SDHolder format end start and duration
    private volatile String name;
    private volatile String label;
    private final ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Integer>> periodSdStore;

    /**
     * Constructor to create the {@code TimetableBean} with all the Attributes
     * of the class being set here making the configuration bean read only
     *
     * @param timetableId Unique Reference identifier
     * @param name a string name of the TimetableBean for identification
     * @param label the colour code label for this TimetableBean
     * @param periodSdAndOptions Start/Duration (SDHolder) set of monitored periodSd
     * @param owner the person creating the bean
     */
    public TimetableBean(int timetableId, String name, String label, Map<Integer, Set<Integer>> periodSdAndOptions,
            String owner) {
        super(timetableId, owner);
        this.name = name != null ? name : "";;
        this.label = label != null ? label : "";
        this.periodSdStore = new ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Integer>>();
        if(periodSdAndOptions != null && !periodSdAndOptions.isEmpty()) {
            for(int periodSd : periodSdAndOptions.keySet()) {
                this.periodSdStore.put(periodSd, new ConcurrentSkipListSet<Integer>());
                if(periodSdAndOptions.get(periodSd) != null) {
                    for(int option : periodSdAndOptions.get(periodSd)) {
                        this.periodSdStore.get(periodSd).add(option);
                    }
                }
            }
        }
    }

    /**
     * This constructor is used in the creation of instances taken from persistence
     */
    public TimetableBean() {
        super();
        this.name = "";
        this.label = "";
        this.periodSdStore = new ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Integer>>();
    }

    /**
     * Returns the timetableId for this TimetableBean.
     */
    public int getTimetableId() {
        return super.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the startTime  of the first periodSd for this TimetableBean.
     * @return returns the first periodSd start time
     */
    public int getStartOfDayTime() {
        return (SDHolder.getStart(periodSdStore.firstKey()));
    }

    /**
     * Returns the closing time (last periodSd end time) for this TimetableBean.
     * @return returns the last periodSd end time
     */
    public int getEndOfDayTime() {
        int endTime = 0;
        for(int key : periodSdStore.keySet()) {
            if(SDHolder.getEnd(key) > endTime) {
                endTime = SDHolder.getEnd(key);
            }
        }
        return (endTime);
    }

    /**
     * Finds the periodSd for a particular time, returns -1 if time is before
     * startOfDayTime and -2 if time falls in a closed time during the day and
     * -3 if the time is after endOfDayTime
     *
     * @param time
     * @return the periodSd where this time falls inside
     */
    public int getPeriodSd(int time) {
        int periodSdChk = SDHolder.compare(periodSdStore.firstKey(), SDHolder.getSD(time, 0));
        if(SDBits.contain(periodSdChk, SDBits.END_BEFORE)) {
            return -1;
        }
        periodSdChk = SDHolder.compare(periodSdStore.lastKey(), SDHolder.getSD(time, 0));
        if(SDBits.contain(periodSdChk, SDBits.START_AFTER)) {
            return -3;
        }
        // find the periodSd in which the time sits
        for(int periodSd : periodSdStore.keySet()) {
            if(SDHolder.inRange(periodSd, SDHolder.getSD(time, 0))) {
                return (periodSd);
            }
        }
        return (-2);
    }

    /**
     * Finds the periodSd for a particular time where the time is the start of an sd.
     * This caters for the situation where the start and end of a period are the same minute.
     * For example, if there are periods 4200030 and 4500030, the time 450 is both the end of the
     * first periodSd and the start of the second.  If the context of the call is that we are
     * looking for a period which matches a START time 450, we need to look for starts before
     * considering the period in which the time sits (which finds ends first).
     * @param time this is a time not a periodSd
     * @param isStart
     * @return
     */
    public int getPeriodSd(int time, boolean isStart) {
        for(int periodSD : periodSdStore.keySet()) {
            if(SDHolder.getStart(periodSD) == time) {
                return (periodSD);
            }
        }
        return (getPeriodSd(time));
    }

    /**
     * Returns the total number of periodSd for this TimetableBean.
     */
    public int getPeriodSdCount() {
        return (periodSdStore.keySet().size());
    }

    /**
     * Returns the number of periodSd up to the time parameter
     *
     * @param time this is a time not a periodSd
     * @return number of periodSd to this time
     */
    public int getPeriodSdCountTo(int time) {
        int counter = 0;
        int periodSdChk = SDHolder.compare(periodSdStore.firstKey(), SDHolder.getSD(time, 0));
        if(SDBits.contain(periodSdChk, SDBits.END_BEFORE)) {
            return -1;
        }
        periodSdChk = SDHolder.compare(periodSdStore.lastKey(), SDHolder.getSD(time, 0));
        if(SDBits.contain(periodSdChk, SDBits.START_AFTER)) {
            return -3;
        }
        for(int key : periodSdStore.keySet()) {
            if(SDHolder.inRange(key, SDHolder.getSD(time, 0))) {
                return (counter);
            }
            counter++;
        }
        return (-2);
    }

    /**
     * Returns a List of periodSd durations for this TimetableBean.
     */
    public Set<Integer> getAllPeriodSd() {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(int periodSd : periodSdStore.keySet()) {
            rtnSet.add(periodSd);
        }
        return (rtnSet);
    }

    /**
     * Returns a Set of periodSd values that represent the end options
     * for a specified start time for this TimetableBean.
     *
     * @param time a time within a periodSd
     * @return a set of end periodSd options for that time
     */
    public Set<Integer> getEndOptionsForTime(int time) {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(int periodSd : periodSdStore.keySet()) {
            // test at +1 minute to ensure that options for previous period are not included
            if(SDHolder.inRange(periodSd, SDHolder.getSD(time, 0))) {
                for(int option : periodSdStore.get(periodSd)) {
                    rtnSet.add(option);
                }
            }
        }
        return (rtnSet);
    }

    /**
     * Returns Set of valid start times (ones where there are some end options)
     * @return All valid start times, NOT sd
     */
    public Set<Integer> getAllValidStartTimes() {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(int key : periodSdStore.keySet()) {
            if(!periodSdStore.get(key).isEmpty()) {
                rtnSet.add(SDHolder.getStart(key));
            }
        }
        return rtnSet;
    }

    /**
     * Returns the whole map of end periodSd options for every start periodSd
     *
     * @return the options map
     */
    public Map<Integer, Set<Integer>> getAllPeriodSdAndEndOptions() {
        Map<Integer, Set<Integer>> rtnMap = new ConcurrentSkipListMap<>();
        for(int periodSd : periodSdStore.keySet()) {
            rtnMap.put(periodSd, new ConcurrentSkipListSet<Integer>());
            for(int option : periodSdStore.get(periodSd)) {
                rtnMap.get(periodSd).add(option);
            }
        }
        return rtnMap;
    }

    /**
     * Returns the whole map of end periodSd options for every start periodSd where there is a
     * valid end option
     *
     * @return the options map without empty end periodSd options
     */
    public Map<Integer, Set<Integer>> getAllPeriodSdWithEndOptions() {
        Map<Integer, Set<Integer>> rtnMap = new ConcurrentSkipListMap<Integer, Set<Integer>>();
        for(int periodSd : periodSdStore.keySet()) {
            if(!periodSdStore.get(periodSd).isEmpty()) {
                rtnMap.put(periodSd, new ConcurrentSkipListSet<Integer>());
                for(int option : periodSdStore.get(periodSd)) {
                    rtnMap.get(periodSd).add(option);
                }
            }
        }
        return rtnMap;
    }

    /**
     * Returns a List of times from periodSd start to periodSd end inclusive, split
     * by the value passed.this includes both the start time and the end time
     * even if the end time does not fall on a split.
     *
     * @param periodSd the periodSd to be split
     * @param split the split value
     * @return a list of split times
     */
    public List<Integer> getTimeSplitsForPeriodSd(int periodSd, int split) {
        List<Integer> rtnList = new LinkedList<>(SDHolder.getTimeSplits(periodSd, split));
        Collections.sort(rtnList);
        return (rtnList);
    }

    /**
     * Add a new periodSd and options to the timetable. This assumes that any clashing
     * periodSd values have been previously removed or are there by design.
     *
     * @param periodSd
     * @param periodSdOptions
     * @param owner
     */
    protected void setPeriodSdAndOptions(int periodSd, Set<Integer> periodSdOptions, String owner) {
        this.periodSdStore.put(periodSd, new ConcurrentSkipListSet<Integer>());
        if(periodSdOptions != null) {
            for(int option : periodSdOptions) {
                this.periodSdStore.get(periodSd).add(option);
            }
        }
        super.setOwner(owner);
    }

    /**
     * Finds the valid start and end SD closest to the sd provided,
     * considering the available periods, valid starts and endOptions.
     * If the sd has no duration, a duration of 1 is given (otherwise it makes no sense)
     * If the sd is in the middle of a valid day and attendance is not possible for any of it, a valid sd cannot be found
     * Prioritises on start:
     * If the start of the sd is before startOfDay, it will be pushed back to the first valid start
     * If the sd is entirely after the endOfDay, will find the last valid start and return the latest valid sd
     * If the sd starts in a period in the day where attendance is not possible, the earliest start is set to the start of the sd
     * (this means that afterschool requests that start before the afternoon session but after the end of the morning session
     * only look at whethere a valid sd can be found in the afternoon)
     * Based on the start found:
     * If the end of the sd is after the endOfDay, it will be brought forward to the last valid end for the start
     * If the sd is entirely before the startOfDay, will find the first valid end for the first available start
     * If there is no valid sd starting on or after the earliest start, returns -1
     *
     * @param periodSd the periodSd SDHolder key value
     * @param earliestStart the first time that could be considered as a possible valid start time
     * @return the modified periodSd SDHolder key value
     */
    public int getValidSd(int periodSd, int earliestStart) {
        if(SDHolder.getDuration(periodSd) == 0) {
            periodSd++;
        }
        if(earliestStart < 0) {
            earliestStart = 0;
        }
        int start = SDHolder.getStart(periodSd);
        int end = SDHolder.getEnd(periodSd);
        // if the sd is within the day
        if(start >= getStartOfDayTime() && end <= getEndOfDayTime()) {
            // if attendance is not possible for any part of it, return -1
            if(!isAttendancePossible(periodSd)) {
                return -1;
            }
            // if the sd starts within a period for which attendance is not possible,
            // make the sd start the earliest start
            if(!isAttendancePossible(SDHolder.getSD(start, 0))) {
                earliestStart = start;
            }
        }
        // find a valid start
        int timetablePeriodSd = getPeriodSd(SDHolder.getStart(periodSd), true);
        // plan start is before timetable startOfDayTime, set start to startOfDay
        if(timetablePeriodSd == -1) {
            start = getStartOfDayTime();
        } // plan start is after timetable endOfDayTime, set start to last valid start
        else if(timetablePeriodSd == -3) {
            List<Integer> starts = new LinkedList<>(getAllValidStartTimes());
            start = starts.size() > 0 ? starts.get(starts.size() - 1) : 0;
        } else {
            start = SDHolder.getStart(timetablePeriodSd);
        }
        // start is now set to a timetable period start: now find latest valid start equal to or before this start
        // and later than or equal to the earliest start
        List<Integer> validStartTimes = new LinkedList<>();
        for(int s : getAllValidStartTimes()) {
            if(s >= earliestStart) {
                validStartTimes.add(s);
            }
        }
        if(validStartTimes.isEmpty()) {
            return -1;
        }
        int lastValidStartTime = validStartTimes.get(0);
        for(int validStartTime : validStartTimes) {
            if(validStartTime > start) {
                break;
            }
            if(validStartTime >= earliestStart) {
                lastValidStartTime = validStartTime;
            }
            lastValidStartTime = validStartTime;
            if(validStartTime == start) {
                break;
            }
        }
        start = lastValidStartTime;

        // now find a valid end for that start
        timetablePeriodSd = getPeriodSd(SDHolder.getEnd(periodSd));
        // plan end is before timetable startOfDayTime, set end to startOfDay
        if(timetablePeriodSd == -1) {
            end = getStartOfDayTime();
        } // plan end is after timetable endOfDayTime, set end to endOfDay
        else if(timetablePeriodSd == -3) {
            end = getEndOfDayTime();
        } else {
            end = SDHolder.getEnd(timetablePeriodSd);
        }

        // if the latest end option for the start is before this end, the latest end option is the valid end
        List<Integer> endOptionsForStart = new LinkedList<>(getEndOptionsForTime(start));
        int lastEndOption = SDHolder.getStart(endOptionsForStart.get(endOptionsForStart.size() - 1));
        if(lastEndOption < end) {
            return SDHolder.getSD(start, lastEndOption - start);
        }

        // find earliest option where the end of the option is equal to or later than the unmatched plan end
        for(int validEndSd : endOptionsForStart) {
            int validEndTime = SDHolder.getEnd(validEndSd);
            if(validEndTime >= end) {
                end = validEndTime;
                break;
            }
        }
        return SDHolder.getSD(start, end - start);
    }

    /**
     * Looks at the configuration of the day to determine whether attendance is possible for
     * the sd.  For example, in the case of before and afterschool clubs, attendance is not possible during
     * the middle of the day.  End options for the morning do not include afternoon options.
     *
     * @param periodSd
     * @return
     */
    public boolean isAttendancePossible(int periodSd) {
        if(SDHolder.getEnd(periodSd) < getStartOfDayTime()) {
            return false;
        }
        if(SDHolder.getStart(periodSd) > getEndOfDayTime()) {
            return false;
        }
        List<Integer> possibleAttendanceSd = new LinkedList<Integer>();
        int latestEndOption = 0;
        for(int start : getAllValidStartTimes()) {
            // if the start is after the current latestEndOption
            if(start > latestEndOption) {
                // find the latest end time for this start
                for(int end : getEndOptionsForTime(start)) {
                    if(SDHolder.getEnd(end) > latestEndOption) {
                        latestEndOption = SDHolder.getEnd(end);
                    }
                }
                // add an sd reprsenting this start and latest end time to possible attendance
                possibleAttendanceSd.add(SDHolder.getSD(start, SDHolder.getEnd(latestEndOption) - start));
            }
        }
        for(int possible : possibleAttendanceSd) {
            if(SDHolder.inRange(possible, periodSd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * removes a periodSd and options
     *
     * @param periodSd
     * @return true if removed, false if not found
     */
    protected boolean removePeriodSd(int periodSd) {
        if(this.periodSdStore.remove(periodSd) == null) {
            return (false);
        }
        return (true);
    }

    public static final Comparator<TimetableBean> CASE_INSENSITIVE_ORDER = new Comparator<TimetableBean>() {

        @Override
        public int compare(TimetableBean t1, TimetableBean t2) {
            if(t1 == null && t2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(t1 != null && t2 == null) {
                return -1;
            }
            if(t1 == null && t2 != null) {
                return 1;
            }
            // compare
            int result = (t1.getName().toLowerCase()).compareTo(t2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // name not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) t1.getIdentifier()).compareTo((Integer) t2.getIdentifier()));
        }
    };

    /**
     * Compares the specified Object with this TimetableBean for equality.  Returns
     * true if and only if the specified Object is also a TimetableBean, and all
     * corresponding attributes are <em>equal</em>.
     *
     * @param obj the Object to be compared for equality with this TimetableBean
     * @return true if the specified Object is equal to this TimetableBean
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final TimetableBean other = (TimetableBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        if(this.periodSdStore != other.periodSdStore && (this.periodSdStore == null || !this.periodSdStore.equals(other.periodSdStore))) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns the hashCode for this TimetableBean.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 41 * hash + (this.label != null ? this.label.hashCode() : 0);
        hash = 41 * hash + (this.periodSdStore != null ? this.periodSdStore.hashCode() : 0);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<Element>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element("TimetableBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("label", label);
        Element allElements = new Element("periodSdStore_map");
        bean.addContent(allElements);
        for(int periodSd : periodSdStore.keySet()) {
            Element eachKey = new Element("periodSdStore_set");
            allElements.addContent(eachKey);
            eachKey.setAttribute("key", Integer.toString(periodSd));
            for(int option : periodSdStore.get(periodSd)) {
                Element eachElement = new Element("periodSdStore");
                eachKey.addContent(eachElement);
                eachElement.setAttribute("value", Integer.toString(option));
            }
        }
        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to
     * put default values in getAttribute() and check the content
     * of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("TimetableBean");
        // set up the data
        name = bean.getAttributeValue("name");
        label = bean.getAttributeValue("label");
        List<Element> allElements = bean.getChild("periodSdStore_map").getChildren("periodSdStore_set");
        periodSdStore.clear();
        for(Element eachElement : allElements) {
            int periodSdKey = Integer.parseInt(eachElement.getAttributeValue("key", "-1"));
            periodSdStore.put(periodSdKey, new ConcurrentSkipListSet<Integer>());
            @SuppressWarnings("unchecked")
            List<Element> allSubElements = eachElement.getChildren("periodSdStore");
            for(Element eachSubElement : allSubElements) {
                periodSdStore.get(periodSdKey).add(Integer.valueOf(eachSubElement.getAttributeValue("value", "-1")));
            }
        }
    }
}
