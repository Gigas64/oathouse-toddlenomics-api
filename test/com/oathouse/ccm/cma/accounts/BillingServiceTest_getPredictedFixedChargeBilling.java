/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingManager;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FIXED_CHILD_DISCOUNT;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.TYPE_FIXED_ITEM;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
import java.util.concurrent.ConcurrentSkipListMap;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class BillingServiceTest_getPredictedFixedChargeBilling {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    private final int accountId = 13;
    private final int lastYwd = CalendarStatic.getRelativeYW(4);

    @Mocked({"getAllBillingOutstanding", "setObjectDiscountId", "removeObject", "cloneObjectBean"})
    private BillingManager bmm;
    @Mocked({"createFixedChargeDiscount"})
    private BillingService bsm;


    @Before
    public void setUp() throws Exception {
        //clear out the attributes
        while(!attributes.isEmpty()) {
            attributes.clear();
        }
        // set up the properties
        String authority = ObjectBean.SYSTEM_OWNED;
        String rootStorePath = Paths.get("./oss/data").toString();
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(Paths.get(rootStorePath + "/conf/oss_log4j.properties").toString());
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = BillingService.getInstance();
        service.clear();

    }

    /*
     * a single fixed item with no discount
     */
    @Test
    public void getPredictedFixedChargeBilling_FixedItemNoDiscount() throws Exception {
        attributes.put("accountId", accountId);
        attributes.put("discountId", -1);
        final BillingBean billing = Builders.getBilling(1, attributes, TYPE_FIXED_ITEM);
        final List<BillingBean> billingList = new LinkedList<>(asList(billing));
        new Expectations() {
            {
                bmm.getAllBillingOutstanding(accountId, lastYwd, BillingEnum.TYPE_FIXED_ITEM); result = billingList; times = 1;
                bmm.setObjectDiscountId(accountId, billing.getBillingId(), -1, ObjectBean.SYSTEM_OWNED); times = 0;
                bmm.removeObject(accountId, anyInt); times = 0;
                bmm.cloneObjectBean(billing.getBillingId(), billing); result = billing; times = 1;
                bsm.createFixedChargeDiscount(accountId, billing, ObjectBean.SYSTEM_OWNED); times = 1;
                BillingManager.setObjectDiscountId(billing, -1, ObjectBean.SYSTEM_OWNED); times = 1;
            }
        };

        List<BillingBean> result = service.getPredictedFixedChargeBilling(accountId, lastYwd);
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(equalTo(billing)));
        assertThat(result.get(0).getDiscountId(), is(-1));
    }

    /*
     * a single fixed item with the discount already set and also returns a discount
     */
    @Test
    public void getPredictedFixedChargeBilling_FixedItemWithDiscount() throws Exception {
        attributes.put("accountId", accountId);
        attributes.put("discountId", 23);
        final BillingBean billing = Builders.getBilling(1, attributes, TYPE_FIXED_ITEM);
        final List<BillingBean> billingList = new LinkedList<>(asList(billing));
        attributes.put("discountId", -1);
        final BillingBean discount = Builders.getBilling(2, attributes, TYPE_FIXED_CHILD_DISCOUNT);
        attributes.put("discountId", discount.getBillingId());
        final BillingBean cloneBilling = Builders.getBilling(1, attributes, TYPE_FIXED_ITEM);
        new Expectations() {
            {
                bmm.getAllBillingOutstanding(accountId, lastYwd, BillingEnum.TYPE_FIXED_ITEM); result = billingList; times = 1;
                bmm.setObjectDiscountId(accountId, billing.getBillingId(), -1, ObjectBean.SYSTEM_OWNED); times = 1;
                bmm.removeObject(accountId, 23); times = 1;
                bmm.cloneObjectBean(billing.getBillingId(), billing); result = cloneBilling; times = 1;
                bsm.createFixedChargeDiscount(accountId, cloneBilling, ObjectBean.SYSTEM_OWNED); result = discount; times = 1;
                BillingManager.setObjectDiscountId(cloneBilling, discount.getBillingId(), ObjectBean.SYSTEM_OWNED); times = 1;
            }
        };

        List<BillingBean> result = service.getPredictedFixedChargeBilling(accountId, lastYwd);
        assertThat(result.size(), is(2));
        assertThat(result.get(0), is(equalTo(cloneBilling)));
        assertThat(result.get(0).getDiscountId(), is(discount.getBillingId()));
        assertThat(result.get(1), is(equalTo(discount)));
        assertThat(result.get(1).getDiscountId(), is(-1));
    }

}
