package com.oathouse.ccm.cma.booking;

import com.oathouse.ccm.builders.BSBuilder;
import com.oathouse.ccm.builders.RSBuilder;
import com.oathouse.ccm.builders.TSBuilder;
import com.oathouse.ccm.cos.profile.ChildBean;
import com.oathouse.ccm.cos.profile.RelationType;
import com.oathouse.ccm.cos.bookings.BookingRequestBean;
import com.oathouse.ccm.cos.bookings.BTFlagIdBits;
import com.oathouse.ccm.cma.ServicePool;
import com.oathouse.ccm.cos.bookings.BookingBean;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import static com.oathouse.ccm.cma.VABoolean.asSet;
import com.oathouse.oss.storage.objectstore.*;
import com.oathouse.oss.storage.valueholder.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import org.junit.*;
/**
 *
 * @author Darryl Oatridge
 */
public class TestChildBookingRequestService_LegacyTests {

    private ServicePool engine;
    private ChildBookingRequestService requestService;
    private String owner = BookingBean.SYSTEM_OWNED;
    private int roomId;
    private int accountId1;
    private int accountId2;
    private int accountId3;
    private int childId11;
    private int childId21;
    private int childId22;
    private int childId23;
    private int childId31;
    private int childId32;
    private int contactId1;
    private int contactId2;
    private int contactId3;

    @Before
    public void setUp() throws Exception {
        engine = BSBuilder.getEngine();
        assertTrue(engine.clearAll());
        // create shorter reference name to ChildBookingRequestService
        requestService = engine.getChildBookingRequestService();
        // set up nursery
        RSBuilder.setupSystemProperties();
        RSBuilder.setupAccountHolder();
        RSBuilder.setupHolidayConcession();
        roomId = RSBuilder.setupRoom();
        TSBuilder.setupTimetables(roomId);
        //set up two accounts
        contactId1 = BSBuilder.setupContact().getContactId();
        accountId1 = BSBuilder.setupAccount("oat00tst", contactId1).getAccountId();
        contactId2 = BSBuilder.setupContact().getContactId();
        accountId2 = BSBuilder.setupAccount("oat01tst", contactId2).getAccountId();
        contactId3 = BSBuilder.setupContact().getContactId();
        accountId3 = BSBuilder.setupAccount("oat02tst", contactId3).getAccountId();

        // set children for account 1
        childId11 = setChild(accountId1, (2 * 52) + 13).getChildId();
        childId21 = setChild(accountId2, (2 * 52) + 26).getChildId();
        childId22 = setChild(accountId2, (4 * 52) + 31).getChildId();
        childId23 = setChild(accountId2, (4 * 52) + 31).getChildId();
        childId31 = setChild(accountId3, (3 * 52) + 1).getChildId();
        childId32 = setChild(accountId3, (4 * 52) + 18).getChildId();
        // set custodians
        engine.getChildService().setCustodialRelationship(childId11, contactId1, RelationType.FATHERS_PARTNER, owner);
        engine.getChildService().setCustodialRelationship(childId21, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId22, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId23, contactId2, RelationType.GRANDPARENT, owner);
        engine.getChildService().setCustodialRelationship(childId31, contactId3, RelationType.AUNT, owner);
        engine.getChildService().setCustodialRelationship(childId32, contactId3, RelationType.AUNT, owner);
        // set child room start
        int startYwd = CalendarStatic.getRelativeYW(-1);
        engine.getAgeRoomService().setChildRoomStartYwd(childId11, roomId, startYwd, owner);
        engine.getAgeRoomService().setChildRoomStartYwd(childId21, roomId, startYwd, owner);
        engine.getAgeRoomService().setChildRoomStartYwd(childId22, roomId, startYwd, owner);
        engine.getAgeRoomService().setChildRoomStartYwd(childId23, roomId, startYwd, owner);
        engine.getAgeRoomService().setChildRoomStartYwd(childId31, roomId, startYwd, owner);
        engine.getAgeRoomService().setChildRoomStartYwd(childId32, roomId, startYwd, owner);


    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * System test: Simple test to check the empty booking, make sure everything starts OK and the basic calls work
     */
    @Test
    public void system01_ServiceStartupTest() throws Exception {
        int StartYwd = CalendarStatic.getRelativeYW(0);
        int EndYwd = CalendarStatic.getRelativeYW(0) + 7;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;
        Set<Integer> requestSdSet = asSet(SDHolder.getSD(20, 19));

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdSet, bookingTypeId, owner);
        // test
        assertEquals(request, requestService.getBookingRequestManager().getObject(accountId1, "BR1"));
        assertEquals(request, requestService.getBookingRequestManager().getObject(request.getRequestId()));

        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        //test
        assertEquals(7, requestService.getBookingRequestsForWeekChild(childId11, StartYwd).size());
        assertEquals(request, requestService.getBookingRequestsForWeekChild(childId11, StartYwd).get(0));
        assertEquals(requestService.getBookingRequestManager().getDefaultObject(), requestService.getBookingRequestsForWeekChild(childId11, StartYwd).get(1));
    }

    /**
     * System test:
     */
    @Test
    public void system02_CreateBookingFromRequest() throws Exception {

        int StartYwd = CalendarStatic.getRelativeYW(0);
        int EndYwd = CalendarStatic.getRelativeYW(0) + 7;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int yw = CalendarStatic.getRelativeYW(0);
        int ywd = CalendarStatic.getRelativeYWD(0);
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;

        ConcurrentSkipListSet<Integer> requestSdInSet = new ConcurrentSkipListSet<>();
        requestSdInSet.add(SDHolder.getSD(20, 20));

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdInSet, bookingTypeId, owner);
        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        // test
        List<BookingExceptionDTO> exceptions = requestService.setYwBookingRequests(yw, false, owner);
        assertEquals(new LinkedList<BookingExceptionDTO>(), exceptions);

        List<BookingBean> bookingList = engine.getChildBookingService().getYwBookings(yw, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG);
        assertEquals(1, bookingList.size());
        assertEquals(200019, bookingList.get(0).getBookingSd());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit01_CreateInvalidSdRequest() throws Exception {
        int StartYwd = CalendarStatic.getRelativeYW(0);
        int EndYwd = CalendarStatic.getRelativeYW(0) + 7;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int yw = CalendarStatic.getRelativeYW(0);
        int ywd = CalendarStatic.getRelativeYWD(0);
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;

        Set<Integer> requestSdSet = asSet(SDHolder.getSD(20, 21));

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdSet, bookingTypeId, owner);
        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        // test
        List<BookingExceptionDTO> exceptions = requestService.setYwBookingRequests(yw, false, owner);
        assertEquals(1,exceptions.size());
        assertEquals(SDHolder.getSD(20, 20), exceptions.get(0).getPreBookingSd());
        assertEquals(SDHolder.getSD(20, 24), exceptions.get(0).getPostBookingSd());
        List<BookingBean> bookingList = engine.getChildBookingService().getYwBookings(yw, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG);
        assertEquals(1, bookingList.size());
        assertEquals(SDHolder.getSD(20, 24), bookingList.get(0).getBookingSd());
    }
    /**
     * Unit test:
     */
    @Test
    public void unit01_CreateClosedDay() throws Exception {
        int StartYwd = CalendarStatic.getRelativeYW(0);
        int EndYwd = CalendarStatic.getRelativeYW(0) + 6;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.SAT_ONLY);
        int yw = CalendarStatic.getRelativeYW(0);
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;

        Set<Integer> requestSdSet = asSet(SDHolder.getSD(10, 20));

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdSet, bookingTypeId, owner);
        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        // test
        List<BookingExceptionDTO> exceptions = requestService.setYwBookingRequests(yw, false, owner);
        assertEquals(2,exceptions.size());
        assertEquals(SDHolder.getSD(10, 19), exceptions.get(0).getPreBookingSd());
        assertEquals(-1, exceptions.get(0).getPostBookingSd());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit02_CreateRequestWhenBookingExists() throws Exception {
        int StartYwd = CalendarStatic.getRelativeYW(1);
        int EndYwd = CalendarStatic.getRelativeYW(1) + 7;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int yw = CalendarStatic.getRelativeYW(1);
        int ywd = yw;
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;

        Set<Integer> requestSdInSet = asSet(SDHolder.getSD(20, 20));
        int bookingSd = SDHolder.getSD(10, 29);

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdInSet, bookingTypeId, owner);
        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        // create one booking
        BookingBean booking1 = engine.getChildBookingService().setBooking(ywd, roomId, bookingSd, childId11,
                contactId1, -1, contactId1, BTIdBits.ATTENDING_STANDARD, "", ywd, owner);
        // test
        List<BookingExceptionDTO> exceptions = requestService.setYwBookingRequests(yw, false, owner);
        assertEquals(1,exceptions.size());
        assertEquals(SDHolder.getSD(20, 19), exceptions.get(0).getPreBookingSd());
        assertEquals(SDHolder.getSD(10, 29), exceptions.get(0).getPostBookingSd());
        assertEquals(booking1.getBookingId(), exceptions.get(0).getBookingId());
        List<BookingBean> bookingList = engine.getChildBookingService().getYwBookings(yw, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG);
        assertEquals(1, bookingList.size());
        assertEquals(booking1.getBookingId(), bookingList.get(0).getBookingId());
    }

    /**
     * Unit test:
     */
    @Test
    public void unit02_setAllBookingRequestsForChild() throws Exception {
        int StartYwd = CalendarStatic.getRelativeYW(1);
        int EndYwd = CalendarStatic.getRelativeYW(5) + 7;
        boolean[] days = BeanBuilder.getDays(BeanBuilder.MON_ONLY);
        int yw = CalendarStatic.getRelativeYW(1);
        int ywd = yw;
        int bookingTypeId = BTIdBits.ATTENDING_STANDARD;

        Set<Integer> requestSdSet = asSet(SDHolder.getSD(20, 20));

        // create the request
        BookingRequestBean request = requestService.createBookingRequest("BR1", "", accountId1, contactId1, contactId1, contactId1, requestSdSet, bookingTypeId, owner);
        // create the dayRange
        requestService.createBookingRequestDayRange(childId11, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        requestService.createBookingRequestDayRange(childId21, request.getRequestId(), StartYwd, EndYwd, "", days, owner);
        // test
        List<BookingExceptionDTO> exceptions = requestService.setAllBookingRequestsForChild(childId11, false, owner);
        assertEquals(new LinkedList<BookingExceptionDTO>(), exceptions);

        // this should have created 5 Bookings
        List<BookingBean> bookingList = engine.getChildBookingService().getBookingManager().getAllObjects();
        assertEquals(5, bookingList.size());
        // there should only be 1 in a week
        int testYw = CalendarStatic.getRelativeYW(3);
        bookingList = engine.getChildBookingService().getYwBookings(testYw, BTIdBits.TYPE_IDENTIFIER, BTFlagIdBits.TYPE_FLAG);
        assertEquals(1, bookingList.size());
        assertEquals(childId11, bookingList.get(0).getProfileId());

    }


    /*
     * Private method to set up child
     */
    private ChildBean setChild(int accountId, int age) throws Exception {
        int departIn = (7 * 52) - age;

        int dob = CalendarStatic.getRelativeYW(-age);
        int departYwd = CalendarStatic.getRelativeYW(departIn);


        return BSBuilder.setupChild(accountId, dob, departYwd);
    }
}
