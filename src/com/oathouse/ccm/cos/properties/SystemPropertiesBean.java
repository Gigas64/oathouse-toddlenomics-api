/*
 * @(#)SystemPropertiesBean.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.properties;

import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jdom2.Element;

/**
 * The {@code SystemPropertiesBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.01 22-Sept-2011
 */
public class SystemPropertiesBean extends ObjectBean {
    private static final long serialVersionUID = 20110922101L;

    // base module attributes
    private volatile int confirmedPeriodWeeks; // weeks ahead that requests are converted to bookings
    private volatile int actualsPeriodWeeks; // used by the front end
    private volatile int maxAgeMonths; // the maximum age of children in the nursery
    private volatile boolean adminSuspended; // locks system while superuser makes changes
    private volatile int reinstateBTIdBit; //BTBits.ATTENDING or BTBits.WAITING
    private volatile int bookingActualLimit; // used in the presentation layer
    private volatile boolean closedDayIncludedInNoticePeriod; // if closed days should be included in the notice period
    private volatile boolean loyaltyApplyedToEducationDays; // loyaltyies apply to education days
    private volatile LegacySystem legacySystem; // what was theold system they used
    private volatile boolean actualDropOffIdMandatory; // the actual dropOffId must be entered before a booking is complete
    private volatile boolean actualPickupIdMandatory; // the actual pickupId must be entered before a booking can be complete
    private volatile int minStaff; // The minimum staff allowed in a room across the nursery

    // display settings
    private volatile String textBTIdSpecial; // the default text for Special bookings
    private volatile String textBTIdStandard; //the display text for Standard bookings
    private final Set<Integer> occupancySdSet; // a set of default periodSd when occupancy variants want to be recorded

    // planning module attributes
    private volatile boolean toExceedCapacityWhenInsertingRequests; // Booking requests to bookings can exceed capacity

    // finance module attributes
    private volatile boolean chargeMargin; // if there is a charge margin
    private volatile int dropOffChargeMargin; // number of minutes margin allowed for dropoff
    private volatile int pickupChargeMargin;  // number of minutes margin allowed for pickup
    private volatile int taxRate;       // stored as rate * 100 (eg 17.50% is stored as 1750)
    private volatile boolean bookingsTaxable;
    /*
     * the first ywd to be invoiced: required because this system is replacing some other system
     * and there needs to be a defined change-over
     */
    private volatile int firstInvoiceYwd;
    private volatile int creditCardFeeRate;    // stored as rate * 100 (eg 3.00% is stored as 300)
    private volatile String paymentInstructions;    // text for account summary
    private volatile RelativeDate defaultInvoiceLastYwd;
    private volatile RelativeDate defaultInvoiceDueYwd;
    private volatile PaymentPeriodEnum paymentPeriod;
    private volatile String regularPaymentInstructions;

    public SystemPropertiesBean(int confirmedPeriodWeeks, int actualsPeriodWeeks, int maxAgeMonths,
            boolean adminSuspended, int reinstateBTIdBit, int bookingActualLimit, boolean closedDayIncludedInNoticePeriod,
            boolean loyaltyApplyedToEducationDays, LegacySystem legacySystem, boolean actualDropOffIdMandatory,
            boolean actualPickupIdMandatory, int minStaff, String textBTIdSpecial, String textBTIdStandard,
            Set<Integer> occupancySdSet, boolean toExceedCapacityWhenInsertingRequests, boolean chargeMargin,
            int dropOffChargeMargin, int pickupChargeMargin, int taxRate, boolean bookingsTaxable, int firstInvoiceYwd,
            int creditCardFeeRate, String paymentInstructions, RelativeDate defaultInvoiceLastYwd,
            RelativeDate defaultInvoiceDueYwd, PaymentPeriodEnum paymentPeriod, String regularPaymentInstructions,
            int identifier, String owner) {
        super(identifier, owner);
        this.confirmedPeriodWeeks = confirmedPeriodWeeks;
        this.actualsPeriodWeeks = actualsPeriodWeeks;
        this.maxAgeMonths = maxAgeMonths;
        this.adminSuspended = adminSuspended;
        this.reinstateBTIdBit = reinstateBTIdBit;
        this.bookingActualLimit = bookingActualLimit;
        this.closedDayIncludedInNoticePeriod = closedDayIncludedInNoticePeriod;
        this.loyaltyApplyedToEducationDays = loyaltyApplyedToEducationDays;
        this.legacySystem = legacySystem;
        this.actualDropOffIdMandatory = actualDropOffIdMandatory;
        this.actualPickupIdMandatory = actualPickupIdMandatory;
        this.minStaff = minStaff;
        this.textBTIdSpecial = textBTIdSpecial;
        this.textBTIdStandard = textBTIdStandard;
        this.occupancySdSet = new ConcurrentSkipListSet<>();
        if(occupancySdSet != null) {
            synchronized (occupancySdSet) {
                this.occupancySdSet.addAll(occupancySdSet);
            }
        }
        this.toExceedCapacityWhenInsertingRequests = toExceedCapacityWhenInsertingRequests;
        this.chargeMargin = chargeMargin;
        this.dropOffChargeMargin = dropOffChargeMargin;
        this.pickupChargeMargin = pickupChargeMargin;
        this.taxRate = taxRate;
        this.bookingsTaxable = bookingsTaxable;
        this.firstInvoiceYwd = firstInvoiceYwd;
        this.creditCardFeeRate = creditCardFeeRate;
        this.paymentInstructions = paymentInstructions;
        this.defaultInvoiceLastYwd = defaultInvoiceLastYwd;
        this.defaultInvoiceDueYwd = defaultInvoiceDueYwd;
        this.paymentPeriod = paymentPeriod;
        this.regularPaymentInstructions = regularPaymentInstructions;
    }

    public SystemPropertiesBean() {
        super();
        this.confirmedPeriodWeeks = ObjectEnum.INITIALISATION.value();
        this.actualsPeriodWeeks = ObjectEnum.INITIALISATION.value();
        this.maxAgeMonths = ObjectEnum.INITIALISATION.value();
        this.adminSuspended = false;
        this.reinstateBTIdBit = ObjectEnum.INITIALISATION.value();
        this.bookingActualLimit = ObjectEnum.INITIALISATION.value();
        this.closedDayIncludedInNoticePeriod = false;
        this.loyaltyApplyedToEducationDays = false;
        this.legacySystem = LegacySystem.UNDEFINED;
        this.actualDropOffIdMandatory = false;
        this.actualPickupIdMandatory = false;
        this.toExceedCapacityWhenInsertingRequests = false;
        this.chargeMargin = false;
        this.dropOffChargeMargin = ObjectEnum.INITIALISATION.value();
        this.pickupChargeMargin = ObjectEnum.INITIALISATION.value();
        this.taxRate = ObjectEnum.INITIALISATION.value();
        this.bookingsTaxable = false;
        this.firstInvoiceYwd = ObjectEnum.INITIALISATION.value();
        this.creditCardFeeRate = ObjectEnum.INITIALISATION.value();
        this.paymentInstructions = "";
        this.defaultInvoiceLastYwd = RelativeDate.UNDEFINED;
        this.defaultInvoiceDueYwd = RelativeDate.UNDEFINED;
        this.minStaff = ObjectEnum.INITIALISATION.value();
        this.textBTIdSpecial = "";
        this.textBTIdStandard = "";
        this.occupancySdSet = new ConcurrentSkipListSet<>();
        this.paymentPeriod = PaymentPeriodEnum.UNDEFINED;
        this.regularPaymentInstructions = "";
    }

    public int getSystemPropertiesId() {
        return super.getIdentifier();
    }

    public int getActualsPeriodWeeks() {
        return actualsPeriodWeeks;
    }

    public boolean isAdminSuspended() {
        return adminSuspended;
    }

    public int getConfirmedPeriodWeeks() {
        return confirmedPeriodWeeks;
    }

    public int getMaxAgeMonths() {
        return maxAgeMonths;
    }

    public int getReinstateBTIdBit() {
        return reinstateBTIdBit;
    }

    public int getBookingActualLimit() {
        return bookingActualLimit;
    }

    /**
     * If true, days when the whole nursery is closed are not counted when calculating
     * the notice period used for cancelling bookings
     * @return
     */
    public boolean isClosedDayIncludedInNoticePeriod() {
        return closedDayIncludedInNoticePeriod;
    }

    public boolean isLoyaltyApplyedToEducationDays() {
        return loyaltyApplyedToEducationDays;
    }

    public LegacySystem getLegacySystem() {
        return legacySystem;
    }

    public boolean isActualDropOffIdMandatory() {
        return actualDropOffIdMandatory;
    }

    public boolean isActualPickupIdMandatory() {
        return actualPickupIdMandatory;
    }

    public boolean isChargeMargin() {
        return chargeMargin;
    }

    public int getDropOffChargeMargin() {
        return dropOffChargeMargin;
    }

    public int getPickupChargeMargin() {
        return pickupChargeMargin;
    }

    /**
     * If true, capacity will not be considered when inserting booking requests.
     * Insertions may exceed room capacity.
     * If false, insertions once capacity is reached will result in WAITING bookings
     * @return
     */
    public boolean isToExceedCapacityWhenInsertingRequests() {
        return toExceedCapacityWhenInsertingRequests;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public boolean isBookingsTaxable() {
        return bookingsTaxable;
    }

    public int getFirstInvoiceYwd() {
        return firstInvoiceYwd;
    }

    public int getCreditCardFeeRate() {
        return creditCardFeeRate;
    }

    public String getPaymentInstructions() {
        return paymentInstructions;
    }

    public RelativeDate getDefaultInvoiceLastYwd() {
        return defaultInvoiceLastYwd;
    }

    public RelativeDate getDefaultInvoiceDueYwd() {
        return defaultInvoiceDueYwd;
    }

    public int getMinStaff() {
        return minStaff;
    }

    public String getTextBTIdStandard() {
        return textBTIdStandard;
    }

    public String getTextBTIdSpecial() {
        return textBTIdSpecial;
    }

    public Set<Integer> getOccupancySdSet() {
        return new ConcurrentSkipListSet<>(occupancySdSet);
    }

    public PaymentPeriodEnum getPaymentPeriod() {
        return paymentPeriod;
    }

    public String getRegularPaymentInstructions() {
        return regularPaymentInstructions;
    }

    /* *********************
     * SETS
     * **********************/
    protected void setActualsPeriodWeeks(int actualsPeriodWeeks, String owner) {
        this.actualsPeriodWeeks = actualsPeriodWeeks;
        super.setOwner(owner);
    }

    protected void setAdminSuspended(boolean adminSuspended, String owner) {
        this.adminSuspended = adminSuspended;
        super.setOwner(owner);
    }

    protected void setBookingActualLimit(int bookingActualLimit, String owner) {
        this.bookingActualLimit = bookingActualLimit;
        super.setOwner(owner);
    }

    protected void setActualDropOffIdMandatory(boolean actualDropOffIdMandatory, String owner) {
        this.actualDropOffIdMandatory = actualDropOffIdMandatory;
        super.setOwner(owner);
    }

    protected void setActualPickupIdMandatory(boolean actualPickupIdMandatory, String owner) {
        this.actualPickupIdMandatory = actualPickupIdMandatory;
        super.setOwner(owner);
    }

    protected void setChargeMargin(boolean chargeMargin, String owner) {
        this.chargeMargin = chargeMargin;
        super.setOwner(owner);
    }

    protected void setDropOffChargeMargin(int dropOffChargeMargin, String owner) {
        this.dropOffChargeMargin = dropOffChargeMargin;
        super.setOwner(owner);
    }

    protected void setPickupChargeMargin(int pickupChargeMargin, String owner) {
        this.pickupChargeMargin = pickupChargeMargin;
        super.setOwner(owner);
    }

    protected void setBookingsTaxable(boolean bookingsTaxable, String owner) {
        this.bookingsTaxable = bookingsTaxable;
        super.setOwner(owner);
    }

    protected void setClosedDayIncludedInNoticePeriod(boolean closedDayIncludedInNoticePeriod, String owner) {
        this.closedDayIncludedInNoticePeriod = closedDayIncludedInNoticePeriod;
        super.setOwner(owner);
    }

    protected void setLoyaltyApplyedToEducationDays(boolean loyaltyApplyedToEducationDays, String owner) {
        this.loyaltyApplyedToEducationDays = loyaltyApplyedToEducationDays;
        super.setOwner(owner);
    }

    protected void setConfirmedPeriodWeeks(int confirmedPeriodWeeks, String owner) {
        this.confirmedPeriodWeeks = confirmedPeriodWeeks;
        super.setOwner(owner);
    }

    protected void setCreditCardFeeRate(int creditCardFeeRate, String owner) {
        this.creditCardFeeRate = creditCardFeeRate;
        super.setOwner(owner);
    }

    protected void setDefaultInvoiceDueYwd(RelativeDate defaultInvoiceDueYwd, String owner) {
        this.defaultInvoiceDueYwd = defaultInvoiceDueYwd;
        super.setOwner(owner);
    }

    protected void setDefaultInvoiceLastYwd(RelativeDate defaultInvoiceLastYwd, String owner) {
        this.defaultInvoiceLastYwd = defaultInvoiceLastYwd;
        super.setOwner(owner);
    }

    protected void setFirstInvoiceYwd(int firstInvoiceYwd, String owner) {
        this.firstInvoiceYwd = firstInvoiceYwd;
        super.setOwner(owner);
    }

    protected void setLegacySystem(LegacySystem legacySystem, String owner) {
        this.legacySystem = legacySystem;
        super.setOwner(owner);
    }

    protected void setPaymentInstructions(String paymentInstructions, String owner) {
        this.paymentInstructions = paymentInstructions;
        super.setOwner(owner);
    }

    protected void setReinstateBTIdBit(int reinstateBTIdBit, String owner) {
        this.reinstateBTIdBit = reinstateBTIdBit;
        super.setOwner(owner);
    }

    protected void setTaxRate(int taxRate, String owner) {
        this.taxRate = taxRate;
        super.setOwner(owner);
    }

    protected void setTextBTIdStandard(String textBTIdStandard, String owner) {
        this.textBTIdStandard = textBTIdStandard;
        super.setOwner(owner);
    }

    protected void setTextBTIdSpecial(String textBTIdSpecial, String owner) {
        this.textBTIdSpecial = textBTIdSpecial;
        super.setOwner(owner);
    }

    protected void setOccupancySdSet(Set<Integer> occupancySdSet, String owner) {
        if(occupancySdSet != null) {
            this.occupancySdSet.clear();
            synchronized (occupancySdSet) {
                this.occupancySdSet.addAll(occupancySdSet);
            }
            super.setOwner(owner);
        }
    }

    protected void setMaxAgeMonths(int maxAgeMonths, String owner) {
        this.maxAgeMonths = maxAgeMonths;
        super.setOwner(owner);
    }

    protected void setPlanModule(boolean toExceedCapacityWhenInsertingRequests, String owner) {
        this.toExceedCapacityWhenInsertingRequests = toExceedCapacityWhenInsertingRequests;
        super.setOwner(owner);
    }

    protected void setMinStaff(int minStaff, String owner) {
        this.minStaff = minStaff;
        super.setOwner(owner);
    }

    protected void setPaymentPeriod(PaymentPeriodEnum paymentPeriod, String owner) {
        this.paymentPeriod = paymentPeriod;
        super.setOwner(owner);
    }

    protected void setRegularPaymentInstructions(String regularPaymentInstructions, String owner) {
        this.regularPaymentInstructions = regularPaymentInstructions;
        super.setOwner(owner);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.confirmedPeriodWeeks;
        hash = 67 * hash + this.actualsPeriodWeeks;
        hash = 67 * hash + this.maxAgeMonths;
        hash = 67 * hash + (this.adminSuspended ? 1 : 0);
        hash = 67 * hash + this.reinstateBTIdBit;
        hash = 67 * hash + this.bookingActualLimit;
        hash = 67 * hash + (this.closedDayIncludedInNoticePeriod ? 1 : 0);
        hash = 67 * hash + (this.loyaltyApplyedToEducationDays ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.legacySystem);
        hash = 67 * hash + (this.actualDropOffIdMandatory ? 1 : 0);
        hash = 67 * hash + (this.actualPickupIdMandatory ? 1 : 0);
        hash = 67 * hash + this.minStaff;
        hash = 67 * hash + Objects.hashCode(this.textBTIdSpecial);
        hash = 67 * hash + Objects.hashCode(this.textBTIdStandard);
        hash = 67 * hash + Objects.hashCode(this.occupancySdSet);
        hash = 67 * hash + (this.toExceedCapacityWhenInsertingRequests ? 1 : 0);
        hash = 67 * hash + (this.chargeMargin ? 1 : 0);
        hash = 67 * hash + this.dropOffChargeMargin;
        hash = 67 * hash + this.pickupChargeMargin;
        hash = 67 * hash + this.taxRate;
        hash = 67 * hash + (this.bookingsTaxable ? 1 : 0);
        hash = 67 * hash + this.firstInvoiceYwd;
        hash = 67 * hash + this.creditCardFeeRate;
        hash = 67 * hash + Objects.hashCode(this.paymentInstructions);
        hash = 67 * hash + Objects.hashCode(this.defaultInvoiceLastYwd);
        hash = 67 * hash + Objects.hashCode(this.defaultInvoiceDueYwd);
        hash = 67 * hash + Objects.hashCode(this.paymentPeriod);
        hash = 67 * hash + Objects.hashCode(this.regularPaymentInstructions);
        return hash + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final SystemPropertiesBean other = (SystemPropertiesBean) obj;
        if(this.confirmedPeriodWeeks != other.confirmedPeriodWeeks) {
            return false;
        }
        if(this.actualsPeriodWeeks != other.actualsPeriodWeeks) {
            return false;
        }
        if(this.maxAgeMonths != other.maxAgeMonths) {
            return false;
        }
        if(this.adminSuspended != other.adminSuspended) {
            return false;
        }
        if(this.reinstateBTIdBit != other.reinstateBTIdBit) {
            return false;
        }
        if(this.bookingActualLimit != other.bookingActualLimit) {
            return false;
        }
        if(this.closedDayIncludedInNoticePeriod != other.closedDayIncludedInNoticePeriod) {
            return false;
        }
        if(this.loyaltyApplyedToEducationDays != other.loyaltyApplyedToEducationDays) {
            return false;
        }
        if(this.legacySystem != other.legacySystem) {
            return false;
        }
        if(this.actualDropOffIdMandatory != other.actualDropOffIdMandatory) {
            return false;
        }
        if(this.actualPickupIdMandatory != other.actualPickupIdMandatory) {
            return false;
        }
        if(this.minStaff != other.minStaff) {
            return false;
        }
        if(!Objects.equals(this.textBTIdSpecial, other.textBTIdSpecial)) {
            return false;
        }
        if(!Objects.equals(this.textBTIdStandard, other.textBTIdStandard)) {
            return false;
        }
        if(!Objects.equals(this.occupancySdSet, other.occupancySdSet)) {
            return false;
        }
        if(this.toExceedCapacityWhenInsertingRequests != other.toExceedCapacityWhenInsertingRequests) {
            return false;
        }
        if(this.chargeMargin != other.chargeMargin) {
            return false;
        }
        if(this.dropOffChargeMargin != other.dropOffChargeMargin) {
            return false;
        }
        if(this.pickupChargeMargin != other.pickupChargeMargin) {
            return false;
        }
        if(this.taxRate != other.taxRate) {
            return false;
        }
        if(this.bookingsTaxable != other.bookingsTaxable) {
            return false;
        }
        if(this.firstInvoiceYwd != other.firstInvoiceYwd) {
            return false;
        }
        if(this.creditCardFeeRate != other.creditCardFeeRate) {
            return false;
        }
        if(!Objects.equals(this.paymentInstructions, other.paymentInstructions)) {
            return false;
        }
        if(this.defaultInvoiceLastYwd != other.defaultInvoiceLastYwd) {
            return false;
        }
        if(this.defaultInvoiceDueYwd != other.defaultInvoiceDueYwd) {
            return false;
        }
        if(this.paymentPeriod != other.paymentPeriod) {
            return false;
        }
        if(!Objects.equals(this.regularPaymentInstructions, other.regularPaymentInstructions)) {
            return false;
        }
        return super.equals(obj);
    }


    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        for(Element e : super.getXMLElement()) {
            rtnList.add(e);
        }
        Element bean = new Element("SystemPropertiesBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("confirmedPeriodWeeks", Integer.toString(confirmedPeriodWeeks));
        bean.setAttribute("actualsPeriodWeeks", Integer.toString(actualsPeriodWeeks));
        bean.setAttribute("maxAgeMonths", Integer.toString(maxAgeMonths));
        bean.setAttribute("adminSuspended", Boolean.toString(adminSuspended));
        bean.setAttribute("reinstateBTIdBit", Integer.toString(reinstateBTIdBit));
        bean.setAttribute("bookingActualLimit", Integer.toString(bookingActualLimit));
        bean.setAttribute("closedDayIncludedInNoticePeriod", Boolean.toString(closedDayIncludedInNoticePeriod));
        bean.setAttribute("loyaltyApplyedToEducationDays", Boolean.toString(loyaltyApplyedToEducationDays));
        bean.setAttribute("legacySystem", legacySystem.toString());
        bean.setAttribute("actualDropOffIdMandatory", Boolean.toString(actualDropOffIdMandatory));
        bean.setAttribute("actualPickupIdMandatory", Boolean.toString(actualPickupIdMandatory));
        bean.setAttribute("toExceedCapacityWhenInsertingRequests", Boolean.toString(toExceedCapacityWhenInsertingRequests));
        bean.setAttribute("chargeMargin", Boolean.toString(chargeMargin));
        bean.setAttribute("dropOffChargeMargin", Integer.toString(dropOffChargeMargin));
        bean.setAttribute("pickupChargeMargin", Integer.toString(pickupChargeMargin));
        bean.setAttribute("taxRate", Integer.toString(taxRate));
        bean.setAttribute("bookingsTaxable", Boolean.toString(bookingsTaxable));
        bean.setAttribute("firstInvoiceYwd", Integer.toString(firstInvoiceYwd));
        bean.setAttribute("creditCardFeeRate", Integer.toString(creditCardFeeRate));
        bean.setAttribute("paymentInstructions", paymentInstructions);
        bean.setAttribute("defaultInvoiceLastYwd", defaultInvoiceLastYwd.toString());
        bean.setAttribute("defaultInvoiceDueYwd", defaultInvoiceDueYwd.toString());
        bean.setAttribute("minStaff", Integer.toString(minStaff));
        bean.setAttribute("textBTIdStandard", textBTIdStandard);
        bean.setAttribute("textBTIdSpecial", textBTIdSpecial);
        bean.setAttribute("paymentPeriod", paymentPeriod.toString());
        bean.setAttribute("regularPaymentInstructions", regularPaymentInstructions);
        Element allElements = new Element("occupancySdSet_set");
        bean.addContent(allElements);
        for(int holder : occupancySdSet) {
            Element eachElement = new Element("occupancySdSet");
            eachElement.setAttribute("value", Integer.toString(holder));
            allElements.addContent(eachElement);
        }

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to
     * put default values in getAttribute() and check the content
     * of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("SystemPropertiesBean");
        // set up the data
        confirmedPeriodWeeks = Integer.parseInt(bean.getAttributeValue("confirmedPeriodWeeks", "-1"));
        actualsPeriodWeeks = Integer.parseInt(bean.getAttributeValue("actualsPeriodWeeks", "-1"));
        maxAgeMonths = Integer.parseInt(bean.getAttributeValue("maxAgeMonths", "-1"));
        adminSuspended = Boolean.parseBoolean(bean.getAttributeValue("adminSuspended", "false"));
        reinstateBTIdBit = Integer.parseInt(bean.getAttributeValue("reinstateBTIdBit", "-1"));
        bookingActualLimit = Integer.parseInt(bean.getAttributeValue("bookingActualLimit", "-1"));
        closedDayIncludedInNoticePeriod = Boolean.parseBoolean(bean.getAttributeValue("closedDayIncludedInNoticePeriod", "false"));
        loyaltyApplyedToEducationDays = Boolean.parseBoolean(bean.getAttributeValue("loyaltyApplyedToEducationDays", "false"));
        legacySystem = LegacySystem.valueOf(bean.getAttributeValue("legacySystem", "NONE"));
        actualDropOffIdMandatory = Boolean.parseBoolean(bean.getAttributeValue("actualDropOffIdMandatory", "false"));
        actualPickupIdMandatory = Boolean.parseBoolean(bean.getAttributeValue("actualPickupIdMandatory", "false"));
        toExceedCapacityWhenInsertingRequests = Boolean.parseBoolean(bean.getAttributeValue("toExceedCapacityWhenInsertingRequests", "false"));
        chargeMargin = Boolean.parseBoolean(bean.getAttributeValue("chargeMargin", "false"));
        dropOffChargeMargin = Integer.parseInt(bean.getAttributeValue("dropOffChargeMargin", "-1"));
        pickupChargeMargin = Integer.parseInt(bean.getAttributeValue("pickupChargeMargin", "-1"));
        taxRate = Integer.parseInt(bean.getAttributeValue("taxRate", "0"));
        bookingsTaxable = Boolean.parseBoolean(bean.getAttributeValue("bookingsTaxable", "false"));
        firstInvoiceYwd = Integer.parseInt(bean.getAttributeValue("firstInvoiceYwd", "0"));
        creditCardFeeRate = Integer.parseInt(bean.getAttributeValue("creditCardFeeRate", "0"));
        paymentInstructions = bean.getAttributeValue("paymentInstructions", "");
        defaultInvoiceLastYwd = RelativeDate.valueOf(bean.getAttributeValue("defaultInvoiceLastYwd", "UNDEFINED"));
        defaultInvoiceDueYwd = RelativeDate.valueOf(bean.getAttributeValue("defaultInvoiceDueYwd", "UNDEFINED"));
        minStaff = Integer.parseInt(bean.getAttributeValue("minStaff", "-1"));
        textBTIdStandard = bean.getAttributeValue("textBTIdStandard", "");
        textBTIdSpecial = bean.getAttributeValue("textBTIdSpecial", "");
        paymentPeriod = PaymentPeriodEnum.valueOf(bean.getAttributeValue("paymentPeriod", "UNDEFINED"));
        regularPaymentInstructions = bean.getAttributeValue("regularPaymentInstructions", "");
        occupancySdSet.clear();
        for(Object o : bean.getChild("occupancySdSet_set").getChildren("occupancySdSet")) {
            Element eachElement = (Element) o;
            occupancySdSet.add(Integer.valueOf(eachElement.getAttributeValue("value", "-1")));
        }
    }
}
