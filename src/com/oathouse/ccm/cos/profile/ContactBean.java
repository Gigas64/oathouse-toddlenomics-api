/*
 * @(#)ContactBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code ContactBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.01 18-July-2010
 */
public class ContactBean extends ProfileBean {

    private static final long serialVersionUID = 20100718101L;
    private volatile String commonName;
    private volatile String business;
    private volatile String phone2;
    private volatile String mobile;
    private volatile String email;
    private volatile RelationType type;
    private volatile int photoId;
    private volatile int availableStart;
    private volatile int availableEnd;

    public ContactBean(int contactId, String title, List<String> forenames, String surname, String commonName,
                       String line1, String line2, String line3, String city, String province, String postcode,
                       String country,
                       String business, String phone, String phone2, String mobile, String email, int photoId,
                       int availableStart, int availableEnd, RelationType type, String notes, String owner) {
        super(contactId, title, forenames, surname, line1, line2, line3, city, province, postcode, country, phone, notes, owner);
        this.commonName = commonName;
        this.business = business;
        this.phone2 = phone2;
        this.mobile = mobile;
        this.email = email;
        this.photoId = photoId;
        this.availableStart = availableStart;
        this.availableEnd = availableEnd;
        this.type = type;
        if(!this.type.isProfessional()) {
            this.type = RelationType.NON_PROFESSIONAL;
        }
    }

    public ContactBean() {
        super();
        this.commonName = "";
        this.business = "";
        this.phone2 = "";
        this.mobile = "";
        this.email = "";
        this.photoId = ObjectEnum.INITIALISATION.value();
        this.availableStart = ObjectEnum.INITIALISATION.value();
        this.availableEnd = ObjectEnum.INITIALISATION.value();
        this.type = RelationType.UNDEFINED;
    }

    public int getContactId() {
        return super.getProfileId();
    }

    public String getCommonName() {
        return commonName;
    }

    public String getBusiness() {
        return business;
    }

    public String getPhone2() {
        return phone2;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public int getPhotoId() {
        return photoId;
    }

    public int getAvailableStart() {
        return availableStart;
    }

    public int getAvailableEnd() {
        return availableEnd;
    }

    /**
     * Helper method to get the availableEnd + 1
     * @return availableEnd + 1
     */
    public int getAvailableEndOut() {
        return availableEnd + 1;
    }

    public RelationType getType() {
        return type;
    }

    protected void setType(RelationType type, String owner) {
        this.type = type;
        super.setOwner(owner);
    }

    protected void setPersonalDetails(String title, List<String> forenames, String surname, String commonName,
            String business, int availableStart, int availableEnd, String owner) {
        this.setProfileName(title, forenames, surname, owner);
        this.commonName = commonName;
        this.business = business;
        this.availableStart = availableStart;
        this.availableEnd = availableEnd;
    }

    protected void setPhotoId(int photoId, String owner)  {
        this.photoId = photoId;
        this.setOwner(owner);
    }

    protected void setCommunications(String phone, String phone2, String mobile, String email, String owner)  {
        this.setProfilePhone(phone, owner);
        this.phone2 = phone2 != null ? phone2 : "";
        this.mobile = mobile != null ? mobile : "";
        this.email = email != null ? email : "";
    }

    public static final Comparator<ContactBean> CASE_INSENSITIVE_BUSINESS_ORDER = new Comparator<ContactBean>() {
        public int compare(ContactBean c1, ContactBean c2) {
            if (c1 == null && c2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (c1 != null && c2 == null) {
                return -1;
            }
            if (c1 == null && c2 != null) {
                return 1;
            }
            // compare
            int result = c1.getBusiness().toLowerCase().compareTo(c2.getBusiness().toLowerCase());
            if(result != 0) {
                return result;
            }
           // compare might not be unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)c1.getIdentifier()).compareTo((Integer)c2.getIdentifier()));
        }
    };

    public static final Comparator<ContactBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<ContactBean>() {
        public int compare(ContactBean c1, ContactBean c2) {
            if (c1 == null && c2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if (c1 != null && c2 == null) {
                return -1;
            }
            if (c1 == null && c2 != null) {
                return 1;
            }
            // compare
            StringBuilder s = new StringBuilder();
            s.append(c1.getSurname());
            s.append(c1.getCommonName());
            s.append(c1.getBusiness());
            String s1 = s.toString().toLowerCase();
            s = new StringBuilder();
            s.append(c2.getSurname());
            s.append(c2.getCommonName());
            s.append(c2.getBusiness());
            String s2 = s.toString().toLowerCase();

            int result = s1.compareTo(s2);
            if(result != 0) {
                return result;
            }
           // compare might not be unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer)c1.getIdentifier()).compareTo((Integer)c2.getIdentifier()));
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
        final ContactBean other = (ContactBean) obj;
        if((this.commonName == null) ? (other.commonName != null) : !this.commonName.equals(other.commonName)) {
            return false;
        }
        if((this.business == null) ? (other.business != null) : !this.business.equals(other.business)) {
            return false;
        }
        if((this.phone2 == null) ? (other.phone2 != null) : !this.phone2.equals(other.phone2)) {
            return false;
        }
        if((this.mobile == null) ? (other.mobile != null) : !this.mobile.equals(other.mobile)) {
            return false;
        }
        if((this.email == null) ? (other.email != null) : !this.email.equals(other.email)) {
            return false;
        }
        if(this.photoId != other.photoId) {
            return false;
        }
        if(this.availableStart != other.availableStart) {
            return false;
        }
        if(this.availableEnd != other.availableEnd) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.commonName != null ? this.commonName.hashCode() : 0);
        hash = 41 * hash + (this.business != null ? this.business.hashCode() : 0);
        hash = 41 * hash + (this.phone2 != null ? this.phone2.hashCode() : 0);
        hash = 41 * hash + (this.mobile != null ? this.mobile.hashCode() : 0);
        hash = 41 * hash + (this.email != null ? this.email.hashCode() : 0);
        hash = 41 * hash + this.photoId;
        hash = 41 * hash + this.availableStart;
        hash = 41 * hash + this.availableEnd;
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
        Element bean = new Element("ContactBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("commonName", commonName);
        bean.setAttribute("business", business);
        bean.setAttribute("phone2", phone2);
        bean.setAttribute("mobile", mobile);
        bean.setAttribute("email", email);
        bean.setAttribute("photoId", Integer.toString(photoId));
        bean.setAttribute("availableStart", Integer.toString(availableStart));
        bean.setAttribute("availableEnd", Integer.toString(availableEnd));
        if(!type.isProfessional()) {
            type = RelationType.NON_PROFESSIONAL;
        }
        bean.setAttribute("type", type.toString());

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
        Element bean = root.getChild("ContactBean");
        // set up the data
        commonName = bean.getAttributeValue("commonName");
        business = bean.getAttributeValue("business");
        phone2 = bean.getAttributeValue("phone2");
        mobile = bean.getAttributeValue("mobile");
        email = bean.getAttributeValue("email");
        photoId = Integer.parseInt(bean.getAttributeValue("photoId", "-1"));
        availableStart = Integer.parseInt(bean.getAttributeValue("availableStart", "-1"));
        availableEnd = Integer.parseInt(bean.getAttributeValue("availableEnd", "-1"));
        type = RelationType.valueOf(bean.getAttributeValue("type"));
    }
}
