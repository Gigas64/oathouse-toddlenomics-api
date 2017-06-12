/*
 * @(#)RelationManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;

/**
 * The {@code RelationManager} Class extends the methods of the parent class.
 * The Map Store construct is:
 *
 * @author Darryl Oatridge
 * @version 1.00 22-Aug-2010
 */
public class RelationManager extends ObjectMapStore<RelationBean> {

    /**
     * Constructs a {@code RelationManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public RelationManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

}
