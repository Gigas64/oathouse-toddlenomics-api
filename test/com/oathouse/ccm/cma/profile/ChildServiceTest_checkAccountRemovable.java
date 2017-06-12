/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

// common imports
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.accounts.TransactionService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
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
public class ChildServiceTest_checkAccountRemovable {

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
    public void checkAccountRemovable_TransactionsOutstanding() throws Exception {
        final int accountId = 11;
        new Expectations() {
            @Cascading private TransactionService transactionService;
            {
                TransactionService.getInstance(); returns(transactionService);
                transactionService.isAccountTransactionsRemovable(accountId); result = false;
            }
        };
        try {
            service.checkAccountRemovable(accountId);
            fail("Should throw IllegalActionException with message 'TransactionsOutstanding'");
        } catch(IllegalActionException iae) {
            assertThat(iae.getMessage(), is("TransactionsOutstanding"));
        }
    }

    /*
     *
     */
    @Test
    public void checkAccountRemovable_TransactionsNotOutstanding() throws Exception {
        final int accountId = 11;
        new Expectations() {
            @Cascading private TransactionService transactionService;
            {
                TransactionService.getInstance(); returns(transactionService);
                transactionService.isAccountTransactionsRemovable(accountId); result = true;
            }
        };
        service.checkAccountRemovable(accountId);
    }

 }