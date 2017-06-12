/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.concessions;

import com.oathouse.ccm.cos.concessions.HolidayConcessionBean;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class HolidayConcessionBeanTest {
    @Before
    public void setUp() {
    }

    @Test
    public void testXMLDOM() {
        HolidayConcessionBean bean = new HolidayConcessionBean(10, 11, true, "tester");
        String xml = bean.toXML();
        HolidayConcessionBean other = new HolidayConcessionBean();
        other.setXMLDOM(bean.getXMLDOM().detachRootElement());
        assertEquals(bean, other);
        assertEquals(xml, other.toXML());
    }
}