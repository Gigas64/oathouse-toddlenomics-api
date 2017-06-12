/*
 * @(#)TariffRelationManager.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import com.oathouse.oss.storage.valueholder.MRHolder;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The {@code TariffRelationManager} Class extends the methods of the parent class. Used to map PriceTariff to PriceList
 * or PriceAdjustment. The mapping is PriceTariffId - MRHolder(MultiRefEnum,priceId) with the relationship being either
 * an internal
 *
 * @author Darryl Oatridge
 * @version 1.00 09-May-2012
 */
public class TariffRelationManager extends ObjectMapStore<TariffRelationBean> {

    /**
     * Constructs a {@code TariffRelationManager}, passing the manager name which is used to distinguish the
     * persistently held data from other managers. Normally the manager name would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public TariffRelationManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
    }

    /**
     * Method gets all priceConfigMr (MRHolder) identifers that are associated with the tariffId argument. If no
     * priceConfigArgs are provided then all priceConfigMr values associated with this tariffId are returned. Else the
     * returned values represent a filtered set of priceConfigMr by priceConfigArgs
     *
     * @param tariffId the tariff identifier to look in
     * @param priceConfigArgs zero or more MultiRefEnum
     * @return a filtered set of priceConfigMr values
     * @throws PersistenceException
     * @throws IllegalValueException
     * @see MRHolder
     * @see MultiRefEnum
     */
    public Set<Integer> getAllPriceConfigMr(int tariffId, MultiRefEnum... priceConfigArgs) throws PersistenceException, IllegalValueException {
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        // if no arguments are passed then return all identifers in this key
        if(priceConfigArgs.length == 0) {
            return this.getAllIdentifier(tariffId);
        }
        // check each relation in this key against the enumerator arguments
        for(TariffRelationBean relation : this.getAllObjects(tariffId)) {
            for(MultiRefEnum priceConfigEnum : priceConfigArgs) {
                // check the enumerator passed is of the right level
                if(!priceConfigEnum.isPriceConfig()) {
                    throw new IllegalValueException("One of the priceConfigArgs types is not a recognised PriceConfig Enumerator");
                }
                if(MRHolder.isType(relation.getTariffRelationId(), priceConfigEnum.type())) {
                    rtnSet.add(relation.getTariffRelationId());
                }
            }
        }
        return rtnSet;
    }

    /**
     * Method for retrieving all priceTariff key values where the given priceConfigMr is contained within.
     *
     * @param priceConfigMr the MRHolder reference value of the relationship
     * @return a set of priceTariffId values
     * @throws PersistenceException
     * @see MRHolder
     * @see MultiRefEnum
     */
    public Set<Integer> getAllPriceTariffForPriceConfigMr(int priceConfigMr) throws PersistenceException {
        // check the enumerator passed is of the correct level
        MultiRefEnum priceConfigEnum = MultiRefEnum.getMultiRefEnum(MRHolder.getType(priceConfigMr));
        if(!priceConfigEnum.isPriceConfig()) {
            throw new IllegalArgumentException("the priceConfigMr argument does not have a recognised MultiRefEnum type");
        }
        Set<Integer> rtnSet = new ConcurrentSkipListSet<>();
        for(int priceTariffId : this.getAllKeys()) {
            if(this.getAllIdentifier(priceTariffId).contains(priceConfigMr)) {
                    rtnSet.add(priceTariffId);
            }
        }
        return rtnSet;
    }

    //<editor-fold defaultstate="collapsed" desc="Override methods">
    @Override
    public int generateIdentifier() throws MaxCountReachedException, PersistenceException {
        throw new PersistenceException("This Manager does not support number generation");
    }

    @Override
    public int generateIdentifier(int startValue, int exclusionTimeout) throws MaxCountReachedException, PersistenceException {
        throw new PersistenceException("This Manager does not support number generation");
    }

    @Override
    public int regenerateIdentifier() throws MaxCountReachedException, PersistenceException {
        throw new PersistenceException("This Manager does not support number generation");
    }

    @Override
    public int regenerateIdentifier(int startValue,
            Set<Integer> excludes, int exclusionTimeout) throws MaxCountReachedException, PersistenceException {
        throw new PersistenceException("This Manager does not support number generation");
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    //</editor-fold>
}
