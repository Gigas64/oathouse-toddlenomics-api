/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)TariffBean.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code TariffBean} Class represents a name/label for
 * a group of price or loyalty beans that are
 * associated through a relationship using a Relation
 *
 * @author Darryl Oatridge
 * @version 1.00 04-May-2012
 */
public class TariffBean extends ObjectBean {

    private static final long serialVersionUID = 20120504100L;
    private volatile String name;
    private volatile String label;

    public TariffBean(int tariffId, String name, String label, String owner) {
        super(tariffId, owner);
        this.name = name;
        this.label = label;
    }

    public TariffBean() {
        super();
        this.name = "";
        this.label = "";
    }

    public int getTariffId() {
        return super.getIdentifier();
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public static final Comparator<TariffBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<TariffBean>() {
        @Override
        public int compare(TariffBean p1, TariffBean p2) {
            if (p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (p1 != null && p2 == null) {
                return -1;
            }
            if (p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)p1.getIdentifier()).compareTo((Integer)p2.getIdentifier()));
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
        final TariffBean other = (TariffBean) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(!Objects.equals(this.label, other.label)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.label);
        return hash + super.hashCode();
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
        Element bean = new Element(VT.Tariff.bean());
        rtnList.add(bean);
         bean.setAttribute("name", name);
        bean.setAttribute("label", label);

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
        Element bean = root.getChild(VT.Tariff.bean());
        name = bean.getAttributeValue("name", "");
        label = bean.getAttributeValue("label", "");
    }

}
