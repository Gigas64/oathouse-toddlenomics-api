package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.profile.AccountManager;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.ChildManager;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;
import mockit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineTransformerTest_toDTO {
    private static int ywd = CalendarStatic.getRelativeYW(0);
    private static int ywdNext = CalendarStatic.getRelativeYW(1);
    private static int invoiceId = 13;

    @Before
    public void setUp() throws Exception {

    }

    /*
     *
     */
    @Test
    public void singleChild_oneOfEachType() throws Exception {

        final int childId = 17;
        final int bookingId = 19;
        final int loyaltyId = 23;
        final AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());

        //set up the list passed in. This will determin what gets to the deligate methods
        final List<BillingBean> billingList = new LinkedList<>();
        int seed = 1;
        HashMap<String, String> fieldSet = new HashMap<>();
        fieldSet.put("id", Integer.toString(1));
        fieldSet.put("ywd", Integer.toString(ywd));
        fieldSet.put("accountId", Integer.toString(account.getAccountId()));
        fieldSet.put("profileId", Integer.toString(childId));
        fieldSet.put("bookingId", Integer.toString(bookingId));
        fieldSet.put("discountId", Integer.toString(-1));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(BillingEnum.TYPE_ADJUSTMENT_ON_ATTENDING)));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(2));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(BillingEnum.TYPE_LOYALTY)));
        fieldSet.put("bookingId", Integer.toString(loyaltyId));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(BillingEnum.TYPE_FIXED_ITEM)));
        fieldSet.put("bookingId", Integer.toString(-1));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(4));
        fieldSet.put("profileId", Integer.toString(-1));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(BillingEnum.TYPE_FIXED_ITEM)));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(5));
        fieldSet.put("discountId", Integer.toString(billingList.get(3).getBillingId()));
        fieldSet.put("billingBits", Integer.toString(BillingEnum.getBillingBits(BillingEnum.TYPE_FIXED_ACCOUNT_DISCOUNT)));
        billingList.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));

        // Expectations of the delagates
        final ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        // bookingMap
        final Map<Integer, List<BillingBean>> bookingMap = new ConcurrentSkipListMap<>();
        bookingMap.put(bookingId, Arrays.asList(billingList.get(0)));
        // loyaltyMap
        final Map<Integer, List<BillingBean>> loyaltyMap = new ConcurrentSkipListMap<>();
        loyaltyMap.put(loyaltyId, Arrays.asList(billingList.get(1)));
        // fixedChildMap
        final Map<Integer, List<BillingBean>> fixedChildMap = new ConcurrentSkipListMap<>();
        fixedChildMap.put(billingList.get(2).getBillingId(), Arrays.asList(billingList.get(2)));
        //fixedAccountMap
        final Map<Integer, List<BillingBean>> fixedAccountMap = new ConcurrentSkipListMap<>();
        fixedAccountMap.put(billingList.get(3).getBillingId(), Arrays.asList(billingList.get(3), billingList.get(4)));

        new Expectations() {
            @Mocked private ChildService childServiceMock;
            @Mocked private ChildManager childManagerMock;
            @Mocked private AccountManager accountManagerMock;
            @Mocked({"getBookingDto", "getLoyaltyDto", "getFixedItemChildDto", "getFixedItemAccountDto"}) private InvoiceLineTransformer notUsed = null;
            {
                ChildService.getInstance(); returns(childServiceMock);
                // first loop childId = 17
                childServiceMock.getAccountManager(); returns(accountManagerMock);
                accountManagerMock.getObject(account.getAccountId()); returns(account);
                childServiceMock.getChildManager(); returns(childManagerMock);
                childManagerMock.getObject(childId); result = child;
                InvoiceLineTransformer.getBookingDto(invoiceId, account, child, bookingMap);
                result = new InvoiceLineTransformerDelegate();
                InvoiceLineTransformer.getLoyaltyDto(invoiceId, account, child, loyaltyMap);
                result = new InvoiceLineTransformerDelegate();
                InvoiceLineTransformer.getFixedItemDto(invoiceId, account, child, fixedChildMap);
                result = new InvoiceLineTransformerDelegate();
                // second loop childId = -1
                childServiceMock.getAccountManager(); returns(accountManagerMock);
                accountManagerMock.getObject(account.getAccountId()); returns(account);
                InvoiceLineTransformer.getFixedItemDto(invoiceId, account, null, fixedAccountMap);
                result = new InvoiceLineTransformerDelegate();
            }
        };
        ConcurrentHashMap<Integer, List<InvoiceLineDTO>> result = InvoiceLineTransformer.toDTO(invoiceId, billingList);
        assertThat(result.keySet().size(), is(2));
        assertThat(result.keySet().contains(childId), is(true));
        //these were generated by the Deligate so test sorted
        assertThat(result.get(childId).size(), is(4));
        assertThat(result.get(childId).get(0).getYwd(), is(ywd));
        assertThat(result.get(childId).get(0).getDescription(), is("Booking1"));
        assertThat(result.get(childId).get(3).getYwd(), is(ywdNext));
        assertThat(result.get(childId).get(3).getDescription(), is("Booking2"));


    }

    /*
     * Deligate inner class
     */
    private static final class InvoiceLineTransformerDelegate implements Delegate<InvoiceLineTransformer> {

        protected static List<InvoiceLineDTO> getBookingDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap) throws PersistenceException, NoSuchIdentifierException {
            List<InvoiceLineDTO> rtnList = new LinkedList<>();
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.BOOKING_ADJUSTMENT, 1, "Booking1", account, child, new BookingBean(), new BookingTypeBean(), new LinkedList<LineItemDTO>()));
            rtnList.add(new InvoiceLineDTO(invoiceId, ywdNext, InvoiceLineDTO.BOOKING_ADJUSTMENT, 1, "Booking2", account, child, new BookingBean(), new BookingTypeBean(), new LinkedList<LineItemDTO>()));
            return rtnList;
        }

        protected static List<InvoiceLineDTO> getLoyaltyDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap) {
            List<InvoiceLineDTO> rtnList = new LinkedList<>();
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.LOYALTY_DISCOUNT, 2, "Loyalty", account, child, null, null, new LinkedList<LineItemDTO>()));
            return rtnList;
        }

        protected static List<InvoiceLineDTO> getFixedItemChildDto(int invoiceId, AccountBean account, ChildBean child, Map<Integer,List<BillingBean>> billingMap) {
            List<InvoiceLineDTO> rtnList = new LinkedList<>();
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.FIXED_ITEM_CHILD, -1, "FixedItemChild", account, child, null, null, new LinkedList<LineItemDTO>()));
            return rtnList;
        }

        protected static List<InvoiceLineDTO> getFixedItemAccountDto(int invoiceId, AccountBean account, Map<Integer,List<BillingBean>> billingMap) {
            List<InvoiceLineDTO> rtnList = new LinkedList<>();
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.FIXED_ITEM_ACCOUNT, -1, "FixedItemAccount", account, null, null, null, new LinkedList<LineItemDTO>()));
            rtnList.add(new InvoiceLineDTO(invoiceId, ywd, InvoiceLineDTO.FIXED_ITEM_ACCOUNT, -1, "FixedItemAccount", account, null, null, null, new LinkedList<LineItemDTO>()));
            return rtnList;
        }

        private InvoiceLineTransformerDelegate() {
        }
    }

}
