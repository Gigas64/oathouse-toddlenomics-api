/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cos.accounts.finance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.oathouse.ccm.cos.accounts.finance.BillingManagerTest_managerMethods.class,
    com.oathouse.ccm.cos.accounts.finance.BillingManagerTest_setObjectOverride.class,
    com.oathouse.ccm.cos.accounts.finance.BillingManagerTest_removeObjectOverride.class,
    com.oathouse.ccm.cos.accounts.finance.BillingManagerTest_beanMethods.class
})
public class BillingManagerTest {

}
