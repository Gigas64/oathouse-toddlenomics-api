/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.cma.VT;
import com.oathouse.ccm.cma.config.AgeRoomService;
import com.oathouse.ccm.cma.config.PropertiesService;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.bookings.BookingManager;
import com.oathouse.ccm.cos.bookings.BookingRequestBean;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mockit.Cascading;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingModel_SetBookingRequests {
            @Mocked BookingRequestBean request;
            @Cascading ChildBookingRequestService requestServiceMock;
            @Cascading ChildBookingService bookingServiceMock;
            @Cascading PropertiesService propertiesServiceMock;
            @Mocked ChildService childServiceMock;
            @Cascading AgeRoomService roomService;

    @Mocked
    BookingManager managerMock;
    final int yw = 2012050;
    final int accountId = 10;
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
    public void test01_BlackBox_NonStrict_NoPrivate(@Mocked(methods = {"setBookingRequests"}, inverse=true) final ChildBookingModel model) throws Exception {
        new NonStrictExpectations() {
            {
                // sort out the singletons
                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
            }
         };
         ChildBookingModel.setBookingRequests(managerMock, yw, includeChildEducationSd);
    }

   /**
     * Unit test:
     */
    @Test
    public void test04_BlackBox_NonStrict_NoPrivate_AccountId(@Mocked(methods = {"setBookingRequests"}, inverse=true) final ChildBookingModel model) throws Exception {
        new NonStrictExpectations() {
            {
                // sort out the singletons
                ChildService.getInstance(); returns(childServiceMock);
                childServiceMock.isId(accountId, VT.ACCOUNT); result = true;
                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
            }
         };
         ChildBookingModel.setBookingRequests(managerMock, accountId, yw, includeChildEducationSd);
         new Verifications() {{
             childServiceMock.isId(accountId, VT.ACCOUNT);
         }};
    }

   /**
     * Unit test:
     */
    @Test
    public void test02_BlackBox_NonStrict(@Mocked(methods = {"setBookingRequests","setBookingFromRequests"}, inverse=true) final ChildBookingModel model) throws Exception {
        final Set<Integer> childList = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4));
        new NonStrictExpectations() {
            {
                // sort out the singletons
                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
                requestServiceMock.getBookingRequestRangeManager().getAllKeys();returns(childList); times = 1;
                PropertiesService.getInstance(); returns(propertiesServiceMock);
                ChildService.getInstance(); returns(childServiceMock);
                // needs to be valid else returns false
                requestServiceMock.validateYw(yw);result = yw;
            }
        };
        ChildBookingModel.setBookingRequests(managerMock, yw, includeChildEducationSd);
         new Verifications() {{
             requestServiceMock.getBookingRequest(anyInt, anyInt); times=28;
         }};
    }

    /**
     * Unit test:
     */
    @Test
    public void test03_setBookingRequests(@Mocked(methods = {"setBookingRequests","setBookingFromRequests"}, inverse=true) final ChildBookingModel model) throws Exception {
        final Set<Integer> childList = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4));
        final int yw0 = yw;
        // record
        new NonStrictExpectations() {
            {
                // sort out the singletons
                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
                requestServiceMock.getBookingRequestRangeManager().getAllKeys();returns(childList); times = 1;
                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
                requestServiceMock.validateYw(yw);result = yw; times = 1;

                PropertiesService.getInstance(); returns(propertiesServiceMock);
                propertiesServiceMock.getSystemProperties().isToExceedCapacityWhenInsertingRequests(); result = false; times = 1;

                ChildService.getInstance(); returns(childServiceMock);
                childServiceMock.isAccountSuspended(anyInt); result = false; times=28;

                ChildBookingRequestService.getInstance(); returns(requestServiceMock);
                requestServiceMock.getBookingRequest(anyInt, anyInt); returns(request); times=28;

                request.getRequestId(); result = 1;

                AgeRoomService.getInstance(); returns(roomService);
                roomService.getRoomForChild(anyInt, anyInt).getRoomId(); minTimes = 1;

                ChildBookingService.getInstance(); returns(bookingServiceMock);
                bookingServiceMock.getValidBookingSd(anyInt, anyInt, anyInt, anyInt); returns(1,2,3,4);

                request.getAllSd(); returns(childList);
            }
        };
        // replay
        BookingManager rtnManager = ChildBookingModel.setBookingRequests(managerMock, yw, includeChildEducationSd);
        assertNotNull(rtnManager);

    }
}
