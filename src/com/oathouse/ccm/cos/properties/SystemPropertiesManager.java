/*
 * @(#)SystemPropertiesManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.properties;

import com.oathouse.ccm.cos.accounts.transaction.PaymentPeriodEnum;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSingleStore;
import java.util.Set;

/**
 * The {@code SystemPropertiesManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.01 20-Oct-2010
 */
public class SystemPropertiesManager extends ObjectSingleStore<SystemPropertiesBean> {

    /**
     * Constructs a {@code SystemPropertiesManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public SystemPropertiesManager(String managerName) {
        super(managerName);
    }

    /**
     * Changes base module attributes
     * @param confirmedPeriodWeeks
     * @param actualsPeriodWeeks
     * @param adminSuspended
     * @param reinstateBTIdBit
     * @param bookingActualLimit
     * @param legacySystem
     * @param closedDayIncludedInNoticePeriod
     * @param loyaltyApplyedToEducationDays
     * @param actualDropOffIdMandatory
     * @param actualPickupIdMandatory
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectBaseModule( int confirmedPeriodWeeks,
            int actualsPeriodWeeks,
            boolean adminSuspended,
            int reinstateBTIdBit,
            int bookingActualLimit,
            boolean closedDayIncludedInNoticePeriod,
            boolean loyaltyApplyedToEducationDays,
            LegacySystem legacySystem,
            boolean actualDropOffIdMandatory,
            boolean actualPickupIdMandatory,
            String owner) throws NoSuchIdentifierException, PersistenceException {

        SystemPropertiesBean bean = getObject();
        bean.setConfirmedPeriodWeeks(confirmedPeriodWeeks, owner);
        bean.setActualsPeriodWeeks(actualsPeriodWeeks, owner);
        bean.setAdminSuspended(adminSuspended, owner);
        bean.setReinstateBTIdBit(reinstateBTIdBit, owner);
        bean.setBookingActualLimit(bookingActualLimit, owner);
        bean.setClosedDayIncludedInNoticePeriod(closedDayIncludedInNoticePeriod, owner);
        bean.setLoyaltyApplyedToEducationDays(loyaltyApplyedToEducationDays, owner);
        bean.setLegacySystem(legacySystem, owner);
        bean.setActualDropOffIdMandatory(actualDropOffIdMandatory, owner);
        bean.setActualPickupIdMandatory(actualPickupIdMandatory, owner);
        setObject(bean);
    }

    /**
     * Changes the maxAgeMonths attribute
     *
     * @param maxAgeMonths
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectMaxAgeMonths(int maxAgeMonths, String owner)
            throws NoSuchIdentifierException, PersistenceException {
        SystemPropertiesBean bean = getObject();
        bean.setMaxAgeMonths(maxAgeMonths, owner);
        setObject(bean);
    }

    /**
     * Changes plan module attributes
     * @param toExceedCapacityWhenInsertingRequests
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectPlanModule(boolean toExceedCapacityWhenInsertingRequests, String owner)
            throws NoSuchIdentifierException, PersistenceException {
        SystemPropertiesBean bean = getObject();
        bean.setPlanModule(toExceedCapacityWhenInsertingRequests, owner);
        setObject(bean);
    }

    /**
     * Changes finance module attributes
     * @param chargeMargin
     * @param dropOffChargeMargin
     * @param pickupChargeMargin
     * @param bookingsTaxable
     * @param taxRate
     * @param firstInvoiceYwd
     * @param creditCardFeeRate
     * @param paymentInstructions
     * @param paymentPeriod
     * @param regularPaymentInstructions
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectFinanceModule(boolean chargeMargin,
            int dropOffChargeMargin,
            int pickupChargeMargin,
            boolean bookingsTaxable,
            int taxRate,
            int firstInvoiceYwd,
            int creditCardFeeRate,
            String paymentInstructions,
            PaymentPeriodEnum paymentPeriod,
            String regularPaymentInstructions,
            String owner) throws NoSuchIdentifierException, PersistenceException {
        SystemPropertiesBean bean = getObject();
        bean.setChargeMargin(chargeMargin, owner);
        bean.setDropOffChargeMargin(dropOffChargeMargin, owner);
        bean.setPickupChargeMargin(pickupChargeMargin, owner);
        bean.setBookingsTaxable(bookingsTaxable, owner);
        bean.setTaxRate(taxRate, owner);
        bean.setFirstInvoiceYwd(firstInvoiceYwd, owner);
        bean.setCreditCardFeeRate(creditCardFeeRate, owner);
        bean.setPaymentInstructions(paymentInstructions, owner);
        bean.setPaymentPeriod(paymentPeriod, owner);
        bean.setRegularPaymentInstructions(regularPaymentInstructions, owner);
        setObject(bean);
    }

    /**
     * Sets the default invoice settings
     * @param defaultInvoiceLastYwd
     * @param defaultInvoiceDueYwd
     * @param owner
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public void setObjectDefaultInvoiceSettings(RelativeDate defaultInvoiceLastYwd,
            RelativeDate defaultInvoiceDueYwd,
            String owner) throws NoSuchIdentifierException, PersistenceException {
        SystemPropertiesBean bean = getObject();
        bean.setDefaultInvoiceLastYwd(defaultInvoiceLastYwd, owner);
        bean.setDefaultInvoiceDueYwd(defaultInvoiceDueYwd, owner);
        setObject(bean);
    }

    /**
     * Sets the minimum number of staff allowed in each room in the nursery
     *
     * @param minStaff
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setObjectMinStaff(int minStaff, String owner) throws PersistenceException, NoSuchIdentifierException {
        SystemPropertiesBean bean = getObject();
        bean.setMinStaff(minStaff, owner);
        setObject(bean);
    }

    /**
     * Sets the default text value for standard and special BookingTypeId representation
     *
     * @param textBTIdStandard
     * @param textBTIdSpecial
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setObjectTextBTId(String textBTIdStandard, String textBTIdSpecial, String owner) throws PersistenceException, NoSuchIdentifierException {
        SystemPropertiesBean bean = getObject();
        bean.setTextBTIdStandard(textBTIdStandard, owner);
        bean.setTextBTIdSpecial(textBTIdSpecial, owner);
        setObject(bean);
    }

    /**
     * Sets the periodSd values for default occupancy trends. This allows certain times of the day
     * to be identified for monitoring of occupancy trends.
     *
     * @param occupancySdSet
     * @param owner
     * @throws PersistenceException
     * @throws NoSuchIdentifierException
     */
    public void setObjectOccupancySdSet(Set<Integer> occupancySdSet, String owner) throws PersistenceException, NoSuchIdentifierException {
        SystemPropertiesBean bean = getObject();
        bean.setOccupancySdSet(occupancySdSet, owner);
        setObject(bean);
    }
}
