/**
 * @(#)DayRangeBean.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:     Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code DayRangeBean} Class used to link a leftRef with a rightRef for
 * a YWD
 *
 * @author      Darryl Oatridge
 * @version 	1.01 18-July-2010
 * @see YWDHolder
 */
public class DayRangeBean extends ObjectBean {

    private static final long serialVersionUID = 20100718101L;
    private volatile int leftRef;
    private volatile int rightRef;
    private volatile int startYwd;
    private volatile int endYwd;
    private final boolean[] days;

    /**
     * Creates a DayRange giving the start, end and room the range applies to.
     * The days of the week allow you to define only certain days of the week to
     * apply this range to and the daySlotId, the reference of the DaySlotConfig this
     * range is applying.
     *
     * @param rangeId
     * @param leftRef room number this applies to
     * @param days
     * @param startYwd
     * @param endYwd
     * @param rightRef
     */
    public DayRangeBean(int rangeId, int leftRef, int rightRef, int startYwd, int endYwd, boolean[] days, String owner) {
        super(rangeId, owner);
        this.leftRef = leftRef;
        this.rightRef = rightRef;
        this.startYwd = startYwd;
        this.endYwd = endYwd;
        this.days = new boolean[7];
        int length = days.length < 7 ? days.length : 7;
        System.arraycopy(days, 0, this.days, 0, length);
    }

    public DayRangeBean() {
        super();
        this.leftRef = -1;
        this.startYwd = -1;
        this.endYwd = -1;
        this.rightRef = -1;
        this.days = new boolean[7];
    }

    /**
     * Returns the identifier for this bean
     */
    public int getDayRangeId() {
        return super.getIdentifier();
    }

    /**
     * a shortened version of getDayRangeId
     */
    public int getRangeId() {
        return super.getIdentifier();
    }

    /**
     * get the start ywd of this dayRange
     */
    public int getStartYwd() {
        return startYwd;
    }

    /**
     * get the end ywd of this dayRange
     */
    public int getEndYwd() {
        return endYwd;
    }

    /**
     * gets the room this applies to
     */
    public int getLeftRef() {
        return leftRef;
    }

    /**
     * get the days of the week this applies too
     */
    public boolean[] getDays() {
        return days;
    }

    /**
     * gets the refId  the range applies to
     */
    public int getRightRef() {
        return rightRef;
    }

    protected void setStartYwd(int startYwd, String owner) {
        this.startYwd = startYwd;
        super.setOwner(owner);
    }

    protected void setEndYwd(int endYwd, String owner) {
        this.endYwd = endYwd;
        super.setOwner(owner);
    }

    /**
     * passing the ywd and tests if these are in range. If you want to just check
     * yw0 then set d between 7 and 9.
     *
     * @param ywd the ywd to check (set day greater than 6 to ignore day)
     * @param leftRef the room to be tested
     * @return true if within day
     */
    public boolean inRange(int ywd, int leftRef) {
        if(this.leftRef == leftRef) {
            if(YWDHolder.getDay(ywd) > 6) {
                int endYw0 = YWDHolder.getYW(endYwd);
                int startYw0 = YWDHolder.getYW(startYwd);
                int yw0 = YWDHolder.getYW(ywd);
                if(yw0 >= startYw0 && yw0 <= endYw0) {
                    return (true);
                }
                return (false);
            }
            if(ywd >= startYwd && ywd <= endYwd && days[YWDHolder.getDay(ywd)]) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Returns if a date range from start year, week, day to end year week day for a particular room
     * and an array of days, falls within the objects rage start end. This is used to check against
     * a range
     *
     * @param leftRef
     * @param start
     * @param end
     * @param rangeDays the days this applies to true = applies
     * @return true if within week.
     */
    public boolean inRange(int leftRef, int start, int end, boolean[] rangeDays) {
        if(this.leftRef != leftRef) {
            return (false);
        }
        if(rangeDays.length != 7) {
            return (false);
        }
        if(!YWDHolder.isValid(start) || !YWDHolder.isValid(end)) {
            return (false);
        }
        if(start > end) {
            return (false);
        }
        if(end < startYwd || start > endYwd) {
            return (false);
        }

        // what is the smallest range
        int bestStart = start > startYwd ? start : startYwd;
        int bestEnd = end < endYwd ? end : endYwd;
        // same week year
        if(YWDHolder.getYW(bestStart) == YWDHolder.getYW(bestEnd)) {
            if(checkRange(leftRef, bestStart, bestEnd, rangeDays)) {
                return (true);
            }
        }
        // check start week and end week
        if(checkRange(leftRef, bestStart, YWDHolder.getYW(bestStart) + 6, rangeDays)
                || checkRange(leftRef, YWDHolder.getYW(bestEnd), bestEnd, rangeDays)) {
            return (true);
        }
        //check a complete week in te middle
        int startWeek = YWDHolder.getWeek(start);
        int endWeek = YWDHolder.getWeek(end);
        // check if it goes over a year
        if(YWDHolder.getYear(start) < YWDHolder.getYear(end)) {
            endWeek += YWDHolder.weeksInYear(start);
        }
        if(startWeek < endWeek - 1 || YWDHolder.getYear(start) < YWDHolder.getYear(end) - 1) {
            if(checkRange(leftRef, YWDHolder.getYW(bestStart) + 10, YWDHolder.getYW(bestStart) + 16, rangeDays)) {
                return (true);
            }
        }
        return (false);
    }

    private boolean checkRange(int leftRef, int start, int end, boolean[] rangeDays) {
        boolean inRange = false;
        for(int ywd = start; ywd <= end; ywd++) {
            if(YWDHolder.getDay(ywd) > 6) {
                ywd += 3;
            }
            if(YWDHolder.getWeek(ywd) > YWDHolder.weeksInYear(ywd)) {
                // moves to next year week 1
                ywd += 1000 - (YWDHolder.weeksInYear(ywd) * 10);
                // this zeros the days
                ywd -= (ywd % 10);
            }
            if(rangeDays[YWDHolder.getDay(ywd)] && inRange(ywd, leftRef)) {
                inRange = true;
            }
        }
        return (inRange);
    }

    public boolean same(DayRangeBean dayRange) {
        if(this.leftRef != dayRange.getLeftRef()) {
            return false;
        }
        if(this.rightRef != dayRange.getRightRef()) {
            return false;
        }
        if(this.startYwd != dayRange.getStartYwd()) {
            return false;
        }
        if(this.endYwd != dayRange.getEndYwd()) {
            return false;
        }
        if(!Arrays.equals(this.days, dayRange.getDays())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final DayRangeBean other = (DayRangeBean) obj;
        if(this.leftRef != other.leftRef) {
            return false;
        }
        if(this.rightRef != other.rightRef) {
            return false;
        }
        if(this.startYwd != other.startYwd) {
            return false;
        }
        if(this.endYwd != other.endYwd) {
            return false;
        }
        if(!Arrays.equals(this.days, other.days)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.leftRef;
        hash = 97 * hash + this.rightRef;
        hash = 97 * hash + this.startYwd;
        hash = 97 * hash + this.endYwd;
        hash = 97 * hash + Arrays.hashCode(this.days);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element(VT.DayRange.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("startYwd", Integer.toString(startYwd));
        bean.setAttribute("endYwd", Integer.toString(endYwd));
        bean.setAttribute("leftRef", Integer.toString(leftRef));
        bean.setAttribute("rightRef", Integer.toString(rightRef));
        Element allElements = new Element("days_array");
        bean.addContent(allElements);
        for(int i = 0; i < days.length; i++) {
            Element eachElement = new Element("days");
            eachElement.setAttribute("index", Integer.toString(i));
            eachElement.setAttribute("value", Boolean.toString(days[i]));
            allElements.addContent(eachElement);
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
        Element bean = root.getChild(VT.DayRange.bean());
        // set up the data
        startYwd = Integer.parseInt(bean.getAttributeValue("startYwd", "0"));
        endYwd = Integer.parseInt(bean.getAttributeValue("endYwd", "0"));
        leftRef = Integer.parseInt(bean.getAttributeValue("leftRef", "0"));
        rightRef = Integer.parseInt(bean.getAttributeValue("rightRef", "0"));
        @SuppressWarnings("unchecked")
        List<Element> allElements = (List<Element>) bean.getChild("days_array").getChildren("days");
        for(Element eachDay : allElements) {
            int dow = Integer.valueOf(eachDay.getAttributeValue("index","0"));
            if(dow < days.length) {
                days[dow] = Boolean.valueOf(eachDay.getAttributeValue("value","false"));
            }
        }
    }
}
