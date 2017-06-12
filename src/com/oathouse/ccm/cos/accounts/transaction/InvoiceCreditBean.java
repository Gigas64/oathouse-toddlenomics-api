/*
 * @(#)InvoiceCreditBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code InvoiceCreditBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Feb-2011
 */
public class InvoiceCreditBean extends TransactionBean {

    private static final long serialVersionUID = 20110220100L;
    private volatile int invoiceId;

    public InvoiceCreditBean(int invoiceCreditId, int ywd, int accountId, int invoiceId, long value, String notes, String owner) {
        super(invoiceCreditId, ywd, accountId, value, notes, owner);
        this.invoiceId = invoiceId;
    }

    public InvoiceCreditBean() {
        super();
        this.invoiceId = ObjectEnum.INITIALISATION.value();
    }

    public int getInvoiceCreditId() {
        return super.getIdentifier();
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final InvoiceCreditBean other = (InvoiceCreditBean) obj;
        if(this.invoiceId != other.invoiceId) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.invoiceId;
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
        Element bean = new Element("InvoiceCreditBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("invoiceId", Integer.toString(invoiceId));

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
        Element bean = root.getChild("InvoiceCreditBean");
        // set up the data
        invoiceId = Integer.parseInt(bean.getAttributeValue("invoiceId", "-1"));
    }

}
