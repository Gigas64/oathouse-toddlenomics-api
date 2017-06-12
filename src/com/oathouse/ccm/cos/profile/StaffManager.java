/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)StaffManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import java.util.List;

/**
 * The {@code StaffManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 11-Sep-2011
 */
public class StaffManager extends ObjectSetStore<StaffBean> {

    /**
     * Constructs a {@code StaffManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public StaffManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /* ***************************************************
     * Protected Set Methods including the bean create
     * ***************************************************/

    public StaffBean createStaff(String title, List<String> forenames, String surname, String commonName,
            String staffRef, String notes, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException {
        StaffBean staff = new StaffBean(
                this.generateIdentifier(), /*id*/
                title, /*title*/
                forenames,  /*forenames*/
                surname,  /*surname*/
                commonName, /*commonName*/
                ObjectEnum.INITIALISATION.value(),  /*dateOfBirth*/
                staffRef,  /*staffRef*/
                "",  /*jobTitle*/
                "",  /*line1*/
                "",  /*line2*/
                "",  /*line3*/
                "",  /*city*/
                "",  /*province*/
                "",  /*postcode*/
                "",  /*country*/
                "",  /*phone*/
                "",  /*mobile*/
                "",  /*email*/
                ObjectEnum.INITIALISATION.value(),  /*startYwd*/
                ObjectEnum.INITIALISATION.value(),  /*departYwd*/
                ObjectEnum.INITIALISATION.value(),  /*noticeYwd*/
                ObjectEnum.INITIALISATION.value(),  /*photoId*/
                ObjectEnum.INITIALISATION.value(), /*defaultRoom*/
                notes,  /*notes*/
                owner /*owner*/
                );
        // now set the object
        return setObject(staff);
    }

    public StaffBean setStaffMainDetails(int staffId, String title, List<String> forenames, String surname, String commonName,
            String staffRef, String notes, String owner) throws PersistenceException, NullObjectException, NoSuchIdentifierException {
        StaffBean staff = getObject(staffId);
        staff.setProfileName(title, forenames, surname, owner);
        staff.setCommonName(commonName, owner);
        staff.setStaffRef(staffRef, owner);
        staff.setProfileNotes(notes, owner);
        return setObject(staff);

    }

    public StaffBean setStaffAddress(int staffId, String line1, String line2, String line3, String city,
            String province, String postcode, String country, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        StaffBean staff = getObject(staffId);
        staff.setProfileAddress(line1, line2, line3, city, province, postcode, country, owner);
        return setObject(staff);
    }

    public StaffBean setStaffContacts(int staffId, String phone, String mobile, String email, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        StaffBean staff = getObject(staffId);
        staff.setProfilePhone(phone, owner);
        staff.setMobile(mobile, owner);
        staff.setEmail(email, owner);
        return setObject(staff);
    }

    public StaffBean setStaffStatus(int staffId, int dateOfBirth, String jobTitle, int defaultRoom, int startYwd, int departYwd, int noticeYwd, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        StaffBean staff = getObject(staffId);
        staff.setDateOfBirth(dateOfBirth, owner);
        staff.setJobTitle(jobTitle, owner);
        staff.setDefaultRoom(defaultRoom, owner);
        staff.setStartYwd(startYwd, owner);
        staff.setDepartYwd(departYwd, owner);
        staff.setNoticeYwd(noticeYwd, owner);
        return setObject(staff);
    }

    public StaffBean setStaffPhoto(int staffId, int photoId, String owner) throws NoSuchIdentifierException, PersistenceException, NullObjectException {
        StaffBean staff = getObject(staffId);
        staff.setPhotoId(photoId, owner);
        return setObject(staff);
    }

}
