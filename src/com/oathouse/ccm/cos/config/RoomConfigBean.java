/**
 * @(#)RoomConfigBean.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code RoomConfigBean} Class
 *
 * @author 		Darryl Oatridge
 * @version 	1.01 18-July-2010
 */
public class RoomConfigBean extends ObjectBean {
    private static final long serialVersionUID = 20100718101L;

    private volatile String name;
    private volatile int ageRangeId;
    private volatile int capacity;
    private volatile int fullBuffer;
    private volatile int floorArea;

    public RoomConfigBean(int roomId, int ageRangeId, String name, int capacity, int fullBuffer, int floorArea, String owner) {
        super(roomId, owner);
        this.name = name != null ? name : "";
        this.ageRangeId = ageRangeId;
        this.capacity = capacity;
        this.fullBuffer = fullBuffer;
        this.floorArea = floorArea;
    }

    public RoomConfigBean() {
        super();
        this.name = "";
        this.ageRangeId = ObjectEnum.INITIALISATION.value();
        this.capacity = ObjectEnum.INITIALISATION.value();
        this.fullBuffer = ObjectEnum.INITIALISATION.value();
        this.floorArea = ObjectEnum.INITIALISATION.value();
    }

    /**
     * Returns the identifier for this bean
     */
    public int getRoomConfigId() {
        return super.getIdentifier();
    }

    /**
     * same as getRoomConfigId
     */
    public int getRoomId() {
        return super.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public int getAgeRangeId() {
        return ageRangeId;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFullBuffer() {
        return fullBuffer;
    }

    public int getFloorArea() {
        return floorArea;
    }

    public static final Comparator<RoomConfigBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<RoomConfigBean>() {
        @Override
        public int compare(RoomConfigBean p1, RoomConfigBean p2) {
            if(p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(p1 != null && p2 == null) {
                return -1;
            }
            if(p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) p1.getIdentifier()).compareTo((Integer) p2.getIdentifier()));
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
        final RoomConfigBean other = (RoomConfigBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if(this.ageRangeId != other.ageRangeId) {
            return false;
        }
        if(this.capacity != other.capacity) {
            return false;
        }
        if(this.fullBuffer != other.fullBuffer) {
            return false;
        }
        if(this.floorArea != other.floorArea) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 13 * hash + this.ageRangeId;
        hash = 13 * hash + this.capacity;
        hash = 13 * hash + this.fullBuffer;
        hash = 13 * hash + this.floorArea;
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
        Element bean = new Element("RoomConfigBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("ageRangeId", Integer.toString(ageRangeId));
        bean.setAttribute("capacity", Integer.toString(capacity));
        bean.setAttribute("fullBuffer", Integer.toString(fullBuffer));
        bean.setAttribute("floorArea", Integer.toString(floorArea));
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
        Element bean = root.getChild("RoomConfigBean");
        // set up the data
        name = bean.getAttributeValue("name");
        ageRangeId = Integer.parseInt(bean.getAttributeValue("ageRangeId", "-1"));
        capacity = Integer.parseInt(bean.getAttributeValue("capacity", "-1"));
        fullBuffer = Integer.parseInt(bean.getAttributeValue("fullBuffer", "-1"));
        floorArea = Integer.parseInt(bean.getAttributeValue("floorArea", "-1"));
    }

}
