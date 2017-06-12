
package com.oathouse.ccm.cma.booking;

// common imports
import com.oathouse.ccm.cma.VABoolean;
import static com.oathouse.ccm.cma.VABoolean.asSet;
import com.oathouse.ccm.cma.profile.ChildService;
import com.oathouse.ccm.cos.bookings.BTIdBits;
import com.oathouse.ccm.cos.bookings.BookingRequestBean;
import com.oathouse.ccm.cos.profile.ContactBean;
import com.oathouse.oss.storage.objectstore.ObjectDBMS;
import com.oathouse.oss.storage.objectstore.BeanBuilder;
import com.oathouse.oss.storage.objectstore.ObjectBean;
import com.oathouse.oss.server.OssProperties;
import com.oathouse.oss.storage.objectstore.ObjectEnum;
import java.nio.file.Paths;
import java.util.*;
import static java.util.Arrays.*;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


/**
 *
 * @author Darryl
 * @since 9 Jun 2012
 */
public class TestChildBookingRequestService_setGlobalRequestsToAccount {

    private final String owner = ObjectBean.SYSTEM_OWNED;
    private ChildBookingRequestService service;

    private final int accountId = 11;

    @Before
    public void setUp() throws Exception {
        String authority = ObjectBean.SYSTEM_OWNED;
        String rootStorePath = Paths.get("./oss/data").toString();
        OssProperties props = OssProperties.getInstance();
        props.setConnection(OssProperties.Connection.FILE);
        props.setStorePath(rootStorePath);
        props.setAuthority(authority);
        props.setLogConfigFile(Paths.get(rootStorePath + "/conf/oss_log4j.properties").toString());
        // reset
        ObjectDBMS.clearAuthority(authority);
        // global instances
        service = ChildBookingRequestService.getInstance();
        service.clear();

    }

    /*
     *
     */
    @Test
    public void setGlobalRequestsToAccount_noGlobals() throws Exception {
        List<BookingRequestBean> resultList = service.setGlobalRequestsToAccount(accountId, owner);
        assertThat(resultList.isEmpty(),is(true));
    }

    /*
     *
     */
    @Test
    public void setGlobalRequestsToAccount_globalBooking() throws Exception {
        // set the global booking
        service.setGlobalBookingRequest("gname", "glabel", asSet(3,5,7), BTIdBits.ATTENDING_STANDARD, owner);
        final List<ContactBean> contactList = new LinkedList<>();
        contactList.add((ContactBean) BeanBuilder.addBeanValues(new ContactBean()));

        // check there are no BookingRequests for the account
        assertThat(service.getBookingRequestManager().getAllObjects(accountId).isEmpty(), is(true));
        assertThat(service.getGlobalRequestManager().getAllObjects().size(), is(1));
        assertThat(service.getGlobalRequestManager().getAllObjects(ObjectEnum.DEFAULT_KEY.value()).size(), is(1));

        new Expectations() {
            @Mocked private ChildService childService;
            @Mocked({"validateParameters"}) private ChildBookingRequestService mock;
            {
                ChildService.getInstance(); returns(childService);
                childService.getLiableContactsForAccount(accountId); result = contactList;
            }
        };
        List<BookingRequestBean> resultList = service.setGlobalRequestsToAccount(accountId, owner);
        assertThat(resultList.size(),is(1));
        assertThat(service.getBookingRequestManager().getAllObjects(accountId).size(), is(1));
        BookingRequestBean result = service.getBookingRequestManager().getAllObjects(accountId).get(0);
        assertThat(result.getName(), is("gname"));
        assertThat(result.getLabel(), is("glabel"));
        // as the SD is an 'in' value, one should be taken off
        assertThat(result.getAllSd(), is(asSet(2,4,6)));
        assertThat(result.getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));

    }

    /*
     *
     */
    @Test
    public void setGlobalRequestsToAccount_globalBookingAndRequestBooking() throws Exception {
        final List<ContactBean> contactList = new LinkedList<>();
        contactList.add((ContactBean) BeanBuilder.addBeanValues(new ContactBean()));
        new NonStrictExpectations() {
            @Mocked private ChildService childService;
            @Mocked({"validateParameters"}) private ChildBookingRequestService mock;
            {
                ChildService.getInstance(); returns(childService);
                childService.getLiableContactsForAccount(accountId); result = contactList;
            }
        };
        // set the global booking
        service.setGlobalBookingRequest("nOne", "lOne", asSet(3,5,7), BTIdBits.ATTENDING_STANDARD, owner);
        service.setGlobalBookingRequest("nTwo", "lTwo", asSet(2,5,7), BTIdBits.ATTENDING_STANDARD, owner);
        service.setBookingRequest(1, "name", "label", accountId, -1, -1, -1, asSet(3,5,7), BTIdBits.ATTENDING_STANDARD, owner);

        // check all the booking requests are there
        assertThat(service.getBookingRequestManager().getAllObjects(accountId).size(), is(1));
        assertThat(service.getGlobalRequestManager().getAllObjects().size(), is(2));
        assertThat(service.getGlobalRequestManager().getAllObjects(ObjectEnum.DEFAULT_KEY.value()).size(), is(2));

        // set the global bookings to the account
        List<BookingRequestBean> resultList = service.setGlobalRequestsToAccount(accountId, owner);
        assertThat(resultList.size(),is(1));
        //check there are only 2
        assertThat(service.getBookingRequestManager().getAllObjects(accountId).size(), is(2));
        //check the original booking request has not been touched
        BookingRequestBean result = service.getBookingRequestManager().getAllObjects(accountId).get(0);
        assertThat(result.getName(), is("name"));
        assertThat(result.getLabel(), is("label"));
        // as the SD is an 'in' value, one should be taken off
        assertThat(result.getAllSd(), is(asSet(2,4,6)));
        assertThat(result.getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));

        //check the extra one is the new global
        result = service.getBookingRequestManager().getAllObjects(accountId).get(1);
        assertThat(result.getName(), is("nTwo"));
        assertThat(result.getLabel(), is("lTwo"));
        // as the SD is an 'in' value, one should be taken off
        assertThat(result.getAllSd(), is(asSet(1,4,6)));
        assertThat(result.getBookingTypeId(), is(BTIdBits.ATTENDING_STANDARD));

    }

    /*
     *
     */
    @Test
    public void setGlobalRequestsToAccount_globalBookingAndRequestBookingSameName() throws Exception {
        final List<ContactBean> contactList = new LinkedList<>();
        contactList.add((ContactBean) BeanBuilder.addBeanValues(new ContactBean()));
        new NonStrictExpectations() {
            @Mocked private ChildService childService;
            @Mocked({"validateParameters"}) private ChildBookingRequestService mock;
            {
                ChildService.getInstance(); returns(childService);
                childService.getLiableContactsForAccount(accountId); result = contactList;
            }
        };
        // set the global booking
        service.setGlobalBookingRequest("name", "label", asSet(3,5,7), BTIdBits.ATTENDING_STANDARD, owner);
        service.setBookingRequest(1, "name", "label", accountId, -1, -1, -1, asSet(4,6,7), BTIdBits.ATTENDING_STANDARD, owner);

        List<BookingRequestBean> resultList = service.setGlobalRequestsToAccount(accountId, owner);
        assertThat(resultList.size(),is(1));
        assertThat(service.getBookingRequestManager().getAllObjects(accountId).size(), is(2));

        BookingRequestBean result = service.getBookingRequestManager().getAllObjects(accountId).get(1);
        assertThat(result.getName(), is("name (Global)"));

    }

}
