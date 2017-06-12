/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)LoyaltyDiscountManager.java
 *
 * Copyright:	Copyright (c) Error: on line 9, column 33 in Templates/Classes/ObjectManager.java
Expecting a date here, found: Feb 20, 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.List;

/**
 * The {@code LoyaltyDiscountManager} Class extends the methods of the parent class.
 * LoyaltyDiscountBean Objects are fix Objects defined by the BillingBits TYPE_LOYALTY.
 * The beans are created automatically at initialisation and can be reset using the
 * setObject() method. A LoyaltyDiscountBean will exist for each of the
 * BillingBits.TYPE_LOYALTY bits
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 20, 2012
 */
public class LoyaltyDiscountManager extends ObjectSetStore<LoyaltyDiscountBean> {

    /**
     * Constructs a {@code LoyaltyDiscountManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public LoyaltyDiscountManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, ObjectDataOptionsEnum.addToArray(dataOptions, ObjectDataOptionsEnum.PERSIST, ObjectDataOptionsEnum.MEMORY));
    }

    /**
     * extends the methods provided by ObjectManager to allow the retrieval
     * of a LoyaltyDiscountBean by its String name.
     *
     * @param name the String name of a price list
     * @return an LoyaltyDiscountBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public LoyaltyDiscountBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(LoyaltyDiscountBean p : this.getAllObjects()) {
            if(p.getName().equalsIgnoreCase(name)) {
                return (p);
            }
        }
        throw new NoSuchIdentifierException("The name '" + name + "' does not exist in the manager");
    }

    /**
     * A static utility method for ensuring all billingBits are correctly set. The only LoyaltyDiscount billingBits
     * that can be changed from the default is the RANGE type. The default values are TYPE_LOYALTY, BILL_CREDIT,
     * CALC_AS_VALUE and RANGE_AT_LEAST.
     *
     * @param billingBits the billing bits to complete
     * @return a complete set of billing bits
     */
    public static int getCompleteBillingBits(int billingBits) {
        //set Default billing enums
        BillingEnum[] rtnArray = new BillingEnum[6];
        rtnArray[0] = TYPE_LOYALTY;
        rtnArray[1] = BILL_CREDIT;
        rtnArray[2] = CALC_AS_VALUE;
        rtnArray[3] = APPLY_NO_DISCOUNT;
        rtnArray[4] = RANGE_AT_LEAST;
        rtnArray[5] = GROUP_LOYALTY;

        List<BillingEnum> billingList = getAllBillingEnums(billingBits);
        for(BillingEnum billing : billingList) {
            // Billing must be TYPE_LOYALTY, BILL_CREDIT, and GROUP_LOYALTY.
            if(billing.getLevel() == TYPE.getLevel()
                    || billing.getLevel() == BILL.getLevel()
                    || billing.getLevel() == GROUP.getLevel()){
                continue;
            }
            // replace the DEFAULT with any BillingEnum that are set
            if(billing.getLevel() > 0) {
                int index = billing.getLevel() - 1;
                rtnArray[index] = billing;
            }
        }
        return(getBillingBits(rtnArray));
    }

}
