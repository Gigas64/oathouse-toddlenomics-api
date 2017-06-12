/*
 * @(#)TransactionBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code TransactionBean} Class is the base class for transaction information.
 *
 * @author Darryl Oatridge
 * @version 1.01 25-Jan-2011
 */
public abstract class TransactionBean extends ObjectBean {

    private static final long serialVersionUID = 20110125101L;
    private volatile int ywd;           // the tax point date of the transaction
    private volatile int accountId;
    private volatile long value;
    private volatile String notes;

    public TransactionBean(int chargeId, int ywd, int accountId, long value, String notes, String owner) {
        super(chargeId, owner);
        this.ywd = ywd;
        this.accountId = accountId;
        this.value = value;
        this.notes = notes;
    }

    public TransactionBean() {
        super();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.value = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    public int getYwd() {
        return ywd;
    }

    public int getAccountId() {
        return accountId;
    }

    public long getValue() {
        return value;
    }

    public String getNotes() {
        return notes;
    }

    protected void setValue(long value, String owner) {
        this.value = value;
        super.setOwner(owner);
    }

    protected void setTaxRate(int taxRate, String owner) {
        super.setOwner(owner);
    }

    /**
     *Sort the bean in created order.
     */
    @Override
    public int compareTo(ObjectBean other) {
        return (this.getCreated() < other.getCreated() ? -1 : (this.getCreated() == other.getCreated() ? 0 : 1));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final TransactionBean other = (TransactionBean) obj;
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.value != other.value) {
            return false;
        }
        if((this.notes == null) ? (other.notes != null) : !this.notes.equals(other.notes)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.ywd;
        hash = 29 * hash + this.accountId;
        hash = 29 * hash + (int) (this.value ^ (this.value >>> 32));
        hash = 29 * hash + (this.notes != null ? this.notes.hashCode() : 0);
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
        Element bean = new Element("TransactionBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("value", Long.toString(value));
        bean.setAttribute("notes", notes);

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
        Element bean = root.getChild("TransactionBean");
        // set up the data
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        value = Long.parseLong(bean.getAttributeValue("value", "-1"));
        notes = bean.getAttributeValue("notes", "");

    }
}
