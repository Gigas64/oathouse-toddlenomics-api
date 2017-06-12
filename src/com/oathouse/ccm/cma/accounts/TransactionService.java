/*
 * @(#)TransactionService.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.exceptions.BusinessRuleException;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.YwdComparator;
import com.oathouse.ccm.cos.accounts.YwdOrderedTypeComparator;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceManager;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceType;
import com.oathouse.ccm.cos.accounts.transaction.CustomerCreditBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerCreditManager;
import com.oathouse.ccm.cos.accounts.transaction.CustomerCreditType;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptBean;
import com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptManager;
import com.oathouse.ccm.cos.accounts.transaction.InvoiceCreditBean;
import com.oathouse.ccm.cos.accounts.transaction.InvoiceCreditManager;
import com.oathouse.ccm.cos.accounts.transaction.PaymentBean;
import com.oathouse.ccm.cos.accounts.transaction.PaymentManager;
import com.oathouse.ccm.cos.accounts.transaction.PaymentType;
import com.oathouse.ccm.cos.accounts.transaction.TransactionBean;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.exceptions.DuplicateIdentifierException;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NoSuchKeyException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.collections.CollectionUtils;

/**
 * The {@literal TransactionService} Class is a encapsulated API, service level singleton that provides access to related
 * underlying manager instances. The class provides three distinct sections: <h2>Manager Retrieval</h2> <p> This section
 * allows the retrieval of the instance of the manager to use as part of this service. For ease of reference each
 * manager has both a long and a short name method call. ALL manager instances should be referenced through the Service
 * and not instantiated directly. </p>
 *
 * <h2>Service Level get Methods</h2> <p> This section is for all get methods that require information outside their own
 * data store. All other get methods can be found up the tree in the manager or bean of the bean type. </p>
 *
 * <h2>Service Level set Methods</h2> <p> All set methods must be implemented up to the Service level as this allows
 * consistency across the API and security for the underlying sets. If we have sets at the Service level, the Manager
 * level and ObjectStore, the inconsistency might cause a set to be made to the manager when a set in the service adds
 * additional logic and thus is bypassed. </p>
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 20, 2012
 */
public class TransactionService {

    // Singleton Instance
    private volatile static TransactionService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    // Manager declaration and instantiation
    private final InvoiceManager invoiceManager = new InvoiceManager(VT.INVOICE.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final CustomerReceiptManager customerReceiptManager = new CustomerReceiptManager(VT.CUSTOMER_RECEIPT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final CustomerCreditManager customerCreditManager = new CustomerCreditManager(VT.CUSTOMER_CREDIT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final InvoiceCreditManager invoiceCreditManager = new InvoiceCreditManager(VT.INVOICE_CREDIT.manager(), ObjectDataOptionsEnum.ARCHIVE);
    private final PaymentManager paymentManager = new PaymentManager(VT.PAYMENT.manager(), ObjectDataOptionsEnum.ARCHIVE);

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private TransactionService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@literal TransactionService} class
     *
     * @return instance of the {@literal TransactionService}
     * @throws PersistenceException
     */
    public static TransactionService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (TransactionService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new TransactionService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@literal TransactionService} class has been initialised. This is used mostly for testing to
     * avoid initialisation of managers when the underlying elements of the initialisation are not available.
     *
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@literal TransactionService}. The method returns an instance of the
     * {@literal TransactionService} so it can be chained. This must be called before a the {@literal TransactionService} is
     * used. e.g {@literal TransactionService myTransactionService = }
     *
     * @return instance of the {@literal TransactionService}
     * @throws PersistenceException
     */
    public synchronized TransactionService init() throws PersistenceException {
        if(initialise) {
            invoiceManager.init();
            customerReceiptManager.init();
            customerCreditManager.init();
            invoiceCreditManager.init();
            paymentManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@literal TransactionService}. The method returns an instance of the
     * {@literal TransactionService} so it can be chained.
     *
     * @return instance of the {@literal TransactionService}
     * @throws PersistenceException
     */
    public TransactionService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@literal TransactionService}. This is generally used for testing. If you wish
     * to refresh the object store reInitialise() should be used.
     *
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = invoiceManager.clear() ? success : false;
        success = customerReceiptManager.clear() ? success : false;
        success = customerCreditManager.clear() ? success : false;
        success = invoiceCreditManager.clear() ? success : false;
        success = paymentManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory. <p> Used to reset the {@literal TransactionService}
     * class instance by setting the INSTANCE reference to null. This is mostly used for testing to clear and reset
     * internal memory stores when the underlying persistence data has been removed. </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Manager Retrieval">
    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     *
     * This section allows the retrieval of the instance
     * of the manager to use as part of this service. For
     * ease of reference each manager has both a long and
     * a short name method call. ALL manager instances should
     * be referenced through the Service and not instantiated
     * directly.
     * ***************************************************/
    public InvoiceManager getInvoiceManager() {
        return invoiceManager;
    }

    public CustomerCreditManager getCustomerCreditManager() {
        return customerCreditManager;
    }

    public CustomerReceiptManager getCustomerReceiptManager() {
        return customerReceiptManager;
    }

    public InvoiceCreditManager getInvoiceCreditManager() {
        return invoiceCreditManager;
    }

    public PaymentManager getPaymentManager() {
        return paymentManager;
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Validation Methods">
    /* ***************************************************
     * S E R V I C E   V A L I D A T I O N   M E T H O D S
     *
     * This section is for all validation methods so that
     * other services can check blind id values are legitimate
     * and any associated business rules are adhere to.
     * ***************************************************/

    /**
     * Validation Method to check pure id values exist. This method ONLY checks an ID exists in a manager
     *
     * @param id the identifier to check
     * @param validationType the type of the id to check
     * @return true if the id exists in the manager of the type
     * @throws PersistenceException
     * @see VT
     */
    public boolean isId(int id, VT validationType) throws PersistenceException {
        switch(validationType) {
            case INVOICE:
                return invoiceManager.isIdentifier(id);
            default:
                return (false);
        }
    }

    /**
     * validation test to check transactions associated with an account can be removed. This method only
     * checks for outstanding balance on the account. It does not consider any bookings that are not yet
     * associated with a booking.
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     * @throws IllegalActionException
     */
    public boolean isAccountTransactionsRemovable(int accountId) throws PersistenceException, IllegalActionException {
        return customerCreditManager.getBalance(accountId) == 0;
    }

    /**
     * Determines whether an invoice needs to be created
     *
     * @param accountId
     * @param fixedItemsOnly
     * @param lastYwd
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws IllegalActionException
     */
    public boolean isInvoiceRequired(int accountId, boolean fixedItemsOnly, int lastYwd) throws NoSuchIdentifierException, PersistenceException, IllegalValueException, MaxCountReachedException, NoSuchKeyException, IllegalActionException, NullObjectException {
        BillingService billingService = BillingService.getInstance();

        if(!fixedItemsOnly) {
            // method returns booknings
            if(!billingService.getPredictedBillingsForAccount(accountId, lastYwd).isEmpty()) {
                return true;
            }
        }
        // find any fixed items
        return !billingService.getBillingManager().getAllBillingOutstanding(accountId, lastYwd, BillingEnum.TYPE_FIXED_ITEM).isEmpty();
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Level Get Methods">
    /* ***************************************************
     * S E R V I C E  L E V E L   G E T   M E T H O D S
     *
     * This section is for all get methods that require
     * information outside their own data store. All other
     * get methods can be found up the tree in the manager
     * or bean of the bean type.
     * ***************************************************/
    /**
     * Finds the value of an unallocated credit from a customer receipt (ie anything not yet allocated to a debit,
     * putting the account in credit)
     *
     * @param accountId
     * @param customerReceiptId
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public long getReceiptBalance(int accountId, int customerReceiptId) throws NoSuchIdentifierException, PersistenceException {
        long total = customerReceiptManager.getObject(accountId, customerReceiptId).getValue();
        for(CustomerCreditBean debit : customerCreditManager.getAllDebitForCreditId(accountId, customerReceiptId, CustomerCreditType.RECEIPT_CREDIT)) {
            total -= debit.getDebit();
        }
        return total;
    }

    /**
     * Finds the debits (invoices and payments) to which a receipt is allocated
     *
     * @param accountId
     * @param customerReceiptId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<ObjectBean> getDebitsForReceipt(int accountId, int customerReceiptId)
            throws PersistenceException, NoSuchIdentifierException {
        LinkedList<ObjectBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean debit : customerCreditManager.getAllDebitForCreditId(accountId, customerReceiptId, CustomerCreditType.RECEIPT_CREDIT)) {
            if(debit.getDebitType() == CustomerCreditType.INVOICE_DEBIT) {
                rtnList.add(invoiceManager.getObject(accountId, debit.getDebitId()));
            } else if(debit.getDebitType() == CustomerCreditType.PAYMENT_DEBIT) {
                rtnList.add(paymentManager.getObject(accountId, debit.getDebitId()));
            }
        }
        return rtnList;
    }

    /**
     * Finds all transactions (invoice credits and customer receipts) that go towards paying for a particular invoice
     *
     * @param accountId
     * @param invoiceId
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public List<TransactionBean> getReceiptsForInvoice(int accountId, int invoiceId)
            throws PersistenceException, NoSuchIdentifierException {
        LinkedList<TransactionBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean credit : customerCreditManager.getAllCreditForDebitId(accountId, invoiceId, CustomerCreditType.INVOICE_DEBIT)) {
            if(credit.getCreditType() == CustomerCreditType.INVOICE_CREDIT) {
                rtnList.add(invoiceCreditManager.getObject(accountId, credit.getCreditId()));
            }
            if(credit.getCreditType() == CustomerCreditType.RECEIPT_CREDIT) {
                rtnList.add(customerReceiptManager.getObject(accountId, credit.getCreditId()));
            }
        }
        return rtnList;
    }

    /**
     * Helper method to calculate the the total values of a list of BillingBean objects. The returned array is of size 3
     * and contains value including tax, the value and the tax. When collecting the values all credit values are taken
     * as negative and charges as positive.
     *
     * <p> The array order is [0] - TransactionService.VALUE_INC_TAX<br> [1] - transactionService.VALUE_ONLY<br> [2]
     * - TransactionService.TAX_ONLY<br> </p>
     *
     * @param billingList the list to get a value from
     * @return an array of values
     * @throws PersistenceException
     */
    public long[] getBillingValues(List<BillingBean> billingList) throws PersistenceException {
        final long[] rtnValues = {0, 0, 0};
        for(BillingBean billing : billingList) {
            long[] billingValues = billing.getSignedValueArray();
            rtnValues[BillingBean.VALUE_INC_TAX] += billingValues[BillingBean.VALUE_INC_TAX];
            rtnValues[BillingBean.VALUE_ONLY] += billingValues[BillingBean.VALUE_ONLY];
            rtnValues[BillingBean.TAX_ONLY] += billingValues[BillingBean.TAX_ONLY];
        }
        return rtnValues;
    }

    /**
     * Helper method to calculate the the total values for an accounts invoice. The returned array is of size 3 and
     * contains value including tax, the value and the tax
     *
     * <p> The array order is [0] - valueIncTax<br> [1] -value<br> [2] - tax<br> </p>
     *
     * @param accountId
     * @param invoiceId
     * @return an array of values
     * @throws PersistenceException
     */
    public long[] getInvoiceValues(int accountId, int invoiceId) throws PersistenceException {
        BillingService billingService = BillingService.getInstance();

        return this.getBillingValues(billingService.getBillingsForInvoice(accountId, invoiceId));
    }

    /**
     * Looks back through the invoices associated with the account and returns the "real" (as in not a receipt fee
     * invoice) invoice at index position. If there aren't enough invoices to get to the index then a
     * NullObjectException is thrown
     *
     * @param accountId
     * @param index
     * @return
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public InvoiceBean getRecentInvoice(int accountId, int index) throws PersistenceException, NullObjectException {
        List<InvoiceBean> invoices = invoiceManager.getAllObjects(accountId);
        Collections.reverse(invoices);
        int count = 0;
        for(InvoiceBean i : invoices) {
            // ignore any fee invoices
            if(i.getInvoiceType() == InvoiceType.RECEIPT_FEE) {
                continue;
            }
            if(count == index) {
                return i;
            }
            count++;
        }
        throw new NullObjectException("There are fewer than " + (index + 1) + " invoices on account " + accountId);
    }

    /**
     * Finds all objects referenced by the customerCreditManager
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     * @throws IllegalValueException
     */
    public List<ObjectBean> getAllTransactionalObjects(int accountId) throws PersistenceException, IllegalValueException {
        List<ObjectBean> rtnList = new LinkedList<>();
        // debit invoices only
        for(InvoiceBean inv : invoiceManager.getAllObjects(accountId)) {
            if(this.getInvoiceValues(accountId, inv.getInvoiceId())[BillingBean.VALUE_ONLY] >= 0) {
                rtnList.add(inv);
            }
        }
        // all payments
        rtnList.addAll(paymentManager.getAllPayments(accountId));
        // all invoice credits
        rtnList.addAll(invoiceCreditManager.getAllObjects(accountId));
        // all receipts
        rtnList.addAll(customerReceiptManager.getAllObjects(accountId));
        // now sort them by ywd
        Collections.sort(rtnList, new YwdComparator());
        return rtnList;
    }

    /**
     * Finds all the transactions on an account (invoices, receipts and payments).
     *
     * Orders them in ywd order, then receipt, invoice, payment for a single day
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     */
    public List<ObjectBean> getAllTransactions(int accountId) throws PersistenceException {
        List<ObjectBean> rtnList = new LinkedList<>();
        rtnList.addAll(customerReceiptManager.getAllObjects(accountId));
        rtnList.addAll(invoiceManager.getAllObjects(accountId));
        rtnList.addAll(paymentManager.getAllPayments(accountId));
        // now sort them into ywd, then receipts, then invoices, then payments
        Collections.sort(rtnList, new YwdOrderedTypeComparator());
        return rtnList;
    }

    /**
     * Finds all the transactions on an account (invoices, receipts and payments) on or after the ywd specified
     *
     * Orders them in ywd order, then receipt, invoice, payment for a single day
     *
     * @param accountId
     * @param firstYwd
     * @return
     * @throws PersistenceException
     */
    public List<ObjectBean> getAllTransactions(int accountId, int firstYwd) throws PersistenceException {
        List<ObjectBean> rtnList = new LinkedList<>();
        for(ObjectBean transaction : getAllTransactions(accountId)) {
            if(transaction instanceof TransactionBean) {
                if(((TransactionBean) transaction).getYwd() >= firstYwd) {
                    rtnList.add(transaction);
                }
            }
            if(transaction instanceof InvoiceBean) {
                if(((InvoiceBean) transaction).getYwd() >= firstYwd) {
                    rtnList.add(transaction);
                }
            }
        }
        // retains original order
        return rtnList;
    }

    /**
     * Finds the total value of all invoices in the system between the dates provided If isDueDate, uses the invoice due
     * date as the comparison; if this is false, uses the invoice issue date
     *
     * @param isDueDate
     * @param startYwd
     * @param endYwd
     * @return
     * @throws PersistenceException
     */
    public long[] getTotalInvoiceValue(boolean isDueDate, int startYwd, int endYwd) throws PersistenceException {
        long[] rtnValues = new long[3];
        Arrays.fill(rtnValues, 0);
        for(InvoiceBean invoice : invoiceManager.getAllObjects(isDueDate, startYwd, endYwd)) {
            long[] invoiceValues = this.getInvoiceValues(invoice.getAccountId(), invoice.getInvoiceId());
            rtnValues[BillingBean.VALUE_INC_TAX] += invoiceValues[BillingBean.VALUE_INC_TAX];
            rtnValues[BillingBean.VALUE_ONLY] += invoiceValues[BillingBean.VALUE_ONLY];
            rtnValues[BillingBean.TAX_ONLY] += invoiceValues[BillingBean.TAX_ONLY];
        }
        return rtnValues;
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Service Level Set Methods">
    /* **************************************************
     * S E R V I C E   L E V E L   S E T   M E T H O D S
     *
     * All set methods should be interfaced at the Service
     * level as this allows consistency across the API and
     * security for the underlying sets. If we have sets at
     * the Service level, the Manager level and ObjectStore,
     * the inconsistency might cause a set to be made to the
     * manager when a set in the service adds additional logic
     * and thus is bypassed.
     * **************************************************/
    /**
     * Attempts to create invoices for each account specified in the list.
     *
     * @param accountIds
     * @param invoiceYwd
     * @param lastYwd
     * @param dueYwd
     * @param notes
     * @param owner
     * @return
     * @throws BusinessRuleException
     * @throws DuplicateIdentifierException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalActionException
     * @throws IllegalValueException
     */
    public List<InvoiceBean> createMultipleInvoices(Set<Integer> accountIds, int invoiceYwd, int lastYwd, int dueYwd,
            String notes, String owner) throws DuplicateIdentifierException, MaxCountReachedException, NoSuchIdentifierException, NoSuchKeyException, NullObjectException, PersistenceException, IllegalActionException, BusinessRuleException, IllegalValueException {
        List<InvoiceBean> rtnList = new LinkedList<>();

        // for multiple invoicing, always look at both booking charges and fixed items
        boolean fixedItemsOnly = false;

        for(int accountId : accountIds) {
            InvoiceBean invoice;
            try {
                invoice = createInvoice(accountId, fixedItemsOnly, invoiceYwd, lastYwd, dueYwd, notes, owner);
            } catch(BusinessRuleException bre) {
                // if there is nothing to invoice then move on
                continue;
            }
            if(invoice != null) {
                rtnList.add(invoice);
            }
        }
        return rtnList;
    }

    /**
     * This method creates an invoice, allocation an invoice id to all fixedChargeBean Object that have not been
     * allocated and creates BillingBean objects for all current bookings not yet invoiced.
     *
     * @param accountId
     * @param fixedItemsOnly
     * @param invoiceYwd
     * @param lastYwd
     * @param dueYwd
     * @param notes
     * @param owner
     * @return
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NoSuchKeyException
     * @throws NullObjectException
     * @throws DuplicateIdentifierException
     * @throws IllegalActionException
     * @throws IllegalValueException
     * @throws BusinessRuleException
     */
    public InvoiceBean createInvoice(int accountId, boolean fixedItemsOnly, int invoiceYwd, int lastYwd, int dueYwd,
            String notes, String owner) throws PersistenceException, MaxCountReachedException, DuplicateIdentifierException, IllegalValueException, NoSuchKeyException, IllegalActionException, NullObjectException, NoSuchIdentifierException, BusinessRuleException {

        BillingService billingService = BillingService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        // do a tidy up of any billings associated with bookings that might have been created and not invoice.
        billingService.removeAllOustandingBillings(accountId);

        // check there is something to invoice
        if(!this.isInvoiceRequired(accountId, fixedItemsOnly, lastYwd)){
            throw new BusinessRuleException("NoChargesForInvoicing");
        }

        // set up the invoice
        int invoiceId = getUniqueId();
        if(customerCreditManager.isDebitIdUsed(accountId, invoiceId, CustomerCreditType.INVOICE_DEBIT)) {
            throw new DuplicateIdentifierException("the invoiceId " + invoiceId
                    + " has already been used for accountId " + accountId + " of type INVOICE_DEBIT", true);
        }
        if(customerCreditManager.isCreditIdUsed(accountId, invoiceId, CustomerCreditType.INVOICE_CREDIT)) {
            throw new DuplicateIdentifierException("the invoiceId " + invoiceId
                    + " has already been used for accountId " + accountId + " of type INVOICE_CREDIT", true);
        }

        // create invoice bean
        InvoiceBean invoice = new InvoiceBean(invoiceId, accountId, invoiceYwd, InvoiceType.STANDARD, lastYwd, dueYwd, notes, owner);
        invoiceManager.setObject(accountId, invoice);

        // set all the billings
        List<BillingBean> billingList = new LinkedList<>();
        // only get the fixed item billings and set invoice
        for(BillingBean billing : billingService.getBillingManager().getAllBillingOutstanding(accountId, lastYwd, TYPE_FIXED_ITEM)) {
            billingService.setBillingInvoiceId(billing.getAccountId(), billing.getBillingId(), invoiceId, owner);
            billingList.add(billing);
        }

        if(!fixedItemsOnly) {
            // set bookings restricted and loop the returned bookings where the pre-charge flag is set
            for(BookingBean booking : bookingService.setInvoiceRestrictions(lastYwd, accountId)) {
                // create billing for each pre-charge bookingvOthers
                billingList.addAll(billingService.setBillingForBooking(accountId, booking, invoiceId, owner));
            }
        }
        long totalCharge = this.getBillingValues(billingList)[BillingBean.VALUE_ONLY];
        // if this is a credit invoice, allocate credit
        if(totalCharge < 0) {
            // as the total is a negative value we need to make it positive and save as a credit
            totalCharge *= -1;
            // add the credit as a new invoiceCredit
            InvoiceCreditBean invoiceCredit = invoiceCreditManager.setObject(accountId, new InvoiceCreditBean(getUniqueId(), invoiceYwd, accountId, invoice.getInvoiceId(), totalCharge, "", owner));
            // add the credit in the CustomerCreditManager to balance credit/debit
            customerCreditManager.setCustomerCredit(invoiceYwd, accountId, invoiceCredit.getInvoiceCreditId(), totalCharge, CustomerCreditType.INVOICE_CREDIT, owner);
        } // this is a debit, allocate debit
        else {
            // add the debit in the CustomerCreditManager to balance credit/debit
            customerCreditManager.setCustomerDebit(invoiceYwd, accountId, invoice.getInvoiceId(), totalCharge, CustomerCreditType.INVOICE_DEBIT, owner);
        }
        return(invoice);
    }

    /**
     * Helper method removes an invoice due to an administrative error. This method voids the invoice with a invoice type of
     * STANDARD_VOID so the error is auditable. All billings associated with this invoice are removed and bookings set
     * back to AUTHORISED.
     *
     * @param accountId
     * @param invoiceId
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     */
    public void adminVoidInvoice(int accountId, int invoiceId, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, MaxCountReachedException, DuplicateIdentifierException, IllegalValueException, IllegalActionException {
        if(!invoiceManager.isIdentifier(accountId, invoiceId)) {
            throw new NoSuchIdentifierException("There is no invoice in account '" + accountId + "' with the invoice id '" + invoiceId + "'");
        }
        BillingService billingService = BillingService.getInstance();
        ChildBookingService bookingService = ChildBookingService.getInstance();

        Set<Integer> bookingIdSet = new ConcurrentSkipListSet<>();
        // find all the billings for this invoice
        for(BillingBean billing : billingService.getBillingsForInvoice(accountId, invoiceId)){
            // reset the billing invoice id to -1 to disassociate from the invoice
            try {
                billingService.setBillingInvoiceId(accountId, billing.getBillingId(), -1, owner);
            } catch(NoSuchIdentifierException nsie) {
                //just ignore
            }
            // if it is a fixed item then keep the billing
            if(billing.hasAnyBillingBit(TYPE_FIXED_ITEM)) {
                continue;
            }
            // make sure we don't keep changing an already changed booking
            if(billing.getBookingId() > 0 && !bookingIdSet.contains(billing.getBookingId())) {
                // reset the booking Authorised if it has been set to reconciled
                bookingService.resetBookingToAuthorised(billing.getYwd(), billing.getBookingId(), owner);
                // if there is a billing value that is not zero then set for immadiate recalc.
                if(billing.getValue() != 0) {
                    bookingService.setBookingActionBits(billing.getYwd(), billing.getBookingId(), ActionBits.IMMEDIATE_RECALC_BIT, true, owner);
                }
                bookingIdSet.add(billing.getBookingId());
            }
            //remove the billing
            billingService.getBillingManager().removeObject(accountId, billing.getBillingId());
        }
        // now void the invoice, this invoice should now be worth zero value
        invoiceManager.setInvoiceVoid(accountId, invoiceId, owner);
        // because customerCreditManager is basically a calculator, now the invoice is zero we need to recalculate
        rebuildCustomerCreditManagerKey(accountId);
    }

    /**
     * Allows an account to be adjusted through invoicing ("sales") either as credit or debit. The adjustment value is
     * entered for ease of tax calculations, but the ex-tax value is invoiced.
     *
     * @param ywd
     * @param accountId
     * @param valueIncTax
     * @param billType BILL_CHARGE (when fixed item discount will be applied) OR BILL_CREDIT (when NO discount will be
     * applied)
     * @param description
     * @param notes
     * @param owner
     * @return
     * @throws DuplicateIdentifierException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws IllegalValueException
     * @throws BusinessRuleException
     * @throws IllegalActionException
     */
    public InvoiceBean setAccountAdjustment(int ywd, int accountId, long valueIncTax, BillingEnum billType,
            String description, String notes, String owner) throws DuplicateIdentifierException, MaxCountReachedException, NoSuchIdentifierException, NullObjectException, PersistenceException, IllegalValueException, BusinessRuleException, IllegalActionException {
        PropertiesService propertiesService = PropertiesService.getInstance();
        BillingService billingService = BillingService.getInstance();

        // this is not a booking, so the tax rate is as set
        int taxRate = propertiesService.getSystemProperties().getTaxRate();
        long valueExTax = TDCalc.getBeforeTaxValue(valueIncTax, taxRate);
        // create the fixed item for the account
        List<BillingBean> billingList = new LinkedList<>();
        CollectionUtils.addIgnoreNull(billingList, billingService.setFixedItem(-1, ywd, accountId, -1, valueExTax, billType, description, notes, owner));
        //invoice this billing only
        InvoiceBean invoice = createFixedInvoice(accountId, billingList, ywd, InvoiceType.RECEIPT_FEE_VOID, notes, owner);
        return (invoice);
    }

    /**
     * Helper method to carry out a void and reset of a customer receipt if a receipt is "edited" in the front end
     *
     * @param customerReceiptId
     * @param ywd
     * @param accountId
     * @param receiptValue
     * @param type
     * @param reference
     * @param notes
     * @param owner
     * @return
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws DuplicateIdentifierException
     */
    public CustomerReceiptBean setCustomerReceipt(int customerReceiptId, int ywd, int accountId, long receiptValue,
            PaymentType type, String reference, String notes, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalValueException, MaxCountReachedException, NullObjectException, DuplicateIdentifierException, BusinessRuleException, IllegalActionException {
        if(customerReceiptId == -1) {
            return setCustomerReceipt(ywd, accountId, receiptValue, type, reference, notes, owner);
        }
        // as there is a customerReceiptId this must be a void
        return adminVoidCustomerReceipt(customerReceiptId, ywd, accountId, receiptValue, type, reference, notes, owner);
    }

    /**
     * sets a Customer Receipt. If the paymentType is a credit card payment then based on the credit card fee rate, an
     * invoice is automatically created for the card fee. The receipt value should be the total amount received
     * including the fee.
     *
     * @param ywd
     * @param accountId
     * @param receiptValue
     * @param type
     * @param reference
     * @param notes
     * @param owner
     * @return the newly created receipt bean
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NullObjectException
     * @throws DuplicateIdentifierException
     * @throws BusinessRuleException
     */
    protected CustomerReceiptBean setCustomerReceipt(int ywd, int accountId, long receiptValue, PaymentType type,
            String reference, String notes, String owner) throws PersistenceException, NoSuchIdentifierException, IllegalValueException, MaxCountReachedException, NullObjectException, DuplicateIdentifierException, BusinessRuleException, IllegalActionException {

        if(receiptValue <= 0) {
            throw new IllegalValueException("receipt value must be positive, was " + receiptValue);
        }
        // if the payment was credit card create an immediate invoice
        if(type.equals(PaymentType.CREDITCARD)) {
            setCreditCardBilling(receiptValue, ywd, accountId, notes, owner);
        }
        // create a customer receipt for the full amount that has been recieved
        CustomerReceiptBean customerReceipt = new CustomerReceiptBean(getUniqueId(), ywd, accountId, receiptValue, type, reference, notes, owner);
        customerReceiptManager.setObject(accountId, customerReceipt);
        // add the credit to the customer credit manager (remember this includes the debited fee for credit cards if it was charged.
        customerCreditManager.setCustomerCredit(ywd, accountId, customerReceipt.getCustomerReceiptId(), receiptValue, CustomerCreditType.RECEIPT_CREDIT, owner);
        return (customerReceipt);
    }

    /**
     * Helper method adjusts a Customer Receipt due to an admin error. This method voids the customer receipt with a
     * payment type of ADMIN_VOID so the error is auditable. It then edits the original payment with the new values
     *
     * @param accountId
     * @param customerReceiptId
     * @param ywd
     * @param receiptValue
     * @param type
     * @param reference
     * @param notes
     * @param owner
     * @return the newly created Customer Receipt
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     * @throws IllegalValueException
     * @throws BusinessRuleException
     * @throws com.oathouse.oss.storage.exceptions.IllegalActionException
     */
    protected CustomerReceiptBean adminVoidCustomerReceipt(int customerReceiptId, int ywd, int accountId, long receiptValue, PaymentType type,
            String reference, String notes, String owner) throws PersistenceException, NoSuchIdentifierException, NullObjectException, MaxCountReachedException, DuplicateIdentifierException, IllegalValueException, BusinessRuleException, IllegalActionException {

        if(!customerReceiptManager.isIdentifier(accountId, customerReceiptId)) {
            throw new NoSuchIdentifierException("There is no Customer Receipt to void in account '" + accountId + "' with the reference id '" + customerReceiptId + "'");
        }
        if(receiptValue < 0) {
            throw new IllegalValueException("an admin void receipt value must be zero or more, was " + receiptValue);
        }
        // clone the original object to make a void
        CustomerReceiptBean voidCustomerReceipt = customerReceiptManager.cloneObjectBean(getUniqueId(), customerReceiptManager.getObject(accountId, customerReceiptId));
        // if the original customer receipt had a credit card payment we need to reverse that out with a fixed credit
        if(voidCustomerReceipt.getPaymentType().equals(PaymentType.CREDITCARD)) {
            PropertiesService propertiesService = PropertiesService.getInstance();

            // calculate the fee for credit cards
            int creditCardFeeRate = propertiesService.getSystemProperties().getCreditCardFeeRate();
            creditCardFeeRate = creditCardFeeRate > 0 ? creditCardFeeRate : 0;
            long valueExTax = TDCalc.getBeforeTaxValue(voidCustomerReceipt.getValue(), creditCardFeeRate);
            long feeIncTax = voidCustomerReceipt.getValue() - valueExTax;
            this.setAccountAdjustment(voidCustomerReceipt.getYwd(), accountId, feeIncTax, BillingEnum.BILL_CREDIT, ApplicationConstants.ADMIN_VOID_CARD_FEE, "", ObjectBean.SYSTEM_OWNED);
        }
        // save the new object and set it to void
        customerReceiptManager.setObject(accountId, voidCustomerReceipt);
        customerReceiptManager.setCustomerReceiptVoid(accountId, voidCustomerReceipt.getCustomerReceiptId(), owner);
        // because customerCreditManager is basically a claculator we need to remove the old credit
        customerCreditManager.removeReceipt(accountId, customerReceiptId, owner);
        // now edit the original but only
         CustomerReceiptBean customerReceipt = null;
        if(receiptValue > 0) {
            // if the payment was credit card create an immediate invoice
            if(type.equals(PaymentType.CREDITCARD)) {
                setCreditCardBilling(receiptValue, ywd, accountId, notes, owner);
            }
            // create a customer receipt for the full amount that has been recieved
            customerReceipt = new CustomerReceiptBean(customerReceiptId, ywd, accountId, receiptValue, type, reference, notes, owner);
            customerReceiptManager.setObject(accountId, customerReceipt);
            // add the credit to the customer credit manager (remember this includes the debited fee for credit cards if it was charged.
            customerCreditManager.setCustomerCredit(ywd, accountId, customerReceipt.getCustomerReceiptId(), receiptValue, CustomerCreditType.RECEIPT_CREDIT, owner);
        } else {
            //if there is no value then delete
            customerReceiptManager.removeObject(accountId, customerReceiptId);
            // create a customer receipt with no value to return
            customerReceipt = new CustomerReceiptBean(customerReceiptId, ywd, accountId, 0, type, reference, notes, owner);
        }
        // rebuild the credit manager
        rebuildCustomerCreditManagerKey(accountId);
        return (customerReceipt);
    }

    /**
     * Helper method to carry out a void and reset of a payment if a payment is "edited" in the front end
     *
     * @param paymentId
     * @param ywd
     * @param accountId
     * @param value
     * @param type
     * @param reference
     * @param description
     * @param notes
     * @param owner
     * @return
     * @throws DuplicateIdentifierException
     * @throws IllegalValueException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws PersistenceException
     */
    public PaymentBean setPayment(int paymentId, int ywd, int accountId, long value, PaymentType type,
            String reference, String description, String notes, String owner)
            throws DuplicateIdentifierException, IllegalValueException, MaxCountReachedException,
            NoSuchIdentifierException, NullObjectException, PersistenceException {
        if(paymentId != -1) {
            adminVoidPayment(accountId, paymentId);
        }
        return setPayment(ywd, accountId, value, type, reference, description, notes, owner);
    }

    /**
     * Creates a payment
     *
     * @param ywd
     * @param accountId
     * @param value
     * @param type
     * @param reference
     * @param description
     * @param notes
     * @param owner
     * @return the new payment
     * @throws MaxCountReachedException
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws DuplicateIdentifierException
     * @throws NoSuchIdentifierException
     */
    public PaymentBean setPayment(int ywd, int accountId, long value, PaymentType type, String reference,
            String description, String notes, String owner) throws MaxCountReachedException, PersistenceException, NullObjectException, DuplicateIdentifierException, NoSuchIdentifierException {
        PaymentBean payment = new PaymentBean(getUniqueId(), ywd, accountId, value, type, reference, description, notes, owner);
        // add the payment
        paymentManager.setObject(accountId, payment);
        // now add to the customer credit as a debit
        customerCreditManager.setCustomerDebit(ywd, accountId, payment.getPaymentId(), value, CustomerCreditType.PAYMENT_DEBIT, owner);
        return (payment);
    }

    /**
     * Helper method removes a Payment due to an admin error. This method voids the payment with a payment type of
     * ADMIN_VOID so the error is auditable. A payment credit is created to reverse out the debit from the original
     * payment.
     *
     * @param accountId
     * @param paymentId
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     */
    public void adminVoidPayment(int accountId, int paymentId) throws PersistenceException, NoSuchIdentifierException, NullObjectException, MaxCountReachedException, DuplicateIdentifierException, IllegalValueException {
        if(!paymentManager.isIdentifier(accountId, paymentId)) {
            throw new NoSuchIdentifierException("There is no payment in account '" + accountId + "' with the reference id '" + paymentId + "'");
        }
        // now void the payment
        paymentManager.setPaymentVoid(accountId, paymentId, ObjectBean.SYSTEM_OWNED);
        // because customerCreditManager is basically a calculator we need to remove the old debit and rebuild the customer credit manager
        customerCreditManager.removePayment(accountId, paymentId, ObjectBean.SYSTEM_OWNED);
        rebuildCustomerCreditManagerKey(accountId);
    }

    /**
     * Method used to remove an account transaction data including billing, invoices and receipts
     *
     * @param accountId
     */
    public void removeAccountTransactions(int accountId) throws PersistenceException, NoSuchIdentifierException, IllegalActionException {
        BillingService billingService = BillingService.getInstance();

        // check the account is removable
        if(!isAccountTransactionsRemovable(accountId)) {
            throw new IllegalActionException("Account '" + accountId + "' has outstanding transactions and cannot be removed");
        }
        // remove the billings
        billingService.getBillingManager().removeKey(accountId);
        // remove invoices
        invoiceManager.removeKey(accountId);
        // remove payments
        paymentManager.removeKey(accountId);
        // remove invoice credit
        invoiceCreditManager.removeKey(accountId);
        // remove customer receipt
        customerReceiptManager.removeKey(accountId);
        // remove the credits
        customerCreditManager.removeKey(accountId);
    }

    /**
     * Rebuilds a customerCreditManager key - ie the calculated balance for an account using the transactional objects
     * to re-reference and rebuild the credit beans. The owners of the objects are carried through into the ccm as they
     * were when the ccm was built originally
     *
     * @param accountId
     * @throws PersistenceException
     * @throws DuplicateIdentifierException
     * @throws MaxCountReachedException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     * @throws IllegalValueException
     */
    public void rebuildCustomerCreditManagerKey(int accountId)
            throws PersistenceException, DuplicateIdentifierException, MaxCountReachedException, NoSuchIdentifierException,
            NullObjectException, IllegalValueException {
        customerCreditManager.removeKey(accountId);
        InvoiceBean inv;
        PaymentBean pay;
        InvoiceCreditBean iCr;
        CustomerReceiptBean rec;
        List<InvoiceBean> priorityInvoices = new LinkedList<>();
        outer:
        for(ObjectBean obj : getAllTransactionalObjects(accountId)) {
            if(obj instanceof InvoiceBean) {
                inv = (InvoiceBean) obj;
                if(inv.getInvoiceType() == InvoiceType.RECEIPT_FEE) {
                    priorityInvoices.add(inv);
                }
                customerCreditManager.setCustomerDebit(inv.getYwd(), accountId, inv.getInvoiceId(),
                        this.getInvoiceValues(accountId, inv.getInvoiceId())[BillingBean.VALUE_INC_TAX],
                        CustomerCreditType.INVOICE_DEBIT, inv.getOwner());
            } else if(obj instanceof PaymentBean) {
                pay = (PaymentBean) obj;
                customerCreditManager.setCustomerDebit(pay.getYwd(), accountId, pay.getPaymentId(), pay.getValue(),
                        CustomerCreditType.PAYMENT_DEBIT, pay.getOwner());
            } else if(obj instanceof InvoiceCreditBean) {
                iCr = (InvoiceCreditBean) obj;
                customerCreditManager.setCustomerCredit(iCr.getYwd(), accountId, iCr.getInvoiceCreditId(), iCr.getValue(),
                        CustomerCreditType.INVOICE_CREDIT, iCr.getOwner());
            } else if(obj instanceof CustomerReceiptBean) {
                rec = (CustomerReceiptBean) obj;
                // try and find a priority (fee) invoice on the same day: there should be one of lower value than the receipt
                for(int i = 0; i < priorityInvoices.size(); i++) {
                    inv = priorityInvoices.get(i);
                    if(inv.getYwd() == rec.getYwd()) {
                        customerCreditManager.setCustomerCreditWithPriority(rec.getYwd(), accountId, rec.getCustomerReceiptId(), inv.getInvoiceId(),
                                rec.getValue(), CustomerCreditType.RECEIPT_CREDIT, rec.getOwner());
                        priorityInvoices.remove(inv);
                        continue outer;
                    }
                }
                // otherwise just allocate the receipt
                customerCreditManager.setCustomerCredit(rec.getYwd(), accountId, rec.getCustomerReceiptId(), rec.getValue(),
                        CustomerCreditType.RECEIPT_CREDIT, rec.getOwner());
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="expanded" desc="Private Class Methods">
    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     *
     * This section is for all private methods for the
     * class. Private methods should be carefully used
     * so as to avoid multi jump calls and as a general
     * rule of thumb, only when multiple methods use a
     * common algorithm.
     * ***************************************************/

    /**
     * Determines if the first invoice was created before the second invoice. If either invoiceId
     * is -1 or the invoiceId does not exist the false is returned
     *
     * @param accountId
     * @param firstInvoiceId
     * @param secondInvoiceId
     * @return true if the first invoice was created before the second invoice
     * @throws PersistenceException
     */
    protected boolean isInvoiceBefore(int accountId, int firstInvoiceId, int secondInvoiceId) throws PersistenceException {
        if(firstInvoiceId > 0 && secondInvoiceId > 0) {
            InvoiceBean firstInvoice;
            InvoiceBean secondInvoice;
            try {
                firstInvoice = invoiceManager.getObject(accountId, firstInvoiceId);
                secondInvoice = invoiceManager.getObject(accountId, secondInvoiceId);
                if(firstInvoice.getCreated() <= secondInvoice.getCreated()) {
                    return true;
                }
            } catch(NoSuchIdentifierException nsie) {
                return false;
            }
        }
        return false;
    }

    private int getUniqueId() throws MaxCountReachedException, PersistenceException {
        ConcurrentSkipListSet<Integer> excludes = new ConcurrentSkipListSet<>();
        excludes.addAll(invoiceManager.getAllIdentifier());
        excludes.addAll(invoiceCreditManager.getAllIdentifier());
        excludes.addAll(customerReceiptManager.getAllIdentifier());
        excludes.addAll(paymentManager.getAllIdentifier());

        return ObjectEnum.generateIdentifier(excludes);
    }

    private void setCreditCardBilling(long receiptValue, int ywd, int accountId, String notes, String owner) throws PersistenceException, MaxCountReachedException, IllegalValueException, NoSuchIdentifierException, NullObjectException, BusinessRuleException, DuplicateIdentifierException, IllegalActionException {
        BillingService billingService = BillingService.getInstance();
        PropertiesService propertiesService = PropertiesService.getInstance();

        // calculate the fee for credit cards
        int creditCardFeeRate = propertiesService.getSystemProperties().getCreditCardFeeRate();
        creditCardFeeRate = creditCardFeeRate > 0 ? creditCardFeeRate : 0;
        long valueExTax = TDCalc.getBeforeTaxValue(receiptValue, creditCardFeeRate);
        long feeIncTax = receiptValue - valueExTax;
        long feeExTax = TDCalc.getBeforeTaxValue(feeIncTax, propertiesService.getSystemProperties().getTaxRate());

        // create the fixedItem for the fee charge (Note we set NO discount on this charge)
        List<BillingBean> billingList = new LinkedList<>();
        CollectionUtils.addIgnoreNull(billingList, billingService.setFixedItem(-1, ywd, accountId, -1, feeExTax, BILL_CHARGE, ApplicationConstants.RECEIPT_CARD_FEE, notes, owner, APPLY_NO_DISCOUNT));
        // there might be no credit card fee
        if(!billingList.isEmpty()) {
            // create invoice for fee charge
            this.createFixedInvoice(accountId, billingList, ywd, InvoiceType.RECEIPT_FEE, notes, owner);
        }
    }

    protected InvoiceBean createFixedInvoice(int accountId, List<BillingBean> billingList, int ywd, InvoiceType type, String notes, String owner) throws PersistenceException, MaxCountReachedException, DuplicateIdentifierException, IllegalValueException, IllegalActionException, NullObjectException, NoSuchIdentifierException, BusinessRuleException {

        BillingService billingService = BillingService.getInstance();

        // do a tidy up of any billings associated with bookings that might have been created and not invoice.
        billingService.removeAllOustandingBillings(accountId);

        // set up the invoice
        int invoiceId = getUniqueId();
        if(customerCreditManager.isDebitIdUsed(accountId, invoiceId, CustomerCreditType.INVOICE_DEBIT)) {
            throw new DuplicateIdentifierException("the invoiceId " + invoiceId
                    + " has already been used for accountId " + accountId + " of type INVOICE_DEBIT", true);
        }
        if(customerCreditManager.isCreditIdUsed(accountId, invoiceId, CustomerCreditType.INVOICE_CREDIT)) {
            throw new DuplicateIdentifierException("the invoiceId " + invoiceId
                    + " has already been used for accountId " + accountId + " of type INVOICE_CREDIT", true);
        }

        // create invoice bean
        InvoiceBean invoice = new InvoiceBean(invoiceId, accountId, ywd, type, ywd, ywd, notes, owner);
        invoiceManager.setObject(accountId, invoice);
        //set the billing invoiceId
        for(BillingBean billing : billingList) {
            billingService.setBillingInvoiceId(accountId, billing.getBillingId(), invoiceId, owner);
        }

        long totalCharge = this.getBillingValues(billingList)[BillingBean.VALUE_ONLY];
        // if this is a credit invoice, allocate credit
        if(totalCharge < 0) {
            // as the total is a negative value we need to make it positive and save as a credit
            totalCharge *= -1;
            // add the credit as a new invoiceCredit
            InvoiceCreditBean invoiceCredit = invoiceCreditManager.setObject(accountId, new InvoiceCreditBean(getUniqueId(), ywd, accountId, invoice.getInvoiceId(), totalCharge, "", owner));
            // add the credit in the CustomerCreditManager to balance credit/debit
            customerCreditManager.setCustomerCredit(ywd, accountId, invoiceCredit.getInvoiceCreditId(), totalCharge, CustomerCreditType.INVOICE_CREDIT, owner);
        } // this is a debit, allocate debit
        else {
            // add the debit in the CustomerCreditManager to balance credit/debit
            customerCreditManager.setCustomerDebit(ywd, accountId, invoice.getInvoiceId(), totalCharge, CustomerCreditType.INVOICE_DEBIT, owner);
        }
        return(invoice);
    }

    //</editor-fold>
}
