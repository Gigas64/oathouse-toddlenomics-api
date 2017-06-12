/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.io.File;
import java.util.*;
import static java.util.Arrays.*;
import java.util.concurrent.ConcurrentHashMap;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineDTOTest_getLineItems {
    private final int invoiceId = 17;

    /*
     *
     */
    @Test
    public void addEverythingThenFilter() throws Exception {
        int accountId = InvoiceLineDTO_scenario.accountId;
        int childId = InvoiceLineDTO_scenario.childId;

        int bookingSd = SDHolder.buildSD(10, 40);
        int discountRate = 500;
        List<BillingBean> billingList = InvoiceLineDTO_scenario.createReconciledBillingList(bookingSd, discountRate, invoiceId);
        ConcurrentHashMap<Integer, List<InvoiceLineDTO>> billingMap = InvoiceLineTransformer.toDTO(accountId, billingList);
        List<InvoiceLineDTO> childList = billingMap.get(childId);
        assertThat(childList.size(), is(3));
        List<InvoiceLineDTO> accountList = billingMap.get(-1);
        assertThat(accountList.size(), is(1));

        InvoiceLineDTO bookingLineDTO = childList.get(0);
        testBooking(bookingLineDTO);

        InvoiceLineDTO loyaltyLineDTO = childList.get(1);
        testLoyalty(loyaltyLineDTO);

        InvoiceLineDTO fixedChildLineDTO = childList.get(2);
        testFixedChild(fixedChildLineDTO);

        InvoiceLineDTO fixedAccountLineDTO = accountList.get(0);
        testFixedAccount(fixedAccountLineDTO);
    }

    private void testBooking(InvoiceLineDTO bookingLineDTO) {
        assertThat(bookingLineDTO.getGroup(), is(InvoiceLineDTO.BOOKING_ADJUSTMENT));
        assertThat(bookingLineDTO.getListPriceValue(), is(67000L));
        assertThat(bookingLineDTO.getListPriceValue(invoiceId - 1), is(60000L));
        assertThat(bookingLineDTO.getListPriceValue(invoiceId), is(7000L));
        assertThat(bookingLineDTO.getListPriceValue(-1), is(0L));
        assertThat(bookingLineDTO.getPenaltyValue(), is(2000L));
        assertThat(bookingLineDTO.getAdjustmentValue(), is(-28000L));
    }

    private void testLoyalty(InvoiceLineDTO loyaltyLineDTO) {
        assertThat(loyaltyLineDTO.getGroup(), is(InvoiceLineDTO.LOYALTY_DISCOUNT));
        assertThat(loyaltyLineDTO.getListPriceValue(), is(0L));
    }

    private void testFixedChild(InvoiceLineDTO fixedChildLineDTO) {
        assertThat(fixedChildLineDTO.getGroup(), is(InvoiceLineDTO.FIXED_ITEM_CHILD));
        assertThat(fixedChildLineDTO.getListPriceValue(), is(3000L));
        assertThat(fixedChildLineDTO.getListPriceValue(invoiceId), is(3000L));
        assertThat(fixedChildLineDTO.getListPriceValue(-1), is(0L));
    }

    private void testFixedAccount(InvoiceLineDTO fixedAccountLineDTO) {
        assertThat(fixedAccountLineDTO.getGroup(), is(InvoiceLineDTO.FIXED_ITEM_ACCOUNT));
        assertThat(fixedAccountLineDTO.getListPriceValue(), is(-1500L));
        assertThat(fixedAccountLineDTO.getListPriceValue(invoiceId), is(-1500L));
        assertThat(fixedAccountLineDTO.getListPriceValue(-1), is(0L));
    }

    private void printMap(Map<Integer, List<InvoiceLineDTO>> billingMap) {
        for(int childId : billingMap.keySet()) {
            System.out.println("ChildId : " +childId);
            for(InvoiceLineDTO dto : billingMap.get(childId)) {
                System.out.println(dto.toString());
            }
        }
    }
}
