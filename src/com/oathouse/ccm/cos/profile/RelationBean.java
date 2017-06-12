/*
 * @(#)RelationBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code RelationBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.01 20-Oct-2010
 */
public class RelationBean extends ObjectBean {

    private static final long serialVersionUID = 20101020101L;
    private volatile RelationType type;

    public RelationBean(int identifier, RelationType type, String owner) {
        super(identifier, owner);
        this.type = type;
    }

    public RelationBean() {
        super();
        this.type = RelationType.UNDEFINED;
    }

    /**
     * Returns the identifier for this bean
     */
    public int getRelationId() {
        return super.getIdentifier();
    }

    public int getProfileId() {
        return (super.getIdentifier());
    }

    public RelationType getType() {
        return type;
    }

    @Override
    public int compareTo(ObjectBean other) {
        RelationBean relation = (RelationBean) other;
        return(((Long)this.getModified()).compareTo((Long)relation.getModified()));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final RelationBean other = (RelationBean) obj;
        if(this.type != other.type) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        return super.hashCode() + hash;
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
        Element bean = new Element(VT.Relation.bean());
        rtnList.add(bean);
        // set the data
        bean.setAttribute("type", type.toString());

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
        Element bean = root.getChild(VT.Relation.bean());
        // set up the data
        type = RelationType.valueOf(bean.getAttributeValue("type", "UNDEFINED"));
    }

}
