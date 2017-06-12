/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingTypeManager;
import com.oathouse.ccm.cos.bookings.BookingState;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.exceptions.RoomClosedException;
import static com.oathouse.oss.storage.valueholder.CalendarStatic.getRelativeYW;
import static com.oathouse.oss.storage.valueholder.CalendarStatic.getRelativeYWD;
import java.util.Arrays;
import java.util.List;
import mockit.Cascading;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingService_ValidateBookings {

    @Mocked private BookingBean bookingMock;
    @Mocked private BookingTypeBean bookingTypeMock;
    @Mocked private BookingManager bookingManagerMock;
    @Cascading private BookingTypeManager bookingTypeManagerMock;
    @Cascading PropertiesService propertiesServiceMock;

    private ChildBookingService bookingService;


    @Before
    public void setUp() throws Exception {
        bookingService = ChildBookingService.getInstance();

    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_StateTypeFails() throws Exception {
        // Record
        new NonStrictExpectations() {
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);

                propertiesServiceMock.getSystemProperties().getConfirmedPeriodWeeks(); result = 1;
                bookingManagerMock.getYwdBookings(anyInt, anyInt, anyInt, anyInt); returns(Arrays.asList(bookingMock));
                bookingMock.isStateType(BookingState.TYPE_BOOKING); result = false;
            }
        };
        // Replay
        bookingService.validateBookings();
     }

    /**
     * Unit test:
     */
    @Test
    public void unit01_ClosedFlagTrue() throws Exception {
        // Record
        new NonStrictExpectations() {
            @Mocked({"getValidBookingSd"}) ChildBookingService bookingService;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);

                propertiesServiceMock.getSystemProperties().getConfirmedPeriodWeeks(); result = 1;
                bookingManagerMock.getYwdBookings(anyInt, anyInt, anyInt, anyInt); returns(Arrays.asList(bookingMock));
                bookingMock.isStateType(BookingState.TYPE_BOOKING); result = true;
                bookingService.getValidBookingSd(bookingMock, anyBoolean, anyInt); result = new RoomClosedException("RoomClosed"); result = 10;
                bookingMock.getBookingSd(); result = 10;
                bookingTypeManagerMock.getObject(bookingMock.getBookingTypeId()); returns(bookingTypeMock);
                bookingTypeMock.isFlagOn(anyInt); result = true; times = 1;
            }
        };
        // Replay
        List<BookingExceptionDTO> rtnList = bookingService.validateBookings();
     }

    /**
     * Unit test:
     */
    @Test
    public void unit01_ClosedFlagFalse() throws Exception {
        // Record
        new NonStrictExpectations() {
            @Mocked({"getValidBookingSd"}) ChildBookingService bookingService;
            {
                PropertiesService.getInstance(); returns(propertiesServiceMock);

                propertiesServiceMock.getSystemProperties().getConfirmedPeriodWeeks(); result = 1;
                bookingManagerMock.getYwdBookings(anyInt, anyInt, anyInt, anyInt); returns(Arrays.asList(bookingMock));
                bookingMock.isStateType(BookingState.TYPE_BOOKING); result = true;
                bookingService.getValidBookingSd(bookingMock, anyBoolean, anyInt); result = new RoomClosedException("RoomClosed"); result = 12; result = 10;
                bookingMock.getBookingSd(); result = 10;
                bookingTypeManagerMock.getObject(bookingMock.getBookingTypeId()); returns(bookingTypeMock);
                bookingTypeMock.isFlagOn(anyInt); result = false; times = 1;
            }
        };
        // Replay
        List<BookingExceptionDTO> rtnList = bookingService.validateBookings();
     }

}