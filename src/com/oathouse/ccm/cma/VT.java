/*
 * @(#)VT.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cma;

/**
 * The {@code VT} Enumeration is a verification type enumeration used as a common
 * naming convention and reference across the services
 *
 * @author Darryl Oatridge
 * @version 1.00 04-Dec-2011
 */
public enum VT {

    // GENERIC BEANS
    DayRange("dayRangeManager", "com.oathouse.ccm.cos.config.DayRangeBean"),
    Booking("bookingManager", "com.oathouse.ccm.cos.bookings.BookingBean"),
    Relation("relationManager", "com.oathouse.ccm.cos.profile.RelationBean"),
    Tariff("tariffManager", "com.oathouse.ccm.cos.config.finance.TariffBean"),
    TariffRelation("tariffRelationManager", "com.oathouse.ccm.cos.config.finance.TariffRelationBean"),

    // Configuration
    PROPERTIES("systemProperties", "com.oathouse.ccm.cos.properties.SystemPropertiesBean"),
    ACCOUNT_HOLDER("accountHolder", "com.oathouse.ccm.cos.properties.AccountHolderBean"),
    MILK_CONCESSION("milkConcession", "com.oathouse.ccm.cos.concessions.MilkConcessionBean"),
    HOLIDAY_CONCESSION("holidayConcession", "com.oathouse.ccm.cos.concessions.HolidayConcessionBean"),

    ROOM_CONFIG("roomConfigManager", "com.oathouse.ccm.cos.config.RoomConfigBean"),
    AGE_RANGE("ageRangeManager", "com.oathouse.ccm.cos.config.AgeRangeBean"),
    ROOM_START("childRoomStartManager", "com.oathouse.ccm.cos.config.ChildRoomStartBean"),

    TIMETABLE("timetableManager", "com.oathouse.ccm.cos.config.TimetableBean"),
    TIMETABLE_EDUCATION("childEducationTimetableManager", "com.oathouse.ccm.cos.config.education.ChildEducationTimetableBean"),
    TIMETABLE_RANGE("timetableDayRange", DayRange.qualifiedBean()),
    TIMETABLE_EDUCATION_RANGE("timetableEducationDayRange", DayRange.qualifiedBean()),

    // PROFILE
    ACCOUNT("accountManager", "com.oathouse.ccm.cos.profile.AccountBean"),
    CHILD("childManager", "com.oathouse.ccm.cos.profile.ChildBean"),
    STAFF("staffManager", "com.oathouse.ccm.cos.profile.StaffBean"),
    CONTACT("contactManager", "com.oathouse.ccm.cos.profile.ContactBean"),
    MEDICAL("medicalManager", "com.oathouse.ccm.cos.profile.MedicalBean"),
    LEGAL_RELATION("legalAccountRelationship", Relation.qualifiedBean()),
    CUSTODIAL_RELATION("custodialRelationship", Relation.qualifiedBean()),
    CHILD_EDUCATION_RANGE("childEducationDayRange", DayRange.qualifiedBean()),

    CHILD_ARCHIVE("childArchive", "com.oathouse.ccm.cos.profile.ChildBean"),
    STAFF_ARCHIVE("staffArchive", "com.oathouse.ccm.cos.profile.StaffBean"),
    ACCOUNT_ARCHIVE("accountArchive", "com.oathouse.ccm.cos.profile.AccountBean"),

    // Bookings
    BOOKING_TYPE("bookingTypeManager", "com.oathouse.ccm.cos.bookings.BookingTypeBean"),
    CHILD_BOOKING("childBookingManager", Booking.qualifiedBean()),
    ACCOUNT_REQUEST("bookingRequestManager", "com.oathouse.ccm.cos.bookings.BookingRequestBean"),
    GLOBAL_REQUEST("globalRequestManager", "com.oathouse.ccm.cos.bookings.BookingRequestBean"),
    CHILD_REQUEST_RANGE("bookingRequestDayRange", DayRange.qualifiedBean()),
    STAFF_BOOKING("staffBookingManager", Booking.qualifiedBean()),
    STAFF_ACTIVITY("staffActivityManager", "com.oathouse.ccm.cos.bookings.ActivityBean"),

    CHILD_BOOKING_ARCHIVE("childBookingArchive", Booking.qualifiedBean()),
    STAFF_BOOKING_ARCHIVE("staffBookingArchive", Booking.qualifiedBean()),
    CHILD_BOOKING_HISTORY("childBookingHistory", Booking.qualifiedBean()),

    // Price Configurarion
    AGE_START("ageStartManager", "com.oathouse.ccm.cos.config.AgeStartBean"),

    PRICE_TARIFF_RANGE("priceTariffDayRange", DayRange.bean()),
    PRICE_TARIFF("priceTariffManager", Tariff.qualifiedBean()),
    PRICE_TARIFF_RELATION("priceTariffRelationship", TariffRelation.qualifiedBean()),
    PRICE_LIST("priceListManager", "com.oathouse.ccm.cos.config.finance.PriceListBean"),
    PRICE_ADJUSTMENT("priceAdjustmentManager", "com.oathouse.ccm.cos.config.finance.PriceAdjustmentBean"),

    LOYALTY_TARIFF_RANGE("loyaltyTariffDayRange", DayRange.qualifiedBean()),
    LOYALTY_TARIFF("loyaltyTariffManager", Tariff.qualifiedBean()),
    LOYALTY_TARIFF_RELATION("loyaltyTariffRelationship", TariffRelation.qualifiedBean()),
    LOYALTY_DISCOUNT("loyaltyDiscountManager", "com.oathouse.ccm.cos.config.finance.LoyaltyDiscountBean"),

    CHILD_EDUCATION_PRICE_REDUCTION("childEducationPriceReductionManager", "com.oathouse.ccm.cos.config.finance.PriceListBean"),
    CHILD_EDUCATION_PRICE_REDUCTION_RANGE("childEducationPriceReductionDayRange", DayRange.qualifiedBean()),

    // Financial
    BILLING("billingManager", "com.oathouse.ccm.cos.accounts.finance.BillingBean"),
    INVOICE("invoiceManager", "com.oathouse.ccm.cos.accounts.invoice.InvoiceBean"),
    CUSTOMER_RECEIPT("customerReceiptManager","com.oathouse.ccm.cos.accounts.invoice.CustomerReceiptBean"),
    CUSTOMER_CREDIT("customerCreditManager","com.oathouse.ccm.cos.accounts.invoice.CustomerCreditBean"),
    INVOICE_CREDIT("invoiceCreditManager","com.oathouse.ccm.cos.accounts.invoice.InvoiceCreditBean"),
    PAYMENT("paymentManager","com.oathouse.ccm.cos.accounts.invoice.PaymentBean"),
    FIXED_TEMPLATE("fixedChargeTemplateManager","com.oathouse.ccm.cos.accounts.invoice.FixedChargeBean"),

    ;

    private final static String accounts = "com.oathouse.ccm.cos.accounts.";
    private final static String accountsFinance = "com.oathouse.ccm.cos.accounts.finance.";
    private final static String accountsInvoice = "com.oathouse.ccm.cos.accounts.invoice.";
    private final static String accountsTransaction = "com.oathouse.ccm.cos.accounts.transaction.";




    private String manager;
    private String bean;

    private VT(String manager, String bean) {
        this.manager = manager;
        this.bean = bean;
    }

    public String bean() {
        return bean.substring(bean.lastIndexOf('.') + 1);
    }

    public String qualifiedBean() {
        return bean;
    }

    public String manager() {
        return manager;
    }


}
