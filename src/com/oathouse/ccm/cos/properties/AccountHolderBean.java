/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)AccountHolderBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.properties;

import com.oathouse.oss.storage.objectstore.*;
import java.util.*;
import org.jdom2.*;

/**
 * The {@code AccountHolderBean} Class hold all the account holder information
 * for the purposes of billing and contacting
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Aug-2010
 */
public class AccountHolderBean extends ObjectBean {

    private static final long serialVersionUID = 20100810100L;
    private volatile String contact;
    private volatile String businessName;
    private volatile String line1;
    private volatile String line2;
    private volatile String line3;
    private volatile String city;
    private volatile String province;
    private volatile String postcode;
    private volatile String country;
    private volatile String phone;
    private volatile String accountId;
    private volatile String priceFile;
    private volatile String superUsername;
    private volatile String superPassword;
    private volatile String returnEmailAddress;
    private volatile String returnEmailDisplayName;
    private volatile String billingEmail;
    private volatile boolean validated;
    private volatile boolean suspended;
    private volatile String timeZone;
    private volatile String language;
    private volatile String currency;
    private volatile int registeredCapacity;
    private volatile int chargeRate; // holds percent in hundreths (1% = 100)

    private AccountHolderBean(int accountHolderId, String contact, String businessName, String line1,
            String line2, String line3, String city, String province, String postcode, String country,
            String phone, String accountId, String priceFile,
            String superUsername, String superPassword, String returnEmailAddress,
            String returnEmailDisplayName, String billingEmail,
            boolean validated, boolean suspended,
            String timeZone, String language, String currency,
            int chargeRate,  int registeredCapacity, String owner) {
        super(accountHolderId, owner);
        this.contact = contact != null ? contact : "";
        this.businessName = businessName != null ? businessName : "";
        this.line1 = line1 != null ? line1 : "";
        this.line2 = line2 != null ? line2 : "";
        this.line3 = line3 != null ? line3 : "";
        this.city = city != null ? city : "";
        this.province = province != null ? province : "";
        this.postcode = postcode != null ? postcode : "";
        this.country = country != null ? country : "";
        this.phone = phone != null ? phone : "";
        this.accountId = accountId != null ? accountId : "";
        this.priceFile = priceFile != null ? priceFile : "";
        this.superUsername = superUsername != null ? superUsername : "superuser";
        this.superPassword = superPassword != null ? superPassword : "superPass";
        this.returnEmailAddress = returnEmailAddress != null ? returnEmailAddress : "";
        this.returnEmailDisplayName = returnEmailDisplayName != null ? returnEmailDisplayName : "";
        this.billingEmail = billingEmail != null ? billingEmail : "";
        this.validated = validated;
        this.suspended = suspended;
        this.timeZone = timeZone != null ? timeZone : "";
        this.language = language != null ? language : "";
        this.currency = currency != null ? currency : "";
        this.chargeRate = chargeRate;
        this.registeredCapacity = registeredCapacity;
    }

    public AccountHolderBean(String contact, String businessName, String line1,
            String line2, String line3, String city, String province, String postcode, String country,
            String phone, String accountId, String priceFile,
            String superUsername, String superPassword, String returnEmailAddress,
            String returnEmailDisplayName, String billingEmail,
            boolean validated, boolean suspended,
            String timeZone, String language, String currency,
            int chargeRate, int registeredCapacity, String owner) {

        this(ObjectEnum.DEFAULT_ID.value(), contact, businessName, line1, line2, line3, city, province, postcode,
                country, phone, accountId, priceFile, superUsername, superPassword, returnEmailAddress,
                returnEmailDisplayName, billingEmail, validated, suspended, timeZone, language, currency,
                chargeRate, registeredCapacity, owner);
    }

    public AccountHolderBean() {
        super();
        this.contact = "";
        this.businessName = "";
        this.line1 = "";
        this.line2 = "";
        this.line3 = "";
        this.city = "";
        this.province = "";
        this.postcode = "";
        this.country = "";
        this.phone = "";
        this.accountId = "";
        this.priceFile = "";
        this.superUsername = "";
        this.superPassword = "";
        this.returnEmailAddress = "";
        this.returnEmailDisplayName = "";
        this.validated = false;
        this.billingEmail = "";
        this.suspended = false;
        this.timeZone = "";
        this.language = "";
        this.currency = "";
        this.chargeRate = ObjectEnum.INITIALISATION.value();
        this.registeredCapacity = ObjectEnum.INITIALISATION.value();
    }

    public int getAccountHolderId() {
        return super.getIdentifier();
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPriceFile() {
        return priceFile;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getCity() {
        return city;
    }

    public String getContact() {
        return contact;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    public String getPhone() {
        return phone;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getProvince() {
        return province;
    }

    public String getReturnEmailAddress() {
        return returnEmailAddress;
    }

    public String getReturnEmailDisplayName() {
        return returnEmailDisplayName;
    }

    public String getSuperPassword() {
        return superPassword;
    }

    public String getSuperUsername() {
        return superUsername;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isValidated() {
        return validated;
    }

    public String getLanguage() {
        return language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public int getChargeRate() {
        return chargeRate;
    }

    public int getRegisteredCapacity() {
        return registeredCapacity;
    }

    protected void setAccountHolder(String contact, String businessName, String line1, String line2, String line3,
            String city, String province, String postcode, String country,
            String phone, String accountId, String owner) {
        this.contact = contact != null ? contact : "";
        this.businessName = businessName != null ? businessName : "";
        this.line1 = line1 != null ? line1 : "";
        this.line2 = line2 != null ? line2 : "";
        this.line3 = line3 != null ? line3 : "";
        this.city = city != null ? city : "";
        this.province = province != null ? province : "";
        this.postcode = postcode != null ? postcode : "";
        this.country = country != null ? country : "";
        this.phone = phone != null ? phone : "";
        this.accountId = accountId != null ? accountId : "";
        super.setOwner(owner);
    }

    protected void setSuperUser(String username, String password, String owner) {
        this.superUsername = username != null ? username : "superuser";
        this.superPassword = password != null ? password : "superPass";
        super.setOwner(owner);
    }

    protected void setReturnEmail(String returnEmailAddress, String returnEmailDisplayName, String owner) {
        this.returnEmailAddress = returnEmailAddress != null ? returnEmailAddress : "";
        this.returnEmailDisplayName = returnEmailDisplayName != null ? returnEmailDisplayName : "";
        super.setOwner(owner);
    }

    protected void setBilling(String priceUrl, boolean validated, String billingEmail, String owner) {
        this.priceFile = priceUrl;
        this.validated = validated;
        this.billingEmail = billingEmail != null ? billingEmail : "";
        super.setOwner(owner);
    }

    protected void setSuspended(boolean isSuspended, String owner) {
        this.suspended = isSuspended;
        super.setOwner(owner);
    }

    protected void setLocale(String timeZone, String language, String currency, String owner) {
        this.timeZone = timeZone != null ? timeZone : "";
        this.language = language != null ? language : "";
        this.currency = currency != null ? currency : "";
        super.setOwner(owner);
    }

    protected void setChargeRate(int chargeRate, String owner) {
        this.chargeRate = chargeRate;
        super.setOwner(owner);
    }

    protected void setRegisteredCapacity(int registeredCapacity, String owner) {
        this.registeredCapacity = registeredCapacity;
        super.setOwner(owner);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.contact);
        hash = 23 * hash + Objects.hashCode(this.businessName);
        hash = 23 * hash + Objects.hashCode(this.line1);
        hash = 23 * hash + Objects.hashCode(this.line2);
        hash = 23 * hash + Objects.hashCode(this.line3);
        hash = 23 * hash + Objects.hashCode(this.city);
        hash = 23 * hash + Objects.hashCode(this.province);
        hash = 23 * hash + Objects.hashCode(this.postcode);
        hash = 23 * hash + Objects.hashCode(this.country);
        hash = 23 * hash + Objects.hashCode(this.phone);
        hash = 23 * hash + Objects.hashCode(this.accountId);
        hash = 23 * hash + Objects.hashCode(this.priceFile);
        hash = 23 * hash + Objects.hashCode(this.superUsername);
        hash = 23 * hash + Objects.hashCode(this.superPassword);
        hash = 23 * hash + Objects.hashCode(this.returnEmailAddress);
        hash = 23 * hash + Objects.hashCode(this.returnEmailDisplayName);
        hash = 23 * hash + Objects.hashCode(this.billingEmail);
        hash = 23 * hash + (this.validated ? 1 : 0);
        hash = 23 * hash + (this.suspended ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.timeZone);
        hash = 23 * hash + Objects.hashCode(this.language);
        hash = 23 * hash + Objects.hashCode(this.currency);
        hash = 23 * hash + this.registeredCapacity;
        hash = 23 * hash + this.chargeRate;
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
        final AccountHolderBean other = (AccountHolderBean) obj;
        if(!Objects.equals(this.contact, other.contact)) {
            return false;
        }
        if(!Objects.equals(this.businessName, other.businessName)) {
            return false;
        }
        if(!Objects.equals(this.line1, other.line1)) {
            return false;
        }
        if(!Objects.equals(this.line2, other.line2)) {
            return false;
        }
        if(!Objects.equals(this.line3, other.line3)) {
            return false;
        }
        if(!Objects.equals(this.city, other.city)) {
            return false;
        }
        if(!Objects.equals(this.province, other.province)) {
            return false;
        }
        if(!Objects.equals(this.postcode, other.postcode)) {
            return false;
        }
        if(!Objects.equals(this.country, other.country)) {
            return false;
        }
        if(!Objects.equals(this.phone, other.phone)) {
            return false;
        }
        if(!Objects.equals(this.accountId, other.accountId)) {
            return false;
        }
        if(!Objects.equals(this.priceFile, other.priceFile)) {
            return false;
        }
        if(!Objects.equals(this.superUsername, other.superUsername)) {
            return false;
        }
        if(!Objects.equals(this.superPassword, other.superPassword)) {
            return false;
        }
        if(!Objects.equals(this.returnEmailAddress, other.returnEmailAddress)) {
            return false;
        }
        if(!Objects.equals(this.returnEmailDisplayName, other.returnEmailDisplayName)) {
            return false;
        }
        if(!Objects.equals(this.billingEmail, other.billingEmail)) {
            return false;
        }
        if(this.validated != other.validated) {
            return false;
        }
        if(this.suspended != other.suspended) {
            return false;
        }
        if(!Objects.equals(this.timeZone, other.timeZone)) {
            return false;
        }
        if(!Objects.equals(this.language, other.language)) {
            return false;
        }
        if(!Objects.equals(this.currency, other.currency)) {
            return false;
        }
        if(this.registeredCapacity != other.registeredCapacity) {
            return false;
        }
        if(this.chargeRate != other.chargeRate) {
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
        Element bean = new Element("AccountHolderBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("contact", contact);
        bean.setAttribute("businessName", businessName);
        bean.setAttribute("line1", line1);
        bean.setAttribute("line2", line2);
        bean.setAttribute("line3", line3);
        bean.setAttribute("city", city);
        bean.setAttribute("province", province);
        bean.setAttribute("postcode", postcode);
        bean.setAttribute("country", country);
        bean.setAttribute("phone", phone);
        bean.setAttribute("superUsername", superUsername);
        bean.setAttribute("superPassword", superPassword);
        bean.setAttribute("returnEmailAddress", returnEmailAddress);
        bean.setAttribute("returnEmailName", returnEmailDisplayName);
        bean.setAttribute("currency", currency);
        bean.setAttribute("accountId", accountId);
        bean.setAttribute("priceFile", priceFile);
        bean.setAttribute("validated", Boolean.toString(validated));
        bean.setAttribute("billingEmail", billingEmail);
        bean.setAttribute("suspended", Boolean.toString(suspended));
        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        bean.setAttribute("timeZone", timeZone);
        bean.setAttribute("language", language);
        bean.setAttribute("chargeRate", Integer.toString(chargeRate));
        bean.setAttribute("registeredCapacity", Integer.toString(registeredCapacity));
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
        Element bean = root.getChild("AccountHolderBean");
        // set up the data
        contact = bean.getAttributeValue("contact", "");
        businessName = bean.getAttributeValue("businessName", "");
        line1 = bean.getAttributeValue("line1", "");
        line2 = bean.getAttributeValue("line2", "");
        line3 = bean.getAttributeValue("line3", "");
        city = bean.getAttributeValue("city", "");
        province = bean.getAttributeValue("province", "");
        postcode = bean.getAttributeValue("postcode", "");
        country = bean.getAttributeValue("country", "");
        phone = bean.getAttributeValue("phone", "");
        superUsername = bean.getAttributeValue("superUsername", "superuser");
        superPassword = bean.getAttributeValue("superPassword", "superPass");
        returnEmailAddress = bean.getAttributeValue("returnEmailAddress", "");
        returnEmailDisplayName = bean.getAttributeValue("returnEmailName", "");
        currency = bean.getAttributeValue("currency", "");
        accountId = bean.getAttributeValue("accountId", "");
        priceFile = bean.getAttributeValue("priceFile", "");
        validated = Boolean.parseBoolean(bean.getAttributeValue("validated", ""));
        billingEmail = bean.getAttributeValue("billingEmail", "");
        suspended = Boolean.parseBoolean(bean.getAttributeValue("suspended", ""));
        timeZone = bean.getAttributeValue("timeZone", "Europe/London");
        language = bean.getAttributeValue("language", "EN_gb");
        chargeRate = Integer.parseInt(bean.getAttributeValue("chargeRate", "-1"));
        registeredCapacity = Integer.parseInt(bean.getAttributeValue("registeredCapacity", "-1"));
    }
}
