/*
 * @(#)AgeRangeManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.education;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code ChildEducationTimetableManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 19-Nov-2010
 */
public class ChildEducationTimetableManager extends ObjectSetStore<ChildEducationTimetableBean> {

    public static final int ALL_ROOMS = -2;
    public static final int NO_EDUCATION = 0;
    protected static final String NO_EDUCATION_LABEL = "#cccccc";
    protected static final String NO_EDUCATION_NAME = "education.none";

    /**
     * Constructs a {@code ChildEducationTimetableManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public ChildEducationTimetableManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        ChildEducationTimetableBean cetb = new ChildEducationTimetableBean(NO_EDUCATION, NO_EDUCATION_NAME, NO_EDUCATION_LABEL, ALL_ROOMS,
                new ConcurrentSkipListSet<Integer>(), "system.default");
        super.resetDefaultObject(cetb);
    }

    /**
     * Returns an AgeRangeBean with the given name. This allows an AgeRangeBean to be
     * reference by name as well as by id
     *
     * @param name the name of the AgeRangeBean
     * @return The AgeRangeBean with the given name
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public ChildEducationTimetableBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(ChildEducationTimetableBean bean : getAllObjects()) {
            if(bean.getName().equalsIgnoreCase(name)) {
                return (bean);
            }
        }
        throw new NoSuchIdentifierException("A ChildEducationTimetableBean with name '" + name + "' does not exist");
    }

    @Override
    public ChildEducationTimetableBean setObject(ChildEducationTimetableBean ob) throws PersistenceException, NullObjectException {
        if(ob.getCetId() != NO_EDUCATION) {
            return super.setObject(ob);
        }
        throw new PersistenceException("The child education timetable to be saved is using the same id as the default");
    }

    @Override
    public ChildEducationTimetableBean removeObject(int cetId) throws PersistenceException {
        if(cetId != NO_EDUCATION) {
            return super.removeObject(cetId);
        }
        return null;
    }
}
