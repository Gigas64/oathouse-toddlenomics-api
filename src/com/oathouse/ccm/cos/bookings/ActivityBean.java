/*
 * @(#)ActivityBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code ActivityBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 22-Sep-2011
 */
public class ActivityBean extends ObjectBean {

    private static final long serialVersionUID = 20110922100L;

    private volatile String name;
    private volatile int payRate;
    private volatile ActivityTypeEnum activityType;
    private volatile ActivityFlagEnum activityFlag;

    public ActivityBean() {
        super();
        this.name = "";
        this.payRate = ObjectEnum.INITIALISATION.value();
        this.activityType = ActivityTypeEnum.UNDEFINED;
        this.activityFlag = ActivityFlagEnum.UNDEFINED;
    }

    public ActivityBean(int identifier, int groupKey, String name, int payRate, ActivityTypeEnum activityType, ActivityFlagEnum activityFlag, String owner) {
        super(identifier, groupKey, owner);
        this.name = name;
        this.payRate = payRate;
        this.activityType = activityType;
        this.activityFlag = activityFlag;
    }

    public int getId() {
        return super.getIdentifier();
    }

    public int getKey() {
        return super.getGroupKey();
    }

    public String getName() {
        return name;
    }

    public int getPayRate() {
        return payRate;
    }

    public boolean isPaid() {
        return payRate > 0 ? true : false;
    }

    public ActivityTypeEnum getActivityType() {
        return activityType;
    }

    public ActivityFlagEnum getActivityFlag() {
        return activityFlag;
    }

    public static final Comparator<ActivityBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<ActivityBean>() {
        @Override
        public int compare(ActivityBean p1, ActivityBean p2) {
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
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + this.payRate;
        hash = 43 * hash + (this.activityType != null ? this.activityType.hashCode() : 0);
        hash = 43 * hash + (this.activityFlag != null ? this.activityFlag.hashCode() : 0);
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
        final ActivityBean other = (ActivityBean) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(this.payRate != other.payRate) {
            return false;
        }
        if(this.activityType != other.activityType) {
            return false;
        }
        if(this.activityFlag != other.activityFlag) {
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
        Element bean = new Element("ActivityBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("payRate",Integer.toString(payRate));
        bean.setAttribute("activityType",activityType.toString());
        bean.setAttribute("activityFlag",activityFlag.toString());

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
        Element bean = root.getChild("ActivityBean");
        // set up the data
        name = bean.getAttributeValue("name", "");
        payRate = Integer.parseInt(bean.getAttributeValue("payRate", "-1"));
        activityType = ActivityTypeEnum.valueOf(bean.getAttributeValue("activityType", "NO_VALUE"));
        activityFlag = ActivityFlagEnum.valueOf(bean.getAttributeValue("activityFlag", "NO_VALUE"));

    }
}
