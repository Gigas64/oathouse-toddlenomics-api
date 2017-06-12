/*
 * @(#)ChildManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.profile;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code MedicalManager} Class extends the Object manager to enable
 * specific functions
 *
 * @author Darryl Oatridge
 * @version 1.00 25-May-2010
 */
public class MedicalManager extends ObjectSetStore<MedicalBean> {

    /**
     * th constructor takes the root store path of the persistence store and
     * the name you would like to give the manager to distinguish it in persistence
     *
     * @param managerName a unique name as a sub store to the store path
     * @param dataOptions
     */
    public MedicalManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Finds the first MedicalBean with the given name, type and childId
     *
     * @param name
     * @param type
     * @param childId
     * @return a medical bean object
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public MedicalBean getObject(String name, MedicalType type, int childId) throws NoSuchIdentifierException, PersistenceException {
        for(MedicalBean m: getAllObjects()) {
            if(m.getName().equalsIgnoreCase(name) && m.getType().equals(type) && m.getChildId()==childId ) {
                return m;
            }
        }
        throw new NoSuchIdentifierException("A MedicalBean with params name '" + name + "', type '" + type +
                "', childId '" + childId + "' could not be found");
    }
}
