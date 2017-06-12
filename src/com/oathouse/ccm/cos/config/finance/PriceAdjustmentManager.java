/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)PriceAdjustmentManager.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import static com.oathouse.ccm.cos.config.finance.BillingEnum.*;
import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code PriceAdjustmentManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 Feb 27, 2012
 */
public class PriceAdjustmentManager extends ObjectSetStore<PriceAdjustmentBean> {

    /**
     * Constructs a {@code PriceAdjustmentManager}, passing the manager name which is used to distinguish the
     * persistently held data from other managers. Normally the manager name would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     */
    public PriceAdjustmentManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, ObjectDataOptionsEnum.addToArray(dataOptions, ObjectDataOptionsEnum.PERSIST, ObjectDataOptionsEnum.MEMORY));
    }

    /**
     * extends the methods provided by ObjectManager to allow the retrieval
     * of a PriceAdjustmentBean by its String name.
     *
     * @param name the String name of a price list
     * @return an PriceAdjustmentBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public PriceAdjustmentBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(PriceAdjustmentBean p : this.getAllObjects()) {
            if(p.getName().equalsIgnoreCase(name)) {
                return (p);
            }
        }
        throw new NoSuchIdentifierException("The name '" + name + "' does not exist in the manager");
    }


    /**
     * Extension method from getAllObjects() that allows the filtering of priceAdjustmentBean objects
     * by the BillingEnums set in the billingBits. the billingArgs are optional and if omitted will
     * return all PriceAdjustmentBean objects
     *
     * @param billingArgs (optional) comma separated list of BillingEnums
     * @return a list of PriceAdjustmentBean objects
     * @throws PersistenceException
     */
    public List<PriceAdjustmentBean> getAllPriceAdjustments(BillingEnum... billingArgs) throws PersistenceException {
        // if there is no filter, return all objects
        if(billingArgs.length == 0) {
            return this.getAllObjects();
        }
        List<PriceAdjustmentBean> rtnList = new LinkedList<>();
        for(PriceAdjustmentBean priceAdjustment : this.getAllObjects()) {
            // convert the number to an enumerator list
            List<BillingEnum> paBillingEnums = BillingEnum.getAllBillingEnums(priceAdjustment.getBillingBits());
            // for each of the billing arguments check if they are in the list
            if(paBillingEnums.containsAll(Arrays.asList(billingArgs))) {
                rtnList.add(priceAdjustment);
            }
        }
        return(rtnList);
    }

    /**
     * Override of setObject to validate the values passed. This does not reject incorrect setting but 'fixes'
     * to expected values. The following adjustments will be made on your behalf.
     * <p>
     * if the rangeSd  less than 1,  BillingBit type is set to TYPE_FIXED_ITEM. This overrides the type set<br>
     * if value is less than 0, value is set to zero <br>
     * if precision less than 0, precision is set to 0<br>
     * if TYPE_FIXED_ITEM, rangeSd is set to zero, repeated is set false and durationSd to zero<br>
     * if repeated is true and durationSd less than 1, repeated is set to false and durationSd to zero<br>
     * if repeated is true and durationSd is greater than rangeSd, durationSd is set to rangeSd<br>
     * </p>
     *
     * @param ob the object to save
     * @return the new object saved
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public PriceAdjustmentBean setObject(PriceAdjustmentBean ob) throws PersistenceException, NullObjectException {
        if(ob == null) {
            throw new NullObjectException("The PriceAdjustmentBean passed has a null value");
        }
        // set the boolean to true if the Type is TYPE_FIXED_ITEM
        boolean addition = getAllBillingEnums(ob.getBillingBits()).contains(TYPE_FIXED_ITEM) ? true: false;
        // if the rangeSd is < 0 then automatic addition ignoring the TYPE set
        addition = ob.getRangeSd() < 1 ? true : addition;
        // sort out any missing billingBit values by adding defaults
        int billingBits = checkBillingBits(ob.getBillingBits(), addition);
        // if value is < 0 then set to 0
        long value = ob.getValue() < 0 ? 0 : ob.getValue();
        // if precision is less than 0 then set to 0
        int precision = ob.getPrecision() < 0 ?  0 : ob.getPrecision();
        // if this is Additions then make sure the other parameters are default
        int rangeSd = addition ? 0 : ob.getRangeSd();
        boolean repeated = addition ? false : ob.isRepeated();
        // if an addition or repeated and the duration is not set then turn of repeated
        if(addition || (repeated && ob.getRepeatDuration() < 1)) {
            repeated = false;
        }
        // if repeated is off then durationSd must be 0
        int durationSd = repeated ? ob.getRepeatDuration() : 0;
        // if repeated and the durationSd is bigger than the rangeSd then set the duration to range
        if(repeated && ob.getRepeatDuration() > ob.getRangeSd()) {
            durationSd = ob.getRangeSd();
        }
        return super.setObject(new PriceAdjustmentBean(ob.getPriceAdjustmentId(), ob.getName(), billingBits,
                value, precision, rangeSd, repeated, durationSd, ob.getOwner()));
    }



    /*
     * private method to check all the billingBits are filled in else the default is taken
     */
    private int checkBillingBits(int billingBits, boolean addition) {
        //set Default billing enums
        BillingEnum[] rtnArray = new BillingEnum[6];
        rtnArray[0] = addition ? TYPE_FIXED_ITEM : TYPE_ADJUSTMENT_ON_ALL;
        rtnArray[1] = BILL_CHARGE;
        rtnArray[2] = CALC_AS_VALUE;
        rtnArray[3] = APPLY_DISCOUNT;
        rtnArray[4] = RANGE_SOME_PART;
        rtnArray[5] = addition ? GROUP_FIXED_ITEM : GROUP_BOOKING;

        List<BillingEnum> billingList = getAllBillingEnums(billingBits);
        for(BillingEnum billing : billingList) {
            //if billing is addition then type MUST be ADDITION
            if(addition && billing.getLevel() == TYPE.getLevel()){
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
