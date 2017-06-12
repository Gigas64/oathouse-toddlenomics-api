package com.oathouse.ccm.cos.config.finance;

import com.oathouse.ccm.cma.VT;
import com.oathouse.oss.storage.objectstore.BuildBeanTester;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.server.OssProperties;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.List;
import java.io.File;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class LoyaltyDiscountManagerTest {

    private LoyaltyDiscountManager manager;

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
        manager = null;
        // create new manager
        manager = new LoyaltyDiscountManager("LoyaltyConfigManager");
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        // manager.clear();
    }

    /**
     * Unit test: Underlying bean is correctly formed.
     */
    @Test
    public void unit01_LoyaltyConfig() throws Exception {
        BuildBeanTester.testObjectBean("com.oathouse.ccm.cos.config.finance." + VT.LOYALTY_DISCOUNT.bean(), false);
    }

    /*
     *
     */
    @Test
    public void test01_getCompleteBillingBits() throws Exception {
        int billingBits;
        int resultBits;

        // nothing set
        billingBits = 0;
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // TYPE changed
        billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // BILL changed
        billingBits = BillingEnum.getBillingBits(BILL_CHARGE);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // CALC changed
        billingBits = BillingEnum.getBillingBits(CALC_AS_PERCENT);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_PERCENT, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // APPLY changed
        billingBits = BillingEnum.getBillingBits(APPLY_DISCOUNT);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // RANGE changed
        billingBits = BillingEnum.getBillingBits(RANGE_SUM_TOTAL);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_SUM_TOTAL, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
        // ALL changed
        billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_SUM_TOTAL, GROUP_BOOKING);
        resultBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_SUM_TOTAL, GROUP_LOYALTY);
        assertThat(LoyaltyDiscountManager.getCompleteBillingBits(billingBits), is(resultBits));
    }

    /*
     * utility private method when debugging to print all objects in a manger
     */
    private void printAll() throws Exception {
        for(LoyaltyDiscountBean bean : manager.getAllObjects()) {
            System.out.println(bean.toString());
        }
    }

    /*
     * utility private method to test the order of the beans in the manager
     */
    private void testOrder(int[] order) throws Exception {
        testOrder(order, manager.getAllObjects());
    }


    /*
     * utility private method to test the order of beans for a given list
     */
    private void testOrder(int[] order, List<LoyaltyDiscountBean> list) throws Exception {
        assertEquals("Testing manager size", order.length, list.size());
        for(int i = 0; i < order.length; i++) {
            assertEquals("Testing manager bean order [" + i + "]", order[i], list.get(i).getIdentifier());
        }
    }
}
