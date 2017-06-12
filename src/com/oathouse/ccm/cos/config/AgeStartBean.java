/*
 * @(#)InvoiceBean.java
 *
 * Copyright:	Copyright (c) 2010
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
 * The {@code AgeStartBean} Class
 *
 * @author Nick Maunder
 * @version 2.00 14-Dec-2010
 */
public class AgeStartBean extends ObjectBean {

    private static final long serialVersionUID = 20101214200L;
    private volatile String name;
    private volatile int startAgeMonths;

    public AgeStartBean(int ageId, String name, int startAgeMonths, String owner) {
        super(ageId, owner);
        this.name = name != null ? name : "";
        this.startAgeMonths = startAgeMonths;
    }

    public AgeStartBean() {
        super();
        this.name = "";
        this.startAgeMonths = ObjectEnum.INITIALISATION.value();
    }

    public int getAgeStartId() {
        return super.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public int getStartAgeMonths() {
        return startAgeMonths;
    }

    public static final Comparator<AgeStartBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<AgeStartBean>() {
        public int compare(AgeStartBean a1, AgeStartBean a2) {
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
            int result = a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // names might not be unique so violates the equals comparability. Can cause disappearing objects in Sets
            return(((Integer)a1.getIdentifier()).compareTo((Integer)a2.getIdentifier()));
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
        final AgeStartBean other = (AgeStartBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if(this.startAgeMonths != other.startAgeMonths) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 31 * hash + this.startAgeMonths;
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
        Element bean = new Element("AgeStartBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("startAgeMonths", Integer.toString(startAgeMonths));
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
        Element bean = root.getChild("AgeStartBean");
        // set up the data
        name = bean.getAttributeValue("name", "");
        startAgeMonths = Integer.parseInt(bean.getAttributeValue("startAgeMonths", "-1"));
    }


}
