/*
 * @(#)MedicalBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code MedicalBean} Class is used to store information about
 * medical and dietary needs
 *
 * @author Darryl Oatridge
 * @version 1.01 18-July-2010
 */
public class MedicalBean extends ObjectBean {
    private static final long serialVersionUID = 20100718101L;

    private volatile int childId;
    private volatile String name;
    private volatile MedicalType type;
    private volatile String description;
    private volatile String instruction;
    private volatile int trigger;
    private volatile int reviewYwd;

    public MedicalBean(int medId, int childId, String name, MedicalType type, String description, String instruction,
            int trigger, int reviewYwd, String owner) {
        super(medId, owner);
        this.childId = childId;
        this.name = name != null ? name : "";
        this.type = type;
        this.description = description != null ? description : "";
        this.instruction = instruction != null ? instruction : "";
        this.trigger = trigger;
        this.reviewYwd = reviewYwd;
    }

    public MedicalBean() {
        super();
        this.childId = ObjectEnum.INITIALISATION.value();
        this.name = "";
        this.type = MedicalType.UNDEFINED;
        this.description = "";
        this.instruction = "";
        this.trigger = ObjectEnum.INITIALISATION.value();
        this.reviewYwd = ObjectEnum.INITIALISATION.value();
    }

    /**
     * Returns the identifier for this bean
     */
    public int getMedicalId() {
        return super.getIdentifier();
    }

    /**
     * same as getMedicalId
     */
    public int getMedId() {
        return getIdentifier();
    }

    public int getChildId() {
        return childId;
    }

    public String getName() {
        return name;
    }

    public MedicalType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getTrigger() {
        return trigger;
    }

    public int getReviewYwd() {
        return reviewYwd;
    }

    public static final Comparator<MedicalBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<MedicalBean>() {
        @Override
        public int compare(MedicalBean m1, MedicalBean m2) {
            if (m1 == null && m2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (m1 != null && m2 == null) {
                return -1;
            }
            if (m1 == null && m2 != null) {
                return 1;
            }
            // compare
            int result = m1.getName().toLowerCase().compareTo(m2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)m1.getIdentifier()).compareTo((Integer)m2.getIdentifier()));
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
        final MedicalBean other = (MedicalBean) obj;
        if(this.childId != other.childId) {
            return false;
        }
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if(this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        if((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if((this.instruction == null) ? (other.instruction != null) : !this.instruction.equals(other.instruction)) {
            return false;
        }
        if(this.trigger != other.trigger) {
            return false;
        }
        if(this.reviewYwd != other.reviewYwd) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.childId;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 29 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 29 * hash + (this.instruction != null ? this.instruction.hashCode() : 0);
        hash = 29 * hash + this.trigger;
        hash = 29 * hash + this.reviewYwd;
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
        Element bean = new Element("MedicalBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("childId", Integer.toString(childId));
        bean.setAttribute("name", name);
        bean.setAttribute("type", type.toString());
        bean.setAttribute("description", description);
        bean.setAttribute("instruction", instruction);
        bean.setAttribute("trigger", Integer.toString(trigger));
        bean.setAttribute("reviewYwd", Integer.toString(reviewYwd));
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
        Element bean = root.getChild("MedicalBean");
        // set up the data
        childId = Integer.parseInt(bean.getAttributeValue("childId", "0"));
        name = bean.getAttributeValue("name");
        type = MedicalType.valueOf(bean.getAttributeValue("type", "UNDEFINED"));
        description = bean.getAttributeValue("description");
        instruction = bean.getAttributeValue("instruction");
        trigger = Integer.parseInt(bean.getAttributeValue("trigger", "0"));
        reviewYwd = Integer.parseInt(bean.getAttributeValue("reviewYwd", "0"));
    }
}
