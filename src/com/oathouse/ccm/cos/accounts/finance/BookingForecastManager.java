/*
 * @(#)BookingForecastManager.java
 *
 * Copyright:	Copyright (c) 2013
 * Company:		Oathouse.com Ltd
 */
package com.oathouse.ccm.cos.accounts.finance;

import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectMapStore;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code BookingForecastManager} Class extends the methods of the parent class.
 * This class is used to model and forecast billing for the future, looking at a set
 * period for each account
 *
 * Key = ywd
 *
 * @author Darryl Oatridge
 * @version 1.00 20-Mar-2013
 */
public class BookingForecastManager extends ObjectMapStore<BookingForecastBean> {

    /**
     * Constructs a {@code BillingManager} used for memory store only so will only persist
     * for the life of the instance.
     */
    public BookingForecastManager() {
        super();
    }

    /**
     * Housekeeping Method: clears out all ywd key references that are before the
     * given cut ywd.
     *
     * @param cutYwd the YWDHolder value cut off
     * @throws PersistenceException
     */
    public void doHousekeeping(int cutYwd) throws PersistenceException {
        for(int ywd : getAllKeys()) {
            if(ywd < cutYwd) {
                this.removeKey(ywd);
                continue;
            }
            // keys are in numerical order so as soon as it equals or greater then exit
            break;
        }
    }

    /**
     * Get Method for all forecasts on a ywd filtered by accountId.
     * @param ywd the ywd key value
     * @param accountId the account to filter on
     * @return a list of all BookingForecastBean objects for an account on a ywd
     * @throws PersistenceException
     */
    public List<BookingForecastBean> getAllForecastsForAccount(int ywd, int accountId) throws PersistenceException {
        List<BookingForecastBean> rtnList = new LinkedList<>();
        for(BookingForecastBean forecast : getAllObjects(ywd)) {
            if(forecast.getAccountId() == accountId) {
                rtnList.add(forecast);
            }
        }
        return rtnList;
    }
}
