/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceType;
import com.oathouse.ccm.cos.accounts.transaction.CustomerCreditBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean;
import com.oathouse.ccm.cos.accounts.transaction.PaymentType;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
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
public class TransactionServiceTest_setCustomerReceipt {
    private final String owner = ObjectBean.SYSTEM_OWNED;
    private TransactionService service;

    // common test constants
    private final int ywd = CalendarStatic.getRelativeYW(0);
    private final int accountId = 17;
    private final String ref = "ref";
    private final String feeDescription = ApplicationConstants.RECEIPT_CARD_FEE;
    private final String notes = "notes";

    // common mock
    @Cascading private BillingService billingServiceMock;
    @Cascading private PropertiesService propertiesServiceMock;

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
     *
     */
    @Test
    public void setCustomerReceipt_creditCard() throws Exception {
        final long value = 1000L;
        final PaymentType type = PaymentType.CREDITCARD;
        final long feeExTax = 0L;
        // internal
        final List<BillingBean> billingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("taxRate", Integer.toString(0));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));

        new Expectations() {
           {
                BillingService.getInstance(); returns(billingServiceMock);
                PropertiesService.getInstance(); returns(propertiesServiceMock);

                propertiesServiceMock.getSystemProperties().getCreditCardFeeRate(); result = 0;
                propertiesServiceMock.getSystemProperties().getTaxRate(); result = 0;
                billingServiceMock.setFixedItem(-1, ywd, accountId, -1, feeExTax, BillingEnum.BILL_CHARGE, feeDescription, notes, owner, BillingEnum.APPLY_NO_DISCOUNT); result = billingList;

                // createInvoiceForBillings method
                BillingService.getInstance(); returns(billingServiceMock);
                billingServiceMock.getBillingManager().isIdentifier(accountId, anyInt); result = true;
                billingServiceMock.setBillingInvoiceId(accountId, billingList.get(0).getBillingId(), anyInt, owner);
            }
        };
        CustomerReceiptBean result = service.setCustomerReceipt(-1, ywd, accountId, value, type, ref, notes, owner);
        assertThat(result.getPaymentType(), is(equalTo(PaymentType.CREDITCARD)));
        // check the values have been added correctly
        List<CustomerCreditBean> creditList = service.getCustomerCreditManager().getAllObjects();
        List<CustomerReceiptBean> receiptList = service.getCustomerReceiptManager().getAllObjects();
        assertThat(creditList.size(), is(2));
        assertThat(creditList.get(0).getCredit(), is(feeExTax));
        assertThat(creditList.get(1).getCredit(), is(value - feeExTax));
        assertThat(receiptList.size(), is(1));
        assertThat(receiptList.get(0).getValue(), is(value));
        // now check the invoice has been created
        List<InvoiceBean> invoiceList = service.getInvoiceManager().getAllObjects(accountId);
        assertThat(invoiceList.size(), is(1));
    }

    /*
     *
     */
    @Test
    public void setCustomerReceipt_notCreditCard() throws Exception {
        final long value = 1000L;
        final PaymentType type = PaymentType.CHEQUE;

        CustomerReceiptBean result = service.setCustomerReceipt(ywd, accountId, value, type, ref, notes, owner);
        assertThat(result.getPaymentType(), is(equalTo(PaymentType.CHEQUE)));
        // check the values have been added correctly
        List<CustomerCreditBean> creditList = service.getCustomerCreditManager().getAllObjects();
        List<CustomerReceiptBean> receiptList = service.getCustomerReceiptManager().getAllObjects();
        assertThat(creditList.size(), is(1));
        assertThat(creditList.get(0).getCredit(), is(value));
        assertThat(receiptList.size(), is(1));
        assertThat(receiptList.get(0).getValue(), is(value));
    }

    /*
     *
     */
    @Test
    public void setCustomerReceipt_cancelExisting() throws Exception {
        // add the receipt

        final long value = 1000L;
        final PaymentType type = PaymentType.CHEQUE;
        CustomerReceiptBean result = service.setCustomerReceipt(ywd, accountId, value, type, ref, notes, owner);
        assertThat(result.getPaymentType(), is(equalTo(PaymentType.CHEQUE)));
        // check the values have been added correctly
        List<CustomerReceiptBean> receiptList = service.getCustomerReceiptManager().getAllObjects(accountId);
        List<CustomerReceiptBean> voidList = service.getCustomerReceiptManager().getAllVoidCustomerReceipts(accountId);
        List<CustomerCreditBean> creditList = service.getCustomerCreditManager().getAllObjects(accountId);
        assertThat(receiptList.size(), is(1));
        assertThat(receiptList.get(0).getValue(), is(value));
        assertThat(voidList.isEmpty(), is(true));
        assertThat(creditList.size(), is(1));
        assertThat(creditList.get(0).getCredit(), is(value));
        service.adminVoidCustomerReceipt(result.getCustomerReceiptId(), ywd, accountId, 0, type, ref, notes, owner);
        // check the values have removed correctly
        receiptList = service.getCustomerReceiptManager().getAllObjects(accountId);
        voidList = service.getCustomerReceiptManager().getAllVoidCustomerReceipts(accountId);
        creditList = service.getCustomerCreditManager().getAllObjects(accountId);
        assertThat(receiptList.isEmpty(), is(true));
        assertThat(voidList.size(), is(1));
        assertThat(voidList.get(0).getValue(), is(value));
        assertThat(voidList.get(0).getPaymentType(), is(PaymentType.ADMIN_VOID));
        assertThat(creditList.isEmpty(), is(true));
    }

    /*
     *
     */
    @Test
    public void setCustomerReceipt_editExistingReciept() throws Exception {
        final long value = 1000L;
        final PaymentType type = PaymentType.CHEQUE;
        final long feeExTax = 0L;
        CustomerReceiptBean result = service.setCustomerReceipt(ywd, accountId, value, type, ref, notes, owner);
        assertThat(result.getPaymentType(), is(equalTo(PaymentType.CHEQUE)));
        // check the values have been added correctly
        List<CustomerCreditBean> creditList = service.getCustomerCreditManager().getAllObjects();
        List<CustomerReceiptBean> receiptList = service.getCustomerReceiptManager().getAllObjects();
        assertThat(creditList.size(), is(1));
        assertThat(creditList.get(0).getCredit(), is(value));
        assertThat(receiptList.size(), is(1));
        assertThat(receiptList.get(0).getValue(), is(value));
    }

}
