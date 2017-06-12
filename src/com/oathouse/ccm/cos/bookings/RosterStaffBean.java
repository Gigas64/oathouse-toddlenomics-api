/*
 * @(#)RosterStaffBean.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code RosterStaffBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 19-May-2013
 */
public class RosterStaffBean extends ObjectBean {

    private static final long serialVersionUID = 20130519100L;
    private volatile int ywd;
    private volatile int activityId;
    private volatile int rosterSd; //the roster SDHolder key value for the allocation period
    private volatile int actualStart; // when the Roster actually started
    private volatile int actualEnd; // when the Roster actually ended
    private volatile String notes;

    public RosterStaffBean() {
        super();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.activityId = ObjectEnum.INITIALISATION.value();
        this.rosterSd = ObjectEnum.INITIALISATION.value();
        this.actualStart = ObjectEnum.INITIALISATION.value();
        this.actualEnd = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    public RosterStaffBean(int identifier, int groupKey, int ywd, int activityId, int rosterSd, int actualStart, int actualEnd, String notes, String owner) {
        super(identifier, groupKey, owner);
        this.ywd = ywd;
        this.activityId = activityId;
        this.rosterSd = rosterSd;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;
        this.notes = notes;
    }

    public int getId() {
        return super.getIdentifier();
    }

    public int getKey() {
        return super.getGroupKey();
    }

    public int getYwd() {
        return ywd;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getRosterSd() {
        return rosterSd;
    }

    public int getActualStart() {
        return actualStart;
    }

    public int getActualEnd() {
        return actualEnd;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.ywd;
        hash = 83 * hash + this.activityId;
        hash = 83 * hash + this.rosterSd;
        hash = 83 * hash + this.actualStart;
        hash = 83 * hash + this.actualEnd;
        hash = 83 * hash + Objects.hashCode(this.notes);
        return hash + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final RosterStaffBean other = (RosterStaffBean) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.activityId != other.activityId) {
            return false;
        }
        if(this.rosterSd != other.rosterSd) {
            return false;
        }
        if(this.actualStart != other.actualStart) {
            return false;
        }
        if(this.actualEnd != other.actualEnd) {
            return false;
        }
        if(!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        return super.equals(obj);
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
        Element bean = new Element("RosterStaffBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("activityId", Integer.toString(activityId));
        bean.setAttribute("rosterSd", Integer.toString(rosterSd));
        bean.setAttribute("actualStart", Integer.toString(actualStart));
        bean.setAttribute("actualEnd", Integer.toString(actualEnd));
        bean.setAttribute("notes", notes);

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return(rtnList);
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
        Element bean = root.getChild("RosterStaffBean");
        // set up the data
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        activityId = Integer.parseInt(bean.getAttributeValue("activityId", "-1"));
        rosterSd = Integer.parseInt(bean.getAttributeValue("rosterSd", "-1"));
        actualStart = Integer.parseInt(bean.getAttributeValue("actualStart", "-1"));
        actualEnd = Integer.parseInt(bean.getAttributeValue("actualEnd", "-1"));
        notes = bean.getAttributeValue("notes", "");

    }

}
