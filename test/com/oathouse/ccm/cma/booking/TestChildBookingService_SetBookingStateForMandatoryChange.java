/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingTypeManager;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import java.util.*;
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingService_SetBookingStateForMandatoryChange {

    @Mocked BookingManager managerMock;
    @Mocked BookingTypeManager unused;
    @Mocked BookingBean booking;
    ChildBookingService service;



    @Before
    public void setUp() throws Exception {
        service = ChildBookingService.getInstance();
        service.clear();
    }

    @After
    public void tearDown() {

    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_ActualDropOffMandatory() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new Expectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet); times = 1;
                managerMock.getAllObjects(1); returns(bookingList); times = 1;
                booking.getActualDropOffId(); result = -1;
                booking.isState(BookingState.COMPLETED); result = true; times = 1;
                booking.getBookingId(); result = 2;
                mock.resetBookingToStarted(1, 2, ObjectBean.SYSTEM_OWNED); times = 1;
            }
        };
        service.setBookingStateForMandatoryChange(true, false);

        new Verifications() {{
           booking.getActualPickupId(); times = 0;
           managerMock.setObjectState(anyInt, anyInt, (BookingState) any, anyString); times = 0;
        }};
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_ActualPickupMandatory() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new NonStrictExpectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet); times = 1;
                managerMock.getAllObjects(1); returns(bookingList); times = 1;
                booking.getActualPickupId(); result = -1;
                booking.isState(BookingState.COMPLETED); result = true; times = 1;
            }
        };
        service.setBookingStateForMandatoryChange(false, true);

        new Verifications() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                booking.getActualDropOffId(); times = 0;
                booking.getActualPickupId(); times = 1;
                booking.isState(BookingState.COMPLETED); times = 1;
                mock.resetBookingToStarted(anyInt, anyInt, anyString); times = 1;
                managerMock.setObjectState(anyInt, anyInt, (BookingState) any, anyString); times = 0;
            }
        };
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_PickupIdNotDefault() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new NonStrictExpectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet); times = 1;
                managerMock.getAllObjects(1); returns(bookingList); times = 1;
                booking.getActualPickupId(); result = 1;
            }
        };
        service.setBookingStateForMandatoryChange(false, true);

        new Verifications() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                booking.getActualDropOffId(); times = 0;
                booking.getActualPickupId(); times = 1;
                booking.isState((BookingState) any); times = 0;
                mock.resetBookingToStarted(anyInt, anyInt, anyString); times = 0;
                managerMock.setObjectState(anyInt, anyInt, (BookingState) any, anyString); times = 0;
            }
        };
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_BookingStateNotCompleted() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new NonStrictExpectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet); times = 1;
                managerMock.getAllObjects(1); returns(bookingList); times = 1;
                booking.getActualPickupId(); result = -1;
                booking.isState(BookingState.COMPLETED); result = false;
            }
        };
        service.setBookingStateForMandatoryChange(false, true);

        new Verifications() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                booking.getActualDropOffId(); times = 0;
                booking.getActualPickupId(); times = 1;
                mock.resetBookingToStarted(anyInt, anyInt, anyString); times = 0;
                managerMock.setObjectState(anyInt, anyInt, (BookingState) any, anyString); times = 0;
            }
        };
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_BothFalse() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new Expectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet);
                managerMock.getAllObjects(1); returns(bookingList);
                booking.getActualStart(); result = 1;
                booking.getActualEnd(); result = 1;
                booking.isState(BookingState.STARTED); result = true;
                booking.getBookingId(); result = 2;
                managerMock.setObjectState(1, 2, BookingState.COMPLETED, ObjectBean.SYSTEM_OWNED);
            }
        };
        service.setBookingStateForMandatoryChange(false, false);

    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_BothFalseWrongState() throws Exception {
        final Set<Integer> keySet = new HashSet<Integer>();
        keySet.add(1);
        final List<BookingBean> bookingList = Arrays.asList(booking);
        new Expectations() {
            @Mocked({"resetBookingToStarted"}) ChildBookingService mock;
            {
                managerMock.getAllKeys(); returns(keySet);
                managerMock.getAllObjects(1); returns(bookingList);
                booking.getActualStart(); result = 1;
                booking.getActualEnd(); result = 1;
                booking.isState(BookingState.STARTED); result = false;
                managerMock.setObjectState(anyInt, anyInt, (BookingState) any, anyString); times = 0;
            }
        };
        service.setBookingStateForMandatoryChange(false, false);

    }
}