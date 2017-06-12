/*
 * @(#)Builders.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.builders;

import com.oathouse.ccm.cma.ApplicationConstants;
import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.accounts.BillingService;
import com.oathouse.ccm.cma.booking.ChildBookingService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.accounts.finance.BillingBean;
import com.oathouse.ccm.cos.bookings.BTBits;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean;
import com.oathouse.ccm.cos.config.finance.BillingEnum;
import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean;
import com.oathouse.ccm.cos.config.finance.PriceAdjustmentBean;
import com.oathouse.ccm.cos.config.finance.PriceListBean;
import com.oathouse.ccm.cos.profile.AccountBean;
import com.oathouse.ccm.cos.profile.AccountStatus;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.properties.SystemPropertiesBean;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.valueholder.CalendarStatic;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@literal Builders} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Nov-2013
 */
public class Builders {
    private static final String owner = ObjectBean.SYSTEM_OWNED;
    public static int accountId = 11;
    public static int childId = 13;
    public static int discountRate = 1000; // 10%

    public static BillingBean getBilling(int id, BillingEnum... billingArgs) throws Exception {
        return getBilling(id, new ConcurrentSkipListMap<String,Integer>(), billingArgs);
    }

    /*
    BillingBits are set as SESSION and reset with BillingEnums passed
    Description is set according to BillingEnum TYPE
        - by including 'Reconciled' as an attribute key makes SESSION or FUNDED have description RECONCILED
        - by including 'Reconciled' if not type SESSION or FUNDED and not discount type  will convert TYPE to string.
    */
    public static BillingBean getBilling(int id, Map<String,Integer> attributes, BillingEnum... billingArgs) throws Exception {
        Map<String, String> beanMap = new ConcurrentSkipListMap<>();
        beanMap.put("accountId", attributes.containsKey("accountId") ? attributes.get("accountId").toString() : Integer.toString(accountId));
        beanMap.put("ywd", attributes.containsKey("ywd") ? attributes.get("ywd").toString() : Integer.toString(CalendarStatic.getRelativeYW(0)));
        beanMap.put("billingSd", attributes.containsKey("billingSd") ? attributes.get("billingSd").toString() : Integer.toString(SDHolder.buildSD("08:00", "17:00")));
        beanMap.put("value", attributes.containsKey("value") ? attributes.get("value").toString() : "20000");
        beanMap.put("taxRate", attributes.containsKey("taxRate") ? attributes.get("taxRate").toString() : "0");
        beanMap.put("bookingId", attributes.containsKey("bookingId") ? attributes.get("bookingId").toString() : "17");
        beanMap.put("profileId", attributes.containsKey("profileId") ? attributes.get("profileId").toString() : Integer.toString(childId));
        beanMap.put("btChargeBit", attributes.containsKey("btChargeBit") ? attributes.get("btChargeBit").toString() : Integer.toString(BTBits.STANDARD_CHARGE_BIT));
        beanMap.put("invoiceId", attributes.containsKey("invoiceId") ? attributes.get("invoiceId").toString() : "-1");
        beanMap.put("discountId", attributes.containsKey("discountId") ? attributes.get("discountId").toString() : "-1");
        beanMap.put("adjustmentId", attributes.containsKey("adjustmentId") ? attributes.get("adjustmentId").toString() : "-1");

        int billingBits = BillingEnum.getBillingBits(TYPE_SESSION, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_EQUAL, GROUP_BOOKING);
        billingBits = BillingEnum.resetBillingBits(billingBits, billingArgs);
        beanMap.put("billingBits", Integer.toString(billingBits));

        BillingEnum billingEnum = BillingEnum.getBillingEnumForLevel(billingBits, BillingEnum.TYPE);
        String description;
        switch(billingEnum) {
            case TYPE_SESSION:
                description = attributes.containsKey("Reconciled") ? ApplicationConstants.SESSION_RECONCILED : ApplicationConstants.SESSION_ESTIMATE;
                break;
            case TYPE_FUNDED:
                description = attributes.containsKey("Reconciled") ? ApplicationConstants.EDUCATION_REDUCTION_RECONCILED : ApplicationConstants.EDUCATION_REDUCTION_ESTIMATE;
                break;
            case TYPE_BOOKING_CHILD_DISCOUNT:
                description = ApplicationConstants.BOOKING_CHILD_DISCOUNT;
                break;
            case TYPE_FIXED_ACCOUNT_DISCOUNT:
                description = ApplicationConstants.FIXED_ACCOUNT_DISCOUNT;
                break;
            case TYPE_FIXED_CHILD_DISCOUNT:
                description = ApplicationConstants.FIXED_CHILD_DISCOUNT;
                break;
            case TYPE_LOYALTY_CHILD_DISCOUNT:
                description = ApplicationConstants.LOYALTY_CHILD_DISCOUNT;
                break;
            default:
                description = attributes.containsKey("Reconciled") ? billingEnum.toString() : "";
        }
        beanMap.put("description", description);
        beanMap.put("notes", "Notes");

        return (BillingBean) BeanBuilder.addBeanValues(new BillingBean(), id, beanMap);
    }

    public static BillingBean setBilling(BillingEnum... billingArgs) throws Exception {
        return setBilling(-1, new ConcurrentSkipListMap<String, Integer>(), billingArgs);
    }

    public static BillingBean setBilling(int id, BillingEnum... billingArgs) throws Exception {
        return setBilling(id, new ConcurrentSkipListMap<String, Integer>(), billingArgs);
    }

    public static BillingBean setBilling(int id, Map<String,Integer> attributes, BillingEnum... billingArgs) throws Exception {
        int billingId = id > 0 ? id : BillingService.getInstance().getBillingManager().generateIdentifier();
        BillingBean billing = getBilling(billingId, attributes, billingArgs);
        BillingService.getInstance().getBillingManager().setObject(billing.getAccountId(), billing);
        return billing;
    }

    public static BookingBean getBooking(int id, BookingState... stateOption) throws Exception {
        return getBooking(id, new ConcurrentSkipListMap<String,Integer>(), stateOption);
    }

    public static BookingBean getBooking(int id, Map<String,Integer> attributes, BookingState... stateOption) throws Exception {
        Map<String, String> beanMap = new ConcurrentSkipListMap<>();
        beanMap.put("ywd", attributes.containsKey("ywd") ? attributes.get("ywd").toString() : Integer.toString(CalendarStatic.getRelativeYW(0)));
        beanMap.put("roomId", attributes.containsKey("roomId") ? attributes.get("roomId").toString() : "9");
        // get the bookingSd so we can set the spanSd if not an attribute
        int bookingSd = attributes.containsKey("bookingSd") ? attributes.get("bookingSd") : SDHolder.buildSD("08:00", "17:00");
        beanMap.put("bookingSd", Integer.toString(bookingSd));
        beanMap.put("spanSd", attributes.containsKey("spanSd") ? attributes.get("spanSd").toString() : Integer.toString(bookingSd));
        beanMap.put("profileId", attributes.containsKey("profileId") ? attributes.get("profileId").toString() : Integer.toString(childId));
        beanMap.put("liableContactId", attributes.containsKey("liableContactId") ? attributes.get("liableContactId").toString() : "19");
        beanMap.put("bookingDropOffId", attributes.containsKey("bookingDropOffId") ? attributes.get("bookingDropOffId").toString() : "-1");
        beanMap.put("actualDropOffId", attributes.containsKey("actualDropOffId") ? attributes.get("actualDropOffId").toString() : "-1");
        beanMap.put("bookingPickupId", attributes.containsKey("bookingPickupId") ? attributes.get("bookingPickupId").toString() : "-1");
        beanMap.put("actualPickupId", attributes.containsKey("actualPickupId") ? attributes.get("actualPickupId").toString() : "-1");
        beanMap.put("bookingTypeId", attributes.containsKey("bookingTypeId") ? attributes.get("bookingTypeId").toString() : Integer.toString(BTIdBits.ATTENDING_STANDARD));
        beanMap.put("actionBits", attributes.containsKey("actionBits") ? attributes.get("actionBits").toString() : "0");
        beanMap.put("requestedYwd", attributes.containsKey("requestedYwd") ? attributes.get("requestedYwd").toString() : Integer.toString(CalendarStatic.getRelativeYW(0)));
        beanMap.put("notes", "Notes");
        BookingState state = stateOption.length > 0 ? stateOption[0] : BookingState.RESTRICTED;
        beanMap.put("state", state.toString());
        int actualStart = BookingState.isEqualOrAfter(state, BookingState.STARTED) ? SDHolder.getStart(Integer.parseInt(beanMap.get("spanSd"))) : -1;
        int actualEnd = BookingState.isEqualOrAfter(state, BookingState.COMPLETED) ? SDHolder.getEnd(Integer.parseInt(beanMap.get("spanSd"))) : -1;
        beanMap.put("actualStart", attributes.containsKey("actualStart") ? attributes.get("actualStart").toString() : Integer.toString(actualStart));
        beanMap.put("actualEnd", attributes.containsKey("actualEnd") ? attributes.get("actualEnd").toString() : Integer.toString(actualEnd));

        return (BookingBean) BeanBuilder.addBeanValues(new BookingBean(), id, beanMap);
    }

    public static BookingBean setBooking(BookingState... stateOption) throws Exception {
        return setBooking(-1, new ConcurrentSkipListMap<String,Integer>(), stateOption);
    }

    public static BookingBean setBooking(int id, BookingState... stateOption) throws Exception {
        return setBooking(id, new ConcurrentSkipListMap<String,Integer>(), stateOption);
    }

    public static BookingBean setBooking(Map<String,Integer> attributes, BookingState... stateOption) throws Exception {
        return setBooking(-1, attributes, stateOption);
    }

    public static BookingBean setBooking(int id, Map<String,Integer> attributes, BookingState... stateOption) throws Exception {
        int bookingId = id > 0 ? id : ChildBookingService.getInstance().getBookingManager().generateIdentifier();
        BookingBean b = getBooking(bookingId, attributes, stateOption);
        ObjectSetStore<BookingTypeBean> bookingTypeManager = new ObjectSetStore<>(VT.BOOKING_TYPE.manager(), ObjectDataOptionsEnum.PERSIST);
        int flagBits = attributes.containsKey("flagBits") ? attributes.get("flagBits") : BTFlagIdBits.STANDARD_FLAGS;
        bookingTypeManager.setObject(new BookingTypeBean(b.getBookingTypeId(), BTIdBits.getAllStrings(b.getBookingTypeId()), flagBits, 0, owner));
        ObjectMapStore<BookingBean> bookingManager = new ObjectMapStore<>(VT.CHILD_BOOKING.manager(), ObjectDataOptionsEnum.PERSIST);
        bookingManager.setObject(b.getYwd(), b);
        ChildBookingService.getInstance().reInitialise();
        return b;
    }

    public static ChildBean getChild() throws Exception {
        return getChild(childId, new ConcurrentSkipListMap<String, Integer>());
    }


    public static ChildBean getChild(int id, Map<String,Integer> attributes) throws Exception {
        Map<String, String> beanMap = new ConcurrentSkipListMap<>();
        beanMap.put("accountId", attributes.containsKey("accountId") ? attributes.get("accountId").toString() : Integer.toString(accountId));
        //sort out dob
        int years = attributes.containsKey("years") ? attributes.get("years") : 3;
        int dob = attributes.containsKey("dob") ? attributes.get("dob") : CalendarStatic.getRelativeYW(-(years * 52 + 10));
        beanMap.put("dateOfBirth", Integer.toString(dob));
        beanMap.put("dateOfBirth", attributes.containsKey("dateOfBirth") ? attributes.get("dateOfBirth").toString() : Integer.toString(dob));
        beanMap.put("departYwd", attributes.containsKey("departYwd") ? attributes.get("departYwd").toString() : Integer.toString(CalendarStatic.getRelativeYW(dob, 12 * 52)));
        beanMap.put("bookingDiscountRate", attributes.containsKey("bookingDiscountRate") ? attributes.get("bookingDiscountRate").toString() : Integer.toString(0));
        // looks for fixed item discount rate first then child discount rate. This is if child set with setting account discount rates
        int rate = attributes.containsKey("fixedItemDiscountRate") ? attributes.get("fixedItemDiscountRate"): 0;
        beanMap.put("fixedItemDiscountRate", Integer.toString(rate));
        beanMap.put("fixedItemDiscountRate", attributes.containsKey("childDiscountRate") ? attributes.get("childDiscountRate").toString() : Integer.toString(rate));
        beanMap.put("bookingDiscountRate", attributes.containsKey("bookingDiscountRate") ? attributes.get("bookingDiscountRate").toString() : Integer.toString(0));
        return (ChildBean) BeanBuilder.addBeanValues(new ChildBean(), id, beanMap);
    }

    // Note this is set up to auto generate childId's
    public static ChildBean setChild() throws Exception {
        return setChild(-1, new ConcurrentSkipListMap<String,Integer>());
    }

    public static ChildBean setChild(int id) throws Exception {
        return setChild(id, new ConcurrentSkipListMap<String,Integer>());
    }

    public static ChildBean setChild(int id, Map<String,Integer> attributes) throws Exception {
        int childId = id > 0 ? id : ChildService.getInstance().getChildManager().generateIdentifier();
        ChildBean child = getChild(childId, attributes);
        Map<String, String> beanMap = new ConcurrentSkipListMap<>();
        beanMap.put("fixedItemDiscountRate", attributes.containsKey("accountDiscountRate") ? attributes.get("accountDiscountRate").toString() : Integer.toString(0));
        beanMap.put("fixedItemDiscountRate", attributes.containsKey("fixedItemDiscountRate") ? attributes.get("fixedItemDiscountRate").toString() : Integer.toString(0));
        beanMap.put("status", AccountStatus.ACTIVE.toString());
        AccountBean account = (AccountBean) BeanBuilder.addBeanValues(new AccountBean(), child.getAccountId(), beanMap);
        ChildService.getInstance().getAccountManager().setObject(account);
        ChildService.getInstance().getChildManager().setObject(child);
        return child;
    }

    public static SystemPropertiesBean getProperties() throws Exception {
        return getProperties(new ConcurrentSkipListMap<String, Integer>());
    }

    public static SystemPropertiesBean getProperties(Map<String,Integer> attributes) throws Exception {
        Map<String, String> beanMap = new ConcurrentSkipListMap<>();
        if(!attributes.containsKey("chargeMargin")) { beanMap.put("chargeMargin",  Boolean.toString(false));}
        beanMap.put("dropOffChargeMargin", attributes.containsKey("dropOffChargeMargin") ? attributes.get("dropOffChargeMargin").toString() : Integer.toString(5));
        beanMap.put("dropOffChargeMargin", attributes.containsKey("dropOffChargeMargin") ? attributes.get("dropOffChargeMargin").toString() : Integer.toString(5));
        beanMap.put("firstInvoiceYwd", attributes.containsKey("firstInvoiceYwd") ? attributes.get("dropOffChargeMargin").toString() : Integer.toString(CalendarStatic.getRelativeYW(-20)));
        beanMap.put("confirmedPeriodWeeks", attributes.containsKey("confirmedPeriodWeeks") ? attributes.get("confirmedPeriodWeeks").toString() : Integer.toString(8));
        beanMap.put("dropOffChargeMargin", attributes.containsKey("dropOffChargeMargin") ? attributes.get("dropOffChargeMargin").toString() : Integer.toString(0));
        beanMap.put("taxRate", attributes.containsKey("taxRate") ? attributes.get("taxRate").toString() : Integer.toString(0));
        if(!attributes.containsKey("loyaltyApplyedToEducationDays")) { beanMap.put("loyaltyApplyedToEducationDays", Boolean.toString(false)); }

        return (SystemPropertiesBean) BeanBuilder.addBeanValues(new SystemPropertiesBean(), 1, beanMap);
    }

    public static PriceListBean getPriceList() throws Exception {
        final Map<Integer, Long> periodSdValues = new ConcurrentSkipListMap<>();
        periodSdValues.put(SDHolder.buildSD("08:00", "08:59"),  5000L);
        periodSdValues.put(SDHolder.buildSD("09:00", "11:59"), 12000L);
        periodSdValues.put(SDHolder.buildSD("12:00", "12:59"),  5000L);
        periodSdValues.put(SDHolder.buildSD("13:00", "15:59"), 12000L);
        periodSdValues.put(SDHolder.buildSD("16:00", "16:59"),  5000L);
        return new PriceListBean(1, "Standard", "#ffffff", periodSdValues, owner);
    }

    public static PriceListBean getPriceReduction() throws Exception {
        final Map<Integer, Long> periodSdValues = new ConcurrentSkipListMap<>();
        periodSdValues.put(SDHolder.buildSD("09:00", "11:59"), 10000L);
        periodSdValues.put(SDHolder.buildSD("12:00", "12:59"),  0L);
        periodSdValues.put(SDHolder.buildSD("13:00", "15:59"), 10000L);
        return new PriceListBean(1, "Reduction", "#000000", periodSdValues, owner);
    }

    public static ChildEducationTimetableBean getChildEdTimetable() {
        Set<Integer> educationSd = new ConcurrentSkipListSet<>();
        educationSd.add(SDHolder.buildSD("09:00", "11:59"));
        educationSd.add(SDHolder.buildSD("12:00", "12:59"));
        educationSd.add(SDHolder.buildSD("13:00", "15:59"));
        return new ChildEducationTimetableBean(1, "Education", "#eeeeee", 1, educationSd, owner);
    }

    public static List<PriceAdjustmentBean> getAdjustmentList() {
        List<PriceAdjustmentBean> rtnList = new LinkedList<>();
        int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        rtnList.add(new PriceAdjustmentBean(1, "Breakfast", billingBits, 550L, -1, SDHolder.buildSD("08:00", "08:29"), false, -1, owner));
        rtnList.add(new PriceAdjustmentBean(2, "Lunch", billingBits, 950L, -1, SDHolder.buildSD("12:00", "12:59"), false, -1, owner));
        return rtnList;
    }

    public static PriceAdjustmentBean getAdjustment() {
        int billingBits = BillingEnum.getBillingBits(TYPE_ADJUSTMENT_ON_ATTENDING, BILL_CHARGE, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_SOME_PART, GROUP_BOOKING);
        return(new PriceAdjustmentBean(1, "Breakfast", billingBits, 550L, -1, SDHolder.buildSD("08:00", "08:29"), false, -1, owner));
    }

    public static LoyaltyDiscountBean getLoyalty(int id) {
        return getLoyalty(id, new ConcurrentSkipListMap<String, Integer>());
    }

    public static LoyaltyDiscountBean getLoyalty(int id, Map<String,Integer> attributes) {
        int billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        billingBits = attributes.containsKey("billingBits") ? attributes.get("billingBits") : billingBits;
        long discount = attributes.containsKey("discount") ? attributes.get("discount") : 10000L;
        int start = attributes.containsKey("start") ? attributes.get("start") : -1;
        int duration = attributes.containsKey("duration") ? attributes.get("duration") : Builders.getSdDuration("8:00", "16:59");
        boolean[] priorityDays = BeanBuilder.getDays(attributes.containsKey("priorityDays") ? attributes.get("priorityDays") : BeanBuilder.MON_TO_FRI);
        String name = "Loyalty " + id;
        return new LoyaltyDiscountBean(id, name, billingBits, discount, start, duration, priorityDays, owner);
    }

    public static List<LoyaltyDiscountBean> getLoyalty(int id, Map<String,Integer> attributes, List<LoyaltyDiscountBean> injectionList) {
        if(injectionList == null) {
            injectionList = new LinkedList<>();
        }
        injectionList.add(getLoyalty(id, attributes));
        return injectionList;
    }

    public static List<LoyaltyDiscountBean> getLoyaltyWeek() {
        List<LoyaltyDiscountBean> rtnList = new LinkedList<>();
        int billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        int duration = SDHolder.getDuration(SDHolder.buildSD("08:00", "16:59"));
        int discount = 400;
        rtnList.add(new LoyaltyDiscountBean(1, "Mon Discount", billingBits, discount, -1, duration, BeanBuilder.getDays(1), owner));
        rtnList.add(new LoyaltyDiscountBean(2, "Tue Discount", billingBits, discount, -1, duration, BeanBuilder.getDays(2), owner));
        rtnList.add(new LoyaltyDiscountBean(3, "Wed Discount", billingBits, discount, -1, duration, BeanBuilder.getDays(4), owner));
        rtnList.add(new LoyaltyDiscountBean(4, "Thu Discount", billingBits, discount, -1, duration, BeanBuilder.getDays(8), owner));
        rtnList.add(new LoyaltyDiscountBean(5, "Fri Discount", billingBits, discount, -1, duration, BeanBuilder.getDays(16), owner));
        return rtnList;
    }

    public static List<LoyaltyDiscountBean> getLoyaltyDay() {
        List<LoyaltyDiscountBean> rtnList = new LinkedList<>();
        int billingBits = BillingEnum.getBillingBits(TYPE_LOYALTY, BILL_CREDIT, CALC_AS_VALUE, APPLY_NO_DISCOUNT, RANGE_AT_LEAST, GROUP_LOYALTY);
        boolean[] priorityDays = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int shortDuration = SDHolder.getDuration(SDHolder.buildSD("08:00", "15:59"));
        int shortDiscount = 400;
        int longDuration = SDHolder.getDuration(SDHolder.buildSD("08:00", "16:59"));
        int longDiscount = 200;
        rtnList.add(new LoyaltyDiscountBean(1, "Short Day Discount", billingBits, shortDiscount, -1, shortDuration, priorityDays, owner));
        rtnList.add(new LoyaltyDiscountBean(2, "Long Day Discount", billingBits, longDiscount, -1, longDuration, priorityDays, owner));
        return rtnList;
    }

    public static List<BillingBean> getBillingType(BillingEnum type, List<BillingBean> billingList) {
        List<BillingBean> rtnList = new LinkedList<>();
        for(BillingBean billing : billingList) {
            if(billing.hasBillingBits(type)) {
                rtnList.add(billing);
            }
        }
        return rtnList;
    }

    public static String getBillingBits(List<BillingBean> billingList) {
        StringBuilder sb = new StringBuilder();
        for(BillingBean billing : billingList) {
            System.out.println("id [" + billing.getBillingId() + "] -> BillingBits : "  + billing.getBillingBits() + " = " + BillingEnum.getAllStrings(billing.getBillingBits()));
        }
        return sb.toString();
    }

    public static void printList(List<BillingBean> billingList, String preText, BillingEnum... include) {
        System.out.println(preText);
        for(BillingBean billing : billingList) {
            if(include.length == 0 || BillingEnum.hasAnyBillingBit(billing.getBillingBits(), include)) {
                System.out.println(billing.getString());
            }
        }
    }

    public static int getSdStart(String startTime) {
        String[] startSplit = startTime.split(":");
        return (Integer.parseInt(startSplit[0]) * 60) + Integer.parseInt(startSplit[1]);
    }

    public static int getSdDuration(String startTime, String endTime) {
        int periodSd = SDHolder.buildSD(startTime, endTime);
        return SDHolder.getDuration(periodSd);
    }

    public static long getPrice(String startTime, String endTime) throws Exception {
        int periodSd = SDHolder.buildSD(startTime, endTime);
        return getPriceList().getPeriodSdSum(periodSd);
    }

    private Builders() {
    }


}
