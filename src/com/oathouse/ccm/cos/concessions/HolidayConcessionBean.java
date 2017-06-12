/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)HolidayConcessionBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.concessions;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code HolidayConcessionBean} Class holds the holiday concession information
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Aug-2010
 */
public class HolidayConcessionBean extends ObjectBean {

    private static final long serialVersionUID = 20100810100L;
    private volatile int defaultAllocation;
    private volatile int countFrom; // -1 is childStartYwd, 0-11 are 1st of each month
    private volatile boolean limitProRata;

    private HolidayConcessionBean(int holidayConcessionId, int defaultAllocation, int countFrom, boolean limitProRata, String owner) {
        super(holidayConcessionId, owner);
        this.defaultAllocation = defaultAllocation;
        this.countFrom = countFrom;
        this.limitProRata = limitProRata;
    }

    public HolidayConcessionBean(int defaultAllocation, int countFrom, boolean limitProRata, String owner) {
        this(ObjectEnum.DEFAULT_ID.value(), defaultAllocation, countFrom, limitProRata, owner);
    }

    public HolidayConcessionBean() {
        super();
        this.defaultAllocation = ObjectEnum.INITIALISATION.value();
        this.countFrom = ObjectEnum.INITIALISATION.value();
        this.limitProRata = false;
    }

    public int getHolidayConcessionId() {
        return super.getIdentifier();
    }

    public int getCountFrom() {
        return countFrom;
    }

    public int getDefaultAllocation() {
        return defaultAllocation;
    }

    public boolean isLimitProRata() {
        return limitProRata;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final HolidayConcessionBean other = (HolidayConcessionBean) obj;
        if(this.defaultAllocation != other.defaultAllocation) {
            return false;
        }
        if(this.countFrom != other.countFrom) {
            return false;
        }
        if(this.limitProRata != other.limitProRata) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + this.defaultAllocation;
        hash = 31 * hash + this.countFrom;
        if(this.limitProRata) {
            hash = 31 * hash + 1;
        }
        return (hash + super.hashCode());
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
        Element bean = new Element("HolidayConcessionBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("defaultAllocation", Integer.toString(defaultAllocation));
        bean.setAttribute("countFrom", Integer.toString(countFrom));
        bean.setAttribute("limitProRata", Boolean.toString(limitProRata));
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
        Element bean = root.getChild("HolidayConcessionBean");
        // set up the data
        defaultAllocation = Integer.parseInt(bean.getAttributeValue("defaultAllocation", "-1"));
        countFrom = Integer.parseInt(bean.getAttributeValue("countFrom", "-1"));
        limitProRata = Boolean.parseBoolean(bean.getAttributeValue("limitProRata", "false"));
    }

}
