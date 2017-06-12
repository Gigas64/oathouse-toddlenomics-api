/*
 * @(#)RoomStartDateManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code RoomStartDateManager} Class extends the methods of the parent class.
 * The manager is used to manage the room start date for each child
 * The Map Store construct is:
 * <blockquote>
 * <pre>
 *      Key - (childId)
 *      Value - (roomId)
 *      Bean - (startDate)
 * </pre>
 * </blockquote>
 *
 * @author Darryl Oatridge
 * @version 1.00 18-Sep-2010
 */
public class ChildRoomStartManager extends ObjectMapStore<ChildRoomStartBean> {

    /**
     * Constructs a {@code RoomStartDateManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public ChildRoomStartManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * gets a list of all the ChildRoomStartBean objects sorted in order
     * of startYwd
     *
     * @param childId
     * @param options
     * @return a list of ChildRoomStartBean objects
     * @throws PersistenceException 
     */
    @Override
    public List<ChildRoomStartBean> getAllObjects(int childId, ObjectDataOptionsEnum... options) throws PersistenceException {
        List<ChildRoomStartBean> rtnList =  super.getAllObjects(childId, options);
        Collections.sort(rtnList);
        return(rtnList);
    }

    /**
     * Gets the room a child should be in based on a current YWDHolder key value. Notice that if the
     * date is before the first startYwd -1 is returned.
     *
     * @param childId
     * @param currentYwd
     * @return roomId
     * @throws PersistenceException
     */
    public int getRoomIdForChildYwd(int childId, int currentYwd) throws PersistenceException {
        int prevRoom = -1;
        LinkedList<ChildRoomStartBean> checkList =  (LinkedList<ChildRoomStartBean>) getAllObjects(childId);
        Collections.sort(checkList);
        if(checkList.isEmpty()) {
            return(-1);
        }
        for(ChildRoomStartBean bean : checkList){
            if(bean.getStartYwd() > currentYwd) {
                return(prevRoom);
            }
            prevRoom = bean.getRoomId();
        }
        return(checkList.getLast().getRoomId());
    }

    /**
     * This helper method allows the retrieval of the first startYwd
     *
     * @param childId
     * @return the startYwd as a YWDHolder key value
     * @throws PersistenceException
     */
    public int getChildFirstStartYwd(int childId) throws PersistenceException {
        LinkedList<ChildRoomStartBean> checkList = (LinkedList<ChildRoomStartBean>) getAllObjects(childId);
        return(checkList.getFirst().getStartYwd());
    }

}
