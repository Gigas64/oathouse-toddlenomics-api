/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)RosterStaffManager.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.ObjectMapStore;

/**
 * The {@code RosterStaffManager} Class extends the methods of the parent class.
 *
 * Key - Ywd
 * Id  - rosterStaffId
 *
 *
 * @author Darryl Oatridge
 * @version 1.00 25-May-2013
 */
public class RosterStaffManager extends ObjectMapStore<RosterStaffBean> {

    /**
     * Constructs a {@code RosterStaffManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public RosterStaffManager(String managerName) {
        super(managerName);
    }

}
