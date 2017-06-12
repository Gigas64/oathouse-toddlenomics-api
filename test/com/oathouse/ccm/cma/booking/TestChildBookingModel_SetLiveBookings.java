/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.oss.storage.exceptions.NullObjectException;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import java.util.Arrays;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingModel_SetLiveBookings {
    @Mocked BookingManager managerMock;
    final int yw = 2012050;
    final int bookingTypeMask = 0;
    final int bookingTypeFlagMask = 0;
    final boolean includeChildEducationSd = false;


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    /**
     * Unit test:
     */
    @Test
    public void test01_getLiveBookings() throws Exception {
        final List<BookingBean> testData = Arrays.asList(
                new BookingBean(),
                new BookingBean()
                );
        new Expectations() {
            ChildBookingService bookingServiceMock;
            ChildBookingRequestService requestServiceMock;
            {
                ChildBookingService.getInstance();returns(bookingServiceMock);
                ChildBookingRequestService.getInstance();returns(requestServiceMock);
                requestServiceMock.validateYw(yw);result = yw;
                managerMock.clear(); result = true;
                bookingServiceMock.getYwBookings(yw, bookingTypeMask, bookingTypeFlagMask);
                returns(testData);  times=1;
                managerMock.setCreateBooking(anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyString, anyString)
                        ; times = 2;
            }
        };
        ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask);
    }

    /**
     * Unit test:
     */
    @Test
    public void test01_getLiveBookings_NoBookings() throws Exception {
        final List<BookingBean> testData = Arrays.asList();
        new Expectations() {
            ChildBookingService bookingServiceMock;
            ChildBookingRequestService requestServiceMock;
            {
                ChildBookingService.getInstance();returns(bookingServiceMock);
                ChildBookingRequestService.getInstance();returns(requestServiceMock);
                requestServiceMock.validateYw(yw);result = yw;
                managerMock.clear(); result = true;
                bookingServiceMock.getYwBookings(yw, bookingTypeMask, bookingTypeFlagMask);
                returns(testData);  times=1;
                managerMock.setObject(anyInt, null); times = 0;
            }
        };
        ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask);
    }

    /**
     * Unit test:
     */
    @Test(expected=NullObjectException.class)
    public void test01_getLiveBookings_NullBookingModel() throws Exception {
        final List<BookingBean> testData = Arrays.asList();
        new Expectations() {
            ChildBookingService bookingServiceMock;
            ChildBookingRequestService requestServiceMock;
            {
                ChildBookingService.getInstance();returns(bookingServiceMock);
                ChildBookingRequestService.getInstance(); maxTimes = 0;;
                requestServiceMock.validateYw(yw); maxTimes = 0;
                managerMock.clear(); maxTimes = 0;
                bookingServiceMock.getYwBookings(yw, bookingTypeMask, bookingTypeFlagMask);
                maxTimes = 0;
                managerMock.setObject(anyInt, null); maxTimes = 0;
            }
        };
        ChildBookingModel.setLiveBookings(null, yw, bookingTypeMask, bookingTypeFlagMask);
    }


    /**
     * Unit test:
     */
    @Test(expected=PersistenceException.class)
    public void test01_getLiveBookings_ClearManagerException() throws Exception {
        final List<BookingBean> testData = Arrays.asList();
        new Expectations() {
            ChildBookingService bookingServiceMock;
            ChildBookingRequestService requestServiceMock;
            {
                ChildBookingService.getInstance();returns(bookingServiceMock);
                ChildBookingRequestService.getInstance();returns(requestServiceMock);
                requestServiceMock.validateYw(yw);result = yw;
                managerMock.clear(); result = false;
                bookingServiceMock.getYwBookings(yw, bookingTypeMask, bookingTypeFlagMask);
                returns(testData);  times=0;
                managerMock.setObject(anyInt, null); times = 0;
            }
        };
        ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask);
    }

}
