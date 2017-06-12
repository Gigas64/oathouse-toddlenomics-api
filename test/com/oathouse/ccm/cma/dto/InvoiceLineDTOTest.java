package com.oathouse.ccm.cma.dto;

import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import org.junit.runner.*;
import org.junit.runners.*;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InvoiceLineDTOTest_getAllLineItemsByType.class,
    InvoiceLineDTOTest_getAllLineItemsValues.class,
    InvoiceLineDTOTest_getInvoiceLineInvoiceIds.class,
    InvoiceLineDTOTest_getInvoiceValues.class,
    InvoiceLineDTOTest_getLineItems.class,
})
public class InvoiceLineDTOTest {

    protected static final String SESSION = "Session";
    protected static final String FUNDED = "Funded";
    protected static final String ESTIMATE = "Estimate";
    protected static final String RECONCILED = "Reconcile";
    protected static final String FIXED = "Fixed";

    protected static final int TAX_RATE = 20;

    //<editor-fold defaultstate="collapsed" desc="InvoiceLineDTO creators">

    protected static InvoiceLineDTO getBookingInvoiceLineDTO() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int group = InvoiceLineDTO.BOOKING_ADJUSTMENT;
        int orderStart = 10;
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        BookingBean booking = (BookingBean) BeanBuilder.addBeanValues(new BookingBean());
        BookingTypeBean bookingType = (BookingTypeBean) BeanBuilder.addBeanValues(new BookingTypeBean());
        List<LineItemDTO> lineItemList = new LinkedList<>();

        // lineItem variables
        int billingSd, taxRate, discountId, billingBits, invoiceId;
        long value;

        // pre-paid session
        billingSd = SDHolder.getSD(orderStart, 30);
        value = 100;
        taxRate = TAX_RATE;
        discountId = -1;
        billingBits = BillingEnum.getBillingBits(TYPE_SESSION,BILL_CHARGE,APPLY_DISCOUNT);
        invoiceId = 13;
        lineItemList.add(new LineItemDTO(SESSION, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));

        // session adjustment
        billingSd = SDHolder.getSD(orderStart, 30);
        value = 10;
        taxRate = TAX_RATE;
        billingBits = BillingEnum.getBillingBits(TYPE_SESSION,BILL_CHARGE,APPLY_DISCOUNT);
        invoiceId = 17;
        lineItemList.add(new LineItemDTO(SESSION, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));


        // funded adjustment
        billingSd = SDHolder.getSD(orderStart, 30);
        value = 80;
        taxRate = TAX_RATE;
        billingBits = BillingEnum.getBillingBits(TYPE_FUNDED,BILL_CREDIT,APPLY_NO_DISCOUNT);
        invoiceId = 17;
        lineItemList.add(new LineItemDTO(FUNDED, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));
        // return the InvoiceLineDTO
        return(new InvoiceLineDTO(17, ywd, group, orderStart, "", account, child, booking, bookingType, lineItemList));
    }

    protected static InvoiceLineDTO getLoyaltyInvoiceLineDTO() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int group = InvoiceLineDTO.LOYALTY_DISCOUNT;
        int orderStart = 10;
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        BookingBean booking = null;
        BookingTypeBean bookingType = null;
        List<LineItemDTO> lineItemList = new LinkedList<>();

        // lineItem variables
        int billingSd, taxRate, discountId, billingBits, invoiceId;
        long value;

        // pre-paid session
        billingSd = SDHolder.getSD(orderStart, 30);
        value = 50;
        taxRate = TAX_RATE;
        discountId = -1;
        billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY,BILL_CREDIT,APPLY_NO_DISCOUNT);
        invoiceId = 13;
        lineItemList.add(new LineItemDTO(ESTIMATE, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));

        // session adjustment
        billingSd = SDHolder.getSD(orderStart, 30);
        value = 30;
        taxRate = TAX_RATE;
        billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY,BILL_CREDIT,APPLY_NO_DISCOUNT);
        invoiceId = 17;
        lineItemList.add(new LineItemDTO(RECONCILED, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));
        // return the InvoiceLineDTO
        return(new InvoiceLineDTO(17, ywd, group, orderStart, "", account, child, booking, bookingType, lineItemList));
    }

    protected static InvoiceLineDTO getFixedChildInvoiceLineDTO() throws Exception {
        int ywd = CalendarStatic.getRelativeYW(0);
        int group = InvoiceLineDTO.BOOKING_ADJUSTMENT;
        int orderStart = 10;
        ChildBean child = (ChildBean) BeanBuilder.addBeanValues(new ChildBean());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean());
        BookingBean booking = null;
        BookingTypeBean bookingType = null;
        List<LineItemDTO> lineItemList = new LinkedList<>();

        // lineItem variables
        int billingSd, taxRate, discountId, billingBits, invoiceId;
        long value;

        billingSd = SDHolder.getSD(orderStart, 30);
        value = 30;
        taxRate = TAX_RATE;
        discountId = -1;
        billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM,BILL_CHARGE,APPLY_DISCOUNT);
        invoiceId = 13;
        lineItemList.add(new LineItemDTO(FIXED, billingSd, value, taxRate, billingBits, invoiceId, discountId, ""));
        // return the InvoiceLineDTO
        return(new InvoiceLineDTO(13, ywd, group, orderStart, "", account, child, booking, bookingType, lineItemList));
    }
    //</editor-fold>

    private InvoiceLineDTOTest() {
    }

}
