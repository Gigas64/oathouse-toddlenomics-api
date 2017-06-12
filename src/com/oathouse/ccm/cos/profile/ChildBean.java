/*
 * @(#)ChildBean.java
 *
 * Copyright:	Copyright (c) 2009
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code ChildBean} Class in an information storage class for profile information.
 *
 * @author Darryl Oatridge
 * @version 2.03 15-Oct-2010
 */
public class ChildBean extends ProfileBean {

    private static final long serialVersionUID = 20101015203L;
    // personal details
    private volatile int accountId; // a child must belong to an account
    private volatile String commonName;
    private volatile int dateOfBirth;
    private volatile int photoId;
    private volatile int holidayAllocation;
    private volatile int bookingDiscountRate;
    private volatile int fixedItemDiscountRate;
    private volatile String gender;
    private volatile int departYwd;
    private volatile int noticeYwd;
    private volatile boolean medFormReceived;
    private volatile boolean tariffDiscountOff;

    private ChildBean(int profileId, int accountId, String title, List<String> forenames, String surname, String line1,
            String line2, String line3, String city, String province, String postcode, String country, String phone,
            String notes, String commonName, int dateOfBirth, int photoId, int holidayAllocation,
            int bookingDiscountRate, int fixedItemDiscountRate, String gender, int departYwd, int noticeYwd,
            boolean medFormReceived, boolean tariffDiscountOff, String owner) {
        super(profileId, title, forenames, surname, line1, line2, line3, city, province, postcode, country, phone, notes, owner);
        this.accountId = accountId;
        this.commonName = commonName;
        this.dateOfBirth = dateOfBirth;
        this.photoId = photoId;
        this.holidayAllocation = holidayAllocation;
        this.bookingDiscountRate = bookingDiscountRate;
        this.fixedItemDiscountRate = fixedItemDiscountRate;
        this.gender = gender;
        this.departYwd = departYwd;
        this.noticeYwd = noticeYwd;
        this.medFormReceived = medFormReceived;
        this.tariffDiscountOff = tariffDiscountOff;
    }

    public ChildBean(
            int profileId,
            int accountId,
            String title,
            List<String> forenames,
            String surname,
            String commonName,
            String line1, String line2, String line3, String city, String province, String postcode, String country,
            String phone,
            String gender,
            int dateOfBirth,
            int photoId,
            int holidayAllocation,
            int departYwd, int noticeYwd,
            boolean medFormReceived,
            boolean tariffDiscountOff,
            String notes, String owner) {

        // bookingDiscountRate and fixedItemDiscountRate are both set to 0
        this(profileId, accountId, title, forenames, surname, line1, line2, line3, city, province, postcode, country,
                phone, notes, commonName, dateOfBirth, photoId, holidayAllocation, 0, 0, gender, departYwd,
                noticeYwd, medFormReceived, tariffDiscountOff, owner);
    }

    public ChildBean() {
        super();
        this.accountId = ObjectEnum.INITIALISATION.value();
        this.commonName = "";
        this.dateOfBirth = ObjectEnum.INITIALISATION.value();
        this.holidayAllocation = ObjectEnum.INITIALISATION.value();
        this.bookingDiscountRate = ObjectEnum.INITIALISATION.value();
        this.fixedItemDiscountRate = ObjectEnum.INITIALISATION.value();
        this.gender = "";
        this.departYwd = ObjectEnum.INITIALISATION.value();
        this.noticeYwd = ObjectEnum.INITIALISATION.value();
        this.photoId = ObjectEnum.INITIALISATION.value();
        this.medFormReceived = false;
        this.tariffDiscountOff = false;

    }

    public int getChildId() {
        return super.getProfileId();
    }

    public int getAccountId() {
        return accountId;
    }

    public String getCommonName() {
        return commonName;
    }

    public int getDateOfBirth() {
        return dateOfBirth;
    }

    public int getHolidayAllocation() {
        return holidayAllocation;
    }

    public int getBookingDiscountRate() {
        return bookingDiscountRate;
    }

    public int getFixedItemDiscountRate() {
        return fixedItemDiscountRate;
    }

    public String getGender() {
        return gender;
    }

    public int getDepartYwd() {
        return departYwd;
    }

    public int getNoticeYwd() {
        return noticeYwd;
    }

    public int getPhotoId() {
        return photoId;
    }

    public boolean isMedFormReceived() {
        return medFormReceived;
    }

    public boolean isTariffDiscountOff() {
        return tariffDiscountOff;
    }

    @Override
    public boolean hasName(String s) {
        if(commonName.toLowerCase().contains(s.toLowerCase()) || super.hasName(s)) {
            return (true);
        }
        return (false);
    }

    public boolean isDOB(int startYwdKey, int endYwdKey) {
        YWDHolder dob = new YWDHolder(dateOfBirth);
        return (dob.inRange(startYwdKey, endYwdKey));
    }

    protected void setAccountId(int accountId, String owner) {
        this.accountId = accountId;
        this.setOwner(owner);
    }

    protected void setPersonalDetails(String title, List<String> forenames, String surname,
            String commonName, String gender, int dateOfBirth, String owner) {
        this.setProfileName(title, forenames, surname, owner);
        this.commonName = commonName != null ? commonName : "";
        this.gender = gender != null ? gender : "";
        this.dateOfBirth = dateOfBirth;
    }

    protected void setHolidayAllocation(int holidayAllocation, String owner) {
        this.holidayAllocation = holidayAllocation;
        this.setOwner(owner);
    }

    protected void setBookingDiscountRate(int bookingDiscountRate, String owner) {
        this.bookingDiscountRate = bookingDiscountRate;
        this.setOwner(owner);
    }

    protected void setFixedItemDiscountRate(int fixedItemDiscountRate, String owner) {
        this.fixedItemDiscountRate = fixedItemDiscountRate;
        this.setOwner(owner);
    }

    protected void setPhotoId(int photoId, String owner) {
        this.photoId = photoId;
        this.setOwner(owner);
    }

    protected void setNoticeYwd(int noticeYwd, String owner) {
        this.noticeYwd = noticeYwd;
        this.setOwner(owner);
    }

    protected void setDepartYwd(int departYwd, String owner) {
        this.departYwd = departYwd;
        this.setOwner(owner);
    }

    protected void setMedFormReceived(boolean medFormReceived, String owner) {
        this.medFormReceived = medFormReceived;
        this.setOwner(owner);
    }

    protected void setTariffDiscountOff(boolean tariffDiscountOff, String owner) {
        this.tariffDiscountOff = tariffDiscountOff;
        this.setOwner(owner);
    }

    /**
     * Comparator to order the beans into DOB order from youngest to oldest.
     */
    public static final Comparator<ChildBean> DOB_ORDER = new Comparator<ChildBean>() {
        @Override
        public int compare(ChildBean c1, ChildBean c2) {
            if(c1 == null && c2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(c1 != null && c2 == null) {
                return -1;
            }
            if(c1 == null && c2 != null) {
                return 1;
            }
            // compare
            int result = ((Integer) c1.getDateOfBirth()).compareTo((Integer) c2.getDateOfBirth());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) c1.getIdentifier()).compareTo((Integer) c2.getIdentifier()));
        }
    };

    /**
     * Comparator to order the beans into DOB order from youngest to oldest.
     */
    public static final Comparator<ChildBean> REVERSE_DOB_ORDER = new Comparator<ChildBean>() {
        @Override
        public int compare(ChildBean c1, ChildBean c2) {
            if(c1 == null && c2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(c1 != null && c2 == null) {
                return -1;
            }
            if(c1 == null && c2 != null) {
                return 1;
            }
            // compare
            int result = ((Integer) c2.getDateOfBirth()).compareTo((Integer) c1.getDateOfBirth());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) c2.getIdentifier()).compareTo((Integer) c1.getIdentifier()));
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
        final ChildBean other = (ChildBean) obj;
        if(this.accountId != other.accountId) {
            return false;
        }
        if(!Objects.equals(this.commonName, other.commonName)) {
            return false;
        }
        if(this.dateOfBirth != other.dateOfBirth) {
            return false;
        }
        if(this.photoId != other.photoId) {
            return false;
        }
        if(this.holidayAllocation != other.holidayAllocation) {
            return false;
        }
        if(this.bookingDiscountRate != other.bookingDiscountRate) {
            return false;
        }
        if(this.fixedItemDiscountRate != other.fixedItemDiscountRate) {
            return false;
        }
        if(!Objects.equals(this.gender, other.gender)) {
            return false;
        }
        if(this.departYwd != other.departYwd) {
            return false;
        }
        if(this.noticeYwd != other.noticeYwd) {
            return false;
        }
        if(this.medFormReceived != other.medFormReceived) {
            return false;
        }
        if(this.tariffDiscountOff != other.tariffDiscountOff) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.accountId;
        hash = 29 * hash + Objects.hashCode(this.commonName);
        hash = 29 * hash + this.dateOfBirth;
        hash = 29 * hash + this.photoId;
        hash = 29 * hash + this.holidayAllocation;
        hash = 29 * hash + this.bookingDiscountRate;
        hash = 29 * hash + this.fixedItemDiscountRate;
        hash = 29 * hash + Objects.hashCode(this.gender);
        hash = 29 * hash + this.departYwd;
        hash = 29 * hash + this.noticeYwd;
        hash = 29 * hash + (this.medFormReceived ? 1 : 0);
        hash = 29 * hash + (this.tariffDiscountOff ? 1 : 0);
        return hash + super.hashCode();
    }

    /**
     * crates all the elements that represent this bean at this level.
     *
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<Element>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element("ChildBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("accountId", Integer.toString(accountId));
        bean.setAttribute("commonName", commonName);
        bean.setAttribute("dateOfBirth", Integer.toString(dateOfBirth));
        bean.setAttribute("photoId", Integer.toString(photoId));
        bean.setAttribute("holidayAllocation", Integer.toString(holidayAllocation));
        bean.setAttribute("bookingDiscountRate", Integer.toString(bookingDiscountRate));
        bean.setAttribute("fixedItemDiscountRate", Integer.toString(fixedItemDiscountRate));
        bean.setAttribute("gender", gender);
        bean.setAttribute("departYwd", Integer.toString(departYwd));
        bean.setAttribute("noticeYwd", Integer.toString(noticeYwd));
        bean.setAttribute("medFormReceived", Boolean.toString(medFormReceived));
        bean.setAttribute("tariffDiscountOff", Boolean.toString(tariffDiscountOff));
        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to put default values in getAttribute() and check the
     * content of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("ChildBean");
        // set up the data
        accountId = Integer.parseInt(bean.getAttributeValue("accountId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        commonName = bean.getAttributeValue("commonName");
        dateOfBirth = Integer.parseInt(bean.getAttributeValue("dateOfBirth", Integer.toString(ObjectEnum.INITIALISATION.value())));
        photoId = Integer.parseInt(bean.getAttributeValue("photoId", Integer.toString(ObjectEnum.INITIALISATION.value())));
        holidayAllocation = Integer.parseInt(bean.getAttributeValue("holidayAllocation", Integer.toString(ObjectEnum.INITIALISATION.value())));
        bookingDiscountRate = Integer.parseInt(bean.getAttributeValue("bookingDiscountRate", Integer.toString(ObjectEnum.INITIALISATION.value())));
        fixedItemDiscountRate = Integer.parseInt(bean.getAttributeValue("fixedItemDiscountRate", Integer.toString(ObjectEnum.INITIALISATION.value())));
        gender = bean.getAttributeValue("gender");
        departYwd = Integer.parseInt(bean.getAttributeValue("departYwd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        noticeYwd = Integer.parseInt(bean.getAttributeValue("noticeYwd", Integer.toString(ObjectEnum.INITIALISATION.value())));
        medFormReceived = Boolean.parseBoolean(bean.getAttributeValue("medFormReceived", "false"));
        tariffDiscountOff = Boolean.parseBoolean(bean.getAttributeValue("tariffDiscountOff", "false"));
    }
}
