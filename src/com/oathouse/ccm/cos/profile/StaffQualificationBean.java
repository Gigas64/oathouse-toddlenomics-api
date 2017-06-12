/*
 * @(#)StaffQualificationBean.java
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
 * The {@code StaffQualificationBean} Class the relation between
 * qualification and staff.
 *
 * @author Darryl Oatridge
 * @version 1.00 06-Oct-2013
 */
public class StaffQualificationBean extends ObjectBean {

    private static final long serialVersionUID = 100L;
    private volatile int achievedYwd; // the ywd it was achieved
    private volatile String notes; // additional notes

    /**
     * Main constructor for the class. The qualification ID is the beanId and the
     * staffId is the key.
     *
     * @param qualificationId
     * @param staffId
     * @param achievedYwd
     * @param notes
     * @param owner
     */
    public StaffQualificationBean(int qualificationId, int staffId, int achievedYwd, String notes, String owner) {
        super(qualificationId, staffId, owner);
        this.achievedYwd = achievedYwd;
        this.notes = notes;
    }

    public StaffQualificationBean() {
        super();
        this.achievedYwd = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    // the id is the Qualification
    public int getId() {
        return super.getIdentifier();
    }

    // the key is the StaffId
    public int getKey() {
        return super.getGroupKey();
    }

    public int getAchievedYwd() {
        return achievedYwd;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.achievedYwd;
        hash = 97 * hash + Objects.hashCode(this.notes);
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
        final StaffQualificationBean other = (StaffQualificationBean) obj;
        if(this.achievedYwd != other.achievedYwd) {
            return false;
        }
        if(!Objects.equals(this.notes, other.notes)) {
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
        Element bean = new Element("StaffQualificationBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("achievedYwd", Integer.toString(achievedYwd));
        bean.setAttribute("notes", notes);

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
        Element bean = root.getChild("StaffQualificationBean");
        // set up the data
        achievedYwd = Integer.parseInt(bean.getAttributeValue("achievedYwd", "-1"));
        notes = bean.getAttributeValue("notes", "");

    }

}
