/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)TariffRelationBean.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code TariffRelationBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 09-May-2012
 */
public class TariffRelationBean extends ObjectBean {

    private static final long serialVersionUID = 20120509100L;

    /**
     * Constructor
     *
     * @param tariffRealtionId
     * @param owner
     */
    public TariffRelationBean(int tariffRealtionId, String owner) {
        super(tariffRealtionId, owner);
    }

    /**
     * Constructor for XML creation (Do Not Use)
     */
    public TariffRelationBean() {
        super();
    }

    public int getTariffRelationId() {
        return super.getIdentifier();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final TariffRelationBean other = (TariffRelationBean) obj;
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        Element bean = new Element(VT.PRICE_TARIFF_RELATION.bean());
        rtnList.add(bean);

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
        Element bean = root.getChild(VT.PRICE_TARIFF_RELATION.bean());
    }

}
