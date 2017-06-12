/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BillingServiceTest_getPredictedFixedChargeBilling.class,
    BillingServiceTest_getLineBillingForInvoice.class,
    BillingServiceTest_createMethods.class,
    BillingServiceTest_createSessionCharge.class,
    BillingServiceTest_setFixedItem.class,
    BillingServiceTest_multiDayLoylaty.class,
    TransactionServiceTest_removeAccountTransactions.class,
    BillingServiceTest_calculateLoyaltyDiscountSuccessCriteria.class,
    TransactionServiceTest_setCustomerReceipt.class,
    BillingServiceTest_calculateLoyaltyDiscountValue.class,
    BillingServiceTest_getPredictedBillingsForAccount.class,
    BillingServiceTest_verifyTotalValue.class,
    BillingServiceTest_helperMethods.class,
    BillingServiceTest_getPredictedLineBillingsForAccount.class,
    BillingServiceTest_getCheckedSd.class,
    TransactionServiceTest_isInvoiceRequired.class,
    BillingServiceTest_getPredictedBillingsForBooking.class,
    BillingServiceTest_createEducationReduction.class,
    TransactionServiceTest_setPayment.class,
    BillingServiceTest_removeBilling.class,
    BillingServiceTest_calculateLoyaltyDiscountDayBookingBillingTotals.class,
    BillingServiceTest_createBookingLoyaltyDiscounts.class,
})
public class AccountPackageSuite {

}
