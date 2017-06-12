/*
 * @(#)QualificationBean.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code QualificationBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 06-Oct-2013
 */
public class QualificationBean extends ObjectBean {

    private static final long serialVersionUID = 100L;
    private volatile String name; // the common name of the qualification
    private volatile String type; // CACHE, NVQ, BTEC, City & Guilds, Montessori
    private volatile int level; //at what level the qualification is at

    public QualificationBean(int qualificationId, String name, String type, int level, String owner) {
        super(qualificationId, ObjectEnum.DEFAULT_KEY.value(), owner);
        this.name = name;
        this.type = type;
        this.level = level;
    }

    public QualificationBean() {
        super();
        this.name = "";
        this.type = "";
        this.level = ObjectEnum.INITIALISATION.value();
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

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.type);
        hash = 59 * hash + this.level;
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
        final QualificationBean other = (QualificationBean) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(!Objects.equals(this.type, other.type)) {
            return false;
        }
        if(this.level != other.level) {
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
        Element bean = new Element("QualificationBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("type", type);
        bean.setAttribute("level", Integer.toString(level));

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
        Element bean = root.getChild("QualificationBean");
        // set up the data
        name = bean.getAttributeValue("name", "");
        type = bean.getAttributeValue("type", "");
        level = Integer.parseInt(bean.getAttributeValue("level", "-1"));
    }

}
