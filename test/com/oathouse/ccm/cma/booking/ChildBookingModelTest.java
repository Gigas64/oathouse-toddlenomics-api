package com.oathouse.ccm.cma.booking;

import org.junit.runner.*;
import org.junit.runners.*;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.oathouse.ccm.cma.booking.TestChildBookingModel_getModelBookingManager.class,
    com.oathouse.ccm.cma.booking.TestChildBookingModel_SetBookingRequests.class,
    com.oathouse.ccm.cma.booking.TestChildBookingModel_SetLiveBookings.class})
public class ChildBookingModelTest {

}
