package com.oathouse.ccm.cma.accounts;

// common imports
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingManager;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
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
public class BillingServiceTest_setFixedItem {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private BillingService service;

    private final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();

    private final int accountId = 13;
    private final int profileId = 11;
    private final int ywd = CalendarStatic.getRelativeYW(0);

    @Mocked() private PropertiesService psm;
    @Mocked() private ChildService csm;

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
        // set up mocks
        final SystemPropertiesBean properties = Builders.getProperties();
        new NonStrictExpectations() {{
            PropertiesService.getInstance(); returns(psm);
            ChildService.getInstance(); returns(csm);
            // standard checks
            psm.getSystemProperties(); result = properties; times = 2;
            csm.isId(accountId, VT.ACCOUNT); result = true;
            csm.isId(profileId, VT.CHILD); result = true;
        }};
    }

    /*
     * BillingBits are standard
     */
    @Test
    public void setFixedItem_StandardBits() throws Exception {
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);

        BillingBean result = service.setFixedItem(1, ywd, accountId, profileId, 10000L, BILL_CHARGE, "", "", owner);
        assertThat(result, notNullValue());
        assertThat(result.getBillingBits(), is(billingBits));
    }

    /*
     * BillingBits are CREDIT
     */
    @Test
    public void setFixedItem_CreditBits() throws Exception {
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);

        BillingBean result = service.setFixedItem(1, ywd, accountId, profileId, 10000L, BILL_CREDIT, "", "", owner);
        assertThat(result, notNullValue());
        assertThat(result.getBillingBits(), is(billingBits));
    }

    /*
     * BillingBits are CREDIT
     */
    @Test
    public void setFixedItem_CreditBitsChangeBits() throws Exception {
        int billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM, BILL_CREDIT, CALC_AS_PERCENT, APPLY_DISCOUNT, RANGE_IGNORED, GROUP_FIXED_ITEM);

        BillingBean result = service.setFixedItem(1, ywd, accountId, profileId, 10000L, BILL_CREDIT, "", "", owner, CALC_AS_PERCENT, APPLY_DISCOUNT, TYPE_SESSION, GROUP_BOOKING);
        assertThat(result, notNullValue());
        assertThat(result.getBillingBits(), is(billingBits));
    }


}