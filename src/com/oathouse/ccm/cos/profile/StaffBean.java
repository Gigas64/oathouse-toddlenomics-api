/*
 * @(#)StaffBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code StaffBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 11-Sep-2011
 */
public class StaffBean extends ProfileBean {

    private static final long serialVersionUID = 20110911100L;

    private volatile String staffRef;
    private volatile String commonName;
    private volatile String jobTitle;
    private volatile int dateOfBirth;
    private volatile String mobile;
    private volatile String email;
    private volatile int photoId;
    private volatile int startYwd;
    private volatile int departYwd;
    private volatile int noticeYwd;
    private volatile int defaultRoom;

    public StaffBean(int staffId, String title, List<String> forenames, String surname, String commonName,
            int dateOfBirth, String staffRef, String jobTitle, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String phone, String mobile, String email,
            int startYwd, int departYwd, int noticeYwd, int photoId, int defaultRoom, String notes, String owner) {
        super(staffId, title, forenames, surname, line1, line2, line3, city, province, postcode, country, phone, notes, owner);
        this.staffRef = staffRef != null ? staffRef : "";
        this.commonName = commonName != null ? commonName : "";
        this.jobTitle = jobTitle != null ? jobTitle : "";
        this.dateOfBirth = dateOfBirth;
        this.mobile = mobile != null ? mobile : "";
        this.email = email != null ? email : "";
        this.photoId = photoId;
        this.startYwd = startYwd;
        this.departYwd = departYwd;
        this.noticeYwd = noticeYwd;
        this.defaultRoom = defaultRoom;
    }

    public StaffBean() {
        super();
        this.staffRef = staffRef != null ? staffRef : "";
        this.jobTitle = jobTitle != null ? jobTitle : "";
        this.commonName = commonName != null ? commonName : "";
        this.dateOfBirth = ObjectEnum.INITIALISATION.value();
        this.mobile = mobile != null ? mobile : "";
        this.email = email != null ? email : "";
        this.photoId = ObjectEnum.INITIALISATION.value();
        this.startYwd = ObjectEnum.INITIALISATION.value();
        this.departYwd = ObjectEnum.INITIALISATION.value();
        this.noticeYwd = ObjectEnum.INITIALISATION.value();
        this.defaultRoom = ObjectEnum.INITIALISATION.value();
    }

    public int getStaffId() {
        return(super.getProfileId());
    }

    public String getCommonName() {
        return commonName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public int getDateOfBirth() {
        return dateOfBirth;
    }

    public int getDepartYwd() {
        return departYwd;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public int getNoticeYwd() {
        return noticeYwd;
    }

    public int getPhotoId() {
        return photoId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getStaffRef() {
        return staffRef;
    }

    public int getStartYwd() {
        return startYwd;
    }

    public int getDefaultRoom() {
        return defaultRoom;
    }

    protected void setCommonName(String commonName, String owner) {
        this.commonName = commonName != null ? commonName : "";
        super.setOwner(owner);
    }

    protected void setDateOfBirth(int dateOfBirth, String owner) {
        this.dateOfBirth = dateOfBirth;
        super.setOwner(owner);
    }

    protected void setDefaultRoom(int defaultRoom, String owner) {
        this.defaultRoom = defaultRoom;
        super.setOwner(owner);
    }

    protected void setDepartYwd(int departYwd, String owner) {
        this.departYwd = departYwd;
        super.setOwner(owner);
    }

    protected void setEmail(String email, String owner) {
        this.email = email != null ? email : "";
        super.setOwner(owner);
    }

    protected void setJobTitle(String jobTitle, String owner) {
        this.jobTitle = jobTitle != null ? jobTitle : "";
        super.setOwner(owner);
    }

    protected void setMobile(String mobile, String owner) {
        this.mobile = mobile != null ? mobile : "";
        super.setOwner(owner);
    }

    protected void setNoticeYwd(int noticeYwd, String owner) {
        this.noticeYwd = noticeYwd;
        super.setOwner(owner);
    }

    protected void setPhotoId(int photoId, String owner) {
        this.photoId = photoId;
        super.setOwner(owner);
    }

    protected void setStaffRef(String staffRef, String owner) {
        this.staffRef = staffRef != null ? staffRef : "";
        super.setOwner(owner);
    }

    protected void setStartYwd(int startYwd, String owner) {
        this.startYwd = startYwd;
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
        final StaffBean other = (StaffBean) obj;
        if((this.staffRef == null) ? (other.staffRef != null) : !this.staffRef.equals(other.staffRef)) {
            return false;
        }
        if((this.commonName == null) ? (other.commonName != null) : !this.commonName.equals(other.commonName)) {
            return false;
        }
        if((this.jobTitle == null) ? (other.jobTitle != null) : !this.jobTitle.equals(other.jobTitle)) {
            return false;
        }
        if(this.dateOfBirth != other.dateOfBirth) {
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
        if(this.startYwd != other.startYwd) {
            return false;
        }
        if(this.departYwd != other.departYwd) {
            return false;
        }
        if(this.noticeYwd != other.noticeYwd) {
            return false;
        }
        if(this.defaultRoom != other.defaultRoom) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.staffRef != null ? this.staffRef.hashCode() : 0);
        hash = 83 * hash + (this.commonName != null ? this.commonName.hashCode() : 0);
        hash = 83 * hash + (this.jobTitle != null ? this.jobTitle.hashCode() : 0);
        hash = 83 * hash + this.dateOfBirth;
        hash = 83 * hash + (this.mobile != null ? this.mobile.hashCode() : 0);
        hash = 83 * hash + (this.email != null ? this.email.hashCode() : 0);
        hash = 83 * hash + this.photoId;
        hash = 83 * hash + this.startYwd;
        hash = 83 * hash + this.departYwd;
        hash = 83 * hash + this.noticeYwd;
        hash = 83 * hash + this.defaultRoom;
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
        Element bean = new Element("StaffBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("staffRef", staffRef);
        bean.setAttribute("jobTitle", jobTitle);
        bean.setAttribute("commonName", commonName);
        bean.setAttribute("dateOfBirth", Integer.toString(dateOfBirth));
        bean.setAttribute("mobile", mobile);
        bean.setAttribute("email", email);
        bean.setAttribute("photoId", Integer.toString(photoId));
        bean.setAttribute("startYwd", Integer.toString(startYwd));
        bean.setAttribute("departYwd", Integer.toString(departYwd));
        bean.setAttribute("noticeYwd", Integer.toString(noticeYwd));
        bean.setAttribute("defaultRoom", Integer.toString(defaultRoom));

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
        Element bean = root.getChild("StaffBean");
        // set up the data
        staffRef = bean.getAttributeValue("staffRef");
        jobTitle = bean.getAttributeValue("jobTitle");
        commonName = bean.getAttributeValue("commonName");
        dateOfBirth = Integer.parseInt(bean.getAttributeValue("dateOfBirth", "0"));
        mobile = bean.getAttributeValue("mobile");
        email = bean.getAttributeValue("email");
        photoId = Integer.parseInt(bean.getAttributeValue("photoId", "-1"));
        startYwd = Integer.parseInt(bean.getAttributeValue("startYwd", "-1"));
        departYwd = Integer.parseInt(bean.getAttributeValue("departYwd", "-1"));
        noticeYwd = Integer.parseInt(bean.getAttributeValue("noticeYwd", "-1"));
        defaultRoom = Integer.parseInt(bean.getAttributeValue("defaultRoom", "-1"));

    }
}
