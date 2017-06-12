package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.*;
/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineTransformerTest_getFixedItemAccountDto {
    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void runThrough() throws Exception {
        final int ywd = CalendarStatic.getRelativeYW(0);
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        final Map<Integer, List<BillingBean>> billingMap = new ConcurrentSkipListMap<>();
        billingMap.put(1, new LinkedList<BillingBean>());
        billingMap.put(2, new LinkedList<BillingBean>());
        HashMap<String, String> fieldSet = new HashMap<>();
        int seed = 1;
        fieldSet.put("id", Integer.toString(1));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("discountId", Integer.toString(-1));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(TYPE_FIXED_ITEM)));
        billingMap.get(1).add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(2));
        billingMap.get(2).add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("discountId", Integer.toString(1));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(TYPE_FIXED_CHILD_DISCOUNT)));
        billingMap.get(1).add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));

        List<InvoiceLineDTO> result = InvoiceLineTransformer.getFixedItemDto(-1, account, null, billingMap);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getBooking(), is(nullValue()));
        assertThat(result.get(0).getBookingType(), is(nullValue()));
        assertThat(result.get(0).getChild(), is(nullValue()));
        assertThat(result.get(0).getLineItems().size(), is(2));
        assertThat(result.get(0).getLineItems().get(0).getBillingBits(), is(BillingEnum.getBillingBits(TYPE_FIXED_ITEM)));
        assertThat(result.get(0).getLineItems().get(1).getBillingBits(), is(BillingEnum.getBillingBits(TYPE_FIXED_CHILD_DISCOUNT)));

    }

    /*
     *
     */
    @Test
    public void withoutBillingItems() throws Exception {
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        final Map<Integer, List<BillingBean>> billingMap = new ConcurrentSkipListMap<>();

        List<InvoiceLineDTO> result = InvoiceLineTransformer.getFixedItemDto(-1, account, null, billingMap);
        assertThat(result.size(), is(0));
    }

    /*
     *
     */
    @Test
    public void nullBillingItems() throws Exception {
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        final Map<Integer, List<BillingBean>> billingMap = null;

        List<InvoiceLineDTO> result = InvoiceLineTransformer.getFixedItemDto(-1, account, null, billingMap);
        assertThat(result.size(), is(0));
    }

}
