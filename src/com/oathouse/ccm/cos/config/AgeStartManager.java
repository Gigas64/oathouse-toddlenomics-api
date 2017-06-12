/*
 * @(#)AgeStartManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;


/**
 * The {@code AgeStartManager} Class extends the methods of the parent class.
 * accountId - invoiceId
 *
 * @author Nick Maunder
 * @version 1.00 14-Dec-2010
 */
public class AgeStartManager extends ObjectSetStore<AgeStartBean> {

    /**
     * Constructs a {@code AgeStartManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public AgeStartManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Finds the AgeStartBean with the highest startAgeMonths that is lower than or equal to ageMonths
     *
     * @param ageMonths
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public AgeStartBean getObjectByAge(int ageMonths) throws PersistenceException, IllegalValueException {
        int highest = Integer.MIN_VALUE;
        AgeStartBean toRtn = null;
        for(AgeStartBean bean : getAllObjects()) {
            if(ageMonths >= bean.getStartAgeMonths() && bean.getStartAgeMonths() > highest) {
                highest = bean.getStartAgeMonths();
                toRtn = bean;
            }
        }
        if(highest == Integer.MIN_VALUE) {
            throw new IllegalValueException("ageMonths [" + ageMonths + "] is too low for any AgeStartBean objects");
        }
        return(toRtn);
    }

}
