/*
 * @(#)FinancialService.java
 *
 * Copyright:	Copyright (c) 2012
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma.accounts;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cos.accounts.invoice.*;
import com.oathouse.oss.storage.exceptions.*;

/**
 * The {@code FinancialService} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 28-Nov-2010
 */
public class FinancialService {

    // Singleton Instance
    private volatile static FinancialService INSTANCE;
    // to stop initialising when initialised
    private volatile boolean initialise = true;
    private final FixedChargeTemplateManager fixedChargeTemplateManager = new FixedChargeTemplateManager(VT.FIXED_TEMPLATE.manager());

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private FinancialService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code FinancialService} class
     *
     * @return instance of the {@code FinancialService}
     * @throws PersistenceException
     */
    public static FinancialService getInstance() throws PersistenceException {
        if(INSTANCE == null) {
            synchronized (FinancialService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new FinancialService().init();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to check if the {@code FinancialService} class has been initialised. This is used mostly for testing to avoid
     * initialisation of managers when the underlying elements of the initialisation are not available.
     *
     * @return true if an instance has been created
     */
    public static boolean hasInstance() {
        if(INSTANCE != null) {
            return (true);
        }
        return (false);
    }

    /**
     * initialise all the managed classes in the {@code FinancialService}. The method returns an instance of the {@code FinancialService}
     * so it can be chained. This must be called before a the {@code FinancialService} is used. e.g  {@code FinancialService myInvoiceService =
     * }
     *
     * @return instance of the {@code FinancialService}
     * @throws PersistenceException
     */
    public synchronized FinancialService init() throws PersistenceException {
        if(initialise) {
            fixedChargeTemplateManager.init();
        }
        initialise = false;
        return (this);
    }

    /**
     * Reinitialises all the managed classes in the {@code FinancialService}. The method returns an instance of the {@code FinancialService}
     * so it can be chained.
     *
     * @return instance of the {@code FinancialService}
     * @throws PersistenceException
     */
    public FinancialService reInitialise() throws PersistenceException {
        initialise = true;
        return (init());
    }

    /**
     * Clears all the managed classes in the {@code FinancialService}. This is generally used for testing. If you wish to
     * refresh the object store reInitialise() should be used.
     *
     * @return true if all the managers were cleared successfully
     * @throws PersistenceException
     */
    public boolean clear() throws PersistenceException {
        boolean success = true;
        success = fixedChargeTemplateManager.clear() ? success : false;
        INSTANCE = null;
        return success;
    }

    /**
     * TESTING ONLY. Use reInitialise() if you wish to reload memory. <p> Used to reset the {@code FinancialService} class
     * instance by setting the INSTANCE reference to null. This is mostly used for testing to clear and reset internal
     * memory stores when the underlying persistence data has been removed. </p>
     */
    public static void removeInstance() {
        INSTANCE = null;
    }
    //</editor-fold>

    /* ***************************************************
     * M A N A G E R   R E T R I E V A L
     * ***************************************************/

    public FixedChargeTemplateManager getFixedChargeTemplateManager() {
        return fixedChargeTemplateManager;
    }

    /* ****************************************
     * F I X E D   C H A R G E   M E T H O D S
     * ***************************************/
    /**
     * Sets a fixed charge template.  There are no referential integrity issues with this bean
     *
     * @param chargeTemplateId
     * @param description
     * @param value
     * @param notes
     * @param owner
     * @return
     * @throws NullObjectException
     * @throws PersistenceException
     * @throws MaxCountReachedException
     */
    public FixedChargeBean setFixedChargeTemplate(int chargeTemplateId, String description, long value, String notes,
            String owner)
            throws NullObjectException, PersistenceException, MaxCountReachedException {
        if(chargeTemplateId == -1) {
            chargeTemplateId = fixedChargeTemplateManager.generateIdentifier();
        }
        return (fixedChargeTemplateManager.setObject(
                new FixedChargeBean(chargeTemplateId, value, description, notes, owner)));
    }

    /**
     * Removes a fixed item template.  Templates are just convenience objects to allow users to pick items from a list.  There
     * are no referential repercussions to a straight removal.
     *
     * @param chargeTemplateId
     * @return the deleted FixChargeTemplateBean Object
     * @throws PersistenceException
     */
    public FixedChargeBean removeFixedChargeTemplate(int chargeTemplateId) throws PersistenceException {
        return fixedChargeTemplateManager.removeObject(chargeTemplateId);
    }
}
