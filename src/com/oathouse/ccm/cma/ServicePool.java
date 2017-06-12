/*
 * @(#)ServicePool.java
 *
 * Copyright:	Copyright (c) 2010
 * Company:	Oathouse.com Ltd
 */
package com.oathouse.ccm.cma;

import com.oathouse.ccm.cma.accounts.*;
import com.oathouse.ccm.cma.booking.*;
import com.oathouse.ccm.cma.config.*;
import com.oathouse.ccm.cma.file.*;
import com.oathouse.ccm.cma.profile.*;
import com.oathouse.oss.server.*;
import com.oathouse.oss.storage.exceptions.*;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.CalendarStatic;

/**
 * The {@literal ServicePool} Class allows for a the services to be retrieved without having to
 * always include the host, port and authority.
 *
 * @author Darryl Oatridge
 * @version 1.00 17-Apr-2011
 */
public class ServicePool {

    /**
     * Main constructor that sets all parameters in the OssProperties instance
     * @param host
     * @param port
     * @param portRange
     * @param storePath
     * @param authority
     * @param connection
     * @param locale
     * @param timeZone
     * @param osLogConfig
     */
    public ServicePool(String host, int port, int portRange, String storePath, String authority, OssProperties.Connection connection, String locale, String timeZone, String osLogConfig) {
        OssProperties.getInstance().setHost(host);
        OssProperties.getInstance().setPort(port);
        OssProperties.getInstance().setPortRange(portRange);
        OssProperties.getInstance().setStorePath(storePath);
        OssProperties.getInstance().setAuthority(authority);
        OssProperties.getInstance().setConnection(connection);
        OssProperties.getInstance().setLogConfigFile(osLogConfig);
        CalendarStatic.setLocale(locale);
        CalendarStatic.setTimeZone(timeZone);
    }

    public ServicePool(String host, int port, int portRange, String storePath, String authority, OssProperties.Connection connection, String osLogConfig) {
        OssProperties.getInstance().setHost(host);
        OssProperties.getInstance().setPort(port);
        OssProperties.getInstance().setPortRange(portRange);
        OssProperties.getInstance().setStorePath(storePath);
        OssProperties.getInstance().setAuthority(authority);
        OssProperties.getInstance().setConnection(connection);
        OssProperties.getInstance().setLogConfigFile(osLogConfig);
    }

    public ServicePool(String host, int port, int portRange, String storePath, String authority, OssProperties.Connection connection) {
        OssProperties.getInstance().setHost(host);
        OssProperties.getInstance().setPort(port);
        OssProperties.getInstance().setPortRange(portRange);
        OssProperties.getInstance().setStorePath(storePath);
        OssProperties.getInstance().setAuthority(authority);
        OssProperties.getInstance().setConnection(connection);
    }

    public ServicePool(String storePath, String authority, OssProperties.Connection connection) {
        OssProperties.getInstance().setStorePath(storePath);
        OssProperties.getInstance().setAuthority(authority);
        OssProperties.getInstance().setConnection(connection);
    }

    public ServicePool(String storePath, String authority) {
        this(storePath, authority, OssProperties.Connection.FILE);
    }

    public ServicePool(String storePath, String authority, String osLogConfig) {
        this(storePath, authority, OssProperties.Connection.FILE);
    }

    public ServicePool(String host, int port, int portRange, String authority, OssProperties.Connection connection) {
        OssProperties.getInstance().setHost(host);
        OssProperties.getInstance().setPort(port);
        OssProperties.getInstance().setAuthority(authority);
        OssProperties.getInstance().setConnection(connection);
        OssProperties.getInstance().setPortRange(portRange);
    }

    public ServicePool(String host, int port, int portRange, String authority) {
        this(host, port, portRange, authority, OssProperties.Connection.NIO);
    }

    public ServicePool(String host, int port, int portRange, String authority, String osLogConfig) {
        this(host, port, portRange, authority, OssProperties.Connection.NIO);
    }

    public ServicePool(int port, int portRange, String authority) {
        this("localhost", port, portRange, authority, OssProperties.Connection.NIO);
    }

    public ServicePool(int port, String authority) {
        this("localhost", port, 1, authority, OssProperties.Connection.NIO);
    }

    public ServicePool init() throws PersistenceException, NoSuchIdentifierException {
        PropertiesService.getInstance();
        AgeRoomService.getInstance();
        TimetableService.getInstance();

        ChildBookingService.getInstance();
        ChildBookingRequestService.getInstance();
        ChildBookingHistory.getInstance();
        StaffBookingService.getInstance();

        PriceConfigService.getInstance();

        BillingService.getInstance();
        FinancialService.getInstance();
        BillingService.getInstance();
        BookingForecastService.getInstance();

        ChildService.getInstance();
        StaffService.getInstance();

        FileService.getInstance();
        return(this);
    }

    public ServicePool reInitialise() throws PersistenceException, NoSuchIdentifierException {
        PropertiesService.getInstance().reInitialise();
        AgeRoomService.getInstance().reInitialise();
        TimetableService.getInstance().reInitialise();

        ChildBookingService.getInstance().reInitialise();
        ChildBookingRequestService.getInstance().reInitialise();
        ChildBookingHistory.getInstance().reInitialise();
        StaffBookingService.getInstance().reInitialise();

        PriceConfigService.getInstance().reInitialise();

        TransactionService.getInstance().reInitialise();
        FinancialService.getInstance().reInitialise();
        BillingService.getInstance().reInitialise();
        BookingForecastService.getInstance().reInitialise();

        ChildService.getInstance().reInitialise();
        StaffService.getInstance().reInitialise();

        FileService.getInstance().reInitialise();
        return this;
    }

    public PropertiesService getPropertiesService() throws PersistenceException {
        return (PropertiesService.getInstance());
    }

    public AgeRoomService getAgeRoomService() throws PersistenceException {
        return (AgeRoomService.getInstance());
    }

    public TimetableService getTimetableService() throws PersistenceException {
        return (TimetableService.getInstance());
    }

    public ChildBookingService getChildBookingService() throws PersistenceException {
        return (ChildBookingService.getInstance());
    }

    public ChildBookingRequestService getChildBookingRequestService() throws PersistenceException {
        return (ChildBookingRequestService.getInstance());
    }

    public ChildBookingHistory getChildBookingHistory() throws PersistenceException {
        return (ChildBookingHistory.getInstance());
    }

    public StaffBookingService getStaffBookingService() throws PersistenceException {
        return (StaffBookingService.getInstance());
    }

    public PriceConfigService getPriceConfigService() throws PersistenceException, NoSuchIdentifierException {
        return (PriceConfigService.getInstance());
    }

    public FinancialService getFinancialService() throws PersistenceException, NoSuchIdentifierException {
        return (FinancialService.getInstance());
    }

    public TransactionService getTransactionService() throws PersistenceException {
        return TransactionService.getInstance();
    }

    public BillingService getBillingService() throws PersistenceException, NoSuchIdentifierException {
        return (BillingService.getInstance());
    }

    public BookingForecastService getBookingForecastService() throws PersistenceException, NoSuchIdentifierException {
        return (BookingForecastService.getInstance());
    }

    public StaffService getStaffService() throws PersistenceException {
        return (StaffService.getInstance());
    }

    public ChildService getChildService() throws PersistenceException {
        return (ChildService.getInstance());
    }

    public FileService getFileService() throws PersistenceException, NoSuchIdentifierException {
        return (FileService.getInstance());
    }

    public void setCalendarStatic(String locale, String timeZone) {
        CalendarStatic.setLocale(locale);
        CalendarStatic.setTimeZone(timeZone);
    }

    public boolean clearAll() {
        PropertiesService.removeInstance();
        AgeRoomService.removeInstance();
        TimetableService.removeInstance();
        ChildBookingService.removeInstance();
        ChildBookingRequestService.removeInstance();
        ChildBookingHistory.removeInstance();
        StaffBookingService.removeInstance();
        PriceConfigService.removeInstance();
        TransactionService.removeInstance();
        FinancialService.removeInstance();
        BillingService.removeInstance();
        BookingForecastService.removeInstance();
        StaffService.removeInstance();
        ChildService.removeInstance();
        FileService.removeInstance();

        return ObjectDBMS.clearAuthority(OssProperties.getInstance().getAuthority());
    }

    public boolean clear() throws PersistenceException, NoSuchIdentifierException {
        boolean success = true;
        if(PropertiesService.hasInstance()) {
            success = PropertiesService.getInstance().clear()?success:false;
        }
        if(AgeRoomService.hasInstance()) {
            success = AgeRoomService.getInstance().clear()?success:false;
        }
        if(TimetableService.hasInstance()) {
            success = TimetableService.getInstance().clear()?success:false;
        }
        if(ChildBookingService.hasInstance()) {
            success = ChildBookingService.getInstance().clear()?success:false;
        }
        if(ChildBookingRequestService.hasInstance()) {
            success = ChildBookingRequestService.getInstance().clear()?success:false;
        }
        if(ChildBookingHistory.hasInstance()) {
            success = ChildBookingHistory.getInstance().clear()?success:false;
        }
        if(StaffBookingService.hasInstance()) {
            success = StaffBookingService.getInstance().clear()?success:false;
        }
        if(PriceConfigService.hasInstance()) {
            success = PriceConfigService.getInstance().clear()?success:false;
        }
        if(FinancialService.hasInstance()) {
            success = FinancialService.getInstance().clear()?success:false;
        }
        if(TransactionService.hasInstance()) {
            success = TransactionService.getInstance().clear()?success:false;
        }
        if(BillingService.hasInstance()) {
            success = BillingService.getInstance().clear()?success:false;
        }
        if(BookingForecastService.hasInstance()) {
            success = BookingForecastService.getInstance().clear()?success:false;
        }
        if(StaffService.hasInstance()) {
            success = StaffService.getInstance().clear()?success:false;
        }
        if(ChildService.hasInstance()) {
            success = ChildService.getInstance().clear()?success:false;
        }
        if(FileService.hasInstance()) {
            success = FileService.getInstance().clear()?success:false;
        }
        return success;
    }

}
