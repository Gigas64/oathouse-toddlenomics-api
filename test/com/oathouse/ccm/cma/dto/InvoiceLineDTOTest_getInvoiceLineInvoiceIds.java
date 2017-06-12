/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
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
public class InvoiceLineDTOTest_getInvoiceLineInvoiceIds {

    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void getAllInvoiceIds() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        Integer[] result = invoiceLine.getLineItemInvoiceIds();
        assertThat(result.length, is(2));
        assertThat(result[0], is(13));
        assertThat(result[1], is(17));
    }

    /*
     *
     */
    @Test
    public void getSelectedInvoiceId() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        Integer[] result = invoiceLine.getLineItemInvoiceIds(17);
        assertThat(result.length, is(1));
        assertThat(result[0], is(13));
    }

    /*
     *
     */
    @Test
    public void testExistingInvoiceAndNonExisting() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        Integer[] result = invoiceLine.getLineItemInvoiceIds(17, 23);
        assertThat(result.length, is(1));
        assertThat(result[0], is(13));
    }

    /*
     *
     */
    @Test
    public void testBothNonExisting() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        Integer[] result = invoiceLine.getLineItemInvoiceIds(23,29);
        assertThat(result.length, is(2));
        assertThat(result[0], is(13));
        assertThat(result[1], is(17));
    }

    /*
     *
     */
    @Test
    public void testAllExisting() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        Integer[] result = invoiceLine.getLineItemInvoiceIds(13,17);
        assertThat(result.length, is(0));
    }

}
