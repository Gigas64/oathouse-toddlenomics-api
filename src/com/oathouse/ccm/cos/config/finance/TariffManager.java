/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)TariffManager.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.config.finance;

import com.oathouse.oss.storage.exceptions.NoSuchIdentifierException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectSetStore;

/**
 * The {@code TariffManager} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 04-May-2012
 */
public class TariffManager extends ObjectSetStore<TariffBean> {
    public static final int NO_TARIFF = ObjectEnum.DEFAULT_ID.value();
    protected static final String NO_TARIFF_NAME = "period.none";
    protected static final String NO_TARIFF_LABEL = "#cccccc";

    /**
     * Constructs a {@code TariffManager}, passing the manager name which is used to distinguish
     * the persistently held data from other managers. Normally the manager name would be
     * the name of the class
     *
     * @param managerName a unique name to identify the manager.
     * @param dataOptions 
     */
    public TariffManager(String managerName, ObjectDataOptionsEnum... dataOptions) {
        super(managerName, dataOptions);
        TariffBean defaultTariff = new TariffBean(NO_TARIFF, NO_TARIFF_NAME, NO_TARIFF_LABEL, ObjectBean.SYSTEM_OWNED);
        super.resetDefaultObject(defaultTariff);

    }

        /**
     * extends the methods provided by ObjectManager to allow the retrieval
     * of a TariffBean by its String name.
     *
     * @param name the String name of a price list
     * @return an TariffBean
     * @throws NoSuchIdentifierException
     * @throws PersistenceException
     */
    public TariffBean getObject(String name) throws NoSuchIdentifierException, PersistenceException {
        for(TariffBean p : this.getAllObjects()) {
            if(p.getName().equalsIgnoreCase(name)) {
                return (p);
            }
        }
        throw new NoSuchIdentifierException("The name '" + name + "' does not exist in the manager");
    }
}
