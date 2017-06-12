/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean;
import com.oathouse.ccm.cos.accounts.transaction.PaymentType;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
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
public class TransactionServiceTest_removeAccountTransactions {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;

    // method parameters
    private final int accountId = Builders.accountId;
    private final int childId = Builders.childId;
    private final int ywd = CalendarStatic.getRelativeYW(0);

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
        service = TransactionService.getInstance();
        service.clear();
        // clear the billing service
        BillingService.getInstance().clear();
        // clear out the other services
        ChildService.getInstance().clear();
        // use the static test method in BillingServiceTest to create an account and child
        Builders.setChild();
    }

    /*
     *
     */
    @Test
    public void removeAccountTransactions_nothingToDelete() throws Exception {
        service.removeAccountTransactions(accountId);
    }

    /*
     *
     */
    @Test
    public void removeAccountTransactions_balanceNotZero() throws Exception {
        service.setCustomerReceipt(-1, ywd, accountId, 5000, PaymentType.CASH, "Pay 5000", "", owner);
        try {
            service.removeAccountTransactions(accountId);
            fail("Should throw exception as balance exists");
        } catch(IllegalActionException iae) {
            assertThat(iae.getMessage(), is("AccountBalanceNotZero"));
        }
    }

    /*
     *
     */
    @Test
    public void removeAccountTransactions_billingOutstanding() throws Exception {
        // set up a billing not invoiced
        final SystemPropertiesBean properties = Builders.getProperties();
        new NonStrictExpectations() {
            @Cascading private PropertiesService propertiesServiceMock;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);
                propertiesServiceMock.getSystemProperties(); result = properties;
            }
        };
        BillingService.getInstance().setFixedItem(-1, ywd, accountId, -1, 1000, BillingEnum.BILL_CREDIT, "Some Fixed Item", "", owner);
        try {
            service.removeAccountTransactions(accountId);
            fail("Should throw exception as outstanding billing exists");
        } catch(IllegalActionException iae) {
            assertThat(iae.getMessage(), is("OutstandingBillingOnAccount"));
        }
    }

    /*
     *
     */
    @Test
    public void removeAccountTransactions_billingInvoiced() throws Exception {
        BillingService billingService = BillingService.getInstance();
        // set up a billing not invoiced
        final SystemPropertiesBean properties = Builders.getProperties();
        new NonStrictExpectations() {
            @Cascading private PropertiesService propertiesServiceMock;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);
                propertiesServiceMock.getSystemProperties(); result = properties;
            }
        };
        BillingBean billing = billingService.setFixedItem(-1, ywd, accountId, -1, 1000, BillingEnum.BILL_CREDIT, "Some Fixed Item", "", owner);
        billingService.setBillingInvoiceId(accountId, billing.getBillingId(), 1, owner);
        service.removeAccountTransactions(accountId);
    }


}