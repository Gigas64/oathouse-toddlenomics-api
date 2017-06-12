/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cos.profile;

import java.util.Set;
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
public class RelationTypeTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCustodianTypes method, of class RelationType.
     */
    @Test
    public void testGetTypes() {
       for(RelationType t : RelationType.getCustodianTypes()) {
           System.out.println(t.toString());
       }
        for(RelationType t : RelationType.getProfessionalTypes()) {
           System.out.println(t.toString());
       }
        for(RelationType t : RelationType.getCustomerTypes()) {
           System.out.println(t.toString());
       }
    }

    @Test
    public void testIsMethods() throws Exception {
        RelationType test1 = RelationType.FAMILY_EMPLOYEE;
        RelationType test2 = RelationType.DENTIST;
        RelationType test3 = RelationType.INVOICE_FULL;
        RelationType test4 = RelationType.LIABLE;
        RelationType test5 = RelationType.LIABLE_INVOICE_FULL;
        RelationType test6 = RelationType.LIABLE_INVOICE_EMAIL_FULL;
        RelationType test7 = RelationType.INVOICE_EMAIL_SUM;

        assertTrue(test1.isCustodian());
        assertFalse(test1.isProfessional());
        assertFalse(test1.isAccounts());
        assertFalse(test2.isCustodian());
        assertTrue(test2.isProfessional());
        assertFalse(test2.isAccounts());
        assertFalse(test3.isCustodian());
        assertFalse(test3.isProfessional());
        assertTrue(test3.isAccounts());

        assertFalse(test3.isLiable());
        assertTrue(test3.isInvoice());

        assertTrue(test4.isLiable());
        assertFalse(test4.isInvoice());

        assertTrue(test5.isLiable());
        assertTrue(test5.isInvoice());

        assertTrue(test6.isLiable());
        assertTrue(test6.isInvoice());

        assertFalse(test7.isLiable());
        assertTrue(test7.isInvoice());

    }

    @Test
    public void testGetType() throws Exception {
        RelationType test1 = RelationType.LIABLE_INVOICE_EMAIL;
        RelationType test2 = RelationType.INVOICE_EMAIL_SUM;
        RelationType test3 = RelationType.INVOICE_SUM;

        assertTrue(test1.isType("LIABLE"));
        assertTrue(test1.isType("INVOICE"));
        assertTrue(test1.isType("INVOICE_EMAIL"));
        assertFalse(test1.isType("INVOICE_SUM"));
    }

}