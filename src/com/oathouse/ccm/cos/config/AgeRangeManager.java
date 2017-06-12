/*
 * @(#)AgeRangeManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code AgeRangeManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 15-Aug-2010
 */
public class AgeRangeManager extends ObjectSetStore<AgeRangeBean> {

    /**
     * Constructs a {@code AgeRangeManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public AgeRangeManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Returns an AgeRangeBean with the given name. This allows an AgeRangeBean to be
     * reference by name as well as by id
     *
     * @param name the name of the AgeRangeBean
     * @return The AgeRangeBean with the given name
     * @throws NoSuchIdentifierException
     */
    public AgeRangeBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(AgeRangeBean bean : getAllObjects()) {
            if(bean.getName().equalsIgnoreCase(name)) {
                return (bean);
            }
        }
        throw new NoSuchIdentifierException("An Age Range Object with name '" + name + "' does not exist");
    }

    /**
     * Returns the youngest YWDHolder key value from the DOB that a child can start
     *
     * @param dob date of birth
     * @return the first start date
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public int getObjectStartDate(int dob) throws NullObjectException, PersistenceException {
        int lowestDate = Integer.MAX_VALUE;
        for(AgeRangeBean bean : getAllObjects()) {
            if(bean.getAgeRangeYW(dob) < lowestDate) {
                lowestDate = bean.getAgeRangeYW(dob);
            }
        }
        if(lowestDate == Integer.MAX_VALUE) {
            throw new NullObjectException("The were no AgeRangeBean objects found");
        }
        return(lowestDate);
    }
}
