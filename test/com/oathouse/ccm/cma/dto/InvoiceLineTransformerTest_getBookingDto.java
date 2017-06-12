package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cos.bookings.BookingTypeManager;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;
import mockit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.*;
/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineTransformerTest_getBookingDto {
    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void runThrough() throws Exception {
        // the booking map
        final Map<Integer, List<BillingBean>> billingMap = new ConcurrentSkipListMap<>();
        // make up the account and children
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());

        final HashMap<String, String> bookingSet = new HashMap<>();
        bookingSet.put("id", Integer.toString(13));
        bookingSet.put("bookingSd", Integer.toString(SDHolder.getSD(10, 20)));
        bookingSet.put("ywd", Integer.toString(CalendarStatic.getRelativeYW(0)));
        final BookingBean booking1 = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), bookingSet);
        bookingSet.put("id", Integer.toString(17));
        bookingSet.put("bookingSd", Integer.toString(SDHolder.getSD(30, 10)));
        bookingSet.put("ywd", Integer.toString(CalendarStatic.getRelativeYW(0)));
        final BookingBean booking2 = (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), bookingSet);

        // make up the billing for the bookingId
        List<BillingBean> billingList1 = new LinkedList<>();
        HashMap<String, String> fieldSet = new HashMap<>();
        int seed = 1;
        fieldSet.put("id", Integer.toString(3));
        fieldSet.put("bookingId", Integer.toString(13));
        billingList1.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        fieldSet.put("id", Integer.toString(4));
        billingList1.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        billingMap.put(13, billingList1);
        List<BillingBean> billingList2 = new LinkedList<>();
        fieldSet.put("id", Integer.toString(5));
        fieldSet.put("bookingId", Integer.toString(17));
        billingList2.add((BillingBean) BeanBuilder.addBeanValues(new BillingBean(), seed++, fieldSet));
        billingMap.put(17, billingList2);

        new Expectations() {
            @Mocked private ChildBookingService bookingServiceMock;
            @Mocked private BookingManager bookingManagerMock;
            @Mocked private BookingTypeManager bookingTypeManagerMock;
            @Mocked private BookingTypeBean bookingTypeMock;
            {
                ChildBookingService.getInstance(); returns(bookingServiceMock);

                for(int bookingId : billingMap.keySet()) {
                    bookingServiceMock.getBookingManager(); returns(bookingManagerMock);
                    bookingManagerMock.getObject(bookingId);result = booking1; result = booking2;
                    bookingServiceMock.getBookingTypeManager(); returns(bookingTypeManagerMock);
                    bookingTypeManagerMock.getObject(anyInt); result = bookingTypeMock;
               }
            }
        };
        List<InvoiceLineDTO> result = InvoiceLineTransformer.getBookingDto(17, account, child, billingMap);
        assertThat(result.size(),is(2));
        assertThat(result.get(0).getLineItems().size(), is(2));
        assertThat(result.get(1).getLineItems().size(), is(1));
        assertThat(result.get(0).getBooking(), is(notNullValue()));
        assertThat(result.get(0).getBookingType(), is(notNullValue()));
        assertThat(result.get(0).getChild(), is(notNullValue()));
    }

    /*
     *
     */
    @Test
    public void emptyMap() throws Exception {
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());


        List<InvoiceLineDTO> result = InvoiceLineTransformer.getBookingDto(-1, account, child, null);
        assertThat(result.size(),is(0));
    }


}
