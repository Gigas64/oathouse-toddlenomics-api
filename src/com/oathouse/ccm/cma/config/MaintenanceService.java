/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)MaintenanceService.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.config;

import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.accounts.TransactionService;
import com.oathouse.ccm.cma.booking.ChildBookingHistory;
import com.oathouse.ccm.cma.booking.ChildBookingRequestService;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.accounts.invoice.InvoiceBean;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.ContactBean;
import com.oathouse.ccm.cos.profile.RelationType;
import com.oathouse.oss.storage.exceptions.IllegalActionException;
import com.oathouse.oss.storage.exceptions.IllegalValueException;
import com.oathouse.oss.storage.exceptions.MaxCountReachedException;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.YWDHolder;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@literal MaintenanceService} is used to provide a cental maintenance reference sit
 * @author Darryl Oatridge
 * @version 1.00 23-Jul-2013
 */
public class MaintenanceService {

    /**
     * Maintenance Method to tidy up any stray children that do not have associated parent or guardian.
     * The method looks for children that have no entry in the parent guardian manager.
     *
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     * @throws NullObjectException
     */
    public static void tidyChildCustodianRelation() throws PersistenceException, NoSuchIdentifierException, NullObjectException {
        ChildService cs = ChildService.getInstance();
        Set<Integer> childRelationSet = cs.getCustodialRelationship().getAllKeys();
        for(ChildBean child : cs.getChildManager().getAllObjects()) {
            int childId = child.getChildId();
            if(childRelationSet.contains(childId) && !cs.getAllCustodiansForChild(childId).isEmpty()) {
                continue;
            }
            for(ContactBean contact : cs.getLiableContactsForAccount(childId)) {
                cs.addRelationship(childId, contact.getContactId(), RelationType.LEGAL_PARENT_GUARDIAN, ObjectBean.SYSTEM_OWNED);
            }
        }
    }

    /**
     * Maintenance Method to tidy up the system DayRange managers by removing
     * old DayRange objects where the end date is older than the date passed.
     *
     * @param ywd
     * @throws PersistenceException
     * @throws com.oathouse.oss.storage.exceptions.IllegalValueException
     */
    public static void tidyDayRangeManagers(int ywd) throws PersistenceException, IllegalValueException {
        TimetableService ts = TimetableService.getInstance();
        ChildBookingRequestService cbrs = ChildBookingRequestService.getInstance();
        ChildService cs = ChildService.getInstance();
        PriceConfigService pcs = PriceConfigService.getInstance();

        // make sure it is the start of a week
        int yw0 = YWDHolder.isValid(ywd) && CalendarStatic.getRelativeYW(-12) > ywd ? YWDHolder.getYW(ywd) : CalendarStatic.getRelativeYW(-12);
        // timetable
        ts.getDayRangeManager().tidyDayRangeObjects(yw0);
        ts.getEducationRangeManager().tidyDayRangeObjects(yw0);
        // child
        cs.getChildEducationRangeManager().tidyDayRangeObjects(yw0);
        // booking requests
        cbrs.getBookingRequestRangeManager().tidyDayRangeObjects(yw0);
        // pricing
        pcs.getChildEducationPriceReductionRangeManager().tidyDayRangeObjects(yw0);
        pcs.getLoyaltyTariffRangeManager().tidyDayRangeObjects(yw0);
        pcs.getPriceTariffRangeManager().tidyDayRangeObjects(yw0);
    }

    /**
     *  Ensures there are no tariff relationships when the tariff no longer exists
     *
     * @throws PersistenceException
     */
    public static void tidyTariffRelationships() throws PersistenceException {
        PriceConfigService pcs = PriceConfigService.getInstance();
        // run through the price Tariff relationships and ensure there is a price Tariff
        for(int tariffId : pcs.getPriceTariffRelationship().getAllKeys()) {
            if(!pcs.getPriceTariffManager().isIdentifier(tariffId) || pcs.getPriceTariffRelationship().getAllIdentifier(tariffId).isEmpty()) {
                pcs.getPriceTariffRelationship().removeKey(tariffId);
            }
        }
        // run through the loyalty tariff relationships and ensure there is a loyalty tariff
        for(int tariffId : pcs.getLoyaltyTariffRelationship().getAllKeys()) {
            if(!pcs.getLoyaltyTariffManager().isIdentifier(tariffId) || pcs.getLoyaltyTariffRelationship().getAllIdentifier(tariffId).isEmpty()) {
                pcs.getLoyaltyTariffRelationship().removeKey(tariffId);
            }
        }
    }

    /**
     * tidies all bookings where a session billing has been reconciled, invoiced and paid
     *
     * @param ywd
     * @return the bookingId set that has been changed
     * @throws PersistenceException
     * @throws com.oathouse.oss.storage.exceptions.MaxCountReachedException
     */
    public static List<BookingBean> tidyBookingsToStateArchive(int ywd) throws PersistenceException, MaxCountReachedException, NoSuchIdentifierException {
        TransactionService ts = TransactionService.getInstance();
        BillingService bs = BillingService.getInstance();
        ChildBookingService cbs = ChildBookingService.getInstance();
        ChildBookingHistory cbh = ChildBookingHistory.getInstance();
        ChildService cs = ChildService.getInstance();

        List<BookingBean> rtnList = new LinkedList<>();
        int cutoffYw = YWDHolder.isValid(ywd) && CalendarStatic.getRelativeYW(-12) > ywd ? ywd : CalendarStatic.getRelativeYW(-12);

        for(int accountId : cs.getAccountManager().getAllIdentifier()) {
            for(InvoiceBean invoice : ts.getInvoiceManager().getAllObjects(accountId)) {
                int invoiceId = invoice.getInvoiceId();
                if(ts.getCustomerCreditManager().getInvoiceBalance(accountId, invoiceId) == 0) {
                    for(BillingBean billing : bs.getBillingsForInvoice(accountId, invoiceId, BillingEnum.TYPE_SESSION)) {
                        if(billing.getDescription().equals(ApplicationConstants.SESSION_RECONCILED) && billing.getYwd() < cutoffYw) {
                            try {
                                BookingBean booking = cbs.getBookingManager().getObject(billing.getYwd(), billing.getBookingId());
                                if(booking.isState(BookingState.ARCHIVED)) {
                                    continue;
                                }
                                System.out.println("Booking Processed : ywd " + booking.getYwd() + " bookingId " + booking.getBookingId() + " State " + booking.getState().toString());
                                cbs.getBookingManager().setObjectState(booking.getYwd(), booking.getBookingId(), BookingState.ARCHIVED, ObjectBean.SYSTEM_OWNED);
                                // set the history so we know we archived it
                                cbh.setBookingHistory(booking);
                                rtnList.add(booking);
                            } catch(NoSuchIdentifierException | NullObjectException | IllegalActionException e) {
                                // Do nothing
                            }
                        }
                    }
                }
            }
        }
        return rtnList;
    }

    private MaintenanceService() {
    }

}
