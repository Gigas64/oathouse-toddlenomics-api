package com.oathouse.ccm.cos.accounts.transaction;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.oathouse.ccm.cos.accounts.transaction.CustomerCreditManagerTest.class,
    com.oathouse.ccm.cos.accounts.transaction.CustomerReceiptManagerTest.class,
    com.oathouse.ccm.cos.accounts.transaction.InvoiceCreditManagerTest.class,
    com.oathouse.ccm.cos.accounts.transaction.PaymentManagerTest.class
})
public class A_Suite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
}