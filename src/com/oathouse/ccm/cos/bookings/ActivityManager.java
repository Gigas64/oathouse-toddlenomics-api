/*
 * @(#)ActivityManager.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.bookings;

import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code ActivityManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 25-May-2013
 */
public class ActivityManager extends ObjectSetStore<ActivityBean> {

    /**
     * Constructs a {@code ActivityManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions an optional list of ObjectDataOptionsEnum
     * @see ObjectDataOptionsEnum
     */
    public ActivityManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

}
