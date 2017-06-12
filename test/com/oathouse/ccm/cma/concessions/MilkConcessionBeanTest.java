/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.concessions;

import com.oathouse.ccm.cos.concessions.MilkConcessionBean;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class MilkConcessionBeanTest {
    private MilkConcessionBean bean;

    @Before
    public void setUp() {
        ConcurrentSkipListSet<Integer> sessions = new ConcurrentSkipListSet<Integer>();
        sessions.add(21);
        sessions.add(22);
        bean = new MilkConcessionBean(2, 10, sessions, 2.4, "pints", "tester");
    }

    /**
     * Test.
     */
    @Test
    public void test() {
    }

    @Test
    public void testXMLDOM() {
        String xml = bean.toXML();
        MilkConcessionBean other = new MilkConcessionBean();
        other.setXMLDOM(bean.getXMLDOM().detachRootElement());
        assertEquals(bean, other);
        assertEquals(xml, other.toXML());
    }
}