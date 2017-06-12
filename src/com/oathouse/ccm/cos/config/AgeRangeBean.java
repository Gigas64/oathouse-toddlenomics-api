/**
 * @(#)AgeRangeBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetBean;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jdom2.Element;

/**
 * The {@code AgeRangeBean} Class is an AgeRangeBean to store AgeRange Information.
 *
 *
 * @author      Darryl Oatridge
 * @version 	1.04 30-Dec-2011
 */
public class AgeRangeBean extends ObjectSetBean {

    private static final long serialVersionUID = 20100718103L;
    private volatile int dobAdd; // days to add to the dob for automatic transfer
    private volatile int specialNoticeDays; // special notice period in ywd
    private volatile int standardNoticeDays; // standard notice period in ywd
    private volatile int requiredStaffRatio; // staff ratio requirement for child
    private volatile int requiredFloorArea; // floor area requirement for child

    /**
     * Constructor for {@code AgeRangeBean} Class. This is a read only class
     *
     * @param ageRangeId the id of the range
     * @param name a name reference
     * @param label a label reference
     * @param ywd
     * @param dobTo
     * @param specialNoticeDays
     * @param standardNoticeDays
     * @param requiredStaffRatio
     * @param requiredFloorArea
     * @param owner
     */
    public AgeRangeBean(int ageRangeId, String name, String label, int ywd, Set<Integer> dobTo,
            int specialNoticeDays, int standardNoticeDays, int requiredStaffRatio, int requiredFloorArea, String owner) {
        super(ageRangeId, name, label, dobTo, owner);
        this.dobAdd = ywd;
        this.specialNoticeDays = specialNoticeDays;
        this.standardNoticeDays = standardNoticeDays;
        this.requiredStaffRatio = requiredStaffRatio;
        this.requiredFloorArea = requiredFloorArea;
    }

    public AgeRangeBean() {
        super();
        this.dobAdd = ObjectEnum.INITIALISATION.value();
        this.specialNoticeDays = ObjectEnum.INITIALISATION.value();
        this.standardNoticeDays = ObjectEnum.INITIALISATION.value();
        this.requiredStaffRatio = ObjectEnum.INITIALISATION.value();
        this.requiredFloorArea = ObjectEnum.INITIALISATION.value();
    }

/**
     * gets the yw0 based on a date of birth that fits with the
     * criteria of the DayRangeBean dobAdd and dobTo. This provides
     * a rough idea of when a child will start in a room.
     *
     * @param dob the date of birth
     * @return the calculated yw0
     */
    public int getAgeRangeYW(int dob) {
        int startYwd = YWDHolder.add(dob, dobAdd);
        // if there are no dobTos, return the start of the first week on or after startYwd
        if(getDobTo().isEmpty()) {
            if(YWDHolder.getYW(startYwd)==startYwd) {
                return (startYwd);
            }
            else {
                return YWDHolder.getYW(YWDHolder.add(startYwd, 10));
            }
        }
        int week;
        int year = YWDHolder.getYear(startYwd);
        // find first week in dobTo after rtnDob week
        for(int ywd : getDobTo()) {
            int toYw = YWDHolder.getYW(year, YWDHolder.getWeek(ywd));
            if(toYw>=startYwd) {
                return (toYw);
            }

        }
        // must be first dobTo in following year
        year++;
        week = YWDHolder.getWeek(getValueStore().first());
        return (YWDHolder.getYW(year, week));
    }

    public int getAgeRangeId() {
        return super.getIdentifier();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getLabel() {
        return super.getLabel();
    }

    public int getDobAdd() {
        return dobAdd;
    }

    public Set<Integer> getDobTo() {
        return super.getValueStore();
    }

    public int getStandardNoticeDays() {
        return standardNoticeDays;
    }

    public int getSpecialNoticeDays() {
        return specialNoticeDays;
    }

    public int getRequiredStaffRatio() {
        return requiredStaffRatio;
    }

    public int getRequiredFloorArea() {
        return requiredFloorArea;
    }

    public int getStandardNoticeYwd() {
        return YWDHolder.getYwdFromDays(standardNoticeDays);
    }

    public int getSpecialNoticeYwd() {
        return YWDHolder.getYwdFromDays(specialNoticeDays);
    }

    public static final Comparator<AgeRangeBean> DOB_ADD_ORDER = new Comparator<AgeRangeBean>() {
        @Override
        public int compare(AgeRangeBean a1, AgeRangeBean a2) {
            if (a1 == null && a2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (a1 != null && a2 == null) {
                return -1;
            }
            if (a1 == null && a2 != null) {
                return 1;
            }
            // compare
            int result = ((Integer)a1.getDobAdd()).compareTo((Integer)a2.getDobAdd());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)a1.getIdentifier()).compareTo((Integer)a2.getIdentifier()));
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final AgeRangeBean other = (AgeRangeBean) obj;
        if(this.dobAdd != other.dobAdd) {
            return false;
        }
        if(this.specialNoticeDays != other.specialNoticeDays) {
            return false;
        }
        if(this.standardNoticeDays != other.standardNoticeDays) {
            return false;
        }
        if(this.requiredStaffRatio != other.requiredStaffRatio) {
            return false;
        }
        if(this.requiredFloorArea != other.requiredFloorArea) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.dobAdd;
        hash = 71 * hash + this.specialNoticeDays;
        hash = 71 * hash + this.standardNoticeDays;
        hash = 71 * hash + this.requiredStaffRatio;
        hash = 71 * hash + this.requiredFloorArea;
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
        Element bean = new Element("AgeRangeBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("dobAdd", Integer.toString(dobAdd));
        bean.setAttribute("standardNoticeDays", Integer.toString(standardNoticeDays));
        bean.setAttribute("specialNoticeDays", Integer.toString(specialNoticeDays));
        bean.setAttribute("requiredStaffRatio", Integer.toString(requiredStaffRatio));
        bean.setAttribute("requiredFloorArea", Integer.toString(requiredFloorArea));
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
        Element bean = root.getChild("AgeRangeBean");
        // set up the data
        dobAdd = Integer.parseInt(bean.getAttributeValue("dobAdd", "-1"));
        standardNoticeDays = Integer.parseInt(bean.getAttributeValue("standardNoticeDays", "-1"));
        specialNoticeDays = Integer.parseInt(bean.getAttributeValue("specialNoticeDays", "-1"));
        requiredStaffRatio = Integer.parseInt(bean.getAttributeValue("requiredStaffRatio", "-1"));
        requiredFloorArea = Integer.parseInt(bean.getAttributeValue("requiredFloorArea", "-1"));
    }

}
