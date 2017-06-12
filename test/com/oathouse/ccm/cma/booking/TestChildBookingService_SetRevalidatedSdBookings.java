package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cos.bookings.BookingTypeManager;
import com.oathouse.ccm.cma.VABoolean;
import com.oathouse.ccm.cos.bookings.BTFlagBits;
import com.oathouse.ccm.cos.bookings.BookingTypeBean;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.ActionBits;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import static com.oathouse.ccm.cma.booking.BookingExceptionDTO.Type.*;
import static com.oathouse.oss.storage.objectstore.ObjectBean.SYSTEM_OWNED;
import com.oathouse.oss.storage.valueholder.*;
import static java.util.Arrays.asList;
import java.util.*;
import mockit.*;
import org.junit.*;
/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingService_SetRevalidatedSdBookings {

    @Mocked private BookingManager bookingManagerMock;
    @Cascading private BookingTypeManager bookingTypeManagerMock;
    @Mocked private BookingTypeBean bookingTypeMock;
    @Cascading PropertiesService propertiesServiceMock;

    private ChildBookingService bookingService;
    private List<BookingExceptionDTO> exceptionList;


    @Before
    public void setUp() throws Exception {

        bookingService = ChildBookingService.getInstance();

        int ywd = CalendarStatic.getRelativeYW(1);
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;
        int bookingSd = SDHolder.getSD(10, 20);
        int changeSd = SDHolder.getSD(15, 20);
        exceptionList = asList(
                new BookingExceptionDTO(ywd, 1, 10, bookingTypeId, bookingSd, bookingSd, bookingTypeId, changeSd, BOOKING_SD_CHANGE),
                new BookingExceptionDTO(ywd, 2, 11, bookingTypeId, bookingSd, bookingSd, bookingTypeId, bookingSd, BOOKING_CLOSED_ROOM)
        );
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_bookingsAreSet() throws Exception {
        // Record
        new NonStrictExpectations() {
            @Mocked({"validateBookings", "setBooking", "setBookingActionBits"}) ChildBookingService bookingService;
            BookingBean bookingMock;
            {
                bookingService.validateBookings(); returns(exceptionList);
                bookingManagerMock.getObject(anyInt, anyInt); returns(bookingMock);
                bookingMock.getBookingTypeId(); returns(99);
                bookingTypeManagerMock.getObject(99); returns(bookingTypeMock);
                bookingTypeMock.isFlagOn(BTFlagBits.BOOKING_CLOSED_FLAG); result = true;
            }
        };
        // Replay
        bookingService.setRevalidatedSdBookings(SYSTEM_OWNED);
        // Verify
        new Verifications() {{
                bookingService.setBooking(anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyInt, anyString, anyInt, anyString, VABoolean.RESET_SPANSD); times = 1;
                bookingService.setBookingActionBits(anyInt, anyInt, ActionBits.ADHOC_BOOKING_BIT, anyBoolean, anyString); times = 1;
            }
        };
    }

}