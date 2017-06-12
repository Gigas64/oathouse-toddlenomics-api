/*
 * @(#)AccountBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code AccountBean} Class provides accounts level information
 *
 * @author Darryl Oatridge
 * @version 1.00 03-Nov-2010
 */
public class AccountBean extends ObjectBean {

    private static final long serialVersionUID = 20101103100L;
    private volatile String accountRef;
    private volatile String notes;
    private volatile AccountStatus status;
    private volatile int fixedItemDiscountRate;
    private volatile boolean contract;
    private volatile PaymentPeriodEnum paymentPeriod;
    private volatile String regularPaymentInstruction;
    private volatile long paymentPeriodValue;

    private AccountBean(int identifier, String accountRef, String notes, AccountStatus status,
            int fixedItemDiscountRate, boolean contract, PaymentPeriodEnum paymentPeriod,
            String regularPaymentInstruction, long paymentPeriodValue,
            String owner) {
        super(identifier, owner);
        this.accountRef = accountRef;
        this.notes = notes;
        this.status = status;
        this.fixedItemDiscountRate = fixedItemDiscountRate;
        this.contract = contract;
        this.paymentPeriod = paymentPeriod;
        this.regularPaymentInstruction = regularPaymentInstruction;
        this.paymentPeriodValue = paymentPeriodValue;
    }

    public AccountBean(int identifier, String accountRef, String notes, String owner) {
       this(identifier, accountRef, notes, AccountStatus.ACTIVE, 0, false, PaymentPeriodEnum.AS_INVOICE, "", 0, owner);
    }

    public AccountBean() {
        super();
        this.accountRef = "";
        this.notes = "";
        this.status = AccountStatus.UNDEFINED;
        this.fixedItemDiscountRate = ObjectEnum.INITIALISATION.value();
        this.contract = false;
        this.paymentPeriod = PaymentPeriodEnum.UNDEFINED;
        this.regularPaymentInstruction = "";
        this.paymentPeriodValue = ObjectEnum.INITIALISATION.value();
    }

    public int getAccountId() {
        return super.getIdentifier();
    }

    public String getAccountRef() {
        return accountRef;
    }

    public String getNotes() {
        return notes;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getFixedItemDiscountRate() {
        return fixedItemDiscountRate;
    }

    public boolean isContract() {
        return contract;
    }

    public PaymentPeriodEnum getPaymentPeriod() {
        return paymentPeriod;
    }

    public String getRegularPaymentInstruction() {
        return regularPaymentInstruction;
    }

    public long getPaymentPeriodValue() {
        return paymentPeriodValue;
    }

    protected void setAccountRef(String accountRef, String owner) {
        this.accountRef = accountRef != null ? accountRef : "";
        super.setOwner(owner);
    }

    protected void setNotes(String notes, String owner) {
        this.notes = notes != null ? notes : "";
        super.setOwner(owner);
    }

    protected void setStatus(AccountStatus status, String owner) {
        this.status = status;
        super.setOwner(owner);
    }

    protected void setFixedItemDiscountRate(int fixedItemDiscountRate, String owner) {
        this.fixedItemDiscountRate = fixedItemDiscountRate;
        super.setOwner(owner);
    }

    protected void setContract(boolean contract, String owner) {
        this.contract = contract;
        super.setOwner(owner);
    }

    protected void setPaymentPeriod(PaymentPeriodEnum paymentPeriod, String owner) {
        this.paymentPeriod = paymentPeriod;
        super.setOwner(owner);
    }

    protected void setRegularPaymentInstruction(String regularPaymentInstruction, String owner) {
        this.regularPaymentInstruction = regularPaymentInstruction;
        super.setOwner(owner);
    }

    protected void setPaymentPeriodValue(long paymentPeriodValue, String owner) {
        this.paymentPeriodValue = paymentPeriodValue;
        super.setOwner(owner);
    }

    public static final Comparator<AccountBean> REF_ORDER = new Comparator<AccountBean>() {
        @Override
        public int compare(AccountBean a1, AccountBean a2) {
            if (a1 == null && a2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (a1 != null && a2 == null) {
                return -1;
            }
            if (a1 == null && a2 != null) {
                return 1;
            }
            // compare
            int result = (a1.getAccountRef().toLowerCase()).compareTo(a2.getAccountRef().toLowerCase());
            if(result != 0) {
                return result;
            }
            // accountRef not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)a1.getIdentifier()).compareTo((Integer)a2.getIdentifier()));
        }
    };

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.accountRef);
        hash = 71 * hash + Objects.hashCode(this.notes);
        hash = 71 * hash + (this.status != null ? this.status.hashCode() : 0);
        hash = 71 * hash + this.fixedItemDiscountRate;
        hash = 71 * hash + (this.contract ? 1 : 0);
        hash = 71 * hash + (this.paymentPeriod != null ? this.paymentPeriod.hashCode() : 0);
        hash = 71 * hash + Objects.hashCode(this.regularPaymentInstruction);
        hash = 71 * hash + (int) (this.paymentPeriodValue ^ (this.paymentPeriodValue >>> 32));
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
        final AccountBean other = (AccountBean) obj;
        if(!Objects.equals(this.accountRef, other.accountRef)) {
            return false;
        }
        if(!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if(this.status != other.status) {
            return false;
        }
        if(this.fixedItemDiscountRate != other.fixedItemDiscountRate) {
            return false;
        }
        if(this.contract != other.contract) {
            return false;
        }
        if(this.paymentPeriod != other.paymentPeriod) {
            return false;
        }
        if(!Objects.equals(this.regularPaymentInstruction, other.regularPaymentInstruction)) {
            return false;
        }
        if(this.paymentPeriodValue != other.paymentPeriodValue) {
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
        List<Element> rtnList = new LinkedList<Element>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element("AccountBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountRef", accountRef);
        bean.setAttribute("notes", notes);
        bean.setAttribute("status", status.toString());
        bean.setAttribute("fixedItemDiscountRate", Integer.toString(fixedItemDiscountRate));
        bean.setAttribute("contract", Boolean.toString(contract));
        bean.setAttribute("paymentPeriod", paymentPeriod.toString());
        bean.setAttribute("regularPaymentInstruction", regularPaymentInstruction);
        bean.setAttribute("paymentPeriodValue", Long.toString(paymentPeriodValue));
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
        Element bean = root.getChild("AccountBean");
        // set up the data
        accountRef = bean.getAttributeValue("accountRef", "");
        notes = bean.getAttributeValue("notes", "");
        status = AccountStatus.valueOf(bean.getAttributeValue("status", "UNDEFINED"));
        fixedItemDiscountRate = Integer.parseInt(bean.getAttributeValue("fixedItemDiscountRate", "-1"));
        contract = Boolean.parseBoolean(bean.getAttributeValue("contract", "false"));
        paymentPeriod = PaymentPeriodEnum.valueOf(bean.getAttributeValue("paymentPeriod", "UNDEFINED"));
        regularPaymentInstruction = bean.getAttributeValue("regularPaymentInstruction", "");
        paymentPeriodValue = Long.parseLong(bean.getAttributeValue("paymentPeriodValue", "-1"));
    }
}
