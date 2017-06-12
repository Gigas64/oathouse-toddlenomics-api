package com.oathouse.ccm.cma.dto;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InvoiceLineTransformerTest_feeInvoiceToDTO.class,
    InvoiceLineTransformerTest_getBookingDto.class,
    InvoiceLineTransformerTest_getFixedItemAccountDto.class,
    InvoiceLineTransformerTest_getFixedItemChildDto.class,
    InvoiceLineTransformerTest_getLoyaltyDto.class,
    InvoiceLineTransformerTest_toDTO.class
})
public class InvoiceLineTransformerTest {
}
