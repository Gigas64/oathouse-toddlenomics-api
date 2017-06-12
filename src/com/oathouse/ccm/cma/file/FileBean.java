/**
 * @(#)FileBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.file;

import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.jdom2.Element;

/**
 * The {@code FileBean} Class is an FileBean to store Image Information.
 *
 *
 * @author      Darryl Oatridge
 * @version 	1.00 7-Apr-2011
 */
public class FileBean extends ObjectBean {

    private static final long serialVersionUID = 20110407100L;
    private volatile String name;
    private volatile String bytes;
    private volatile String contentType;
    private volatile String suffix;

    /**
     * Constructor for {@code FileBean} Class.
     */
    public FileBean(int fileId, String name, File file, String contentType, String suffix, String owner) throws IOException {
        super(fileId, owner);
        this.name = name;
        this.bytes = Base64.encodeBase64URLSafeString(FileUtils.readFileToByteArray(file));
        this.contentType = contentType;
        this.suffix = suffix;
    }

    public FileBean() {
        super();
        this.name = "";
        this.bytes = "";
        this.contentType = "";
        this.suffix = "";
    }

    public int getFileId() {
        return this.getIdentifier();
    }

    public String getName() {
        return name;
    }

    public void getFile(File file) throws IOException {
        FileUtils.writeByteArrayToFile(file, Base64.decodeBase64(bytes));
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public static final Comparator<FileBean> CASE_INSENSITIVE_NAME_ORDER = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean p1, FileBean p2) {
            if(p1 == null && p2 == null) {
                return 0;
            }
            // just in case there are null object values show them last
            if(p1 != null && p2 == null) {
                return -1;
            }
            if(p1 == null && p2 != null) {
                return 1;
            }
            // compare
            int result = (p1.getName().toLowerCase()).compareTo(p2.getName().toLowerCase());
            if(result != 0) {
                return result;
            }
            // dob not unique so violates the equals comparability. Can cause disappearing objects in Sets
            return (((Integer) p1.getIdentifier()).compareTo((Integer) p2.getIdentifier()));
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
        final FileBean other = (FileBean) obj;
        if((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if((this.bytes == null) ? (other.bytes != null) : !this.bytes.equals(other.bytes)) {
            return false;
        }
        if((this.contentType == null) ? (other.contentType != null) : !this.contentType.equals(other.contentType)) {
            return false;
        }
        if((this.suffix == null) ? (other.suffix != null) : !this.suffix.equals(other.suffix)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (this.bytes != null ? this.bytes.hashCode() : 0);
        hash = 43 * hash + (this.contentType != null ? this.contentType.hashCode() : 0);
        hash = 43 * hash + (this.suffix != null ? this.suffix.hashCode() : 0);
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
        Element bean = new Element("FileBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("name", name);
        bean.setAttribute("bytes", bytes);
        bean.setAttribute("contentType", contentType);
        bean.setAttribute("suffix", suffix);
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
        Element bean = root.getChild("FileBean");
        // set up the data
        name = bean.getAttributeValue("name", "");
        bytes = bean.getAttributeValue("bytes", "");
        contentType = bean.getAttributeValue("contentType", "");
        suffix = bean.getAttributeValue("suffix", "");
    }

}
