/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.dto;

// common imports
import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.*;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.builders.Builders;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.TDCalc;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Darryl Oatridge
 */
public class InvoiceLineDTO_scenario {

    private final static String owner = ObjectBean.SYSTEM_OWNED;
    private final static Set<Integer> usedIds = new ConcurrentSkipListSet<>();
    public final static int accountId = Builders.accountId;
    public final static int childId = Builders.childId;

    public static List<BillingBean> createReconciledBillingList(int bookingSd, int discountRate, int invoiceId, VABoolean... includeArgs) throws Exception {
        final Map<String, Integer> attributes = new ConcurrentSkipListMap<>();
        // clear out any that are there
        ObjectDBMS.clearAuthority(ObjectBean.SYSTEM_OWNED);
        //set up internal beans
        attributes.put("bookingDiscountRate", 750);
        attributes.put("childDiscountRate", 500);
        attributes.put("accountDiscountRate", 1000);
        Builders.setChild(childId, attributes);
        // clear out and reset the booking
        ChildBookingService.getInstance().clear();
        attributes.clear();
        attributes.put("bookingSd",bookingSd);
        BookingBean booking = Builders.setBooking(1, attributes);
        /*
         * Create the billings
         */
        List<BillingBean> rtnList = new LinkedList<>();

        long value;
        String[] description = new String[2];
        int billingBits;

        // SESSION (value = 60000 - 3000
        if(INCLUDE_SESSION.isIn(includeArgs) || includeArgs.length == 0) {
            value = 60000;
            description[0] = ApplicationConstants.SESSION_ESTIMATE;
            description[1] = ApplicationConstants.SESSION_CHILD_DISCOUNT;
            billingBits = BillingEnum.getBillingBits(TYPE_SESSION,BILL_CHARGE,CALC_AS_VALUE,APPLY_DISCOUNT,RANGE_EQUAL,GROUP_BOOKING);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId - 1, description));
            value = 2000;
            description[0] = ApplicationConstants.SESSION_RECONCILED;
            description[1] = ApplicationConstants.SESSION_CHILD_DISCOUNT;
            billingBits = BillingEnum.getBillingBits(TYPE_SESSION,BILL_CHARGE,CALC_AS_VALUE,APPLY_DISCOUNT,RANGE_EQUAL,GROUP_BOOKING);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId, description));
        }
        // EDUCATION REDUCTION = -30000
        if(INCLUDE_EDUCATION.isIn(includeArgs) || includeArgs.length == 0) {
            value = 30000;
            description[0] = ApplicationConstants.EDUCATION_REDUCTION_RECONCILED;
            description[1] = "";
            billingBits = BillingEnum.getBillingBits(TYPE_FUNDED,BILL_CREDIT,CALC_AS_VALUE,APPLY_NO_DISCOUNT,RANGE_SOME_PART,GROUP_BOOKING);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId, description));
        }
        if(INCLUDE_ADJUSTMENTS.isIn(includeArgs) || includeArgs.length == 0) {
            // PENALTIES = 2000
            value = 2000;
            description[0] = "Late Pickup";
            description[1] = "";
            billingBits = BillingEnum.getBillingBits(TYPE_LATE_PICKUP,BILL_CHARGE,CALC_AS_VALUE,APPLY_NO_DISCOUNT,RANGE_IGNORED,GROUP_BOOKING);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId, description));
            // ADJUSTMENTS = 5000 - 250
            value = 5000;
            description[0] = "Lunch";
            description[1] = ApplicationConstants.ADJUSTMENT_CHILD_DISCOUNT;
            billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING,BILL_CHARGE,CALC_AS_VALUE,APPLY_DISCOUNT,RANGE_EQUAL,GROUP_BOOKING);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId, description));
        }
        // LOYALTY = -10000
        if(INCLUDE_LOYALTY.isIn(includeArgs) || includeArgs.length == 0) {
            value = 10000;
            description[0] = "Loyalty Day Discount";
            description[1] = "";
            billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY,BILL_CREDIT,CALC_AS_VALUE,APPLY_NO_DISCOUNT,RANGE_AT_LEAST,GROUP_LOYALTY);
            rtnList.addAll(getBilling(booking, value, discountRate, billingBits, invoiceId, description));
        }
        if(INCLUDE_FIXED.isIn(includeArgs) || includeArgs.length == 0) {
            // FIXED CHILD ITEM = 3000 - 150
            value = 3000;
            description[0] = "Class Photo";
            description[1] = ApplicationConstants.FIXED_CHILD_DISCOUNT;
            billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM,BILL_CHARGE,CALC_AS_VALUE,APPLY_DISCOUNT,RANGE_IGNORED,GROUP_FIXED_ITEM);
            rtnList.addAll(getBilling(null, value, discountRate, billingBits, invoiceId, description));
            // FIXED ACCOUNT ITEM = -1500
            value = 1500;
            description[0] = "Credit Refund";
            description[1] = ApplicationConstants.FIXED_ACCOUNT_DISCOUNT;
            billingBits = BillingEnum.getBillingBits(TYPE_FIXED_ITEM,BILL_CREDIT,CALC_AS_VALUE,APPLY_NO_DISCOUNT,RANGE_IGNORED,GROUP_FIXED_ITEM);
            rtnList.addAll(getBilling(null, value, discountRate, billingBits, invoiceId, description));
        }
        return rtnList;

    }

    protected static List<BillingBean> getBilling(BookingBean booking, long value, int discountRate, int billingBits, int invoiceId, String[] description) throws Exception {
        int ywd = booking == null ? CalendarStatic.getRelativeYWD(0) : booking.getYwd();
        int chargeSd = booking == null ? -1 : booking.getBookingSd();
        int bookingId = booking == null ? -1 : booking.getBookingId();
        int chargeBit = booking == null ? -1 : booking.getBookingTypeChargeBit();
        int profileId = booking == null ? description[1].equals(ApplicationConstants.FIXED_CHILD_DISCOUNT) ? childId : -1 : booking.getProfileId();
        int taxRate = 0;
        int adjustmentId = -1;
        String notes ="notes";

        List<BillingBean> rtnList = new LinkedList<>();
        int billingId = getBillingId();
        int discountId = -1;
        if(BillingEnum.hasAnyBillingBit(billingBits, APPLY_DISCOUNT) && discountRate > 0) {
            int discountBits = BillingEnum.getBillingBits(TYPE_BOOKING_CHILD_DISCOUNT, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_IGNORED, GROUP_BOOKING);
            if(BillingEnum.hasAnyBillingBit(billingBits, TYPE_ADJUSTMENT_ON_ALL, TYPE_ADJUSTMENT_ON_ATTENDING)) {
                discountBits = BillingEnum.resetBillingBits(discountBits, TYPE_ADJUSTMENT_CHILD_DISCOUNT);
                adjustmentId = 37;
            }
            if(BillingEnum.hasBillingBit(billingBits, TYPE_FIXED_ITEM)) {
                if(description[1].equals(ApplicationConstants.FIXED_CHILD_DISCOUNT)) {
                    discountBits = BillingEnum.resetBillingBits(discountBits, TYPE_FIXED_CHILD_DISCOUNT, GROUP_FIXED_ITEM);
                } else {
                    discountBits = BillingEnum.resetBillingBits(discountBits, TYPE_FIXED_ACCOUNT_DISCOUNT, GROUP_FIXED_ITEM);
                }
            }
            long discountValue = TDCalc.getDiscountValue(value, discountRate);
            discountId = getBillingId();
            BillingBean discount = new BillingBean(discountId, accountId, ywd, -1, discountValue, taxRate, bookingId, profileId, chargeBit, discountBits, invoiceId, description[1], "", -1, adjustmentId, owner);
            rtnList.add(discount);
            discountId = discount.getBillingId();
        }
        // the billing
        BillingBean billing = new BillingBean(billingId, accountId, ywd, chargeSd, value, taxRate, bookingId, profileId, chargeBit, billingBits, invoiceId, description[0], notes, discountId, adjustmentId, owner);
        rtnList.add(0, billing);
        return rtnList;
    }

    protected static int getBillingId() throws Exception {
        int billingId = ObjectEnum.generateIdentifier(usedIds);
        usedIds.add(billingId);
        return billingId;
    }
}
