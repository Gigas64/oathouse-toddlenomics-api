package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.accounts.TDCalc;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.*;
/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineDTOTest_getInvoiceValues {

    @Before
    public void setUp() {

    }

    /*
     *
     */
    @Test
    public void testTheValuesAreCorrect() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        long value = 100 + 10 - 80;
        long tax = TDCalc.getTax(value, InvoiceLineDTOTest.TAX_RATE);
        long incTax = value + tax;

        long[] result = invoiceLine.getSumValueArray();
        assertThat(result[0], is(incTax));
        assertThat(result[1], is(value));
        assertThat(result[2], is(tax));
    }

    /*
     *
     */
    @Test
    public void testTheInvoiceFilter() throws Exception {
        InvoiceLineDTO invoiceLine = InvoiceLineDTOTest.getBookingInvoiceLineDTO();

        long value = 10 - 80;
        long tax = TDCalc.getTax(value, InvoiceLineDTOTest.TAX_RATE);
        long incTax = value - tax;

        long[] result = invoiceLine.getSumValueArray(17);
        assertThat(result[0], is(incTax));
        assertThat(result[1], is(value));
        assertThat(result[2], is(tax));
    }

}
