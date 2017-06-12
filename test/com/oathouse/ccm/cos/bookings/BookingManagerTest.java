package com.oathouse.ccm.cos.bookings;

import org.junit.runner.*;
import org.junit.runners.*;

/**
 *
 * @author Darryl Oatridge
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestBookingManager_BookingBeanTest.class,
    TestBookingManager_CleanStateAndActuals.class,
    TestBookingManager_LegacyTestMethods.class,
    TestBookingManager_getAllFilteredObjectsForYwd.class,
})
public class BookingManagerTest {


}
