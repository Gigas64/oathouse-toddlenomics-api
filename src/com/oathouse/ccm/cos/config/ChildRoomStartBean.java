/*
 * @(#)ChildRoomStartBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * The {@code ChildRoomStartBean} Class Stores is used to identify
 * the child start ywd in a defined room. The id used is the room
 *
 * @author Darryl Oatridge
 * @version 1.01 25-Sep-2010
 */
public class ChildRoomStartBean extends ObjectBean {

    private static final long serialVersionUID = 20100925101L;
    private volatile int startYwd;

    public ChildRoomStartBean(int roomId, int startDate, String owner) {
        super(roomId, owner);
        this.startYwd = startDate;
    }

    public ChildRoomStartBean() {
        super();
        this.startYwd = ObjectEnum.INITIALISATION.value();
    }

    /**
     * Returns the identifier for this bean
     */
    public int getChildRoomStartId() {
        return super.getIdentifier();
    }

    /**
     * The childRoomStartId is the roomId
     * @return the childRoomStartId
     */
    public int getRoomId() {
        return(super.getIdentifier());
    }

    public int getStartYwd() {
        return startYwd;
    }

    @Override
    public int compareTo(ObjectBean other) {
        ChildRoomStartBean comp = (ChildRoomStartBean) other;
        return(startYwd - comp.getStartYwd());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ChildRoomStartBean other = (ChildRoomStartBean) obj;
        if(this.startYwd != other.startYwd) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.startYwd;
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
        Element bean = new Element("ChildRoomStartBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("startYwd", Integer.toString(startYwd));
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
        Element bean = root.getChild("ChildRoomStartBean");
        // set up the data
        startYwd = Integer.parseInt(bean.getAttributeValue("startYwd", "-1"));
    }

}
