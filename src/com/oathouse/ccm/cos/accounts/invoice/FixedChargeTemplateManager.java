/*
 * @(#)FixedChargeTemplateManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.invoice;

import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code FixedChargeTemplateManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 04-Feb-2011
 */
public class FixedChargeTemplateManager extends ObjectSetStore<FixedChargeBean> {

    /**
     * Constructs a {@code FixedChargeTemplateManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public FixedChargeTemplateManager(String managerName) {
        super(managerName);
    }

}
