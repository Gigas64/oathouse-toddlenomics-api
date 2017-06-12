/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cos.accounts.transaction.PaymentBean;
import com.oathouse.ccm.cos.accounts.transaction.PaymentType;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.io.File;
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
public class TransactionServiceTest_setPayment {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String sep = File.separator;
        String rootStorePath = "." + sep + "oss" + sep + "data";
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(rootStorePath + sep + "conf" + sep + "oss_log4j.properties");
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = TransactionService.getInstance();
        service.clear();

    }

    /*
     * test a normal standard payment amount
     */
    @Test
    public void setPayment_standardPayment() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int accountId = 11;
        long value = 10000L;
        service.setPayment(-1, ywd, accountId, value, PaymentType.CASH, "", "", "", owner);
        long balance = service.getCustomerCreditManager().getBalance(accountId);
        assertThat(balance, is(value));
        assertThat(service.getPaymentManager().getAllObjects(accountId).size(), is(1));
    }

    /*
     *
     */
    @Test
    public void voidPayment() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int accountId = 11;
        long value = 10000L;
        PaymentBean payment = service.setPayment(-1, ywd, accountId, value, PaymentType.CASH, "", "", "", owner);
        // now void the payment
        value = 7500L;
        service.setPayment(payment.getPaymentId(), -1, accountId, value, PaymentType.CASH, "", "", "", owner);
        long balance = service.getCustomerCreditManager().getBalance(accountId);
        assertThat(balance, is(value));
        assertThat(service.getPaymentManager().getAllObjects(accountId).size(), is(2));
    }
}
