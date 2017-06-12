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
 * The {@code CustomerReceiptBean} Class encapsulates a customer payment
 *
 * @author 	Darryl Oatridge
 * @version 2.00 30-Jan-2010
 */
public class CustomerReceiptBean extends TransactionBean {

    private static final long serialVersionUID = 2010130200L;
    private volatile PaymentType paymentType;
    private volatile String reference;  // an external reference: cheque no, paying-in no, bacs ref etc

    public CustomerReceiptBean(int customerReceiptId, int ywd, int accountId, long value, PaymentType paymentType,
            String reference, String notes, String owner) {
        super(customerReceiptId, ywd, accountId, value, notes, owner);
        this.paymentType = paymentType;
        this.reference = reference;
    }

    public CustomerReceiptBean() {
        super();
        this.paymentType = PaymentType.UNDEFINED;
        this.reference = "";
    }

    public int getCustomerReceiptId() {
        return super.getIdentifier();
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public String getReference() {
        return reference;
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
        final CustomerReceiptBean other = (CustomerReceiptBean) obj;
        if(this.paymentType != other.paymentType) {
            return false;
        }
        if((this.reference == null) ? (other.reference != null) : !this.reference.equals(other.reference)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.paymentType != null ? this.paymentType.hashCode() : 0);
        hash = 73 * hash + (this.reference != null ? this.reference.hashCode() : 0);
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
        Element bean = new Element("CustomerReceiptBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("paymentType", paymentType.toString());
        bean.setAttribute("reference", reference);
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
        Element bean = root.getChild("CustomerReceiptBean");
        // set up the data
        paymentType = PaymentType.valueOf(bean.getAttributeValue("paymentType", "UNDEFINED"));
        reference = bean.getAttributeValue("reference", "");
    }

}
