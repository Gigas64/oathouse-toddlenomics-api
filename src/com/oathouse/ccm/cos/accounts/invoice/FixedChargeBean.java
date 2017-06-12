/**
 * @(#)FixedChargeBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code FixedChargeBean} Class describes invoice items that are set once (unlike bookings):
 * eg adjustments to the account and sales of items rather than childcare (eg photographs)
 *
 * @author 	Darryl Oatridge
 * @version 2.01 23-Feb-2011
 */
public class FixedChargeBean extends ChargeBean {

    private static final long serialVersionUID = 20110223201L;
    private volatile String description;

    public FixedChargeBean(int fixedChargeId, int ywd, int accountId, int profileId, int invoiceId, long value, int taxRate,
            int discountRate, String description, String notes, String owner) {
        super(fixedChargeId, ywd, accountId, profileId, invoiceId, value, taxRate, discountRate, notes, owner);
        this.description = description;
    }

    /**
     * Main constructor used to create a FixedChargeBean Object. The invoice is not set. The taxRate and discountRate
     * are set to zero
     *
     * @param fixedChargeId
     * @param ywd
     * @param accountId
     * @param profileId
     * @param value
     * @param description
     * @param notes
     * @param owner
     */
    public FixedChargeBean(int fixedChargeId, int ywd, int accountId, int profileId, long value, String description, String notes, String owner) {
        // no invoice value;
        //taxRate and DIscountRate set to zero (0)
        this(fixedChargeId, ywd, accountId, profileId, ObjectEnum.INITIALISATION.value(), value, 0, 0, description, notes, owner);
    }

    /**
     * Constructor used for the storage of FixedChargeBean templates
     *
     * @param fixedChargeId
     * @param value
     * @param description
     * @param notes
     * @param owner
     */
    public FixedChargeBean(int fixedChargeId, long value, String description, String notes, String owner) {
        this(fixedChargeId,
                ObjectEnum.INITIALISATION.value(),
                ObjectEnum.INITIALISATION.value(),
                ObjectEnum.INITIALISATION.value(),
                ObjectEnum.INITIALISATION.value(),
                value, 0, 0, description, notes, owner);
    }

    public FixedChargeBean() {
        super();
        this.description = "";
    }

    public int getFixedChargeId() {
        return super.getIdentifier();
    }
    public String getDescription() {
        return description;
    }

    public static final Comparator<FixedChargeBean> CASE_INSENSITIVE_DESCRIPTION_ORDER = new Comparator<FixedChargeBean>() {
        @Override
        public int compare(FixedChargeBean f1, FixedChargeBean f2) {
            if (f1 == null && f2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (f1 != null && f2 == null) {
                return -1;
            }
            if (f1 == null && f2 != null) {
                return 1;
            }
            // compare
            int result = (f1.getDescription().toLowerCase()).compareTo(f2.getDescription().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)f1.getIdentifier()).compareTo((Integer)f2.getIdentifier()));
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
        final FixedChargeBean other = (FixedChargeBean) obj;
        if((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.description != null ? this.description.hashCode() : 0);
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
        Element bean = new Element("FixedChargeBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("description", description);

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
        // extract the super data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("FixedChargeBean");
        // set up the data
        description = bean.getAttributeValue("description", "");
    }
}
