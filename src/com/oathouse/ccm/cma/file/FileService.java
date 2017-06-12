/*
 * @(#)FileService.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.file;

import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.io.File;
import java.io.IOException;

/**
 * The {@code FileService} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 7-Apr-2011
 */
public class FileService {
    // Singleton Instance
    private volatile static FileService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise;
    // util
    private volatile FileManager imageManager;
    private volatile FileManager matchExceptionsManager;


    // private Method to avoid instanciation externally
    private FileService() {
        imageManager = new FileManager("imageManager");
        matchExceptionsManager = new FileManager("matchExceptionsManager");
        // needs to be initialised
        initialise = true;
    }

    public final static boolean hasInstance() {
        if(INSTANCE != null) {
            return(true);
        }
        return(false);
    }

    public static FileService getInstance() throws PersistenceException, NoSuchIdentifierException {
        if(INSTANCE == null) {
            synchronized (FileService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new FileService().init();
                }
            }
        }
        return INSTANCE;
    }

    public FileService reInitialise() throws PersistenceException, NoSuchIdentifierException {
        initialise = true;
        return (init());
    }

    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = imageManager.clear()?success:false;
        success = matchExceptionsManager.clear()?success:false;
        INSTANCE = null;
        return success;
    }

    public synchronized FileService init() throws PersistenceException, NoSuchIdentifierException {
        if(initialise) {
            imageManager.init();
            matchExceptionsManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Mostly used for testing to reset the instance
     */
    public static void removeInstance() {
        INSTANCE = null;
    }

    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     * ***************************************************/
    public FileManager getImageManager() {
        return imageManager;
    }

    public FileManager getMatchExceptionsManager() {
        return matchExceptionsManager;
    }

    /* ***************************************************
     * S E R V I C E   L E V E L   M E T H O D S
     * ***************************************************/

    public FileBean setImage(int fileId, String name, File file, String contentType, String suffix, String owner)
            throws IOException, NullObjectException, PersistenceException, MaxCountReachedException {
        if(fileId == ObjectEnum.INITIALISATION.value()) {
            fileId = imageManager.generateIdentifier();
        }
        return (imageManager.setObject(new FileBean(fileId, name, file, contentType, suffix, owner)));
    }

    public void removeImage(int fileId) throws PersistenceException {
        if(imageManager.isIdentifier(fileId)) {
            imageManager.removeObject(fileId);
        }
    }

    public FileBean setMatchExceptionsFile(int fileId, String name, File file, String contentType, String suffix, String owner)
            throws IOException, NullObjectException, PersistenceException, MaxCountReachedException {
        if(fileId == ObjectEnum.INITIALISATION.value()) {
            fileId = matchExceptionsManager.generateIdentifier();
        }
        return (matchExceptionsManager.setObject(new FileBean(fileId, name, file, contentType, suffix, owner)));
    }

    public void removeMatchExceptionsFile(int fileId) throws PersistenceException {
        if(matchExceptionsManager.isIdentifier(fileId)) {
            matchExceptionsManager.removeObject(fileId);
        }
    }
}
