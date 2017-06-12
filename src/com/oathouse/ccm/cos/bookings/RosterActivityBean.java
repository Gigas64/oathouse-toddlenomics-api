/*
 * @(#)RosterActivityBean.java
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
 * The {@code RosterActivityBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 19-May-2013
 */
public class RosterActivityBean extends ObjectBean {

    private static final long serialVersionUID = 20130519100L;
    private volatile int rosterId; // the staff roster this activity roster belongs to
    private volatile int activityId; // the The activity id associated with this activityroster
    private volatile int rosterSd; //the roster SDHolder key value for the allocation period
    private volatile String notes;

    public RosterActivityBean() {
        super();
        this.rosterId = ObjectEnum.INITIALISATION.value();
        this.activityId = ObjectEnum.INITIALISATION.value();
        this.rosterSd = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    public RosterActivityBean(int identifier, int groupKey, int rosterId, int activityId, int rosterSd, String notes, String owner) {
        super(identifier, groupKey, owner);
        this.rosterId = rosterId;
        this.activityId = activityId;
        this.rosterSd = rosterSd;
        this.notes = notes;
    }

    public int getId() {
        return super.getIdentifier();
    }

    public int getKey() {
        return super.getGroupKey();
    }

    public int getRosterId() {
        return rosterId;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getRosterSd() {
        return rosterSd;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.rosterId;
        hash = 83 * hash + this.activityId;
        hash = 83 * hash + this.rosterSd;
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
        final RosterActivityBean other = (RosterActivityBean) obj;
        if(this.rosterId != other.rosterId) {
            return false;
        }
        if(this.activityId != other.activityId) {
            return false;
        }
        if(this.rosterSd != other.rosterSd) {
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
        Element bean = new Element("RosterActivityBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("rosterId", Integer.toString(rosterId));
        bean.setAttribute("activityId", Integer.toString(activityId));
        bean.setAttribute("rosterSd", Integer.toString(rosterSd));
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
        Element bean = root.getChild("RosterActivityBean");
        // set up the data
        rosterId = Integer.parseInt(bean.getAttributeValue("rosterId", "-1"));
        activityId = Integer.parseInt(bean.getAttributeValue("activityId", "-1"));
        rosterSd = Integer.parseInt(bean.getAttributeValue("rosterSd", "-1"));
        notes = bean.getAttributeValue("notes", "");
    }

}
