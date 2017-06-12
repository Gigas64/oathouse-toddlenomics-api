/*
 * @(#)EducationTimetableBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.education;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetBean;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jdom2.Element;

/**
 * The {@code EducationTimetableBean} Class holds a set of
 * periods that represent the education periods during a day.
 * The periods are stored as SDHolder key values. and referenced using
 * the childId.
 *
 * @author Darryl Oatridge
 * @version 1.00 02-Aug-2010
 */
public class ChildEducationTimetableBean extends ObjectSetBean {

    private static final long serialVersionUID = 20100802100L;
    private volatile int roomId;

    public ChildEducationTimetableBean(int cetId, String name, String label, int roomId, Set<Integer> educationSd, String owner) {
        super(cetId, name, label, educationSd, owner);
        this.roomId = roomId;
    }

    public ChildEducationTimetableBean() {
        super();
        this.roomId = ObjectEnum.INITIALISATION.value();
    }

    /**
     * Returns the identifier for this bean
     */
    public int getChildEducationTimetableId() {
        return super.getIdentifier();
    }

    /**
     * same as getChildEducationTimetableId()
     */
    public int getCetId() {
        return(super.getIdentifier());
    }

    @Override
    public String getName() {
        return (super.getName());
    }

    @Override
    public String getLabel() {
        return (super.getLabel());
    }

    public int getRoomId() {
        return roomId;
    }

    public Set<Integer> getAllEducationSd() {
        return (super.getValueStore());
    }

    public int getSessionCount() {
        return (super.getValueStore().size());
    }

    /**
     * Determines whether a time (mins from midnight) is within any of the sessions
     * @param time
     * @return
     */
    public boolean isInSession(int time) {
        int compareSd = SDHolder.getSD(time, 0);
        for(int sd:super.getValueStore()) {
            if(SDHolder.inRange(sd, compareSd)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ChildEducationTimetableBean other = (ChildEducationTimetableBean) obj;
         if(this.roomId != other.roomId) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.roomId;
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
        Element bean = new Element("ChildEducationTimetableBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("roomId", Integer.toString(roomId));
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
        Element bean = root.getChild("ChildEducationTimetableBean");
        // set up the data
        roomId = Integer.parseInt(bean.getAttributeValue("roomId", "-1"));
    }

}
