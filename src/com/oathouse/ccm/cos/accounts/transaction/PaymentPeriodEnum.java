/*
 * @(#)PaymentPeriodEnum.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.transaction;

import java.util.*;

/**
 * The {@code PaymentPeriodEnum} Enumeration
 *
 * @author Darryl Oatridge
 * @version 1.00 22-Aug-2012
 */
public enum PaymentPeriodEnum {
    /** initialisation and error values */
    UNDEFINED,
    NO_VALUE,
    /** variable amount is paid as per invoice */
    AS_INVOICE,
    /** a yearly fixed amount is paid */
    YEARLY,
    /** a quarterly fixed amount is paid */
    QUARTERLY,
    /** a monthly fixed amount is paid */
    MONTHLY,
    /** every two weeks a fixed amount is paid */
    BI_WEEKLY,
    /** a weekly fixed amount is paid */
    WEEKLY;

    public static List<PaymentPeriodEnum> getRegularPaymentPeriods() {
        return Arrays.asList(YEARLY, QUARTERLY, MONTHLY, BI_WEEKLY, WEEKLY);
    }

    public boolean isRegularPayment() {
        return getRegularPaymentPeriods().contains(this);
    }
}
