/**
 * @(#)CustomerReceiptBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code CustomerReceiptBean} Class holds information regarding a customer credit and debit.
 * Each Bean can only hold one receiptId and one invoiceId so where a receipt or invoice is split
 * across multiple credits or debits, multipleCustomerReceiptBeans will exist.
 *
 *
 * @author 	Darryl Oatridge
 * @version 1.03 20-Feb-2010
 */
public class CustomerCreditBean extends ObjectBean {

    private static final long serialVersionUID = 20101210100L;
    private volatile int accountId;
    private volatile int creditYwd;
    private volatile int creditId; // customerReceiptId or invoiceId
    private volatile long credit;   // credit value
    private volatile CustomerCreditType creditType; // the type of credit to identify the type of id
    private volatile int debitYwd;
    private volatile int debitId; // invoiceId or paymentId
    private volatile long debit;    // debit value
    private volatile CustomerCreditType debitType; // the type of debit to identify the type of id

    public CustomerCreditBean(int customerCreditId, int accountId, int creditYwd, int creditId, long credit,
            CustomerCreditType creditType, int debitYwd, int debitId, long debit, CustomerCreditType debitType, String owner) {
        super(customerCreditId, owner);
        this.accountId = accountId;
        this.creditYwd = creditYwd;
        this.creditId = creditId;
        this.credit = credit;
        this.creditType = creditType;
        this.debitYwd = debitYwd;
        this.debitId = debitId;
        this.debit = debit;
        this.debitType = debitType;
    }

    public CustomerCreditBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.creditYwd = ObjectEnum.INITIALISATION.value();
        this.creditId = ObjectEnum.INITIALISATION.value();
        this.credit = ObjectEnum.INITIALISATION.value();
        this.creditType = CustomerCreditType.UNDEFINED;
        this.debitYwd = ObjectEnum.INITIALISATION.value();
        this.debitId = ObjectEnum.INITIALISATION.value();
        this.debit = ObjectEnum.INITIALISATION.value();
        this.debitType = CustomerCreditType.UNDEFINED;
    }

    public int getCustomerCreditId() {
        return(super.getIdentifier());
    }

    public int getAccountId() {
        return accountId;
    }

    public int getCreditYwd() {
        return creditYwd;
    }

    public long getCredit() {
        return credit;
    }

    public int getCreditId() {
        return creditId;
    }

    public CustomerCreditType getCreditType() {
        return creditType;
    }

    public int getDebitYwd() {
        return debitYwd;
    }

    public long getDebit() {
        return debit;
    }

    public int getDebitId() {
        return debitId;
    }

    public CustomerCreditType getDebitType() {
        return debitType;
    }

    protected void setCreditYwd(int ywd, String owner) {
        this.creditYwd = ywd;
        super.setOwner(owner);
    }

    protected void setCreditId(int receiptId, String owner) {
        this.creditId = receiptId;
        super.setOwner(owner);
    }

    protected void setCredit(long credit, String owner) {
        this.credit = credit;
        super.setOwner(owner);
    }

    protected void setCreditType(CustomerCreditType creditType, String owner) {
        this.creditType = creditType;
        super.setOwner(owner);
    }

    protected void setDebitYwd(int ywd, String owner) {
        this.debitYwd = ywd;
        super.setOwner(owner);
    }

    protected void setDebitId(int invoiceId, String owner) {
        this.debitId = invoiceId;
        super.setOwner(owner);
    }

    protected void setDebit(long debit, String owner) {
        this.debit = debit;
        super.setOwner(owner);
    }

    protected void setDebitType(CustomerCreditType debitType, String owner) {
        this.debitType = debitType;
        super.setOwner(owner);
    }

    public int compareTo(CustomerCreditBean other) {
        if(other == null) {
            // show null objects last
            return -1;
        }
        int ywd = debitYwd < 0 ? creditYwd : debitYwd;
        int otherYwd = other.getDebitYwd() < 0 ? other.getCreditYwd() : other.getDebitYwd();
        int ywdCmp = ywd < otherYwd ? -1 : (ywd == otherYwd ? 0 : 1);
        if(ywdCmp != 0) {
            return(ywdCmp);
        }
        return(super.compareTo(other));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final CustomerCreditBean other = (CustomerCreditBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(this.creditYwd != other.creditYwd) {
            return false;
        }
        if(this.creditId != other.creditId) {
            return false;
        }
        if(this.credit != other.credit) {
            return false;
        }
        if(this.creditType != other.creditType) {
            return false;
        }
        if(this.debitYwd != other.debitYwd) {
            return false;
        }
        if(this.debitId != other.debitId) {
            return false;
        }
        if(this.debit != other.debit) {
            return false;
        }
        if(this.debitType != other.debitType) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.accountId;
        hash = 23 * hash + this.creditYwd;
        hash = 23 * hash + this.creditId;
        hash = 23 * hash + (int) (this.credit ^ (this.credit >>> 32));
        hash = 23 * hash + (this.creditType != null ? this.creditType.hashCode() : 0);
        hash = 23 * hash + this.debitYwd;
        hash = 23 * hash + this.debitId;
        hash = 23 * hash + (int) (this.debit ^ (this.debit >>> 32));
        hash = 23 * hash + (this.debitType != null ? this.debitType.hashCode() : 0);
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
        Element bean = new Element("CustomerCreditBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("creditYwd", Integer.toString(creditYwd));
        bean.setAttribute("creditId", Integer.toString(creditId));
        bean.setAttribute("credit", Long.toString(credit));
        bean.setAttribute("creditType", creditType.toString());
        bean.setAttribute("debitYwd", Integer.toString(debitYwd));
        bean.setAttribute("debitId", Integer.toString(debitId));
        bean.setAttribute("debit", Long.toString(debit));
        bean.setAttribute("debitType", debitType.toString());

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
        Element bean = root.getChild("CustomerCreditBean");
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", "-1"));
        creditYwd = Integer.parseInt(bean.getAttributeValue("creditYwd", "-1"));
        creditId = Integer.parseInt(bean.getAttributeValue("creditId", "-1"));
        credit = Long.parseLong(bean.getAttributeValue("credit", "-1"));
        creditType = CustomerCreditType.valueOf(bean.getAttributeValue("creditType", "UNDEFINED"));
        debitYwd = Integer.parseInt(bean.getAttributeValue("debitYwd", "-1"));
        debitId = Integer.parseInt(bean.getAttributeValue("debitId", "-1"));
        debit = Long.parseLong(bean.getAttributeValue("debit", "-1"));
        debitType = CustomerCreditType.valueOf(bean.getAttributeValue("debitType", "UNDEFINED"));
    }

}
