package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.oss.storage.objectstore.*;
import java.util.*;
import java.util.concurrent.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineTransformerTest_getLoyaltyDto {

    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void runThrough() throws Exception {
        // the loylaty map
        final Map<Integer, List<BillingBean>> loyaltyMap = new ConcurrentSkipListMap<>();
        // make up the children
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        // make up the billing for the loyaltyId
        List<BillingBean> billingList = new LinkedList<>();
        HashMap<String, String> fieldSet = new HashMap<>();
        int seed = 1;
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("bookingId", Integer.toString(13)); // this is the loyalty Id
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(4));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        loyaltyMap.put(13, billingList);
        billingList = new LinkedList<>();
        fieldSet.put("id", Integer.toString(5));
        fieldSet.put("bookingId", Integer.toString(17)); // this is the loyalty Id
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        loyaltyMap.put(17, billingList);

        // run the method.
        List<InvoiceLineDTO> result = InvoiceLineTransformer.getLoyaltyDto(17, account, child, loyaltyMap);
        assertThat(result.size(),is(2));
        assertThat(result.get(0).getLineItems().size(), is(2));
        assertThat(result.get(1).getLineItems().size(), is(1));
        assertThat(result.get(0).getBooking(), is(nullValue()));
        assertThat(result.get(0).getBookingType(), is(nullValue()));
        assertThat(result.get(0).getChild(), is(notNullValue()));
    }



}
