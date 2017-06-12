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
public class InvoiceLineDTOTest_getAllLineItemsByType {

    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void testGenericMethodForBookingWithWrongTypes() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        List<LineItemDTO> result = invoiceLine.getLineItems(TYPE_FIXED_ITEM);
        assertThat(result.size(), is(0));
    }

    /*
     *
     */
    @Test
    public void testGenericMethodForBooking() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        List<LineItemDTO> result = invoiceLine.getLineItems(TYPE_SESSION);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.SESSION));
        assertThat(result.get(1).getDescription(), is(InvoiceLineDTOTest.SESSION));
        assertThat(result.get(0).getInvoiceId(), is(13));
        assertThat(result.get(1).getInvoiceId(), is(17));
    }

    /*
     *
     */
    @Test
    public void testGenericMethodForLoyalty() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getLoyaltyInvoiceLineDTO();
        List<LineItemDTO> result = invoiceLine.getLineItems(TYPE_LOYALTY);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.ESTIMATE));
        assertThat(result.get(1).getDescription(), is(InvoiceLineDTOTest.RECONCILED));
        assertThat(result.get(0).getInvoiceId(), is(13));
        assertThat(result.get(1).getInvoiceId(), is(17));
    }

    /*
     *
     */
    @Test
    public void testBookingByInvoice() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();
        //no invoice first to check it works
        List<LineItemDTO> result = invoiceLine.getListPriceItems();
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.SESSION));
        assertThat(result.get(1).getDescription(), is(InvoiceLineDTOTest.SESSION));
        assertThat(result.get(0).getInvoiceId(), is(13));
        assertThat(result.get(1).getInvoiceId(), is(17));
        // only invoice 17
        result = invoiceLine.getListPriceItems(17);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.SESSION));
        assertThat(result.get(0).getInvoiceId(), is(17));
        // all adjustments
        result = invoiceLine.getEducationReductionItems(17);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.FUNDED));
        assertThat(result.get(0).getInvoiceId(), is(17));
    }

    /*
     *
     */
    @Test
    public void testLoyaltyByInvoice() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getLoyaltyInvoiceLineDTO();
        //no invoice first to check it works
        List<LineItemDTO> result = invoiceLine.getLineItems();
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.ESTIMATE));
        assertThat(result.get(1).getDescription(), is(InvoiceLineDTOTest.RECONCILED));
        assertThat(result.get(0).getInvoiceId(), is(13));
        assertThat(result.get(1).getInvoiceId(), is(17));
        // only invoice 17
        result = invoiceLine.getLineItems(17);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.RECONCILED));
        assertThat(result.get(0).getInvoiceId(), is(17));
    }

    /*
     *
     */
    @Test
    public void testInvoiceIdNotExist() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getFixedChildInvoiceLineDTO();
        //no invoice first to check it works
        List<LineItemDTO> result = invoiceLine.getLineItems(13,23);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDescription(), is(InvoiceLineDTOTest.FIXED));
        assertThat(result.get(0).getInvoiceId(), is(13));
    }

}
