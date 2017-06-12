/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)StaffQualificationManager.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;

/**
 * The {@code StaffQualificationManager} Class extends the methods of the parent class.
 *
 * Key = StaffId
 * Id  = QualificationId
 *
 * @author Darryl Oatridge
 * @version 1.00 06-Oct-2013
 */
public class StaffQualificationManager extends ObjectMapStore<StaffQualificationBean> {

    /**
     * Constructs a {@code StaffQualificationManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions an optional list of ObjectDataOptionsEnum
     * @see ObjectDataOptionsEnum
     */
    public StaffQualificationManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

}
