/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.config;

import com.oathouse.oss.storage.exceptions.PersistenceException;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl
 */
public class ChildRoomStartManagerTest {
    String rootStorePath;
    ChildRoomStartManager manager;

    @Before
    public void setUp() throws Exception {
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
        manager = new ChildRoomStartManager("ChildRoomStartManager");
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        manager.clear();
    }

    /**
     * Test of getAllObjects method, of class ChildRoomStartManager.
     */
    @Test
    public void testGetAllObjects() throws Exception {
        manager.setObject(1, new ChildRoomStartBean(1, 2010011, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(2, 2010143, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(3, 2009126, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(4, 2010142, "Tester"));
        manager.setObject(2, new ChildRoomStartBean(5, 2009126, "Tester"));
        manager.setObject(2, new ChildRoomStartBean(4, 2010142, "Tester"));
        assertEquals(4,manager.getAllObjects(1).size());
        assertEquals(2,manager.getAllObjects(2).size());
        assertEquals(3,manager.getAllObjects(1).get(0).getRoomId());
        assertEquals(1,manager.getAllObjects(1).get(1).getRoomId());
        assertEquals(4,manager.getAllObjects(1).get(2).getRoomId());
        assertEquals(2,manager.getAllObjects(1).get(3).getRoomId());

    }

    @Test
    public void testGetRoomIdForChildYwd() throws Exception  {
        manager.setObject(1, new ChildRoomStartBean(1, 10, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(2, 20, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(3, 30, "Tester"));
        manager.setObject(1, new ChildRoomStartBean(4, 40, "Tester"));
        manager.setObject(2, new ChildRoomStartBean(2, 30, "Tester"));
        manager.setObject(2, new ChildRoomStartBean(9, 40, "Tester"));
        assertEquals(-1, manager.getRoomIdForChildYwd(1, 9));
        assertEquals(1, manager.getRoomIdForChildYwd(1, 10));
        assertEquals(1, manager.getRoomIdForChildYwd(1, 19));
        assertEquals(2, manager.getRoomIdForChildYwd(1, 20));
        assertEquals(2, manager.getRoomIdForChildYwd(1, 29));
        assertEquals(3, manager.getRoomIdForChildYwd(1, 30));
        assertEquals(3, manager.getRoomIdForChildYwd(1, 39));
        assertEquals(4, manager.getRoomIdForChildYwd(1, 40));
        assertEquals(4, manager.getRoomIdForChildYwd(1, 49));
        assertEquals(4, manager.getRoomIdForChildYwd(1, 1000));
    }

}