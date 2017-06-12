/*
 * @(#)RoomConfigManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code RoomConfigManager} Class adds a few methods to the ObjectSetStore
 *
 * @author Darryl Oatridge
 * @version 1.00 14-Aug-2010
 */
public class RoomConfigManager extends ObjectSetStore<RoomConfigBean> {

    public static final int NO_ROOM_ID = ObjectEnum.DEFAULT_ID.value();
    protected static final String NO_ROOM_NAME = "room.none";


    /**
     * Constructs a {@code RoomConfigManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public RoomConfigManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        super.resetDefaultObject(new RoomConfigBean(NO_ROOM_ID, -1, NO_ROOM_NAME, 0, 0, 0, ObjectBean.SYSTEM_OWNED));
    }

    /**
     * returns the total of all the rooms capacities added together
     *
     * @return total room capacity
     * @throws PersistenceException
     */
    public int getTotalRoomCapacity() throws PersistenceException {
        int total = 0;
        for(RoomConfigBean b : getAllObjects()) {
            total += b.getCapacity();
        }
        return(total);
    }

    /**
     * Get the room that is associated with an ageRange, if more than one, pick largest capacity
     *
     * @param ageRangeId
     * @return room Id
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public RoomConfigBean getRoomForAgeGroup(int ageRangeId) throws NoSuchIdentifierException, PersistenceException {
        int capacity = 0;
        RoomConfigBean room = null;
        for(RoomConfigBean b : getAllObjects()) {
            if(b.getAgeRangeId() == ageRangeId) {
                if(b.getCapacity() > capacity) {
                    capacity = b.getCapacity();
                    room = b;
                }
            }
        }
        if(room != null) {
            return(room);
        }
        throw new NoSuchIdentifierException("The ageRangeId '" + ageRangeId + "' does not exist in the ageGroupManager");
    }
}
