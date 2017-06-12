/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.config;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
                com.oathouse.ccm.cos.config.DayRangeManagerTest.class,
                com.oathouse.ccm.cos.config.ChildRoomStartManagerTest.class,
                com.oathouse.ccm.cos.config.RoomConfigManagerTest.class,
                com.oathouse.ccm.cos.config.TimetableManagerTest.class
})
public class A_Suite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
