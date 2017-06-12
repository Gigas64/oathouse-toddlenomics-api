/*
 * @(#)PriceListManager.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;
import com.oathouse.oss.storage.valueholder.SDHolder;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The {@code PriceListManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.01 11-Dec-2010
 */
public class PriceListManager extends ObjectSetStore<PriceListBean> {

    /** useful static to identify the default PriceListBean which has an id of 0 */
    public static final int NO_PRICE_LIST = 0;
    protected static final String NO_PRICE_LIST_NAME = "period.none";
    protected static final String NO_PRICE_LIST_LABEL = "#cccccc";

    /**
     * Constructs a {@code PriceListManager}, passing the root path of where all persistence data
     * is to be held. Additionally the manager name is used to distinguish the persistently held
     * data from other managers stored under the same root path. Normally the manager name
     * would be the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public PriceListManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        PriceListBean defaultPriceList = new PriceListBean(NO_PRICE_LIST, NO_PRICE_LIST_NAME, NO_PRICE_LIST_LABEL,
                new ConcurrentSkipListMap<Integer, Long>(), "system.default");
        super.resetDefaultObject(defaultPriceList);
    }

    /**
     * extends the methods provided by ObjectManager to allow the retrieval
     * of a PriceListBean by its String name.
     *
     * @param name the String name of a price list
     * @return PriceListBean Object
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public PriceListBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(PriceListBean p : this.getAllObjects()) {
            if(p.getName().equalsIgnoreCase(name)) {
                return (p);
            }
        }
        throw new NoSuchIdentifierException("The name '" + name + "' does not exist in this manager");
    }

    /**
     * Overridden to allow checking of periodSdValues to ensure there is no periodSd overlaps
     *
     * @param ob the PriceListBean to be stored
     * @return the PriceListBean that was newly stored
     * @throws PersistenceException
     * @throws NullObjectException
     */
    @Override
    public PriceListBean setObject(PriceListBean ob) throws PersistenceException, NullObjectException {
        // test there are no overlaps in the costings
        Set<Integer> testSet = ob.getPeriodSdValues().keySet();
        if(testSet == null || testSet.isEmpty()) {
            throw new NullObjectException("There are no periodSd values in the PriceListBean");
        }
        if(SDHolder.hasOverlap(testSet)) {
            throw new PersistenceException("A periodSd in the priceListBean overlaps with another");
        }
        return super.setObject(ob);
    }
}
