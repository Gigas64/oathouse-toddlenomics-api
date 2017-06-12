/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
import java.util.*;
import static java.util.Arrays.*;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineDTOTest_getAllLineItemsValues {

    @Before
    public void setUp() {

    }

    /*
     * test the generic method
     */
    @Test
    public void testGenericMethod() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        assertThat(InvoiceLineDTO.getAllLineItemsValue(invoiceLine.getLineItems(TYPE_SESSION)), is(110L));
    }

        /*
     *
     */
    @Test
    public void testGenericMethodForBookingWithWrongTypes() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        assertThat(InvoiceLineDTO.getAllLineItemsValue(invoiceLine.getLineItems(TYPE_LOYALTY)), is(0L));
    }

    /*
     *
     */
    @Test
    public void testGenericMethodForLoyalty() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getLoyaltyInvoiceLineDTO();
        assertThat(InvoiceLineDTO.getAllLineItemsValue(invoiceLine.getLineItems(TYPE_LOYALTY)), is(-80L));
    }

    /*
     *
     */
    @Test
    public void testBookingByInvoice() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        //no invoice first to check it works
        assertThat(invoiceLine.getListPriceValue(), is(110L));
        // only invoice 17
        assertThat(invoiceLine.getListPriceValue(17), is(10L));
        // all adjustments
        assertThat(invoiceLine.getAdjustmentValue(17), is(-80L));
    }

    /*
     *
     */
    @Test
    public void testLoyaltyByInvoice() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getLoyaltyInvoiceLineDTO();
        //no invoice first to check it works
        assertThat(invoiceLine.getLineItemsValue(), is(-80L));
        // only invoice 17
        assertThat(invoiceLine.getLineItemsValue(17), is(-30L));
    }

}
