/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingManagerTest_rollback {

    private String owner = ObjectBean.SYSTEM_OWNED;
    private String rootStorePath;
    private BillingManager manager;

    public BillingManagerTest_rollback() {
        String sep = File.separator;
        rootStorePath = "." + sep + "oss" + sep + "data";
    }

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "");
        // reset
        ObjectDBMS.clearAuthority(authority);
        manager = null;
        // create new manager
        manager = new BillingManager("BillingManager");
        manager.init();
        manager.clear();
    }

    /*
     *
     */
    @Test
    public void rollback_billingIdSet_Single() throws Exception {
        int accountId = 0;
        List<BillingBean> BillingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("invoiceId", Integer.toString(-1));
        BillingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        manager.setObject(accountId, BillingList.get(0));
        assertThat(manager.isIdentifier(accountId, BillingList.get(0).getBillingId()), is(true));
        Set<Integer> result = manager.rollback(accountId, BillingList);
        assertThat(result.isEmpty(), is(true));
        assertThat(manager.isIdentifier(accountId, BillingList.get(0).getBillingId()), is(false));
    }

    /*
     *
     */
    @Test
    public void rollback_billingIdSet_MultipleWithFailures() throws Exception {
        int accountId = 0;
        List<BillingBean> BillingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("invoiceId", Integer.toString(-1));
        BillingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        BillingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("invoiceId", Integer.toString(10));
        BillingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        manager.setObject(accountId, BillingList.get(0));
        manager.setObject(accountId, BillingList.get(1));
        manager.setObject(accountId, BillingList.get(2));
        assertThat(manager.getAllIdentifier(accountId).size(), is(3));
        Set<Integer> result = manager.rollback(accountId, BillingList);
        assertThat(result.size(), is(1));
        assertThat(manager.isIdentifier(accountId, BillingList.get(0).getBillingId()), is(false));
        assertThat(manager.isIdentifier(accountId, BillingList.get(1).getBillingId()), is(false));
        assertThat(manager.isIdentifier(accountId, BillingList.get(2).getBillingId()), is(true));
    }

 }
