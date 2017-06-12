/*
 * @(#)CustomerCreditManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import com.oathouse.oss.storage.exceptions.DuplicateIdentifierException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code CustomerCreditManager} Class extends the methods of the parent class.
 * Used for the reference of Customer Credit against Debit. It is possible to
 * have multiple CustomerCreditBean objects for a single debit or a single
 * credit. The different types of credit and debit can be found in the CustomerCreditType
 * enumeration.
 *
 * key = Account
 *
 * @author Darryl Oatridge
 * @version 1.00 09-Feb-2011
 */
public class CustomerCreditManager extends ObjectMapStore<CustomerCreditBean> {

    /**
     * Constructs a {@code CustomerCreditManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions
     */
    public CustomerCreditManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /* ****************************************
     * G E T   M E T H O D S
     * ***************************************/
    /**
     * Returns a set of outstanding InvoiceId Integers. This is invoices that still have a balance greater
     * than zero. This also includes part paid invoices
     *
     * @param accountId
     * @return a set of invoiceId references
     * @throws PersistenceException
     */
    public Set<Integer> getOutstandingInvoiceId(int accountId) throws PersistenceException {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == ObjectEnum.INITIALISATION.value() && creditBean.getDebitType() == CustomerCreditType.INVOICE_DEBIT) {
                rtnSet.add(creditBean.getDebitId());
            }
        }
        return rtnSet;
    }

    /**
     * Returns a set of outstanding identifiers for beans that are credit. These include all credit types
     * including customer receipts and credit invoice types
     *
     * @param accountId
     * @return a set of creditId references
     * @throws PersistenceException
     */
    public Set<Integer> getOutstandingCreditId(int accountId) throws PersistenceException {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == ObjectEnum.INITIALISATION.value()) {
                rtnSet.add(creditBean.getCreditId());
            }
        }
        return rtnSet;
    }

    /**
     * Returns a set of credit beans that belong to a single creditId
     *
     * @param accountId
     * @param creditId
     * @param creditType
     * @return
     * @throws PersistenceException
     */
    public List<CustomerCreditBean> getAllDebitForCreditId(int accountId, int creditId, CustomerCreditType creditType) throws PersistenceException {
        List<CustomerCreditBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == creditId && creditBean.getCreditType() == creditType && creditBean.getDebitId() != ObjectEnum.INITIALISATION.value()) {
                rtnList.add(creditBean);
            }
        }
        return rtnList;
    }

    /**
     * Returns a list of credit beans that belong to a single debitId.
     * @param accountId
     * @param debitId
     * @param debitType
     * @return
     * @throws PersistenceException
     */
    public List<CustomerCreditBean> getAllCreditForDebitId(int accountId, int debitId, CustomerCreditType debitType) throws PersistenceException {
        List<CustomerCreditBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == debitId && creditBean.getDebitType() == debitType && creditBean.getCreditId() != ObjectEnum.INITIALISATION.value()) {
                rtnList.add(creditBean);
            }
        }
        return rtnList;
    }

    /**
     * Overridden method: finds the credit bean using the parameters supplied
     * @param accountId
     * @param debitId
     * @param debitType
     * @param creditId
     * @param creditType
     * @return
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public CustomerCreditBean getObject(int accountId, int debitId, CustomerCreditType debitType,
            int creditId, CustomerCreditType creditType)
            throws NoSuchIdentifierException, PersistenceException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == debitId && creditBean.getDebitType() == debitType
                    && creditBean.getCreditId() == creditId && creditBean.getCreditType() == creditType) {
                return creditBean;
            }
        }
        throw new NoSuchIdentifierException("A credit bean with debitId " + debitId + ", type " + debitType + ", creditId " + creditId +
                ", type " + creditType + " could not be found for accountId " + accountId);
    }

    public long[] getAccountDebtors(int accountId) {
        long[] rtnArray = new long[7];

        return rtnArray;
    }

    /**
     * returns the current invoice balance.
     *
     * @param accountId
     * @param invoiceId
     * @return long value
     * @throws PersistenceException
     */
    public long getInvoiceBalance(int accountId, int invoiceId) throws PersistenceException {
        long rtnValue = 0;
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == invoiceId && creditBean.getCreditId() == ObjectEnum.INITIALISATION.value()
                                                && creditBean.getDebitType() == CustomerCreditType.INVOICE_DEBIT) {
                rtnValue += creditBean.getDebit();
            }
        }
        return rtnValue;
    }

    /**
     * the current credit balance
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     */
    public long getCreditBalance(int accountId) throws PersistenceException {
        long rtnValue = 0;
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebit() == 0) {
                rtnValue += creditBean.getCredit();
            }
        }
        return rtnValue;
    }

    /**
     * the current debit balance
     *
     * @param accountId
     * @return
     * @throws PersistenceException
     */
    public long getDebitBalance(int accountId) throws PersistenceException {
        long rtnValue = 0;
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCredit() == 0) {
                rtnValue += creditBean.getDebit();
            }
        }
        return rtnValue;
    }

    /**
     * returns the account balance (debit - credit)
     *
     * @param accountId
     * @return account balance
     * @throws PersistenceException
     */
    public long getBalance(int accountId) throws PersistenceException {
        long credit = 0;
        long debit = 0;
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            debit += creditBean.getDebit();
            credit += creditBean.getCredit();
        }
        return (debit - credit);
    }


    /**
     * returns the balance as of a ywd provided.
     *
     * @param accountId
     * @param ywd
     * @return account balance
     * @throws PersistenceException
     */
    public long getBalanceBroughtForward(int accountId, int ywd) throws PersistenceException {
        long credit = 0;
        long debit = 0;
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitYwd() != -1 && creditBean.getDebitYwd() < ywd) {
                debit += creditBean.getDebit();
            }
            if(creditBean.getCreditYwd() != -1 && creditBean.getCreditYwd() < ywd) {
                credit += creditBean.getCredit();
            }
        }
        return (debit - credit);
    }

    /**
     * Finds an "aged" balance between two inclusive ywds.
     * The closing balance is at the END of the endOfPeriodYwd.
     * The opening balance is at the start of the startOfPeriodYwd.
     * @param accountId
     * @param startOfPeriodYwd
     * @param endOfPeriodYwd
     * @return
     * @throws PersistenceException
     */
    public long getPeriodBalance(int accountId, int startOfPeriodYwd, int endOfPeriodYwd) throws PersistenceException {
        // find the balance b/f at the END of the endOfPeriodYwd
        long closingBalance = getBalanceBroughtForward(accountId, YWDHolder.add(endOfPeriodYwd, 1));
        long openingBalance = getBalanceBroughtForward(accountId, startOfPeriodYwd);
        return (closingBalance - openingBalance);
    }

    /**
     * returns the total debtor balance (debit - credit)
     *
     * @return account balance
     * @throws PersistenceException
     */
    public long getBalance() throws PersistenceException {
        long credit = 0;
        long debit = 0;
        for(CustomerCreditBean creditBean : getAllObjects()) {
            debit += creditBean.getDebit();
            credit += creditBean.getCredit();
        }
        return (debit - credit);
    }


    /**
     * returns the total debtor balance as of a ywd provided.
     *
     * @param ywd
     * @return account balance
     * @throws PersistenceException
     */
    public long getBalanceBroughtForward(int ywd) throws PersistenceException {
        long credit = 0;
        long debit = 0;
        for(CustomerCreditBean creditBean : getAllObjects()) {
            if(creditBean.getDebitYwd() != -1 && creditBean.getDebitYwd() < ywd) {
                debit += creditBean.getDebit();
            }
            if(creditBean.getCreditYwd() != -1 && creditBean.getCreditYwd() < ywd) {
                credit += creditBean.getCredit();
            }
        }
        return (debit - credit);
    }

    /**
     * Finds the total debtor "aged" balance between two inclusive ywds.
     * The closing balance is at the END of the endOfPeriodYwd.
     * The opening balance is at the start of the startOfPeriodYwd.
     *
     * @param startOfPeriodYwd
     * @param endOfPeriodYwd
     * @return
     * @throws PersistenceException
     */
    public long getPeriodBalance(int startOfPeriodYwd, int endOfPeriodYwd) throws PersistenceException {
        // find the balance b/f at the END of the endOfPeriodYwd
        long closingBalance = getBalanceBroughtForward(YWDHolder.add(endOfPeriodYwd, 1));
        long openingBalance = getBalanceBroughtForward(startOfPeriodYwd);
        return (closingBalance - openingBalance);
    }


    /* ****************************************
     * S E T   M E T H O D S
     * ***************************************/
    /**
     * Sets a customer a customer credit against their debit. If no debit is found a credit is created. In the
     * event where the credit does not cover the debit, a new CustomerCreditBean is created with the outstanding
     * debit. If The credit exceeds the debit then a new CustomerCreditBean is created with the outstanding credit.
     *
     * @param ywd
     * @param credit
     * @param accountId
     * @param creditId
     * @param creditType
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     */
    public void setCustomerCredit(int ywd, int accountId, int creditId, long credit, CustomerCreditType creditType, String owner)
            throws PersistenceException, NullObjectException, MaxCountReachedException, DuplicateIdentifierException {
        setCustomerCreditWithPriority(ywd, accountId, creditId, -1, credit, creditType, owner);
    }

    /**
     * Sets a customer credit against their debit but prioritising the first debit to be paid. The debitId passed
     * will be the first debit to be paid off if it has not already. In the event where the credit does not cover
     * the debit, a new CustomerCreditBean is created with the outstanding debit. If The credit exceeds the debit
     * then a new CustomerCreditBean is created with the outstanding credit.
     *
     * @param ywd
     * @param credit
     * @param accountId
     * @param debitId
     * @param creditId
     * @param creditType
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     */
    public void setCustomerCreditWithPriority(int ywd, int accountId, int creditId, int debitId, long credit, CustomerCreditType creditType, String owner) throws PersistenceException, DuplicateIdentifierException, NullObjectException, MaxCountReachedException {
        if(isCreditIdUsed(accountId, creditId, creditType)) {
            throw new DuplicateIdentifierException("The creditId " + creditId + " has already been used for type " + creditType.toString());
        }
        long creditRemaining = credit;
        // set the priority invoice
        creditRemaining = setCredit(getOutstandingDebit(accountId, debitId), ywd, accountId, creditId, creditRemaining, creditType, owner);
        for(CustomerCreditBean creditBean : getOutstandingDebitExcludingCreditId(accountId, creditId)) {
            creditRemaining = setCredit(creditBean, ywd, accountId, creditId, creditRemaining, creditType, owner);
        }
        if(creditRemaining > 0) {
            //allocate the credit remain now all debit has been paid off
            setObject(accountId, new CustomerCreditBean(generateIdentifier(), accountId,
                    ywd, creditId, creditRemaining, creditType,
                    ObjectEnum.INITIALISATION.value(), ObjectEnum.INITIALISATION.value(), 0, CustomerCreditType.NO_VALUE, owner));
        }
     }

    /**
     * Sets a customer a customer debit against their credit. If no credit is found a debit is created. In the
     * event where credit exceeds the debit, a new CustomerCreditBean is created with the outstanding
     * credit. If The debit exceeds the credit then a new CustomerCreditBean is created with the outstanding debit.
     *
     * @param ywd
     * @param accountId
     * @param debitId
     * @param debit
     * @param debitType
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     * @throws MaxCountReachedException
     * @throws DuplicateIdentifierException
     * @throws NoSuchIdentifierException
     */
    public void setCustomerDebit(int ywd, int accountId, int debitId, long debit, CustomerCreditType debitType, String owner)
            throws PersistenceException, NullObjectException, MaxCountReachedException, DuplicateIdentifierException, NoSuchIdentifierException {
        if(isDebitIdUsed(accountId, debitId, debitType)) {
            throw new DuplicateIdentifierException("The debitId " + debitId + " has already been used for type " + debitType.toString());
        }
        long debitRemaining = debit;
        for(CustomerCreditBean creditBean : getOutstandingCredit(accountId)) {
            // debit is at least as great as credit available
            if(debitRemaining >= creditBean.getCredit()) {
                debitRemaining -= creditBean.getCredit();
                // set debit data
                creditBean.setDebit(creditBean.getCredit(), owner);
                creditBean.setDebitId(debitId, owner);
                creditBean.setDebitType(debitType, owner);
                creditBean.setDebitYwd(ywd, owner);
                // save debit data
                setObject(accountId, creditBean);
                // if there is no debit left then there is nothing more to be done
                if(debitRemaining == 0) {
                    return;
                }
            }
            // the debit is less than the credit
            else {
                long creditRemaining = creditBean.getCredit() - debitRemaining;
                // create a new CustomerCreditBean that is complete with the last of the debit, using new creditId if there is one
                setObject(accountId, new CustomerCreditBean(generateIdentifier(), accountId,
                        creditBean.getCreditYwd(), creditBean.getCreditId(), debitRemaining, creditBean.getCreditType(),
                        ywd, debitId, debitRemaining, debitType, owner));
                // keep the origional bean and ywd with reduced credit remaining, updating with new creditId if there is one
                creditBean.setCredit(creditRemaining, owner);
                // save
                setObject(accountId, creditBean);
                // as there is no credit left return
                return;
            }
        }
        //no credit so just create a new debit
        setObject(accountId, new CustomerCreditBean(generateIdentifier(), accountId,
                ObjectEnum.INITIALISATION.value(), ObjectEnum.INITIALISATION.value(), 0, CustomerCreditType.NO_VALUE,
                ywd, debitId, debitRemaining, debitType, owner));
    }

    /* ****************************************
     *  R E M O V E   M E T H O D S
     * ***************************************/
    /**
     * removes a Payment. If an payment also had a credit, the credit will remain but become outstanding
     *
     * @param accountId
     * @param debitId
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void removePayment(int accountId, int debitId, String owner) throws PersistenceException, NullObjectException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == debitId) {
                if(creditBean.getCreditId() == ObjectEnum.INITIALISATION.value()) {
                    removeObject(accountId, creditBean.getCreditId());
                } else {
                    creditBean.setDebitId(ObjectEnum.INITIALISATION.value(), owner);
                    creditBean.setDebit(0, owner);
                    setObject(accountId, creditBean);
                }
            }
        }
    }

    /**
     * removes a receipt. If the receipt is associated wit a debit then the debit remains but becomes outstanding
     *
     * @param accountId
     * @param creditId
     * @param owner
     * @throws PersistenceException
     * @throws NullObjectException
     */
    public void removeReceipt(int accountId, int creditId, String owner) throws PersistenceException, NullObjectException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == creditId) {
                if(creditBean.getDebitId() == ObjectEnum.INITIALISATION.value()) {
                    removeObject(accountId, creditBean.getCustomerCreditId());
                } else {
                    creditBean.setCreditId(ObjectEnum.INITIALISATION.value(), owner);
                    creditBean.setCredit(0, owner);
                    setObject(accountId, creditBean);
                }
            }
        }
    }


    /* ************************************************
     * P R I V A T E   M E T H O D S
     * ************************************************/
    public boolean isDebitIdUsed(int accountId, int debitId, CustomerCreditType type) throws PersistenceException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == debitId && creditBean.getDebitType() == type) {
                return (true);
            }
        }
        return (false);
    }

    public boolean isCreditIdUsed(int accountId, int creditId, CustomerCreditType type) throws PersistenceException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == creditId && creditBean.getCreditType() == type) {
                return (true);
            }
        }
        return (false);
    }

    private LinkedList<CustomerCreditBean> getOutstandingDebit(int accountId) throws PersistenceException {
        LinkedList<CustomerCreditBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == ObjectEnum.INITIALISATION.value()) {
                rtnList.add(creditBean);
            }
        }
        Collections.sort(rtnList);
        return rtnList;
    }

    private LinkedList<CustomerCreditBean> getOutstandingDebitExcludingCreditId(int accountId, int creditId) throws PersistenceException {
        LinkedList<CustomerCreditBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == ObjectEnum.INITIALISATION.value() && creditBean.getCreditId() != creditId) {
                rtnList.add(creditBean);
            }
        }
        Collections.sort(rtnList);
        return rtnList;
    }

    private CustomerCreditBean getOutstandingDebit(int accountId, int debitId) throws PersistenceException {
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getCreditId() == ObjectEnum.INITIALISATION.value() && creditBean.getDebitId() == debitId) {
                return(creditBean);
            }
        }
        return null;
    }

    private LinkedList<CustomerCreditBean> getOutstandingCredit(int accountId) throws PersistenceException {
        LinkedList<CustomerCreditBean> rtnList = new LinkedList<>();
        for(CustomerCreditBean creditBean : getAllObjects(accountId)) {
            if(creditBean.getDebitId() == ObjectEnum.INITIALISATION.value()) {
                rtnList.add(creditBean);
            }
        }
        Collections.sort(rtnList);
        return rtnList;
    }

    private long setCredit(CustomerCreditBean creditBean, int ywd, int accountId, int creditId, long credit, CustomerCreditType creditType, String owner) throws PersistenceException, NullObjectException, MaxCountReachedException {
        if(creditBean == null || credit == 0) {
            return credit;
        }
        long creditRemaining = credit;
        if(creditRemaining >= creditBean.getDebit()) {
            creditRemaining -= creditBean.getDebit();
            // set credit data
            creditBean.setCredit(creditBean.getDebit(), owner);
            creditBean.setCreditType(creditType, owner);
            creditBean.setCreditId(creditId, owner);
            creditBean.setCreditYwd(ywd, owner);
            // save data
            setObject(accountId, creditBean);
            // return the credit remaining (this could be zero)
            return creditRemaining;
        } else { // the credit is less than the debit
            long debitRemaining = creditBean.getDebit() - creditRemaining;
            // create a new CustomerCreditBean that is complete with the lastof the credit
            setObject(accountId, new CustomerCreditBean(generateIdentifier(), accountId,
                    ywd, creditId, creditRemaining, creditType,
                    creditBean.getDebitYwd(), creditBean.getDebitId(), creditRemaining, creditBean.getDebitType(), owner));
            // keep the origional bean and ywd with reduced debit remaining
            creditBean.setDebit(debitRemaining, owner);
            // now save this reduced customerCredit
            setObject(accountId, creditBean);
            // no credit remaining
            return 0;
        }
    }
}
