/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.profile;

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
                com.oathouse.ccm.cos.profile.AccountManagerTest.class,
                com.oathouse.ccm.cos.profile.ChildManagerTest.class,
                com.oathouse.ccm.cos.profile.ContactManagerTest.class,
                com.oathouse.ccm.cos.profile.MedicalManagerTest.class,
                com.oathouse.ccm.cos.profile.RelationTypeTest.class,
                com.oathouse.ccm.cos.profile.RelationManagerTest.class,
                com.oathouse.ccm.cos.profile.StaffManagerTest.class
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
