/*
 * @(#)ProfileBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The {@code ProfileBean} Class contains the base information for a profile. The
 * attributes notes and status are not included in with the equals() and hashcode()
 *
 * @author  Darryl Oatridge
 * @version 1.01 18-July-2010
 */
public abstract class ProfileBean extends ObjectBean {

    private static final long serialVersionUID = 20100718101L;
    // personal details
    private volatile String title;
    private final LinkedList<String> forenames;
    private volatile String surname;
    // Address
    private volatile String line1;
    private volatile String line2;
    private volatile String line3;
    private volatile String city;
    private volatile String province;
    private volatile String postcode;
    private volatile String country;
    // Contact Information
    private volatile String phone;
    // additional info
    private volatile String notes;

    public ProfileBean(int profileId, String title,
            List<String> forenames, String surname, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String phone, String notes, String owner) {
        super(profileId, owner);
        this.title = title != null ? title : "";
        this.forenames = new LinkedList<String>();
        if(forenames != null && !forenames.isEmpty()) {
            this.forenames.addAll(forenames);
        }
        this.surname = surname != null ? surname : "";
        this.line1 = line1 != null ? line1 : "";
        this.line2 = line2 != null ? line2 : "";
        this.line3 = line3 != null ? line3 : "";
        this.city = city != null ? city : "";
        this.province = province != null ? province : "";
        this.postcode = postcode != null ? postcode : "";
        this.country = country != null ? country : "";
        this.phone = phone != null ? phone : "";
        this.notes = notes != null ? notes : "";
    }

    public ProfileBean() {
        super();
        this.title = "";
        this.forenames = new LinkedList<String>();
        this.surname = "";
        this.line1 = "";
        this.line2 = "";
        this.line3 = "";
        this.city = "";
        this.province = "";
        this.postcode = "";
        this.country = "";
        this.phone = "";
        this.notes = "";
    }

    protected int getProfileId() {
        return super.getIdentifier();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getForenames() {
        return new LinkedList<String>(forenames);
    }

    public String getForename() {
        return forenames.getFirst();
    }

    public String getSurname() {
        return surname;
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

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getPhone() {
        return phone;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasNotes(String s) {
        if(notes.toLowerCase().contains(s.toLowerCase())) {
            return (true);
        }
        return (false);
    }

    public boolean hasName(String s) {
        StringBuilder sb = new StringBuilder('\u001E');
        for(String forename : forenames) {
            sb.append(forename).append('\u001F'); // Unit Sep
        }
        sb.append(surname).append('\u001E');
        if(sb.toString().toLowerCase().contains(s.toLowerCase())) {
            return (true);
        }
        return (false);
    }

    public boolean hasAddress(String s) {
        StringBuilder sb = new StringBuilder('\u001E');
        sb.append(line1).append('\u001F');
        sb.append(line2).append('\u001F');
        sb.append(line3).append('\u001F');
        sb.append(city).append('\u001F');
        sb.append(province).append('\u001F');
        sb.append(postcode).append('\u001F');
        sb.append(country).append('\u001F');
        sb.append(phone).append('\u001E');
        if(sb.toString().toLowerCase().contains(s.toLowerCase())) {
            return (true);
        }
        return (false);
    }

    protected void setProfileName(String title, List<String> forenames, String surname, String owner) {
        this.title = title != null ? title : "";
        this.forenames.clear();
        if(forenames != null && !forenames.isEmpty()) {
            this.forenames.addAll(forenames);
        }
        this.surname = surname != null ? surname : "";
        this.setOwner(owner);
    }

    protected void setProfileAddress(String line1, String line2, String line3, String city,
            String province, String postcode, String country, String owner) {
        this.line1 = line1 != null ? line1 : "";
        this.line2 = line2 != null ? line2 : "";
        this.line3 = line3 != null ? line3 : "";
        this.city = city != null ? city : "";
        this.province = province != null ? province : "";
        this.postcode = postcode != null ? postcode : "";
        this.country = country != null ? country : "";
        this.setOwner(owner);
    }

    protected void setProfilePhone(String phone, String owner) {
        this.phone = phone != null ? phone : "";
        this.setOwner(owner);
    }

    protected void setProfileNotes(String notes, String owner) {
        this.notes = notes != null ? notes : "";
        this.setOwner(owner);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ProfileBean other = (ProfileBean) obj;
        if((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
            return false;
        }
        if(this.forenames != other.forenames && (this.forenames == null || !this.forenames.equals(other.forenames))) {
            return false;
        }
        if((this.surname == null) ? (other.surname != null) : !this.surname.equals(other.surname)) {
            return false;
        }
        if((this.line1 == null) ? (other.line1 != null) : !this.line1.equals(other.line1)) {
            return false;
        }
        if((this.line2 == null) ? (other.line2 != null) : !this.line2.equals(other.line2)) {
            return false;
        }
        if((this.line3 == null) ? (other.line3 != null) : !this.line3.equals(other.line3)) {
            return false;
        }
        if((this.city == null) ? (other.city != null) : !this.city.equals(other.city)) {
            return false;
        }
        if((this.province == null) ? (other.province != null) : !this.province.equals(other.province)) {
            return false;
        }
        if((this.postcode == null) ? (other.postcode != null) : !this.postcode.equals(other.postcode)) {
            return false;
        }
        if((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if((this.phone == null) ? (other.phone != null) : !this.phone.equals(other.phone)) {
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
        hash = 73 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 73 * hash + (this.forenames != null ? this.forenames.hashCode() : 0);
        hash = 73 * hash + (this.surname != null ? this.surname.hashCode() : 0);
        hash = 73 * hash + (this.line1 != null ? this.line1.hashCode() : 0);
        hash = 73 * hash + (this.line2 != null ? this.line2.hashCode() : 0);
        hash = 73 * hash + (this.line3 != null ? this.line3.hashCode() : 0);
        hash = 73 * hash + (this.city != null ? this.city.hashCode() : 0);
        hash = 73 * hash + (this.province != null ? this.province.hashCode() : 0);
        hash = 73 * hash + (this.postcode != null ? this.postcode.hashCode() : 0);
        hash = 73 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 73 * hash + (this.phone != null ? this.phone.hashCode() : 0);
        hash = 73 * hash + (this.notes != null ? this.notes.hashCode() : 0);
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
        Element bean = new Element("ProfileBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("title", title);
        bean.setAttribute("surname", surname);
        bean.setAttribute("line1", line1);
        bean.setAttribute("line2", line2);
        bean.setAttribute("line3", line3);
        bean.setAttribute("city", city);
        bean.setAttribute("province", province);
        bean.setAttribute("postcode", postcode);
        bean.setAttribute("country", country);
        bean.setAttribute("phone", phone);
        bean.setAttribute("notes", notes);
        Element allElements = new Element("forenames_set");
        bean.addContent(allElements);
        if(forenames != null) {
            for(String eachValue : forenames) {
                Element eachElement = new Element("forenames");
                eachElement.setAttribute("value", eachValue);
                allElements.addContent(eachElement);
            }
        }
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
        Element bean = root.getChild("ProfileBean");
        // set up the data
        title = bean.getAttributeValue("title","");
        surname = bean.getAttributeValue("surname","");
        line1 = bean.getAttributeValue("line1","");
        line2 = bean.getAttributeValue("line2","");
        line3 = bean.getAttributeValue("line3","");
        city = bean.getAttributeValue("city","");
        province = bean.getAttributeValue("province","");
        postcode = bean.getAttributeValue("postcode","");
        country = bean.getAttributeValue("country","");
        phone = bean.getAttributeValue("phone","");
        notes = bean.getAttributeValue("notes","");
        forenames.clear();
        @SuppressWarnings("unchecked")
        List<Element> allElements = (List<Element>) bean.getChild("forenames_set").getChildren("forenames");
        for(Element eachElement : allElements) {
            forenames.add(eachElement.getAttributeValue("value", ""));
        }
    }
}
