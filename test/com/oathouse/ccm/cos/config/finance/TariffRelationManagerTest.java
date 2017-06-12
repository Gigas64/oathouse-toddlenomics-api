/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.config.finance;

import static com.oathouse.ccm.cos.config.finance.MultiRefEnum.*;
import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import static com.oathouse.oss.storage.valueholder.MRHolder.*;
import java.io.File;
import java.util.Set;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class TariffRelationManagerTest {

    private String owner;
    private String rootStorePath;
    private TariffRelationManager manager;
    private int priceTariffId01 = 1;
    private int priceTariffId02 = 2;
    private int loyaltyTariffId = 3;

    public TariffRelationManagerTest() {
        owner = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String authority = owner;
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new TariffRelationManager("TariffRelationManager");
        manager.init();
        manager.clear();
    }

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_TariffRelation() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.finance." + VT.PRICE_TARIFF_RELATION.bean(), false);
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getAllPriceConfigMr() throws Exception {
        Set<Integer> testSet;
        setRelationships();

        // no filter
        testSet = manager.getAllPriceConfigMr(priceTariffId01);
        assertThat(testSet.size(), is(4));

        testSet = manager.getAllPriceConfigMr(loyaltyTariffId);
        assertThat(testSet.size(), is(3));

        // one filter
        testSet = manager.getAllPriceConfigMr(priceTariffId01, PRICE_SPECIAL);
        assertThat(testSet.size(), is(1));
        assertTrue(testSet.contains(getMR(PRICE_SPECIAL.type(), 1)));

        testSet = manager.getAllPriceConfigMr(loyaltyTariffId, LOYALTY_STANDARD);
        assertThat(testSet.size(), is(2));
        assertTrue(testSet.contains(getMR(LOYALTY_STANDARD.type(), 1)));
        assertTrue(testSet.contains(getMR(LOYALTY_STANDARD.type(), 2)));

        // test two filters
        testSet = manager.getAllPriceConfigMr(priceTariffId01, PRICE_SPECIAL, ADJUSTMENT_SPECIAL);
        assertThat(testSet.size(), is(3));
        assertTrue(testSet.contains(getMR(ADJUSTMENT_SPECIAL.type(), 1)));
        assertTrue(testSet.contains(getMR(ADJUSTMENT_SPECIAL.type(), 2)));
        assertTrue(testSet.contains(getMR(PRICE_SPECIAL.type(), 1)));
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_getAllPriceTariffForPriceConfigMr() throws Exception {
        Set<Integer> testSet;
        setRelationships();

        testSet = manager.getAllPriceTariffForPriceConfigMr(getMR(PRICE_STANDARD.type(), 1));
        assertThat(testSet.size(), is(1));
        assertTrue(testSet.contains(priceTariffId01));

        testSet = manager.getAllPriceTariffForPriceConfigMr(getMR(PRICE_SPECIAL.type(), 1));
        assertThat(testSet.size(), is(2));
        assertTrue(testSet.contains(priceTariffId01));
        assertTrue(testSet.contains(priceTariffId02));
    }


    private void setRelationships() throws Exception {
        manager.setObject(priceTariffId01, new TariffRelationBean(getMR(PRICE_SPECIAL.type(), 1), owner));
        manager.setObject(priceTariffId01, new TariffRelationBean(getMR(PRICE_STANDARD.type(), 1), owner));
        manager.setObject(priceTariffId01, new TariffRelationBean(getMR(ADJUSTMENT_SPECIAL.type(), 1), owner));
        manager.setObject(priceTariffId01, new TariffRelationBean(getMR(ADJUSTMENT_SPECIAL.type(), 2), owner));
        manager.setObject(loyaltyTariffId, new TariffRelationBean(getMR(LOYALTY_STANDARD.type(), 1), owner));
        manager.setObject(loyaltyTariffId, new TariffRelationBean(getMR(LOYALTY_STANDARD.type(), 2), owner));
        manager.setObject(loyaltyTariffId, new TariffRelationBean(getMR(LOYALTY_SPECIAL.type(), 2), owner));
        manager.setObject(priceTariffId02, new TariffRelationBean(getMR(PRICE_SPECIAL.type(), 1), owner));
    }
}