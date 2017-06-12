package com.oathouse.ccm.cma.booking;

import static com.oathouse.ccm.cma.VABoolean.*;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.oss.storage.exceptions.PersistenceException;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import com.oathouse.oss.storage.objectstore.ObjectDataOptionsEnum;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingModel_getModelBookingManager {

    final int yw = 2012050;
    final int accountId = 10;
    final int bookingTypeMask = 0;
    final int bookingTypeFlagMask = 0;
    final boolean includeChildEducationSd = false;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {


    }

    /**
     * Unit test: with includeLiveBooking = false
     */
    @Test
    public void getModelBookingManager_NoLiveBookings() throws Exception {

        new Expectations() {
            @Mocked(methods = {"getModelBookingManager"}, inverse=true) ChildBookingModel model;
            BookingManager managerMock;
            {
                managerMock = new BookingManager("bookingManagerModel", ObjectDataOptionsEnum.MEMORY);
                managerMock.init();
                ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask); times = 0;
                ChildBookingModel.setBookingRequests(managerMock, yw, includeChildEducationSd); returns(managerMock); times = 1;
            }
        };
        BookingManager manager = ChildBookingModel.getModelBookingManager(yw, bookingTypeMask, bookingTypeFlagMask);
        assertNotNull(manager);
    }

    /**
     * Unit test: with includeLiveBooking = false
     */
    @Test
    public void getModelBookingManager_LiveBookings(@Mocked(methods = {"getModelBookingManager"}, inverse=true) final ChildBookingModel model) throws Exception {

        new Expectations() {
            BookingManager managerMock;
            {
                managerMock = new BookingManager("bookingManagerModel", ObjectDataOptionsEnum.MEMORY);
                managerMock.init();
                ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask); returns(managerMock); times = 1;
                ChildBookingModel.setBookingRequests(managerMock, yw, includeChildEducationSd); returns(managerMock); times = 1;
            }
        };
        boolean includeLiveBookings = true;
        BookingManager manager = ChildBookingModel.getModelBookingManager(yw, bookingTypeMask, bookingTypeFlagMask, INCLUDE_LIVE_BOOKINGS);
        assertNotNull(manager);
    }

    /**
     * Unit test: with includeLiveBooking = false
     */
    @Test
    public void getModelBookingManager_NoLiveBookings_WithAccount() throws Exception {

        new Expectations() {
            @Mocked(methods = {"getModelBookingManager"}, inverse=true) ChildBookingModel model;
            BookingManager managerMock;
            {
                managerMock = new BookingManager();
                managerMock.init();
                ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask); times = 0;
                ChildBookingModel.setBookingRequests(managerMock, accountId, yw, includeChildEducationSd); returns(managerMock); times = 1;
            }
        };
        BookingManager manager = ChildBookingModel.getModelBookingManager(accountId, yw, bookingTypeMask, bookingTypeFlagMask);
        assertNotNull(manager);
    }

    /**
     * Unit test: with includeLiveBooking = false
     */
    @Test
    public void getModelBookingManager_LiveBookings_WithAccount(@Mocked(methods = {"getModelBookingManager"}, inverse=true) final ChildBookingModel model) throws Exception {

        new Expectations() {
            BookingManager managerMock;
            {
                managerMock = new BookingManager();
                managerMock.init();
                ChildBookingModel.setLiveBookings(managerMock, yw, bookingTypeMask, bookingTypeFlagMask); returns(managerMock); times = 1;
                ChildBookingModel.setBookingRequests(managerMock, accountId, yw, includeChildEducationSd); returns(managerMock); times = 1;
            }
        };
        BookingManager manager = ChildBookingModel.getModelBookingManager(accountId, yw, bookingTypeMask, bookingTypeFlagMask, INCLUDE_LIVE_BOOKINGS);
        assertNotNull(manager);
    }

}
