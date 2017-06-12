/**
 * @(#)CustomerReceiptBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code CustomerReceiptBean} Class encapsulates a payment made by the nursery
 *
 * @author 	Darryl Oatridge
 * @version 1.02 23-Jan-2011
 */
public class PaymentBean extends TransactionBean {

    private static final long serialVersionUID = 20110123102L;
    private volatile PaymentType paymentType;
    private volatile String reference;  // an external reference: cheque no, paying-in no, bacs ref etc
    private volatile String description; // what the payment is for

    public PaymentBean(int paymentId, int ywd, int accountId, long value, PaymentType paymentType,
            String reference, String description, String notes, String owner) {
        super(paymentId, ywd, accountId, value, notes, owner);
        this.paymentType = paymentType;
        this.reference = reference != null ? reference : "";
        this.description = description != null ? description : "";
    }

    public PaymentBean() {
        super();
        this.paymentType = paymentType.UNDEFINED;
        this.reference = "";
        this.description = "";
    }

    public int getPaymentId() {
        return (super.getIdentifier());
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    protected void setPaymentType(PaymentType paymentType, String owner) {
        this.paymentType = paymentType;
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
        final PaymentBean other = (PaymentBean) obj;
        if(this.paymentType != other.paymentType) {
            return false;
        }
        if((this.reference == null) ? (other.reference != null) : !this.reference.equals(other.reference)) {
            return false;
        }
        if((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.paymentType != null ? this.paymentType.hashCode() : 0);
        hash = 59 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        hash = 59 * hash + (this.description != null ? this.description.hashCode() : 0);
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
        Element bean = new Element("PaymentBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("paymentType", paymentType.toString());
        bean.setAttribute("reference", reference);
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
        Element bean = root.getChild("PaymentBean");
        // set up the data
        paymentType = PaymentType.valueOf(bean.getAttributeValue("paymentType", "UNDEFINED"));
        reference = bean.getAttributeValue("reference", "");
        description = bean.getAttributeValue("description", "");
    }
}
