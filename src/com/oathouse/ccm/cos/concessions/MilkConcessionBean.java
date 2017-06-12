/*
 * @(#)MilkConcessionBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.concessions;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jdom2.Element;

/**
 * The {@code MilkConcessionBean} Class is what it is, a milk concessions bean that
 * stores applications concessions
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Aug-2010
 */
public class MilkConcessionBean extends ObjectBean {

    private static final long serialVersionUID = 20100810100L;
    private volatile int claimMinMonths;
    private volatile int claimMaxMonths;
    private final ConcurrentSkipListSet<Integer> sessionTimes;
    private volatile double entitlement;
    private volatile String units;

    private MilkConcessionBean(int milkConcessionId, int claimMinMonths, int claimMaxMonths,
            Set<Integer> sessionTimes, double entitlement, String units, String owner) {
        super(milkConcessionId, owner);
        this.claimMinMonths = claimMinMonths;
        this.claimMaxMonths = claimMaxMonths;
        this.sessionTimes = new ConcurrentSkipListSet<Integer>();
        if(sessionTimes != null) {
            this.sessionTimes.addAll(sessionTimes);
        }
        this.entitlement = entitlement;
        this.units = units != null ? units : "";
    }

    public MilkConcessionBean(int claimMinMonths, int claimMaxMonths,
            Set<Integer> sessionTimes, double entitlement, String units, String owner) {
       this(ObjectEnum.DEFAULT_ID.value(), claimMinMonths, claimMaxMonths, sessionTimes, entitlement, units, owner);
    }

    public MilkConcessionBean() {
        super();
        this.claimMinMonths = ObjectEnum.INITIALISATION.value();
        this.claimMaxMonths = ObjectEnum.INITIALISATION.value();
        this.sessionTimes = new ConcurrentSkipListSet<Integer>();
        this.entitlement = ObjectEnum.INITIALISATION.value();
        this.units = "";
    }
    public int getMilkConcessionId() {
        return super.getIdentifier();
    }

    public int getClaimMaxMonths() {
        return claimMaxMonths;
    }

    public int getClaimMinMonths() {
        return claimMinMonths;
    }

    public double getEntitlement() {
        return entitlement;
    }

    public Set<Integer> getSessionTimes() {
        return new ConcurrentSkipListSet<Integer>(sessionTimes);
    }

    public String getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final MilkConcessionBean other = (MilkConcessionBean) obj;
        if(this.claimMinMonths != other.claimMinMonths) {
            return false;
        }
        if(this.claimMaxMonths != other.claimMaxMonths) {
            return false;
        }
        if(this.sessionTimes != other.sessionTimes && (this.sessionTimes == null || !this.sessionTimes.equals(other.sessionTimes))) {
            return false;
        }
        if(Double.doubleToLongBits(this.entitlement) != Double.doubleToLongBits(other.entitlement)) {
            return false;
        }
        if((this.units == null) ? (other.units != null) : !this.units.equals(other.units)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.claimMinMonths;
        hash = 29 * hash + this.claimMaxMonths;
        hash = 29 * hash + (this.sessionTimes != null ? this.sessionTimes.hashCode() : 0);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.entitlement) ^ (Double.doubleToLongBits(this.entitlement) >>> 32));
        hash = 29 * hash + (this.units != null ? this.units.hashCode() : 0);
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
        Element bean = new Element("MilkConcessionBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("claimMinMonths", Integer.toString(claimMinMonths));
        bean.setAttribute("claimMaxMonths", Integer.toString(claimMaxMonths));
        bean.setAttribute("entitlement", Double.toString(entitlement));
        bean.setAttribute("units", units);
        Element allElements = new Element("sessionTimes_set");
        bean.addContent(allElements);
        if(sessionTimes != null) {
            for(int i : sessionTimes) {
                Element eachElement = new Element("sessionTime");
                eachElement.setAttribute("value", Integer.toString(i));
                allElements.addContent(eachElement);
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
        Element bean = root.getChild("MilkConcessionBean");
        // set up the data
        claimMinMonths = Integer.parseInt(bean.getAttributeValue("claimMinMonths", "0"));
        claimMaxMonths = Integer.parseInt(bean.getAttributeValue("claimMaxMonths", "0"));
        entitlement = Double.parseDouble(bean.getAttributeValue("entitlement", "0"));
        units = bean.getAttributeValue("units", "pints");
        @SuppressWarnings("unchecked")
        List<Element> allElements = (List<Element>) bean.getChild("sessionTimes_set").getChildren("sessionTime");
        sessionTimes.clear();
        for(Element eachElement : allElements) {
            sessionTimes.add(Integer.parseInt(eachElement.getAttributeValue("value", "-1")));
        }
    }
}
