/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

// common imports
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.AccountStatus;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ChildServiceTest_createChild {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String rootStorePath = Paths.get("./oss/data").toString();
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(Paths.get(rootStorePath + "/conf/oss_log4j.properties").toString());
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = ChildService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void createChild_setsCustodians() throws Exception {
        // create the account
        int accountId = 11;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(accountId));
        fieldSet.put("status", AccountStatus.ACTIVE.toString());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), fieldSet);
        ChildService.getInstance().getAccountManager().setObject(account);

        // create a legal guardian
        

    }


}