/*
 * @(#)FileManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.file;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code FileManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 7-Apr-2011
 */
public class FileManager extends ObjectSetStore<FileBean> {

    /**
     * Constructs a {@code FileManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public FileManager(String managerName) {
        super(managerName);
    }

    /**
     * Finds a file bean given its name
     * @param name
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public FileBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(FileBean file : getAllObjects()) {
            if(file.getName().equals(name)) {
                return file;
            }
        }
        throw new NoSuchIdentifierException("The file with name [" + name + "] does not exist");
    }

}
