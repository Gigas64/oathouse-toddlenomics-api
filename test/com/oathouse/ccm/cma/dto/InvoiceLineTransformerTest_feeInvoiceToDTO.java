package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.accounts.TransactionService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceManager;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceType;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.AccountManager;
import com.oathouse.ccm.cos.profile.ChildManager;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
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
public class InvoiceLineTransformerTest_feeInvoiceToDTO {

    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void runThrough() throws Exception {
        // method parameters
        final int accountId = 13;
        final int invoiceId = 17;

        // internal
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(invoiceId));
        fieldSet.put("accountId", Integer.toString(accountId));
        fieldSet.put("invoiceType", InvoiceType.RECEIPT_FEE.toString());
        final InvoiceBean invoice = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), fieldSet);
        final List<BillingBean> billingList = new LinkedList<>();
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean()));
        new Expectations() {
            @Mocked private TransactionService transactionServiceMock;
            @Mocked private InvoiceManager invoiceManagerMock;
            @Mocked private ChildService childServiceMock;
            @Mocked private AccountManager childManagerMock;
            @Mocked private BillingService billingServiceMock;
            {
                TransactionService.getInstance(); returns(transactionServiceMock);
                ChildService.getInstance(); returns(childServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                transactionServiceMock.getInvoiceManager(); returns(invoiceManagerMock);
                invoiceManagerMock.getObject(invoiceId); returns(invoice);

                childServiceMock.getAccountManager(); returns (childManagerMock);
                childManagerMock.getObject(invoice.getAccountId()); returns(account);

                billingServiceMock.getBillingsForInvoice(accountId, invoiceId); returns(billingList);
            }
        };
        InvoiceLineTransformer.feeInvoiceToDTO(invoiceId);
    }

    /*
     *
     */
    @Test(expected=IllegalActionException.class)
    public void notFeeInvoice() throws Exception {
        // method parameters
        final int accountId = 13;
        final int invoiceId = 17;

        // internal
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(invoiceId));
        fieldSet.put("invoiceType", InvoiceType.STANDARD.toString());
        final InvoiceBean invoice = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), fieldSet);
        new Expectations() {
            @Mocked private TransactionService transactionServiceMock;
            @Mocked private InvoiceManager invoiceManagerMock;
            {
                TransactionService.getInstance(); returns(transactionServiceMock);

                transactionServiceMock.getInvoiceManager(); returns(invoiceManagerMock);
                invoiceManagerMock.getObject(invoiceId); returns(invoice);
            }
        };
        InvoiceLineTransformer.feeInvoiceToDTO(invoiceId);
    }

    /*
     *
     */
    @Test(expected=IllegalActionException.class)
    public void noBilling() throws Exception {
        // method parameters
        final int accountId = 13;
        final int invoiceId = 17;

        // internal
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(invoiceId));
        fieldSet.put("accountId", Integer.toString(accountId));
        fieldSet.put("invoiceType", InvoiceType.RECEIPT_FEE.toString());
        final InvoiceBean invoice = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), fieldSet);
        // empty billing
        final List<BillingBean> billingList = new LinkedList<>();
        new Expectations() {
            @Mocked private TransactionService transactionServiceMock;
            @Mocked private InvoiceManager invoiceManagerMock;
            @Mocked private ChildService childServiceMock;
            @Mocked private AccountManager childManagerMock;
            @Mocked private BillingService billingServiceMock;
            {
                TransactionService.getInstance(); returns(transactionServiceMock);
                ChildService.getInstance(); returns(childServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                transactionServiceMock.getInvoiceManager(); returns(invoiceManagerMock);
                invoiceManagerMock.getObject(invoiceId); returns(invoice);

                childServiceMock.getAccountManager(); returns (childManagerMock);
                childManagerMock.getObject(invoice.getAccountId()); returns(account);

                billingServiceMock.getBillingsForInvoice(accountId, invoiceId); returns(billingList);
            }
        };
        InvoiceLineTransformer.feeInvoiceToDTO(invoiceId);
    }

    /*
     *
     */
    @Test(expected=IllegalActionException.class)
    public void toManyBilling() throws Exception {
        // method parameters
        final int accountId = 13;
        final int invoiceId = 17;

        // internal
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(invoiceId));
        fieldSet.put("accountId", Integer.toString(accountId));
        fieldSet.put("invoiceType", InvoiceType.RECEIPT_FEE.toString());
        final InvoiceBean invoice = (InvoiceBean) BeanBuilder.addBeanValues(new InvoiceBean(), fieldSet);
        // empty billing
        final List<BillingBean> billingList = new LinkedList<>();
        new Expectations() {
            @Mocked private TransactionService transactionServiceMock;
            @Mocked private InvoiceManager invoiceManagerMock;
            @Mocked private ChildService childServiceMock;
            @Mocked private AccountManager childManagerMock;
            @Mocked private BillingService billingServiceMock;
            {
                TransactionService.getInstance(); returns(transactionServiceMock);
                ChildService.getInstance(); returns(childServiceMock);
                BillingService.getInstance(); returns(billingServiceMock);

                transactionServiceMock.getInvoiceManager(); returns(invoiceManagerMock);
                invoiceManagerMock.getObject(invoiceId); returns(invoice);

                childServiceMock.getAccountManager(); returns (childManagerMock);
                childManagerMock.getObject(invoice.getAccountId()); returns(account);

                billingServiceMock.getBillingsForInvoice(accountId, invoiceId); returns(billingList);
            }
        };
        InvoiceLineTransformer.feeInvoiceToDTO(invoiceId);
    }

}
