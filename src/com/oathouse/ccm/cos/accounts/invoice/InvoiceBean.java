/*
 * @(#)InvoiceBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code InvoiceBean} Class
 *
 * @author Darryl Oatridge
 * @version 2.00 10-Dec-2010
 */
public class InvoiceBean extends ObjectBean {

    private static final long serialVersionUID = 20101210200L;
    private volatile int accountId;
    private volatile int ywd;
    private volatile InvoiceType invoiceType;
    private volatile int lastYwd;
    private volatile int dueYwd;
    private volatile String notes;

    public InvoiceBean(int invoiceId, int accountId, int ywd, InvoiceType invoiceType, int lastYwd,
            int dueYwd, String notes, String owner) {
        super(invoiceId, owner);
        this.accountId = accountId;
        this.ywd = ywd;
        this.invoiceType = invoiceType;
        this.lastYwd = lastYwd;
        this.dueYwd = dueYwd;
        this.notes = notes != null ? notes : "";
    }

    public InvoiceBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.ywd = ObjectEnum.INITIALISATION.value();
        this.invoiceType = InvoiceType.UNDEFINED;
        this.lastYwd = ObjectEnum.INITIALISATION.value();
        this.dueYwd = ObjectEnum.INITIALISATION.value();
        this.notes = "";
    }

    public int getInvoiceId() {
        return super.getIdentifier();
    }

    public int getAccountId() {
        return accountId;
    }

    public int getYwd() {
        return ywd;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public boolean isType(InvoiceType type) {
        return invoiceType == type;
    }

    /**
     * This is the latest ywd that this invoice took into account when created.
     * Eg, if the invoice was "for December", this would be equivalent to 31 Dec.
     * @return
     */
    public int getLastYwd() {
        return lastYwd;
    }

    /**
     * This is the date on which the invoice is due for payment: this allows aged debt to be
     * calculated.  For example, an invoice "for December" might be due on 1 December.
     * @return
     */
    public int getDueYwd() {
        return dueYwd;
    }

    public String getNotes() {
        return notes;
    }

    protected void setInvoiceType(InvoiceType invoiceType, String owner) {
        this.invoiceType = invoiceType;
        super.setOwner(owner);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final InvoiceBean other = (InvoiceBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.ywd != other.ywd) {
            return false;
        }
        if(this.invoiceType != other.invoiceType) {
            return false;
        }
        if(this.lastYwd != other.lastYwd) {
            return false;
        }
        if(this.dueYwd != other.dueYwd) {
            return false;
        }
        if((this.notes == null) ? (other.notes != null) : !this.notes.equals(other.notes)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.accountId;
        hash = 97 * hash + this.ywd;
        hash = 97 * this.invoiceType.hashCode();
        hash = 97 * hash + this.lastYwd;
        hash = 97 * hash + this.dueYwd;
        hash = 97 * hash + (this.notes != null ? this.notes.hashCode() : 0);
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
        Element bean = new Element("InvoiceBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("ywd", Integer.toString(ywd));
        bean.setAttribute("invoiceType", invoiceType.toString());
        bean.setAttribute("lastYwd", Integer.toString(lastYwd));
        bean.setAttribute("dueYwd", Integer.toString(dueYwd));
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
        Element bean = root.getChild("InvoiceBean");
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        ywd = Integer.parseInt(bean.getAttributeValue("ywd", "-1"));
        invoiceType = InvoiceType.valueOf(bean.getAttributeValue("invoiceType", "NO_VALUE"));
        lastYwd = Integer.parseInt(bean.getAttributeValue("lastYwd", "-1"));
        dueYwd = Integer.parseInt(bean.getAttributeValue("dueYwd", "-1"));
        notes = bean.getAttributeValue("notes", "");
    }
}
