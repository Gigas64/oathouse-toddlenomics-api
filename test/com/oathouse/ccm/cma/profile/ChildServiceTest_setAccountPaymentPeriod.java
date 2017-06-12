/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.profile;

// common imports
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesManager;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetBean;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectSingleStore;
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
public class ChildServiceTest_setAccountPaymentPeriod {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildService service;

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
        service = ChildService.getInstance();
    }

    /*
     * run through to ensure the method works
     */
    @Test
    public void runThrough() throws Exception {
        int accountId = 13;
        PaymentPeriodEnum paymentPeriod = PaymentPeriodEnum.MONTHLY;
        String regularPaymentInstruction = "ABC";
        long paymentPeriodValue = 457L;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(accountId));
        fieldSet.put("paymentPeriod", PaymentPeriodEnum.AS_INVOICE.toString());
        fieldSet.put("regularPaymentInstruction", "");
        fieldSet.put("paymentPeriodValue", Long.toString(0L));
        AccountBean control = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), fieldSet);

        service.getAccountManager().setObject(control);
        AccountBean result = service.getAccountManager().getObject(accountId);
        //control test
        assertThat(result, is(control));
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.AS_INVOICE));
        assertThat(result.getPaymentPeriodValue(), is(0L));

        // method test
        service.setAccountPaymentPeriod(accountId, paymentPeriod, regularPaymentInstruction, paymentPeriodValue, owner);
        result = service.getAccountManager().getObject(accountId);
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.MONTHLY));
        assertThat(result.getPaymentPeriodValue(), is(paymentPeriodValue));
        assertEquals(regularPaymentInstruction, result.getRegularPaymentInstruction());
    }

    /*
     * run through to ensure the method works
     */
    @Test
    public void setAsInvoiceButWithPositiveValue() throws Exception {
        int accountId = 13;
        PaymentPeriodEnum paymentPeriod = PaymentPeriodEnum.AS_INVOICE;
        String regularPaymentInstruction = "ABC";
        long paymentPeriodValue = 457L;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(accountId));
        fieldSet.put("paymentPeriod", PaymentPeriodEnum.MONTHLY.toString());
        fieldSet.put("regularPaymentInstruction", "DEF");
        fieldSet.put("paymentPeriodValue", Long.toString(paymentPeriodValue));
        AccountBean control = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), fieldSet);

        service.getAccountManager().setObject(control);
        AccountBean result = service.getAccountManager().getObject(accountId);
        // control test
        assertThat(result, is(control));
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.MONTHLY));
        assertThat(result.getPaymentPeriodValue(), is(paymentPeriodValue));

        // method test should change the value to zero
        service.setAccountPaymentPeriod(accountId, paymentPeriod, regularPaymentInstruction, paymentPeriodValue, owner);
        result = service.getAccountManager().getObject(accountId);
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.AS_INVOICE));
        assertThat(result.getPaymentPeriodValue(), is(-1L));
        assertEquals("", result.getRegularPaymentInstruction());
    }

        /*
     * run through to ensure the method works
     */
    @Test
    public void setValueAsNegativeNumber() throws Exception {
        int accountId = 13;
        PaymentPeriodEnum paymentPeriod = PaymentPeriodEnum.AS_INVOICE;
        String regularPaymentInstruction = "ABC";
        long paymentPeriodValue = 457L;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(accountId));
        fieldSet.put("paymentPeriod", PaymentPeriodEnum.MONTHLY.toString());
        fieldSet.put("regularPaymentInstruction", "DEF");
        fieldSet.put("paymentPeriodValue", Long.toString(paymentPeriodValue));
        AccountBean control = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), fieldSet);

        service.getAccountManager().setObject(control);
        AccountBean result = service.getAccountManager().getObject(accountId);
        // control test
        assertThat(result, is(control));
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.MONTHLY));
        assertThat(result.getPaymentPeriodValue(), is(paymentPeriodValue));

        // method test should change the value to zero
        service.setAccountPaymentPeriod(accountId, paymentPeriod, regularPaymentInstruction, -1, owner);
        result = service.getAccountManager().getObject(accountId);
        assertThat(result.getPaymentPeriod(), is(PaymentPeriodEnum.AS_INVOICE));
        assertThat(result.getPaymentPeriodValue(), is(-1L));
        assertEquals("", result.getRegularPaymentInstruction());
    }

}
